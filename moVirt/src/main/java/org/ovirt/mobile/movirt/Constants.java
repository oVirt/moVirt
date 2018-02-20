package org.ovirt.mobile.movirt;

public interface Constants {
    String APP_PACKAGE = "org.ovirt.mobile.movirt";
    String APP_PACKAGE_DOT = APP_PACKAGE + ".";
    String PREFERENCES_NAME_SUFFIX = "_preferences";

    String ACCOUNT_KEY = "ACCOUNT_KEY";
    String ACCOUNT_TYPE = APP_PACKAGE_DOT + "authenticator";
    int REMOVE_ACCOUNT_CALLBACK_TIMEOUT = 3; // seconds -  better safe than sorry, but shouldn't be needed

    int SECONDS_IN_MINUTE = 60;
    int MAX_LOGIN_TIMEOUT = 20; // sec

    int MAX_EVENTS_PER_ENTITY = 250; // max events to download for standalone entity

    int MAX_ACCOUNT_NAME_LENTH = 20;

    String FOLLOW_STATISTICS = "follow=statistics";
}
