(ns com.twinql.clojure.facebook.sig-test
  (:use clojure.test com.twinql.clojure.facebook clojure.contrib.logging))

(deftest test-params-for-signature
  (testing "keeps fb_sig_* and drops everything else"
           (is (= {"foo" "keep"}
                  (params-for-signature {:fb_sig_foo "keep"
                                         :fb_sig     "skip this"
                                         :boo        "skip this too"})))))