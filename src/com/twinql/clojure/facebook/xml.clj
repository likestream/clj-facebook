(in-ns 'com.twinql.clojure.facebook)

(defn process-facebook-xml
  "Takes a stream as input."
  [x]
  (let [zip (clojure.zip/xml-zip (parse x))
        tag (tag (clojure.zip/root zip))]
    (if (= tag :error_response)
      ;; Return an error structure.
      [tag
       (Integer/parseInt (first (xml-> zip :error_code text)))
       (first (xml-> zip :error_msg text))]
      zip)))
