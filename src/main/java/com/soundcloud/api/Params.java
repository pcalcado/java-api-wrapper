package com.soundcloud.api;

public interface Params {
    /**
     * <a href="https://github.com/soundcloud/api/wiki/10.2-Resources%3A-tracks">Tracks</a>
     */
    @SuppressWarnings({"UnusedDeclaration"})
    interface Track {
        String TITLE         = "track[title]";          // required
        String TYPE          = "track[track_type]";
        String ASSET_DATA    = "track[asset_data]";
        String ARTWORK_DATA  = "track[artwork_data]";
        String POST_TO       = "track[post_to][][id]";
        String POST_TO_EMPTY = "track[post_to][]";
        String TAG_LIST      = "track[tag_list]";
        String SHARING       = "track[sharing]";
        String STREAMABLE    = "track[streamable]";
        String DOWNLOADABLE  = "track[downloadable]";
        String SHARED_EMAILS = "track[shared_to][emails][][address]";
        String SHARING_NOTE  = "track[sharing_note]";
        String PUBLIC        = "public";
        String PRIVATE       = "private";
    }

    /**
     * <a href="https://github.com/soundcloud/api/wiki/10.1-Resources%3A-users">Users</a>
     */
    @SuppressWarnings({"UnusedDeclaration"})
    interface User {
        String NAME                  = "user[username]";
        String PERMALINK             = "user[permalink]";
        String EMAIL                 = "user[email]";
        String PASSWORD              = "user[password]";
        String PASSWORD_CONFIRMATION = "user[password_confirmation]";
        String TERMS_OF_USE          = "user[terms_of_use]";
        String AVATAR                = "user[avatar_data]";
    }

    /**
     * <a href="https://github.com/soundcloud/api/wiki/10.5-Resources%3A-comments">Comments</a>
     */
    @SuppressWarnings({"UnusedDeclaration"})
    interface Comment {
        String BODY      = "comment[body]";
        String TIMESTAMP = "comment[timestamp]";
        String REPLY_TO  = "comment[reply_to]";
    }
}
