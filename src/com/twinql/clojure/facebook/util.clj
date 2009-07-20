(ns com.twinql.clojure.facebook.util
  (:refer-clojure))

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
(defn id->str [x]
  (or
    (cond
      (string? x) x
      (keyword? x) (name x))
    (str x)))
