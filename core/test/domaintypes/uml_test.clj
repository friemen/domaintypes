(ns domaintypes.uml-test
  (:use [domaintypes core core-test uml]
        [clojure.test])
  )


(def expected-umlstr
"@startuml
Expenses o-- \"0..2 trips\" Trip
class Expenses {
}

class SimpleRecord {
  foo
  bar
}

class Trip {
  dist : km-distance
  from-city : required-string
  to-city : required-string
  reason : any
}

@enduml")

(deftest umlstr-test
  (is (= expected-umlstr (umlstr 'domaintypes.core-test))))

