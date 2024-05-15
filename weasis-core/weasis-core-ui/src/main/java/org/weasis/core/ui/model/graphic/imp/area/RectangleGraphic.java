/*
 * Copyright (c) 2009-2020 Weasis Team and other contributors.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at https://www.eclipse.org/legal/epl-2.0, or the Apache
 * License, Version 2.0 which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */
package org.weasis.core.ui.model.graphic.imp.area;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.weasis.core.api.image.measure.MeasurementsAdapter;
import org.weasis.core.api.image.util.MeasurableLayer;
import org.weasis.core.api.image.util.Unit;
import org.weasis.core.ui.Messages;
import org.weasis.core.ui.model.graphic.AbstractDragGraphicArea;
import org.weasis.core.ui.model.utils.bean.MeasureItem;
import org.weasis.core.ui.model.utils.bean.Measurement;
import org.weasis.core.ui.model.utils.exceptions.InvalidShapeException;
import org.weasis.core.ui.util.MouseEventDouble;

import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlType(name = "irectangle")
@XmlRootElement(name = "irectangle")
public class RectangleGraphic extends AbstractDragGraphicArea {
  private static final long serialVersionUID = -2862114022989550927L;

  public static final Integer POINTS_NUMBER = 8;

  public static final Icon ICON =
	      new ImageIcon(RectangleGraphic.class.getResource("/icon/10x10/draw-rectangle.png"));

  public static final Measurement AREA =
      new Measurement(Messages.getString("measure.area"), 1, true, true, true);
  public static final Measurement PERIMETER =
      new Measurement(Messages.getString("measure.perimeter"), 2, true, true, false);
  public static final Measurement TOP_LEFT_POINT_X =
      new Measurement(Messages.getString("measure.topx"), 3, true, true, false);
  public static final Measurement TOP_LEFT_POINT_Y =
      new Measurement(Messages.getString("measure.topy"), 4, true, true, false);
  public static final Measurement CENTER_X =
      new Measurement(Messages.getString("measure.centerx"), 5, true, true, false);
  public static final Measurement CENTER_Y =
      new Measurement(Messages.getString("measure.centery"), 6, true, true, false);
  public static final Measurement WIDTH =
      new Measurement(Messages.getString("measure.width"), 7, true, true, false);
  public static final Measurement HEIGHT =
      new Measurement(Messages.getString("measure.height"), 8, true, true, false);

  protected static final List<Measurement> MEASUREMENT_LIST = new ArrayList<>();

  public boolean asAnnotation = false;
  public String annotationSide;
  public String annotationKey;
  public String annotationVal;

  public static final Icon NAD_ICON =
	      new ImageIcon(RectangleGraphic.class.getResource("/icon/10x10/nad.png"));
  public static final Icon MILD_ICON =
	      new ImageIcon(RectangleGraphic.class.getResource("/icon/10x10/mild.png"));
  public static final Icon MODERATE_ICON =
	      new ImageIcon(RectangleGraphic.class.getResource("/icon/10x10/moderate.png"));
  public static final Icon SEVERE_ICON =
	      new ImageIcon(RectangleGraphic.class.getResource("/icon/10x10/severe.png"));
  public static final Icon OTHERS_ICON =
	      new ImageIcon(RectangleGraphic.class.getResource("/icon/10x10/others.png"));

  public static final Icon AP_ICON =
	      new ImageIcon(RectangleGraphic.class.getResource("/icon/10x10/ap.png"));
  public static final Icon W_AP_ICON =
	      new ImageIcon(RectangleGraphic.class.getResource("/icon/10x10/w_ap.png"));
  public static final Icon L_ICON =
	      new ImageIcon(RectangleGraphic.class.getResource("/icon/10x10/l.png"));
  public static final Icon S_ICON =
	      new ImageIcon(RectangleGraphic.class.getResource("/icon/10x10/s.png"));
  public static final Icon A_ICON =
	      new ImageIcon(RectangleGraphic.class.getResource("/icon/10x10/a.png"));
  public static final Icon AP_G_ICON =
	      new ImageIcon(RectangleGraphic.class.getResource("/icon/10x10/ap_g.png"));
  public static final Icon T1_ICON =
	      new ImageIcon(RectangleGraphic.class.getResource("/icon/10x10/t1.png"));
  public static final Icon T2_ICON =
	      new ImageIcon(RectangleGraphic.class.getResource("/icon/10x10/t2.png"));
  public static final Icon T3_ICON =
	      new ImageIcon(RectangleGraphic.class.getResource("/icon/10x10/t3.png"));
  public static final Icon T4_ICON =
	      new ImageIcon(RectangleGraphic.class.getResource("/icon/10x10/t4.png"));

  static {
    MEASUREMENT_LIST.add(TOP_LEFT_POINT_X);
    MEASUREMENT_LIST.add(TOP_LEFT_POINT_Y);
    MEASUREMENT_LIST.add(CENTER_X);
    MEASUREMENT_LIST.add(CENTER_Y);
    MEASUREMENT_LIST.add(WIDTH);
    MEASUREMENT_LIST.add(HEIGHT);
    MEASUREMENT_LIST.add(AREA);
    MEASUREMENT_LIST.add(PERIMETER);
  }

  public RectangleGraphic() {
    super(POINTS_NUMBER);
  }
  public RectangleGraphic(boolean isAnnotation, String side, String key, String val) {
    this();
	this.asAnnotation = isAnnotation;
    this.annotationSide = side;
    this.annotationKey = key;
    this.annotationVal = val;
  }

  public RectangleGraphic(RectangleGraphic graphic) {
    super(graphic);
    
    // assign any properties that are declared in this class separately
    this.asAnnotation = graphic.asAnnotation;
    this.annotationSide = graphic.annotationSide;
    this.annotationKey = graphic.annotationKey;
    this.annotationVal = graphic.annotationVal;
    
    // place a predefined box with fixed points
    if(this.asAnnotation) {
    	double x = 0, y = 0, w = 0, h = 0;
    	
    	// how to find - x, y, w, h from set of 8 points
    	// x = x0
    	// y = y0
    	// w = x2 - x0
    	// h = y3 - y0
    	
    	switch(this.annotationKey.toLowerCase()) {
	    	case "view" :
	    		x = 10;
	    		y = 10;
	    		w = 2;
	    		h = 2;
	    		break;    	
	    	case "patellofemoral" :
	    		x = 482.191887675507;
	    		y = 358.74570982839316;
	    		w = 396.37597;
	    		h = 129.48517;
	    		break;
	    	case "lateral tibiofemoral" :
	    		x = 578.6546850994076;
	    		y = 319.295070017849;
	    		w = 295.4027989;
	    		h = 200;
	    		break;
	    	case "medial tibiofemoral" :
	    		x = 280.4896260081277;
	    		y = 318.1292461953591;
	    		w = 295.4027989;
	    		h = 200;
	    		break;
	    	case "joint effusion" :
	    		x = 1086.776212832551;
	    		y = 551.1095461658841;
	    		w = 259.87636;
	    		h = 567.49765;
	    		break;
	    	case "vascular calcification" :
	    		x = 381.6418547895059;
	    		y = 461.6589383770592;
	    		w = 441.35997;
	    		h = 956.1507;
	    		break;
	    	case "quadriceps spur" :
	    		x = 1112.2025625381325;
	    		y = 1057.1439902379498;
	    		w = 211.47712;
	    		h = 382.63026;
	    		break;
	    	case "osd" :
	    		x = 542.1984732824429;
	    		y = 1717.2519083969466;
	    		w = 223.83206;
	    		h = 340.70992;
	    		break;

	    	case "ap ghj" :
	    		x = 1456.7183811129848;
	    		y = 670.643338954469;
	    		w = 252.95109;
	    		h = 376.05396;
	    		break;
	    	case "ap acj" :
	    		x = 1591.6256323777404;
	    		y = 412.633220910624;
	    		w = 286.94222;
	    		h = 210.79258;
	    		break;
	    	case "ap sac" :
	    		x = 1891.9726633922016;
	    		y = 653.8553219700459;
	    		w = 161.7103;
	    		h = 234.32595;
	    		break;

	    	
	    	case "apg ghj" :
	    		x = 520.7323892519969;
	    		y = 511.25115645148674;
	    		w = 207.69789;
	    		h = 380.42858;
	    		break;
	    	case "apg sac" :
	    		x = 634.6973342986008;
	    		y = 396.50896327792105;
	    		w = 404.55357;
	    		h = 290.37774;
	    		break;
	    	case "as" :
	    		x = 1194.0094412345948;
	    		y = 740.2246317357879;
	    		w = 396.37596;
	    		h = 341.90529;
	    		break;

	    	
	    	default : break;
    	}
    	
    	
    	
    	
    	Point2D NW = new Point2D.Double(x, y);
    	Point2D N = new Point2D.Double(x + w / 2, y);
    	Point2D NE = new Point2D.Double(x + w, y);
    	Point2D E = new Point2D.Double(x + w, y + h / 2);
    	Point2D SE = new Point2D.Double(x + w, y + h);
    	Point2D S = new Point2D.Double(x + w / 2, y + h);
    	Point2D SW = new Point2D.Double(x, y + h);
    	Point2D W = new Point2D.Double(x, y + h / 2);

    	    	
	    pts.add(NW);
	    pts.add(SE);
	    pts.add(NE);
	    pts.add(SW);
	    pts.add(N);
	    pts.add(S);
	    pts.add(E);
	    pts.add(W);
	    	    
	    buildShape(null);
    }

  }

  @Override
  public RectangleGraphic copy() {
    return new RectangleGraphic(this);
  }

  @Override
  public Icon getIcon() {
	Icon btnIcon = ICON;
	
	if(this.asAnnotation) {
		switch(this.annotationVal.toLowerCase()) {
			case "normal" :
				btnIcon = NAD_ICON;
				break;
			case "mild" :
				btnIcon = MILD_ICON;
				break;
			case "moderate" :
				btnIcon = MODERATE_ICON;
				break;
			case "severe" :
				btnIcon = SEVERE_ICON;
				break;
			case "others" :
				btnIcon = OTHERS_ICON;
				break;
			case "ap" :
				btnIcon = AP_ICON;
				break;
			case "w_ap" :
				btnIcon = W_AP_ICON;
				break;
			case "l" :
				btnIcon = L_ICON;
				break;
			case "s" :
				btnIcon = S_ICON;
				break;
			case "a" :
				btnIcon = A_ICON;
				break;
			case "ap_g" :
				btnIcon = AP_G_ICON;
				break;
			case "t1" :
				btnIcon = T1_ICON;
				break;
			case "t2" :
				btnIcon = T2_ICON;
				break;
			case "t3" :
				btnIcon = T3_ICON;
				break;
			case "t4" :
				btnIcon = T4_ICON;
				break;
		}
	}
	  
    return btnIcon;
  }

  @Override
  public String getUIName() {
    return Messages.getString("MeasureToolBar.rect");
  }

  public RectangleGraphic buildGraphic(Rectangle2D rectangle) throws InvalidShapeException {
    Rectangle2D r =
        Optional.ofNullable(rectangle)
            .orElseThrow(() -> new InvalidShapeException("Rectangle2D is null!"));
    setHandlePointList(r);
    prepareShape();
    return this;
  }

  @Override
  protected void prepareShape() throws InvalidShapeException {
    if (!isShapeValid()) {
      throw new InvalidShapeException("This shape cannot be drawn");
    }
    buildShape(null);
  }

  @Override
  public Integer moveAndResizeOnDrawing(
      Integer handlePointIndex, Double deltaX, Double deltaY, MouseEventDouble mouseEvent) {
    if (Objects.equals(handlePointIndex, UNDEFINED)) { // move shape
      for (Point2D point : pts) {
        if (point != null) {
          point.setLocation(point.getX() + deltaX, point.getY() + deltaY);
        }
      }
    } else {
      Rectangle2D rectangle = new Rectangle2D.Double();

      rectangle.setFrameFromDiagonal(
          getHandlePoint(eHandlePoint.NW.index), getHandlePoint(eHandlePoint.SE.index));

      double x = rectangle.getX();
      double y = rectangle.getY();
      double w = rectangle.getWidth();
      double h = rectangle.getHeight();

      eHandlePoint pt = eHandlePoint.valueFromIndex(handlePointIndex);

      if (pt.equals(eHandlePoint.W) || pt.equals(eHandlePoint.NW) || pt.equals(eHandlePoint.SW)) {
        x += deltaX;
        w -= deltaX;
      }

      if (pt.equals(eHandlePoint.N) || pt.equals(eHandlePoint.NW) || pt.equals(eHandlePoint.NE)) {
        y += deltaY;
        h -= deltaY;
      }

      if (pt.equals(eHandlePoint.E) || pt.equals(eHandlePoint.NE) || pt.equals(eHandlePoint.SE)) {
        w += deltaX;
      }

      if (pt.equals(eHandlePoint.S) || pt.equals(eHandlePoint.SW) || pt.equals(eHandlePoint.SE)) {
        h += deltaY;
      }

      if (w < 0) {
        w = -w;
        x -= w;
        pt = pt.getVerticalMirror();
      }

      if (h < 0) {
        h = -h;
        y -= h;
        pt = pt.getHorizontalMirror();
      }

      handlePointIndex = pt.index;
      rectangle.setFrame(x, y, w, h);

      setHandlePointList(rectangle);
    }

    return handlePointIndex;
  }

  @Override
  public void buildShape(MouseEventDouble mouseevent) {
    Rectangle2D rectangle = null;

    if (pts.size() > 1) {
      if (!getHandlePoint(eHandlePoint.NW.index).equals(getHandlePoint(eHandlePoint.SE.index))) {
        rectangle = new Rectangle2D.Double();
        rectangle.setFrameFromDiagonal(
            getHandlePoint(eHandlePoint.NW.index), getHandlePoint(eHandlePoint.SE.index));
      }
    }

    setShape(rectangle, mouseevent);
    updateLabel(mouseevent, getDefaultView2d(mouseevent));
  }

  @Override
  public List<Measurement> getMeasurementList() {
    return MEASUREMENT_LIST;
  }

  @Override
  public List<MeasureItem> computeMeasurements(
      MeasurableLayer layer, boolean releaseEvent, Unit displayUnit) {

    if (layer != null && layer.hasContent() && isShapeValid()) {
      MeasurementsAdapter adapter = layer.getMeasurementAdapter(displayUnit);

      if (adapter != null) {
        ArrayList<MeasureItem> measVal = new ArrayList<>();
        Rectangle2D rect = new Rectangle2D.Double();

        rect.setFrameFromDiagonal(
            getHandlePoint(eHandlePoint.NW.index), getHandlePoint(eHandlePoint.SE.index));

        double ratio = adapter.getCalibRatio();

        if (TOP_LEFT_POINT_X.getComputed()) {
          measVal.add(
              new MeasureItem(
                  TOP_LEFT_POINT_X, adapter.getXCalibratedValue(rect.getX()), adapter.getUnit()));
        }
        if (TOP_LEFT_POINT_Y.getComputed()) {
          measVal.add(
              new MeasureItem(
                  TOP_LEFT_POINT_Y, adapter.getYCalibratedValue(rect.getY()), adapter.getUnit()));
        }
        if (CENTER_X.getComputed()) {
          measVal.add(
              new MeasureItem(
                  CENTER_X, adapter.getXCalibratedValue(rect.getCenterX()), adapter.getUnit()));
        }
        if (CENTER_Y.getComputed()) {
          measVal.add(
              new MeasureItem(
                  CENTER_Y, adapter.getYCalibratedValue(rect.getCenterY()), adapter.getUnit()));
        }
        if (WIDTH.getComputed()) {
          measVal.add(new MeasureItem(WIDTH, ratio * rect.getWidth(), adapter.getUnit()));
        }
        if (HEIGHT.getComputed()) {
          measVal.add(new MeasureItem(HEIGHT, ratio * rect.getHeight(), adapter.getUnit()));
        }
        if (AREA.getComputed()) {
          Double val = rect.getWidth() * rect.getHeight() * ratio * ratio;
          String unit =
              "pix".equals(adapter.getUnit()) // NON-NLS
                  ? adapter.getUnit()
                  : adapter.getUnit() + "2";
          measVal.add(new MeasureItem(AREA, val, unit));
        }
        if (PERIMETER.getComputed()) {
          Double val = (rect.getWidth() + rect.getHeight()) * 2 * ratio;
          measVal.add(new MeasureItem(PERIMETER, val, adapter.getUnit()));
        }

        List<MeasureItem> stats = getImageStatistics(layer, releaseEvent);
        if (stats != null) {
          measVal.addAll(stats);
        }
        
        if(this.asAnnotation) {
        	measVal.add(new MeasureItem(new Measurement("Side", 99, false, false, true), this.annotationSide, ""));
        	measVal.add(new MeasureItem(new Measurement("Key", 99, false, false, true), this.annotationKey, ""));
        	measVal.add(new MeasureItem(new Measurement("Value", 99, false, false, true), this.annotationVal, ""));
        }

        return measVal;
      }
    }
    return Collections.emptyList();
  }

  protected void setHandlePointList(Rectangle2D rectangle) {

    double x = rectangle.getX();
    double y = rectangle.getY();
    double w = rectangle.getWidth();
    double h = rectangle.getHeight();

    while (pts.size() < pointNumber) {
      pts.add(new Point2D.Double());
    }

    setHandlePoint(eHandlePoint.NW.index, new Point2D.Double(x, y));
    setHandlePoint(eHandlePoint.N.index, new Point2D.Double(x + w / 2, y));
    setHandlePoint(eHandlePoint.NE.index, new Point2D.Double(x + w, y));
    setHandlePoint(eHandlePoint.E.index, new Point2D.Double(x + w, y + h / 2));
    setHandlePoint(eHandlePoint.SE.index, new Point2D.Double(x + w, y + h));
    setHandlePoint(eHandlePoint.S.index, new Point2D.Double(x + w / 2, y + h));
    setHandlePoint(eHandlePoint.SW.index, new Point2D.Double(x, y + h));
    setHandlePoint(eHandlePoint.W.index, new Point2D.Double(x, y + h / 2));
  }

  public enum eHandlePoint {
    NONE(-1),
    NW(0),
    SE(1),
    NE(2),
    SW(3),
    N(4),
    S(5),
    E(6),
    W(7);
    // 0 and 1 must be diagonal point of rectangle

    static final Map<Integer, eHandlePoint> map = new HashMap<>(eHandlePoint.values().length);

    static {
      for (eHandlePoint corner : eHandlePoint.values()) {
        map.put(corner.index, corner);
      }
    }

    public final int index;

    eHandlePoint(int index) {
      this.index = index;
    }

    public int getIndex() {
      return index;
    }

    static eHandlePoint valueFromIndex(int index) {
      return Optional.ofNullable(map.get(index))
          .orElseThrow(
              () ->
                  new RuntimeException(
                      "Not a valid index for a rectangular DragGraphic : " + index));
    }

    eHandlePoint getVerticalMirror() {
      switch (this) {
        case NW:
          return eHandlePoint.NE;
        case NE:
          return eHandlePoint.NW;
        case W:
          return eHandlePoint.E;
        case E:
          return eHandlePoint.W;
        case SW:
          return eHandlePoint.SE;
        case SE:
          return eHandlePoint.SW;
        default:
          return this;
      }
    }

    eHandlePoint getHorizontalMirror() {
      switch (this) {
        case NW:
          return eHandlePoint.SW;
        case SW:
          return eHandlePoint.NW;
        case N:
          return eHandlePoint.S;
        case S:
          return eHandlePoint.N;
        case NE:
          return eHandlePoint.SE;
        case SE:
          return eHandlePoint.NE;
        default:
          return this;
      }
    }
  }
}
