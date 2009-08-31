(ns com.twinql.clojure.facebook
  (:refer-clojure)
  (:use clojure.contrib.ns-utils))

(immigrate
  'com.twinql.clojure.facebook.sig
  'com.twinql.clojure.facebook.request
  'com.twinql.clojure.facebook.sessions
  'com.twinql.clojure.facebook.sessionless
  'com.twinql.clojure.facebook.session-required
  'com.twinql.clojure.facebook.handlers)
