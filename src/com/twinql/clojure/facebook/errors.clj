;;; Error handling needs a bunch of work; there are no specialized
;;; exceptions defined, so it's all dumped into a string.

(ns com.twinql.clojure.facebook.errors
  (:refer-clojure)
  (:require [com.twinql.clojure.http :as http]))

(def *errors*
  {  1 "Unknown; please resubmit."
     2 "Service unavailable."
     4 "Maximum requests limit reached."
     5 "Remote address not allowed."
   100 "Parameter missing or invalid."
   101 "Invalid API key."
   102 "Improper or expired session key. Log in again."
   103 "Call ID not sequential."
   104 "Incorrect signature."
   601 "Error parsing FQL statement."
   602 "Field does not exist."
   603 "Table does not exist."
   604 "Statement not indexable."
   605 "Function does not exist."
   606 "Wrong number of arguments to function."
   614 "Unresolved dependency in multiquery."})

;; TODO: eventually we want a structured error format,
;; not a plain Exception.
(defn fb-error [code & message]
  (throw (new Exception
              (str "Facebook request failed (" code "): "
                   (apply str message)))))

(defn spot-error [m]
  (if (and (map? m)
           (contains? m :error_code))
    (fb-error (:error_code m) (:error_msg m))
    m))
  
(defn response->content
  "Return the content, or throw an exception if there was an error
  or the HTTP request failed."
  [m]
  (let [code (:code m)]
    (if (and (< code 300)
             (>= code 200))
      (spot-error (:content m))
      (throw (new Exception
                  (str "HTTP request failed (" code "): "
                       (:reason m)))))))
