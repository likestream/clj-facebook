(ns com.twinql.clojure.facebook.sig
  (:refer-clojure)
  (:use uk.co.holygoat.util.md5))

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
  (apply str (map (fn [[key val]]
                    (str (name key) "="
                         (parameter-value val)))
                  pairs)))

(defn generate-signature
  [args secret]
  (md5-sum
    (str
      (parameter-string
        (sort-by #(name (key %))
                 java.lang.String/CASE_INSENSITIVE_ORDER args))
      secret)))

(defn add-signature
  "Adds a signature to an argument map."
  [map secret]
  (assoc map :sig (generate-signature map secret)))
