(ns com.twinql.clojure.facebook.handlers-test
  (:use clojure.test
        com.twinql.clojure.facebook))

;;; Post-remove callback.
;;; This is what we get from Compojure...
(def example-post-remove-callback
  {:fb_sig "cc567c09355176d729657d208110f886", 
   :fb_sig_app_id "103381584023", 
   :fb_sig_api_key "323ba847e1fc870fd7ee26e5d23ae100", 
   :fb_sig_user "557701228", 
   :fb_sig_added "0", 
   :fb_sig_time "1254763730.2948", 
   :fb_sig_in_new_facebook "1", 
   :fb_sig_locale "en_US", 
   :fb_sig_uninstall "1"})

;;; ... and this is how it translates.
(def example-post-remove-callback-translated
  {:fb_sig "cc567c09355176d729657d208110f886", 
   :fb_sig_app_id "103381584023", 
   :fb_sig_api_key "323ba847e1fc870fd7ee26e5d23ae100", 
   :fb_sig_user "557701228", 
   :fb_sig_added false, 
   :fb_sig_time 1.2547637302948E9, 
   :fb_sig_in_new_facebook true, 
   :fb_sig_locale "en_US", 
   :fb_sig_uninstall true})

;;; Post-auth callback.
(def example-post-auth-callback
  {:fb_sig_expires "1254837600",
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
   :fb_sig_ss "KWJXNJsEeceQWkZADrvSYQ__"})

(def example-post-auth-callback-translated
  {:fb_sig_expires 1.2548376E9,
   :fb_sig_session_key "2.l0plyE9b3UzjWCcs_STqyg__.86400.1254837600-557701228",
   :fb_sig_app_id "103381584023",
   :fb_sig_ext_perms "auto_publish_recent_activity",
   :fb_sig_locale "en_US",
   :fb_sig_in_new_facebook true,
   :fb_sig_user "557701228",
   :fb_sig_time 1.2547493549156E9,
   :fb_sig_authorize true, 
   :fb_sig "0574853d1cca47f583546c1e6c6a3cee", 
   :fb_sig_api_key "323ba847e1fc870fd7ee26e5d23ae100", 
   :fb_sig_added true, 
   :fb_sig_profile_update_time 1.249261339E9, 
   :fb_sig_ss "KWJXNJsEeceQWkZADrvSYQ__"})

(def truncated-post-auth-callback
  {:fb_sig_expires "1254837600",
   :fb_sig_session_key "2.l0plyE9b3UzjWCcs_STqyg__.86400.1254837600-557701228",
   :fb_sig_app_id "103381584023",
   :fb_sig_locale "en_US",
   :fb_sig_in_new_facebook "1",
   :fb_sig_user "557701228",
   :fb_sig_time "1254749354.9156",
   :fb_sig_authorize "1", 
   :fb_sig "0574853d1cca47f583546c1e6c6a3cee", 
   :fb_sig_api_key "323ba847e1fc870fd7ee26e5d23ae100", 
   :fb_sig_added "1", 
   :fb_sig_profile_update_time "1249261339", 
   :fb_sig_ss "KWJXNJsEeceQWkZADrvSYQ__"})

(deftest post-auth-params-test
  (is (= (fb-params example-post-auth-callback)
         example-post-auth-callback-translated)))

;; Basic signature stuff, not involving the middleware handler.
(deftest signature-checking-test
  (with-fb-keys ["323ba847e1fc870fd7ee26e5d23ae100"
                 "037e6278b8bf2c317b5c5a789c736f2d"]
    (are [params] (has-valid-sig? params)
         example-post-auth-callback
         example-post-remove-callback)
    (are [params] (not (has-valid-sig? params))
         truncated-post-auth-callback)))

;;; Ensure that a post-auth callback contains the necessary parameters.
(deftest post-auth-completeness-test
  (let [params (fb-params example-post-auth-callback)]
    (is (every? (partial contains? params)
                #{:fb_sig_authorize :fb_sig_added
                  :fb_sig_expires :fb_sig_time
                  :fb_sig_user
                  :fb_sig_app_id :fb_sig_session_key}))
    (is (> (:fb_sig_expires params)
           (:fb_sig_time params)))))

(deftest post-remove-params-test
  (is (= (fb-params example-post-remove-callback)
         example-post-remove-callback-translated)))

(deftest fb-params-test
  (is (= {:fb_sig_added false,
          :fb_sig_api_key "API_KEY",
          :fb_sig "SIG",
          :fb_sig_in_iframe true,
          :fb_sig_time 1.2547623936364E9,
          :fb_sig_iframe_key "IFRAME_KEY",
          :fb_sig_in_new_facebook true,
          :fb_sig_locale "en_US",
          :fb_sig_app_id "APP_ID"}
         (fb-params {:fb_sig "SIG",
                     :fb_sig_app_id "APP_ID",
                     :fb_sig_api_key "API_KEY",
                     :fb_sig_added "0",
                     :fb_sig_time "1254762393.6364",
                     :fb_sig_in_new_facebook "1",
                     :fb_sig_locale "en_US",
                     :fb_sig_iframe_key "IFRAME_KEY",
                     :fb_sig_in_iframe "1"}))))

(deftest with-signature-verification-test
  (let [handler (with-signature-verification identity)]
    (with-secret-key "11111111111111111111111111111111"
      (are [result request] (= result (handler request))
           {:params {}}
           {:params {}}
           
           {:params {:fb_sig_install true}}
           {:params {:fb_sig_install "1"}}
           
           {:params {:fb_sig "BAD SIG", :fb_sig_data "SOME DATA"}}
           {:params {:fb_sig "BAD SIG", :fb_sig_data "SOME DATA"}}

           {:facebook_signature_valid true,
            :params {:fb_sig "b56d02ddc8ee9749eac0cc31c8232d77", :fb_sig_data "SOME DATA"}}
           {:params {:fb_sig "b56d02ddc8ee9749eac0cc31c8232d77", :fb_sig_data "SOME DATA"}}))))

(deftest facebook-user-id-test
  (with-secret-key "11111111111111111111111111111111"
    (are [id request] (= id (facebook-user-id (verify-signature request)))
         nil
         {:params {}}
         
         nil
         {:params {:fb_sig "BAD SIG", :fb_sig_user "SOME USER"}}
         
         "SOME USER"
         {:params {:fb_sig "18d112a6dff5b1120f0ae872b33a8a3d", :fb_sig_user "SOME USER"}})))
