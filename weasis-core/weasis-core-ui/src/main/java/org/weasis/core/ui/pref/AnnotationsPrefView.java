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
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.BufferedReader;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTextField;
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
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

public class AnnotationsPrefView extends AbstractItemDialogPage { 
  private final JLabel jLabelPathologies = new JLabel("Pathologies" + StringUtil.COLON);
  private final JLabel jLabelModality = new JLabel("Modality" + StringUtil.COLON);
  private final JLabel jLabelStudy = new JLabel("Study" + StringUtil.COLON);
  private final JLabel jLabelSide = new JLabel("Side" + StringUtil.COLON);
  private final JLabel jLabelView = new JLabel("Views" + StringUtil.COLON);
  private final JLabel jLabelPathShortForm = new JLabel("Short from" + StringUtil.COLON);
  private final JLabel jLabelPathFullForm = new JLabel("Full from" + StringUtil.COLON);
  private final JLabel jLabelPathPattern = new JLabel("Pattern" + StringUtil.COLON);
  
  private JLabel structureTxt = new JLabel();
  private JLabel modalityTxt = new JLabel();
  private JLabel studyTxt = new JLabel();
  private JLabel sideTxt = new JLabel();
  private JLabel viewTxt = new JLabel();
  private JLabel patternTxt = new JLabel();
  
  private final JTextField structure = new JTextField();
  private final JTextField structureShortTxt = new JTextField();
  private final JComboBox patternDrpdwn = new JComboBox<>(new String[] {"Pattern1 : Mild, Mod...", "Pattern2 : Absent, Pres..."});
  private final JButton pathologySaveBtn = new JButton("Save");
  private final JComboBox modality = new JComboBox<>(new String[] {"X-ray", "CT", "US", "Mammo", "MRI"});
  private final JComboBox study = new JComboBox<>(new String[] {"Knee", "Shoulder"});
  private final JComboBox side = new JComboBox<>(new String[] {"", "Bilateral", "Others"});
  private final JTextField view = new JTextField();
  private final JComboBox pathologyDrpdwn = new JComboBox<>(new String[] {"Pathologies"});
  private final JButton patternDrpdwnEdit = new JButton("Edit");
  private final JButton saveStudyParams = new JButton("Commit To Server");
  private JPanel viewsPanel = new JPanel(new FlowLayout());
  private String viewsRetrievedTxt;

  public AnnotationsPrefView() {
    super("Annotations");
    setBorder(new EmptyBorder(5, 1, 1, 1));
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

    JPanel panel = new JPanel();
    panel.setBorder(
        new TitledBorder(
            null,
            "Study Parameters",
            TitledBorder.LEADING,
            TitledBorder.TOP,
            null,
            null));
    
    view.setPreferredSize(new Dimension(200, 30));
    structure.setPreferredSize(new Dimension(200, 30));
    structureShortTxt.setPreferredSize(new Dimension(200, 30));
    
    add(panel);
    GridBagLayout gblpanel = new GridBagLayout();
    gblpanel.columnWidths = new int[] {0, 0};
    gblpanel.rowHeights = new int[] {0, 0, 0, 0, 0, 0};
    gblpanel.columnWeights = new double[] {0.0, 0.0, 1.0};
    gblpanel.rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0};
    panel.setLayout(gblpanel);
    
    pathologySaveBtn.addActionListener(e -> structureSave());
    patternDrpdwnEdit.addActionListener(e -> enableEdit());
    side.addActionListener(e -> readPathologyOptionsFromFile());
    saveStudyParams.addActionListener(e ->savePathologyOptionsToFile());
    
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.anchor = GridBagConstraints.NORTHWEST;
//    ((FlowLayout) panel.getLayout()).setAlignment(FlowLayout.LEADING);
    
    gbc.gridx = 0;
    gbc.gridy = 0;
    panel.add(jLabelModality, gbc);
    
    gbc.gridx = 1;
    gbc.gridy = 0;
    panel.add(modality, gbc);
    
    gbc.gridx = 0;
    gbc.gridy = 1;
    panel.add(jLabelStudy, gbc);
    
    gbc.gridx = 1;
    gbc.gridy = 1;
    panel.add(study, gbc);
    
    gbc.gridx = 0;
    gbc.gridy = 2;
    panel.add(jLabelSide, gbc);
    
    gbc.gridx = 1;
    gbc.gridy = 2;
    panel.add(side, gbc);
    
//    gbc.gridx = 0;
//    gbc.gridy = 3;
//    panel.add(jLabelView, gbc);
    
    gbc.gridx = 0;
    gbc.gridy = 3;
    panel.add(jLabelPathShortForm, gbc);
    
//    gbc.gridx = 1;
//    gbc.gridy = 3;
//    panel.add(view, gbc);
    
    gbc.gridx = 1;
    gbc.gridy = 3;
    panel.add(structureShortTxt, gbc);
    
//    gbc.gridx = 0;
//    gbc.gridy = 4;
//    panel.add(jLabelPathologies, gbc);
    
    gbc.gridx = 0;
    gbc.gridy = 4;
    panel.add(jLabelPathFullForm, gbc);
    
//    gbc.gridx = 1;
//    gbc.gridy = 4;
//    panel.add(structure, gbc);
    
    gbc.gridx = 1;
    gbc.gridy = 4;
    panel.add(structure, gbc);
    
//    gbc.gridx = 2;
//    gbc.gridy = 4;
//    panel.add(structureShortTxt, gbc);
    
//    gbc.gridx = 3;
//    gbc.gridy = 4;
//    panel.add(patternDrpdwn, gbc);
//    
//    gbc.gridx = 5;
//    gbc.gridy = 4;
//    panel.add(pathologySaveBtn, gbc);
    
    gbc.gridx = 0;
    gbc.gridy = 5;
    panel.add(jLabelPathPattern, gbc);
    
//    gbc.gridx = 1;
//    gbc.gridy = 5;
//    panel.add(pathologyDrpdwn, gbc);
    
    gbc.gridx = 1;
    gbc.gridy = 5;
    panel.add(patternDrpdwn, gbc);
    
//    gbc.gridx = 2;
//    gbc.gridy = 5;
//    panel.add(patternDrpdwnEdit, gbc);
    
    gbc.gridx = 2;
    gbc.gridy = 5;
    panel.add(pathologySaveBtn, gbc);
    
//    gbc.gridx = 0;
//    gbc.gridy = 6;
//    panel.add(jLabelPattern, gbc); 
    
    gbc.gridx = 0;
    gbc.gridy = 6;
    panel.add(jLabelView, gbc);
    
//    gbc.gridx = 1;
//    gbc.gridy = 6;
//    panel.add(saveStudyParams, gbc);
    
    gbc.gridx = 1;
    gbc.gridy = 6;
    panel.add(viewsPanel, gbc);
    
    gbc.gridx = 0;
    gbc.gridy = 7;
    panel.add(jLabelPathologies, gbc);
    
    gbc.gridx = 1;
    gbc.gridy = 7;
    panel.add(pathologyDrpdwn, gbc);
    
    gbc.gridx = 1;
    gbc.gridy = 8;
    panel.add(saveStudyParams, gbc);
    
    JPanel panel2 = new JPanel();
    FlowLayout flowLayout1 = (FlowLayout) panel2.getLayout();
    flowLayout1.setHgap(10);
    flowLayout1.setAlignment(FlowLayout.RIGHT);
    flowLayout1.setVgap(7);
    add(panel2);

    JButton btnNewButton = new JButton(org.weasis.core.ui.Messages.getString("restore.values"));
    panel2.add(btnNewButton);
    btnNewButton.addActionListener(e -> resetoDefaultValues());
  }

  @Override
  public void resetoDefaultValues() {
  
  } 

  @Override
  public void closeAdditionalWindow() {
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
  
  public void structureSave() {
	  String strTxt = structure.getText().trim();
	  String strShortTxt = structureShortTxt.getText().trim();
	  String drpDwnTxt = (String) patternDrpdwn.getSelectedItem();
	  drpDwnTxt = drpDwnTxt.split(":")[0].trim();

	  structure.setText("");
	  structureShortTxt.setText("");
	  Component[] viewCheckBoxes = viewsPanel.getComponents();
	  for (int k = 0; k < viewCheckBoxes.length; k++) {
		  if(!((JCheckBox) viewCheckBoxes[k]).isSelected()) continue;
		  String drpDwnVal = ((JCheckBox) viewCheckBoxes[k]).getText()+":"+strTxt+"|"+strShortTxt+"|"+drpDwnTxt;
		  for (int i = 0; i < pathologyDrpdwn.getItemCount(); i++) {
	          if (pathologyDrpdwn.getItemAt(i).equals(drpDwnVal)) {
	              return;
	          }
	      }
		  pathologyDrpdwn.addItem(drpDwnVal);
	  }
  }
  
  public void enableEdit() {
	  String selectedPattern = (String) pathologyDrpdwn.getSelectedItem();
	  if(selectedPattern != null && !selectedPattern.equalsIgnoreCase("Pathologies") && selectedPattern.contains("|")) {
		  structure.setText(selectedPattern.split("\\|")[0]);
		  structureShortTxt.setText(selectedPattern.split("\\|")[1]);
		  patternDrpdwn.setSelectedItem(selectedPattern.split("\\|")[2]);
	  }
  }
  
  public void readPathologyOptionsFromFile() {
	  // Read the file
      StringBuilder content = new StringBuilder();
      try {
    	  BufferedReader br = new BufferedReader(new FileReader("D:/temp_file/xray_knee.txt"));
//    	  String studyPathologiesUrl = "http://ai-viewer-updater.medreport360.com/ko_objects/pathologies/xray_knee.txt";
//    	  URL url = new URL(studyPathologiesUrl);
//    	  InputStream input = url.openStream();
//    	  BufferedReader br = new BufferedReader(new InputStreamReader(input));
    	  
    	  String line;
    	  int lineCtr = 1;
          while ((line = br.readLine()) != null) {
              content.append(line).append("\n");
//              if(line.contains(":") && line.contains("|")) {
//            	  pathologyDrpdwn.addItem(line);
//              } else {
//            	  view.setText(line);         	  
//              }
              if(lineCtr == 1) {
            	  viewsRetrievedTxt = line;
            	  String[] viewOPts = line.split("\\|");
            	  viewsPanel.removeAll();
            	  for(String option : viewOPts) {
            		  viewsPanel.add(new JCheckBox(option.split(":")[0]));
            	  }
            	  viewsPanel.revalidate();
            	  viewsPanel.repaint();
              } else {
            	  pathologyDrpdwn.addItem(line);
              }
              
              lineCtr++;
          }    
      } catch (Exception e) {
          e.printStackTrace();
      }

  }
  
  public void savePathologyOptionsToFile() {
//	  String viewTxt = view.getText().trim();
	  StringBuilder patternsString = new StringBuilder();
	  patternsString.append(viewsRetrievedTxt+"\n");
      for (int i = 0; i < pathologyDrpdwn.getItemCount(); i++) {
    	  if(pathologyDrpdwn.getItemAt(i).equals("Pathologies")) continue;
    	  patternsString.append(pathologyDrpdwn.getItemAt(i));
    	  if(i != pathologyDrpdwn.getItemCount() - 1) {
    		  patternsString.append("\n");
    	  }
      }
      
      // Write the content to file
//      try (BufferedWriter writer = new BufferedWriter(new FileWriter("D:/temp_file/xray_knee.txt"))) {
//          writer.write(patternsString.toString());
//      } catch (Exception e) {
//          // 
//      }
      
      try {
		  URL url = new URL("http://ai-viewer-updater.medreport360.com/ko_objects/pathologies/index.php");
	      Map<String,Object> params = new LinkedHashMap<>();
	      params.put("fileName", "xray_knee.txt");
	      params.put("fileContent", patternsString);
	      
	      StringBuilder postData = new StringBuilder();
	      for (Map.Entry<String,Object> param : params.entrySet()) {
	          if (postData.length() != 0) postData.append('&');
	          postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
	          postData.append('=');
	          postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
	      }
	      byte[] postDataBytes = postData.toString().getBytes("UTF-8");
	
	      HttpURLConnection conn = (HttpURLConnection)url.openConnection();
	      conn.setRequestMethod("POST");
	      conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
	      conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
	      conn.setDoOutput(true);
	      conn.getOutputStream().write(postDataBytes);
	
	      Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
	
	      for (int c; (c = in.read()) >= 0;)
	          System.out.print((char)c);
	  } catch (Exception e) {
		e.printStackTrace();
	  }   
  }
}