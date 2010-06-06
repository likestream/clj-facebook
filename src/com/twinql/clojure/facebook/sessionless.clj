;;; Sessionless API calls.
;;; http://wiki.developers.facebook.com/index.php/Category:Sessionless_API

(ns com.twinql.clojure.facebook.sessionless
  (:refer-clojure)
  (:require [clojure.contrib.json :as json])
  (:use com.twinql.clojure.facebook.util)
  (:use com.twinql.clojure.facebook.api))

(def-fb-api-call
  admin-ban-users "admin.banUsers"
  :docstring
  "Prevents users from accessing an application's canvas page and forums."
  :required [[uids :uids json/json-str]])

(def-fb-api-call
  admin-unban-users "admin.unbanUsers"
  :docstring
  "Unbans a list of users previously banned using admin.banUsers."
  :required [[uids :uids json/json-str]])

(def-fb-api-call
  admin-get-allocation "admin.getAllocation"
  :docstring
  "Returns the current allocation limits for your application for the specified
  integration points. Allocation limits are determined daily."
  :required [[integration-point-name :integration_point_name id->str]])
  
(def-fb-api-call
  admin-get-metrics "admin.getMetrics"
  :docstring
  "Returns values for the application metrics displayed on the Usage and HTTP
  Request tabs of the application's Insights page.
  See last-day, last-week, last-month."
  :required [[metrics :metrics json/json-str]]
  :other-args [[start end period]]
  :other-map {:start_time (time->unix start)
              :end_time (time->unix end)
              :period (period->seconds period)})
 
(def-fb-api-call
  admin-set-restriction-info "admin.setRestrictionInfo"
  :docstring
  "Sets the demographic restrictions for the application. This call lets you
  restrict users at the application level."
  :required [[restrictions :restrictions json/json-str]])

(def-fb-api-call
  admin-get-restriction-info "admin.getRestrictionInfo"
  :docstring
  "Returns the demographic restrictions for the application.")

(def-fb-api-call
  admin-set-app-properties "admin.setAppProperties"
  :docstring
  "Sets (several) property values for an application. These values previously
  were only accessible through the Facebook Developer application.
  properties must be a map."
  :required [[properties :properties json/json-str]])

(def-fb-api-call
  admin-get-app-properties "admin.getAppProperties"
  :docstring
  "Gets property values previously set for an application on either the
  Facebook Developer application or the with the admin.setAppProperties call.
  properties is a vector."
  :required [[properties :properties json/json-str]])

(def-fb-api-call
  admin-get-banned-users "admin.getBannedUsers"
  :docstring
  "Returns a list of users who were banned by an application using
  admin.banUsers."
  :optional [[uids :uids json/json-str]]
  :validation [(sequential? uids)])

(def-fb-api-call
  application-get-public-info "application.getPublicInfo"
  :docstring ""
  :optional [])

(def-fb-api-call
  auth-get-session "auth.getSession"
  :docstring "Returns the session key bound to an auth_token, as returned by auth.createToken or in the callback_url. Should be called immediately after the user has logged in.
  For Facebook canvas pages, the session key is passed to your page using POST with the fb_sig_session_key parameter."
  :required [[auth_token :auth_token]]
  :optional [[generate_session_secret :generate-session-secret as-bool-param]])

(def-fb-api-call
  auth-revoke-authorization "auth.revokeAuthorization"
  :docstring "If this method is called for the logged in user, then no further API calls can be made on that user's behalf until the user decides to authorize the application again."
  :optional [[uid :uid]])

(def-fb-api-call
  auth-revoke-extended-permission "auth.revokeExtendedPermission"
  :docstring ""
  :required [[perm :perm id->str]]
  :optional [[uid :uid]])

(def-fb-api-call
  profile-get-info "profile.getInfoOptions"
  :docstring
  "Returns the set of typeahead options available for the specified
  field in an application info section. You specify the typeahead
  options with profile.setInfoOptions."
  :required [[field :field]])

(def-fb-api-call
  profile-set-info "profile.setInfo"
  :docstring
  "Configures an application info section that the specified user can install
  on the Info tab of her profile.
  You prompt the user to install the section using the fb:add-section-button
  tag (set the section parameter to info). The button remains on your canvas
  page until the user clicks it to add the section."
  :required [[title :title]
             [type :type]
             [info-fields :info_fields json/json-str]
             [uid :uid]]
  :validation [({1 5} type)
               (sequential? info-fields)])

(def-fb-api-call
  profile-set-info-options "profile.setInfoOptions"
  :docstring
  "Specifies the available items for a field in an application info section.
  These options populate the typeahead for a thumbnail."
  :required [[field :field]
             [options :options json/json-str]]
  :validation [(and (map? options)     ; Only this will encode correctly.
                    (contains? options :label)
                    (contains? options :link))])
  
(def-fb-api-call
  users-get-standard-info "users.getStandardInfo"
  :docstring
  "Returns an array of user-specific information for use by the application
  itself. Make this call on behalf of your application when you need analytic
  information only. Don't display this information to any users. If you need to
  display information to other users, call `users.getInfo`.
  
  The information you can get from this call is limited to:
  
    uid
    first_name
    last_name
    username
    name
    locale
    affiliations (regional type only)
    profile_url
    timezone
    birthday
    sex
    current_location
    proxied_email
  
  The only storable values returned from this call are the user IDs.
  
  This call returns nothing for any users who haven't authorized your application
  to access their information.
  
  This method may not be used by desktop applications or in the context of a
  session secret-based session."
  
  :required [[uids :uids seq->comma-separated]
             [fields :fields seq->comma-separated]]
  :validation [(sequential? fields)
               (every? (string-set-checker
                         #{"uid" "affiliations" "birthday" "current_location"
                           "first_name" "last_name" "locale" "name"
                           "profile_url" "proxied_email" "sex" "timezone"
                           "username"})
                       fields)])
  

(comment
  (with-new-session []
    (admin-get-allocation "notifications_per_day"))
  (with-new-session []
    (admin-get-metrics ["active_users"] (last-day))))
