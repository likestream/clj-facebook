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

(defn optional-args->assoc-form [input args]
  (let [arg (first args)]
    (if arg
      (let [[var key val-trans] arg
            val-gen (gensym)]
        `(let [~val-gen ~input
               y# ~var]
           (if y#
             (assoc ~(optional-args->assoc-form
                       val-gen (rest args))
                    ~key (~val-trans y#))
             ~val-gen)))
      input)))
         

(defmacro def-sessionless [name method & opt]
  (let [{:keys [docstring required optional other-args other-map]}
        (apply hash-map opt)]
    `(defn ~name ~docstring
       ~(vec (concat (args->arglist required)
                     other-args
                     (args->arglist optional)  ; Not really optional, just nil.
                     ))
       (response->content
         (make-facebook-request
           ~(optional-args->assoc-form
              (assoc
                (merge (args->map required)
                       other-map)
                :method method
                :format "JSON")
              optional))))))

(def-sessionless
  admin-ban-users "admin.banUsers"
  :docstring
  "Prevents users from accessing an application's canvas page and forums."
  :required [[uids :uids json/encode-to-str]])

(def-sessionless
  admin-unban-users "admin.unbanUsers"
  :docstring
  "Unbans a list of users previously banned using admin.banUsers."
  :required [[uids :uids json/encode-to-str]])

(def-sessionless
  admin-get-allocation "admin.getAllocation"
  :docstring
  "Returns the current allocation limits for your application for the specified
  integration points. Allocation limits are determined daily."
  :required [[integration-point-name :integration_point_name id->str]])
  
(def-sessionless
  admin-get-metrics "admin.getMetrics"
  :docstring
  "Returns values for the application metrics displayed on the Usage and HTTP
  Request tabs of the application's Insights page.
  See last-day, last-week, last-month."
  :required [[metrics :metrics json/encode-to-str]]
  :other-args [[start end period]]
  :other-map {:start_time (time->unix start)
              :end_time (time->unix end)
              :period (period->seconds period)})
 
(def-sessionless
  admin-set-restriction-info "admin.setRestrictionInfo"
  :docstring
  "Sets the demographic restrictions for the application. This call lets you
  restrict users at the application level."
  :required [[restrictions :restrictions json/encode-to-str]])

(def-sessionless
  admin-get-restriction-info "admin.getRestrictionInfo"
  :docstring
  "Returns the demographic restrictions for the application.")

(def-sessionless
  admin-set-app-properties "admin.setAppProperties"
  :docstring
  "Sets (several) property values for an application. These values previously
  were only accessible through the Facebook Developer application.
  properties must be a map."
  :required [[properties :properties json/encode-to-str]])

(def-sessionless
  admin-get-app-properties "admin.getAppProperties"
  :docstring
  "Gets property values previously set for an application on either the
  Facebook Developer application or the with the admin.setAppProperties call.
  properties is a vector."
  :required [[properties :properties json/encode-to-str]])

(def-sessionless
  admin-get-banned-users "admin.getBannedUsers"
  :docstring
  "Returns a list of users who were banned by an application using
  admin.banUsers.
  If uids is nil, it is omitted from the request."
  :optional [[uids :uids json/encode-to-str]])

(comment
  (with-new-session []
    (admin-get-allocation "notifications_per_day"))
  (with-new-session []
    (admin-get-metrics ["active_users"] (last-day))))
