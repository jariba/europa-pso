// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: ResourceProfile.java,v 1.6 2004-03-04 21:30:27 taylor Exp $
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
import gov.nasa.arc.planworks.util.Extent;
import gov.nasa.arc.planworks.util.MouseEventOSX;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.nodes.NodeGenerics;
import gov.nasa.arc.planworks.viz.nodes.ResourceNameNode;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanViewSet;
import gov.nasa.arc.planworks.viz.partialPlan.ResourceView;


/**
 * <code>ResourceProfile</code> - JGo widget to render a resource's extents

 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *       NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class ResourceProfile extends BasicNode implements Extent {

  private static final int RESOURCE_NAME_Y_OFFSET = 2;
  private static final int NUM_LEVEL_SCALE_TICKS = 6;
  private static final int LEVEL_SCALE_TICK_WIDTH = 10;
  private static final double ONE_HALF_MULTIPLIER = 0.5;

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
  private int cellRow; // for layout algorithm
  private String resourceId;
  private int extentYTop;
  private int extentYBottom;
  private int profileYOrigin;
  private double levelScaleScaling;
  private int levelScaleWidth;
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
    cellRow = Algorithms.NO_ROW;
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

    renderBorders( resourceProfileView.getJGoRulerView().scaleTime( earliestStartTime),
                   resourceProfileView.getJGoRulerView().scaleTime( latestEndTime),
                   resourceProfileView.getJGoExtentDocument());
    levelScaleWidth = resourceProfileView.getLevelScaleViewWidth() -
      ViewConstants.RESOURCE_LEVEL_SCALE_WIDTH_OFFSET;
    renderBorders( 0, levelScaleWidth, resourceProfileView.getJGoLevelScaleDocument());

    renderResourceName();

    levelLimitMin = resource.getLevelLimitMin();
    levelLimitMax = resource.getLevelLimitMax();
    double minMax[] = ResourceProfile.getResourceMinMax( resource);
    levelMin = (int) minMax[0];
    levelMax = (int) minMax[1];
    levelScaleScaling = (extentYBottom - extentYTop) / (levelMax - levelMin);
    // System.err.println( "extentYTop " + extentYTop + " extentYBottom " + extentYBottom);
    // System.err.println( " levelMin " + levelMin + " levelMax " +
    //                     levelMax + " levelScaleScaling " + levelScaleScaling);

    renderLevelScaleLinesAndTicks();

    renderLimits();

    renderLevels();

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
   * <code>getExtentYTop</code>
   *
   * @return - <code>int</code> - 
   */
  public final int getExtentYTop() {
    return extentYTop;
  }

  /**
   * <code>getExtentYBottom</code>
   *
   * @return - <code>int</code> - 
   */
  public final int getExtentYBottom() {
    return extentYBottom;
  }

  /**
   * <code>getProfileYOrigin</code>
   *
   * @return - <code>int</code> - 
   */
  public final int getProfileYOrigin() {
    return profileYOrigin;
  }

  private void renderBorders( final int xLeft, final int xRight,
                              final JGoDocument jGoDocument) {
    profileYOrigin = scaleY( 0);
    JGoStroke divider = new JGoStroke();
    divider.addPoint( xLeft, profileYOrigin);
    divider.addPoint( xRight, profileYOrigin);
    divider.setDraggable( false); divider.setResizable( false);
    divider.setSelectable( false);
    divider.setPen( new JGoPen( JGoPen.SOLID, 2, ColorMap.getColor( "black")));
    jGoDocument.addObjectAtTail( divider);

    extentYTop = scaleY( ViewConstants.RESOURCE_PROFILE_MAX_Y_OFFSET);
    JGoStroke extentTop = new JGoStroke();
    extentTop.addPoint( xLeft, extentYTop);
    extentTop.addPoint( xRight, extentYTop);
    extentTop.setDraggable( false); extentTop.setResizable( false);
    extentTop.setSelectable( false);
    extentTop.setPen( new JGoPen( JGoPen.SOLID, 2, ColorMap.getColor( "green3")));
    jGoDocument.addObjectAtTail( extentTop);

    extentYBottom = scaleY( ViewConstants.RESOURCE_PROFILE_CELL_HEIGHT -
                            ViewConstants.RESOURCE_PROFILE_MIN_Y_OFFSET);
    JGoStroke extentBottom = new JGoStroke();
    extentBottom.addPoint( xLeft, extentYBottom);
    extentBottom.addPoint( xRight, extentYBottom);
    extentBottom.setDraggable( false); extentBottom.setResizable( false);
    extentBottom.setSelectable( false);
    extentBottom.setPen( new JGoPen( JGoPen.SOLID, 2, ColorMap.getColor( "green3")));
    jGoDocument.addObjectAtTail( extentBottom);
  } // end renderBorders

  private void renderResourceName() {
    int xTop = resourceProfileView.getLevelScaleViewWidth() - nodeLabelWidth +
      (int) (ViewConstants.TIMELINE_VIEW_INSET_SIZE * ONE_HALF_MULTIPLIER);
    Point nameLoc = new Point( xTop, scaleY( RESOURCE_NAME_Y_OFFSET));
    ResourceNameNode nameNode = new ResourceNameNode( nameLoc, resource);
    nameNode.setResizable( false); nameNode.setEditable( false);
    nameNode.setDraggable( false); nameNode.setSelectable( false);
    nameNode.setBkColor( ViewConstants.VIEW_BACKGROUND_COLOR);
    resourceProfileView.getJGoLevelScaleDocument().addObjectAtTail( nameNode);
  } // end renderResourceName

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
                   scaleResourceLevel( (double) level + tickDelta * ONE_HALF_MULTIPLIER));
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
    JGoStroke levelLimitMaxLine = new JGoStroke();
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

    JGoStroke levelLimitMinLine = new JGoStroke();
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
    ProfileLine levelMaxLine = new ProfileLine();
    ProfileLine levelMinLine = new ProfileLine();
    double lastLevelMax = initialCapacity;
    double lastLevelMin = initialCapacity;
    levelMaxLine.addPoint( resourceProfileView.getJGoRulerView().scaleTime( earliestStartTime),
                           scaleResourceLevel( lastLevelMax));
    levelMinLine.addPoint( resourceProfileView.getJGoRulerView().scaleTime( earliestStartTime),
                           scaleResourceLevel( lastLevelMin));
    while (instantItr.hasNext()) {
      PwResourceInstant instant = (PwResourceInstant) instantItr.next();
      int time = instant.getTime();
      double currentLevelMax = instant.getLevelMax();
      double currentLevelMin = instant.getLevelMin();
      levelMaxLine.addPoint( resourceProfileView.getJGoRulerView().scaleTime( time),
                             scaleResourceLevel( lastLevelMax));
      levelMaxLine.addPoint( resourceProfileView.getJGoRulerView().scaleTime( time),
                             scaleResourceLevel( currentLevelMax ));
      levelMinLine.addPoint( resourceProfileView.getJGoRulerView().scaleTime( time),
                             scaleResourceLevel( lastLevelMin));
      levelMinLine.addPoint( resourceProfileView.getJGoRulerView().scaleTime( time),
                             scaleResourceLevel( currentLevelMin));
      lastLevelMax = currentLevelMax; lastLevelMin = currentLevelMin;
    }
    levelMaxLine.addPoint( resourceProfileView.getJGoRulerView().scaleTime( latestEndTime),
                           scaleResourceLevel( lastLevelMax));
    levelMinLine.addPoint( resourceProfileView.getJGoRulerView().scaleTime( latestEndTime),
                           scaleResourceLevel( lastLevelMin));
    levelMaxLine.setDraggable( false); levelMaxLine.setResizable( false);
    levelMaxLine.setSelectable( false);
    levelMaxLine.setPen( new JGoPen( JGoPen.SOLID, 2, ColorMap.getColor( "magenta")));
    levelMinLine.setDraggable( false); levelMinLine.setResizable( false);
    levelMinLine.setSelectable( false);
    levelMinLine.setPen( new JGoPen( JGoPen.SOLID, 2, ColorMap.getColor( "blue")));
    resourceProfileView.getJGoExtentDocument().addObjectAtTail( levelMinLine);
    resourceProfileView.getJGoExtentDocument().addObjectAtTail( levelMaxLine);
  } // end renderLevels


  private int scaleResourceLevel( final double level) {
    return (extentYBottom - (int) ((level - levelMin) * levelScaleScaling));
  }

//   /**
//    * <code>getToolTipText</code>
//    *
//    * @return - <code>String</code> - 
//    */
//   public final String getToolTipText() {
//     StringBuffer tip = new StringBuffer( "<html>key = ");
//     tip.append( resource.getId().toString());
//     tip.append( "</html>");
//     return tip.toString();
//   } // end getToolTipText


//   /**
//    * <code>getToolTipText</code> - when over 1/8 scale overview resource node
//    *
//    * @param isOverview - <code>boolean</code> - 
//    * @return - <code>String</code> - 
//    */
//   public final String getToolTipText( final boolean isOverview) {
//     StringBuffer tip = new StringBuffer( "<html> ");
//     tip.append( resource.getName());
//     tip.append( "<br>key=");
//     tip.append( resource.getId().toString());
//     tip.append( "</html>");
//     return tip.toString();
//   } // end getToolTipText



  /**
   * <code>ProfileLine</code> - render profile as a line
   *
   */
  public  class ProfileLine extends JGoStroke {

    /**
     * <code>ProfileLine</code> - constructor 
     *
     */
    public ProfileLine() {
      super();
    }

    /**
     * <code>getToolTipText</code>
     *
     * @return - <code>String</code> - 
     */
    public final String getToolTipText() {
      return null;
    } // end getToolTipText

  /**
   * <code>getToolTipText</code> - when over 1/8 scale overview resource node
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
        //       mouseRightPopupMenu( viewCoords);
        //       return true;
      }
      return false;
    } // end doMouseClick   

  } // end class ProfileLine


  /**
   * <code>getStart</code> - implements Extent
   *
   *           allow for label width as well as time extent
   *
   * @return - <code>int</code> - 
   */
  public final int getStart() {
    int xStart = resourceProfileView.getJGoRulerView().scaleTime( earliestStartTime);
//     if (resourceId.equals( "185") || resourceId.equals( "978")) {
//       System.err.println( "xStart: " + predicateName + " xStart " +
//                           String.valueOf( (xStart - ViewConstants.TIMELINE_VIEW_INSET_SIZE)));
//     }
    return xStart - ViewConstants.TIMELINE_VIEW_INSET_SIZE;
  }

  /**
   * <code>getEnd</code> - implements Extent
   *
   *           allow for label width as well as time extent
   *
   * @return - <code>int</code> - 
   */
  public final int getEnd() {
    int xStart = resourceProfileView.getJGoRulerView().scaleTime( earliestStartTime);
    int xEnd = resourceProfileView.getJGoRulerView().scaleTime( latestEndTime);
    int xStartPlusLabel = xStart;
//     if (resourceId.equals( "185") || resourceId.equals( "978")) {
//       System.err.println( "xEnd: " + predicateName + " xEnd " +
//                           String.valueOf( (xEnd + ViewConstants.TIMELINE_VIEW_INSET_SIZE)) +
//                           " xStartPlusLabel " +
//                           String.valueOf( (xStartPlusLabel +
//                                           ViewConstants.TIMELINE_VIEW_INSET_SIZE)));
//       System.err.println( "isShowLabels " + isShowLabels + " nodeLabelWidth " +
//                           String.valueOf( nodeLabelWidth));
//     }
    return Math.max( xEnd, xStartPlusLabel) + ViewConstants.TIMELINE_VIEW_INSET_SIZE;
  }

  /**
   * <code>getRow</code> - implements Extent
   *
   * @return - <code>int</code> - 
   */
  public final int getRow() {
    return cellRow;
  }

  /**
   * <code>setRow</code> - implements Extent
   *
   * @param row - <code>int</code> - 
   */
  public final void setRow( final int row) {
    cellRow = row;
  }

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

  private int scaleY( final int cellDelta) {
    return resourceProfileView.getStartYLoc() +
      (int) (cellRow * ViewConstants.RESOURCE_PROFILE_CELL_HEIGHT) + cellDelta;
  }


 
  /**
   * <code>doMouseClick</code> - Mouse-Right: Set Active Resource
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
    ResourceProfile resourceProfile = (ResourceProfile) obj.getTopLevelObject();
    if (MouseEventOSX.isMouseLeftClick( modifiers, PlanWorks.isMacOSX())) {
      // do nothing
    } else if (MouseEventOSX.isMouseRightClick( modifiers, PlanWorks.isMacOSX())) {
//       mouseRightPopupMenu( viewCoords);
//       return true;
    }
    return false;
  } // end doMouseClick   

  private void mouseRightPopupMenu( final Point viewCoords) {
    JPopupMenu mouseRightPopup = new JPopupMenu();

    JMenuItem navigatorItem = new JMenuItem( "Open Navigator View");
    navigatorItem.addActionListener( new ActionListener() {
        public final void actionPerformed( final ActionEvent evt) {
          MDIInternalFrame navigatorFrame = resourceProfileView.openNavigatorViewFrame();
          Container contentPane = navigatorFrame.getContentPane();
          PwPartialPlan partialPlan = resourceProfileView.getPartialPlan();
//           contentPane.add( new NavigatorView( ResourceProfile.this, partialPlan,
//                                               resourceProfileView.getViewSet(),
//                                               navigatorFrame));
        }
      });
    mouseRightPopup.add( navigatorItem);

    JMenuItem activeResourceItem = new JMenuItem( "Set Active Resource");
    final PwResource activeResource = ResourceProfile.this.getResource();
    // check for empty slots
    if (activeResource != null) {
      activeResourceItem.addActionListener( new ActionListener() {
          public final void actionPerformed( final ActionEvent evt) {
            ((PartialPlanViewSet) resourceProfileView.getViewSet()).
              setActiveResource( activeResource);
            System.err.println( "ResourceProfile setActiveResource: " +
                                activeResource.getName() +
                                " (key=" + activeResource.getId().toString() + ")");
          }
        });
      mouseRightPopup.add( activeResourceItem);

      NodeGenerics.showPopupMenu( mouseRightPopup, resourceProfileView, viewCoords);
    }
  } // end mouseRightPopupMenu


} // end class ResourceProfile


  
  
