(defproject domaintypes/lein-domaintypesdoc "1.0.1"
  :description "Creating documentation for domain data types (a.k.a records)."
  :url "https://github.com/friemen/domaintypes"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :eval-in-leiningen true
  :dependencies [[net.sourceforge.plantuml/plantuml "7965"]
                 [domaintypes/core "1.0.1"]]
  :scm {:name "git"
         :url "https://github.com/friemen/domaintypes/lein-domaintypes"}
  :repositories [["clojars" {:url "https://clojars.org/repo"
                             :creds :gpg}]])
