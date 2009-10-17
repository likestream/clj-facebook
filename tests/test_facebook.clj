(ns test-facebook
  (:require [clojure.test :as t])
  (:gen-class))

;; Shamefully stolen from the Clojure tests.

(def test-namespaces
     ['uk.co.holygoat.util.md5-test
      'com.twinql.clojure.facebook.api-test
      'com.twinql.clojure.facebook.sessions-test
      'com.twinql.clojure.facebook.sig-test
      'com.twinql.clojure.facebook.compojure-test
      'com.twinql.clojure.facebook.handlers-test])

(defn run []
  (println "Loading tests...")
  (apply require :reload-all test-namespaces)
  (apply t/run-tests test-namespaces))
 
(defn run-ant []
  (let [rpt t/report]
    (binding [;; binding to *err* because, in ant, when the test target
              ;; runs after compile-clojure, *out* doesn't print anything
              *out* *err*
              t/*test-out* *err*
              t/report (fn report [m]
                         (if (= :summary (:type m))
                           (do (rpt m)
                               (if (or (pos? (:fail m)) (pos? (:error m)))
                                 (throw (new Exception (str (:fail m) " failures, " (:error m) " errors.")))))
                           (rpt m)))]
      (run))))
 
(defn -main
  "Run all defined tests from the command line"
  [& args]
  (run)
  (System/exit 0))
