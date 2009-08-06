(ns com.twinql.clojure.facebook.api
  (:refer-clojure)
  (:require [org.danlarkin.json :as json])
  ;; Even sessionless requests still need
  ;; version, api_key, etc...
  (:use com.twinql.clojure.facebook.util)
  (:use com.twinql.clojure.facebook.errors)
  (:use com.twinql.clojure.facebook.request)
  (:use com.twinql.clojure.facebook.sessions))

(defn- args->arglist [args]
  (map first args))

(defn- args->map [args]
  (into {} (map (fn [[arg key val-trans]]
                  [key (if val-trans
                         (list val-trans arg)
                         arg)])
                args)))

(defn- optional-args->assoc-form [input args]
  (let [arg (first args)]
    (if arg
      (let [[var key val-trans] arg
            val-gen (gensym)]
        `(let [~val-gen ~input
               y# ~var]
           (if y#
             (assoc ~(optional-args->assoc-form
                       val-gen (rest args))
                    ~key (~(or val-trans identity) y#))
             ~val-gen)))
      input)))
         
;; Rough edge: these generated functions currently have a fixed
;; arglist, so no optionals, no keyword parameters. Etc.
;; Also, this looks damn ugly. Sorry.
(defmacro def-fb-api-call [name method & opt]
  (let [{:keys [docstring required optional other-args other-map]}
        (apply hash-map opt)]
    `(defn ~name ~docstring
       ~(vec (concat (args->arglist required)
                     other-args
                     (args->arglist optional)  ; Not really optional, just nil.
                     ))
       (response->content
         (make-facebook-request
           ~(optional-args->assoc-form
              (assoc
                (merge (args->map required)
                       other-map)
                :method method
                :format "JSON")
              optional))))))
