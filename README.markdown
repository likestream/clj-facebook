# What is it? #

A client API for Facebook.

# What is supported? #

Currently, requesting tokens, maintaining and incrementing session properties,
and performing sessionless requests. For example:


    (use 'com.twinql.clojure.facebook.util)        ; For (last-day).
    (use 'com.twinql.clojure.facebook.sessions)
    (use 'com.twinql.clojure.facebook.sessionless)
     
    (with-new-session []
      (admin-get-allocation "notifications_per_day"))
    =>
    10

    (with-new-session []
      (admin-get-metrics ["active_users"] (last-day)))
    =>
    [{:active_users 0, :end_time "1247986800"}]

Bad requests will throw an exception (currently with a descriptive message but
no useful programmatic attributes).
