/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.stackleader.kubefx.preferences;

public class KubeFxVersion {
    
    private final static int MAJOR_VERSION = 0;
    private final static int MINOR_VERSION = 1;
    
    public static String getVersion(){
        return MAJOR_VERSION+"."+MINOR_VERSION;
    }
    
    public static int getMajorVersion(){
        return MAJOR_VERSION;
    }
    
    public static int getMinorVersion(){
        return MINOR_VERSION;
    }
    
}
