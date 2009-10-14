(ns com.twinql.clojure.facebook.util
  (:refer-clojure)
  (:require [org.danlarkin.json :as json]))

(defn #^String as-str
  "Because contrib's isn't typed correctly."
  [x]
  (if (instance? clojure.lang.Named x)
    (name x)
    (str x)))

(defn seq->comma-separated [x]
  (apply str (interpose \, x)))

(defn as-bool-param [x]
  (if x "true" "false"))

(defn facebook-json-response->bool [x]
  (get {false false
        0     false
        true  true
        1     true}
       x x))

(defn time->unix [x]
  (cond
    (number? x)
    x
    
    (keyword? x)
    (if (= :now x)
      (let [millis (long (System/currentTimeMillis))
            now (long (/ millis 1000))]
        now)
      (throw (new Exception (str "Unrecognized time " x))))
    
    (instance? java.util.Date x)
    (let [#^java.util.Date d x
          millis (long (.getTime d))
          now (long (/ millis 1000))]
      now)))

;;; These three functions return [start end period],
;;; suitable for use in admin-get-metrics.
(defn last-day []
  (let [end (long (time->unix :now))]
    [(- end 86400) end 86400]))

(defn last-week []
  (let [end (long (time->unix :now))]
    [(- end 604800) end 604800]))

(defn last-month []
  (let [end (long (time->unix :now))]
    [(- end 2592000) end 2592000]))

(defn period->seconds
  "Accepts :day, :week, :month. Passes through seconds."
  [x]
  (if (number? x)
    x
    (if (keyword? x)
      ({:day 86400
        :week 604800
        :month 2592000} x)
      (throw (new Exception (str "Unrecognized period " x))))))

;; Might want to change :foo-bar to "foo_bar", too.
(defn #^String id->str [x]
  (or
    (cond
      (string? x) x
      (keyword? x) (name x))
    (str x)))

(defn fb-true? [x]
  (contains? #{"1" 1 "true" true} x))

(def present?
  (complement nil?))     ; Whether a param exists.

(defn str->int [x]
  (Long/parseLong x))

(defn str->timestamp [x]
  (Double/parseDouble x))

;; The name is more documentation than anything.
(defn decode-json-array [x]
  (json/decode-from-str x))

(defn assoc-when [coll key val]
  (if val
    (assoc coll key val)
    coll))

(defn rename-keys-with
  "If f returns nil, the key is dropped."
  [m f]
  (apply hash-map
         (mapcat
           (fn [[k v]]
             (let [new-key (f k)]
               (when new-key
                 [new-key v])))
           m)))

(defn string-set-checker
  "Returns a predicate that checks for its argument in the set.
  Works on strings, symbols, or keywords."
  [string-set]
  (fn [x]
    (contains? string-set (as-str x))))
  
(defn query-name? [x]
  (and (string? x)
       (re-find #"^[_0-9a-zA-Z]+$" x)))
  
(defmacro unless [x & body]
  `(when (not ~x)
     ~@body))
