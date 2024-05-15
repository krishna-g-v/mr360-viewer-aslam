/*
 * Copyright (c) 2009-2020 Weasis Team and other contributors.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at https://www.eclipse.org/legal/epl-2.0, or the Apache
 * License, Version 2.0 which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */
package org.weasis.core.ui.editor.image.dockable;

import bibliothek.gui.dock.common.CLocation;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;
import java.util.Optional;

import javax.swing.AbstractAction;
import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import org.weasis.core.api.gui.util.ActionState;
import org.weasis.core.api.gui.util.ActionW;
import org.weasis.core.api.gui.util.ComboItemListener;
import org.weasis.core.api.gui.util.JMVUtils;
import org.weasis.core.api.gui.util.JToogleButtonGroup;
import org.weasis.core.api.gui.util.TableHeaderRenderer;
import org.weasis.core.api.gui.util.ToggleButtonListener;
import org.weasis.core.api.image.util.MeasurableLayer;
import org.weasis.core.api.image.util.Unit;
import org.weasis.core.api.media.data.ImageElement;
import org.weasis.core.api.service.BundleTools;
import org.weasis.core.api.util.FontTools;
import org.weasis.core.ui.Messages;
import org.weasis.core.ui.docking.PluginTool;
import org.weasis.core.ui.docking.UIManager;
import org.weasis.core.ui.editor.image.ImageViewerEventManager;
import org.weasis.core.ui.editor.image.ImageViewerPlugin;
import org.weasis.core.ui.editor.image.MeasureToolBar;
import org.weasis.core.ui.editor.image.MouseActions;
import org.weasis.core.ui.editor.image.ViewCanvas;
import org.weasis.core.ui.editor.image.ViewerPlugin;
import org.weasis.core.ui.editor.image.ViewerToolBar;
import org.weasis.core.ui.model.graphic.DragGraphic;
import org.weasis.core.ui.model.graphic.Graphic;
import org.weasis.core.ui.model.graphic.GraphicSelectionListener;
import org.weasis.core.ui.model.graphic.imp.area.ObliqueRectangleGraphic;
import org.weasis.core.ui.model.layer.LayerType;
import org.weasis.core.ui.model.utils.ImageStatistics;
import org.weasis.core.ui.model.utils.bean.MeasureItem;
import org.weasis.core.ui.model.utils.bean.Measurement;
import org.weasis.core.ui.pref.PreferenceDialog;
import org.weasis.core.ui.pref.ViewSetting;
import org.weasis.core.ui.util.ColorLayerUI;
import org.weasis.core.ui.util.SimpleTableModel;
import org.weasis.core.ui.util.TableColumnAdjuster;
import org.weasis.core.ui.util.TableNumberRenderer;
import org.weasis.core.util.StringUtil;

public class MeasureTool extends PluginTool implements GraphicSelectionListener {
  private static final long serialVersionUID = 1117961156637401550L;

  public static final String BUTTON_NAME = ActionW.DRAW + " & " + ActionW.MEASURE;
  public static final String LABEL_PREF_NAME = Messages.getString("MeasureTool.lab_img");
  public static final ViewSetting viewSetting = new ViewSetting();

  protected final ImageViewerEventManager<? extends ImageElement> eventManager;
  private final JScrollPane rootPane;
  private JPanel tableContainer;
  private JTable jtable;
  public static JPanel iconsTableContainer;
  public static JTable iconsTable;
  public static List<JPanel> iconsTables = new ArrayList<>();
  public static Object[][] labelsArray = new Object[1][];
  public static int pathCount = 0;

  private List<DragGraphic> selectedGraphic;

  public MeasureTool(ImageViewerEventManager<? extends ImageElement> eventManager) {
    super(BUTTON_NAME, BUTTON_NAME, PluginTool.Type.TOOL, 30, Boolean.parseBoolean(BundleTools.SYSTEM_PREFERENCES.getProperty("measuretool.maximized")));
    this.eventManager = eventManager;
    this.rootPane = new JScrollPane();
    dockable.setTitleIcon(new ImageIcon(MeasureTool.class.getResource("/icon/16x16/measure.png")));
    dockable.addCDockableLocationListener(
            event -> {
              if (event.isShowingChanged()) {
                  if(event.getNewShowing()) {
                	  BundleTools.SYSTEM_PREFERENCES.setProperty("measuretool.maximized", "true");
                  } else {
                	  if(event.getNewLocation() != null)
                		  BundleTools.SYSTEM_PREFERENCES.setProperty("measuretool.maximized", "false");
                  }
                  BundleTools.saveSystemPreferences();
              }
            });
    setDockableWidth(
        javax.swing.UIManager.getLookAndFeel() != null
            ? javax.swing.UIManager.getLookAndFeel()
                    .getClass()
                    .getName()
                    .startsWith("org.pushingpixels")
                ? 290
                : 205
            : 205);
    jbInit();
  }

  private void jbInit() {
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    add(getIconsPanel());
    add(getPathologyPanel());
    add(getSelectedMeasurePanel());
  }

  public final JPanel getIconsPanel() {
    final JPanel transform = new JPanel();
    transform.setAlignmentX(Component.LEFT_ALIGNMENT);
    transform.setAlignmentY(Component.TOP_ALIGNMENT);
    transform.setLayout(new BoxLayout(transform, BoxLayout.Y_AXIS));

    MeasureTool.buildIconPanel(transform, eventManager, ActionW.MEASURE, ActionW.DRAW_MEASURE, 4);
    MeasureTool.buildIconPanel(transform, eventManager, ActionW.DRAW, ActionW.DRAW_GRAPHICS, 4);
    
//    String prefPath = BundleTools.SYSTEM_PREFERENCES.getProperty("weasis.pref.dir");
//	String profileName = Paths.get(prefPath).getFileName().toString();
	
	// get the study description and filter the pathologies as per the study - knee, shoulder, etc.
	
//	if(profileName.equalsIgnoreCase("v.sarode@primeradiology.com.au") || profileName.equalsIgnoreCase("update201@mail.com")) {
//	    // AI annotation sections
//	    MeasureTool.buildAnnotationIconPanel(transform, eventManager, ActionW.MEASURE, ActionW.DRAW_MEASURE, 4, "Knee Views", 14, 21);
//	    MeasureTool.buildAnnotationIconPanel(transform, eventManager, ActionW.MEASURE, ActionW.DRAW_MEASURE, 4, "Patellofemoral", 22, 31);
//	    MeasureTool.buildAnnotationIconPanel(transform, eventManager, ActionW.MEASURE, ActionW.DRAW_MEASURE, 4, "Medial TibioFemoral", 32, 41);
//	    MeasureTool.buildAnnotationIconPanel(transform, eventManager, ActionW.MEASURE, ActionW.DRAW_MEASURE, 4, "Lateral TibioFemoral", 42, 51);
//	    MeasureTool.buildAnnotationIconPanel(transform, eventManager, ActionW.MEASURE, ActionW.DRAW_MEASURE, 4, "Joint Effusion", 52, 57);
//	    MeasureTool.buildAnnotationIconPanel(transform, eventManager, ActionW.MEASURE, ActionW.DRAW_MEASURE, 4, "Vascular Calcification", 58, 63);
//	    MeasureTool.buildAnnotationIconPanel(transform, eventManager, ActionW.MEASURE, ActionW.DRAW_MEASURE, 4, "Quadriceps Spur", 64, 69);
//	    MeasureTool.buildAnnotationIconPanel(transform, eventManager, ActionW.MEASURE, ActionW.DRAW_MEASURE, 4, "OSD", 70, 75);
//	    
//	    MeasureTool.buildAnnotationIconPanel(transform, eventManager, ActionW.MEASURE, ActionW.DRAW_MEASURE, 4, "Shoudler Views", 76, 83);
//	    MeasureTool.buildAnnotationIconPanel(transform, eventManager, ActionW.MEASURE, ActionW.DRAW_MEASURE, 4, "AP GHJ", 84, 93);
//	    MeasureTool.buildAnnotationIconPanel(transform, eventManager, ActionW.MEASURE, ActionW.DRAW_MEASURE, 4, "AP ACJ", 94, 103);
//	    MeasureTool.buildAnnotationIconPanel(transform, eventManager, ActionW.MEASURE, ActionW.DRAW_MEASURE, 4, "AP SAC", 104, 109);
//	    MeasureTool.buildAnnotationIconPanel(transform, eventManager, ActionW.MEASURE, ActionW.DRAW_MEASURE, 4, "AP-G GHJ", 110, 119);
//	    MeasureTool.buildAnnotationIconPanel(transform, eventManager, ActionW.MEASURE, ActionW.DRAW_MEASURE, 4, "AP-G ACJ", 110, 119);
//	    MeasureTool.buildAnnotationIconPanel(transform, eventManager, ActionW.MEASURE, ActionW.DRAW_MEASURE, 4, "AP-G SAC", 120, 125);
//	    MeasureTool.buildAnnotationIconPanel(transform, eventManager, ActionW.MEASURE, ActionW.DRAW_MEASURE, 4, "AS", 126, 135);
//
//	}

    transform.add(Box.createVerticalStrut(5));

    JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 0));
    transform.add(panel);

    JLabel label = new JLabel(Messages.getString("MeasureToolBar.line") + StringUtil.COLON);
    panel.add(label);

    JButton button = new JButton(Messages.getString("MeasureTool.pick"));
    button.setBackground(viewSetting.getLineColor());
    button.addActionListener(
        e -> {
          JButton btn = (JButton) e.getSource();
          Color newColor =
              JColorChooser.showDialog(
                  SwingUtilities.getWindowAncestor(MeasureTool.this),
                  Messages.getString("MeasureTool.pick_color"),
                  btn.getBackground());
          if (newColor != null) {
            btn.setBackground(newColor);
            viewSetting.setLineColor(newColor);
            updateMeasureProperties(viewSetting);
          }
        });
    panel.add(button);

    JSpinner spinner = new JSpinner();
    JMVUtils.setNumberModel(spinner, viewSetting.getLineWidth(), 1, 8, 1);
    spinner.addChangeListener(
        e -> {
          Object val = ((JSpinner) e.getSource()).getValue();

          if (val instanceof Integer) {
            viewSetting.setLineWidth((Integer) val);
            updateMeasureProperties(viewSetting);
          }
        });
    panel.add(spinner);

    ActionState drawOnceAction = eventManager.getAction(ActionW.DRAW_ONLY_ONCE);
    if (drawOnceAction instanceof ToggleButtonListener) {
      transform.add(Box.createVerticalStrut(5));
      JPanel panel1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
      transform.add(panel1);
      JCheckBox checkDraw =
          ((ToggleButtonListener) drawOnceAction).createCheckBox(ActionW.DRAW_ONLY_ONCE.getTitle());
      checkDraw.setSelected(viewSetting.isDrawOnlyOnce());
      checkDraw.setAlignmentX(Component.LEFT_ALIGNMENT);
      panel1.add(checkDraw);
    }
    JPanel panel1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    transform.add(panel1);
    JCheckBox chckbxBasicImageStatistics =
        new JCheckBox(Messages.getString("MeasureTool.pix_stats"), viewSetting.isBasicStatistics());
    chckbxBasicImageStatistics.setAlignmentX(Component.LEFT_ALIGNMENT);
    panel1.add(chckbxBasicImageStatistics);
    chckbxBasicImageStatistics.addActionListener(
        e -> {
          JCheckBox box = (JCheckBox) e.getSource();
          boolean sel = box.isSelected();
          viewSetting.setBasicStatistics(sel);
          // Force also advanced statistics
          viewSetting.setMoreStatistics(sel);
          for (Measurement m : ImageStatistics.ALL_MEASUREMENTS) {
            m.setComputed(sel);
          }
          synchronized (UIManager.VIEWER_PLUGINS) {
            for (int i = UIManager.VIEWER_PLUGINS.size() - 1; i >= 0; i--) {
              ViewerPlugin<?> p = UIManager.VIEWER_PLUGINS.get(i);
              if (p instanceof ImageViewerPlugin) {
                for (Object v : ((ImageViewerPlugin<?>) p).getImagePanels()) {
                  if (v instanceof ViewCanvas) {
                    ViewCanvas<?> view = (ViewCanvas<?>) v;
                    view.getGraphicManager().updateLabels(true, view);
                  }
                }
              }
            }
          }
        });
    panel1.add(chckbxBasicImageStatistics);

    ActionState spUnitAction = eventManager.getAction(ActionW.SPATIAL_UNIT);
    if (spUnitAction instanceof ComboItemListener) {
      final JPanel panel4 = new JPanel(new FlowLayout(FlowLayout.LEADING, 2, 3));
      final JLabel lutLabel = new JLabel();
      lutLabel.setText(Messages.getString("MeasureTool.unit") + StringUtil.COLON);
      panel4.add(lutLabel);
      final JComboBox unitComboBox = ((ComboItemListener) spUnitAction).createCombo(120);
      unitComboBox.setSelectedItem(Unit.PIXEL);
      panel4.add(unitComboBox);
      transform.add(panel4);
    }

    transform.add(Box.createVerticalStrut(5));
    JPanel panel2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    transform.add(panel2);
    final JButton btnGerenralOptions = new JButton(Messages.getString("MeasureTool.more_options"));
    btnGerenralOptions.setAlignmentX(Component.LEFT_ALIGNMENT);
    panel2.add(btnGerenralOptions);
    btnGerenralOptions.addActionListener(
        e -> {
          ColorLayerUI layer = ColorLayerUI.createTransparentLayerUI(MeasureTool.this);
          PreferenceDialog dialog =
              new PreferenceDialog(SwingUtilities.getWindowAncestor(MeasureTool.this));
          dialog.showPage(LABEL_PREF_NAME);
          ColorLayerUI.showCenterScreen(dialog, layer);
        });
    transform.add(panel2);
    transform.add(Box.createVerticalStrut(5));

    return transform;
  }

  private static void updateMeasureProperties(final ViewSetting setting) {
    if (setting != null) {
      MeasureToolBar.measureGraphicList.forEach(
          g -> MeasureToolBar.applyDefaultSetting(setting, g));
      MeasureToolBar.drawGraphicList.forEach(g -> MeasureToolBar.applyDefaultSetting(setting, g));
    }
  }

  public JPanel getSelectedMeasurePanel() {
    final JPanel transform = new JPanel();
    transform.setAlignmentY(Component.TOP_ALIGNMENT);
    transform.setAlignmentX(Component.LEFT_ALIGNMENT);
    transform.setLayout(new BoxLayout(transform, BoxLayout.Y_AXIS));
    transform.setBorder(
        BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(10, 3, 0, 3),
            new TitledBorder(
                null,
                Messages.getString("MeasureTool.sel"),
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                FontTools.getFont12Bold(),
                Color.GRAY)));

    JPanel panel1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    transform.add(panel1);
    transform.add(Box.createVerticalStrut(5));
    jtable = createMultipleRenderingTable(new SimpleTableModel(new String[] {}, new Object[][] {}));
    jtable.setFont(FontTools.getFont10());

    jtable.getTableHeader().setReorderingAllowed(false);

    tableContainer = new JPanel();
    tableContainer.setBorder(BorderFactory.createEtchedBorder());
    tableContainer.setPreferredSize(new Dimension(50, 80));
    tableContainer.setLayout(new BorderLayout());
    transform.add(tableContainer);

    return transform;
  }

  @Override
  public Component getToolComponent() {
    JViewport viewPort = rootPane.getViewport();
    if (viewPort == null) {
      viewPort = new JViewport();
      rootPane.setViewport(viewPort);
    }
    if (viewPort.getView() != this) {
      viewPort.setView(this);
    }
    return rootPane;
  }

  @Override
  protected void changeToolWindowAnchor(CLocation clocation) {
    // Do nothing
  }

  public static JTable createMultipleRenderingTable(TableModel model) {
    JTable table = new JTable(model);
    table.getColumnModel().setColumnMargin(3);
    return table;
  }

  public static void createTableHeaders(JTable table) {
    table.getColumnModel().getColumn(0).setHeaderRenderer(new TableHeaderRenderer());
    table.getColumnModel().getColumn(1).setHeaderRenderer(new TableHeaderRenderer());
  }

  public void setSelectedGraphic(Graphic graph, MeasurableLayer layer) {
    List<MeasureItem> measList = null;

    if (graph != null && layer != null && graph.getLayerType() == LayerType.MEASURE) {
      Unit unit = null;
      ActionState spUnitAction = eventManager.getAction(ActionW.SPATIAL_UNIT);
      if (spUnitAction instanceof ComboItemListener) {
        unit = (Unit) ((ComboItemListener) spUnitAction).getSelectedItem();
      }
      measList = graph.computeMeasurements(layer, true, unit);
    }
    updateMeasuredItems(measList);
  }

  @Override
  public void updateMeasuredItems(List<MeasureItem> measList) {
    tableContainer.removeAll();

    // just clear tableContainer if measList is null
    if (measList != null) {
      String[] headers = {
        Messages.getString("MeasureTool.param"), Messages.getString("MeasureTool.val")
      };
      jtable.setModel(new SimpleTableModel(headers, getLabels(measList)));
      jtable.getColumnModel().getColumn(1).setCellRenderer(new TableNumberRenderer());
      createTableHeaders(jtable);
      int height =
          (jtable.getRowHeight() + jtable.getRowMargin()) * jtable.getRowCount()
              + jtable.getTableHeader().getHeight()
              + 5;
      tableContainer.setPreferredSize(
          new Dimension(jtable.getColumnModel().getTotalColumnWidth(), height));
      tableContainer.add(jtable.getTableHeader(), BorderLayout.PAGE_START);
      tableContainer.add(jtable, BorderLayout.CENTER);
      TableColumnAdjuster.pack(jtable);
    } else {
      tableContainer.setPreferredSize(new Dimension(50, 50));
    }
    tableContainer.revalidate();
    tableContainer.repaint();
  }

  public static Object[][] getLabels(List<MeasureItem> measList) {
    if (measList != null) {
      Object[][] labels = new Object[measList.size()][];
      for (int i = 0; i < labels.length; i++) {
        MeasureItem m = measList.get(i);
        Object[] row = new Object[2];
        StringBuilder buffer = new StringBuilder(m.getMeasurement().getName());
        if (m.getLabelExtension() != null) {
          buffer.append(m.getLabelExtension());
        }
        if (m.getUnit() != null) {
          buffer.append(" [");
          buffer.append(m.getUnit());
          buffer.append("]");
        }
        row[0] = buffer.toString();
        row[1] = m.getValue();
        labels[i] = row;
      }
      return labels;
    }
    return null;
  }

  public static int getNumberOfMeasures(boolean[] select) {
    int k = 0;
    for (boolean b : select) {
      if (b) {
        k++;
      }
    }
    return k;
  }

  @Override
  public void handle(List<Graphic> selectedGraphicList, MeasurableLayer layer) {
    Graphic g = null;
    List<DragGraphic> list = null;

    if (selectedGraphicList != null) {
      if (selectedGraphicList.size() == 1) {
        g = selectedGraphicList.get(0);
      }

      list = new ArrayList<>();

      for (Graphic graphic : selectedGraphicList) {
        if (graphic instanceof DragGraphic) {
          list.add((DragGraphic) graphic);
        }
      }
    }

    boolean computeAllMeasures = true;
    if (selectedGraphic != null) {
      if (g != null && selectedGraphic.size() == 1) {
        // Warning only comparing if it is the same instance, cannot compare handle points.
        // Update of the list of measures is performed in the drag sequence (move, complete). Here
        // only the
        // change of selection will compute the measurements
        if (g == selectedGraphic.get(0)) {
          computeAllMeasures = false;
        }
      }
      selectedGraphic.clear();
    }

    this.selectedGraphic = list;
    if (computeAllMeasures) {
      // if g equals null means graphic is not single or no graphic is selected
      setSelectedGraphic(g, layer);
    }
  }

  public static void buildIconPanel(
      JPanel rootPanel,
      ImageViewerEventManager<?> eventManager,
      ActionW action,
      ActionW graphicAction,
      int lineLength) {
    Optional<ComboItemListener> actionState =
        eventManager.getAction(graphicAction, ComboItemListener.class);
    if (!actionState.isPresent()) {
      return;
    }

    final JPanel pIcons = new JPanel();
    pIcons.setBorder(
        BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(10, 3, 0, 3),
            new TitledBorder(
                null,
                graphicAction.getTitle(),
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                FontTools.getFont12Bold(),
                Color.GRAY)));

    JToogleButtonGroup measures = actionState.get().createButtonGroup();
    JToggleButton[] items = measures.getJToggleButtonList();

    pIcons.setLayout(new GridBagLayout());
//    for (int i = 0; i < items.length; i++) { // original
    int noOfTools = (items.length > 14) ? 14 : items.length;
    for (int i = 0; i < noOfTools; i++) { // first 14 are the default ones, from 14 onwards they are AI annotation tools
      items[i].addActionListener(
          e -> {
            ImageViewerPlugin<? extends ImageElement> view =
                eventManager.getSelectedView2dContainer();
            if (view != null) {
              final ViewerToolBar toolBar = view.getViewerToolBar();
              if (toolBar != null) {
                String cmd = action.cmd();
                if (!toolBar.isCommandActive(cmd)) {
                  MouseActions mouseActions = eventManager.getMouseActions();
                  mouseActions.setAction(MouseActions.T_LEFT, cmd);
                  view.setMouseActions(mouseActions);
                  toolBar.changeButtonState(MouseActions.T_LEFT, cmd);
                }
              }
            }
          });
      GridBagConstraints constraints = new GridBagConstraints();
      constraints.insets = new Insets(0, 0, 5, 5);
      constraints.gridx = i % lineLength;
      constraints.gridy = i / lineLength;
      Dimension size = items[i].getPreferredSize();
      if (size != null && size.width > size.height) {
        items[i].setPreferredSize(new Dimension(size.height + 2, size.height));
      }
      pIcons.add(items[i], constraints);
    }
    JPanel panelLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    panelLeft.add(pIcons);
    rootPanel.add(panelLeft);
  }
  
  public static void buildAnnotationIconPanel(
	      JPanel rootPanel,
	      ImageViewerEventManager<?> eventManager,
	      ActionW action,
	      ActionW graphicAction,
	      int lineLength, String panelTitle, int startIndex, int endIndex) {
	    Optional<ComboItemListener> actionState =
	        eventManager.getAction(graphicAction, ComboItemListener.class);
	    if (!actionState.isPresent()) {
	      return;
	    }

	    JPanel panelLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
	    panelLeft.setBorder(
	        BorderFactory.createCompoundBorder(
	        	BorderFactory.createEmptyBorder(0, 3, 0, 3),
	            new TitledBorder(
	                null,
	                panelTitle,
	                TitledBorder.DEFAULT_JUSTIFICATION,
	                TitledBorder.DEFAULT_POSITION,
	                FontTools.getFont12Bold(),
	                Color.GRAY)));

//	    for(int k=0; k<2; k++) {
//	    	String subPanelTitle = (k == 0) ? "Right" : "Left";
	    	final JPanel pRightIcons = new JPanel();
	    	pRightIcons.setBorder(
		        BorderFactory.createCompoundBorder(
		            BorderFactory.createEmptyBorder(0, 3, 0, 3),
		            new TitledBorder(
		                null,
		                "Right",
		                TitledBorder.DEFAULT_JUSTIFICATION,
		                TitledBorder.DEFAULT_POSITION,
		                FontTools.getFont10(),
		                Color.GRAY)));
		    pRightIcons.setLayout(new GridBagLayout());

	    	final JPanel pLeftIcons = new JPanel();
	    	pLeftIcons.setBorder(
		        BorderFactory.createCompoundBorder(
		            BorderFactory.createEmptyBorder(0, 3, 0, 3),
		            new TitledBorder(
		                null,
		                "Left",
		                TitledBorder.DEFAULT_JUSTIFICATION,
		                TitledBorder.DEFAULT_POSITION,
		                FontTools.getFont10(),
		                Color.GRAY)));
		    pLeftIcons.setLayout(new GridBagLayout());

		    
		    
		    JToogleButtonGroup measures = actionState.get().createButtonGroup();
		    JToggleButton[] items = measures.getJToggleButtonList();
		    
		    int midIndx = (startIndex + endIndex) / 2;
		    for (int i = startIndex; i <= endIndex; i++) {		      
		      items[i].addActionListener(
		          e -> {
		            ImageViewerPlugin<? extends ImageElement> view =
		                eventManager.getSelectedView2dContainer();
		            if (view != null) {
		              final ViewerToolBar toolBar = view.getViewerToolBar();
		              if (toolBar != null) {
		                String cmd = action.cmd();	                
		                if (!toolBar.isCommandActive(cmd)) {
		                  MouseActions mouseActions = eventManager.getMouseActions();
		                  mouseActions.setAction(MouseActions.T_LEFT, cmd);
		                  view.setMouseActions(mouseActions);
		                  toolBar.changeButtonState(MouseActions.T_LEFT, cmd);
		                }
		              }
		            }
		          });
		      GridBagConstraints constraints = new GridBagConstraints();
		      constraints.insets = new Insets(0, 0, 5, 5);
		      constraints.gridx = i % lineLength;
		      constraints.gridy = i / lineLength;
		      Dimension size = items[i].getPreferredSize();
		      if (size != null && size.width > size.height) {
		        items[i].setPreferredSize(new Dimension(size.height + 2, size.height));
		      }
		      if( i <= midIndx ) {
		    	  pRightIcons.add(items[i]);
		      } else {
		    	  pLeftIcons.add(items[i]);
		      }
		      
		      
		    }
	//	    JPanel panelLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		    panelLeft.add(pRightIcons);
		    panelLeft.add(pLeftIcons);
//	    }
	    rootPanel.add(panelLeft);
	  }
  
  public static void buildAnnotationIconPanelTable(
	      JPanel rootPanel,
	      ImageViewerEventManager<?> eventManager,
	      ActionW action,
	      ActionW graphicAction,
	      int lineLength, String panelTitle, int startIndex, int endIndex) {
	    Optional<ComboItemListener> actionState =
	        eventManager.getAction(graphicAction, ComboItemListener.class);
	    if (!actionState.isPresent()) {
	      return;
	    }
	    int m = 0;
	    int n = 0;
	    JToggleButton[] rightIconsArray = new JToggleButton[5];
	    JToggleButton[] leftIconsArray = new JToggleButton[5];
        Object[] row = new Object[3];
        
        row[0] = panelTitle;
		     
		    JToogleButtonGroup measures = actionState.get().createButtonGroup();
		    JToggleButton[] items = measures.getJToggleButtonList();
		    
		    int midIndx = (startIndex + endIndex) / 2;
		    for (int i = startIndex; i <= endIndex; i++) {		      
		      items[i].addActionListener(
		          e -> {
		            ImageViewerPlugin<? extends ImageElement> view =
		                eventManager.getSelectedView2dContainer();
		            if (view != null) {
		              final ViewerToolBar toolBar = view.getViewerToolBar();
		              if (toolBar != null) {
		                String cmd = action.cmd();	                
		                if (!toolBar.isCommandActive(cmd)) {
		                  MouseActions mouseActions = eventManager.getMouseActions();
		                  mouseActions.setAction(MouseActions.T_LEFT, cmd);
		                  view.setMouseActions(mouseActions);
		                  toolBar.changeButtonState(MouseActions.T_LEFT, cmd);
		                }
		              }
		            }
		          });
		      GridBagConstraints constraints = new GridBagConstraints();
		      constraints.insets = new Insets(0, 0, 5, 5);
		      constraints.gridx = i % lineLength;
		      constraints.gridy = i / lineLength;
		      Dimension size = items[i].getPreferredSize();
		      if (size != null && size.width > size.height) {
		        items[i].setPreferredSize(new Dimension(size.height + 2, size.height));
		      }
		      if( i <= midIndx ) {
		    	  rightIconsArray[m++] = ((JToggleButton) items[i]);
		      } else {
		    	  leftIconsArray[n++] = ((JToggleButton)items[i]);
		      }
		      
		      
		    }
		    row[1] = rightIconsArray;
		    row[2] = leftIconsArray;
		    labelsArray[pathCount++] = row;
	  }
  
  public JPanel getPathologyPanel() {
	    final JPanel transform = new JPanel();
	    transform.setAlignmentY(Component.TOP_ALIGNMENT);
	    transform.setAlignmentX(Component.LEFT_ALIGNMENT);
	    transform.setLayout(new BoxLayout(transform, BoxLayout.Y_AXIS));
	    transform.setBorder(
	        BorderFactory.createCompoundBorder(
	            BorderFactory.createEmptyBorder(10, 3, 0, 3),
	            new TitledBorder(
	                null,
	                "Pathologies",
	                TitledBorder.DEFAULT_JUSTIFICATION,
	                TitledBorder.DEFAULT_POSITION,
	                FontTools.getFont12Bold(),
	                Color.GRAY)));
//	    iconsTable = createMultipleRenderingTable(new SimpleTableModel(new String[] {}, new Object[][] {}));
//	    iconsTable.setFont(FontTools.getFont10());
	    String[] studyType = {"shoulder", "knee"};
	    for(int h=0; h<studyType.length; h++) {
		    String prefPath = BundleTools.SYSTEM_PREFERENCES.getProperty("weasis.pref.dir");
			String profileName = Paths.get(prefPath).getFileName().toString();
		    if(profileName.equalsIgnoreCase("v.sarode@primeradiology.com.au") || profileName.equalsIgnoreCase("update201@mail.com")) {
			    // AI annotation sections
		    	if(studyType[h].equalsIgnoreCase("knee")) {
		    		MeasureTool.buildAnnotationIconPanelTable(transform, eventManager, ActionW.MEASURE, ActionW.DRAW_MEASURE, 4, "Knee Views", 14, 21);
//			    MeasureTool.buildAnnotationIconPanelTable(transform, eventManager, ActionW.MEASURE, ActionW.DRAW_MEASURE, 4, "Patellofemoral", 22, 31);
//			    MeasureTool.buildAnnotationIconPanelTable(transform, eventManager, ActionW.MEASURE, ActionW.DRAW_MEASURE, 4, "Medial TibioFemoral", 32, 41);
//			    MeasureTool.buildAnnotationIconPanelTable(transform, eventManager, ActionW.MEASURE, ActionW.DRAW_MEASURE, 4, "Lateral TibioFemoral", 42, 51);
//			    MeasureTool.buildAnnotationIconPanelTable(transform, eventManager, ActionW.MEASURE, ActionW.DRAW_MEASURE, 4, "Joint Effusion", 52, 57);
//			    MeasureTool.buildAnnotationIconPanelTable(transform, eventManager, ActionW.MEASURE, ActionW.DRAW_MEASURE, 4, "Vascular Calcification", 58, 63);
//			    MeasureTool.buildAnnotationIconPanelTable(transform, eventManager, ActionW.MEASURE, ActionW.DRAW_MEASURE, 4, "Quadriceps Spur", 64, 69);
//			    MeasureTool.buildAnnotationIconPanelTable(transform, eventManager, ActionW.MEASURE, ActionW.DRAW_MEASURE, 4, "OSD", 70, 75);
		    	} else {
		    		MeasureTool.buildAnnotationIconPanelTable(transform, eventManager, ActionW.MEASURE, ActionW.DRAW_MEASURE, 4, "Shoudler Views", 76, 83);
//			    MeasureTool.buildAnnotationIconPanelTable(transform, eventManager, ActionW.MEASURE, ActionW.DRAW_MEASURE, 4, "AP GHJ", 84, 93);
//			    MeasureTool.buildAnnotationIconPanelTable(transform, eventManager, ActionW.MEASURE, ActionW.DRAW_MEASURE, 4, "AP ACJ", 94, 103);
//			    MeasureTool.buildAnnotationIconPanelTable(transform, eventManager, ActionW.MEASURE, ActionW.DRAW_MEASURE, 4, "AP SAC", 104, 109);
//			    MeasureTool.buildAnnotationIconPanelTable(transform, eventManager, ActionW.MEASURE, ActionW.DRAW_MEASURE, 4, "AP-G GHJ", 110, 119);
//			    MeasureTool.buildAnnotationIconPanelTable(transform, eventManager, ActionW.MEASURE, ActionW.DRAW_MEASURE, 4, "AP-G ACJ", 110, 119);
//			    MeasureTool.buildAnnotationIconPanelTable(transform, eventManager, ActionW.MEASURE, ActionW.DRAW_MEASURE, 4, "AP-G SAC", 120, 125);
//			    MeasureTool.buildAnnotationIconPanelTable(transform, eventManager, ActionW.MEASURE, ActionW.DRAW_MEASURE, 4, "AS", 126, 135);
		    	}
			}
	
		    JPanel panel1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		    transform.add(panel1);
		    transform.add(Box.createVerticalStrut(5));
		    
		    String[] columnNames = {
		            "", "Right", "Left"
		          };
	//	    DefaultTableModel model = new DefaultTableModel(labelsArray, columnNames);
	//	    DefaultTableModel model = new DefaultTableModel(labelsArray, columnNames) {
	//          @Override
	//          public Class<?> getColumnClass(int columnIndex) {
	//              if (columnIndex == 0) {
	//                  return String.class; // For the Name column
	//              } else {
	//                  return JToggleButton[].class; // For the Toggle columns
	//              }
	//          }
	//
	//          @Override
	//          public boolean isCellEditable(int row, int column) {
	//              return column != 0; // Allow editing for toggle columns only
	//          }
	//      };
	      
		    iconsTable = new JTable(new DefaultTableModel(labelsArray, columnNames)) {
	          @Override
	          public TableCellRenderer getCellRenderer(int row, int column) {
	              if (column == 1 || column == 2) {
	                  return new ToggleButtonArrayRenderer();
	              }
	              return super.getCellRenderer(row, column);
	          }
	
	          @Override
	          public TableCellEditor getCellEditor(int row, int column) {
	              if (column == 1 || column == 2) {
	                  return new ToggleButtonArrayEditor();
	              }
	              return super.getCellEditor(row, column);
	          }
	      };
	      
	//      iconsTable = new JTable(model);
	
		    iconsTable.getTableHeader().setReorderingAllowed(false);
	
		    iconsTableContainer = new JPanel();
		    iconsTableContainer.setBorder(BorderFactory.createEtchedBorder());
		    iconsTableContainer.setPreferredSize(new Dimension(50, 80));
		    iconsTableContainer.setLayout(new BorderLayout());
		    iconsTableContainer.setName(studyType[h]);
		    transform.add(iconsTableContainer);
		    
		    
	
		    iconsTable.addMouseListener(new MouseAdapter() {
	          @Override
	          public void mouseClicked(MouseEvent e) {
	              // Handle mouse click events
	              int row = iconsTable.rowAtPoint(e.getPoint());
	              int column = iconsTable.columnAtPoint(e.getPoint());
	              if (row >= 0 && column >= 0) {
	                  Object value = iconsTable.getValueAt(row, column);
	                  System.out.println("Clicked: " + value);
	              }
	          }
	      });
	
	//	    iconsTable.getColumnModel().getColumn(1).setCellRenderer(new ToggleButtonArrayRenderer());
	//	    iconsTable.getColumnModel().getColumn(1).setCellEditor(new ToggleButtonArrayEditor());
	//	    iconsTable.getColumnModel().getColumn(2).setCellRenderer(new ToggleButtonArrayRenderer());
	//	    iconsTable.getColumnModel().getColumn(2).setCellEditor(new ToggleButtonArrayEditor());
		    iconsTable.setRowHeight(28);
		          
		    int height =
		              (iconsTable.getRowHeight() + iconsTable.getRowMargin()) * iconsTable.getRowCount()
		                  + iconsTable.getTableHeader().getHeight()
		                  + 50;
		    iconsTableContainer.setPreferredSize(
		              new Dimension(iconsTable.getColumnModel().getTotalColumnWidth(), height));
		    iconsTableContainer.add(iconsTable.getTableHeader(), BorderLayout.PAGE_START);
		    iconsTableContainer.add(iconsTable, BorderLayout.CENTER);
		    iconsTableContainer.setVisible(false);
		    iconsTables.add(iconsTableContainer);
	//	          TableColumnAdjuster.pack(iconsTable);
	//	          iconsTableContainer.revalidate();
	//	          iconsTableContainer.repaint();
		    labelsArray = new Object[1][];
		    pathCount = 0;
	    }
	    return transform;
	  }
  
  	  
  class ToggleButtonArrayRenderer extends JPanel implements TableCellRenderer {
      public ToggleButtonArrayRenderer() {
          setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
          setOpaque(true);
      }

      @Override
      public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
//          removeAll();
          JToggleButton[] buttons = (JToggleButton[]) value;
          for (JToggleButton button : buttons) {
        	  if(button != null) {
        		  add(button);
        	  }
          }
          this.setVisible(true);
          return this;
      }
  }
  
  class ToggleButtonArrayEditor extends AbstractCellEditor implements TableCellEditor {
      private JPanel panel;
      private JToggleButton[] toggleButtons;

      public ToggleButtonArrayEditor() {
    	  setOpaque(true);
          panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
          panel.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "enter");
          panel.getActionMap().put("enter", new AbstractAction() {
              @Override
              public void actionPerformed(ActionEvent e) {
                  fireEditingStopped(); // Stop editing when Enter key is pressed
              }
          });
      }

      @Override
      public java.awt.Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
    	  removeAllButtons();
    	  toggleButtons = (JToggleButton[]) value;
          for (int i = 0; i < toggleButtons.length; i++) {
        	  if(toggleButtons[i] != null) {
        		  toggleButtons[i].setFocusable(true);
        		  panel.add(toggleButtons[i]);
        	  }
          }
          return panel;
      }

      @Override
      public Object getCellEditorValue() {
         return toggleButtons;
      }
      
      @Override
      public boolean isCellEditable(EventObject e){
          return true;
      }
      
      private void removeAllButtons() {
          panel.removeAll();
          panel.revalidate();
          panel.repaint();
      }
  }
}
