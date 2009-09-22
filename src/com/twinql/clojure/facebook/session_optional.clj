(ns com.twinql.clojure.facebook.session-optional
  (:refer-clojure)
  (:require [org.danlarkin.json :as json])
  (:use com.twinql.clojure.facebook.util)
  (:use com.twinql.clojure.facebook.api))

(def-fb-api-call
  profile-get-info "profile.getInfo"
  :docstring
  "Returns the specified user's application info section for the
  calling application. These info sections have either been set via
  a previous profile.setInfo call or by the user editing them directly. The
  content returned is specified within info_fields."
  :optional [[uid :uid]
             [session-key :session_key]]
  :validation [(or uid session-key)])
