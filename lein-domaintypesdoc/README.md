lein-domaintypesdoc
===================

A Leiningen plugin to create documentation for domain types (a.k.a records).

Usage
-----

Put `[domaintypes/lein-domaintypesdoc "1.0.1"]` into the `:plugins` vector of your project.clj.
Add a key-value pair to project.clj that defines the namespaces the plugin will
include in the UML class diagram.

    :domaintypesdoc-for [ns1 ns2 ...]

See also the [sample project](../sample/project.clj).
    
To run the plugin type

    $ lein domaintypesdoc

