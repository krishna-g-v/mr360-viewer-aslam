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
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
import org.weasis.core.util.StringUtil;
import org.weasis.core.util.StringUtil.Suffix;
import org.weasis.core.ui.editor.image.ViewCanvas;
//import org.weasis.dicom.codec.DicomImageElement;
//import org.weasis.dicom.viewer2d.EventManager;
//import org.weasis.dicom.viewer2d.View2dContainer;

import tests.detailed.handler.DragHandler;
import tests.detailed.handler.JSDialogHandler;
import tests.detailed.handler.KeyboardHandler;
import tests.detailed.handler.MessageRouterHandler;
import tests.detailed.handler.MessageRouterHandlerEx;

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
import org.cef.browser.CefMessageRouter;
import org.cef.callback.CefContextMenuParams;
import org.cef.callback.CefMenuModel;
import org.cef.handler.CefAppHandlerAdapter;
import org.cef.handler.CefContextMenuHandler;
import org.cef.handler.CefDisplayHandlerAdapter;
import org.cef.handler.CefFocusHandlerAdapter;
import tests.detailed.ui.ControlPanel;
import org.cef.handler.CefLifeSpanHandlerAdapter;
import org.cef.handler.CefLoadHandlerAdapter;
import org.cef.handler.CefRenderHandler;

@SuppressWarnings("serial")
public class ZoomToolBar extends WtoolBar {
  public static JFrame browserFrame = new JFrame();
  public static JFrame loadedBrowser = null;
  public static CefClient clientRem = null;
  public CefBrowser browserRem = null;
  public static CefBrowser viewerAI = null;
  public static CefBrowser annotatorViewerAI = null;
  public static CefBrowser reportViewerAI = null;
  public static CefApp cefApptn = null;
  private static boolean browserFocus_ = true;
  private static final ArrayList<CefBrowser> bindBrowsers = new ArrayList<CefBrowser>();
  public ViewerPlugin<?> reportWinViewer;
  public ViewerPlugin<?> annotatorWinViewer;
  public ViewerPlugin<?> predictionWinViewer;
  public Component predictionView = null;
  public CefApp cefApp;

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
//    	  if (loadedBrowser == null) {
//    	  	loadedBrowser = getBrowserFrame();
//    	  	loadedBrowser.setVisible(true);
//    	  } else {
//    		browserRem.loadURL("http://ai.medreport360.com/showAI.php?foldername="+DefaultView2d.loadDicomFolderName);
//    		loadedBrowser.setVisible(true);
//    	  }
    	  EventQueue.invokeLater(() -> {
    		  predictionWinViewer = DefaultView2d.loadPredictionView;
        	  if(viewerAI == null) {
        		  CefApp.addAppHandler(new CefAppHandlerAdapter(null) {
            		  @Override
            	      public void stateHasChanged(CefAppState state) {
            	            System.out.println("state");
            	      }
            	  });
        		  
            	  final CefSettings settings = new CefSettings();
            	  settings.windowless_rendering_enabled = false;
            	  cefApp = CefApp.getInstance(settings);
            	  
        		  predictionView = getBrowserUI(cefApp);
        	      predictionWinViewer.add(predictionView, BorderLayout.CENTER);
        	  } else {
//        		  predictionWinViewer.add(getBrowserUI());
        		  viewerAI.loadURL("http://ai.medreport360.com/showAI.php?foldername="+DefaultView2d.loadDicomFolderName);
        	  }
        	          
//        	  predictionWinViewer.getDockable().setMaximizable(false);
        	  String predictionWinTitle = "Prediction";
        	  predictionWinViewer.getDockable().setTitleToolTip(predictionWinTitle);
        	  predictionWinViewer.setPluginName(StringUtil.getTruncatedString(predictionWinTitle, 25, Suffix.THREE_PTS));
        	         
        	  predictionWinViewer.showDockable();
//        	  predictionWinViewer.setSelectedAndGetFocus();
//        	  predictionWinViewer.setSelected(true);
        	  
        	  predictionWinViewer.addComponentListener(
        		        new ComponentAdapter() {
        		          @Override
        		          public void componentResized(ComponentEvent e) {
        		        	  System.out.println("window panel resized");
//        		        	  viewerAI.doClose();
//        		        	  viewerAI.stopLoad();
//        		        	  try {
    							viewerAI.createImmediately();
//    						} catch (Exception e1) {
//    							// TODO Auto-generated catch block
//    							e1.printStackTrace();
//    						}
        		        	  try {
        		        		  CefRenderHandler realHandler = viewerAI.getRenderHandler();
        							if(realHandler != null) {
        								realHandler.wait(5000);
        							}
    						} catch (InterruptedException e1) {
    							// TODO Auto-generated catch block
    							e1.printStackTrace();
    						}
//        		        	  viewerAI.reload();
//        		        	  Component cp = getBrowserUI(cefApp);
//        		        	  predictionWinViewer.removeAll();
//        		        	  predictionWinViewer.add(cp, BorderLayout.CENTER);
//        		        	  viewerAI.loadURL("http://ai.medreport360.com/showAI.php?foldername="+DefaultView2d.loadDicomFolderName);
        		          }
        		        });
        	  
        	  annotatorWinViewer = DefaultView2d.loadAnnotatorView;
        	  Component annotatorView = null;
        	  if(annotatorViewerAI == null) {
        		  annotatorView = getAnnotatorUI();
        		  annotatorWinViewer.add(annotatorView);
        	  } else {
//        		  annotatorWinViewer.add( getAnnotatorUI());
        		  annotatorViewerAI.loadURL("http://google.com");
        	  }
          	
        	  annotatorWinViewer.getDockable().setMaximizable(false);
        	  String annotatorWinTitle = "Annotator";
        	  annotatorWinViewer.getDockable().setTitleToolTip(annotatorWinTitle);
        	  annotatorWinViewer.setPluginName(StringUtil.getTruncatedString(annotatorWinTitle, 25, Suffix.THREE_PTS));
           
        	  annotatorWinViewer.showDockable();
//        	  annotatorWinViewer.setSelectedAndGetFocus();
//        	  annotatorWinViewer.setSelected(true);
        	  
        	  annotatorWinViewer.addComponentListener(
      		        new ComponentAdapter() {
      		          @Override
      		          public void componentResized(ComponentEvent e) {
//      		        	  annotatorViewerAI.doClose();
      		        	System.out.println("window panel resized");
      		        	try {
      		        		CefRenderHandler realHandler = annotatorViewerAI.getRenderHandler();
    						if(realHandler != null) {
    							realHandler.wait(5000);
    						}
    					} catch (InterruptedException e1) {
    						// TODO Auto-generated catch block
    						e1.printStackTrace();
    					}
//      		        	annotatorViewerAI.stopLoad();
//    		        	  try {
//    		        		  annotatorViewerAI.createImmediately();
//    					} catch (Exception e1) {
//    						// TODO Auto-generated catch block
//    						e1.printStackTrace();
//    					}
//    		        	  annotatorViewerAI.reload();
//    		        	  Component cp = getAnnotatorUI();
//    		        	  annotatorWinViewer.removeAll();
//    		        	  annotatorWinViewer.add(cp, BorderLayout.CENTER);
      		          }
      		        });

            reportWinViewer = DefaultView2d.loadReportView;
            Component reportView = null;
            if(reportViewerAI == null) {
              reportView = getReportUI();
              reportWinViewer.add(reportView);
            } else {
//            	reportWinViewer.add(getReportUI());
              reportViewerAI.loadURL("http://google.com");
            }
            
            reportWinViewer.getDockable().setMaximizable(false);
            String reportWinTitle = "Report";
            reportWinViewer.getDockable().setTitleToolTip(reportWinTitle);
            reportWinViewer.setPluginName(StringUtil.getTruncatedString(reportWinTitle, 25, Suffix.THREE_PTS));
           
            reportWinViewer.showDockable();
//            reportWinViewer.setSelectedAndGetFocus();
//            reportWinViewer.setSelected(true);
            
            reportWinViewer.addComponentListener(
      		        new ComponentAdapter() {
      		          @Override
      		          public void componentResized(ComponentEvent e) {
      		        	System.out.println("window panel resized");
//      		        	reportViewerAI.doClose();
//    		        	Component cp = getReportUI();
//    		        	reportWinViewer.removeAll();
//    		        	reportWinViewer.add(cp, BorderLayout.CENTER);
//      		        	reportViewerAI.stopLoad();
//    		        	  try {
//    		        		  reportViewerAI.createImmediately();
//    					} catch (Exception e1) {
//    						// TODO Auto-generated catch block
//    						e1.printStackTrace();
//    					}
      		        	try {
      		        		CefRenderHandler realHandler = reportViewerAI.getRenderHandler();
    						if(realHandler != null) {
    							realHandler.wait(5000);
    						}
    					} catch (InterruptedException e1) {
    						// TODO Auto-generated catch block
    						e1.printStackTrace();
    					}
//    		        	  reportViewerAI.reload();
      		        	  
      		          }
      		        });
    	  });
    	  
    	  
//  	  if (loadedBrowser == null) {
//	  	loadedBrowser = getBrowserFrame();
//	  	loadedBrowser.setVisible(true);
//	  } else {
//		browserRem.loadURL("http://ai.medreport360.com/showAI.php?foldername="+DefaultView2d.loadDicomFolderName);
//		loadedBrowser.setVisible(true);
//	  }
         
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
	  
//	  CefApp.addAppHandler(new CefAppHandlerAdapter(null) {
//          @Override
//          public void stateHasChanged(CefAppState state) {
//              
//          }
//      });
//
//      final CefSettings settings = new CefSettings();
//      settings.windowless_rendering_enabled = false;
//      
//      final CefApp cefApp = CefApp.getInstance(settings);
//      final CefClient client = cefApp.createClient();
      
//      client.addContextMenuHandler(new ContextMenuHandler(browserFrame));

      final CefBrowser browser = clientRem.createBrowser("http://google.com", false, false);
      final Component browserUI = browser.getUIComponent();
//      clientRem = client;
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
      browserFrame.setResizable(true);
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
  
  public Component getBrowserUI(CefApp cefApp) {
	  CefClient client = cefApp.createClient();
	  
	  CefMessageRouter msgRouter = CefMessageRouter.create();
      msgRouter.addHandler(new MessageRouterHandler(), true);
      msgRouter.addHandler(new MessageRouterHandlerEx(client), false);
      client.addMessageRouter(msgRouter);
      client.addContextMenuHandler(new ContextMenuHandler(cefApp));
      client.addDragHandler(new DragHandler());
      client.addJSDialogHandler(new JSDialogHandler());
      client.addKeyboardHandler(new KeyboardHandler());
      client.addLifeSpanHandler(new CefLifeSpanHandlerAdapter() {
          @Override
          public void onAfterCreated(CefBrowser browser) {
              System.out.println("BrowserFrame.onAfterCreated id=" + browser.getIdentifier());
          }

          @Override
          public void onAfterParentChanged(CefBrowser browser) {
              System.out.println(
                      "BrowserFrame.onAfterParentChanged id=" + browser.getIdentifier());
//              if (afterParentChangedAction_ != null) {
//                  SwingUtilities.invokeLater(afterParentChangedAction_);
//                  afterParentChangedAction_ = null;
//              }
//              browser.createImmediately();
//              browser.reload();
          }

          @Override
          public boolean doClose(CefBrowser browser) {
              boolean result = browser.doClose();
              System.out.println("BrowserFrame.doClose id=" + browser.getIdentifier()
                      + " CefBrowser.doClose=" + result);
              return result;
          }

          @Override
          public void onBeforeClose(CefBrowser browser) {
              System.out.println("BrowserFrame.onBeforeClose id=" + browser.getIdentifier());
//              if (--browserCount_ == 0) {
//                  System.out.println("BrowserFrame.onBeforeClose CefApp.dispose");
//                  CefApp.getInstance().dispose();
//              browser.createImmediately();
//              browser.reload();
//              }
          }
      });
      

      CefBrowser browser = client.createBrowser("http://ai.medreport360.com/showAI.php?foldername="+DefaultView2d.loadDicomFolderName, false, false, null);
	  Component browserUI = browser.getUIComponent();
	  ControlPanel control_pane_ = new ControlPanel(browser);
      // Clear focus from the address field when the browser gains focus.
//      client.addFocusHandler(new CefFocusHandlerAdapter() {
//          @Override
//          public void onGotFocus(CefBrowser browser) {
////              try {
////				if (browserFocus_) return;
////			} catch (Exception e) {
////				// TODO Auto-generated catch block
////				e.printStackTrace();
////			}
//              browserFocus_ = true;
//              KeyboardFocusManager.getCurrentKeyboardFocusManager().clearGlobalFocusOwner();
//              browser.setFocus(true);
//              control_pane_.getAddressField().requestFocus();
//              browser.reload();
//          }
//
//          @Override
//          public void onTakeFocus(CefBrowser browser, boolean next) {
//              browserFocus_ = false;
//              
//          }
//      });
	  
	  

	  
      client.addDisplayHandler(new CefDisplayHandlerAdapter() {
          @Override
          public void onAddressChange(CefBrowser browser, CefFrame frame, String url) {
//        	  ControlPanel control_pane_ = new ControlPanel(browser);
//              control_pane_.setAddress(browser, url);
              browser.reload();
          }
      });
      
   // ↓ Prevent focus loop ↓
      client.addFocusHandler(new CefFocusHandlerAdapter() {
         @Override
         public void onGotFocus(CefBrowser browser) {
            System.out.println("onGotFocus");
         }
         @Override
         public boolean onSetFocus(final CefBrowser browser, FocusSource focusSource) {
            if(browser == null) return false;
            for(CefBrowser bindBrowser : bindBrowsers) {
               if(bindBrowser.getUIComponent().hasFocus()) {
                  return false;
               }
            }
            return true;
         }
      });
      // ↑ Prevent focus loop ↑
      
      client.addLoadHandler(new CefLoadHandlerAdapter() {
          @Override
          public void onLoadingStateChange(CefBrowser browser, boolean isLoading,
                  boolean canGoBack, boolean canGoForward) {
//        	  ControlPanel control_pane_ = new ControlPanel(browser);
//              control_pane_.update(browser, isLoading, canGoBack, canGoForward);
//              browser.reload();
//              status_panel_.setIsInProgress(isLoading);
//
//              if (!isLoading && !errorMsg_.isEmpty()) {
//                  browser.loadURL(DataUri.create("text/html", errorMsg_));
//                  errorMsg_ = "";
//              }
        	  System.out.println("loading state");
          }

          @Override
          public void onLoadError(CefBrowser browser, CefFrame frame, ErrorCode errorCode,
                  String errorText, String failedUrl) {
//              if (errorCode != ErrorCode.ERR_NONE && errorCode != ErrorCode.ERR_ABORTED) {
//                  errorMsg_ = "<html><head>";
//                  errorMsg_ += "<title>Error while loading</title>";
//                  errorMsg_ += "</head><body>";
//                  errorMsg_ += "<h1>" + errorCode + "</h1>";
//                  errorMsg_ += "<h3>Failed to load " + failedUrl + "</h3>";
//                  errorMsg_ += "<p>" + (errorText == null ? "" : errorText) + "</p>";
//                  errorMsg_ += "</body></html>";
//                  browser.stopLoad();
//              }
        	  System.out.println("loading state err "+failedUrl+" "+errorCode);
//        	  browser.stopLoad();
//        	  browser.createImmediately();
        	  String io = browser.getURL();
        	  
        	  
          }
      });
      
      client.onTooltip(annotatorViewerAI, "nullllllllll");
      
	  viewerAI = browser;
	  cefApptn = cefApp;
	  clientRem = client;
	  bindBrowsers.add(viewerAI);
	 
	  browserUI.addComponentListener(
		        new ComponentAdapter() {

		          @Override
		          public void componentResized(ComponentEvent e) {
//		        	  if(!browserUI.isShowing()) return;
//		        	  predictionWinViewer.removeAll();
//		        	  predictionWinViewer.add(browserUI);
//		        	  browser.reload();
//		        	  browserUI.setSize(500, 500);
		        	  browserUI.repaint();
		        	  System.out.println("browser resized");
		          }
		        });
//	  browser.setWindowVisibility(true);
	  
	  return browserUI;
  }

  public Component getAnnotatorUI() {
//      final CefClient client = cefApptn.createClient();
  final CefBrowser browser = clientRem.createBrowser("http://google.com", false, false, null);
      Component browserUI = browser.getUIComponent();
      annotatorViewerAI = browser;
      bindBrowsers.add(annotatorViewerAI);
      browserUI.addComponentListener(
		        new ComponentAdapter() {
//
		          @Override
		          public void componentResized(ComponentEvent e) {
//		        	  annotatorWinViewer.add(browserUI);
//		        	  if(!browserUI.isShowing()) return;
////		        	  annotatorWinViewer.removeAll();
////		        	  annotatorWinViewer.add(browserUI);		        	  
////		        	  browser.reload();
//		        	  browserUI.setSize(500, 500);
		        	  browserUI.repaint();
		        	  System.out.println("browser resized");
		          }
		        });
   // ↓ Prevent focus loop ↓
//      client.addFocusHandler(new CefFocusHandlerAdapter() {
//         @Override
//         public void onGotFocus(CefBrowser browser) {
//            System.out.println("onGotFocus");
//         }
//         @Override
//         public boolean onSetFocus(final CefBrowser browser, FocusSource focusSource) {
//            if(browser == null) return false;
//            for(CefBrowser bindBrowser : bindBrowsers) {
//               if(bindBrowser.getUIComponent().hasFocus()) {
//                  return false;
//               }
//            }
//            return true;
//         }
//      });
      // ↑ Prevent focus loop ↑
//      browser.setWindowVisibility(true);
	    return browserUI;
  }

  public Component getReportUI() {
//      final CefClient client = cefApptn.createClient();
      final CefBrowser browser = clientRem.createBrowser("http://google.com", false, false, null);
      Component browserUI = browser.getUIComponent();
      reportViewerAI = browser;
      bindBrowsers.add(reportViewerAI);
      browserUI.addComponentListener(
		        new ComponentAdapter() {

		          @Override
		          public void componentResized(ComponentEvent e) {
//		        	  if(!browserUI.isShowing()) return;
//		        	  reportWinViewer.removeAll();
//		        	  reportWinViewer.add(browserUI);
//		        	  browser.reload();
//		        	  browserUI.setSize(500, 500);
//		        	  reportWinViewer.add(clientRem.createBrowser("http://google.com", false, false).getUIComponent());
		        	  System.out.println("browser resized");
		        	  browserUI.repaint();
		          }
		        });
//      browser.setWindowVisibility(true);
   // ↓ Prevent focus loop ↓
//      client.addFocusHandler(new CefFocusHandlerAdapter() {
//         @Override
//         public void onGotFocus(CefBrowser browser) {
//            System.out.println("onGotFocus");
//         }
//         @Override
//         public boolean onSetFocus(final CefBrowser browser, FocusSource focusSource) {
//            if(browser == null) return false;
//            for(CefBrowser bindBrowser : bindBrowsers) {
//               if(bindBrowser.getUIComponent().hasFocus()) {
//                  return false;
//               }
//            }
//            return true;
//         }
//      });
      // ↑ Prevent focus loop ↑
      
      return browserUI;
  }
}

// TODO: Add required options - Below System generated code to be overridden 
class ContextMenuHandler implements CefContextMenuHandler {
	private final CefApp owner_;

    public ContextMenuHandler(CefApp owner) {
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

