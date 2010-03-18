# What is it? #

A client API for Facebook, and some sparse documentation.

# Building #

Use Leiningen (or ant).

# What is supported? #

## Client ##

Currently, requesting tokens, maintaining and incrementing session properties,
performing requests, and cleaning up Facebook responses. For example:


    (use 'com.twinql.clojure.facebook.util)        ; For (last-day).
    (use 'com.twinql.clojure.facebook.sessions)
    (use 'com.twinql.clojure.facebook.sessionless)
     
    (with-new-fb-session []
      (admin-get-allocation "notifications_per_day"))
    =>
    10

    (with-new-fb-session []
      (admin-get-metrics ["active_users"] (last-day)))
    =>
    [{:active_users 0, :end_time "1247986800"}]

Bad requests will throw an exception (currently with a descriptive message but
no useful programmatic attributes).

Facebook API calls have required arguments (which appear directly in the
arglist), and optional arguments (which are encoded as keyword arguments).
These are included in the docstring for each function. For example:

    user=> (doc feed-publish-user-action)    
    -------------------------
    com.twinql.clojure.facebook/feed-publish-user-action
    ([template-bundle-id template-data & args627])
      Publishes a story on behalf of the user owning the session, using the
       specified template bundle. By default, this method can publish one line
       stories to the user's Wall only.
    Keyword arguments:
      target-ids
      body-general
      story-size
      user-message


Facebook applications authenticate themselves with an API key and a secret key.

`clj-facebook` expects these to either live in
`com.twinql.clojure.facebook.sessions/*secret*` and
`com.twinql.clojure.facebook.sessions/*api-key*` (you can use `alter-var-root`
for this), or for `*secret*` to be be bound by `with-secret-key` and the API
key to be passed in to `new-session` or the argument list of
`with-new-fb-session`. For example:

    (use 'com.twinql.clojure.facebook.sessions)

    ;; Per-thread-binding.
    (with-secret-key "my-secret-key"
      (with-new-fb-session ["my-api-key"]
        ;; Do stuff here.
        ))

    ;; ... or permanently set the var root:
    (alter-var-root (var *api-key*)
      (constantly "my-api-key"))

    (alter-var-root (var *secret*)
      (constantly "my-secret-key"))

The reason for the discrepancy is that the API key is used once to construct
the session (stored in `*session*`: you must increment it after each request by
calling `fetch-session`), whilst the secret is used for signing each request.

You should define suitable macros to simplify your code in the most suitable
way for how you use the Facebook API.

## Server ##

On the server side, some utilities are provided to aid in the development of
(primarily iframe) applications: processing known Facebook request parameters;
verifying signatures; generating Facebook login URLs, etc.

These are demonstrated by the test application in `test.clj`.

## Implemented API ##

Each Facebook API call translates into a Clojure function. See the translations below.

Use `doc` to see the arguments and description.

### Sessionless ###

* `admin.banUsers`:           `admin-ban-users`
* `admin.getAllocation`:      `admin-get-allocation`
* `admin.getAppProperties`:   `admin-get-app-properties`
* `admin.getBannedUsers`:     `admin-get-banned-users`
* `admin.getMetrics`:         `admin-get-metrics`
* `admin.getRestrictionInfo`: `admin-get-restriction-info`
* `admin.setAppProperties`:   `admin-set-app-properties`
* `admin.setRestrictionInfo`: `admin-set-restriction-info`
* `admin.unbanUsers`:         `admin-unban-users`

* `auth.createToken`: as the function `com.twinql.clojure.facebook.auth/create-token`.

* `application.getPublicInfo`
* `auth.getSession`
* `auth.revokeAuthorization`
* `auth.revokeExtendedPermission`
* `profile.getInfoOptions`
* `profile.setInfo`
* `profile.setInfoOptions`
* `users.getStandardInfo`


### Session required ###

* `auth.expireSession`:                 `auth-expire-session`
* `auth.getSignedPublicSessionData`:    `auth-get-signed-public-session-data`
* `auth.promoteSession`:                `auth-promote-session`
* `connect.getUnconnectedFriendsCount`: `connect-get-unconnected-friends-count`
* `events.rsvp`:                        `events-rsvp`
* `feed.publishUserAction`:             `feed-publish-user-action`
* `friends.areFriends`:                 `friends-are-friends`
* `groups.get`:                         `groups-get`
* `groups.getMembers`:                  `groups-get-members`
* `status.get`:                         `status-get`
* `notifications.getList`:              `notifications-get-list`
* `notifications.markRead`:             `notifications-mark-read`
* `users.getLoggedInUser`:              `users-get-logged-in-user`
* `users.isVerified`:                   `users-is-verified`

### Session optional ###

* `fql.multiquery`
* `fql.query`
* `friends.get`
* `friends.getMutualFriends`
* `profile.getInfo`

## Not Implemented ##

### Sessionless ###

* `batch.run`
* `connect.registerUsers`
* `connect.unregisterUsers`
* `data.getCookies`
* `data.setCookie`
* `fbml.deleteCustomTags`
* `fbml.getCustomTags`
* `fbml.refreshImgSrc`
* `fbml.refreshRefUrl`
* `fbml.registerCustomTags`
* `fbml.setRefHandle`
* `fbml.uploadNativeStrings`
* `feed.deactivateTemplateBundleByID`
* `feed.getRegisteredTemplateBundleByID`
* `feed.getRegisteredTemplateBundles`
* `feed.publishTemplatizedAction`
* `feed.registerTemplateBundle`
* `intl.getTranslations`
* `marketplace.getCategories`
* `marketplace.getSubCategories`
* `pages.isAppAdded`
* `permissions.checkAvailableApiAccess`
* `permissions.checkGrantedApiAccess`
* `permissions.grantApiAccess`
* `permissions.revokeApiAccess`

### Session required ###

* `friends.getAppUsers`
* `friends.getLists`
* `links.get`
* `liveMessage.send`
* `marketplace.getListings`
* `marketplace.search`
* `notes.get`
* `notifications.get`
* `pages.isAdmin`
* `pages.isFan`
* `photos.get`
* `photos.getAlbums`
* `photos.getTags`
* `stream.get`
* `video.getUploadLimits`
* `video.upload`

### Session optional ###

* `comments.add`
* `comments.remove`
* `events.cancel`
* `events.create`
* `events.edit`
* `events.get`
* `events.getMembers`
* `links.post`
* `marketplace.createListing`
* `marketplace.removeListing`
* `message.getThreadsInFolder`
* `notes.create`
* `notes.delete`
* `notes.edit`
* `notifications.send`
* `notifications.sendEmail`
* `pages.getInfo`
* `photos.addTag`
* `photos.createAlbum`
* `photos.upload`
* `profile.getFBML`
* `profile.setFBML`
* `status.set`
* `stream.addComment`
* `stream.addLike`
* `stream.getComments`
* `stream.getFilters`
* `stream.publish`
* `stream.remove`
* `stream.removeComment`
* `stream.removeLike`
* `users.getInfo`
* `users.hasAppPermission`
* `users.isAppUser`
* `users.setStatus`

## Authors ##

Development funded by LikeStream LLC (Don Jackson and Shirish Andhare), see http://www.likestream.org/opensource.

Developed by Richard Newman. Contributions from Relevance Inc.
