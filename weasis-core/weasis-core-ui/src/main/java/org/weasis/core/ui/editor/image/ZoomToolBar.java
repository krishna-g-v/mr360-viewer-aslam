/*
 * Copyright (c) 2009-2020 Weasis Team and other contributors.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at https://www.eclipse.org/legal/epl-2.0, or the Apache
 * License, Version 2.0 which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */
package org.weasis.core.ui.editor.image;

import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;

import org.weasis.core.api.media.data.MediaSeries;
import org.weasis.core.api.gui.util.ActionState;
import org.weasis.core.api.gui.util.ActionW;
import org.weasis.core.api.gui.util.ComboItemListener;
import org.weasis.core.api.gui.util.DropButtonIcon;
import org.weasis.core.api.gui.util.DropDownButton;
import org.weasis.core.api.gui.util.GuiExecutor;
import org.weasis.core.api.gui.util.SliderChangeListener;
import org.weasis.core.api.gui.util.ToggleButtonListener;
import org.weasis.core.api.service.AuditLog;
import org.weasis.core.ui.Messages;
import org.weasis.core.ui.editor.image.dockable.MeasureTool;
import org.weasis.core.ui.pref.Monitor;
import org.weasis.core.ui.util.WtoolBar;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.*;

import org.cef.CefApp;
import org.cef.CefApp.CefAppState;
import org.cef.CefClient;
import org.cef.CefSettings;
import org.cef.OS;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.callback.CefContextMenuParams;
import org.cef.callback.CefMenuModel;
import org.cef.handler.CefAppHandlerAdapter;
import org.cef.handler.CefContextMenuHandler;


@SuppressWarnings("serial")
public class ZoomToolBar extends WtoolBar {
  public static JFrame browserFrame = new JFrame();
  public static JFrame loadedBrowser = null;
  public CefClient clientRem = null;
  public CefBrowser browserRem = null;

  public ZoomToolBar(final ImageViewerEventManager<?> eventManager, int index, boolean showLens) {
    super(Messages.getString("ZoomToolBar.zoomBar"), index);
    if (eventManager == null) {
      throw new IllegalArgumentException("EventManager cannot be null");
    }

    final DropDownButton zoom =
        new DropDownButton(
            "zoom", // NON-NLS
            new DropButtonIcon(
                new ImageIcon(MouseActions.class.getResource("/icon/16x16/zoom.png")))) {

          @Override
          protected JPopupMenu getPopupMenu() {
            return getZoomPopupMenuButton(this, eventManager);
          }
        };
    zoom.setToolTipText(Messages.getString("ZoomToolBar.zoom_type"));
    add(zoom);

    ActionState zoomAction = eventManager.getAction(ActionW.ZOOM);
    if (zoomAction != null) {
      zoomAction.registerActionState(zoom);
    }

    if (showLens) {
      final JToggleButton jButtonLens =
          new JToggleButton(
              new ImageIcon(MouseActions.class.getResource("/icon/16x16/zoom-lens.png")));
      jButtonLens.setToolTipText(Messages.getString("ViewerToolBar.show_lens"));
      ActionState lens = eventManager.getAction(ActionW.LENS);
      if (lens instanceof ToggleButtonListener) {
        ((ToggleButtonListener) lens).registerActionState(jButtonLens);
      }
      add(jButtonLens);
      
      final JToggleButton jLinkModeButton = new JToggleButton(new ImageIcon(MouseActions.class.getResource("/icon/16x16/link.png")));
      jLinkModeButton.setToolTipText("Link / Unlink");
      ActionListener actionListener = new ActionListener() {
          public void actionPerformed(ActionEvent actionEvent) {
            AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
            boolean selected = abstractButton.getModel().isSelected();
//            System.out.println("Action - selected=" + selected + "\n");
            if(selected) {
//            	jLinkModeButton.setIcon(new ImageIcon(MouseActions.class.getResource("/icon/16x16/unlink.png")));
//            	DefaultView2d.linkMode = false;
            	DefaultView2d.manualLinkBtnPressed = true;
            }
            else {
//            	jLinkModeButton.setIcon(new ImageIcon(MouseActions.class.getResource("/icon/16x16/link.png")));
//            	DefaultView2d.linkMode = true;
            	DefaultView2d.manualLinkBtnPressed = false;
            	DefaultView2d.referenceIndex = 0; //resetting here when manual link scroll is unchecked
            }
            
         // register events only for CT modality
            ImageViewerPlugin viewerContainer = eventManager.getSelectedView2dContainer();
            MediaSeries viewerDicomSeries = viewerContainer.getSelectedImagePane().getSeries();
            if(viewerDicomSeries.getToolTips().toUpperCase().contains("Modality: CT".toUpperCase())) {
            	ComboItemListener synchAction = eventManager.getAction(ActionW.SYNCH, ComboItemListener.class).orElse(null);
                eventManager.updateAllListeners(viewerContainer,
                		synchAction == null ? SynchView.NONE : (SynchView) synchAction.getSelectedItem());
            }
          }
        };
      // Attach Listeners
      jLinkModeButton.addActionListener(actionListener);
      DefaultView2d.linkModeButtonRef = jLinkModeButton;
      
      add(jLinkModeButton);
      
      // Add button to get the browser frame
      final JButton ShowBrowserButton = new JButton(new ImageIcon(MouseActions.class.getResource("/icon/16x16/browser.png")));
      ShowBrowserButton.setToolTipText("Show AI Window");
      
      ShowBrowserButton.addActionListener(e -> {
    	  if (loadedBrowser == null) {
    	  	loadedBrowser = getBrowserFrame();
    	  	loadedBrowser.setVisible(true);
    	  } else {
    		browserRem.loadURL("http://ai.medreport360.com/showAI.php?foldername="+DefaultView2d.loadDicomFolderName);
    		loadedBrowser.setVisible(true);
    	  }
         
      });
      add(ShowBrowserButton);
    }
  }

  private JPopupMenu getZoomPopupMenuButton(
      DropDownButton dropDownButton, final ImageViewerEventManager<?> eventManager) {
    JPopupMenu popupMouseButtons = new JPopupMenu();
    for (JMenuItem jMenuItem : getZoomListMenuItems(eventManager)) {
      popupMouseButtons.add(jMenuItem);
    }
    return popupMouseButtons;
  }

  public static List<JMenuItem> getZoomListMenuItems(
      final ImageViewerEventManager<?> eventManager) {

    List<JMenuItem> list = new ArrayList<>();
    final JMenuItem actualZoomMenu =
        new JMenuItem(
            Messages.getString("ViewerToolBar.zoom_1"),
            new ImageIcon(MouseActions.class.getResource("/icon/10x10/zoom-original.png")));
    actualZoomMenu.addActionListener(
        e -> {
          ActionState zoom = eventManager.getAction(ActionW.ZOOM);
          if (zoom instanceof SliderChangeListener) {
            ((SliderChangeListener) zoom).setRealValue(1.0);
          }
        });
    list.add(actualZoomMenu);

    ImageViewerPlugin<?> selCt = eventManager.getSelectedView2dContainer();
    Window win = selCt == null ? null : SwingUtilities.getWindowAncestor(selCt);
    if (win != null) {
      GraphicsConfiguration config = win.getGraphicsConfiguration();
      Monitor monitor = MeasureTool.viewSetting.getMonitor(config.getDevice());
      if (monitor != null) {
        double realFactor = monitor.getRealScaleFactor();
        if (realFactor > 0.0) {
          final JMenuItem realSizeMenu =
              new JMenuItem(
                  Messages.getString("ZoomToolBar.real_zoom"),
                  new ImageIcon(MouseActions.class.getResource("/icon/10x10/zoom-real.png")));
          realSizeMenu.addActionListener(
              e -> {
                /*
                 * Pass the value -100.0 (convention: -100.0 => real world size) directly to the property
                 * change, otherwise the value is adjusted by the BoundedRangeModel
                 */
                eventManager.firePropertyChange(
                    ActionW.SYNCH.cmd(), null, new SynchEvent(null, ActionW.ZOOM.cmd(), -100.0));
                AuditLog.LOGGER.info("action:{} val:-100.0", ActionW.ZOOM.cmd());
              });
          list.add(realSizeMenu);
        }
      }
    }

    final JMenuItem bestFitMenu =
        new JMenuItem(
            Messages.getString("ViewerToolBar.zoom_b"),
            new ImageIcon(MouseActions.class.getResource("/icon/10x10/zoom-bestfit.png")));
    bestFitMenu.addActionListener(
        e -> {
          // Pass the value -200.0 (convention: -200.0 = > best fit zoom value) directly to the
          // property change,
          // otherwise the value is adjusted by the BoundedRangeModel
          eventManager.firePropertyChange(
              ActionW.SYNCH.cmd(), null, new SynchEvent(null, ActionW.ZOOM.cmd(), -200.0));
          AuditLog.LOGGER.info("action:{} val:-200.0", ActionW.ZOOM.cmd());
        });
    list.add(bestFitMenu);

    // final JMenuItem areaZoomMenu =
    // new JMenuItem("Area selection", new ImageIcon(
    // MouseActions.class.getResource("/icon/10x10/zoom-select.png")));
    // areaZoomMenu.addActionListener(new ActionListener() {
    //
    // @Override
    // public void actionPerformed(ActionEvent e) {
    //
    // }
    // });
    // list.add(areaZoomMenu);

    return list;
  }
  
  // get the browser frame
  public JFrame getBrowserFrame() {
	  browserFrame.setTitle("AI");
	  Image icon = Toolkit.getDefaultToolkit().getImage(System.getProperty("weasis.codebase.local") + File.separator + "resources/images/logo-button.png");
	  browserFrame.setIconImage(icon);
	  
	  CefApp.addAppHandler(new CefAppHandlerAdapter(null) {
          @Override
          public void stateHasChanged(CefAppState state) {
              
          }
      });

      final CefSettings settings = new CefSettings();
      settings.windowless_rendering_enabled = false;
      
      final CefApp cefApp = CefApp.getInstance(settings);
      final CefClient client = cefApp.createClient();
      
      client.addContextMenuHandler(new ContextMenuHandler(browserFrame));

      final CefBrowser browser = client.createBrowser("http://ai.medreport360.com/showAI.php?foldername="+DefaultView2d.loadDicomFolderName, false, false);
      final Component browserUI = browser.getUIComponent();
      clientRem = client;
      browserRem = browser;
      
   // Set up the UI for this example implementation.
      JPanel contentPanel = new JPanel(new BorderLayout());
      JButton reloadBtn = new JButton("Reload");
      
      reloadBtn.setAlignmentX(LEFT_ALIGNMENT);
      reloadBtn.addActionListener(new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
              if (reloadBtn.getText().equalsIgnoreCase("reload")) {
                  int mask = OS.isMacintosh() ? ActionEvent.META_MASK : ActionEvent.CTRL_MASK;
                  if ((e.getModifiers() & mask) != 0) {
                      browser.reloadIgnoreCache();
                  } else {
                      browser.reload();
                  }
              } else {
            	  browser.stopLoad();
              }
          }
      });
      contentPanel.add(Box.createHorizontalStrut(1));
      contentPanel.add(reloadBtn, BorderLayout.NORTH);
      contentPanel.add(browserUI, BorderLayout.CENTER);
      
      browserFrame.getContentPane().add(contentPanel);
      browserFrame.pack();
      browserFrame.setLocationRelativeTo(null);
      browserFrame.setSize(1145, 756);
//      browserFrame.setVisible(true);

      browserFrame.addWindowListener(new WindowAdapter() {
          @Override
          public void windowClosing(WindowEvent e) {
        	  browserFrame.setVisible(false);
          }
      });
	  
	  
//	  browserFrame.setSize(500, 500);
//	  browserFrame.setLocationRelativeTo(null);
//	  browserFrame.add(new JLabel("Browser Elems here"));
	  return browserFrame;
  }
}

// TODO: Add required options - Below System generated code to be overridden 
class ContextMenuHandler implements CefContextMenuHandler {
	private final Frame owner_;

    public ContextMenuHandler(Frame owner) {
        owner_ = owner;
    }
    
	@Override
	public void onBeforeContextMenu(CefBrowser arg0, CefFrame arg1, CefContextMenuParams arg2, CefMenuModel arg3) {
		
		arg3.clear();
	}

	@Override
	public boolean onContextMenuCommand(CefBrowser arg0, CefFrame arg1, CefContextMenuParams arg2, int arg3, int arg4) {
		
		return false;
	}

	@Override
	public void onContextMenuDismissed(CefBrowser arg0, CefFrame arg1) {
		
		
	}
	
}

