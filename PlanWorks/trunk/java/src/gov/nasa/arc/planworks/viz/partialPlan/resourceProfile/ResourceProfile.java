// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: ResourceProfile.java,v 1.10 2004-03-09 01:48:28 taylor Exp $
//
// PlanWorks
//
// Will Taylor -- started 26Jan04
//

package gov.nasa.arc.planworks.viz.partialPlan.resourceProfile;

import java.awt.Color;
import java.awt.Container;
import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.List;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoDocument;
import com.nwoods.jgo.JGoObject;
import com.nwoods.jgo.JGoPen;
import com.nwoods.jgo.JGoStroke;
import com.nwoods.jgo.JGoText;
import com.nwoods.jgo.JGoView;

// PlanWorks/java/lib/JGo/Classier.jar
import com.nwoods.jgo.examples.BasicNode;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.PwResource;
import gov.nasa.arc.planworks.db.PwResourceInstant;
import gov.nasa.arc.planworks.mdi.MDIInternalFrame;
import gov.nasa.arc.planworks.util.Algorithms;
import gov.nasa.arc.planworks.util.ColorMap;
import gov.nasa.arc.planworks.util.MouseEventOSX;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.nodes.NodeGenerics;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanViewSet;
import gov.nasa.arc.planworks.viz.partialPlan.ResourceView;
import gov.nasa.arc.planworks.viz.partialPlan.navigator.NavigatorView;


/**
 * <code>ResourceProfile</code> - JGo widget to render a resource's extents

 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *       NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class ResourceProfile extends BasicNode {

  private static final int NUM_LEVEL_SCALE_TICKS = 6;
  private static final int LEVEL_SCALE_TICK_WIDTH = 10;

  private PwResource resource;
  private int earliestStartTime;
  private int latestStartTime;
  private int earliestEndTime;
  private int latestEndTime;
  private int earliestDurationTime;
  private int latestDurationTime;
  private Color backgroundColor;
  private FontMetrics levelScaleFontMetrics;
  private ResourceProfileView resourceProfileView;

  private String nodeLabel;
  private int nodeLabelWidth;
  private String resourceId;
  private double levelScaleScaling;
  private int levelScaleWidth;
  private int profileYOrigin;
  private int extentYBottom;
  private double levelMax; // actual levels
  private double levelMin;
  private double levelLimitMin; // level limits - can be exceeded
  private double levelLimitMax;

  /**
   * <code>ResourceProfile</code> - constructor 
   *
   * @param resource - <code>PwResource</code> - 
   * @param backgroundColor - <code>Color</code> - 
   * @param levelScaleFontMetrics - <code>FontMetrics</code> - 
   * @param resourceProfileView - <code>ResourceProfileView</code> - 
   */
  public ResourceProfile( final PwResource resource, final Color backgroundColor,
                          final FontMetrics levelScaleFontMetrics,
                          final ResourceProfileView resourceProfileView) {
    super();
    this.resource = resource;
    earliestStartTime = resource.getHorizonStart();
    latestEndTime = resource.getHorizonEnd();
    resourceId = resource.getId().toString();
//     System.err.println( "Resource Node: " + resourceId + " eS " +
//                         earliestStartTime + " lE " + latestEndTime);

    this.backgroundColor = backgroundColor;
    this.levelScaleFontMetrics = levelScaleFontMetrics;
    this.resourceProfileView = resourceProfileView;

    nodeLabel = resource.getName();
    nodeLabelWidth = ResourceProfile.getNodeLabelWidth( nodeLabel, resourceProfileView);

    configure();
  } // end constructor


  /**
   * <code>getNodeLabelWidth</code>
   *
   * @param label - <code>String</code> - 
   * @param view - <code>ResourceProfileView</code> - 
   * @return - <code>int</code> - 
   */
  public static int getNodeLabelWidth( final String label, final ResourceProfileView view) {
    while (view.getFontMetrics() == null) {
      Thread.yield();
    }
    return SwingUtilities.computeStringWidth( view.getFontMetrics(), label) +
      ViewConstants.TIMELINE_VIEW_INSET_SIZE;
  } // end getNodeLabelWidth

  /**
   * <code>getTickLabelMaxWidth</code>
   *
   * @param resource - <code>PwResource</code> - 
   * @param levelScaleFontMetrics - <code>FontMetrics</code> - 
   * @return - <code>int</code> - 
   */
  public static int getTickLabelMaxWidth( final PwResource resource,
                                          final FontMetrics levelScaleFontMetrics) {
    int maxLabelWidth = 0;
    double minMax[] = ResourceProfile.getResourceMinMax( resource);
    int tickDelta = ((int) minMax[1] - (int) minMax[0]) / NUM_LEVEL_SCALE_TICKS;
    int level = (int) minMax[0];
    while (level < (int) minMax[1]) {
      String tickLabel = new Double( level).toString();
      int labelWidth = SwingUtilities.computeStringWidth( levelScaleFontMetrics, tickLabel);
      if (labelWidth > maxLabelWidth) {
        maxLabelWidth = labelWidth;
      }
      level += tickDelta;
    }
    return maxLabelWidth + LEVEL_SCALE_TICK_WIDTH + ViewConstants.TIMELINE_VIEW_INSET_SIZE;
  } // end getTickLabelMaxWidth

  private static double[] getResourceMinMax( PwResource resource) {
    double minMax[] = new double[] { resource.getLevelLimitMin(), resource.getLevelLimitMax() };
    List instantList = resource.getInstantList();
    Iterator instantItr = instantList.iterator();
    while (instantItr.hasNext()) {
      PwResourceInstant instant = (PwResourceInstant) instantItr.next();
      if (instant.getLevelMax() > minMax[1]) {
        minMax[1] = instant.getLevelMax();
      }
      if (instant.getLevelMin() < minMax[0]) {
        minMax[0] = instant.getLevelMin();
      }
    }
    return minMax;
  } // end getResourceMinMax

  /**
   * <code>configure</code> - called by ResourceProfileView.layoutResourceProfiles
   *
   */
  public final void configure() {
    // put the label in the LevelScaleView, rather than the ExtentView
    int currentYLoc = resourceProfileView.getCurrentYLoc();
    profileYOrigin = currentYLoc;
    // System.err.println( "profileYOrigin " + profileYOrigin);
    levelScaleWidth = resourceProfileView.getLevelScaleViewWidth() -
      ViewConstants.RESOURCE_LEVEL_SCALE_WIDTH_OFFSET;
    ResourceView.renderBordersUpper
      ( resourceProfileView.getJGoRulerView().scaleTime( earliestStartTime),
        resourceProfileView.getJGoRulerView().scaleTime( latestEndTime), currentYLoc,
        resourceProfileView.getJGoExtentDocument());
    ResourceView.renderBordersUpper
      ( 0, levelScaleWidth, currentYLoc, resourceProfileView.getJGoLevelScaleDocument());
    ResourceView.renderResourceName( resource, resourceProfileView.getLevelScaleViewWidth() -
                                     nodeLabelWidth, currentYLoc,
                                     resourceProfileView.getJGoLevelScaleDocument(),
                                     resourceProfileView);

    currentYLoc = currentYLoc + ViewConstants.RESOURCE_PROFILE_MAX_Y_OFFSET +
      (int) (2 * ResourceView.Y_MARGIN);
    int extentYTop = currentYLoc;
    currentYLoc = currentYLoc + ViewConstants.RESOURCE_PROFILE_CELL_HEIGHT;
    extentYBottom = currentYLoc;

    levelLimitMin = resource.getLevelLimitMin();
    levelLimitMax = resource.getLevelLimitMax();
    double minMax[] = ResourceProfile.getResourceMinMax( resource);
    levelMin = (int) minMax[0];
    levelMax = (int) minMax[1];
    levelScaleScaling = (extentYBottom - extentYTop) / (levelMax - levelMin);
//     System.err.println( "extentYTop " + extentYTop + " extentYBottom " + extentYBottom);
//     System.err.println( " levelMin " + levelMin + " levelMax " +
//                         levelMax + " levelScaleScaling " + levelScaleScaling);

    renderLevelScaleLinesAndTicks();

    renderLimits();

    renderLevels();

    currentYLoc += (int) (2 * ResourceView.Y_MARGIN);
    ResourceView.renderBordersLower
      ( resourceProfileView.getJGoRulerView().scaleTime( earliestStartTime),
        resourceProfileView.getJGoRulerView().scaleTime( latestEndTime), currentYLoc,
        resourceProfileView.getJGoExtentDocument());
    ResourceView.renderBordersLower
      ( 0, levelScaleWidth, currentYLoc, resourceProfileView.getJGoLevelScaleDocument());

    currentYLoc += ViewConstants.RESOURCE_PROFILE_MIN_Y_OFFSET;
    resourceProfileView.setCurrentYLoc( currentYLoc);
  } // end configure

  /**
   * <code>getResource</code>
   *
   * @return - <code>PwResource</code> - 
   */
  public final PwResource getResource() {
    return resource;
  }

  /**
   * <code>getPredicateName</code>
   *
   * @return - <code>String</code> - 
   */
  public final String getName() {
    return resource.getName();
  }

  /**
   * <code>getProfileYOrigin</code>
   *
   * @return - <code>int</code> - 
   */
  public final int getProfileYOrigin() {
    return profileYOrigin;
  }

  private void renderLevelScaleLinesAndTicks() {
    int tickDelta = ((int) levelMax - (int) levelMin) / NUM_LEVEL_SCALE_TICKS;
//      System.err.println( "renderLevelScale: max " + levelMax + " min " + levelMin +
//                          " tickDelta " + tickDelta);
    int level = (int) levelMin;
    while (level <= levelMax) {
//       System.err.println( "  level " + level);
      JGoStroke tickLine = new JGoStroke();
      tickLine.addPoint( levelScaleWidth - LEVEL_SCALE_TICK_WIDTH,
                         scaleResourceLevel( (double) level));
      tickLine.addPoint( levelScaleWidth, scaleResourceLevel( (double) level));
      tickLine.setDraggable( false); tickLine.setResizable( false);
      tickLine.setSelectable( false);
      tickLine.setPen( new JGoPen( JGoPen.SOLID, 1, ColorMap.getColor( "white")));
      resourceProfileView.getJGoLevelScaleDocument().addObjectAtTail( tickLine);

      JGoStroke tickLevelLine = new JGoStroke();
      tickLevelLine.addPoint( resourceProfileView.getJGoRulerView().
                              scaleTime( earliestStartTime),
                              scaleResourceLevel( (double) level));
      tickLevelLine.addPoint( resourceProfileView.getJGoRulerView().
                              scaleTime( latestEndTime),
                              scaleResourceLevel( (double) level));
      tickLevelLine.setDraggable( false); tickLevelLine.setResizable( false);
      tickLevelLine.setSelectable( false);
      tickLevelLine.setPen( new JGoPen( JGoPen.SOLID, 1, ColorMap.getColor( "white")));
      resourceProfileView.getJGoExtentDocument().addObjectAtTail( tickLevelLine);

      level += tickDelta;
    }
    // labels
    level = (int) levelMin;
    while (level < levelMax) {
      String label = new Double( level).toString();
      int xTop = levelScaleWidth / 2;
      Point labelLoc =
        new Point( levelScaleWidth - LEVEL_SCALE_TICK_WIDTH - 2 -
                   SwingUtilities.computeStringWidth( levelScaleFontMetrics, label),
                   scaleResourceLevel( (double) level +
                                       tickDelta * ResourceView.ONE_HALF_MULTIPLIER));
      JGoText labelObject = new JGoText( labelLoc, label);
      labelObject.setResizable( false); labelObject.setEditable( false);
      labelObject.setDraggable( false); labelObject.setSelectable( false);
      labelObject.setBkColor( ViewConstants.VIEW_BACKGROUND_COLOR);
      labelObject.setFontSize( ResourceView.LEVEL_SCALE_FONT_SIZE);
      resourceProfileView.getJGoLevelScaleDocument().addObjectAtTail( labelObject);
      level += tickDelta;
    }
  } // end renderLevelScaleLinesAndTicks

  private void renderLimits() {
    JGoStroke levelLimitMaxLine = new ProfileLine( levelLimitMax);
    levelLimitMaxLine.addPoint( resourceProfileView.getJGoRulerView().
                                scaleTime( earliestStartTime),
                                scaleResourceLevel( levelLimitMax));
    levelLimitMaxLine.addPoint( resourceProfileView.getJGoRulerView().
                                scaleTime( latestEndTime),
                                scaleResourceLevel( levelLimitMax));
    // System.err.println( " pointY " + scaleResourceLevel( initialCapacity));
    levelLimitMaxLine.setDraggable( false); levelLimitMaxLine.setResizable( false);
    levelLimitMaxLine.setSelectable( false);
    levelLimitMaxLine.setPen( new JGoPen( JGoPen.SOLID, 2, ColorMap.getColor( "red")));
    resourceProfileView.getJGoExtentDocument().addObjectAtTail( levelLimitMaxLine);

    JGoStroke levelLimitMinLine = new ProfileLine( levelLimitMin);
    levelLimitMinLine.addPoint( resourceProfileView.getJGoRulerView().
                                scaleTime( earliestStartTime),
                                scaleResourceLevel( levelLimitMin));
    levelLimitMinLine.addPoint( resourceProfileView.getJGoRulerView().
                                scaleTime( latestEndTime),
                                scaleResourceLevel( levelLimitMin));
    // System.err.println( " pointY " + scaleResourceLevel( initialCapacity));
    levelLimitMinLine.setDraggable( false); levelLimitMinLine.setResizable( false);
    levelLimitMinLine.setSelectable( false);
    levelLimitMinLine.setPen( new JGoPen( JGoPen.SOLID, 2, ColorMap.getColor( "red")));
    resourceProfileView.getJGoExtentDocument().addObjectAtTail( levelLimitMinLine);
  } // end renderLimits

  private void renderLevels() {
    double initialCapacity = resource.getInitialCapacity();
    List instantList = resource.getInstantList();
    Iterator instantItr = instantList.iterator();
    double lastLevelMax = initialCapacity;
    double lastLevelMin = initialCapacity;
    int xLeft = resourceProfileView.getJGoRulerView().scaleTime( earliestStartTime);
    int yLeftMaxLine = scaleResourceLevel( lastLevelMax);
    int yLeftMinLine = scaleResourceLevel( lastLevelMin);
    int xRight, yRightMaxLine, yRightMinLine;
    while (instantItr.hasNext()) {
      PwResourceInstant instant = (PwResourceInstant) instantItr.next();
      int time = instant.getTime();
      xRight = resourceProfileView.getJGoRulerView().scaleTime( time);
      yRightMaxLine =  scaleResourceLevel( lastLevelMax);
      yRightMinLine =  scaleResourceLevel( lastLevelMin);
      addLineSegment( xLeft, yLeftMaxLine, xRight, yRightMaxLine, lastLevelMax, "max");
      addLineSegment( xLeft, yLeftMinLine, xRight, yRightMinLine, lastLevelMin, "min");

      yLeftMaxLine = yRightMaxLine; yLeftMinLine = yRightMinLine; xLeft = xRight;
      double currentLevelMax = instant.getLevelMax();
      double currentLevelMin = instant.getLevelMin();
      System.err.println( "renderLevels time " + time + " currentLevelMax " +
                          currentLevelMax + " currentLevelMin " + currentLevelMin);
      yRightMaxLine =  scaleResourceLevel( currentLevelMax);
      yRightMinLine =  scaleResourceLevel( currentLevelMin);
      addLineSegment( xLeft, yLeftMaxLine, xRight, yRightMaxLine, currentLevelMax, "max");
      addLineSegment( xLeft, yLeftMinLine, xRight, yRightMinLine, currentLevelMin, "min");
 
      yLeftMaxLine = yRightMaxLine; yLeftMinLine = yRightMinLine; xLeft = xRight;
      lastLevelMax = currentLevelMax; lastLevelMin = currentLevelMin; 
    }
    
    xRight = resourceProfileView.getJGoRulerView().scaleTime( latestEndTime);
    yRightMaxLine =  scaleResourceLevel( lastLevelMax);
    yRightMinLine =  scaleResourceLevel( lastLevelMin);
    addLineSegment( xLeft, yLeftMaxLine, xRight, yRightMaxLine, lastLevelMax, "max");
    addLineSegment( xLeft, yLeftMinLine, xRight, yRightMinLine, lastLevelMin, "min");
  } // end renderLevels

  private void addLineSegment( int xLeft, int yLeft, int xRight, int yRight,
                               double level, String type) {
    JGoStroke lineSegment = null;
    if (yLeft == yRight) { // horizontal segment
      lineSegment = new ProfileLine( level);
      lineSegment.addPoint( xLeft, yLeft);
      lineSegment.addPoint( xRight, yRight);
    } else { // vertical segment
      lineSegment = new JGoStroke();
      lineSegment.addPoint( xLeft, yLeft);
      lineSegment.addPoint( xRight, yRight);
    }
    if (type.equals( "max")) {
      addMaxLineSegment( lineSegment);
    } else if (type.equals( "min")) {
      addMinLineSegment( lineSegment);
    }
  } // end addLineSegment

  private void addMaxLineSegment( JGoStroke maxLineSegment) {
    maxLineSegment.setDraggable( false); maxLineSegment.setResizable( false);
    maxLineSegment.setSelectable( false);
    maxLineSegment.setPen( new JGoPen( JGoPen.SOLID, 2, ColorMap.getColor( "magenta")));
    resourceProfileView.getJGoExtentDocument().addObjectAtTail( maxLineSegment);
  } // end  addMaxLineSegment 

  private void addMinLineSegment( JGoStroke minLineSegment) {
    minLineSegment.setDraggable( false); minLineSegment.setResizable( false);
    minLineSegment.setSelectable( false);
    minLineSegment.setPen( new JGoPen( JGoPen.SOLID, 2, ColorMap.getColor( "blue")));
    resourceProfileView.getJGoExtentDocument().addObjectAtTail( minLineSegment);
  } // end  addMinLineSegment 

  private int scaleResourceLevel( final double level) {
    return (extentYBottom - (int) ((level - levelMin) * levelScaleScaling));
  }



  /**
   * <code>ProfileLine</code> - render profile segment as a line
   *
   */
  public class ProfileLine extends JGoStroke {

    private double quantity;

    /**
     * <code>ProfileLine</code> - constructor 
     *
     */
    public ProfileLine( double quantity) {
      super();
      this.quantity = quantity;
    }

    /**
     * <code>getToolTipText</code>
     *
     * @return - <code>String</code> - 
     */
    public final String getToolTipText() {
      StringBuffer toolTip = new StringBuffer( "quantitiy = ");
      toolTip.append( new Double( quantity).toString());
      return toolTip.toString();
    } // end getToolTipText

    /**
     * <code>getToolTipText</code>
     *
     * @param isOverview - <code>boolean</code> - 
     * @return - <code>String</code> - 
     */
    public final String getToolTipText( final boolean isOverview) {
    StringBuffer tip = new StringBuffer( "<html> ");
    tip.append( resource.getName());
    tip.append( "<br>key=");
    tip.append( resource.getId().toString());
    tip.append( "</html>");
    return tip.toString();
    } // end getToolTipText

    /**
     * <code>doMouseClick</code> - 
     *
     * @param modifiers - <code>int</code> - 
     * @param docCoords - <code>Point</code> - 
     * @param viewCoords - <code>Point</code> - 
     * @param view - <code>JGoView</code> - 
     * @return - <code>boolean</code> - 
     */
    public final boolean doMouseClick( final int modifiers, final Point docCoords,
                                       final Point viewCoords, final JGoView view) {
      JGoObject obj = view.pickDocObject( docCoords, false);
      //         System.err.println( "doMouseClick obj class " +
      //                             obj.getTopLevelObject().getClass().getName());
      ProfileLine transactionObject = (ProfileLine) obj.getTopLevelObject();
      if (MouseEventOSX.isMouseLeftClick( modifiers, PlanWorks.isMacOSX())) {
        // do nothing
      } else if (MouseEventOSX.isMouseRightClick( modifiers, PlanWorks.isMacOSX())) {
        // mouseRightPopupMenu( viewCoords);
        // return true;
      }
      return false;
    } // end doMouseClick   

  } // end class ProfileLine

  /**
   * <code>toString</code>
   *
   * @return - <code>String</code> - 
   */
  public final String toString() {
    return resource.getId().toString();
  }

  /**
   * <code>equals</code>
   *
   * @param other - <code>ResourceProfile</code> - 
   * @return - <code>boolean</code> - 
   */
  public final boolean equals( final ResourceProfile other) {
    return resource.getId().equals( other.getResource().getId());
  }

  /**
   * <code>hashCode</code>
   *
   * @return - <code>int</code> - 
   */
  public final int hashCode() {
    return resource.getId().intValue();
  }



} // end class ResourceProfile


  
 






















