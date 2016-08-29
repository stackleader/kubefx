package com.stackleader.kubefx.ui.utils;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dcnorris
 */
public class FXUtilities {
    private static final Logger LOG = LoggerFactory.getLogger(FXUtilities.class);
    public static void runAndWait(Runnable runnable) throws InterruptedException, ExecutionException {
        if (Platform.isFxApplicationThread()) {
            runnable.run();
        } else {
            FutureTask<Void> future = new FutureTask<>(runnable, null);
            Platform.runLater(future);
            future.get();
        }
    }
    public static void runAndWaitUnChecked(Runnable runnable) {
        if (Platform.isFxApplicationThread()) {
            runnable.run();
        } else {
            try {
                FutureTask<Void> future = new FutureTask<>(runnable, null);
                Platform.runLater(future);
                future.get();
            } catch (InterruptedException | ExecutionException ex) {
                LOG.error(ex.getMessage(), ex);
            }
        }
    }

}
