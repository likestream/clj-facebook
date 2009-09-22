;;; 
;;; TODO: all of these calls need to be tested. Some have ill-defined response
;;; types, or might not work at all.
;;; Remember, this is a first draft!
;;; 

(ns com.twinql.clojure.facebook.session-required
  (:refer-clojure)
  (:require [org.danlarkin.json :as json])
  (:use com.twinql.clojure.facebook.util)
  (:use com.twinql.clojure.facebook.api))

;;; The only difference between a session-required API call and a sessionless
;;; one is the session_key parameter.
;;; Consequently, we'll simply add the session key to the *session* map, and
;;; make-facebook-request will include it on our behalf.

;;; 
;;; Defining lots of calls gets repetitive.
;;; It's macro time!
;;; 
(defn- session-required-form [[name fb-name & args]]
  `(def-fb-api-call
     ~name ~fb-name
     :session-required? true
     ~@args))

(defmacro defining-session-required [& forms]
  `(do
     ~@(map session-required-form forms)))
                
(defining-session-required
  
  [auth-expire-session "auth.expireSession"
   :docstring
   "Invalidates the current session being used, regardless of whether it is
   temporary or infinite. After successfully calling this function, no further
   API calls requiring a session will succeed using this session. If the
   invalidation is successful, this will return true."]

  [auth-get-signed-public-session-data "auth.getSignedPublicSessionData"
   :docstring
   "Get a structure that can be passed to another app as proof of session. The
   other app can verify it using public key of this app."]

  [auth-promote-session "auth.promoteSession"
   :docstring
   "Creates a temporary session secret for the current (non-infinite) session
   of a Web application. If a session secret already exists, this method
   returns the existing one.
   This session secret will not be used in the signature for the server-side
   component of an application, it is only meant for use by applications which
   additionally want to use a client side component (for example, using the
   JavaScript Client Library)."]

  [connect-get-unconnected-friends-count "connect.getUnconnectedFriendsCount"
   :docstring
   "This method returns the number of friends of the current user who have
   accounts on your site, but have not yet connected their accounts."]

  [events-rsvp "events.rsvp"
   :required [[eid :eid]
              [rsvp-status :rsvp_status as-str]]
   :validation [(#{"attending" "unsure" "declined"} (as-str rsvp-status))]]

  [friends-are-friends "friends.areFriends"
   :required [[uids1 :uids1 seq->comma-separated]
              [uids2 :uids2 seq->comma-separated]]
   :validation [(= (count uids1)
                   (count uids2))]]

  [friends-get-app-users "friends.getAppUsers"]

  [friends-get-lists "friends.getLists"]


  [feed-publish-user-action "feed.publishUserAction"
   :docstring
   "Publishes a story on behalf of the user owning the session, using the
   specified template bundle. By default, this method can publish one line
   stories to the user's Wall only."
   :required [[template-bundle-id :template_bundled_id]
              [template-data      :template_data json/encode-to-str]]  ; JSON object.
   
   :optional [[target-ids   :target_ids seq->comma-separated]  ; List of IDs.
              [body-general :body_general]
              [story-size   :story_size]              ; 1 or 2.
              [user-message :user_message]]
   
   :validation [(contains? #{nil 1 2} story-size)
                (or (nil? user-message)
                    (and story-size    ; nil => 1 by default.
                         (not (= 1 story-size))))]]

  [status-get "status.get"
   :docstring
   "Returns the user's current and most recent statuses. Takes optional uid and
   limit arguments, both integers."
   :optional [[uid :uid]
              [limit :limit]]]
  
  ;; TODO: the documentation for this call is sparse. It might return only
  ;; XML.
  [notifications-get-list "notifications.getList"
   :docstring
   "This method gets all the current session user's notifications, as well as
   data for the applications that generated those notifications. It is a
   wrapper around the notification and application FQL tables; you can achieve
   more fine-grained control by using those two FQL tables in conjunction with
   the fql.multiquery API call."
   :optional [[start-time :start_time time->unix]
              [include-read :include_read as-bool-param]]]
  
  ;; Returns true or false.
  [notifications-mark-read "notifications.markRead"
   :docstring
   "This method marks one or more notifications as read. You return the
   notifications by calling notifications.getList or querying the notification
   FQL table.
   Applications must pass a valid session key, and can only mark the
   notifications of the current session user."
   :response facebook-json-response->bool
   :required [[notification-ids :notification_ids seq->comma-separated]]]
  
  [users-get-logged-in-user "users.getLoggedInUser"
   :docstring 
   "Gets the user ID (uid) associated with the current session. This value
   should be stored for the duration of the session, to avoid unnecessary
   subsequent calls to this method."]
  
  [users-is-verified "users.isVerified"
   :docstring
   "Determines whether Facebook considers the current user to be verified or
   not. We use verification with certain features such as granting verified
   users increased limits for the length and file size of videos they can upload
   directly on Facebook or through your applications.
   Important: You may *never* disclose a user's verified state anywhere in your
   application or on Facebook, whether to that user or the user's friends."
   :response facebook-json-response->bool])
