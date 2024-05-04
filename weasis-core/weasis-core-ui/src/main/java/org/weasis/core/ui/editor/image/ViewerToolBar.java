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
import javax.swing.border.LineBorder;
import java.awt.Color;
import java.awt.Insets;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.event.ListDataEvent;

import org.osgi.framework.FrameworkUtil;
import org.osgi.service.prefs.Preferences;
import org.weasis.core.api.gui.util.ActionState;
import org.weasis.core.api.gui.util.ActionW;
import org.weasis.core.api.gui.util.ComboItemListener;
import org.weasis.core.api.gui.util.DropButtonIcon;
import org.weasis.core.api.gui.util.DropDownButton;
import org.weasis.core.api.gui.util.GroupPopup;
import org.weasis.core.api.gui.util.GroupRadioMenu;
import org.weasis.core.api.gui.util.RadioMenuItem;
import org.weasis.core.api.media.data.ImageElement;
import org.weasis.core.api.media.data.MediaSeries;
import org.weasis.core.api.service.BundlePreferences;
import org.weasis.core.api.service.BundleTools;
import org.weasis.core.api.service.WProperties;
import org.weasis.core.ui.Messages;
import org.weasis.core.ui.util.WtoolBar;


public class ViewerToolBar<E extends ImageElement> extends WtoolBar implements ActionListener {

  public static final List<ActionW> actionsButtons =
      Collections.synchronizedList(
          new ArrayList<>(
              Arrays.asList(
                  new ActionW[] {
                    ActionW.PAN,
                    ActionW.WINLEVEL,
                    ActionW.SCROLL_SERIES,
                    ActionW.ZOOM,
                    ActionW.ROTATION,
                    ActionW.MEASURE,
                    ActionW.DRAW,
                    ActionW.CONTEXTMENU,
                    ActionW.CROSSHAIR,
                    ActionW.NO_ACTION
                  })));

  public static final ActionW[] actionsScroll = {
    ActionW.SCROLL_SERIES, ActionW.ZOOM, ActionW.ROTATION, ActionW.NO_ACTION
  };
  public static final Icon MouseLeftIcon =
      new ImageIcon(MouseActions.class.getResource("/icon/16x16/mouse-left.png"));
  public static final Icon MouseRightIcon =
      new ImageIcon(MouseActions.class.getResource("/icon/16x16/mouse-right.png"));
  public static final Icon MouseMiddleIcon =
      new ImageIcon(MouseActions.class.getResource("/icon/16x16/mouse-middle.png"));
  public static final Icon MouseWheelIcon =
      new ImageIcon(MouseActions.class.getResource("/icon/16x16/mouse-wheel.png"));
  public static final Icon MouseSaveIcon =
	      new ImageIcon(MouseActions.class.getResource("/icon/16x16/mouse-save.png"));

  protected final ImageViewerEventManager<E> eventManager;
  private final DropDownButton mouseLeft;
  private final DropDownButton mouseMiddle;
  private final DropDownButton mouseRight;
  private final DropDownButton mouseWheel;
  public static DropDownButton synchButton;
  
  public static boolean mouseActionsSaved = false;

  public ViewerToolBar(
      final ImageViewerEventManager<E> eventManager,
      int activeMouse,
      WProperties props,
      int index) {
    super(Messages.getString("ViewerToolBar.title"), index);
    if (eventManager == null) {
      throw new IllegalArgumentException("EventManager cannot be null");
    }
    this.eventManager = eventManager;

    MouseActions actions = eventManager.getMouseActions();

    if ((activeMouse & InputEvent.BUTTON1_DOWN_MASK) == InputEvent.BUTTON1_DOWN_MASK) {
      mouseLeft = buildMouseButton(actions, MouseActions.T_LEFT);
      mouseLeft.setToolTipText(
          Messages.getString("ViewerToolBar.change")
              + " "
              + Messages.getString("ViewerToolBar.m_action"));
      add(mouseLeft);
    } else {
      mouseLeft = null;
    }

    if ((activeMouse & InputEvent.BUTTON2_DOWN_MASK) == InputEvent.BUTTON2_DOWN_MASK) {
      mouseMiddle = buildMouseButton(actions, MouseActions.T_MIDDLE);
      add(mouseMiddle);
    } else {
      mouseMiddle = null;
    }

    if ((activeMouse & InputEvent.BUTTON3_DOWN_MASK) == InputEvent.BUTTON3_DOWN_MASK) {
      mouseRight = buildMouseButton(actions, MouseActions.T_RIGHT);
      add(mouseRight);
    } else {
      mouseRight = null;
    }

    if ((activeMouse & MouseActions.SCROLL_MASK) == MouseActions.SCROLL_MASK) {
      mouseWheel =
          new DropDownButton(
              MouseActions.T_WHEEL,
              buildMouseIcon(MouseActions.T_WHEEL, actions.getAction(MouseActions.T_WHEEL))) {

            @Override
            protected JPopupMenu getPopupMenu() {
              return getPopupMenuScroll(this);
            }
          };
      mouseWheel.setToolTipText(Messages.getString("ViewerToolBar.change"));
      add(mouseWheel);
    } else {
      mouseWheel = null;
    }
    
    final JButton saveMouseActions = new JButton();
    saveMouseActions.setToolTipText("Save Mouse Actions");
    saveMouseActions.setIcon(MouseSaveIcon);
    saveMouseActions.addActionListener(new ActionListener () {
    	public void actionPerformed(ActionEvent e) {
    		Preferences bundlePrefs = BundlePreferences.getDefaultPreferences(FrameworkUtil.getBundle(eventManager.getSelectedView2dContainer().getClass()).getBundleContext());
    		eventManager.getMouseActions().savePreferences(bundlePrefs);
    		mouseActionsSaved = true;
    	}
    });
    add(saveMouseActions);
    
    if (activeMouse > 1) {
      addSeparator(WtoolBar.SEPARATOR_2x24);
    }

    // DropDownButton testLayoutBtn = null;
    if (props.getBooleanProperty("weasis.toolbar.layoutbouton", true)) {
      final DropDownButton layout =
          new DropDownButton(
              "layout", // NON-NLS
              new DropButtonIcon(
                  new ImageIcon(MouseActions.class.getResource("/icon/16x16/layout.png")))) {

            @Override
            protected JPopupMenu getPopupMenu() {
              return getLayoutPopupMenuButton(this);
            }
          };
      layout.setToolTipText(Messages.getString("ViewerToolBar.layout"));
      add(layout);
      // testLayoutBtn = layout;

      // Direct button for layout changes
      JMenu layoutOpts = ((ComboItemListener) eventManager.getAction(ActionW.LAYOUT)).createUnregisteredRadioMenu("layout");

      final JButton btn_1x1 = new JButton();
      btn_1x1.setToolTipText("1x1 View");
      btn_1x1.setIcon(new ImageIcon(WtoolBar.class.getResource("/icon/16x16/11.png")));
      btn_1x1.addActionListener(new ActionListener () {
        public void actionPerformed(ActionEvent e) {
          ((RadioMenuItem)layoutOpts.getMenuComponents()[0]).doClick(); 
        }
      });

      
      // Dimension testDim = btn_1x1.getPreferredSize();
      // testDim.width = 16;
      // testDim.height = 16;
      
      // btn_1x1.setPreferredSize(testDim);
      // btn_1x1.setBorderPainted(true);
      // btn_1x1.setBorder(new LineBorder(Color.WHITE));
      // btn_1x1.setMargin(new Insets(4, 4, 4, 4));
      add(btn_1x1);

      final JButton btn_1x2 = new JButton();
      btn_1x2.setToolTipText("1x2 View");
      btn_1x2.setIcon(new ImageIcon(WtoolBar.class.getResource("/icon/16x16/12.png")));
      btn_1x2.addActionListener(new ActionListener () {
        public void actionPerformed(ActionEvent e) {
          ((RadioMenuItem)layoutOpts.getMenuComponents()[1]).doClick();          
        }
      });
      // btn_1x2.setPreferredSize(testDim);
      // btn_1x2.setBorderPainted(true);
      // btn_1x2.setMargin(new Insets(4, 4, 4, 4));
      // btn_1x2.setBorder(new LineBorder(Color.WHITE));

      add(btn_1x2);

      // AI enable/disable
      String prefPath = BundleTools.SYSTEM_PREFERENCES.getProperty("weasis.pref.dir");
  	  String profileName = Paths.get(prefPath).getFileName().toString();
  	  if(profileName.equalsIgnoreCase("v.sarode@primeradiology.com.au") || profileName.equalsIgnoreCase("update201@mail.com")) {
	      final JToggleButton ai_view = new JToggleButton();
	      ai_view.setToolTipText("AI View");
	      ai_view.setIcon(new ImageIcon(WtoolBar.class.getResource("/icon/16x16/12i.png")));
	      ai_view.addActionListener(new ActionListener () {
	        public void actionPerformed(ActionEvent e) {
	        	AbstractButton abstractButton = (AbstractButton) e.getSource();
	            boolean selected = abstractButton.getModel().isSelected();
	            
	            // Using manual link key feature
	            if(selected) {
		        	DefaultView2d.aiViewEnabled = true;
		        	eventManager.getAction(ActionW.SYNCH, ComboItemListener.class).orElse(null).setSelectedItem(SynchView.DEFAULT_STACK); // select stack mode
	            } else {
	            	DefaultView2d.aiViewEnabled = false;
	            	DefaultView2d.referenceIndex = 0; //resetting here when manual link scroll is unchecked
	            }
	            
	            ImageViewerPlugin viewerContainer = eventManager.getSelectedView2dContainer();
	            MediaSeries<E> viewerDicomSeries = viewerContainer.getSelectedImagePane().getSeries();
	            if(viewerDicomSeries.getToolTips().toUpperCase().contains("Modality: CR".toUpperCase())) {
	            	ComboItemListener synchAction = eventManager.getAction(ActionW.SYNCH, ComboItemListener.class).orElse(null);
	                eventManager.updateAllListeners(viewerContainer,
	                		synchAction == null ? SynchView.NONE : (SynchView) synchAction.getSelectedItem());
	            }
	        }
	      });
	      add(ai_view);
  	  }
      
      
      final JButton btn_1x3 = new JButton();
      btn_1x3.setToolTipText("1x3 View");
      btn_1x3.setIcon(new ImageIcon(WtoolBar.class.getResource("/icon/16x16/13.png")));
      btn_1x3.addActionListener(new ActionListener () {
        public void actionPerformed(ActionEvent e) {
          ((RadioMenuItem)layoutOpts.getMenuComponents()[2]).doClick();          
        }
      });
      // btn_1x3.setPreferredSize(testDim);
      // btn_1x3.setBorderPainted(true);
      // btn_1x3.setMargin(new Insets(4, 4, 4, 4));
      // btn_1x3.setBorder(new LineBorder(Color.WHITE));
      add(btn_1x3);

      final JButton btn_1x4 = new JButton();
      btn_1x4.setToolTipText("1x4 View");
      btn_1x4.setIcon(new ImageIcon(WtoolBar.class.getResource("/icon/16x16/14.png")));
      btn_1x4.addActionListener(new ActionListener () {
        public void actionPerformed(ActionEvent e) {
          ((RadioMenuItem)layoutOpts.getMenuComponents()[3]).doClick();          
        }
      });
      // btn_1x4.setPreferredSize(testDim);
      // btn_1x4.setBorderPainted(true);
      // btn_1x4.setMargin(new Insets(4, 4, 4, 4));
      // btn_1x4.setBorder(new LineBorder(Color.WHITE));

      add(btn_1x4);

      // final JButton btn_2x2_f2 = new JButton();
      // btn_2x2_f2.setToolTipText("3 Views (col 2,1)");
      // btn_2x2_f2.setIcon(((RadioMenuItem)layoutOpts.getMenuComponents()[6]).getIcon());
      // btn_2x2_f2.addActionListener(new ActionListener () {
      //   public void actionPerformed(ActionEvent e) {
      //     ((RadioMenuItem)layoutOpts.getMenuComponents()[6]).doClick();          
      //   }
      // });
      // add(btn_2x2_f2);

      // final JButton btn_2_f1x2 = new JButton();
      // btn_2_f1x2.setToolTipText("3 Views (row 1,2)");
      // btn_2_f1x2.setIcon(((RadioMenuItem)layoutOpts.getMenuComponents()[5]).getIcon());
      // btn_2_f1x2.addActionListener(new ActionListener () {
      //   public void actionPerformed(ActionEvent e) {
      //     ((RadioMenuItem)layoutOpts.getMenuComponents()[5]).doClick();          
      //   }
      // });
      // add(btn_2_f1x2);

      final JButton btn_2x1 = new JButton();
      btn_2x1.setToolTipText("2x1 View");
      btn_2x1.setIcon(new ImageIcon(WtoolBar.class.getResource("/icon/16x16/21.png")));
      btn_2x1.addActionListener(new ActionListener () {
        public void actionPerformed(ActionEvent e) {
          ((RadioMenuItem)layoutOpts.getMenuComponents()[4]).doClick();          
        }
      });
      add(btn_2x1);

      final JButton btn_2x2 = new JButton();
      btn_2x2.setToolTipText("2x2 View");
      btn_2x2.setIcon(new ImageIcon(WtoolBar.class.getResource("/icon/16x16/22.png")));
      btn_2x2.addActionListener(new ActionListener () {
        public void actionPerformed(ActionEvent e) {
          ((RadioMenuItem)layoutOpts.getMenuComponents()[9]).doClick();          
        }
      });
      add(btn_2x2);

      final JButton btn_2x4 = new JButton();
      btn_2x4.setToolTipText("2x4 View");
      btn_2x4.setIcon(new ImageIcon(WtoolBar.class.getResource("/icon/16x16/24.png")));
      btn_2x4.addActionListener(new ActionListener () {
        public void actionPerformed(ActionEvent e) {
          ((RadioMenuItem)layoutOpts.getMenuComponents()[11]).doClick();          
        }
      });
      add(btn_2x4);

      final JButton btn_2x5 = new JButton();
      btn_2x5.setToolTipText("2x5 View");
      btn_2x5.setIcon(new ImageIcon(WtoolBar.class.getResource("/icon/16x16/25.png")));
      btn_2x5.addActionListener(new ActionListener () {
        public void actionPerformed(ActionEvent e) {
          ((RadioMenuItem)layoutOpts.getMenuComponents()[12]).doClick();          
        }
      });
      add(btn_2x5);

      final JButton btn_3x1 = new JButton();
      btn_3x1.setToolTipText("3x1 View");
      btn_3x1.setIcon(new ImageIcon(WtoolBar.class.getResource("/icon/16x16/31.png")));
      btn_3x1.addActionListener(new ActionListener () {
        public void actionPerformed(ActionEvent e) {
          ((RadioMenuItem)layoutOpts.getMenuComponents()[13]).doClick();          
        }
      });
      add(btn_3x1);

      final JButton btn_3x4 = new JButton();
      btn_3x4.setToolTipText("3x4 View");
      btn_3x4.setIcon(new ImageIcon(WtoolBar.class.getResource("/icon/16x16/34.png")));
      btn_3x4.addActionListener(new ActionListener () {
        public void actionPerformed(ActionEvent e) {
          ((RadioMenuItem)layoutOpts.getMenuComponents()[16]).doClick();          
        }
      });
      add(btn_3x4);

      final JButton btn_3x5 = new JButton();
      btn_3x5.setToolTipText("3x5 View");
      btn_3x5.setIcon(new ImageIcon(WtoolBar.class.getResource("/icon/16x16/35.png")));
      btn_3x5.addActionListener(new ActionListener () {
        public void actionPerformed(ActionEvent e) {
          ((RadioMenuItem)layoutOpts.getMenuComponents()[17]).doClick();          
        }
      });
      add(btn_3x5);

      final JButton btn_4x2 = new JButton();
      btn_4x2.setToolTipText("4x2 View");
      btn_4x2.setIcon(new ImageIcon(WtoolBar.class.getResource("/icon/16x16/42.png")));
      btn_4x2.addActionListener(new ActionListener () {
        public void actionPerformed(ActionEvent e) {
          ((RadioMenuItem)layoutOpts.getMenuComponents()[19]).doClick();          
        }
      });

      // Dimension testDim1 = btn_4x2.getPreferredSize();
      // testDim1.width = 16;
      // testDim1.height = 16;
      // btn_4x2.setPreferredSize(testDim1);
      // btn_4x2.setBorderPainted(true);
      // btn_4x2.setBorder(new LineBorder(Color.WHITE));

      add(btn_4x2);

      // final JButton btn_4x1 = new JButton();
      // btn_4x1.setToolTipText("4x1 View");
      // btn_4x1.setIcon(((RadioMenuItem)layoutOpts.getMenuComponents()[13]).getIcon());
      // btn_4x1.addActionListener(new ActionListener () {
      //   public void actionPerformed(ActionEvent e) {
      //     ((RadioMenuItem)layoutOpts.getMenuComponents()[13]).doClick();          
      //   }
      // });
      // add(btn_4x1);

    }

    if (props.getBooleanProperty("weasis.toolbar.synchbouton", true)) {
      add(Box.createRigidArea(new Dimension(5, 0)));

      synchButton = buildSynchButton();
      add(synchButton);

      final JButton defaultStackBtn = new JButton();
      defaultStackBtn.setToolTipText("Stack");
      defaultStackBtn.setText("Stack");
      defaultStackBtn.setIcon(((RadioMenuItem)synchButton.getMenuModel().createJPopupMenu().getComponent(1)).getIcon());
      defaultStackBtn.addActionListener(new ActionListener () {
        public void actionPerformed(ActionEvent e) {
          ((RadioMenuItem)synchButton.getMenuModel().createJPopupMenu().getComponent(1)).doClick();
        }
      });
      add(defaultStackBtn);

      final JButton defaultTileBtn = new JButton();
      defaultTileBtn.setToolTipText("Tile");
      defaultTileBtn.setText("Tile");
      defaultTileBtn.setIcon(((RadioMenuItem)synchButton.getMenuModel().createJPopupMenu().getComponent(2)).getIcon());
      defaultTileBtn.addActionListener(new ActionListener () {
        public void actionPerformed(ActionEvent e) {
          ((RadioMenuItem)synchButton.getMenuModel().createJPopupMenu().getComponent(2)).doClick();
        }
      });
      add(defaultTileBtn);
    } else {
      synchButton = null;
    }

    if (props.getBooleanProperty("weasis.toolbar.reset", true)) {
      final JButton resetButton = new JButton();
      resetButton.setToolTipText(Messages.getString("ViewerToolBar.disReset"));
      resetButton.setIcon(new ImageIcon(WtoolBar.class.getResource("/icon/16x16/reset.png")));
      resetButton.addActionListener(e -> eventManager.resetDisplay());
      ActionState layout = eventManager.getAction(ActionW.RESET);
      if (layout != null) {
        layout.registerActionState(resetButton);
      }
      add(resetButton);
    }

    /* trial on how to click the button
    {
      final JButton testButton = new JButton();
      testButton.setToolTipText("Test Button");
      // testButton.setIcon(new ImageIcon(WtoolBar.class.getResource("/icon/16x16/flip.png")));
      testButton.setIcon(((RadioMenuItem)((ComboItemListener) eventManager.getAction(ActionW.LAYOUT)).createUnregisteredRadioMenu("layout").getMenuComponents()[1]).getIcon());
      // testButton.addActionListener(e -> eventManager.resetDisplay());
      // ActionState layout = eventManager.getAction(ActionW.RESET);
      // if (layout != null) {
      //   layout.registerActionState(testButton);
      // }


      // newLayoutAction(View2dContainer.DEFAULT_LAYOUT_LIST.toArray(new GridBagLayoutModel[1]));

      final DropDownButton testLayoutBtn1 = testLayoutBtn;
      testButton.addActionListener(new ActionListener () {
      public void actionPerformed(ActionEvent e)
      {
        // testLayoutBtn1.doClick();
        ActionState layout = eventManager.getAction(ActionW.LAYOUT);
        ((RadioMenuItem)((ComboItemListener) layout).createUnregisteredRadioMenu("layout").getMenuComponents()[1]).doClick();
      }
    });
      add(testButton);
    }
    trial on how to click the button */
  }

  private DropDownButton buildMouseButton(MouseActions actions, String actionLabel) {
    String action = actions.getAction(actionLabel);
    final DropDownButton button =
        new DropDownButton(actionLabel, buildMouseIcon(actionLabel, action)) {

          @Override
          protected JPopupMenu getPopupMenu() {
            return getPopupMenuButton(this);
          }
        };
    button.setActionCommand(action);
    button.setToolTipText(Messages.getString("ViewerToolBar.change"));
    return button;
  }

  public DropDownButton getMouseLeft() {
    return mouseLeft;
  }

  private JPopupMenu getLayoutPopupMenuButton(DropDownButton dropDownButton) {
    ActionState layout = eventManager.getAction(ActionW.LAYOUT);
    JPopupMenu popupMouseButtons = new JPopupMenu();
    if (layout instanceof ComboItemListener) {
      JMenu menu = ((ComboItemListener) layout).createUnregisteredRadioMenu("layout"); // NON-NLS
      popupMouseButtons.setInvoker(dropDownButton);
      Component[] cps = menu.getMenuComponents();
      for (Component cp : cps) {
        popupMouseButtons.add(cp);
      }
    }
    return popupMouseButtons;
  }

  public void removeMouseAction(ActionW action) {
    if (action != null) {
      actionsButtons.remove(action);
      String cmd = action.cmd();
      MouseActions actions = eventManager.getMouseActions();
      if (cmd.equals(mouseLeft.getActionCommand())) {
        ActionW last = actionsButtons.get(0);
        actions.setAction(MouseActions.T_LEFT, last.cmd());
        changeButtonState(MouseActions.T_LEFT, last.cmd());
      }
    }
  }

  private JPopupMenu getPopupMenuButton(DropDownButton dropButton) {
    String type = dropButton.getType();
    String action = eventManager.getMouseActions().getAction(type);
    JPopupMenu popupMouseButtons = new JPopupMenu(type);
    popupMouseButtons.setInvoker(dropButton);
    ButtonGroup groupButtons = new ButtonGroup();
    synchronized (actionsButtons) {
      for (ActionW b : actionsButtons) {
        if (eventManager.isActionRegistered(b)) {
          JRadioButtonMenuItem radio =
              new JRadioButtonMenuItem(b.getTitle(), b.getIcon(), b.cmd().equals(action));
          radio.setActionCommand(b.cmd());
          radio.addActionListener(this);
          if (MouseActions.T_LEFT.equals(type)) {
            radio.setAccelerator(KeyStroke.getKeyStroke(b.getKeyCode(), b.getModifier()));
          }
          popupMouseButtons.add(radio);
          groupButtons.add(radio);
        }
      }
    }
    return popupMouseButtons;
  }

  private JPopupMenu getPopupMenuScroll(DropDownButton dropButton) {
    String type = dropButton.getType();
    String action = eventManager.getMouseActions().getAction(type);
    JPopupMenu popupMouseScroll = new JPopupMenu(type);
    popupMouseScroll.setInvoker(dropButton);
    ButtonGroup groupButtons = new ButtonGroup();
    for (ActionW actionW : actionsScroll) {
      if (eventManager.isActionRegistered(actionW)) {
        JRadioButtonMenuItem radio =
            new JRadioButtonMenuItem(
                actionW.getTitle(), actionW.getIcon(), actionW.cmd().equals(action));
        radio.setActionCommand(actionW.cmd());
        radio.addActionListener(this);
        popupMouseScroll.add(radio);
        groupButtons.add(radio);
      }
    }

    return popupMouseScroll;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (e.getSource() instanceof JRadioButtonMenuItem) {
      JRadioButtonMenuItem item = (JRadioButtonMenuItem) e.getSource();
      if (item.getParent() instanceof JPopupMenu) {
        JPopupMenu pop = (JPopupMenu) item.getParent();
        MouseActions mouseActions = eventManager.getMouseActions();
        mouseActions.setAction(pop.getLabel(), item.getActionCommand());
        ImageViewerPlugin<E> view = eventManager.getSelectedView2dContainer();
        if (view != null) {
          view.setMouseActions(mouseActions);
        }
        if (pop.getInvoker() instanceof DropDownButton) {
          changeButtonState(pop.getLabel(), item.getActionCommand());
        }
      }
    }
  }

  public boolean isCommandActive(String cmd) {
    int active = eventManager.getMouseActions().getActiveButtons();
    return cmd != null && checkButtonCommand(cmd, mouseLeft)
        || (((active & InputEvent.BUTTON2_DOWN_MASK) == InputEvent.BUTTON2_DOWN_MASK)
            && checkButtonCommand(cmd, mouseMiddle))
        || (((active & InputEvent.BUTTON3_DOWN_MASK) == InputEvent.BUTTON3_DOWN_MASK)
            && checkButtonCommand(cmd, mouseRight));
  }

  private static boolean checkButtonCommand(String cmd, JButton button) {
    return button != null && cmd.equals(button.getActionCommand());
  }

  public void changeButtonState(String type, String action) {
    DropDownButton button = getDropDownButton(type);
    if (button != null) {
      Icon icon = buildMouseIcon(type, action);
      button.setIcon(icon);
      button.setActionCommand(action);
    }
  }

  private Icon buildMouseIcon(String type, String action) {
    final Icon mouseIcon = getMouseIcon(type);
    ActionW actionW = getAction(actionsButtons, action);
    final Icon smallIcon = actionW == null ? ActionW.NO_ACTION.getIcon() : actionW.getIcon();

    return new DropButtonIcon(
        new Icon() {

          @Override
          public void paintIcon(Component c, Graphics g, int x, int y) {
            if (c instanceof AbstractButton) {
              AbstractButton model = (AbstractButton) c;
              Icon icon = null;
              if (!model.isEnabled()) {
                icon = UIManager.getLookAndFeel().getDisabledIcon(model, mouseIcon);
              }
              if (icon == null) {
                icon = mouseIcon;
              }
              icon.paintIcon(c, g, x, y);
              if (smallIcon != null) {
                Icon sIcon = null;
                if (!model.isEnabled()) {
                  sIcon = UIManager.getLookAndFeel().getDisabledIcon(model, smallIcon);
                }
                if (sIcon == null) {
                  sIcon = smallIcon;
                }
                int sx = x + mouseIcon.getIconWidth() - sIcon.getIconWidth();
                int sy = y + mouseIcon.getIconHeight() - sIcon.getIconHeight();
                sIcon.paintIcon(c, g, sx, sy);
              }
            }
          }

          @Override
          public int getIconWidth() {
            return mouseIcon.getIconWidth();
          }

          @Override
          public int getIconHeight() {
            return mouseIcon.getIconHeight();
          }
        });
  }

  private DropDownButton buildSynchButton() {
    GroupPopup menu = null;
    ActionState synch = eventManager.getAction(ActionW.SYNCH);
    SynchView synchView = SynchView.DEFAULT_STACK;
    if (synch instanceof ComboItemListener) {
      ComboItemListener m = (ComboItemListener) synch;
      Object sel = m.getSelectedItem();
      if (sel instanceof SynchView) {
        synchView = (SynchView) sel;
      }
      menu = new SynchGroupMenu();
      m.registerActionState(menu);
    }
    final DropDownButton button =
        new DropDownButton(ActionW.SYNCH.cmd(), buildSynchIcon(synchView), menu) {
          @Override
          protected JPopupMenu getPopupMenu() {
            JPopupMenu menu =
                (getMenuModel() == null) ? new JPopupMenu() : getMenuModel().createJPopupMenu();
            menu.setInvoker(this);
            return menu;
          }
        };
    button.setToolTipText(Messages.getString("ViewerToolBar.synch"));
    if (synch != null) {
      synch.registerActionState(button);
    }
    return button;
  }

  private static Icon buildSynchIcon(SynchView synch) {
    final Icon mouseIcon = new ImageIcon(MouseActions.class.getResource("/icon/16x16/synch.png"));
    final Icon smallIcon = synch.getIcon();
    return new DropButtonIcon(
        new Icon() {

          @Override
          public void paintIcon(Component c, Graphics g, int x, int y) {
            if (c instanceof AbstractButton) {
              AbstractButton model = (AbstractButton) c;
              Icon icon = null;
              if (!model.isEnabled()) {
                icon = UIManager.getLookAndFeel().getDisabledIcon(model, mouseIcon);
              }
              if (icon == null) {
                icon = mouseIcon;
              }
              if (smallIcon != null) {
                Icon sIcon = null;
                if (!model.isEnabled()) {
                  sIcon = UIManager.getLookAndFeel().getDisabledIcon(model, smallIcon);
                }
                if (sIcon == null) {
                  sIcon = smallIcon;
                }
                int x2 = x + mouseIcon.getIconWidth() / 2 - sIcon.getIconWidth() / 2;
                int y2 = y + mouseIcon.getIconHeight() / 2 - sIcon.getIconHeight() / 2;
                sIcon.paintIcon(c, g, x2, y2);
              }
              icon.paintIcon(c, g, x, y);
            }
          }

          @Override
          public int getIconWidth() {
            return mouseIcon.getIconWidth();
          }

          @Override
          public int getIconHeight() {
            return mouseIcon.getIconHeight();
          }
        });
  }

  public DropDownButton getDropDownButton(String type) {
    if (MouseActions.T_LEFT.equals(type)) {
      return mouseLeft;
    } else if (MouseActions.T_RIGHT.equals(type)) {
      return mouseRight;
    } else if (MouseActions.T_MIDDLE.equals(type)) {
      return mouseMiddle;
    } else if (MouseActions.T_WHEEL.equals(type)) {
      return mouseWheel;
    }
    return null;
  }

  public ActionW getAction(List<ActionW> buttons, String command) {
    if (buttons != null) {
      synchronized (buttons) { // NOSONAR lock object is the list for iterating its elements safely
        for (ActionW a : buttons) {
          if (a.cmd().equals(command)) {
            return a;
          }
        }
      }
    }
    return null;
  }

  private static Icon getMouseIcon(String type) {
    if (MouseActions.T_LEFT.equals(type)) {
      return MouseLeftIcon;
    } else if (MouseActions.T_RIGHT.equals(type)) {
      return MouseRightIcon;
    } else if (MouseActions.T_MIDDLE.equals(type)) {
      return MouseMiddleIcon;
    } else if (MouseActions.T_WHEEL.equals(type)) {
      return MouseWheelIcon;
    }
    return MouseLeftIcon;
  }

  public static ActionW getNextCommand(List<ActionW> buttons, String command) {
    if (buttons != null && !buttons.isEmpty()) {
      int index = 0;
      synchronized (buttons) { // NOSONAR lock object is the list for iterating its elements safely
        for (int i = 0; i < buttons.size(); i++) {
          ActionW b = buttons.get(i);
          if (b.cmd().equals(command)) {
            index = (i == buttons.size() - 1) ? 0 : i + 1;
            break;
          }
        }
        return buttons.get(index);
      }
    }
    return null;
  }

  class SynchGroupMenu extends GroupRadioMenu<SynchView> {

    @Override
    public void contentsChanged(ListDataEvent e) {
      super.contentsChanged(e);
      changeButtonState();
    }

    public void changeButtonState() {
      Object sel = dataModel.getSelectedItem();
      if (sel instanceof SynchView && synchButton != null) {
        Icon icon = buildSynchIcon((SynchView) sel);
        synchButton.setIcon(icon);
        synchButton.setActionCommand(sel.toString());
      }
    }
  }
}
