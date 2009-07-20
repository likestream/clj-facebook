(ns com.twinql.clojure.facebook.errors
  (:refer-clojure)
  (:require [com.twinql.clojure.http :as http]))

(def *errors*
  {  1 "Unknown; please resubmit."
     4 "Maximum requests limit reached."
   100 "Parameter missing or invalid."
   101 "Invalid API key." })

(defn handle-error-json [x]
  ;; Extract error_code, error_msg information.
  nil)

;; TODO: eventually we want a structured error format.
(defn fb-error [code & message]
  (throw (new Exception
              (str "Facebook request failed (" code "): "
                   (apply str message)))))

(defn spot-error [m]
  (if (and (map? m)
           (contains? m :error_code))
    (fb-error (:error_code m) (:error_msg m))
    m))
  
(defn response->content [m]
  (let [code (:code m)]
    (if (and (< code 300)
             (>= code 200))
      (spot-error (:content m))
      (throw (new Exception (str "HTTP request failed (" code "): " (:reason m)))))))
