(ns com.twinql.clojure.facebook.handlers
  (:refer-clojure)
  (:use com.twinql.clojure.facebook.sig)
  ;(:use compojure)
  )
  
;;; The Compojure integration point.
;;; Handles Facebook callbacks.

;; Facebook parameters sent in callbacks.
; fb_sig_added: If set to true, then the user has authorized your application.
; fb_sig_api_key: Your application's API key.
; fb_sig_friends: The UIDs of the visiting user's friends.
; fb_sig_locale: The user's locale.
; fb_sig_time: The current time, which is a UNIX timestamp.
; fb_sig_user/fb_sig_canvas_user: The visiting user's ID. fb_sig_canvas_user is
;   passed if user has not authorized your application, while fb_sig_user is
;   passed if the user has authorized your application. Neither is passed when an
;   application tab is being requested; fb_sig_profile_user is passed instead.
; fb_sig_linked_account_ids[n]: These are the account_id values that match the
;   user's email hash that were previously sent to facebook using
;   connect.registerUsers
;   
; Facebook passes the following parameters only if fb_sig_added is true (that
;   is, if the user has authorized your application):
; fb_sig_session_key: The valid session key for this user. This parameter isn't
;   passed when an application tab is being requested; fb_sig_profile_session_key
;   is passed instead.
; fb_sig_expires: The time when this session key will expire. This parameter is
;   set to 0 if the session is infinite -- that is, the user granted your
;   application offline access. This is a UNIX timestamp.
; fb_sig_profile_update_time: The time when this user's profile was last
;   updated. This is a UNIX timestamp.
; fb_sig_ext_perms: Any extended permissions that the user has granted to your
;   application. This parameter is sent only if the user has granted any.
;   These parameters are relevant to requests sent to your application:
; fb_sig_in_canvas: This parameter is true if the request is for your
;   application's canvas page.
; fb_sig_in_profile_tab: This parameter is sent if this request is for a user's
;   tab for your application.
; fb_sig_profile_user: The user ID of the profile owner for the tab being
;   requested.
; fb_sig_profile_session_key: The session key for the profile owner, which you
;   use to render this user's profile tab content.
; fb_sig_page_id: The ID of the Page if this request is on behalf of a Page.
; fb_sig_page_added: If this request is on behalf of a Page, this parameter
;   indicates whether the Page has added this application.
; fb_sig_logged_out_facebook: This parameter is sent if the user is not
;   currently logged into Facebook.
; fb_sig_ss: The session secret, used in place of your application's secret key
;   for secure API calls. This is sent only to a SWF rendered by fb:swf that
;   resides within the domain or subdomain of your application's callback URL.
;   The session secret also gets passed to a SWF object inside your Publisher,
;   and can be passed to IFrames rendered with fb:iframe.
;   

(defn fb-true? [x]
  (contains? #{"1" 1 "true" true} x))

(def present?
  (complement nil?))     ; Whether a param exists.

(defn str->int [x]
  x)

(defn str->timestamp [x]
  x)

(defn decode-json-array [x]
  x)

(defn facebook-parameters->session-key [params]
  (when (fb-true? (:fb_sig_added params))
    (or (:fb_sig_session_key params)
        (:fb_sig_profile_session_key params))))


(def facebook-post-auth-callback-parameters
  {:fb_sig_install fb-true?
   :fb_sig_profile_update_time str->timestamp
   :fb_sig_session_key identity
   :fb_sig_expires str->int})

(def facebook-parameters
  {:fb_sig identity           ; Signature.
   :fb_sig_added fb-true?
   :fb_sig_api_key identity
   :fb_sig_friends identity
   :fb_sig_locale identity
   :fb_sig_time str->timestamp
   ;; These three are used for auth in various circumstances.
   :fb_sig_user identity
   :fb_sig_canvas_user identity
   :fb_sig_profile_user identity
   :fb_sig_linked_account_ids decode-json-array})

(def facebook-request-parameters
  {:fb_sig_in_canvas fb-true?
   :fb_sig_in_iframe fb-true?
   :fb_sig_in_profile_tab present?
   :fb_sig_profile_user identity
   :fb_sig_profile_session_key identity
   :fb_sig_page_id identity
   :fb_sig_page_added fb-true?
   :fb_sig_in_new_facebook fb-true?          ; *shrug*
   :fb_sig_logged_out_facebook present?})

(defn transform-map
  "Apply the transformers in f (a map from key to unary function) to params
  (an ordinary map)."
  [fs params]
  (select-keys
    (merge-with #(%1 %2) fs params)
    (keys params)))
  
(defn fb-params [params]
  (transform-map
    (merge facebook-request-parameters
           facebook-post-auth-callback-parameters
           facebook-parameters)
    params))
  
;; Example params when loading an iframe, no auth.
;; Several of these are not present in the docs.
#_
{
 :fb_sig "dafb43c8906eae472c66b1be4857f580", 
 :fb_sig_app_id "103381584023", 
 :fb_sig_api_key "323ba847e1fc870fd7ee26e5d23ae100", 
 :fb_sig_canvas_user "100000037364957", 
 :fb_sig_added "0", 
 :fb_sig_time "1249101223.507", 
 :fb_sig_in_new_facebook "1", 
 :fb_sig_locale "en_US", 
 :fb_sig_in_iframe "1"
 }
