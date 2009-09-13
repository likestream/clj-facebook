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

(defn- optional-args->assoc-form
  "Generate a form which associates the args into a map if they are 
  non-nil. Used to allow nil optional arguments without sending them
  as part of a request."
  [input args]
  
  ;; Can't just use the multiple value version of assoc: we don't want
  ;; to associate if the value is nil.
  ;; At least we can use transients to make this a little faster.
  (list 'persistent!
        (loop [arg (first args)
               res (rest args)
               in  (list 'transient input)]
          (if arg
            (let [[var key val-trans] arg
                  g (gensym)]
              (recur (first res)
                     (rest res)
                     `(let [~g ~in]
                        (if (nil? ~var)
                          ~g
                        (assoc! ~g ~key
                                ~(if val-trans
                                   (list val-trans var)
                                   var))))))
            in))))

(defn- session-key-checker-form [name]
  `(unless *session-key*
     (throw (new Exception (str "No session key provided to " ~name)))))

(defn- v-form [v]
  `(or ~v
       (throw 
         (new Exception (str "Validation failed: " ~v)))))

(defn- validation-form [name validation]
  `(and 
     ~@(map v-form validation)))

;; Rough edge: these generated functions currently have a fixed
;; arglist, so no optionals, no keyword parameters. Etc.
;; Also, this looks damn ugly. Sorry.
(defmacro def-fb-api-call [name method & opt]
  (let [{:keys [docstring required optional other-args other-map
                validation
                session-required?]}
        (apply hash-map opt)
        args-var (gensym "args")]
    `(defn ~name ~(str (or docstring (str name ": " required))
                       (if optional
                         (apply str "\nKeyword arguments:\n\t"
                                (interpose "\n\t" (args->arglist optional)))
                         ""))
       ~(vec (concat (args->arglist required)
                     other-args
                     ;; Could do something fancy to define multiple arity
                     ;; versions with real optional arguments... or take the keyword
                     ;; approach...
                     (when optional
                       `(& ~args-var))))
       (let [~@(when optional
                 (list {:keys (args->arglist optional)}
                       args-var))]
         ~(when session-required?
            (session-key-checker-form name))
         ~(when validation
            (validation-form name validation))
         (response->content
           (make-facebook-request
             ~(optional-args->assoc-form
                (assoc
                  (merge (args->map required)
                         other-map)
                  :method method
                  :format "JSON")
                optional)))))))
