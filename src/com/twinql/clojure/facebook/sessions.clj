(ns com.twinql.clojure.facebook.sessions
  (:refer-clojure)
  (:use com.twinql.clojure.facebook.sig))

;; Convenience var for storing the current session.
(defonce *session* nil)

;; Bind this to your API key. Use the macros below!
(defonce *api-key* nil)

;; Bind this to your secret key. Use the macros below!
(defonce *secret* nil)

;; Bind this to the user's session (from params, probably).
(defonce *session-key* nil)

(defmacro with-session-key [params & body]
  `(binding [*session-key* (:fb_sig_session_key ~params)] ~@body))

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
     :call_id 0 }))

(defn fetch-session
  "Return an equivalent session with a later ID.
  When called with no args, returns the current value of and updates the
  current bound *session*."
  ([]
   (let [current @*session*]
     (var-set *session* (fetch-session current))
     current))
  ([session]
   (assoc session :call_id
          (inc (:call_id session)))))

(defmacro with-fb-session [session & body]
  `(binding [*session* ~session]
     ~@body))

(defmacro with-new-fb-session [[& session-args] & body]
  `(with-fb-session (new-fb-session ~@session-args)
     ~@body))

;; We need these because the immigration of symbols
;; is imperfect. We can't rely on users binding these names.
(defmacro with-secret-key [secret-key & body]
  `(binding [*secret* ~secret-key]
     ~@body))

(defmacro with-api-key [api-key & body]
  `(binding [*api-key* ~api-key]
     ~@body))

(defmacro with-fb-keys [[api-key secret-key] & body]
  `(binding [*api-key* ~api-key
             *secret* ~secret-key]
     ~@body))

(defn has-valid-sig?
  "True if the sig is specified and valid, nil otherwise."
  ([params] (has-valid-sig? params *secret*))
  ([params secret]
      (when-not secret
        (throw (new Exception "No secret key.")))
      (when-let [sig (:fb_sig params)]
        (let [computed (generate-signature
                        (params-for-signature params)
                        secret)]
          (= sig computed)))))