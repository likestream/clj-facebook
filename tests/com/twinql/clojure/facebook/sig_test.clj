(ns com.twinql.clojure.facebook.sig-test
  (:use clojure.test com.twinql.clojure.facebook))

;; TODO: configure logging properties to reduce build noise on
;; unmatched sig
(deftest has-valid-sig?-test
  (let [secret "11111111111111111111111111111111"]
    (are [result sig] (= result (has-valid-sig? sig secret))
         nil {}
         true {:fb_sig "1a80cbf2859e019559365d6be478794e"}
         nil  {:fb_sig "1a80cbf2859e019559365d6be478794e"
                :fb_sig_other_stuff "some more stuff"
                :ignored_stuff "does not contributed to sig"}
         true {:fb_sig "6bc4c6b27fc3874b4ad18b666c3f656f"
               :fb_sig_other_stuff "some more stuff"
               :ignored_stuff "does not contributed to sig"})))

(deftest test-params-for-signature
  (testing "keeps fb_sig_* and drops everything else"
           (is (= {"foo" "keep"}
                  (params-for-signature {:fb_sig_foo "keep"
                                         :fb_sig     "skip this"
                                         :boo        "skip this too"})))))