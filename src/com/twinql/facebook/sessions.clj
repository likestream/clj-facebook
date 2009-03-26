(in-ns 'com.twinql.facebook)

(defn new-session
  "Return a new session with version etc."
  ([method api-key]
   (new-session api-key method "1.0"))
  ([method api-key version]
   { :v version
     :api_key api-key
     :method method
     :session-id 0 }))

(defn next-session
  "Return an equivalent session with a later ID."
  [session]
  (assoc session :session-id
         (inc (:session-id session))))
