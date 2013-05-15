domaintypes
===========

Modeling domain data on the basis of Clojure records.

 - [core](core) provides the macros for defining 
   simple types and complex types, plus some functionality for
   validation.

 - [lein-domaintypesdoc](lein-domaintypesdoc) is a leiningen plugin 
   that generates PlantUML class diagram and PNG image from namespaces containing
   domain types.

 - [samples](samples) contains a demonstration how the macros are used.
 

A sample model would be described like this

    (defsimpletype required-string "A non-blank string" string? #(> (count %) 0))
    (defsimpletype task-status "Status of a Task" #{:new :in-progress :done})

    (defcomplextype Member [name {:dt required-string}])

    (defcomplextype Team [members {:dt Member :card [1 any]}])

    (defcomplextype Task [description {:dt required-string}
                          status      {:dt task-status}
                          assignee    {:dt Member :nillable true}])

    (defcomplextype Sprint [name  {:dt required-string}
                            tasks {:dt Task :card [0 any]}])


A corresponding graphical overview of complex types looks like this

![UML diagram of complextypes](domaintypes.png)

The image file in the samples/target dir is created by the command

    $ lein domaintypesdoc
