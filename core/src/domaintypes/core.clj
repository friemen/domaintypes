(ns domaintypes.core
  (:use [clojure.string :only [join split]]))

;;
;; Concepts:
;; A constraint function is a predicate. If the predicate returns true for
;; its arguments the arguments are considered valid.
;;
;; A simple type is a map that describes a data dictionary item.
;; The map contains:
;;   :doc           A human readable description of the domain value type.
;;   :constraints   A vector with constraint functions that are appied to a
;;                  corresponding value.
;;
;; A complex type is a record and an additional var that points to a map with keys
;;   :constraints   Vector of constraint functions that apply to whole
;;                  instances of the record.
;;   :fields        A map from keywords to field metadata maps (see below).
;;
;; The metadata map for each field contains:
;;   :dt            Reference to record class or a map representing a domain type
;;   :nillable      True, if nil is a valid value for field
;;   :constraints   Vector of constraint functions that are applied to value of the field
;;   :card          Pair of min + max occurences if the field references a collection
;;
;; This namespace offers two macros and the valid? function.
;;   defsimpletype creates a new var pointing to a map that describes a simple domain type.
;;   defcomplextype creates a complex domain type, in essence a record with metadata.
;;   valid? returns true if the instance of a record matches all constraints.
;;

;; Utilities

(defn ns-and-simplename
  "Returns a pair of strings, first is the namespace name, second is the simple type name of a class."
  [record-class]
  (let [classname-parts (split (.getName record-class) #"\u002E")
        simple-name (last classname-parts)
        ns-name (.replaceAll (join "." (drop-last 1 classname-parts)) "_" "-")]
    [ns-name simple-name]))


;; Macros to define simple and complex types

(defmacro defsimpletype
  "Defines a symbol for a map that represents a primitive, single-valued domain type."
  [sym doc & constraints]
  `(def ~sym {:name ~(str sym) :doc ~doc :constraints ~(into [] constraints) }))


(defn- metamap
  "Returns a map {:fields symbol->metadata-map, :constraints constraints-vector}
   from a seq of field specs where each symbol is followed by a map containing metadata for the symbol
   and a seq of constraint functions."
  [field-specs constraints]
  {:fields (->> field-specs (partition 2) (map (fn [[sym info]] [(keyword sym) info])) (into {}))
   :constraints (vec constraints)})

(defn- metadata-symbol
  [name]
  (symbol (str "metadata-for-" name)))

(defn metadata
  "Returns the metadata for a record, or nil if the record has no metadata."
  [record-class]
  (let [[ns-name simple-name] (ns-and-simplename record-class)
        ns (the-ns (symbol ns-name))
        var (get (ns-publics ns) (metadata-symbol simple-name))]
    (if (nil? var) nil (var-get var))))

(defmacro defcomplextype
  "Defines a record and a var that contains the metadata as map for each field symbol
   and constraint functions that are applied to validate a record instance as a whole."
  [sym field-specs & constraints]
  (let [metadata (metamap field-specs constraints)]
    `(do (def ~(metadata-symbol sym) ~metadata)
         (defrecord ~sym ~(->> metadata :fields keys (map name) (map symbol) vec)))))

(defsimpletype any "Any type is allowed.")


;; Infrastructure for defining constraints and validating record instances

(defn record-constraints
  "Returns a seq of constraints that apply to an instance of the given record class as a whole."
  [record-class]
  (:constraints (metadata record-class)))

(defn basis
  "Reflectively determines and returns the vector of all symbols of the given record class."
  [record-class]
  (let [m (first (filter #(= "getBasis" (.getName %)) (.getMethods record-class)))]
    (.invoke m nil (object-array 0))))

(defn to-one?
  "Returns true, if the pair of min and max values that describes the
   cardinality of a field is either nil or both values are set to 1."
  [card]
  (or (nil? card) (and (= 1 (first card)) (= 1 (second card)))))

(declare valid?)


(defn- collection-constraints
  "Returns a vector of predicates that result from the cardinality and the specified type."
  [card type]
  (let [[min max] card]
    [#(coll? %)
     #(<= min (count %))
     #(or (= any max) (<= (count %) max))
     #(reduce (fn [result item] (and
                                 result
                                 (instance? type item)
                                 (valid? item)))
              true %)]))


(defn- symbol-constraints-pair
  "Returns a pair [sym {:nillable true/false, :predicates [predicates]}] for the specified
   symbol sym. The predicates the union of the fields constraints, the constraints of the
   declared type plus the constraints that result from the specified cardinality."
  [metadata key]
  (let [{type :dt, nillable :nillable, constraints :constraints, card :card} (-> metadata :fields key)]
    [key {:nillable nillable
          :predicates (concat constraints
                              (if (to-one? card)
                                (cond
                                 (class? type) [#(instance? type %) #(valid? %)]
                                 (map? type) (:constraints type))
                                (collection-constraints card type)))}]))


(defn- field-constraints
  "Returns a map {sym -> {:nillable true/false, :predicates [predicates]}}
   for all symbols of the given record."
  [record-class]
  (->> record-class
       basis
       (map keyword)
       (map symbol-constraints-pair (repeat (metadata record-class)))
       (into {})))


(defn- single-values-valid?
  "Returns true if all field values conform to their respective constraints."
  [record]
  (reduce (fn [result [key {nillable :nillable, predicates :predicates}]]
            (and result (if-let [value (get record key)]
                          (reduce (fn [result p] (and result (p value))) true predicates)
                          (true? nillable))))
          true
          (field-constraints (type record))))


(defn- tuples-valid?
  "Returns true if the record conforms to all constraints defined on complex type level."
  [record]
  (reduce (fn [result p]
            (and result (p record)))
          true
          (record-constraints (type record))))


(defn valid?
  "Returns true if all values of the given record adhere to the constraints of
   the records symbols."
  [record]
  (and (single-values-valid? record)
       (tuples-valid? record)))


;; Functions to find and describe record classes in a namespace

(defn record-of-ns?
  "Returns true, if the record-class belongs to the namespace ns."
  [ns record-class]
  (let [ns-name (str ns)]
    (= ns-name (first (ns-and-simplename record-class)))))

(defn ns-records
  "Returns a seq of records that are part of the given namespace."
  [ns]
  (->> ns ns-map vals
       (filter class?)
       (filter (partial record-of-ns? ns))
       (sort #(compare (.getSimpleName %1) (.getSimpleName %2)))))

(defn describe
  "Returns a seq of maps, where each map describes a field of a record.
   The keys/values are the same that have been passed as metadata with
   fields in the record definition. Additionally the key :attr-name exists
   that references the field name as string."
  [record-class]
  (if-let [fields-metadata (-> record-class metadata :fields)]
    (->> record-class
         basis
         (map (fn [sym] (assoc (fields-metadata (keyword sym)) :attr-name (str sym)))))
    (->> record-class
         basis
         (map (fn [sym] {:attr-name (str sym)})))))

(defn attrs-and-assocs
  "Returns a pair of attributes and associations. If a field has as
   :dt value a record-class then the field description is returned as
   association, otherwise as attribute."
  [record-class]
  (let [grouping (->> record-class
                      describe
                      (group-by #(-> % :dt class?)))]
    [(grouping false) (grouping true)]))


