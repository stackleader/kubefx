/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.stackleader.kubefx.preferences.internal;

/**
 *
 * @author muralidhar
 */
public class SessionPreferences {
    
    private static String recentSelectedFilePath;
    
    public static String getRecentSelectedFilePath() {
        return recentSelectedFilePath;
    }

    public static void setRecentSelectedFilePath(String recentSelectedFilePath) {
        SessionPreferences.recentSelectedFilePath = recentSelectedFilePath;
        
    }
    
    
}
