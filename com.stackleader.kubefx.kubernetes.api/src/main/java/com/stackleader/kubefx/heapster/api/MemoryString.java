package com.stackleader.kubefx.heapster.api;

import java.text.DecimalFormat;

/**
 *
 * @author dcnorris
 */
public class MemoryString {

    String valueString;
    long value;

    public MemoryString(long memory) {
        this.value = memory;
        this.valueString = formatFileSize(memory);
    }

    public String getValueString() {
        return valueString;
    }

    public long getValue() {
        return value;
    }

//http://stackoverflow.com/questions/13539871/converting-kb-to-mb-gb-tb-dynamicaly
    private static String formatFileSize(long size) {
        String hrSize = null;

        double b = size;
        double k = size / 1024.0;
        double m = ((size / 1024.0) / 1024.0);
        double g = (((size / 1024.0) / 1024.0) / 1024.0);
        double t = ((((size / 1024.0) / 1024.0) / 1024.0) / 1024.0);

        DecimalFormat dec = new DecimalFormat("0.00");

        if (t > 1) {
            hrSize = dec.format(t).concat(" TB");
        } else if (g > 1) {
            hrSize = dec.format(g).concat(" GB");
        } else if (m > 1) {
            hrSize = dec.format(m).concat(" MB");
        } else if (k > 1) {
            hrSize = dec.format(k).concat(" KB");
        } else {
            hrSize = dec.format(b).concat(" Bytes");
        }

        return hrSize;
    }
}
