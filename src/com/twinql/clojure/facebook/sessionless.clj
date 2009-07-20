;;; Sessionless API calls.
;;; http://wiki.developers.facebook.com/index.php/Category:Sessionless_API

(ns com.twinql.clojure.facebook.sessionless
  (:refer-clojure)
  (:require [org.danlarkin.json :as json])
  ;; This seems insane, but sessionless requests still need
  ;; version, api_key, etc...
  (:use com.twinql.clojure.facebook.util)
  (:use com.twinql.clojure.facebook.errors)
  (:use com.twinql.clojure.facebook.request)
  (:use com.twinql.clojure.facebook.sessions))

(defn args->arglist [args]
  (map first args))

(defn args->map [args]
  (into {} (map (fn [[arg key val-trans]]
                  [key (list val-trans arg)])
                args)))

(defmacro def-sessionless [name method docstring args & opt]
  (let [[other-args other-map] opt]
  `(defn ~name ~docstring
     ~(vec (concat (args->arglist args) other-args))
     (response->content
       (make-facebook-request
         ~(assoc
            (merge (args->map args) other-map)
            :method method
            :format "JSON"))))))

(def-sessionless
  admin-ban-users "admin.banUsers"
  "Prevents users from accessing an application's canvas page and forums."
  [[uids :uids json/encode-to-str]])

(def-sessionless
  admin-unban-users "admin.unbanUsers"
  "Unbans a list of users previously banned using admin.banUsers."
  [[uids :uids json/encode-to-str]])

(def-sessionless
  admin-get-allocation "admin.getAllocation"
  "Returns the current allocation limits for your application for the specified
  integration points. Allocation limits are determined daily."
  [[integration-point-name :integration_point_name id->str]])
  
(def-sessionless
  admin-get-metrics "admin.getMetrics"
  "Returns values for the application metrics displayed on the Usage and HTTP
  Request tabs of the application's Insights page.
  See last-day, last-week, last-month."
  [[metrics :metrics json/encode-to-str]]
  [[start end period]]
  {:start_time (time->unix start)
   :end_time (time->unix end)
   :period (period->seconds period)})
 
(def-sessionless
  admin-set-restriction-info "admin.setRestrictionInfo"
  "Sets the demographic restrictions for the application. This call lets you
  restrict users at the application level."
  [[restrictions :restrictions json/encode-to-str]])

(def-sessionless
  admin-get-restriction-info "admin.getRestrictionInfo"
  "Returns the demographic restrictions for the application."
  [])

(def-sessionless
  admin-set-app-properties "admin.setAppProperties"
  "Sets (several) property values for an application. These values previously
  were only accessible through the Facebook Developer application.
  properties must be a map."
  [[properties :properties json/encode-to-str]])

(def-sessionless
  admin-get-app-properties "admin.getAppProperties"
  "Gets property values previously set for an application on either the
  Facebook Developer application or the with the admin.setAppProperties call.
  properties is a vector."
  [[properties :properties json/encode-to-str]])

;; TODO: getBannedUsers. There is a filter array which is optional; I need to
;; make sure that empty input does not result in incorrect filtering.

(comment
  (with-new-session []
    (admin-get-allocation "notifications_per_day"))
  (with-new-session []
    (admin-get-metrics ["active_users"] (last-day))))
