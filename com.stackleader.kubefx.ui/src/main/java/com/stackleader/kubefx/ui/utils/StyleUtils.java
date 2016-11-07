package com.stackleader.kubefx.ui.utils;

import javafx.scene.Node;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

/**
 *
 * @author dcnorris
 */
public class StyleUtils {

    private static BundleContext bc = FrameworkUtil.getBundle(StyleUtils.class).getBundleContext();

    public static void applyDarkTheme(Node node) {
        node.getScene().getStylesheets().add(bc.getBundle().getEntry("main.css").toExternalForm());
        node.getStyleClass().add("theme-dark");
    }
}
