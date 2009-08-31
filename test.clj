;;;
;;; Test application for handling Facebook requests.
;;; Serves pages in an iframe.
;;; 
;;; Y'might want to switch to clj-html for HTML output; it's substantially
;;; quicker than Compojure's builtins. It uses the same technique as CL-WHO for
;;; optimizing output.
;;; 
;;; Note that this test page does no escaping!
;;;
;;; Current Facebook app settings:
;;; 
;;;  API Key                             323ba847e1fc870fd7ee26e5d23ae100
;;;  Application Secret                  037e6278b8bf2c317b5c5a789c736f2d
;;;  Application ID                      103381584023
;;;  Canvas Callback URL                 http://www01.siptone.net/test/canvas/
;;;  Canvas URL                          http://apps.facebook.com/cljtest/
;;;  FBML/iframe                         iframe
;;;  Application Type                    Website
;;;  Post-Authorize Redirect URL         http://apps.facebook.com/cljtest/needslogin
;;;  Post-Remove URL                     http://www01.siptone.net/test/postremove
;;;  Post-Authorize URL                  http://www01.siptone.net/test/postauth
;;; 

(ns test
  (:refer-clojure)
  (:use compojure)
  (:use com.twinql.clojure.facebook.sig)
  (:use com.twinql.clojure.facebook.request)
  (:use com.twinql.clojure.facebook.sessions)
  (:use com.twinql.clojure.facebook.sessionless)
  (:require [com.twinql.clojure.facebook.session-required :as fb])
  (:use com.twinql.clojure.facebook.handlers))

;; Trivial storage of logged-in users.
;; Doesn't persist!
(def *users* (ref {}))

;; Request debugging.
(def *print-fb-params?* true)

;; Bind the session information for this application.
;; Use this macro around any code you want to make API
;; calls.
(defmacro with-cljtest [& body]
  `(fb/with-fb-keys ["323ba847e1fc870fd7ee26e5d23ae100"
                     "037e6278b8bf2c317b5c5a789c736f2d"]
     ~@body))

;; Common processing of params.
;; Note: no error handling in param verification!
;; It might be useful to redirect people to the app's canvas page when
;; signature verification fails.
(defmacro with-fb-params [m & body]
  `(with-cljtest
     (let [~'params (process-params ~'params)
           
           ;; Bind a predetermined set of labels to functions of the processed
           ;; parameters.
           ~@(when m
               (mapcat (fn [[bind-to f]]
                         (list bind-to (list f 'params)))
                       m))]
       (when *print-fb-params?*
         (prn ~'params))
       (with-session-key ~'params
         ~@body))))

;; Take the parameters received by the post-auth handler,
;; mutating the users map.
(defn handle-user-login [params]
  (println "# ... handling user login with params:")
  (prn (select-keys params [:fb_sig_user, :fb_sig_authorize, :fb_sig_expires]))
  
  (when (contains? params :fb_sig_authorize)
    (let [user (:fb_sig_user params)
          authorized? (:fb_sig_authorize params)
          until (:fb_sig_expires params)]
      ;; No need to verify the parameters -- process-params already did.
      (when user
        (dosync
          (if authorized?
            (alter *users* assoc user until)
            (alter *users* dissoc user)))
        
        ;; Now update disk version.
        (with-open [f (java.io.FileWriter. "/opt/clj/users.clojure")]
          (.write f (prn-str @*users*)))))))

(defn handle-user-logout [params]
  (println "# ... handling user logout with params:")
  (prn (select-keys params [:fb_sig_user, :fb_sig_uninstall]))
  
  (when (get params :fb_sig_uninstall false)
    (let [user (:fb_sig_user params)]
      ;; No need to verify the parameters -- process-params already did.
      (when user
        (dosync
          (alter *users* dissoc user))))))

;; Checks the user from the parameters against the expiry in the users map.
(defn user-logged-in? [params]
  (let [expires (get @*users* (:fb_sig_user params) 0)]
    (> (* expires 1000)
       (System/currentTimeMillis))))

(defroutes test-app

  ;;; Auth callbacks.
  (POST "/test/postauth"
    (println "# postauth: ")
    (with-fb-params {}
      (handle-user-login params)
      "OK"))

  (POST "/test/postremove"
    (println "# postremove: ")
    (with-fb-params {}
      (handle-user-logout params)
      "OK"))

  ;; An authentication-restricted page.
  (GET "/test/canvas/needslogin"
    (println "# Accessed needs-login page.")
    (with-fb-params {this-user :fb_sig_user
                     logged-in? user-logged-in?}
      (html
        [:h1 "This page requires login."]
        (if logged-in?
          (html
            [:p
             [:b "Your current status: "]
             (with-new-fb-session []
               (html
                 (-> (fb/status-get this-user 1) first :message str)))])
          (html [:p "Hey, you're not logged in!"])))))

           
  ;; iframes are the ugly duckling of Facebook applications.
  ;; You need to be very careful to use the correct URLs for various redirects,
  ;; and use target="_parent" on login links.
  ;; Resources:
  ;; <http://www.maybefriday.com/blog/2009/07/facebook-apps-and-iframes/>
  (GET "/test/canvas/"
    (println "# Accessed main canvas page.")
    (with-fb-params {logged-in? user-logged-in?}
      (with-new-fb-session [] 
        (html
          [:h1 "Page generated by Clojure."]
          (if logged-in?
            (html
              [:p "Welcome back!"]
              [:p "Allocation:"]
              [:p (str (admin-get-allocation "emails_per_day"))])
            (html
              [:p [:a {:target "_parent"
                       :href (login-url)}
                   "Access the login-required page."]]))))))

  (GET "/*"
    (println "# Accessed default page.")
    (html [:h1 "Hello World"])))

;; Load users.
(println
  "Users are"
  (dosync
    (ref-set *users*
             (read-string
               (slurp "/opt/clj/users.clojure")))))

;; Run this behind pound, URL prefix /test/.
;; See http://www01.siptone.net/test/
(run-server {:port 8088}
  "/*" (servlet test-app))
