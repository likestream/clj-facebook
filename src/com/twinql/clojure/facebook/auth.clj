(ns com.twinql.clojure.facebook.auth
  (:refer-clojure)
  (:use com.twinql.clojure.facebook.errors)
  (:use com.twinql.clojure.facebook.request))

(defn create-token
  "Returns the new token, or throws an exception on error."
  []
  (response->content
    (make-facebook-request
      {:method "auth.createToken"
       :format "JSON"})))
