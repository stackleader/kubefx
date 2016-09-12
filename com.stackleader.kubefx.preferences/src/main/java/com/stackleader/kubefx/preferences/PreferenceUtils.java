/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.stackleader.kubefx.preferences;

import com.stackleader.kubefx.preferences.internal.NbPreferences;
import java.io.File;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dcnorris
 */
public class PreferenceUtils {

    private static final Logger LOG = LoggerFactory.getLogger(PreferenceUtils.class);
    private static final String KUBEFX_HOME_DIR = ".kubefx";

    private static String DATA_HOME_DIR;

    public static Preferences getDefaultPrefsNode() {
        return NbPreferences.userRootImpl();
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
            DATA_HOME_DIR = System.getProperty("user.home") + File.separator + KUBEFX_HOME_DIR;
            File igbDataHomeFile = new File(DATA_HOME_DIR);
            igbDataHomeFile.mkdir();
        }
        return new File(DATA_HOME_DIR + File.separator);
    }

    public static File getPreferenceConfigDirectory() {
        File applicationDataDirectory = getApplicationDataDirectory();
        File preferenceConfigDirectory = new File(applicationDataDirectory.getPath() + File.separator + KubeFxVersion.getVersion() + File.separator + "preferences");
        preferenceConfigDirectory.mkdir();
        return preferenceConfigDirectory;
    }

    private static String getPreferenceNodeName(String name) {
        final String replace = name.replace('.', '/');
        return replace;
    }

}
