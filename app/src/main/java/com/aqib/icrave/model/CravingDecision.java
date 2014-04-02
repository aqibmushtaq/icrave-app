package com.aqib.icrave.model;

/**
 * Created by aqib on 22/01/14.
 */
public class CravingDecision {

    public static final int SAVE = 0;
    public static final int EAT_HEALTHY = 1;
    public static final int EAT_UNHEALTHY = 2;
    public static final int ANOTHER_IMAGE = 3;

    public static String getStringValue (int decision) {
        switch (decision) {
            case SAVE: return "Save";
            case EAT_HEALTHY: return "Eat Healthy";
            case EAT_UNHEALTHY: return "Eat Unhealthy";
            case ANOTHER_IMAGE: return "Another Image";
            default: throw new IllegalArgumentException(String.format("getStringValue does not support value ", decision));
        }
    }

}
