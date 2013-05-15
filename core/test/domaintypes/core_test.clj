(ns domaintypes.core-test
  (:use [domaintypes.core]
        [clojure.test]))

;; Sample domain types and a record definition

(defsimpletype km-distance "Non-negative distance in KM" number? (partial <= 0))
(defsimpletype required-string "A mandatory text of any length > 0" string? #(> (count %) 0))

(defcomplextype Trip [dist      {:dt km-distance}
                      from-city {:dt required-string}
                      to-city   {:dt required-string}
                      reason    {:dt any :nillable false}]
  #(not= (:from-city %) (:to-city %)))

(defcomplextype Expenses [trips {:dt Trip :card [0 2]}])

(defrecord SimpleRecord [foo bar])

;; Tests 

(deftest ns-and-simplename-test
  (is (= ["domaintypes.core-test" "SimpleRecord"]
         (ns-and-simplename SimpleRecord)))
  (is (= ["java.lang" "String"]
         (ns-and-simplename java.lang.String))))

(deftest record-constraints-test
  (is (= 0 (count (record-constraints Expenses))))
  (is (= 1 (count (record-constraints Trip)))))

(deftest basis-test
  (is (= ['dist 'from-city 'to-city 'reason]
         (basis Trip)))
  (is (= ['foo 'bar]
         (basis SimpleRecord))))

(deftest to-one-test
  (are [r    card] (= r (to-one? card))
       true  nil
       true  [1 1]
       false [1 2]
       false [0 3]))

(deftest valid-data
  (is (valid? (Trip. 20.0 "Bonn" "Cologne" "")))
  (is (valid? (Expenses. [(Trip. 20.0 "Bonn" "Cologne" "")
                          (Trip. 50.0 "Cologne" "Aachen" "")]))))

(deftest invalid-data
  (is (not (valid? (Trip. 20.0 "Bonn" "Bonn" ""))))
  (is (not (valid? (Expenses. [(Trip. 20.0 "Bonn" "Cologne" nil)]))))
  (is (not (valid? (Expenses. [(Trip. 20.0 "Bonn" "Cologne" "")
                               (Trip. 50.0 "Cologne" "Aachen" "")
                               (Trip. 30.0 "Aachen" "Luettich" "")]))))
  (is (not (valid? (Expenses. ["FooBar"])))))

(deftest record-of-ns-test
  (is (record-of-ns? 'domaintypes.core-test Trip))
  (is (not (record-of-ns? 'domaintypes.core-test java.lang.String))))

(deftest ns-records-test
  (is (= #{Trip Expenses SimpleRecord}
         (set (ns-records 'domaintypes.core-test)))))

(deftest describe-test
  (let [d (describe Trip)]
    (is (= ["dist" "from-city" "to-city" "reason"] (map :attr-name d)))
    (is (= km-distance (-> d first :dt)))))

;; (valid? (Trip. 20.0 "Bonn" "Cologne" "")) => true
;; (valid? (Trip. 0 "Bonn" "Cologne" "")) => true
;; (valid? (Expenses. [(Trip. 20.0 "Bonn" "Cologne" "")])) => true




