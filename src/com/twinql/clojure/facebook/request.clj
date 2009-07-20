(ns com.twinql.clojure.facebook.request
  (:refer-clojure)
  (:use com.twinql.clojure.facebook.sessions)
  (:use com.twinql.clojure.facebook.sig)
  (:import 
     (java.lang Exception)
     (java.net URI URLEncoder))
  (:require 
     [com.twinql.clojure.http :as http]
     [org.danlarkin.json :as json]))

(def *facebook-rest-api* (new URI "http://api.facebook.com/restserver.php"))

(defmethod http/entity-as :json [entity as]
  (json/decode-from-reader (http/entity-as entity :reader)))

(defn make-facebook-request 
  "args should include your method."
  ([args]
   (make-facebook-request *session*
                          *secret*
                          args))
  ([secret args]
   (make-facebook-request *session*
                          secret
                          args))
  
  ([session secret args]
   (when (nil? session)
     (throw (new Exception "No session. Use with-new-session to establish one.")))
   (http/post *facebook-rest-api*
              :query (add-signature
                       (merge session args)
                       secret)
              :as :json)))
