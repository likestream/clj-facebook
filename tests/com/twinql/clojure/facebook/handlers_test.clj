(ns com.twinql.clojure.facebook.handlers-test
  (:use clojure.test com.twinql.clojure.facebook))

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