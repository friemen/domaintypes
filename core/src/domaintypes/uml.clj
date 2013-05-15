(ns domaintypes.uml
  (:use [domaintypes.core]
        [clojure.string :only [join split]]))

;; Functions to create PlantUML compatible, printable strings from record classes

(defn- cardstr
  ([card]
     (cardstr card "" ""))
  ([card prefix suffix]
  (if (to-one? card)
    ""
    (str prefix
         (let [[min max] card]
           (str min ".." (if (= any max) "*" max)))
         suffix))))

(defn- assocstr
  "Returns a PlantUML string for an association between classes."
  [record-name desc]
  (let [{name :attr-name dt :dt card :card} desc]
      (str record-name
           " o-- \""
           (cardstr card)
           " " name "\" " (.getSimpleName dt))))

(defn- attrstr
  "Returns a PlantUML string for an attribute of a class."
  [desc]
  (let [{name :attr-name dt :dt card :card} desc]
      (str name
           (if dt (str " : " (:name dt)) "")
           (cardstr card " [" "]"))))

(defn recordstr
  "Returns a PlantUML string that contains the class and all its associations."
  [record-class]
  (let [record-name (second (ns-and-simplename record-class))
        [attrs assocs] (attrs-and-assocs record-class)]
    (str (->> assocs
              (map (partial assocstr record-name))
              (clojure.string/join "\n"))
         "\n"
         "class " record-name " {"
         (->> attrs
              (map attrstr)
              (map #(.concat "\n  " %))
              (apply str))
         "\n}\n")))

(defn nsstr
  "Returns the concatenated PlantUML strings for all records of the given namespace."
  [ns]
  (->> ns ns-records (map recordstr) (apply str)))

(defn umlstr
  "Returns the concatenated PlantUML strings for all records of all namespaces."
  [& nss]
  (str "@startuml\n"
       (->> nss (map nsstr) (apply str))
       "\n@enduml"))

