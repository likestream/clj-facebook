(ns com.twinql.clojure.facebook.sessions-test
  (:use clojure.test com.twinql.clojure.facebook))

(deftest new-fb-session-test
  (testing "you must bind the var, not the immigrated var!"
           (binding [com.twinql.clojure.facebook.sessions/*api-key* "right-key"
                     com.twinql.clojure.facebook/*api-key* "wrong-key"]
              (are [result args] (= result (apply new-fb-session args))
                   {:v "1.0" :api_key "right-key" :call_id 0} []))))

(deftest has-valid-sig?-test
  (let [secret "11111111111111111111111111111111"]
    (are [result sig] (= result (has-valid-sig? sig secret))
         nil {}
         true {:fb_sig "1a80cbf2859e019559365d6be478794e"}
         false {:fb_sig "1a80cbf2859e019559365d6be478794e"
                :fb_sig_other_stuff "some more stuff"
                :ignored_stuff "does not contributed to sig"}
         true {:fb_sig "6bc4c6b27fc3874b4ad18b666c3f656f"
               :fb_sig_other_stuff "some more stuff"
               :ignored_stuff "does not contributed to sig"})))


