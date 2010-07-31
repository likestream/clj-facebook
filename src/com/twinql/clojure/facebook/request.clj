(ns com.twinql.clojure.facebook.request
  (:refer-clojure)
  (:use com.twinql.clojure.facebook.sessions)
  (:use com.twinql.clojure.facebook.sig)
  (:use com.twinql.clojure.facebook.util)
  (:import 
     (org.apache.http.conn.params
       ConnManagerPNames
       ConnPerRoute)
     (org.apache.http.conn.routing
       HttpRoute)
     (java.lang Exception)
     (java.net URI URLEncoder))
  (:require 
     [com.twinql.clojure.http :as http]))

(def *facebook-rest-api*  (new URI "http://api.facebook.com/restserver.php"))
(def *facebook-login*     (new URI "http://www.facebook.com/login.php"))
(def *facebook-authorize* (new URI "http://www.facebook.com/authorize.php"))
(def *facebook-http-params*
     (http/map->params {:tcp-nodelay true}))

;; By default, use a thread-safe client connection manager
;; with a limit of 100 concurrent connections.
;; We only ever use JSON, so clj-apache-http will close them
;; automatically on our behalf.
(def *facebook-ccm* (http/thread-safe-connection-manager
                      (http/scheme-registry true)
                      (doto (http/connection-limits 100)
                        (.setParameter ConnManagerPNames/MAX_CONNECTIONS_PER_ROUTE
                                       (proxy [ConnPerRoute] []
                                         (#^int getMaxForRoute [#^HttpRoute route]
                                           100))))))

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
     (when-not (string? secret)
       (throw
        (new Exception "Non-string secret key is not suitable for make-facebook-request.")))
     (when (nil? session)
       (throw
        (new Exception "No session. Use with-new-fb-session to establish one.")))
     (let [query (add-signature
                  (assoc-when
                   (merge session args)
                   :session_key *session-key*)
                  secret)]
       (http/post *facebook-rest-api*
                  :connection-manager *facebook-ccm*
                  :query query
                  :as :json
                  :parameters *facebook-http-params*))))

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
