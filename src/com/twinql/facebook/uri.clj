(in-ns 'com.twinql.facebook)

(defn encode-query [q]
  "q is a map or list of pairs."
  (. URLEncodedUtils format
     (map (fn [[param value]] 
            (new BasicNameValuePair (str param) (str value)))
          q)
     "UTF-8"))

(defn uri-escape [s]
  (. URLEncoder encode s))

(defmulti
  parameter-value
  "Return a suitable string for a parameter: stringified, sequences turned into
  comma-separated escaped values."
  class)

(defmethod parameter-value :default [s]
  (parameter-value (str s)))

(defmethod parameter-value String [s] s)

(defmethod parameter-value clojure.lang.Sequential [s]
  (apply str (interpose "," (map parameter-value s))))

(defmethod parameter-value clojure.lang.Named [s]
  (parameter-value (name s)))
    
(defn parameter-string
  [pairs]
  (apply str (map #(let [[key val] %]
                     (str (name key) "="
                          (parameter-value val)))
                  pairs)))
