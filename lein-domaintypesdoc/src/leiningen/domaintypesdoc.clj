(ns leiningen.domaintypesdoc
  (:require [clojure.java.io :as io])
  (:use [domaintypes.core]
        [clojure.string :only [join split]]
        [leiningen.core.eval :only [eval-in-project]])
  (:import [net.sourceforge.plantuml SourceStringReader]))


(defn- umlfilename
  [project]
  (str (:target-path project) "/domaintypes.uml"))

(defn- produce-plantuml
  [project]
  (let [nss (->> project :domaintypesdoc-for (map str) vec)
        umlfile (umlfilename project)]
    (eval-in-project
     project
     `(spit ~umlfile (apply domaintypes.uml/umlstr (map symbol ~nss)))
     `(do (require 'domaintypes.uml)
        (doseq [ns# (map symbol ~nss)] (require ns#)))))) 

(defn- generate-png
  [project]
  (let [umlfile (umlfilename project)
        pngfile (io/file (:target-path project) "domaintypes.png")
        pnggenerator (SourceStringReader. (slurp umlfile))]
      (with-open [os (io/output-stream pngfile)]
        (.generateImage pnggenerator os))))


(defn domaintypesdoc
  "Creates domaintypes UML diagram for namespaces referenced by :domaintypesdoc-for."
  [project]
  (produce-plantuml project)
  (generate-png project))


