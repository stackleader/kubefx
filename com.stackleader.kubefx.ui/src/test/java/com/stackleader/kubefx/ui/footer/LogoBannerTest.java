/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.stackleader.kubefx.ui.footer;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.junit.Test;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.testfx.framework.junit.ApplicationTest;

/**
 *
 * @author dcnorris
 */
public class LogoBannerTest extends ApplicationTest {

    @Mock
    private BundleContext bc = mock(BundleContext.class);

    @Mock
    private Bundle bundle = mock(Bundle.class);

    public LogoBannerTest() {
        MockitoAnnotations.initMocks(this);
        when(bc.getBundle())
                .thenReturn(bundle);
        when(bundle.getEntry("logo.css"))
                .thenReturn(LogoBanner.class.getClassLoader().getResource("logo.css"));
        when(bundle.getEntry("stackLeaderLogo.png"))
                .thenReturn(LogoBanner.class.getClassLoader().getResource("stackLeaderLogo.png"));
    }

    @Override
    public void start(Stage stage) {
        final LogoBanner logoBanner = new LogoBanner();
        logoBanner.activate(bc);
        BorderPane borderPane = new BorderPane(logoBanner);
        BorderPane.setAlignment(logoBanner, Pos.BOTTOM_LEFT);
        Scene scene = new Scene(borderPane, 800, 50);
        stage.setScene(scene);
        stage.show();
    }

    @Test
    public void show_banner() throws InterruptedException {
        Thread.sleep(5000);
    }

}
