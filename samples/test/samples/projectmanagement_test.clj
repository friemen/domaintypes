(ns samples.projectmanagement-test
  (:use [clojure.test]
        [samples.projectmanagement]
        [domaintypes.core :only [valid?]])
  (:import [samples.projectmanagement Member Team Task Sprint]))

(deftest member-test
  (is (valid? (Member. "Foo")))
  (are [name] (not (valid? (Member. name)))
       ""
       nil))

(deftest team-test
  (is (valid? (Team. [(Member. "Foo")])))
  (are [members] (not (valid? (Team. members)))
       nil
       []))

(deftest task-test
  (is (valid? (Task. "Foo" :new (Member. "Bar"))))
  (are [desc status assignee] (not (valid? (Task. desc status assignee)))
       "" :new nil
       "Foo" :baz (Member. "Bar")))
