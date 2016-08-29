/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.stackleader.kubefx.preferences;

import java.io.File;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PreferenceUtils {

    private static final Logger LOG = LoggerFactory.getLogger(PreferenceUtils.class);
    private static final String ROOT_PREFERENCE_NODE_NAME = "com/stackleader/kubefx";
    private static String DATA_HOME_DIR;

    public static Preferences getDefaultPrefsNode() {
        String prefDirPath = getApplicationDataDirectory().getAbsolutePath() + File.separator + "preferences" + File.separator + KubeFxVersion.getVersion() + File.separator;
        System.setProperty("java.util.prefs.userRoot", prefDirPath);
        return Preferences.userRoot().node(ROOT_PREFERENCE_NODE_NAME);
    }

    public static Preferences getPackagePrefsNode(Class c) {
        final String packageName = getPreferenceNodeName(c.getPackage().getName());
        return getDefaultPrefsNode().node(packageName);
    }

    public static Preferences getClassPrefsNode(Class c) {
        final String className = getPreferenceNodeName(c.getCanonicalName());
        return getDefaultPrefsNode().node(className);
    }

    public static void clearAllPreferences() {
        try {
            getDefaultPrefsNode().removeNode();
        } catch (BackingStoreException ex) {
            LOG.error(ex.getMessage(), ex);
        }
    }

    public static File getApplicationDataDirectory() {
        if (DATA_HOME_DIR == null) {
            String kubefxHome = ".kubefx";
            DATA_HOME_DIR = System.getProperty("user.home") + File.separator + kubefxHome;
            File kubeFxHomeFile = new File(DATA_HOME_DIR);
            kubeFxHomeFile.mkdir();
        }
        return new File(DATA_HOME_DIR + File.separator);
    }

    private static String getPreferenceNodeName(String name) {
        final String replace = name.replace('.', '/');
        return replace.replaceFirst(ROOT_PREFERENCE_NODE_NAME + "/", "");
    }

}
