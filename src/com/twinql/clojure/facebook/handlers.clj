;;; All of this code exists to turn Facebook responses into
;;; useful Clojure data structures.
;;; Facebook's callbacks are a mess.
;;; Consequently there're a lot of experimental results dumped into this file
;;; and AUTH!

(ns com.twinql.clojure.facebook.handlers
  (:refer-clojure)
  (:use com.twinql.clojure.facebook.util
        com.twinql.clojure.facebook.sessions))
  
(defn facebook-parameters->session-key [params]
  (when (fb-true? (:fb_sig_added params))
    (or (:fb_sig_session_key params)
        (:fb_sig_profile_session_key params))))


(def facebook-post-auth-callback-parameters
     {:fb_sig_install             fb-true?
      :fb_sig_uninstall           fb-true?
      :fb_sig_authorize           fb-true?
      :fb_sig_added               fb-true?
      :fb_sig_profile_update_time str->timestamp
      :fb_sig_expires             str->timestamp

      :fb_sig_session_key         identity
      })

(def facebook-parameters
     {:fb_sig_time               str->timestamp
      :fb_sig_linked_account_ids decode-json-array
      
      :fb_sig_ext_perms          identity ; TODO: list. Not sure of
                                          ; the format.   
      
      :fb_sig                    identity ; Signature.
      :fb_sig_api_key            identity
      :fb_sig_friends            identity
      :fb_sig_locale             identity
 
      ;; These three are used for auth in various circumstances.
      :fb_sig_user               identity
      :fb_sig_canvas_user        identity
      :fb_sig_profile_user       identity
      })

(def facebook-request-parameters
     {:installed                  fb-true? ; Not included in sig!
      :fb_sig_in_canvas           fb-true?
      :fb_sig_in_iframe           fb-true?
      :fb_sig_page_added          fb-true?
      :fb_sig_in_new_facebook     fb-true? ; *shrug*
      :fb_sig_in_profile_tab      present?
      :fb_sig_logged_out_facebook present?
   
      :fb_sig_profile_user        identity
      :fb_sig_profile_session_key identity
      :fb_sig_page_id             identity
      })

(def all-facebook-parameters
     (remove-values-if #{identity}
                       facebook-request-parameters
                       facebook-post-auth-callback-parameters
                       facebook-parameters))

(defn- transform-map
  "Apply the transformers in f (a map from key to unary function) to params
  (an ordinary map)."
  [fs params]
  (select-keys
    (merge-with #(%1 %2) fs params)
    (keys params)))
  
(defn fb-params [params]
  (transform-map
   all-facebook-parameters
   params))

(defn verify-signature
  "Middleware that converts facebook params to useful types, and
  adds :facebook-signature-valid to the request if sig is valid."
  [request]
  (let [valid? (has-valid-sig? (request :params))
        request (update-in request [:params] fb-params)]
    (if valid?
      (assoc request :facebook_signature_valid true)
      request)))

(defn with-signature-verification
  "Middleware that applies verify-signature to a request."
  [handler]
  (fn [request] (handler (verify-signature request))))

(defn facebook-user-id 
  "Extracts the facebook user id from a request, *if* the request has a valid
  id. Checks the signature. Must be called after request has been preprocessed,
  e.g. inside a with-signature-verification form."
  [request]
  (and (request :facebook_signature_valid)
       (get-in request [:params :fb_sig_user])))
