(in-ns 'com.twinql.facebook)

(defn generate-signature
  [args secret]
  (md5-sum
    (str
      (parameter-string
        (sort-by #(name (key %))
                 java.lang.String/CASE_INSENSITIVE_ORDER args))
      secret)))

(def *auth*)
(def *facebook-rest-api* (new URI "http://api.facebook.com/restserver.php"))
