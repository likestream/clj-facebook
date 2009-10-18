(ns com.twinql.clojure.facebook.request
  (:refer-clojure)
  (:use com.twinql.clojure.facebook.sessions)
  (:use com.twinql.clojure.facebook.sig)
  (:use com.twinql.clojure.facebook.util)
  (:import 
     (java.lang Exception)
     (java.net URI URLEncoder))
  (:require 
     [com.twinql.clojure.http :as http]
     [org.danlarkin.json :as json]))

(def *facebook-rest-api*  (new URI "http://api.facebook.com/restserver.php"))
(def *facebook-login*     (new URI "http://www.facebook.com/login.php"))
(def *facebook-authorize* (new URI "http://www.facebook.com/authorize.php"))

;; I'd love to roll this into the HTTP library, but I don't want to impose
;; a dependency on the JSON library...
(defmethod http/entity-as :json [entity as]
  (json/decode-from-reader (http/entity-as entity :reader)))

(defn make-facebook-request 
  "`args` should include your method."
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
     (throw
       (new Exception "No session. Use with-new-fb-session to establish one.")))
   (http/post *facebook-rest-api*
              :query (add-signature
                       (assoc-when
                         (merge session args)
                         :session_key *session-key*)
                       secret)
              :as :json)))

(defn login-url
  "Produce a URI to redirect a user to the Facebook login page to authorize
  your application. Call within a with-session form."
  ;; TODO: redirect to canvas?
  ([]
   (http/resolve-uri *facebook-login* *session*))
  ([next-url]
   (http/resolve-uri *facebook-login* (assoc *session* :next next-url)))
  ([next-url cancel-url]
   (http/resolve-uri *facebook-login* (assoc *session*
                                             :next next-url
                                             :next_cancel cancel-url))))

(defn authorize-url
  "Permissions should be a string or a sequence of strings.
  See <http://wiki.developers.facebook.com/index.php/Extended_permissions>."
  ([permissions next-url cancel-url]
   (http/resolve-uri *facebook-authorize*
                     (assoc *session*
                            :ext_perm permissions
                            :next next-url
                            :next_cancel cancel-url)))
  ([permissions next-url]
   (http/resolve-uri *facebook-authorize*
                     (assoc *session*
                            :ext_perm permissions
                            :next next-url))))

;; Post-auth callback URL.
;; Post-remove callback URL.
;; Information update callback URL.
;; Publish callback URL.
;; Self-publish.
