package org.tsuyoi.edgecomp.common;

public class PluginStatics {
    public static final String SWIPE_RECORD_DATA_PLANE_IDENTIFIER_KEY = "sourceSwipe";
    public static final String SWIPE_RECORD_DATA_PLANE_IDENTIFIER_VALUE = "attendenceTrackingSwipe";
    public static final String SWIPE_RESULT_DATA_PLANE_IDENTIFIER_KEY = "swipeIdentity";
    public static final String SWIPE_RESULT_DATA_PLANE_IDENTIFIER_VALUE = "attendenceTrackingIdentity";

    public static String getSiteSwipeRecordDataPlaneValue(String site) {
        return String.format("%s-%s", SWIPE_RECORD_DATA_PLANE_IDENTIFIER_VALUE, site);
    }

    public static String getSiteSwipeResultDataPlaneValue(String site) {
        return String.format("%s-%s", SWIPE_RESULT_DATA_PLANE_IDENTIFIER_VALUE, site);
    }
}
