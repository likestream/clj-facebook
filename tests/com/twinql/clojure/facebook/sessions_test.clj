(ns com.twinql.clojure.facebook.sessions-test
  (:use clojure.test com.twinql.clojure.facebook))

(deftest new-fb-session-test
  (testing "you must bind the var, not the immigrated var!"
           (binding [com.twinql.clojure.facebook.sessions/*api-key* "right-key"
                     com.twinql.clojure.facebook/*api-key* "wrong-key"]
              (are [result args] (= result (apply new-fb-session args))
                   {:v "1.0" :api_key "right-key" :call_id 0} []))))

