(ns uk.co.holygoat.util.md5
  (:refer-clojure)
  (:import
     (java.security 
       NoSuchAlgorithmException
       MessageDigest)
     (java.math BigInteger)))

(defn md5-sum
  "Compute the hex MD5 sum of a string."
  [#^String str]
  (let [alg (doto (MessageDigest/getInstance "MD5")
              (.reset)
              (.update (.getBytes str)))]
    (try
      (.toString (new BigInteger 1 (.digest alg)) 16)
      (catch NoSuchAlgorithmException e
        (throw (new RuntimeException e))))))
