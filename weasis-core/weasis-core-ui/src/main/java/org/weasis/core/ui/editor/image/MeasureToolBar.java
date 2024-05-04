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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.event.ListDataEvent;
import org.weasis.core.api.gui.util.ActionW;
import org.weasis.core.api.gui.util.ComboItemListener;
import org.weasis.core.api.gui.util.DropButtonIcon;
import org.weasis.core.api.gui.util.DropDownButton;
import org.weasis.core.api.gui.util.GroupRadioMenu;
import org.weasis.core.api.gui.util.RadioMenuItem;
import org.weasis.core.api.service.BundleTools;
import org.weasis.core.api.service.WProperties;
import org.weasis.core.ui.Messages;
import org.weasis.core.ui.editor.image.dockable.MeasureTool;
import org.weasis.core.ui.model.GraphicModel;
import org.weasis.core.ui.model.graphic.DragGraphic;
import org.weasis.core.ui.model.graphic.Graphic;
import org.weasis.core.ui.model.graphic.imp.AnnotationGraphic;
import org.weasis.core.ui.model.graphic.imp.PixelInfoGraphic;
import org.weasis.core.ui.model.graphic.imp.angle.AngleToolGraphic;
import org.weasis.core.ui.model.graphic.imp.angle.CobbAngleToolGraphic;
import org.weasis.core.ui.model.graphic.imp.angle.FourPointsAngleToolGraphic;
import org.weasis.core.ui.model.graphic.imp.angle.OpenAngleToolGraphic;
import org.weasis.core.ui.model.graphic.imp.area.EllipseGraphic;
import org.weasis.core.ui.model.graphic.imp.area.ObliqueRectangleGraphic;
import org.weasis.core.ui.model.graphic.imp.area.PolygonGraphic;
import org.weasis.core.ui.model.graphic.imp.area.RectangleGraphic;
import org.weasis.core.ui.model.graphic.imp.area.SelectGraphic;
import org.weasis.core.ui.model.graphic.imp.area.ThreePointsCircleGraphic;
import org.weasis.core.ui.model.graphic.imp.line.LineGraphic;
import org.weasis.core.ui.model.graphic.imp.line.ParallelLineGraphic;
import org.weasis.core.ui.model.graphic.imp.line.PerpendicularLineGraphic;
import org.weasis.core.ui.model.graphic.imp.line.PolylineGraphic;
import org.weasis.core.ui.model.layer.LayerType;
import org.weasis.core.ui.pref.ViewSetting;
import org.weasis.core.ui.util.WtoolBar;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JMenu;
import org.weasis.core.api.gui.util.RadioMenuItem;
import org.weasis.core.api.gui.util.GroupRadioMenu;
import java.awt.Insets;

@SuppressWarnings("serial")
public class MeasureToolBar extends WtoolBar {
  private static final long serialVersionUID = -6672565963157176685L;

  public static final SelectGraphic selectionGraphic = new SelectGraphic();

  public static final Icon MeasureIcon =
      new ImageIcon(MouseActions.class.getResource("/icon/16x16/measure.png"));
  public static final Icon drawIcon =
      new ImageIcon(MouseActions.class.getResource("/icon/16x16/draw.png"));
  public static final List<Graphic> measureGraphicList = new ArrayList<>();
  public static final List<Graphic> drawGraphicList = new ArrayList<>();

  static {
    WProperties p = BundleTools.SYSTEM_PREFERENCES;
    if (p.getBooleanProperty("weasis.measure.selection", true)) {
      measureGraphicList.add(selectionGraphic);
    }
    if (p.getBooleanProperty("weasis.measure.line", true)) {
      measureGraphicList.add(new LineGraphic());
    }
    if (p.getBooleanProperty("weasis.measure.polyline", true)) {
      measureGraphicList.add(new PolylineGraphic());
    }
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
      measureGraphicList.add(new ObliqueRectangleGraphic());
    }
    if (p.getBooleanProperty("weasis.measure.ellipse", true)) {
      measureGraphicList.add(new EllipseGraphic());
    }
    if (p.getBooleanProperty("weasis.measure.threeptcircle", true)) {
      measureGraphicList.add(new ThreePointsCircleGraphic());
    }
    if (p.getBooleanProperty("weasis.measure.polygon", true)) {
      measureGraphicList.add(new PolygonGraphic());
    }
    if (p.getBooleanProperty("weasis.measure.perpendicular", true)) {
      measureGraphicList.add(new PerpendicularLineGraphic());
    }
    if (p.getBooleanProperty("weasis.measure.parallele", true)) {
      measureGraphicList.add(new ParallelLineGraphic());
    }
    if (p.getBooleanProperty("weasis.measure.angle", true)) {
      measureGraphicList.add(new AngleToolGraphic());
    }
    if (p.getBooleanProperty("weasis.measure.openangle", true)) {
      measureGraphicList.add(new OpenAngleToolGraphic());
    }
    if (p.getBooleanProperty("weasis.measure.fourptangle", true)) {
      measureGraphicList.add(new FourPointsAngleToolGraphic());
    }
    if (p.getBooleanProperty("weasis.measure.cobbangle", true)) {
      measureGraphicList.add(new CobbAngleToolGraphic());
    }
    if (p.getBooleanProperty("weasis.measure.pixelinfo", true)) {
      measureGraphicList.add(new PixelInfoGraphic());
    }
    
    // AI Annotation Tools
//    if (p.getBooleanProperty("weasis.measure.rectangle1", true)) {
//      measureGraphicList.add(new ObliqueRectangleGraphic(true));
//    }
    
    // index 14 - 17
    // Right
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
	  measureGraphicList.add(new RectangleGraphic(true, "Right", "View", "AP"));
	}
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
	  measureGraphicList.add(new RectangleGraphic(true, "Right", "View", "W_AP"));
	}
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
	  measureGraphicList.add(new RectangleGraphic(true, "Right", "View", "L"));
	}
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
	  measureGraphicList.add(new RectangleGraphic(true, "Right", "View", "S"));
	}
    // index 18 - 21
    // Left
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
	  measureGraphicList.add(new RectangleGraphic(true, "Left", "View", "AP"));
	}
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
	  measureGraphicList.add(new RectangleGraphic(true, "Left", "View", "W_AP"));
	}
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
	  measureGraphicList.add(new RectangleGraphic(true, "Left", "View", "L"));
	}
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
	  measureGraphicList.add(new RectangleGraphic(true, "Left", "View", "S"));
	}
    
    // index 22 - 26
    // Right
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
      measureGraphicList.add(new RectangleGraphic(true, "Right", "Patellofemoral", "Normal"));
    }
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
      measureGraphicList.add(new RectangleGraphic(true, "Right", "Patellofemoral", "Mild"));
    }
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
      measureGraphicList.add(new RectangleGraphic(true, "Right", "Patellofemoral", "Moderate"));
    }
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
      measureGraphicList.add(new RectangleGraphic(true, "Right", "Patellofemoral", "Severe"));
    }
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
      measureGraphicList.add(new RectangleGraphic(true, "Right", "Patellofemoral", "Others"));
    }
    // index 27 - 31
    // Left
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
      measureGraphicList.add(new RectangleGraphic(true, "Left", "Patellofemoral", "Normal"));
    }
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
	  measureGraphicList.add(new RectangleGraphic(true, "Left", "Patellofemoral", "Mild"));
	}
	if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
	  measureGraphicList.add(new RectangleGraphic(true, "Left", "Patellofemoral", "Moderate"));
	}
	if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
	  measureGraphicList.add(new RectangleGraphic(true, "Left", "Patellofemoral", "Severe"));
	}
	if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
	  measureGraphicList.add(new RectangleGraphic(true, "Left", "Patellofemoral", "Others"));
	}
    
    // index 32 - 36
	// Right
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
      measureGraphicList.add(new RectangleGraphic(true, "Right", "Medial TibioFemoral", "Normal"));
    }
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
      measureGraphicList.add(new RectangleGraphic(true, "Right", "Medial TibioFemoral", "Mild"));
    }
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
      measureGraphicList.add(new RectangleGraphic(true, "Right", "Medial TibioFemoral", "Moderate"));
    }
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
      measureGraphicList.add(new RectangleGraphic(true, "Right", "Medial TibioFemoral", "Severe"));
    }
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
      measureGraphicList.add(new RectangleGraphic(true, "Right", "Medial TibioFemoral", "Others"));
    }
    // index 37 - 41
	// Left
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
      measureGraphicList.add(new RectangleGraphic(true, "Left", "Medial TibioFemoral", "Normal"));
    }
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
      measureGraphicList.add(new RectangleGraphic(true, "Left", "Medial TibioFemoral", "Mild"));
    }
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
      measureGraphicList.add(new RectangleGraphic(true, "Left", "Medial TibioFemoral", "Moderate"));
    }
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
      measureGraphicList.add(new RectangleGraphic(true, "Left", "Medial TibioFemoral", "Severe"));
    }
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
      measureGraphicList.add(new RectangleGraphic(true, "Left", "Medial TibioFemoral", "Others"));
    }

    // index 42 - 46
	// Right
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
      measureGraphicList.add(new RectangleGraphic(true, "Right", "Lateral TibioFemoral", "Normal"));
    }
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
      measureGraphicList.add(new RectangleGraphic(true, "Right", "Lateral TibioFemoral", "Mild"));
    }
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
      measureGraphicList.add(new RectangleGraphic(true, "Right", "Lateral TibioFemoral", "Moderate"));
    }
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
      measureGraphicList.add(new RectangleGraphic(true, "Right", "Lateral TibioFemoral", "Severe"));
    }
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
      measureGraphicList.add(new RectangleGraphic(true, "Right", "Lateral TibioFemoral", "Others"));
    }
    // index 47 - 51
    // Left
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
      measureGraphicList.add(new RectangleGraphic(true, "Left", "Lateral TibioFemoral", "Normal"));
    }
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
      measureGraphicList.add(new RectangleGraphic(true, "Left", "Lateral TibioFemoral", "Mild"));
    }
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
      measureGraphicList.add(new RectangleGraphic(true, "Left", "Lateral TibioFemoral", "Moderate"));
    }
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
      measureGraphicList.add(new RectangleGraphic(true, "Left", "Lateral TibioFemoral", "Severe"));
    }
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
      measureGraphicList.add(new RectangleGraphic(true, "Left", "Lateral TibioFemoral", "Others"));
    }

    // index 52 - 54
    // Right
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
      measureGraphicList.add(new RectangleGraphic(true, "Right", "Joint Effusion", "Normal"));
    }
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
      measureGraphicList.add(new RectangleGraphic(true, "Right", "Joint Effusion", "Mild"));
    }
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
      measureGraphicList.add(new RectangleGraphic(true, "Right", "Joint Effusion", "Others"));
    }
    // index 55 - 57
    // Left
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
      measureGraphicList.add(new RectangleGraphic(true, "Left", "Joint Effusion", "Normal"));
    }
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
      measureGraphicList.add(new RectangleGraphic(true, "Left", "Joint Effusion", "Mild"));
    }
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
      measureGraphicList.add(new RectangleGraphic(true, "Left", "Joint Effusion", "Others"));
    }
    
    // index 58 - 60
    // Right
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
      measureGraphicList.add(new RectangleGraphic(true, "Right", "Vascular Calcification", "Normal"));
    }
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
      measureGraphicList.add(new RectangleGraphic(true, "Right", "Vascular Calcification", "Mild"));
    }
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
      measureGraphicList.add(new RectangleGraphic(true, "Right", "Vascular Calcification", "Others"));
    }
    // index 61 - 63
    // Left
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
      measureGraphicList.add(new RectangleGraphic(true, "Left", "Vascular Calcification", "Normal"));
    }
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
      measureGraphicList.add(new RectangleGraphic(true, "Left", "Vascular Calcification", "Mild"));
    }
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
      measureGraphicList.add(new RectangleGraphic(true, "Left", "Vascular Calcification", "Others"));
    }
    

    // index 64 - 66
    // Right
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
      measureGraphicList.add(new RectangleGraphic(true, "Right", "Quadriceps Spur", "Normal"));
    }
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
      measureGraphicList.add(new RectangleGraphic(true, "Right", "Quadriceps Spur", "Mild"));
    }
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
      measureGraphicList.add(new RectangleGraphic(true, "Right", "Quadriceps Spur", "Others"));
    }
    // index 67 - 69
    // Left
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
      measureGraphicList.add(new RectangleGraphic(true, "Left", "Quadriceps Spur", "Normal"));
    }
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
      measureGraphicList.add(new RectangleGraphic(true, "Left", "Quadriceps Spur", "Mild"));
    }
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
      measureGraphicList.add(new RectangleGraphic(true, "Left", "Quadriceps Spur", "Others"));
    }

    // index 70 - 72
    // Right    
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
      measureGraphicList.add(new RectangleGraphic(true, "Right", "OSD", "Normal"));
    }
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
      measureGraphicList.add(new RectangleGraphic(true, "Right", "OSD", "Mild"));
    }
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
      measureGraphicList.add(new RectangleGraphic(true, "Right", "OSD", "Others"));
    }
    // index 73 - 75
    // Left    
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
      measureGraphicList.add(new RectangleGraphic(true, "Left", "OSD", "Normal"));
    }
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
      measureGraphicList.add(new RectangleGraphic(true, "Left", "OSD", "Mild"));
    }
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
      measureGraphicList.add(new RectangleGraphic(true, "Left", "OSD", "Others"));
    }

    
    
    
    
    
    
    
    
    
    
    
    
    
    // index 76 - 79
    // Right
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
	  measureGraphicList.add(new RectangleGraphic(true, "Right", "View", "AP"));
	}
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
	  measureGraphicList.add(new RectangleGraphic(true, "Right", "View", "AP_G"));
	}
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
	  measureGraphicList.add(new RectangleGraphic(true, "Right", "View", "L"));
	}
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
	  measureGraphicList.add(new RectangleGraphic(true, "Right", "View", "A"));
	}
    // index 80 - 83
    // Left
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
	  measureGraphicList.add(new RectangleGraphic(true, "Left", "View", "AP"));
	}
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
	  measureGraphicList.add(new RectangleGraphic(true, "Left", "View", "AP_G"));
	}
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
	  measureGraphicList.add(new RectangleGraphic(true, "Left", "View", "L"));
	}
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
	  measureGraphicList.add(new RectangleGraphic(true, "Left", "View", "A"));
	}

    
    // index 84 - 88
	// Right
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
      measureGraphicList.add(new RectangleGraphic(true, "Right", "AP GHJ", "Normal"));
    }
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
      measureGraphicList.add(new RectangleGraphic(true, "Right", "AP GHJ", "Mild"));
    }
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
      measureGraphicList.add(new RectangleGraphic(true, "Right", "AP GHJ", "Moderate"));
    }
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
      measureGraphicList.add(new RectangleGraphic(true, "Right", "AP GHJ", "Severe"));
    }
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
      measureGraphicList.add(new RectangleGraphic(true, "Right", "AP GHJ", "Others"));
    }
    // index 89 - 93
    // Left
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
      measureGraphicList.add(new RectangleGraphic(true, "Left", "AP GHJ", "Normal"));
    }
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
      measureGraphicList.add(new RectangleGraphic(true, "Left", "AP GHJ", "Mild"));
    }
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
      measureGraphicList.add(new RectangleGraphic(true, "Left", "AP GHJ", "Moderate"));
    }
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
      measureGraphicList.add(new RectangleGraphic(true, "Left", "AP GHJ", "Severe"));
    }
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
      measureGraphicList.add(new RectangleGraphic(true, "Left", "AP GHJ", "Others"));
    }

    
    // index 94 - 98
	// Right
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
      measureGraphicList.add(new RectangleGraphic(true, "Right", "AP ACJ", "Normal"));
    }
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
      measureGraphicList.add(new RectangleGraphic(true, "Right", "AP ACJ", "Mild"));
    }
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
      measureGraphicList.add(new RectangleGraphic(true, "Right", "AP ACJ", "Moderate"));
    }
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
      measureGraphicList.add(new RectangleGraphic(true, "Right", "AP ACJ", "Severe"));
    }
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
      measureGraphicList.add(new RectangleGraphic(true, "Right", "AP ACJ", "Others"));
    }
    // index 99 - 103
    // Left
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
      measureGraphicList.add(new RectangleGraphic(true, "Left", "AP ACJ", "Normal"));
    }
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
      measureGraphicList.add(new RectangleGraphic(true, "Left", "AP ACJ", "Mild"));
    }
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
      measureGraphicList.add(new RectangleGraphic(true, "Left", "AP ACJ", "Moderate"));
    }
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
      measureGraphicList.add(new RectangleGraphic(true, "Left", "AP ACJ", "Severe"));
    }
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
      measureGraphicList.add(new RectangleGraphic(true, "Left", "AP ACJ", "Others"));
    }

    
    // index 104 - 106
	// Right
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
      measureGraphicList.add(new RectangleGraphic(true, "Right", "AP SAC", "Normal"));
    }
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
      measureGraphicList.add(new RectangleGraphic(true, "Right", "AP SAC", "Mild"));
    }
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
      measureGraphicList.add(new RectangleGraphic(true, "Right", "AP SAC", "Others"));
    }
    // index 107 - 109
    // Left
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
      measureGraphicList.add(new RectangleGraphic(true, "Left", "AP SAC", "Normal"));
    }
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
      measureGraphicList.add(new RectangleGraphic(true, "Left", "AP SAC", "Mild"));
    }
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
      measureGraphicList.add(new RectangleGraphic(true, "Left", "AP SAC", "Others"));
    }


    // index 110 - 114
	// Right
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
      measureGraphicList.add(new RectangleGraphic(true, "Right", "APG GHJ", "Normal"));
    }
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
      measureGraphicList.add(new RectangleGraphic(true, "Right", "APG GHJ", "Mild"));
    }
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
      measureGraphicList.add(new RectangleGraphic(true, "Right", "APG GHJ", "Moderate"));
    }
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
      measureGraphicList.add(new RectangleGraphic(true, "Right", "APG GHJ", "Severe"));
    }
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
      measureGraphicList.add(new RectangleGraphic(true, "Right", "APG GHJ", "Others"));
    }
    // index 115 - 119
    // Left
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
      measureGraphicList.add(new RectangleGraphic(true, "Left", "APG GHJ", "Normal"));
    }
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
      measureGraphicList.add(new RectangleGraphic(true, "Left", "APG GHJ", "Mild"));
    }
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
      measureGraphicList.add(new RectangleGraphic(true, "Left", "APG GHJ", "Moderate"));
    }
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
      measureGraphicList.add(new RectangleGraphic(true, "Left", "APG GHJ", "Severe"));
    }
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
      measureGraphicList.add(new RectangleGraphic(true, "Left", "APG GHJ", "Others"));
    }

    
    // index 120 - 122
	// Right
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
      measureGraphicList.add(new RectangleGraphic(true, "Right", "APG SAC", "Normal"));
    }
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
      measureGraphicList.add(new RectangleGraphic(true, "Right", "APG SAC", "Mild"));
    }
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
      measureGraphicList.add(new RectangleGraphic(true, "Right", "APG SAC", "Others"));
    }
    // index 123 - 125
    // Left
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
      measureGraphicList.add(new RectangleGraphic(true, "Left", "APG SAC", "Normal"));
    }
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
      measureGraphicList.add(new RectangleGraphic(true, "Left", "APG SAC", "Mild"));
    }
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
      measureGraphicList.add(new RectangleGraphic(true, "Left", "APG SAC", "Others"));
    }

    
    // index 126 - 130
	// Right
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
      measureGraphicList.add(new RectangleGraphic(true, "Right", "AS", "T1"));
    }
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
      measureGraphicList.add(new RectangleGraphic(true, "Right", "AS", "T2"));
    }
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
      measureGraphicList.add(new RectangleGraphic(true, "Right", "AS", "T3"));
    }
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
      measureGraphicList.add(new RectangleGraphic(true, "Right", "AS", "T4"));
    }
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
      measureGraphicList.add(new RectangleGraphic(true, "Right", "AS", "Others"));
    }
    // index 131 - 135
    // Left
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
      measureGraphicList.add(new RectangleGraphic(true, "Left", "AS", "T1"));
    }
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
      measureGraphicList.add(new RectangleGraphic(true, "Left", "AS", "T2"));
    }
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
      measureGraphicList.add(new RectangleGraphic(true, "Left", "AS", "T3"));
    }
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
      measureGraphicList.add(new RectangleGraphic(true, "Left", "AS", "T4"));
    }
    if (p.getBooleanProperty("weasis.measure.rectangle", true)) {
      measureGraphicList.add(new RectangleGraphic(true, "Left", "AS", "Others"));
    }

    
    measureGraphicList.forEach(g -> g.setLayerType(LayerType.MEASURE));

    if (p.getBooleanProperty("weasis.draw.selection", true)) {
      drawGraphicList.add(selectionGraphic);
    }
    if (p.getBooleanProperty("weasis.draw.line", true)) {
      drawGraphicList.add(
          new LineGraphic() {
            @Override
            public int getKeyCode() {
              return 0;
            }
          });
    }
    if (p.getBooleanProperty("weasis.draw.polyline", true)) {
      drawGraphicList.add(new PolylineGraphic());
    }
    if (p.getBooleanProperty("weasis.draw.rectangle", true)) {
      drawGraphicList.add(new ObliqueRectangleGraphic());
    }
    if (p.getBooleanProperty("weasis.draw.ellipse", true)) {
      drawGraphicList.add(new EllipseGraphic() {
            @Override
            public int getKeyCode() {
              return 0;
            }
          });
    }
    if (p.getBooleanProperty("weasis.draw.threeptcircle", true)) {
      drawGraphicList.add(new ThreePointsCircleGraphic() {
          @Override
          public int getKeyCode() {
            return 0;
          }
        });
    }
    if (p.getBooleanProperty("weasis.draw.polygon", true)) {
      drawGraphicList.add(
          new PolygonGraphic() {
            @Override
            public int getKeyCode() {
              return 0;
            }
          });
    }
    drawGraphicList.forEach(
        g -> {
          g.setLayerType(LayerType.DRAW);
          g.setLabelVisible(false);
        });

    if (p.getBooleanProperty("weasis.draw.textGrahic", true)) {
      Graphic graphic = new AnnotationGraphic();
      graphic.setLayerType(LayerType.ANNOTATION);
      drawGraphicList.add(graphic);
    }
    selectionGraphic.setLayerType(LayerType.TEMP_DRAW);
  }

  protected final JButton jButtondelete = new JButton();
  protected final JButton jButtonText = new JButton();
  protected final Component measureButtonGap = Box.createRigidArea(new Dimension(10, 0));
  protected final ImageViewerEventManager<?> eventManager;

  @SuppressWarnings("rawtypes")
  public MeasureToolBar(final ImageViewerEventManager<?> eventManager, int index) {
    super(Messages.getString("MeasureToolBar.title"), index);
    if (eventManager == null) {
      throw new IllegalArgumentException("EventManager cannot be null");
    }
    this.eventManager = eventManager;

    MeasureToolBar.measureGraphicList.forEach(
        g -> MeasureToolBar.applyDefaultSetting(MeasureTool.viewSetting, g));
    MeasureToolBar.drawGraphicList.forEach(
        g -> MeasureToolBar.applyDefaultSetting(MeasureTool.viewSetting, g));

    Optional<ComboItemListener> measure =
        eventManager.getAction(ActionW.DRAW_MEASURE, ComboItemListener.class);
    Optional<ComboItemListener> draw =
        eventManager.getAction(ActionW.DRAW_GRAPHICS, ComboItemListener.class);
    if (measure.isPresent()) {

      DropDownButton measureDropDownBtn = buildButton(measure.get());
      add(measureDropDownBtn);

      final JButton measureLineBtn = new JButton();
      measureLineBtn.setToolTipText("Measure (M)");
      measureLineBtn.setText("M");
      measureLineBtn.setIcon(((RadioMenuItem)measureDropDownBtn.getMenuModel().createJPopupMenu().getComponent(1)).getIcon());
      measureLineBtn.addActionListener(new ActionListener () {
        public void actionPerformed(ActionEvent e) {
          ImageViewerPlugin<?> view = eventManager.getSelectedView2dContainer();
          if (view != null) {
            @SuppressWarnings("rawtypes")
            final ViewerToolBar toolBar = view.getViewerToolBar();
            if (toolBar != null) {
              String cmd = ActionW.MEASURE.cmd();
              if (!toolBar.isCommandActive(cmd)) {
                MouseActions mouseActions = eventManager.getMouseActions();
                mouseActions.setAction(MouseActions.T_LEFT, cmd);
                view.setMouseActions(mouseActions);
                toolBar.changeButtonState(MouseActions.T_LEFT, cmd);
              }
            }
          }
          ((RadioMenuItem)measureDropDownBtn.getMenuModel().createJPopupMenu().getComponent(1)).doClick();
        }
      });
      add(measureLineBtn);

      final JButton measureEllipseBtn = new JButton();
      measureEllipseBtn.setToolTipText("Density (D)");
      measureEllipseBtn.setText("D");
      measureEllipseBtn.setIcon(((RadioMenuItem)measureDropDownBtn.getMenuModel().createJPopupMenu().getComponent(5)).getIcon());
      measureEllipseBtn.addActionListener(new ActionListener () {
        public void actionPerformed(ActionEvent e) {
          ImageViewerPlugin<?> view = eventManager.getSelectedView2dContainer();
          if (view != null) {
            @SuppressWarnings("rawtypes")
            final ViewerToolBar toolBar = view.getViewerToolBar();
            if (toolBar != null) {
              String cmd = ActionW.MEASURE.cmd();
              if (!toolBar.isCommandActive(cmd)) {
                MouseActions mouseActions = eventManager.getMouseActions();
                mouseActions.setAction(MouseActions.T_LEFT, cmd);
                view.setMouseActions(mouseActions);
                toolBar.changeButtonState(MouseActions.T_LEFT, cmd);
              }
            }
          }
          ((RadioMenuItem)measureDropDownBtn.getMenuModel().createJPopupMenu().getComponent(5)).doClick();
        }
      });
      add(measureEllipseBtn);

      final JButton measurePerpendicularBtn = new JButton();
      measurePerpendicularBtn.setToolTipText("Perpendicular (T)");
      measurePerpendicularBtn.setText("T");
      measurePerpendicularBtn.setIcon(((RadioMenuItem)measureDropDownBtn.getMenuModel().createJPopupMenu().getComponent(7)).getIcon());
      measurePerpendicularBtn.addActionListener(new ActionListener () {
        public void actionPerformed(ActionEvent e) {
          ImageViewerPlugin<?> view = eventManager.getSelectedView2dContainer();
          if (view != null) {
            @SuppressWarnings("rawtypes")
            final ViewerToolBar toolBar = view.getViewerToolBar();
            if (toolBar != null) {
              String cmd = ActionW.MEASURE.cmd();
              if (!toolBar.isCommandActive(cmd)) {
                MouseActions mouseActions = eventManager.getMouseActions();
                mouseActions.setAction(MouseActions.T_LEFT, cmd);
                view.setMouseActions(mouseActions);
                toolBar.changeButtonState(MouseActions.T_LEFT, cmd);
              }
            }
          }
          ((RadioMenuItem)measureDropDownBtn.getMenuModel().createJPopupMenu().getComponent(7)).doClick();
        }
      });
      add(measurePerpendicularBtn);

      final JButton measureAngleBtn = new JButton();
      measureAngleBtn.setToolTipText("Angle (A)");
      measureAngleBtn.setText("A");
      measureAngleBtn.setIcon(((RadioMenuItem)measureDropDownBtn.getMenuModel().createJPopupMenu().getComponent(9)).getIcon());
      measureAngleBtn.addActionListener(new ActionListener () {
        public void actionPerformed(ActionEvent e) {
          ImageViewerPlugin<?> view = eventManager.getSelectedView2dContainer();
          if (view != null) {
            @SuppressWarnings("rawtypes")
            final ViewerToolBar toolBar = view.getViewerToolBar();
            if (toolBar != null) {
              String cmd = ActionW.MEASURE.cmd();
              if (!toolBar.isCommandActive(cmd)) {
                MouseActions mouseActions = eventManager.getMouseActions();
                mouseActions.setAction(MouseActions.T_LEFT, cmd);
                view.setMouseActions(mouseActions);
                toolBar.changeButtonState(MouseActions.T_LEFT, cmd);
              }
            }
          }
          ((RadioMenuItem)measureDropDownBtn.getMenuModel().createJPopupMenu().getComponent(9)).doClick();
        }
      });
      add(measureAngleBtn);

      final JButton measureCobbsAngleBtn = new JButton();
      measureCobbsAngleBtn.setToolTipText("Cobbs (C)");
      measureCobbsAngleBtn.setText("C");
      measureCobbsAngleBtn.setIcon(((RadioMenuItem)measureDropDownBtn.getMenuModel().createJPopupMenu().getComponent(12)).getIcon());
      measureCobbsAngleBtn.addActionListener(new ActionListener () {
        public void actionPerformed(ActionEvent e) {
          ImageViewerPlugin<?> view = eventManager.getSelectedView2dContainer();
          if (view != null) {
            @SuppressWarnings("rawtypes")
            final ViewerToolBar toolBar = view.getViewerToolBar();
            if (toolBar != null) {
              String cmd = ActionW.MEASURE.cmd();
              if (!toolBar.isCommandActive(cmd)) {
                MouseActions mouseActions = eventManager.getMouseActions();
                mouseActions.setAction(MouseActions.T_LEFT, cmd);
                view.setMouseActions(mouseActions);
                toolBar.changeButtonState(MouseActions.T_LEFT, cmd);
              }
            }
          }
          ((RadioMenuItem)measureDropDownBtn.getMenuModel().createJPopupMenu().getComponent(12)).doClick();
        }
      });
      add(measureCobbsAngleBtn);

    }
    if (draw.isPresent()) {
      add(buildButton(draw.get()));
    }

    if (measure.isPresent() || draw.isPresent()) {
      jButtondelete.setToolTipText(Messages.getString("MeasureToolBar.del"));
      jButtondelete.setIcon(
          new ImageIcon(MouseActions.class.getResource("/icon/16x16/draw-delete.png")));
      jButtondelete.addActionListener(
          e -> {
            GraphicModel gm = eventManager.getSelectedViewPane().getGraphicManager();
            if (gm.getSelectedGraphics().isEmpty()) {
              gm.setSelectedAllGraphics();
            }
            gm.deleteSelectedGraphics(eventManager.getSelectedViewPane(), Boolean.TRUE);
          });
      if (measure.isPresent()) {
        measure.get().registerActionState(jButtondelete);
      } else if (draw.isPresent()) {
        draw.get().registerActionState(jButtondelete);
      }
      add(jButtondelete);
    }
  }

  public static void applyDefaultSetting(ViewSetting setting, Graphic graphic) {
    if (graphic instanceof DragGraphic) {
      DragGraphic g = (DragGraphic) graphic;
      g.setLineThickness((float) setting.getLineWidth());
      g.setPaint(setting.getLineColor());
    }
  }

  private DropDownButton buildButton(ComboItemListener<?> action) {
    boolean draw = action.getActionW() == ActionW.DRAW_GRAPHICS;

    MeasureGroupMenu menu = new MeasureGroupMenu(action.getActionW());
    action.registerActionState(menu);

    for (Component mitem : menu.getRadioMenuItemListCopy()) {
      RadioMenuItem ritem = (RadioMenuItem) mitem;
      if (ritem.getUserObject() instanceof Graphic) {
        Graphic g = (Graphic) ritem.getUserObject();
        if (g.getKeyCode() != 0) {
          ritem.setAccelerator(KeyStroke.getKeyStroke(g.getKeyCode(), g.getModifier()));
        }
      }
    }

    DropDownButton dropDownButton =
        new DropDownButton(
            action.getActionW().cmd(),
            buildIcon(selectionGraphic, draw ? drawIcon : MeasureIcon),
            menu) {
          @Override
          protected JPopupMenu getPopupMenu() {
            JPopupMenu m =
                (getMenuModel() == null) ? new JPopupMenu() : getMenuModel().createJPopupMenu();
            m.setInvoker(this);
            return m;
          }
        };
    menu.setButton(dropDownButton);
    action.registerActionState(dropDownButton);

    dropDownButton.setToolTipText(
        draw
            ? Messages.getString("MeasureToolBar.drawing_tools")
            : Messages.getString("MeasureToolBar.tools"));

    // when user press the measure icon, set the action to measure
    dropDownButton.addActionListener(
        e -> {
          ImageViewerPlugin<?> view = eventManager.getSelectedView2dContainer();
          if (view != null) {
            @SuppressWarnings("rawtypes")
            final ViewerToolBar toolBar = view.getViewerToolBar();
            if (toolBar != null) {
              String cmd = draw ? ActionW.DRAW.cmd() : ActionW.MEASURE.cmd();
              if (!toolBar.isCommandActive(cmd)) {
                MouseActions mouseActions = eventManager.getMouseActions();
                mouseActions.setAction(MouseActions.T_LEFT, cmd);
                view.setMouseActions(mouseActions);
                toolBar.changeButtonState(MouseActions.T_LEFT, cmd);
              }
            }
          }
        });
    return dropDownButton;
  }

  public static Icon buildIcon(final Graphic graphic, Icon bckIcon) {

    return new DropButtonIcon(
        new Icon() {

          @Override
          public void paintIcon(Component c, Graphics g, int x, int y) {
            if (c instanceof AbstractButton) {
              AbstractButton model = (AbstractButton) c;
              Icon icon = null;

              if (!model.isEnabled()) {
                icon = UIManager.getLookAndFeel().getDisabledIcon(model, bckIcon);
              }
              if (icon == null) {
                icon = bckIcon;
              }
              icon.paintIcon(c, g, x, y);
              if (graphic != null) {
                Icon smallIcon = null;
                if (!model.isEnabled()) {
                  smallIcon = UIManager.getLookAndFeel().getDisabledIcon(model, graphic.getIcon());
                }
                if (smallIcon == null) {
                  smallIcon = graphic.getIcon();
                }
                if (smallIcon != null) {
                  int offsetx = bckIcon.getIconWidth() - smallIcon.getIconWidth() - 1;
                  int offsety = bckIcon.getIconHeight() - smallIcon.getIconHeight() - 1;
                  smallIcon.paintIcon(c, g, x + offsetx, y + offsety);
                }
              }
            }
          }

          @Override
          public int getIconWidth() {
            return bckIcon.getIconWidth();
          }

          @Override
          public int getIconHeight() {
            return bckIcon.getIconHeight();
          }
        });
  }

  static class MeasureGroupMenu extends GroupRadioMenu<Graphic> {
    private final ActionW action;
    private JButton button;

    public MeasureGroupMenu(ActionW action) {
      super();
      this.action = action;
    }

    @Override
    public void contentsChanged(ListDataEvent e) {
      super.contentsChanged(e);
      changeButtonState();
    }

    public void changeButtonState() {
      Object sel = dataModel.getSelectedItem();
      if (sel instanceof Graphic && button != null) {
        Icon icon =
            buildIcon((Graphic) sel, action == ActionW.DRAW_GRAPHICS ? drawIcon : MeasureIcon);
        button.setIcon(icon);
        button.setActionCommand(sel.toString());
      }
    }

    public JButton getButton() {
      return button;
    }

    public void setButton(JButton button) {
      this.button = button;
    }
  }
}
