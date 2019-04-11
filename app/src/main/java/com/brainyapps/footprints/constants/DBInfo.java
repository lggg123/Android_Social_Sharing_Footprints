package com.brainyapps.footprints.constants;

import com.brainyapps.footprints.models.Post;
import com.brainyapps.footprints.models.User;

/**
 * Created by SuperMan on 4/18/2018.
 */

public class DBInfo {
    public final static String TBL_EMAIL = "emaillist";
    public final static String TBL_USER = User.TABLE_NAME;
    public final static String TBL_REPORT = "reports";

    // Email List
    public final static String EMAIL = "email";
    public final static String ROLE = "role";

    /* Same to Model classes variable name */
    // User Model Class
    public final static String USER_ID = "userId";
    public final static String USER_EMAIL = "userEmail";
    public final static String USER_PASSWORD = "userPassword";
    public final static String USER_FIRSTNAME = "firstName";
    public final static String USER_LASTNAME = "lastName";
    public final static String USER_PHOTO = "photoUrl";
    public final static String USER_FACEBOOKID = "facebookId";
    public final static String USER_PRIVACY = "privacy";
    public final static String USER_BRIF = "brif";
    public final static String USER_ADDRESS = "address";
    public final static String USER_SIGNUP_TYPE = "signupType";
    public final static String USER_BANNED = "banned";

    public final static String USER_POSTS = "posts";
    public final static String USER_FOLLOWERS = "followers";
    public final static String USER_FOLLOWINGS = "followings";
    public final static String USER_PENDING = "pending";

    public final static String REPORT_ID = "reportId";
    public final static String REPORT_BY = "reporterId";
    public final static String REPORT_IN = "reportedId";
    public final static String REPORT_CONTENT = "reportContent";

    public final static String TBL_POST = Post.TABLE_NAME;
    public final static String POST_ID = "postId";
    public final static String POST_TITLE = "userEmail";
    public final static String POST_DESCRIPTION = "userPassword";
    public final static String MEDIA_TYPE = "firstName";
    public final static String MEDIA_URL = "lastName";
    public final static String POST_TIME = "postedTime";
    public final static String POST_IS_LOCKED = "isLocked";
    public final static String USER_GOOGLEPOSITION = "googlePosition";
    public final static String POST_LIKES = "likes";
    public final static String POST_COMMENTS = "comments";

    public final static String COMMENT = "comment";
    public final static String COMMENT_TIME = "commentTime";
}
