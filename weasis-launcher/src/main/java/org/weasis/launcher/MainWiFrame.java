package org.weasis.launcher;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.*;

import org.cef.CefApp;
import org.cef.CefApp.CefAppState;
import org.cef.CefClient;
import org.cef.CefSettings;
import org.cef.browser.CefBrowser;
import org.cef.handler.CefAppHandlerAdapter;

public class MainWiFrame extends JFrame {

    public MainWiFrame(String startURL, boolean useOSR, boolean isTransparent) {
        CefApp.addAppHandler(new CefAppHandlerAdapter(null) {
            @Override
            public void stateHasChanged(CefAppState state) {
                
            }
        });

        final CefSettings settings = new CefSettings();
        settings.windowless_rendering_enabled = useOSR;
        final CefApp cefApp = CefApp.getInstance(settings);

        final CefClient client = cefApp.createClient();

        final CefBrowser browser = client.createBrowser(startURL, useOSR, isTransparent);
        final Component browserUI = browser.getUIComponent();

        this.getContentPane().add(browserUI, BorderLayout.CENTER);
        this.pack();
        this.setLocationRelativeTo(null);
        this.setSize(350, 300);
        this.setVisible(true);

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                
            }
        });
    }
}
