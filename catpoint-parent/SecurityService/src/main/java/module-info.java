module com.udacity.catpoint.security {
    requires miglayout;
    requires java.desktop;
    requires transitive com.udacity.catpoint.image;
    requires java.prefs;
    requires com.google.common;
    requires com.google.gson;
    exports com.udacity.catpoint.security.service;
    exports com.udacity.catpoint.security.data;
    exports com.udacity.catpoint.security.application;

    opens com.udacity.catpoint.security.data to com.google.gson;

}