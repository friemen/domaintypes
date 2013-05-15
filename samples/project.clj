(defproject domaintypes/samples "1.0.0"
  :description "Samples for domain types."
  :url "https://github.com/friemen/domaintypes"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [domaintypes/core "1.0.0"]]
  :plugins [[domaintypes/lein-domaintypesdoc "1.0.0"]]
  :domaintypesdoc-for [samples.projectmanagement])
