(in-ns 'com.twinql.facebook)

(def *errors*
  {  1 "Unknown; please resubmit."
     4 "Maximum requests limit reached."
   100 "Parameter missing or invalid."
   101 "Invalid API key." })

(defn handle-error-xml [x]
  ;; Extract error_code, error_msg information.
  nil)
