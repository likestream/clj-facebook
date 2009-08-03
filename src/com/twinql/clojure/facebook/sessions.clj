(ns com.twinql.clojure.facebook.sessions
  (:refer-clojure))

;; Convenience var for storing the current session.
(defonce *session* nil)

;; Bind this to your API key.
(defonce *api-key* nil)

;; Bind this to your secret key.
(defonce *secret* nil)

(defn new-fb-session
  "Return a new session with version etc.
  The return value needs to be augmented with a method for some calls."
  ([]
   (new-fb-session *api-key* "1.0"))
  ([api-key]
   (new-fb-session api-key "1.0"))
  ([api-key version]
   { :v version
     :api_key api-key
     :session-id 0 }))

(defn fetch-session
  "Return an equivalent session with a later ID.
  When called with no args, returns the current value of and updates the
  current bound *session*."
  ([]
   (let [current @*session*]
     (var-set *session* (fetch-session current))
     current))
  ([session]
   (assoc session :session-id
          (inc (:session-id session)))))

(defmacro with-fb-session [session & body]
  `(binding [*session* ~session]
     ~@body))

(defmacro with-new-fb-session [[& session-args] & body]
  `(with-fb-session (new-fb-session ~@session-args)
     ~@body))

(defmacro with-secret-key [secret-key & body]
  `(binding [*secret* ~secret-key]
     ~@body))
