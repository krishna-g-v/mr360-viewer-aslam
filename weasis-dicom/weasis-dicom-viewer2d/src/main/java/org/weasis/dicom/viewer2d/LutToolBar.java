/*
 * Copyright (c) 2009-2020 Weasis Team and other contributors.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at https://www.eclipse.org/legal/epl-2.0, or the Apache
 * License, Version 2.0 which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */
package org.weasis.dicom.viewer2d;

import java.awt.Component;
import java.awt.Graphics;
import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import javax.swing.UIManager;
import org.weasis.core.api.gui.util.ActionState;
import org.weasis.core.api.gui.util.ActionW;
import org.weasis.core.api.gui.util.ComboItemListener;
import org.weasis.core.api.gui.util.DropButtonIcon;
import org.weasis.core.api.gui.util.DropDownButton;
import org.weasis.core.api.gui.util.GroupPopup;
import org.weasis.core.api.gui.util.ToggleButtonListener;
import org.weasis.core.ui.editor.image.ImageViewerEventManager;
import org.weasis.core.ui.editor.image.MouseActions;
import org.weasis.core.ui.editor.image.ViewerToolBar;
import org.weasis.core.ui.util.WtoolBar;
import org.weasis.dicom.codec.DicomImageElement;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JMenu;
import org.weasis.core.api.gui.util.RadioMenuItem;
import org.weasis.core.api.gui.util.GroupRadioMenu;
import java.awt.Insets;

public class LutToolBar extends WtoolBar {

  public LutToolBar(final ImageViewerEventManager<DicomImageElement> eventManager, int index) {
    super(Messages.getString("LutToolBar.lookupbar"), index);
    if (eventManager == null) {
      throw new IllegalArgumentException("EventManager cannot be null");
    }

    GroupPopup menu = null;
    ActionState presetAction = eventManager.getAction(ActionW.PRESET);
    if (presetAction instanceof ComboItemListener) {
      menu = ((ComboItemListener) presetAction).createGroupRadioMenu();
    }

    final JButton winLevelBtn = new JButton();
    winLevelBtn.setToolTipText("Window Level (W)");
    winLevelBtn.setText("W");
    winLevelBtn.setIcon(new ImageIcon(WtoolBar.class.getResource("/icon/16x16/winLevel.png")));
    winLevelBtn.addActionListener(new ActionListener () {
      public void actionPerformed(ActionEvent e) {
//    	  eventManager.getSelectedView2dContainer().getViewerToolBar().changeButtonState(MouseActions.T_LEFT, ActionW.WINLEVEL.cmd());
    	  eventManager.changeLeftMouseAction(ActionW.WINLEVEL.cmd());
      }
    });
    add(winLevelBtn);

    final DropDownButton presetButton =
        new DropDownButton(ActionW.WINLEVEL.cmd(), buildWLIcon(), menu) {
          @Override
          protected JPopupMenu getPopupMenu() {
            JPopupMenu menu =
                (getMenuModel() == null) ? new JPopupMenu() : getMenuModel().createJPopupMenu();
            menu.setInvoker(this);
            return menu;
          }
        };

    presetButton.setToolTipText(Messages.getString("LutToolBar.presets"));
    add(presetButton);
    if (presetAction != null) {
      presetAction.registerActionState(presetButton);
    }

    // Preset buttons on main menu bar
//    final JButton presetBtn0 = new JButton();
//    presetBtn0.setToolTipText("Auto");
//    presetBtn0.setText("Auto");
//    presetBtn0.setIcon(new ImageIcon(WtoolBar.class.getResource("/icon/16x16/0.png")));
//    presetBtn0.addActionListener(new ActionListener () {
//      public void actionPerformed(ActionEvent e) {
//        ((RadioMenuItem)presetButton.getMenuModel().createJPopupMenu().getComponent(1)).doClick(); // index 1 is Auto
//      }
//    });
//    add(presetBtn0);

    final JButton presetBtn1 = new JButton();
    presetBtn1.setToolTipText("Default");
    presetBtn1.setText("Default");
    presetBtn1.setIcon(new ImageIcon(WtoolBar.class.getResource("/icon/16x16/1.png")));
    presetBtn1.addActionListener(new ActionListener () {
      public void actionPerformed(ActionEvent e) {
        ((RadioMenuItem)presetButton.getMenuModel().createJPopupMenu().getComponent(0)).doClick(); // index 0 is Default
      }
    });
    add(presetBtn1);

    final JButton presetBtn2 = new JButton();
    presetBtn2.setToolTipText("Brain");
    presetBtn2.setText("Brain");
    presetBtn2.setIcon(new ImageIcon(WtoolBar.class.getResource("/icon/16x16/2.png")));
    presetBtn2.addActionListener(new ActionListener () {
      public void actionPerformed(ActionEvent e) {
        ((RadioMenuItem)presetButton.getMenuModel().createJPopupMenu().getComponent(2)).doClick();
      }
    });
    add(presetBtn2);

    final JButton presetBtn3 = new JButton();
    presetBtn3.setToolTipText("Lung");
    presetBtn3.setText("Lung");
    presetBtn3.setIcon(new ImageIcon(WtoolBar.class.getResource("/icon/16x16/3.png")));
    presetBtn3.addActionListener(new ActionListener () {
      public void actionPerformed(ActionEvent e) {
        ((RadioMenuItem)presetButton.getMenuModel().createJPopupMenu().getComponent(3)).doClick();
      }
    });
    add(presetBtn3);

    final JButton presetBtn4 = new JButton();
    presetBtn4.setToolTipText("Mediastinum");
    presetBtn4.setText("Mediastinum");
    presetBtn4.setIcon(new ImageIcon(WtoolBar.class.getResource("/icon/16x16/4.png")));
    presetBtn4.addActionListener(new ActionListener () {
      public void actionPerformed(ActionEvent e) {
        ((RadioMenuItem)presetButton.getMenuModel().createJPopupMenu().getComponent(4)).doClick();
      }
    });
    add(presetBtn4);

    final JButton presetBtn5 = new JButton();
    presetBtn5.setToolTipText("Abdomen");
    presetBtn5.setText("Abdomen");
    presetBtn5.setIcon(new ImageIcon(WtoolBar.class.getResource("/icon/16x16/5.png")));
    presetBtn5.addActionListener(new ActionListener () {
      public void actionPerformed(ActionEvent e) {
        ((RadioMenuItem)presetButton.getMenuModel().createJPopupMenu().getComponent(5)).doClick();
      }
    });
    add(presetBtn5);

    final JButton presetBtn6 = new JButton();
    presetBtn6.setToolTipText("Bone");
    presetBtn6.setText("Bone");
    presetBtn6.setIcon(new ImageIcon(WtoolBar.class.getResource("/icon/16x16/6.png")));
    presetBtn6.addActionListener(new ActionListener () {
      public void actionPerformed(ActionEvent e) {
        ((RadioMenuItem)presetButton.getMenuModel().createJPopupMenu().getComponent(6)).doClick();
      }
    });
    add(presetBtn6);

    final JButton presetBtn7 = new JButton();
    presetBtn7.setToolTipText("MIP");
    presetBtn7.setText("MIP");
    presetBtn7.setIcon(new ImageIcon(WtoolBar.class.getResource("/icon/16x16/7.png")));
    presetBtn7.addActionListener(new ActionListener () {
      public void actionPerformed(ActionEvent e) {
        ((RadioMenuItem)presetButton.getMenuModel().createJPopupMenu().getComponent(7)).doClick();
      }
    });
    add(presetBtn7);

    GroupPopup menuLut = null;
    ActionState lutAction = eventManager.getAction(ActionW.LUT);
    if (lutAction instanceof ComboItemListener) {
      menuLut = ((ComboItemListener) lutAction).createGroupRadioMenu();
    }

    final DropDownButton lutButton =
        new DropDownButton(ActionW.LUT.cmd(), buildLutIcon(), menuLut) {
          @Override
          protected JPopupMenu getPopupMenu() {
            JPopupMenu menu =
                (getMenuModel() == null) ? new JPopupMenu() : getMenuModel().createJPopupMenu();
            menu.setInvoker(this);
            return menu;
          }
        };

    lutButton.setToolTipText(Messages.getString("LutToolBar.lustSelection"));
    add(lutButton);
    if (lutAction != null) {
      lutAction.registerActionState(lutButton);
    }

    final JToggleButton invertButton = new JToggleButton();
    invertButton.setToolTipText(ActionW.INVERT_LUT.getTitle());
    invertButton.setIcon(new ImageIcon(WtoolBar.class.getResource("/icon/16x16/invert.png")));
    ActionState invlutAction = eventManager.getAction(ActionW.INVERT_LUT);
    if (invlutAction instanceof ToggleButtonListener) {
      ((ToggleButtonListener) invlutAction).registerActionState(invertButton);
    }
    add(invertButton);
  }

  private Icon buildLutIcon() {
    final Icon mouseIcon = new ImageIcon(WtoolBar.class.getResource("/icon/16x16/lut.png"));

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

  private Icon buildWLIcon() {
    final Icon mouseIcon = new ImageIcon(WtoolBar.class.getResource("/icon/16x16/preset.png"));

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
}
