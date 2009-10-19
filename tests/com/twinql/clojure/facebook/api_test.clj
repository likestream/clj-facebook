(ns com.twinql.clojure.facebook.api-test
  (:use clojure.test com.twinql.clojure.facebook.api))

;; this is weak sauce, as the entire def-fb-api-call macro
;; needs to be refactored. Driven by a bug though.
(deftest optional-args-binding-form-test
  (is (= (list '{:keys (uid)} `(apply hash-map args))
         (optional-args-binding-form '[[uid :uid]] `args)))
  (is (nil? (optional-args-binding-form '[] `args))))
