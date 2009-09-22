;;; Tools for defining new Facebook API calls.
;;; Definitions themselves are in sessionless.clj, session_required.clj.

(ns com.twinql.clojure.facebook.api
  (:refer-clojure)
  
  (:require [org.danlarkin.json :as json])
  
  (:use com.twinql.clojure.facebook.util)
  (:use com.twinql.clojure.facebook.errors)
  (:use com.twinql.clojure.facebook.request)
  (:use com.twinql.clojure.facebook.sessions))

(defn- args->arglist
  "`args` is like
    [[foo :foo transformer]]
    We want a list of just the Clojure args."
  [args]
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
  (if args
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
              in)))
    input))

(defn- session-key-checker-form [name]
  `(unless *session-key*
     (throw (new Exception (str "No session key provided to " ~name)))))

(defn- v-form [name v]
  `(or ~v
       (throw 
         (new Exception (str "Validation failed in " ~name ": " ~v)))))

(defn- validation-form [name validation]
  `(and 
     ~@(map (partial v-form name) validation)))

;; This looks damn ugly. Sorry.
(defmacro def-fb-api-call
  "Define a Facebook API call function.
   Required arguments: name, method.  E.g.,
  
     foo-bar-baz, \"foo.barBaz\".
  
   Optional arguments are provided as keywords:
  
      :docstring:  self-explanatory.
      :response:   sometimes Facebook's JSON response is inadequate; e.g.,
                   using 0 or 1 as truth values (sometimes as well as false!).
                   This option can specify a post-processing function.
      :required:   a sequence of required arguments. (See below.)
      :optional:   similarly for optional arguments, which are treated as
                   keyword arguments.
      :other-args: if you need direct control over a portion of the argument
                   list, do this. It's spliced directly in.
      :other-map:  a map that contributes to the final request map. It has
                   access to the arguments in `other-args`.
      :validation: a sequence of forms. These are inlined directly as code,
                   with a descriptive exception, so make them pretty.
      :session-required?: if true, code is inserted to check for a session key.
  
   Arguments (required, optional) should be provided as sequences:
   
      [[arg1 :arg_kwd transformer]
       [arg2 :no_transformer]]
  
   The first value is used in the Clojure function; the second in the Facebook
   request map; the third is used to transform the value before sending it."
  
  [name method & opt]
  (let [{:keys [docstring required optional other-args other-map
                validation
                session-required?
                response]}
        (apply hash-map opt)
        args-var (gensym "args")
        output-var (gensym "output")]
    
    `(defn ~name
       
       ;; Docstring.
       ~(str (or docstring (str name ": " required))
             ;; Generate a docstring including keyword arguments.
             (if optional
               (apply str "\nKeyword arguments:\n\t"
                      (interpose "\n\t" (args->arglist optional)))
               ""))
       
       ;; Arglist.
       ~(vec
          (concat
            (args->arglist required)
            other-args
            ;; Could do something fancy to define multiple arity
            ;; versions with real optional arguments... instead, take the keyword
            ;; approach...
            (when optional
              `(& ~args-var))))
       
       ;; Destructure keyword arguments.
       (let [~@(when optional
                 (list {:keys (args->arglist optional)}
                       args-var))]
         
         ;; Include validation forms. The nils if they don't exist
         ;; can be safely ignored.
         ~(when session-required?
            (session-key-checker-form name))
         ~(when validation
            (validation-form name validation))
        
         (let [request-map#
               ;; Build up a map from the required arguments, other arguments,
               ;; and optional (keyword) arguments.
               ~(optional-args->assoc-form
                  (assoc
                    (merge (args->map required)
                           other-map)
                    :method method
                    :format "JSON")
                      optional)

               ~output-var
               (response->content
                 (make-facebook-request
                   request-map#))]
           
           ~(if response
              (list response output-var)
              output-var))))))
