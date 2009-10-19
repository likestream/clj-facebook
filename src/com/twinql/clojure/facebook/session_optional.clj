(ns com.twinql.clojure.facebook.session-optional
  (:refer-clojure)
  (:require [org.danlarkin.json :as json])
  (:use com.twinql.clojure.facebook.util)
  (:use com.twinql.clojure.facebook.api))

(def-fb-api-call
  profile-get-info "profile.getInfo"
  :docstring
  "Returns the specified user's application info section for the
  calling application. These info sections have either been set via
  a previous profile.setInfo call or by the user editing them directly. The
  content returned is specified within info_fields."
  :optional [[uid :uid]
             [session-key :session_key]]
  :validation [(or uid session-key)])

(def-fb-api-call
  fql-query "fql.query"
  :docstring
  "Evaluates an FQL (Facebook Query Language) query.
  Warning: If you use JSON as the output format, you may run into problems when
  selecting multiple fields with the same name or with selecting multiple
  \"anonymous\" fields (for example, SELECT 1+2, 3+4 ...)."
  :required [[query :query]]
  :optional [[session-key :session_key]])

(def-fb-api-call
  fql-multi-query "fql.multiquery"
  :docstring
  "Evaluates a series of FQL (Facebook Query Language) queries in one call and
  returns the data at one time.
  
  This method takes a JSON-encoded dictionary called queries where the
  individual queries use the exact same syntax as a query made with fql.query.
  However, this method allows for more complex queries to be made. You can
  fetch data from one query and use it in another query within the same call.
  The WHERE clause is optional in the latter query, since it references data
  that’s already been fetched. To reference the results of one query in another
  query within the same call, specify its name in the FROM clause, preceded by
  #."
  :required [[queries :queries json/encode-to-str]]
  :optional [[session-key :session_key]]
  :validation [(map? queries)
               (every? string? (vals queries))
               (every? query-name? (keys queries))])

(def-fb-api-call
  friends-get "friends.get"
  :docstring ""
  :optional [[session-key :session_key]
             [flid :flid]
             [uid :uid]])

(def-fb-api-call
  users-get-info "users.getInfo"
  :docstring
  "Returns a wide array of user-specific information for each user
  identifier passed, limited by the view of the current user. The
  current user is determined from the session_key parameter. The only
  storable values returned from this call are those under the
  affiliations element, the notes_count value, the proxied_email
  address, and the contents of the profile_update_time element.

  Use this call to get user data that you intend to display to other
  users (of your application, for example). If you need some basic
  information about a user for analytics purposes, call
  users.getStandardInfo instead."
  :required [[uids :uids seq->comma-separated]
             [fields :fields seq->comma-separated]]
  :optional [[session-key :session_key]])

(def-fb-api-call
  friends-get-mutual-friends "friends.getMutualFriends"
  :docstring ""
  :optional [[session-key :session_key]
             [source-uid :source_uid]]
  :required [[target-uid :target_uid]]
  :validation [(or source-uid
                   session-key)])

(def-fb-api-call
  send-notification "notifications.send"
  :docstring "Sends a notification to a set of users. Notifications
  are items sent by an application to a user's notifications page in
  response to some sort of user activity within an application. You
  can also send messages to the logged-in user's
  notifications (located on the right hand side of the chat bar), as
  well as on their notifications page."
  :required [[to_ids :to_ids seq->comma-separated]
             [notification :notification]]
  :optional [[session-key :session-key]
             [type :type]])