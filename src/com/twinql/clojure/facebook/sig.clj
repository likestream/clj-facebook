(ns com.twinql.clojure.facebook.sig
  (:refer-clojure)
  (:use com.twinql.clojure.facebook.util)
  (:use uk.co.holygoat.util.md5))

;;; 
;;; Utils.
;;; 
(defmulti
  parameter-value
  "Return a suitable string for a parameter: stringified, sequences turned into
  comma-separated escaped values."
  class)

(defmethod parameter-value :default [s]
  (parameter-value (str s)))

(defmethod parameter-value String [s] s)

(defmethod parameter-value clojure.lang.Sequential [s]
  (apply str (interpose "," (map parameter-value s))))

(defmethod parameter-value clojure.lang.Named [s]
  (parameter-value (name s)))
   
(defn- #^String namestr [x]
  (if (string? x)
    x
    (name x)))

(defn- #^String parameter-string
  [pairs]
  (apply str (map (fn [[key val]]
                    (str (namestr key) "="
                         (parameter-value val)))
                  pairs)))

;;;
;;; Generation.
;;; 
(defn generate-signature
  [args secret]
  (md5-sum
    (str
      (parameter-string
        (sort-by #(namestr (key %))
                 java.lang.String/CASE_INSENSITIVE_ORDER args))
      secret)))

(defn add-signature
  "Adds a signature to an argument map."
  [map secret]
  (assoc map :sig (generate-signature map secret)))

;;; 
;;; Verification.
;;; 
 
(defn- facebook-sig-param-name [k]
  (let [#^String n (name k)]
    ;; Handily drops fb_sig.
    (when (zero? (.indexOf n "fb_sig_"))
      (.substring n 7))))

(defn- params-for-signature [params]
  (rename-keys-with params facebook-sig-param-name))

;; TODO: it would be nice to augment this with a real Compojure middleware 
;; function, so developers can wrap their handlers in a 
;; "with-signature-verification" form.
(defn verify-sig
  "Returns the parameters on success; throws an exception on failure."
  [params secret]
  (unless secret
    (throw (new Exception "No secret key provided to verify-sig.")))
  (let [sig (:fb_sig params)
        computed (generate-signature
                   (params-for-signature params)
                    secret)]
    
    (unless sig
      (throw (new Exception "No signature to compare!")))
            
    (if (= sig computed)
      params
      (throw (new Exception
                  (str "Signature does not match: computed = " computed
                       ", should be " sig "."))))))
