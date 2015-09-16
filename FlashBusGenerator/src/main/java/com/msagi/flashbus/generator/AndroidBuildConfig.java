package com.msagi.flashbus.generator;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Created by msagi on 27/08/15.
 */
public class AndroidBuildConfig {

    private static final String FIELD_DEBUG = "DEBUG";

    private static final String FIELD_APPLICATION_ID = "APPLICATION_ID";

    private boolean debug = false;

    private String applicationId = "com.msagi.flashbus";

    private String buildType = "release";

    private String flavour = "";

    private int versionCode = 1;

    private String versionName = "1.0";

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(final String applicationId) {
        this.applicationId = applicationId;
    }

    public String getBuildType() {
        return buildType;
    }

    public void setBuildType(final String buildType) {
        this.buildType = buildType;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(final boolean debug) {
        this.debug = debug;
    }

    public String getFlavour() {
        return flavour;
    }

    public void setFlavour(final String flavour) {
        this.flavour = flavour;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(final int versionCode) {
        this.versionCode = versionCode;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(final String versionName) {
        this.versionName = versionName;
    }

    public static AndroidBuildConfig fromBuildConfig(final Class buildConfigClass) {
        if (buildConfigClass == null) {
            throw new IllegalArgumentException("buildConfigClass == null");
        }
        final AndroidBuildConfig androidBuildConfig = new AndroidBuildConfig();
        final Field[] fields = buildConfigClass.getDeclaredFields();
        for (final Field field : fields) {
            final int fieldModifiers = field.getModifiers();
            if (Modifier.isPublic(fieldModifiers) && Modifier.isStatic(fieldModifiers) && Modifier.isFinal(fieldModifiers)) {
                final String fieldName = field.getName();
                try {
                    if (fieldName.equals(FIELD_DEBUG) && field.getType() == Boolean.class) {
                        androidBuildConfig.setDebug(field.getBoolean(buildConfigClass));
                    } else if (fieldName.equals(FIELD_APPLICATION_ID) && field.getType() == String.class) {
                        androidBuildConfig.setApplicationId((String) field.get(buildConfigClass));
                    }
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("error loading build configuration: class: " + buildConfigClass + " field: " + fieldName);
                }
            }
        }
        return androidBuildConfig;
    }

    @Override
    public String toString() {
        return String.format("AndroidBuildConfig[applicationId: %s, debug: %s]", applicationId, debug);
    }
}
