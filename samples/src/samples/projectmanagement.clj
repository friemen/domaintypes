(ns samples.projectmanagement
  (:use [domaintypes.core]))

(defsimpletype required-string "A non-blank string" string? #(> (count %) 0))
(defsimpletype task-status "Status of a Task" #{:new :in-progress :done})

(defcomplextype Member [name {:dt required-string}])

(defcomplextype Team [members {:dt Member :card [1 any]}])

(defcomplextype Task [description {:dt required-string}
                      status      {:dt task-status}
                      assignee    {:dt Member :nillable true}])

(defcomplextype Sprint [name  {:dt required-string}
                        tasks {:dt Task :card [0 any]}])
