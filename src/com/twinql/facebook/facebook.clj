(in-ns 'com.twinql.facebook)

(defn stream->string [ #^java.io.InputStream is ]
  (let [reader (new java.io.BufferedReader
                    (new java.io.InputStreamReader is))
        
        sb (new StringBuilder)]
    (try
      (loop [line (.readLine reader)]
        (when line
          (.append sb (str line "\n"))
          (recur (.readLine reader))))
      (catch java.io.IOException e
        (.printStackTrace e))
      (finally
        (try
          (.close is)
          (catch java.io.IOException e
            (.printStackTrace e)))))
    (.toString sb)))

(defn make-facebook-request 
  "Returns [response, next session]."
  ([method api-key secret args]
   (let [session (new-session method api-key)]
     (make-facebook-request method api-key secret session args)))
  
  ([method api-key secret session args]
   (let [http-client (new DefaultHttpClient)
         http-post
         (doto (new HttpPost *facebook-rest-api*)
           (.setEntity (query->body
                         (add-signature
                           (merge session args)
                           secret))))]

     [(process-facebook-xml
        ;; Returns an InputStream.
        (.getContent
          (.getEntity
            (.execute http-client
                      http-post))))
      (next-session session)])))
