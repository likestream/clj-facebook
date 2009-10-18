(ns com.twinql.clojure.facebook.compojure-test
  (:refer-clojure)
  (:require compojure)
  (:require [org.danlarkin.json :as json])
  (:require [com.twinql.clojure.http :as http])
  (:require [compojure.server.grizzly :as grizzly])
  (:use clojure.test
        com.twinql.clojure.facebook))
        
(compojure/defroutes compojure-test-app
                    
  ;; Let's make a fake API listener and see what we're sending.
  (compojure/POST "/restserver.php"
    (with-fb-keys ["323ba847e1fc870fd7ee26e5d23ae100"
                   "037e6278b8bf2c317b5c5a789c736f2d"]
      {:status 200
       :headers {"Content-Type" "text/json"}
       :body (json/encode-to-str params)}))
                    
  ;; Test post-auth callbacks.
  (compojure/ANY "/test/postauth"
    (with-fb-keys ["323ba847e1fc870fd7ee26e5d23ae100"
                   "037e6278b8bf2c317b5c5a789c736f2d"]
      (if (has-valid-sig? params)
        (let [p (fb-params params)]
          (if p
            (if (= (:fb_sig_user p) "557701228")
              (if (:fb_sig_authorize p)
                "Success"
                "No fb_sig_authorize param.")
              (str "fb_sig_user is " (:fb_sig_user p)))
            "No parameters."))
        "Invalid signature."))))

;; Abstract away the task of starting a local server, stopping it 
;; when we're done.
(defmacro with-local-servlet [[grizzly-args test-app] & body]
  `(let [server# (grizzly/grizzly-server
                   ~grizzly-args
                   "/*" (compojure/servlet ~test-app))]
     (.start server#)
     (try
       (do
         ~@body)
       (finally
         (.stop server#)))))

(deftest basic-api-call
  (with-local-servlet [{:port 9123} compojure-test-app]
    (binding [com.twinql.clojure.facebook.request/*facebook-rest-api*
              "http://127.0.0.1:9123/restserver.php"]
      (with-fb-keys ["323ba847e1fc870fd7ee26e5d23ae100"
                     "037e6278b8bf2c317b5c5a789c736f2d"]
        (with-new-fb-session []
          (let [result (admin-get-banned-users :uids [123 456])]
            (is (= (json/decode-from-str (:uids result))
                   [123 456]))))))))
    
(deftest compojure-post-auth-callback
  (with-local-servlet [{:port 9123} compojure-test-app]
    (let [result
          (try
            (.trim
              (:content
                (http/post
                  "http://127.0.0.1:9123/test/postauth" 
                  :query {:fb_sig_expires "1254837600",
                          :fb_sig_session_key "2.l0plyE9b3UzjWCcs_STqyg__.86400.1254837600-557701228",
                          :fb_sig_app_id "103381584023",
                          :fb_sig_ext_perms "auto_publish_recent_activity",
                          :fb_sig_locale "en_US",
                          :fb_sig_in_new_facebook "1",
                          :fb_sig_user "557701228",
                          :fb_sig_time "1254749354.9156",
                          :fb_sig_authorize "1", 
                          :fb_sig "0574853d1cca47f583546c1e6c6a3cee", 
                          :fb_sig_api_key "323ba847e1fc870fd7ee26e5d23ae100", 
                          :fb_sig_added "1", 
                          :fb_sig_profile_update_time "1249261339", 
                          :fb_sig_ss "KWJXNJsEeceQWkZADrvSYQ__"}
                  :as :string)))
            (catch Throwable e
              (report :fail (str "Got exception " e " making post-auth request."))))]
      (is (= result "Success")))))
