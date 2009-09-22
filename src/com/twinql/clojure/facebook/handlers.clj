;;; All of this code exists to turn Facebook responses into
;;; useful Clojure data structures.
;;; Facebook's callbacks are a mess.
;;; Consequently there're a lot of experimental results dumped into this file
;;; and AUTH!

(ns com.twinql.clojure.facebook.handlers
  (:refer-clojure)
  (:use com.twinql.clojure.facebook.util)
  (:use com.twinql.clojure.facebook.sig)
  (:use com.twinql.clojure.facebook.sessions))
  
(defn facebook-parameters->session-key [params]
  (when (fb-true? (:fb_sig_added params))
    (or (:fb_sig_session_key params)
        (:fb_sig_profile_session_key params))))


(def facebook-post-auth-callback-parameters
  {:fb_sig_install fb-true?
   :fb_sig_uninstall fb-true?
   :fb_sig_authorize fb-true?
   :fb_sig_added fb-true?
   :fb_sig_profile_update_time str->timestamp
   :fb_sig_session_key identity
   :fb_sig_expires str->timestamp})

(def facebook-parameters
  {:fb_sig identity           ; Signature.
   :fb_sig_api_key identity
   :fb_sig_friends identity
   :fb_sig_locale identity
   :fb_sig_time str->timestamp
   ;; These three are used for auth in various circumstances.
   :fb_sig_user identity
   :fb_sig_canvas_user identity
   :fb_sig_profile_user identity
   :fb_sig_ext_perms identity    ; TODO: list. Not sure of the format.
   :fb_sig_linked_account_ids decode-json-array})

(def facebook-request-parameters
  {:installed fb-true?                       ; Not included in sig!
   :fb_sig_in_canvas fb-true?
   :fb_sig_in_iframe fb-true?
   :fb_sig_in_profile_tab present?
   :fb_sig_profile_user identity
   :fb_sig_profile_session_key identity
   :fb_sig_page_id identity
   :fb_sig_page_added fb-true?
   :fb_sig_in_new_facebook fb-true?          ; *shrug*
   :fb_sig_logged_out_facebook present?})

(defn- transform-map
  "Apply the transformers in f (a map from key to unary function) to params
  (an ordinary map)."
  [fs params]
  (select-keys
    (merge-with #(%1 %2) fs params)
    (keys params)))
  
(defn- fb-params [params]
  (transform-map
    (merge facebook-request-parameters
           facebook-post-auth-callback-parameters
           facebook-parameters)
    params))
  
(defn process-params
  "Transforms request parameters and verifies the signature.
  Raises an exception on failure."
  [params]
  (fb-params
    (verify-sig params *secret*)))
