(in-ns 'com.twinql.clojure.facebook)

(defn friends-get [& args]
  (let [{:keys [session-key flid uid]} (apply hash-map args)]
    (when (not (or session-key uid))
      (throw (new Exception "Either session-key or uid must be provided.")))
    (make-facebook-request
      "friends.get")))
