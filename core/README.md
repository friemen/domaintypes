domaintypes
===========

Defining domain data types in Clojure on the basis of records.


Concepts
--------
A constraint function is a predicate. If the predicate returns true for
its arguments the arguments are considered valid.

A simple type is a map that describes a data dictionary item.
The map contains:

    :doc           A human readable description of the domain value type.
    :constraints   A vector with constraint functions that are appied to a
                  corresponding value.

A complex type is a record and an additional var that points to a map with keys:

    :constraints   Vector of constraint functions that apply to whole
                   instances of the record.
    :fields        A map from keywords to field metadata maps (see below).

The metadata map for each field contains:

    :dt            Reference to record class or a map representing a domain type
    :nillable      True, if nil is a valid value for field
    :constraints   Vector of constraint functions that are applied to value of the field
    :card          Pair of min + max occurences if the field references a collection

This namespace offers two macros and the valid? function.
 - defsimpletype creates a new var pointing to a map that describes a simple domain type.
 - defcomplextype creates a complex domain type, in essence a record with metadata.
 - valid? returns true if the instance of a record matches all constraints.

