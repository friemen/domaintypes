domaintypes
===========

Defining domain data types in Clojure on the basis of records.

Concepts
--------
A _constraint function_ is a predicate. If the predicate returns true for
its arguments the arguments are considered valid.

A _simple type_ is a map that describes a data dictionary item.
The map contains:

    :doc           A human readable description of the domain value type.
    :constraints   A vector with constraint functions that are appied to a
                   corresponding value.

A _complex type_ is a record and an additional var that points to a map with keys:

    :constraints   Vector of constraint functions that apply to whole
                   instances of the record.
    :fields        A map from keywords to field metadata maps (see below).

The metadata map for each field contains:

    :dt            Reference to record class or a map representing a domain type
    :nillable      True, if nil is a valid value for field
    :constraints   Vector of constraint functions that are applied to value of the field
    :card          Pair of min + max occurences if the field references a collection

Syntax
------
The domaintypes.core namespace offers two macros and the valid? function:

    (defsimpletype <sym> <description> <constraint-fn>*) 
	
creates a new var named `<sym>`, which points to a map that describes a simple domain type.

    (defcomplextype <sym> [<sym1> <metadata-map1>
	                       ...
	                       <symN> <metadata-mapN>] 
						   <constraint-fn>*)

creates a complex domain type named `<sym>`, in essence a record and a var `metadata-for-<sym>` 
pointing to a metadata map.

    (valid? <record-instance>)

returns true if the instance of a record matches all constraints.
The constraints are a set of

 - Functions attached to simple types.
 - Functions implicitly derived from field metadata map.
 - Functions attached to a field using the :constraints key in the metadata map.
 - Functions attached to complex types that take the whole record instance.
 
