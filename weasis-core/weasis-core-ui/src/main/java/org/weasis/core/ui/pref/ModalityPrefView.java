/*
 * Copyright (c) 2009-2020 Weasis Team and other contributors.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at https://www.eclipse.org/legal/epl-2.0, or the Apache
 * License, Version 2.0 which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */
package org.weasis.core.ui.pref;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JLabel;
import org.weasis.core.util.StringUtil;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import org.weasis.core.api.gui.util.AbstractItemDialogPage;
import org.weasis.core.api.service.BundleTools;
import org.weasis.core.ui.Messages;
import javax.swing.JComboBox;
import org.weasis.core.ui.editor.image.ViewerToolBar;
import org.weasis.core.ui.editor.image.MouseActions;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.event.ListDataEvent;
import org.weasis.core.api.gui.util.ActionState;
import org.weasis.core.api.gui.util.ActionW;
import org.weasis.core.api.gui.util.ComboItemListener;
import org.weasis.core.api.gui.util.DropButtonIcon;
import org.weasis.core.api.gui.util.DropDownButton;
import org.weasis.core.api.gui.util.GroupPopup;
import org.weasis.core.api.gui.util.GroupRadioMenu;
import org.weasis.core.api.media.data.ImageElement;
import org.weasis.core.api.service.WProperties;
import org.weasis.core.ui.util.WtoolBar;
import org.weasis.core.ui.editor.image.ImageViewerEventManager;
import org.weasis.core.ui.editor.image.SynchView;
import javax.swing.DefaultComboBoxModel;
import java.util.Optional;
import org.weasis.core.api.image.GridBagLayoutModel;
import org.weasis.core.ui.editor.image.ImageViewerPlugin;

public class ModalityPrefView extends AbstractItemDialogPage { 
  private final JLabel jLabelUsLayout = new JLabel("Layout" + StringUtil.COLON);
  private final JLabel jLabelCtLayout = new JLabel("Layout" + StringUtil.COLON);
  private final JLabel jLabelCrLayout = new JLabel("Layout" + StringUtil.COLON);
  private final JLabel jLabelUsSynch = new JLabel("Synch" + StringUtil.COLON);
  private final JLabel jLabelCtSynch = new JLabel("Synch" + StringUtil.COLON);
  private final JLabel jLabelCrSynch = new JLabel("Synch" + StringUtil.COLON);
  
  private JLabel usLayoutTxt = new JLabel();
  private JLabel ctLayoutTxt = new JLabel();
  private JLabel crLayoutTxt = new JLabel();
  private JLabel usSynchTxt = new JLabel();
  private JLabel ctSynchTxt = new JLabel();
  private JLabel crSynchTxt = new JLabel();
  
  
  private final DropDownButton usLayout, ctLayout, crLayout, synchButton, ctSynchButton, crSynchButton;
  private final ComboItemListener usComboLayout = new ComboItemListener<GridBagLayoutModel>(
    ActionW.LAYOUT, Optional.ofNullable(ImageViewerPlugin.DEFAULT_LAYOUT_LIST.toArray(new GridBagLayoutModel[0])).orElseGet(() -> new GridBagLayoutModel[0])) {
    @Override
    public void itemStateChanged(Object object) {
    	usLayoutTxt.setText(((GridBagLayoutModel) this.getModel().getSelectedItem()).getId());
    }
  };
  private final ComboItemListener ctComboLayout = new ComboItemListener<GridBagLayoutModel>(
    ActionW.LAYOUT, Optional.ofNullable(ImageViewerPlugin.DEFAULT_LAYOUT_LIST.toArray(new GridBagLayoutModel[0])).orElseGet(() -> new GridBagLayoutModel[0])) {
    @Override
    public void itemStateChanged(Object object) {
    	ctLayoutTxt.setText(((GridBagLayoutModel) this.getModel().getSelectedItem()).getId());
    }
  };
  private final ComboItemListener crComboLayout = new ComboItemListener<GridBagLayoutModel>(
    ActionW.LAYOUT, Optional.ofNullable(ImageViewerPlugin.DEFAULT_LAYOUT_LIST.toArray(new GridBagLayoutModel[0])).orElseGet(() -> new GridBagLayoutModel[0])) {
    @Override
    public void itemStateChanged(Object object) {
    	crLayoutTxt.setText(((GridBagLayoutModel) this.getModel().getSelectedItem()).getId());
    }
  };

  private final ComboItemListener usSynchView = new ComboItemListener<SynchView>(
        ActionW.SYNCH, Optional.ofNullable(Arrays.asList(SynchView.NONE, SynchView.DEFAULT_STACK, SynchView.DEFAULT_TILE).toArray(new SynchView[0])).orElseGet(() -> new SynchView[0])) {
    @Override
    public void itemStateChanged(Object object) {
    	usSynchTxt.setText(((SynchView) this.getModel().getSelectedItem()).getUIName());
    }
  };

  private final ComboItemListener ctSynchView = new ComboItemListener<SynchView>(
        ActionW.SYNCH, Optional.ofNullable(Arrays.asList(SynchView.NONE, SynchView.DEFAULT_STACK, SynchView.DEFAULT_TILE).toArray(new SynchView[0])).orElseGet(() -> new SynchView[0])) {
    @Override
    public void itemStateChanged(Object object) {
    	ctSynchTxt.setText(((SynchView) this.getModel().getSelectedItem()).getUIName());
    }
  };

  private final ComboItemListener crSynchView = new ComboItemListener<SynchView>(
        ActionW.SYNCH, Optional.ofNullable(Arrays.asList(SynchView.NONE, SynchView.DEFAULT_STACK, SynchView.DEFAULT_TILE).toArray(new SynchView[0])).orElseGet(() -> new SynchView[0])) {
    @Override
    public void itemStateChanged(Object object) {
    	crSynchTxt.setText(((SynchView) this.getModel().getSelectedItem()).getUIName());
    }
  };

  public ModalityPrefView() {
    super("Modality");
    setBorder(new EmptyBorder(5, 1, 1, 1));
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

    JPanel panel = new JPanel();
    panel.setBorder(
        new TitledBorder(
            null,
            "US",
            TitledBorder.LEADING,
            TitledBorder.TOP,
            null,
            null));

    add(panel);
  
    ((FlowLayout) panel.getLayout()).setAlignment(FlowLayout.LEADING);

    panel.add(jLabelUsLayout);

    usLayout = new DropDownButton("layout", new DropButtonIcon(new ImageIcon(MouseActions.class.getResource("/icon/32x32/layout.png")))) {
      @Override
      protected JPopupMenu getPopupMenu() {
        return getLayoutPopupMenuButton(this, usComboLayout);
      }
    };
    usLayout.setToolTipText("Define US Layout");

    panel.add(usLayout);
    panel.add(usLayoutTxt);

    jLabelUsSynch.setBorder(new EmptyBorder(0, 50, 0, 0));

    panel.add(jLabelUsSynch);
    
    synchButton = new DropDownButton(ActionW.SYNCH.cmd(), new DropButtonIcon(new ImageIcon(MouseActions.class.getResource("/icon/32x32/synch.png")))) {
      @Override
      protected JPopupMenu getPopupMenu() {
        return getLayoutPopupMenuButton(this, usSynchView);
      }
    };
    synchButton.setToolTipText("Define US Synch");
    
    panel.add(synchButton);
    panel.add(usSynchTxt);

    JPanel ctPanel = new JPanel();
    ctPanel.setBorder(
        new TitledBorder(
            null,
            "CT",
            TitledBorder.LEADING,
            TitledBorder.TOP,
            null,
            null));
    add(ctPanel);
    GridBagLayout gblCtPanel = new GridBagLayout();
    
    ((FlowLayout) ctPanel.getLayout()).setAlignment(FlowLayout.LEADING);

    ctPanel.add(jLabelCtLayout);

    ctLayout = new DropDownButton("layout", new DropButtonIcon(new ImageIcon(MouseActions.class.getResource("/icon/32x32/layout.png")))) {
      @Override
      protected JPopupMenu getPopupMenu() {
        return getLayoutPopupMenuButton(this, ctComboLayout);
      }
    };
    ctLayout.setToolTipText("Define CT Layout");

    ctPanel.add(ctLayout);
    ctPanel.add(ctLayoutTxt);

    jLabelCtSynch.setBorder(new EmptyBorder(0, 50, 0, 0));

    ctPanel.add(jLabelCtSynch);

    ctSynchButton = new DropDownButton(ActionW.SYNCH.cmd(), new DropButtonIcon(new ImageIcon(MouseActions.class.getResource("/icon/32x32/synch.png")))) {
      @Override
      protected JPopupMenu getPopupMenu() {
        return getLayoutPopupMenuButton(this, ctSynchView);
      }
    };
    ctSynchButton.setToolTipText("Define CT Synch");

    ctPanel.add(ctSynchButton);
    ctPanel.add(ctSynchTxt);

    JPanel crPanel = new JPanel();
    crPanel.setBorder(
        new TitledBorder(
            null,
            "X-ray",
            TitledBorder.LEADING,
            TitledBorder.TOP,
            null,
            null));
    add(crPanel);
    
    ((FlowLayout) crPanel.getLayout()).setAlignment(FlowLayout.LEADING);

    crPanel.add(jLabelCrLayout);

    crLayout = new DropDownButton("layout", new DropButtonIcon(new ImageIcon(MouseActions.class.getResource("/icon/32x32/layout.png")))) {
      @Override
      protected JPopupMenu getPopupMenu() {
        return getLayoutPopupMenuButton(this, crComboLayout);
      }
    };
    crLayout.setToolTipText("Define X-ray Layout");

    crPanel.add(crLayout);
    crPanel.add(crLayoutTxt);

    jLabelCrSynch.setBorder(new EmptyBorder(0, 50, 0, 0));

    crPanel.add(jLabelCrSynch);

    crSynchButton = new DropDownButton(ActionW.SYNCH.cmd(), new DropButtonIcon(new ImageIcon(MouseActions.class.getResource("/icon/32x32/synch.png")))) {
      @Override
      protected JPopupMenu getPopupMenu() {
        return getLayoutPopupMenuButton(this, crSynchView);
      }
    };
    crSynchButton.setToolTipText("Define X-ray Synch");

    crPanel.add(crSynchButton);
    crPanel.add(crSynchTxt);  
    
    JPanel panel2 = new JPanel();
    FlowLayout flowLayout1 = (FlowLayout) panel2.getLayout();
    flowLayout1.setHgap(10);
    flowLayout1.setAlignment(FlowLayout.RIGHT);
    flowLayout1.setVgap(7);
    add(panel2);

    JButton btnNewButton = new JButton(org.weasis.core.ui.Messages.getString("restore.values"));
    panel2.add(btnNewButton);
    btnNewButton.addActionListener(e -> resetoDefaultValues());
    

    
    // if (BundleTools.SYSTEM_PREFERENCES.getBooleanProperty("us.layout", true)) {
      String us_layout = BundleTools.SYSTEM_PREFERENCES.getProperty("us.layout");
      String ct_layout = BundleTools.SYSTEM_PREFERENCES.getProperty("ct.layout");
      String cr_layout = BundleTools.SYSTEM_PREFERENCES.getProperty("cr.layout");
      retrivedFromProps(usComboLayout, us_layout, usLayoutTxt);
      retrivedFromProps(ctComboLayout, ct_layout, ctLayoutTxt);
      retrivedFromProps(crComboLayout, cr_layout, crLayoutTxt);
    // }
      String us_synch = BundleTools.SYSTEM_PREFERENCES.getProperty("us.view.mode");
      String ct_synch = BundleTools.SYSTEM_PREFERENCES.getProperty("ct.view.mode");
      String cr_synch = BundleTools.SYSTEM_PREFERENCES.getProperty("cr.view.mode");
      retrivedFromProps(usSynchView, us_synch, usSynchTxt);
      retrivedFromProps(ctSynchView, ct_synch, ctSynchTxt);
      retrivedFromProps(crSynchView, cr_synch, crSynchTxt);
  }

  @Override
  public void resetoDefaultValues() {
    usComboLayout.getModel().setSelectedItem(usComboLayout.getFirstItem());
    ctComboLayout.getModel().setSelectedItem(usComboLayout.getFirstItem());
    crComboLayout.getModel().setSelectedItem(usComboLayout.getFirstItem());

    usSynchView.getModel().setSelectedItem(usSynchView.getFirstItem());
    ctSynchView.getModel().setSelectedItem(ctSynchView.getFirstItem());
    crSynchView.getModel().setSelectedItem(crSynchView.getFirstItem());
  } 

  @Override
  public void closeAdditionalWindow() {
    GridBagLayoutModel selectedUsLayoutObject = (GridBagLayoutModel) usComboLayout.getModel().getSelectedItem();
    String selectedUsLayoutTitle = selectedUsLayoutObject.getId();

    GridBagLayoutModel selectedCtLayoutObject = (GridBagLayoutModel) ctComboLayout.getModel().getSelectedItem();
    String selectedCtLayoutTitle = selectedCtLayoutObject.getId();

    GridBagLayoutModel selectedCrLayoutObject = (GridBagLayoutModel) crComboLayout.getModel().getSelectedItem();
    String selectedCrLayoutTitle = selectedCrLayoutObject.getId();

    SynchView selectedUsSynchObject = (SynchView) usSynchView.getModel().getSelectedItem();
    String selectedUsSynchTitle = selectedUsSynchObject.getUIName();

    SynchView selectedCtSynchObject = (SynchView) ctSynchView.getModel().getSelectedItem();
    String selectedCtSynchTitle = selectedCtSynchObject.getUIName();

    SynchView selectedCrSynchObject = (SynchView) crSynchView.getModel().getSelectedItem();
    String selectedCrSynchTitle = selectedCrSynchObject.getUIName();

    BundleTools.SYSTEM_PREFERENCES.setProperty("us.layout", selectedUsLayoutTitle);

    BundleTools.SYSTEM_PREFERENCES.setProperty("ct.layout", selectedCtLayoutTitle);

    BundleTools.SYSTEM_PREFERENCES.setProperty("cr.layout", selectedCrLayoutTitle);

    BundleTools.SYSTEM_PREFERENCES.setProperty("us.view.mode", selectedUsSynchTitle);

    BundleTools.SYSTEM_PREFERENCES.setProperty("ct.view.mode", selectedCtSynchTitle);

    BundleTools.SYSTEM_PREFERENCES.setProperty("cr.view.mode", selectedCrSynchTitle);


    BundleTools.saveSystemPreferences();
  }

   public JPopupMenu getLayoutPopupMenuButton(DropDownButton dropDownButton, ActionState layout) {
    JPopupMenu popupMouseButtons = new JPopupMenu();
    if (layout instanceof ComboItemListener) {
      JMenu menu = ((ComboItemListener) layout).createUnregisteredRadioMenu("layout"); // NON-NLS
      // popupMouseButtons.setInvoker(dropDownButton);
      Component[] cps = menu.getMenuComponents();
      for (Component cp : cps) {
        popupMouseButtons.add(cp);
      }
    }
    return popupMouseButtons;
  }

  //Retrieve saved properties into modality preferences
  public void retrivedFromProps(ComboItemListener thisModel, String propLayout, JLabel thisLabel) {
    Object[] list = thisModel.getAllItem();
    Object retrievedLayoutObj = null;
    for (int i = 0; i < list.length; i++) {
    	String layoutType = "";
      if(list[i] instanceof SynchView) {
        layoutType = list[i].toString();
      } else {
        layoutType = ((GridBagLayoutModel) list[i]).getId();
      }
    	if(layoutType.equals(propLayout)) {
    		retrievedLayoutObj = list[i];
        break;
    	}
    }
    
    if(retrievedLayoutObj != null) {
      thisModel.getModel().setSelectedItem(retrievedLayoutObj);
      thisLabel.setText(propLayout);
    }
  }
}