// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: ResourceTransactionView.java,v 1.2 2004-02-11 02:29:31 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 04feb04
//

package gov.nasa.arc.planworks.viz.partialPlan.resourceTransaction;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.util.ArrayList;
import java.util.Iterator;    
import java.util.List;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JOptionPane;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.SwingUtilities;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoDocument;
import com.nwoods.jgo.JGoPen;
import com.nwoods.jgo.JGoStroke;
import com.nwoods.jgo.JGoView;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.PwPlanningSequence;
import gov.nasa.arc.planworks.db.PwResource;
import gov.nasa.arc.planworks.db.PwSlot;
// testing
import gov.nasa.arc.planworks.db.PwIntervalDomain;
import gov.nasa.arc.planworks.db.impl.PwIntervalDomainImpl;
import gov.nasa.arc.planworks.db.impl.PwResourceImpl;
import gov.nasa.arc.planworks.db.impl.PwResourceInstantImpl;
import gov.nasa.arc.planworks.db.impl.PwResourceTransactionImpl;

import gov.nasa.arc.planworks.mdi.MDIInternalFrame;
import gov.nasa.arc.planworks.util.Algorithms;
import gov.nasa.arc.planworks.util.ColorMap;
import gov.nasa.arc.planworks.util.MouseEventOSX;
import gov.nasa.arc.planworks.util.UniqueSet;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.ViewGenerics;
import gov.nasa.arc.planworks.viz.VizViewOverview;
import gov.nasa.arc.planworks.viz.nodes.NodeGenerics;
import gov.nasa.arc.planworks.viz.partialPlan.AskNodeByKey;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanView;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanViewSet;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanViewState;
import gov.nasa.arc.planworks.viz.partialPlan.TimeScaleView;
import gov.nasa.arc.planworks.viz.partialPlan.resourceProfile.ResourceProfileView;
import gov.nasa.arc.planworks.viz.viewMgr.ViewableObject;
import gov.nasa.arc.planworks.viz.viewMgr.ViewSet;

/**
 * <code>ResourceTransactionView</code> - render the transactions of a
 *                partial plan's resources
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                  NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class ResourceTransactionView extends PartialPlanView  {

  private static final int SLEEP_FOR_50MS = 50;

  private long startTimeMSecs;
  private ViewSet viewSet;
  private MDIInternalFrame viewFrame;
  private ExtentView jGoExtentView;
  private JGoView jGoLevelScaleView;
  private TimeScaleView jGoRulerView;
  private Component horizontalStrut;
  private List resourceTransactionSetList; // element ResourceTransactionSet
  private int slotLabelMinLength;
  private int maxSlots;
  private int startXLoc;
  private int startYLoc;
  private int xOrigin;
  private int maxCellRow;
  private float timeScale;
  private JGoStroke timeScaleMark;
  private int levelScaleViewWidth;
  private int fillerWidth;
  private int maxYExtent;
  private JGoStroke maxExtentViewHeightPoint;
  private JGoStroke maxLevelViewHeightPoint;
  private boolean isStepButtonView;

  /**
   * variable <code>currentYLoc</code>
   *
   */
  protected int currentYLoc;

  private static Point docCoords;


  /**
   * <code>ResourceTransactionView</code> - constructor 
   *                             Use SwingUtilities.invokeLater( runInit) to
   *                             properly render the JGo widgets
   *
   * @param partialPlan - <code>ViewableObject</code> - 
   * @param vSet - <code>ViewSet</code> - 
   */
  public ResourceTransactionView( final ViewableObject partialPlan, final ViewSet vSet) {
    super( (PwPartialPlan) partialPlan, (PartialPlanViewSet) vSet);
    resourceTransactionViewInit( vSet);
    isStepButtonView = false;

    SwingUtilities.invokeLater( runInit);
  } // end constructor


  /**
   * <code>ResourceTransactionView</code> - constructor 
   *
   * @param partialPlan - <code>ViewableObject</code> - 
   * @param vSet - <code>ViewSet</code> - 
   * @param state - <code>PartialPlanViewState</code> - 
   */
  public ResourceTransactionView( final ViewableObject partialPlan, final ViewSet vSet, 
                                  final PartialPlanViewState state) {
    super( (PwPartialPlan) partialPlan, (PartialPlanViewSet) vSet);
    resourceTransactionViewInit( vSet);
    isStepButtonView = true;
    setState( state);
    SwingUtilities.invokeLater( runInit);
  }

  private void resourceTransactionViewInit( final ViewSet vSet) {
    this.startTimeMSecs = System.currentTimeMillis();
    this.viewSet = (PartialPlanViewSet) vSet;
    
    // startXLoc = ViewConstants.TIMELINE_VIEW_X_INIT * 2;
    startXLoc = 1;
    startYLoc = ViewConstants.TIMELINE_VIEW_Y_INIT;
    currentYLoc = startYLoc;
    maxCellRow = 0;
    timeScaleMark = null;
    maxYExtent = 0;
    maxExtentViewHeightPoint = null;
    maxLevelViewHeightPoint = null;
    slotLabelMinLength = ViewConstants.TIMELINE_VIEW_EMPTY_NODE_LABEL_LEN;
    // create panels/views after fontMetrics available
   } // end resourceTransactionViewInit

  private void createLevelScaleAndExtentPanel() {
    LevelScalePanel levelScalePanel = new LevelScalePanel();
    levelScalePanel.setLayout( new BoxLayout( levelScalePanel, BoxLayout.Y_AXIS));

    jGoLevelScaleView = new JGoView();
    jGoLevelScaleView.setBackground( ViewConstants.VIEW_BACKGROUND_COLOR);
    jGoLevelScaleView.getVerticalScrollBar().addAdjustmentListener
      ( new VerticalScrollBarListener());
    
    jGoLevelScaleView.validate();
    jGoLevelScaleView.setVisible( true);
    levelScalePanel.add( jGoLevelScaleView, BorderLayout.NORTH);                 
    add( levelScalePanel, "West");

    JPanel extentViewPanel = new JPanel();
    extentViewPanel.setLayout( new BoxLayout( extentViewPanel, BoxLayout.Y_AXIS));

    jGoExtentView = new ExtentView();
    jGoExtentView.setBackground( ViewConstants.VIEW_BACKGROUND_COLOR);
    jGoExtentView.getHorizontalScrollBar().addAdjustmentListener
                                     ( new HorizontalScrollBarListener());
    jGoExtentView.getVerticalScrollBar().addAdjustmentListener
                                     ( new VerticalScrollBarListener());
    jGoExtentView.validate();
    jGoExtentView.setVisible( true);
    extentViewPanel.add( jGoExtentView, BorderLayout.NORTH);
    add( extentViewPanel, "Center");
  } // end createLevelScaleAndExtentPanel

  private void createFillerAndRulerPanel() {
    FillerAndRulerPanel fillerAndRulerPanel = new FillerAndRulerPanel();
    fillerAndRulerPanel.setLayout( new BoxLayout( fillerAndRulerPanel, BoxLayout.X_AXIS));
    fillerAndRulerPanel.setBackground( ViewConstants.VIEW_BACKGROUND_COLOR);

    horizontalStrut = Box.createHorizontalStrut( fillerWidth);
    fillerAndRulerPanel.add( horizontalStrut, BorderLayout.WEST);

    JPanel rulerPanel = new JPanel();
    rulerPanel.setLayout( new BoxLayout( rulerPanel, BoxLayout.Y_AXIS));

    jGoRulerView = new TimeScaleView( startXLoc, startYLoc, partialPlan, this);
    jGoRulerView.setBackground( ViewConstants.VIEW_BACKGROUND_COLOR);
    jGoRulerView.getHorizontalScrollBar().addAdjustmentListener
                                     ( new HorizontalScrollBarListener());
    jGoRulerView.validate();
    jGoRulerView.setVisible( true);
    rulerPanel.add( jGoRulerView, BorderLayout.NORTH);
    fillerAndRulerPanel.add( rulerPanel, BorderLayout.EAST);
    add( fillerAndRulerPanel, "South");
  } // end createFillerAndRulerPanel

  /**
   * <code>getState</code>
   *
   * @return - <code>PartialPlanViewState</code> - 
   */
  public final PartialPlanViewState getState() {
    return new ResourceTransactionViewState( this);
  }

//   public boolean showLabels(){return isShowLabels;}
//   public int displayMode(){return temporalDisplayMode;}

  /**
   * <code>setState</code>
   *
   * @param state - <code>PartialPlanViewState</code> - 
   */
  public final void setState( final PartialPlanViewState state) {
    super.setState( state);
    ResourceTransactionViewState resourceState = (ResourceTransactionViewState) state;
//     isShowLabels = state.showingLabels();
//     temporalDisplayMode = state.displayMode();
  }

  private Runnable runInit = new Runnable() {
      public final void run() {
        init();
      }
    };

  /**
   * <code>init</code> - wait for instance to become displayable, determine
   *                     appropriate font metrics, and render the JGo timeline,
   *                     and slot widgets
   *
   *    These functions are not done in the constructor to avoid:
   *    "Cannot measure text until a JGoExtentView exists and is part of a visible window".
   *     int extentScrollExtent = jGoExtentView.getHorizontalScrollBar().getSize().getWidth();
   *    called by componentShown method on the JFrame
   *    JGoExtentView.setVisible( true) must be completed -- use runInit in constructor
   */
  public final void init() {
    // jGoExtentView.setCursor( new Cursor( Cursor.WAIT_CURSOR));
    // wait for ResourceTransactionView instance to become displayable
    while (! this.isDisplayable()) {
      try {
        Thread.currentThread().sleep( SLEEP_FOR_50MS);
      } catch (InterruptedException excp) {
      }
      // System.err.println( "timelineView displayable " + this.isDisplayable());
    }
    this.computeFontMetrics( this);
    
    levelScaleViewWidth = computeMaxResourceLabelWidth();
    fillerWidth = levelScaleViewWidth + ViewConstants.JGO_SCROLL_BAR_WIDTH;

    setLayout( new BorderLayout());

    createLevelScaleAndExtentPanel();

    createFillerAndRulerPanel();
  
    this.setVisible( true);
    
    boolean doFreeTokens = false;
    xOrigin = jGoRulerView.collectAndComputeTimeScaleMetrics( doFreeTokens, this);
    jGoRulerView.createTimeScale();

    boolean isRedraw = false, isScrollBarAdjustment = false;
    renderResourceExtent();

    viewFrame = viewSet.openView( this.getClass().getName());
    if (! isStepButtonView) {
      expandViewFrame( viewFrame,
                       (int) (Math.max( jGoExtentView.getDocumentSize().getWidth(),
                                        jGoRulerView.getDocumentSize().getWidth()) +
                              jGoLevelScaleView.getDocumentSize().getWidth() +
                              jGoExtentView.getVerticalScrollBar().getSize().getWidth()),
                       (int) (jGoExtentView.getDocumentSize().getHeight() +
                              jGoRulerView.getDocumentSize().getHeight()));
    } else {
      // force re-display
      this.validate();
    }
    // print out info for created nodes
    // iterateOverJGoDocument(); // slower - many more nodes to go thru
    // iterateOverNodes();

    int maxStepButtonY = addStepButtons( jGoExtentView);
    if (! isStepButtonView) {
      expandViewFrameForStepButtons( viewFrame);
    }
    // equalize ExtentView & ScaleView widths so horizontal scrollbars are equal
    // equalize ExtentView & LevelScaleView heights so vertical scrollbars are equal
    equalizeViewWidthsAndHeights( maxStepButtonY, isRedraw, isScrollBarAdjustment);

    long stopTimeMSecs = System.currentTimeMillis();
    System.err.println( "   ... elapsed time: " +
                        (stopTimeMSecs - startTimeMSecs) + " msecs.");
    
    // jGoExtentView.setCursor( new Cursor( Cursor.DEFAULT_CURSOR));
  } // end init


  /**
   * <code>redraw</code> - called by Content Spec to apply user's content spec request.
   *                       setVisible(true | false)
   *                       according to the Content Spec enabled ids
   *
   */
  public final void redraw() {
    Thread thread = new RedrawViewThread();
    thread.setPriority( Thread.MIN_PRIORITY);
    thread.start();
  }

  /**
   * <code>RedrawViewThread</code> - execute redraw in a new thread
   *
   */
  class RedrawViewThread extends Thread {

    public RedrawViewThread() {
    }  // end constructor

    public final void run() {
      boolean isRedraw = true, isScrollBarAdjustment = false;
      currentYLoc = startYLoc;

      renderResourceExtent();

      int maxStepButtonY = addStepButtons( jGoExtentView);
      if (! isStepButtonView) {
        expandViewFrameForStepButtons( viewFrame);
      }
      // equalize ExtentView & ScaleView widths so horizontal scrollbars are equal
      // equalize ExtentView & LevelScaleView heights so vertical scrollbars are equal
      equalizeViewWidthsAndHeights( maxStepButtonY, isRedraw, isScrollBarAdjustment);
    } // end run

  } // end class RedrawViewThread

  private void renderResourceExtent() {
    jGoExtentView.getDocument().deleteContents();

    validTokenIds = viewSet.getValidIds();
    displayedTokenIds = new ArrayList();
    if (resourceTransactionSetList != null) {
      resourceTransactionSetList.clear();
    }
    resourceTransactionSetList = new UniqueSet();

    createResourceTransactionSets();

    boolean showDialog = true;
    // isContentSpecRendered( PlanWorks.RESOURCE_TRANSACTION_VIEW, showDialog);

  } // end createResourceTransactionView

  /**
   * <code>getJGoExtentView</code> - 
   *
   * @return - <code>JGoView</code> - 
   */
  public final JGoView getJGoExtentView()  {
    return this.jGoExtentView;
  }

  /**
   * <code>getJGoExtentDocument</code> - the resource extent view document
   *
   * @return - <code>JGoDocument</code> - 
   */
  public final JGoDocument getJGoExtentDocument()  {
    return this.jGoExtentView.getDocument();
  }

  /**
   * <code>getJGoLevelScaleDocument</code> - the level scale view document
   *
   * @return - <code>JGoDocument</code> - 
   */
  public final JGoDocument getJGoLevelScaleDocument()  {
    return this.jGoLevelScaleView.getDocument();
  }

  /**
   * <code>getLevelScaleViewWidth</code>
   *
   * @return - <code>int</code> - 
   */
  public final int getLevelScaleViewWidth() {
    return levelScaleViewWidth;
  }

  /**
   * <code>getResourceTransactionSetList</code>
   *
   * @return - <code>List</code> - of ResourceTransactionSet
   */
  public final List getResourceTransactionSetList() {
    return resourceTransactionSetList;
  }

  /**
   * <code>getTimeScale</code>
   *
   * @return - <code>float</code> - 
   */
  public final float getTimeScale() {
    return jGoRulerView.getTimeScale();
  }

  /**
   * <code>getTimeScaleStart</code>
   *
   * @return - <code>int</code> - 
   */
  public final int getTimeScaleStart() {
    return jGoRulerView.getTimeScaleStart();
  }

  /**
   * <code>getTimeScaleEnd</code>
   *
   * @return - <code>int</code> - 
   */
  public final int getTimeScaleEnd() {
    return jGoRulerView.getTimeScaleEnd();
  }

  /**
   * <code>getJGoRulerView</code>
   *
   * @return - <code>TimeScaleView</code> - 
   */
  public final TimeScaleView getJGoRulerView() {
    return jGoRulerView;
  }

  private List createDummyData( final boolean isNameOnly) {
    int startTime = 0, endTime = 0;
    if (! isNameOnly) {
      startTime = getTimeScaleStart();
      endTime = getTimeScaleEnd();
    }
    List resourceList = new ArrayList();
    List resourceInstantList = new ArrayList();
    PwIntervalDomain instantDomain =
      (PwIntervalDomain) new PwIntervalDomainImpl ( "type", "10", "10");
    resourceInstantList.add
      ( new PwResourceInstantImpl( new Integer( 99011), instantDomain, 4., 6.));
    resourceInstantList.add
      ( new PwResourceInstantImpl( new Integer( 99012),
                                   new PwIntervalDomainImpl( "type", "20", "20"), 10., 14.));
    resourceInstantList.add
      ( new PwResourceInstantImpl( new Integer( 99013),
                                   new PwIntervalDomainImpl( "type", "30", "30"), -2., 0.));
    resourceInstantList.add
      ( new PwResourceInstantImpl( new Integer( 99014),
                                   new PwIntervalDomainImpl( "type", "50", "50"), 2., 4.));

    UniqueSet transactionSet = new UniqueSet();
    transactionSet.add
      ( new PwResourceTransactionImpl( new Integer( 990111),
                                       new PwIntervalDomainImpl( "type", "10", "50"), 2., 2.));
    transactionSet.add
      ( new PwResourceTransactionImpl( new Integer( 990112),
                                       new PwIntervalDomainImpl( "type", "20", "50"), 6., 6.));
    transactionSet.add
      ( new PwResourceTransactionImpl( new Integer( 990113),
                                       new PwIntervalDomainImpl( "type", "30", "50"), -8., -8.));

    PwResource dummyResource =
      new PwResourceImpl( new Integer( 9901), "Resource1", 4., 0., 12., startTime, endTime,
                          transactionSet, resourceInstantList);
    resourceList.add( dummyResource);

    transactionSet = new UniqueSet();
    transactionSet.add
      ( new PwResourceTransactionImpl( new Integer( 990111),
                                       new PwIntervalDomainImpl( "type", "10", "50"), 2., 2.));
    transactionSet.add
      ( new PwResourceTransactionImpl( new Integer( 990112),
                                       new PwIntervalDomainImpl( "type", "20", "50"), 6., 6.));
    transactionSet.add
      ( new PwResourceTransactionImpl( new Integer( 990113),
                                       new PwIntervalDomainImpl( "type", "30", "50"), -8., -8.));
    transactionSet.add
      ( new PwResourceTransactionImpl( new Integer( 990114),
                                       new PwIntervalDomainImpl( "type", "40", "50"), 4., 4.));
    transactionSet.add
      ( new PwResourceTransactionImpl( new Integer( 990115),
                                       new PwIntervalDomainImpl( "type", "50", "60"), -4., -4.));
        
    dummyResource =
      new PwResourceImpl( new Integer( 9902), "Resource2", 4., 0., 12., startTime, endTime,
                          transactionSet, resourceInstantList);
    resourceList.add( dummyResource);
    dummyResource =
      new PwResourceImpl( new Integer( 9903), "ResourceThree", 4., 0., 12., startTime, endTime,
                          transactionSet, resourceInstantList);
    resourceList.add( dummyResource);
    dummyResource =
      new PwResourceImpl( new Integer( 9904), "ResourceFour", 4., 0., 12., startTime, endTime,
                          transactionSet, resourceInstantList);
    resourceList.add( dummyResource);
    dummyResource =
      new PwResourceImpl( new Integer( 9905), "ResourceFive", 4., 0., 12., startTime, endTime,
                          transactionSet, resourceInstantList);
    resourceList.add( dummyResource);
    return resourceList;
  } // end createDummyData


  private void createResourceTransactionSets() {
    boolean isNamesOnly = false;
    // resourceList will come from partialPlan
    List resourceList = createDummyData( isNamesOnly);
    Iterator resourceItr = resourceList.iterator();
    while (resourceItr.hasNext()) {
      PwResource resource = (PwResource) resourceItr.next();
      ResourceTransactionSet resourceTransactionSet =
        new ResourceTransactionSet( resource,
                                    ColorMap.getColor( ViewConstants.FREE_TOKEN_BG_COLOR),
                                    this);
      // System.err.println( "resourceTransactionSet " + resourceTransactionSet);
      jGoExtentView.getDocument().addObjectAtTail( resourceTransactionSet);
      resourceTransactionSetList.add( resourceTransactionSet);
    }

  } // end createResourceTransactionSets

  private int computeMaxResourceLabelWidth() {
    boolean isNamesOnly = true;
    int maxWidth = ViewConstants.JGO_SCROLL_BAR_WIDTH * 2;
    // resourceList will come from partialPlan
    List resourceList = createDummyData( isNamesOnly);
    Iterator resourceItr = resourceList.iterator();
    while (resourceItr.hasNext()) {
      PwResource resource = (PwResource) resourceItr.next();
      int width = ResourceTransactionSet.getNodeLabelWidth( resource.getName(), this);
      if (width > maxWidth) {
        maxWidth = width;
      }
    }
    return maxWidth;
  } // end computeMaxResourceLabelWidth



//   private void iterateOverNodes() {
//     int numResourceTransactionSets = resourceTransactionSetList.size();
//     //System.err.println( "iterateOverNodes: numResourceTransactionSets " + numResourceTransactionSets);
//     Iterator resourceIterator = resourceTransactionSetList.iterator();
//     while (resourceIterator.hasNext()) {
//       ResourceTransactionSet resourceTransactionSet = (ResourceTransactionSet) resourceIterator.next();
//       System.err.println( "name '" + resourceTransactionSet.getPredicateName() + "' location " +
//                           resourceTransactionSet.getLocation());
//     }
//   } // end iterateOverNodes


//   private void iterateOverJGoDocument() {
//     JGoListPosition position = jGoExtentView.getDocument().getFirstObjectPos();
//     int cnt = 0;
//     while (position != null) {
//       JGoObject object = jGoExtentView.getDocument().getObjectAtPos( position);
//       position = jGoExtentView.getDocument().getNextObjectPosAtTop( position);
//       //System.err.println( "iterateOverJGoDoc: position " + position +
//       //                   " className " + object.getClass().getName());
//       if (object instanceof ResourceTransactionSet) {
//         ResourceTransactionSet resourceTransactionSet = (ResourceTransactionSet) object;

//       }
//       cnt += 1;
// //       if (cnt > 100) {
// //         break;
// //       }
//     }
//     //System.err.println( "iterateOverJGoDoc: cnt " + cnt);
//   } // end iterateOverJGoDocument


  /**
   * <code>equalizeViewWidthsAndHeights</code>
   *
   *                    write a line at the max horizontal extent in each view, and
   *                    at max vertical extent in jGoExtentView & JGoLevelScaleView
   *                    is also called by PartialPlanView.ButtonAdjustmentListener
   *
   * @param maxStepButtonY - <code>int</code> - 
   * @param isRedraw - <code>boolean</code> - 
   * @param isScrollBarAdjustment - <code>boolean</code> - 
   */
  public final void equalizeViewWidthsAndHeights( final int maxStepButtonY,
                                                  final boolean isRedraw, 
                                                  final boolean isScrollBarAdjustment) {
    Dimension extentViewDocSize = jGoExtentView.getDocumentSize();
    Dimension rulerViewDocSize = jGoRulerView.getDocumentSize();
//     System.err.println( "extentViewDocumentWidth B" + extentViewDocSize.getWidth() +
//                         " rulerViewDocumentWidth B" + rulerViewDocSize.getWidth());
    int xRulerMargin = ViewConstants.TIMELINE_VIEW_X_INIT;
    int jGoDocBorderWidth = ViewConstants.JGO_DOC_BORDER_WIDTH;
    if (isRedraw) {
      xRulerMargin = 0;
    }
    int maxWidth = Math.max( (int) extentViewDocSize.getWidth() - jGoDocBorderWidth,
                             (int) rulerViewDocSize.getWidth() + xRulerMargin -
                             jGoDocBorderWidth);
//     System.err.println( "maxWidth " + maxWidth);
    if (! isScrollBarAdjustment) {
      equalizeViewWidths( maxWidth, isRedraw);
    }

    equalizeViewHeights( maxWidth, maxStepButtonY);

//     extentViewDocSize = jGoExtentView.getDocumentSize();
//     rulerViewDocSize = jGoRulerView.getDocumentSize();
//     System.err.println( "extentViewDocumentWidth A" + extentViewDocSize.getWidth() +
//                         " rulerViewDocumentWidth A" + rulerViewDocSize.getWidth());
  } // end equalizeViewWidthsAndHeights

  private void equalizeViewWidths( final int maxWidth, final boolean isRedraw) {
    JGoStroke maxViewWidthPoint = new JGoStroke();
    maxViewWidthPoint.addPoint( maxWidth, ViewConstants.TIMELINE_VIEW_Y_INIT);
    maxViewWidthPoint.addPoint( maxWidth, ViewConstants.TIMELINE_VIEW_Y_INIT * 2);
    // make mark invisible
    maxViewWidthPoint.setPen( new JGoPen( JGoPen.SOLID, 1, 
                                          ViewConstants.VIEW_BACKGROUND_COLOR));
    jGoExtentView.getDocument().addObjectAtTail( maxViewWidthPoint);

    if ( ! isRedraw) {
      maxViewWidthPoint = new JGoStroke();
      maxViewWidthPoint.addPoint( maxWidth, ViewConstants.TIMELINE_VIEW_Y_INIT);
      maxViewWidthPoint.addPoint( maxWidth, ViewConstants.TIMELINE_VIEW_Y_INIT * 2);
      // make mark invisible
      maxViewWidthPoint.setPen( new JGoPen( JGoPen.SOLID, 1,
                                            ViewConstants.VIEW_BACKGROUND_COLOR));
      jGoRulerView.getDocument().addObjectAtTail( maxViewWidthPoint);
    }
  } // end equalizeViewWidths

  private void equalizeViewHeights( final int maxWidth, final int maxStepButtonY) {
    // always put mark at max y location, so on redraw jGoRulerView does not expand
    int maxY = Math.max( currentYLoc + 2, maxStepButtonY);
    if (maxY > maxYExtent) {
      maxYExtent = maxY;
      if (maxExtentViewHeightPoint != null) {
        jGoExtentView.getDocument().removeObject( maxExtentViewHeightPoint);
      }
      maxExtentViewHeightPoint = new JGoStroke();
      maxExtentViewHeightPoint.addPoint( maxWidth, maxYExtent);
      maxExtentViewHeightPoint.addPoint( maxWidth - ViewConstants.TIMELINE_VIEW_X_INIT,
                                         maxYExtent);
      // make mark invisible
      maxExtentViewHeightPoint.setPen( new JGoPen( JGoPen.SOLID, 1,
                                                   ViewConstants.VIEW_BACKGROUND_COLOR));
      jGoExtentView.getDocument().addObjectAtTail( maxExtentViewHeightPoint);

      if (maxLevelViewHeightPoint != null) {
        jGoLevelScaleView.getDocument().removeObject( maxLevelViewHeightPoint);
      }
      maxLevelViewHeightPoint = new JGoStroke();
      maxLevelViewHeightPoint.addPoint( 0, maxYExtent);
      maxLevelViewHeightPoint.addPoint( levelScaleViewWidth -
                                        ViewConstants.RESOURCE_LEVEL_SCALE_WIDTH_OFFSET,
                                        maxYExtent);
      // make mark invisible
      maxLevelViewHeightPoint.setPen( new JGoPen( JGoPen.SOLID, 1,
                                                  ViewConstants.VIEW_BACKGROUND_COLOR));
      jGoLevelScaleView.getDocument().addObjectAtTail( maxLevelViewHeightPoint);
    }
  } // end equalizeViewHeights


  /**
   * <code>LevelScalePanel</code> - require level scale view panel to be of fixed width
   *
   */
  class LevelScalePanel extends JPanel {

    /**
     * <code>LevelScalePanel</code> - constructor 
     *
     */
    public LevelScalePanel() {
      super();
    }

    /**
     * <code>getMinimumSize</code>
     *
     * @return - <code>Dimension</code> - used by BoxLayout
     */
    public final Dimension getMinimumSize() {
      return new Dimension
        ( (int) (jGoLevelScaleView.getDocumentSize().getWidth() +
                 jGoLevelScaleView.getVerticalScrollBar().getSize().getWidth()),
          (int) (ResourceTransactionView.this.getSize().getHeight() -
                 jGoRulerView.getDocumentSize().getHeight() -
                 jGoRulerView.getHorizontalScrollBar().getSize().getHeight()));
    }

    /**
     * <code>getMaximumSize</code> - used by BoxLayout
     *
     * @return - <code>Dimension</code> - 
     */
    public final Dimension getMaximumSize() {
      return new Dimension
        ( (int) (jGoLevelScaleView.getDocumentSize().getWidth() +
                 jGoLevelScaleView.getVerticalScrollBar().getSize().getWidth()),
          (int) (ResourceTransactionView.this.getSize().getHeight() -
                 jGoRulerView.getDocumentSize().getHeight() -
                 jGoRulerView.getHorizontalScrollBar().getSize().getHeight()));
    }

    /**
     * <code>getPreferredSize</code> - used by BorderLayout
     *
     * @return - <code>Dimension</code> - 
     */
    public final Dimension getPreferredSize() {
      if ((jGoLevelScaleView.getVerticalScrollBar() == null) ||
          (jGoRulerView.getHorizontalScrollBar() == null)) {
        return new Dimension
          ( (int) jGoLevelScaleView.getDocumentSize().getWidth(),
            (int) (ResourceTransactionView.this.getSize().getHeight() -
                   jGoRulerView.getDocumentSize().getHeight()));
      } else {
        return new Dimension
          ( (int) (jGoLevelScaleView.getDocumentSize().getWidth() +
                   jGoLevelScaleView.getVerticalScrollBar().getSize().getWidth()),
            (int) (ResourceTransactionView.this.getSize().getHeight() -
                   jGoRulerView.getDocumentSize().getHeight() -
                   jGoRulerView.getHorizontalScrollBar().getSize().getHeight()));
      }
    }

  } // end class LevelScalePanel


  /**
   * <code>FillerAndRulerPanel</code> - require ruler view panel to be of fixed height
   *
   */
  class FillerAndRulerPanel extends JPanel {

    /**
     * <code>FillerAndRulerPanel</code> - constructor 
     *
     */
    public FillerAndRulerPanel() {
      super();
    }

    /**
     * <code>getMinimumSize</code>
     *
     * @return - <code>Dimension</code> - used by BoxLaout
     */
    public final Dimension getMinimumSize() {
      return new Dimension( (int) ResourceTransactionView.this.getSize().getWidth(),
                            (int) (jGoRulerView.getDocumentSize().getHeight() +
                                   jGoRulerView.getHorizontalScrollBar().getSize().getHeight()));
    }

    /**
     * <code>getMaximumSize</code>
     *
     * @return - <code>Dimension</code> - used by BoxLaout
     */
    public final Dimension getMaximumSize() {
      return new Dimension( (int) ResourceTransactionView.this.getSize().getWidth(),
                            (int) (jGoRulerView.getDocumentSize().getHeight() +
                                   jGoRulerView.getHorizontalScrollBar().getSize().getHeight()));
    }

    /**
     * <code>getPreferredSize</code> - used by BorderLayout
     *
     * @return - <code>Dimension</code> - 
     */
    public final Dimension getPreferredSize() {
      return new Dimension( (int) ResourceTransactionView.this.getSize().getWidth(),
                            (int) (jGoRulerView.getDocumentSize().getHeight() +
                                   jGoRulerView.getHorizontalScrollBar().getSize().getHeight()));
    }
  } // end class FillerAndRulerPanel


  /**
   * <code>HorizontalScrollBarListener</code> - keep both jGoExtentView & jGoRulerView aligned,
   *                                  when user moves one scroll bar
   *
   */
  class HorizontalScrollBarListener implements AdjustmentListener {

    /**
     * <code>adjustmentValueChanged</code> - keep both jGoExtentView & jGoRulerView
     *                                aligned, even when user moves one scroll bar
     *
     * @param event - <code>AdjustmentEvent</code> - 
     */
    public final void adjustmentValueChanged( final AdjustmentEvent event) {
      JScrollBar source = (JScrollBar) event.getSource();
      // to get immediate incremental adjustment, rather than waiting for
      // final position, comment out next check
      // if (! source.getValueIsAdjusting()) {
        int newPostion = source.getValue();
        if (newPostion != jGoExtentView.getHorizontalScrollBar().getValue()) {
          jGoExtentView.getHorizontalScrollBar().setValue( newPostion);
        } else if (newPostion != jGoRulerView.getHorizontalScrollBar().getValue()) {
          jGoRulerView.getHorizontalScrollBar().setValue( newPostion);
        }
        // }
    } // end adjustmentValueChanged 

  } // end class HorizontalScrollBarListener 


  /**
   * <code>VerticalScrollBarListener</code> - keep both jGoExtentView & jGoLevelScaleView
   *                                  aligned, when user moves one scroll bar
   *
   */
  class VerticalScrollBarListener implements AdjustmentListener {

    /**
     * <code>adjustmentValueChanged</code> - keep both jGoExtentView & jGoLevelScaleView
     *                                aligned, even when user moves one scroll bar
     *
     * @param event - <code>AdjustmentEvent</code> - 
     */
    public final void adjustmentValueChanged( final AdjustmentEvent event) {
      JScrollBar source = (JScrollBar) event.getSource();
      // to get immediate incremental adjustment, rather than waiting for
      // final position, comment out next check
      // if (! source.getValueIsAdjusting()) {
        int newPostion = source.getValue();
        if (newPostion != jGoExtentView.getVerticalScrollBar().getValue()) {
          jGoExtentView.getVerticalScrollBar().setValue( newPostion);
        } else if (newPostion != jGoLevelScaleView.getVerticalScrollBar().getValue()) {
          jGoLevelScaleView.getVerticalScrollBar().setValue( newPostion);
        }
        // }
    } // end adjustmentValueChanged 

  } // end class VerticalScrollBarListener 


  /**
   * <code>ExtentView</code> - subclass doBackgroundClick to handle drawing
   *                               vertical time marks on view
   *
   */
  class ExtentView extends JGoView {

    /**
     * <code>ExtentView</code> - constructor 
     *
     */
    public ExtentView() {
      super();
    }

    /**
     * <code>doBackgroundClick</code> - Mouse-Right pops up menu:
     *                             1) draws vertical line in extent view to
     *                                focus that time point across all resource nodes
     *
     * @param modifiers - <code>int</code> - 
     * @param dCoords - <code>Point</code> - 
     * @param viewCoords - <code>Point</code> - 
     */
    public final void doBackgroundClick( final int modifiers, final Point dCoords,
                                         final Point viewCoords) {
      PwResource resource = findNearestResource( dCoords);
      // System.err.println( "doBackgroundClick: resource " + resource.getName());
      if (MouseEventOSX.isMouseLeftClick( modifiers, PlanWorks.isMacOSX())) {
        // do nothing
      } else if (MouseEventOSX.isMouseRightClick( modifiers, PlanWorks.isMacOSX())) {
        ResourceTransactionView.docCoords = dCoords;
        mouseRightPopupMenu( resource, viewCoords);
      }
    } // end doBackgroundClick


  } // end class ExtentView

  private PwResource findNearestResource( final Point dCoords) {
    int docY = (int) dCoords.getY();
    PwResource resourceCandidate = null;
    Iterator reourceTransSetItr = resourceTransactionSetList.iterator();
    while (reourceTransSetItr.hasNext()) {
      ResourceTransactionSet resourceTransSet =
        (ResourceTransactionSet) reourceTransSetItr.next();
      if (docY >= resourceTransSet.getTransactionSetYOrigin()) {
        resourceCandidate = resourceTransSet.getResource();
      } else {
        break;
      }
    }
    return resourceCandidate;
  } // end findNearestResource


  private void mouseRightPopupMenu( final PwResource resource, final Point viewCoords) {
    String partialPlanName = partialPlan.getPartialPlanName();
    PwPlanningSequence planSequence = PlanWorks.getPlanWorks().getPlanSequence( partialPlan);
    JPopupMenu mouseRightPopup = new JPopupMenu();

//     JMenuItem nodeByKeyItem = new JMenuItem( "Find by Key");
//     createNodeByKeyItem( nodeByKeyItem);
//     mouseRightPopup.add( nodeByKeyItem);

    createOpenViewItems( partialPlan, partialPlanName, planSequence, mouseRightPopup,
                         PlanWorks.RESOURCE_TRANSACTION_VIEW);

    JMenuItem overviewWindowItem = new JMenuItem( "Overview Window");
    createOverviewWindowItem( overviewWindowItem, this, viewCoords);
    mouseRightPopup.add( overviewWindowItem);

    JMenuItem raiseContentSpecItem = new JMenuItem( "Raise Content Filter");
    createRaiseContentSpecItem( raiseContentSpecItem);
    mouseRightPopup.add( raiseContentSpecItem);
    
    String timeMarkTitle = "Set Time Scale Line";
    if (doesViewFrameExist( PlanWorks.RESOURCE_TRANSACTION_VIEW)) {
      timeMarkTitle = timeMarkTitle.concat( "/Snap to Resource Profile");
    }
    JMenuItem timeMarkItem = new JMenuItem( timeMarkTitle);
    createTimeMarkItem( timeMarkItem, resource);
    mouseRightPopup.add( timeMarkItem);

//     JMenuItem activeResourceItem = new JMenuItem( "Snap to Active Resource");
//     createActiveResourceItem( activeResourceItem);
//     mouseRightPopup.add( activeResourceItem);

    if (doesViewFrameExist( PlanWorks.NAVIGATOR_VIEW)) {
      mouseRightPopup.addSeparator();
      JMenuItem closeWindowsItem = new JMenuItem( "Close Navigator Views");
      createCloseNavigatorWindowsItem( closeWindowsItem);
      mouseRightPopup.add( closeWindowsItem);
    }
    createAllViewItems( partialPlan, partialPlanName, planSequence, mouseRightPopup);

    NodeGenerics.showPopupMenu( mouseRightPopup, this, viewCoords);
  } // end mouseRightPopupMenu


//   private void createActiveResourceItem( final JMenuItem activeResourceItem) {
//     activeResourceItem.addActionListener( new ActionListener() {
//         public final void actionPerformed( final ActionEvent evt) {
//           PwResource activeResource =
//             ((PartialPlanViewSet) ResourceTransactionView.this.getViewSet()).getActiveResource();
//           if (activeResource != null) {
//             findAndSelectResource( activeResource);
//           }
//         }
//       });
//   } // end createActiveResourceItem


//   private void createNodeByKeyItem( final JMenuItem nodeByKeyItem) {
//     nodeByKeyItem.addActionListener( new ActionListener() {
//         public final void actionPerformed( final ActionEvent evt) {
//           AskNodeByKey nodeByKeyDialog =
//             new AskNodeByKey( "Find by Key", "key (int)", ResourceTransactionView.this);
//           Integer nodeKey = nodeByKeyDialog.getNodeKey();
//           if (nodeKey != null) {
//             // System.err.println( "createNodeByKeyItem: nodeKey " + nodeKey.toString());

//             PwResource resourceToFind = null;
//             // PwResource resourceToFind = partialPlan.getResource( nodeKey);
            
//             findAndSelectResource( resourceToFind);
//           }
//         }
//       });
//   } // end createNodeByKeyItem


  /**
   * <code>findAndSelectResource</code>
   *
   * @param resourceToFind - <code>PwResource</code> - 
   * @param xLoc - <code>int</code> - 
   */
  public final void findAndSelectResource( final PwResource resourceToFind,
                                           final int xLoc) {
    boolean isResourceFound = false;
    Iterator resourceSetListItr = resourceTransactionSetList.iterator();
    while (resourceSetListItr.hasNext()) {
      ResourceTransactionSet resourceTransactionSet =
        (ResourceTransactionSet) resourceSetListItr.next();
//       System.err.println( "resourceToFind id = " + resourceToFind.getId() +
//                           " resource id = " + resourceTransactionSet.getResource().getId());
      if (resourceTransactionSet.getResource().getId().equals( resourceToFind.getId())) {
        System.err.println( "ResourceTransactionView found resource: " +
                            resourceToFind.getName() + 
                            " (key=" + resourceToFind.getId().toString() + ")");
        isResourceFound = true;

        jGoExtentView.getHorizontalScrollBar().
          setValue( Math.max( 0, (int) (xLoc - (jGoExtentView.getExtentSize().getWidth() / 2))));
        jGoExtentView.getVerticalScrollBar().
          setValue( Math.max( 0, (int) (findResourceYLoc( resourceToFind) -
                                   (jGoExtentView.getExtentSize().getHeight() / 2))));
      }
    }
    if (! isResourceFound) {
      // Content Spec filtering may cause this to happen
      String message = "Resource '" +  resourceToFind.getName() +
        "' (key=" + resourceToFind.getId().toString() + ") not found.";
      JOptionPane.showMessageDialog( PlanWorks.getPlanWorks(), message,
                                     "Resource Not Found in ResourceTransactionView",
                                     JOptionPane.ERROR_MESSAGE);
      System.err.println( message);
    }
  } // end findAndSelectResource


  private int findResourceYLoc( final PwResource resourceToFind) {
    int yLoc = 0;
    Iterator resourceTransSetItr = resourceTransactionSetList.iterator();
    while (resourceTransSetItr.hasNext()) {
      ResourceTransactionSet resourceTransSet =
        (ResourceTransactionSet) resourceTransSetItr.next();
      if (resourceToFind.getId().equals( resourceTransSet.getResource().getId())) {
        return resourceTransSet.getTransactionSetYOrigin();
      }
    }
    return yLoc;
  } // end findResourceYLoc


  private void createTimeMarkItem( final JMenuItem timeMarkItem,
                                   final  PwResource resourceToFind) {
    timeMarkItem.addActionListener( new ActionListener() {
        public final void actionPerformed( final ActionEvent evt) {
          int xLoc = (int) ResourceTransactionView.docCoords.getX();
//           System.err.println( "doMouseClick: xLoc " + xLoc + " time " +
//                               jGoRulerView.scaleXLoc( xLoc));
          createTimeMark( xLoc);

          // draw mark in ResourceProfileView & scroll to same resource
          if (doesViewFrameExist( PlanWorks.RESOURCE_PROFILE_VIEW)) {
            MDIInternalFrame resourceProfileFrame =
              viewSet.openView( PlanWorks. getViewClassName
                                ( PlanWorks.RESOURCE_PROFILE_VIEW));
            ResourceProfileView resourceProfileView = null;
            Container contentPane = resourceProfileFrame.getContentPane();
            for (int i = 0, n = contentPane.getComponentCount(); i < n; i++) {
              // System.err.println( "i " + i + " " +
              //                    contentPane.getComponent( i).getClass().getName());
              if (contentPane.getComponent( i) instanceof ResourceProfileView) {
                resourceProfileView =
                  (ResourceProfileView) contentPane.getComponent( i);
              }
            }
            if (resourceProfileView != null) {
              resourceProfileView.createTimeMark( xLoc);
              // scroll ResourceProfileView to resourceToFind and timeMark
              resourceProfileView.findAndSelectResource( resourceToFind, xLoc);
            }
          }
        }
      });
  } // end createTimeMarkItem


  private void createOverviewWindowItem( final JMenuItem overviewWindowItem,
                                         final ResourceTransactionView resourceTransactionView,
                                         final Point viewCoords) {
    overviewWindowItem.addActionListener( new ActionListener() { 
        public final void actionPerformed( final ActionEvent evt) {
          VizViewOverview currentOverview =
            ViewGenerics.openOverviewFrame( PlanWorks.RESOURCE_TRANSACTION_VIEW, partialPlan,
                                            resourceTransactionView, viewSet, jGoExtentView,
                                            viewCoords);
          if (currentOverview != null) {
            overview = currentOverview;
          }
        }
      });
  } // end createOverviewWindowItem

  /**
   * <code>createTimeMark</code> -- allow ResourceProfile to set same time mark here
   *
   * @param xLoc - <code>int</code> - 
   */
  public void createTimeMark( int xLoc) {
    if (timeScaleMark != null) {
      jGoExtentView.getDocument().removeObject( timeScaleMark);
      // jGoExtentView.validate();
    } 
    timeScaleMark = new TimeScaleMark( xLoc);
    timeScaleMark.addPoint( xLoc, startYLoc);
    timeScaleMark.addPoint( xLoc, currentYLoc + 2);
    jGoExtentView.getDocument().addObjectAtTail( timeScaleMark);
  } // end createTimeMark

  /**
   * <code>TimeScaleMark</code> - color the mark and provide its time value
   *                              as a tool tip.
   *
   */
  class TimeScaleMark extends JGoStroke {

    private int xLoc;

    /**
     * <code>TimeScaleMark</code> - constructor 
     *
     * @param xLoc - <code>int</code> - 
     */
    public TimeScaleMark( final int xLocation) {
      super();
      this.xLoc = xLocation;
      setDraggable( false);
      setResizable( false);
      setPen( new JGoPen( JGoPen.SOLID, 1,  ColorMap.getColor( "red")));
    }

    public final String getToolTipText() {
      return String.valueOf( jGoRulerView.scaleXLoc( xLoc) + 1);
    }

  } // end class TimeScaleMark


    

} // end class ResourceTransactionView
 



