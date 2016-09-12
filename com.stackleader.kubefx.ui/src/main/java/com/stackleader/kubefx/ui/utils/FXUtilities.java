package com.stackleader.kubefx.ui.utils;

import java.util.concurrent.CountDownLatch;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dcnorris
 */
public class FXUtilities {

    private static final Logger LOG = LoggerFactory.getLogger(FXUtilities.class);

    public static void runAndWait(Runnable runnable) {
        if (Platform.isFxApplicationThread()) {
            runnable.run();
        } else {
            final CountDownLatch doneLatch = new CountDownLatch(1);
            Platform.runLater(() -> {
                try {
                    runnable.run();
                } finally {
                    doneLatch.countDown();
                }
            });
            try {
                doneLatch.await();
            } catch (InterruptedException ex) {
            }
        }
    }

}
