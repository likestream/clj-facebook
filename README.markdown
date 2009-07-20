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

Facebook applications authenticate themselves with an API key and a secret key.

`clj-facebook` expects these to either live in
`com.twinql.clojure.facebook.sessions/*secret*` and
`com.twinql.clojure.facebook.sessions/*api-key*` (you can use `alter-var-root`
for this), or for `*secret*` to be be bound by `with-secret-key` and the API
key to be passed in to `new-session` or the argument list of
`with-new-session`. For example:

    (use 'com.twinql.clojure.facebook.sessions)

    ;; Per-thread-binding.
    (with-secret-key "my-secret-key"
      (with-new-session ["my-api-key"]
        ;; Do stuff here.
        ))

    ;; ... or permanently set the var root:
    (alter-var-root (var *api-key*)
      (fn [x] "my-api-key"))

    (alter-var-root (var *secret*)
      (fn [x] "my-secret-key"))

The reason for the discrepancy is that the API key is used once to construct
the session (stored in `*session*`: you must increment it after each request by
calling `fetch-session`), whilst the secret is used for signing each request.

You should define suitable macros to simplify your code in the most suitable
way for how you use the Facebook API.
