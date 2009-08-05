(ns com.twinql.clojure.facebook.session-required
  (:refer-clojure)
  (:require [org.danlarkin.json :as json])
  (:use com.twinql.clojure.facebook.util)
  (:use com.twinql.clojure.facebook.api))

;;; The only difference between a session-required API call and a sessionless
;;; one is the session_key parameter.
;;; Consequently, we'll simply add the session key to the *session* map, and
;;; make-facebook-request will include it on our behalf.

(def-fb-api-call
  auth-expire-session "auth.expireSession"
  :docstring
  "Invalidates the current session being used, regardless of whether it is temporary or infinite. After successfully calling this function, no further API calls requiring a session will succeed using this session. If the invalidation is successful, this will return true.")

(def-fb-api-call
  status-get "status.get"
  :docstring
  "Returns the user's current and most recent statuses. Takes optional uid and limit arguments, both integers."
  :optional [[uid :uid prn]
             [limit :limit prn]])
