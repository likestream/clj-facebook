(ns com.twinql.clojure.facebook.compojure-test
  (:refer-clojure)
  (:require compojure)
  (:require [com.twinql.clojure.http :as http])
  (:require [compojure.server.grizzly :as grizzly])
  (:use clojure.test
        com.twinql.clojure.facebook))
        
(compojure/defroutes compojure-test-app
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

(deftest compojure-post-auth-callback
  (let [server (grizzly/grizzly-server
                 {:port 9123}
                 "/*" (compojure/servlet compojure-test-app))]
    (.start server)
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
      (.stop server)
      (is (= result "Success")))))
