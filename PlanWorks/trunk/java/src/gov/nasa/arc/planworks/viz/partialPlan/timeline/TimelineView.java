// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: TimelineView.java,v 1.54 2004-05-13 20:24:13 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 18May03
//

package gov.nasa.arc.planworks.viz.partialPlan.timeline;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import javax.swing.BoxLayout;
import javax.swing.JInternalFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoArea;
import com.nwoods.jgo.JGoDocument;
import com.nwoods.jgo.JGoListPosition;
import com.nwoods.jgo.JGoObject;
import com.nwoods.jgo.JGoView;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.DbConstants;
import gov.nasa.arc.planworks.db.PwDomain;
import gov.nasa.arc.planworks.db.PwObject;
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.PwPlanningSequence;
import gov.nasa.arc.planworks.db.PwSlot;
import gov.nasa.arc.planworks.db.PwTimeline;
import gov.nasa.arc.planworks.db.PwToken;
import gov.nasa.arc.planworks.mdi.MDIInternalFrame;
import gov.nasa.arc.planworks.util.ColorMap;
import gov.nasa.arc.planworks.util.MouseEventOSX;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.ViewGenerics;
import gov.nasa.arc.planworks.viz.ViewListener;
import gov.nasa.arc.planworks.viz.VizViewOverview;
import gov.nasa.arc.planworks.viz.nodes.NodeGenerics;
import gov.nasa.arc.planworks.viz.nodes.TokenNode;
import gov.nasa.arc.planworks.viz.partialPlan.AskNodeByKey;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanView;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanViewSet;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanViewState;
import gov.nasa.arc.planworks.viz.viewMgr.ViewableObject;
import gov.nasa.arc.planworks.viz.viewMgr.ViewSet;


/**
 * <code>TimelineView</code> - render a partial plan's timelines and slots
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                  NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class TimelineView extends PartialPlanView {

  private static final double LABEL_MIN_LEN_FIRST_SLOT_FACTOR = 1.25;

  private PwPartialPlan partialPlan;
  private long startTimeMSecs;
  private ViewSet viewSet;
  private TimelineJGoView jGoView;
  private JGoDocument jGoDocument;
  private List timelineNodeList; // element TimelineNode
  private List freeTokenNodeList; // element TokenNode
  private int slotLabelMinLength;
  private JGoArea mouseOverNode;
  private boolean isAutoSnapEnabled;
  private boolean isStepButtonView;

  /**
   * <code>TimelineView</code> - constructor - 
   *                             Use SwingUtilities.invokeLater( runInit) to
   *                             properly render the JGo widgets
   *
   * @param partialPlan - <code>ViewableObject</code> - 
   * @param viewSet - <code>ViewSet</code> - 
   */
  public TimelineView( ViewableObject partialPlan,  ViewSet viewSet) {
    super( (PwPartialPlan) partialPlan, (PartialPlanViewSet) viewSet);
    timelineViewInit( (PwPartialPlan) partialPlan, viewSet);
    isStepButtonView = false;
    SwingUtilities.invokeLater( runInit);
  } // end constructor

  /**
   * <code>TimelineView</code> - constructor 
   *
   * @param partialPlan - <code>ViewableObject</code> - 
   * @param viewSet - <code>ViewSet</code> - 
   * @param s - <code>PartialPlanViewState</code> - 
   */
  public TimelineView(ViewableObject partialPlan, ViewSet viewSet,
                      PartialPlanViewState s) {
    super( (PwPartialPlan) partialPlan, (PartialPlanViewSet) viewSet);
    timelineViewInit( (PwPartialPlan) partialPlan, viewSet);
    isStepButtonView = true;
    setState(s);
    SwingUtilities.invokeLater( runInit);
  }

  /**
   * <code>TimelineView</code> - constructor 
   *
   * @param partialPlan - <code>ViewableObject</code> - 
   * @param viewSet - <code>ViewSet</code> - 
   * @param viewListener - <code>ViewListener</code> - 
   */
  public TimelineView( ViewableObject partialPlan, ViewSet viewSet,
                       ViewListener viewListener) {
    super( (PwPartialPlan) partialPlan, (PartialPlanViewSet) viewSet);
    timelineViewInit( (PwPartialPlan) partialPlan, viewSet);
    isStepButtonView = false;
    if (viewListener != null) {
      addViewListener( viewListener);
    }
    SwingUtilities.invokeLater( runInit);
  }

  private void timelineViewInit(ViewableObject partialPlan, ViewSet viewSet) {
    this.partialPlan = (PwPartialPlan) partialPlan;
    this.viewSet = (PartialPlanViewSet) viewSet;
    ViewListener viewListener = null;
    viewFrame = viewSet.openView( this.getClass().getName(), viewListener);
    // for PWTestHelper.findComponentByName
    this.setName( viewFrame.getTitle());

    setLayout( new BoxLayout( this, BoxLayout.Y_AXIS));
    slotLabelMinLength = ViewConstants.TIMELINE_VIEW_EMPTY_NODE_LABEL_LEN;
    mouseOverNode = null;
    isAutoSnapEnabled = false;

    jGoView = new TimelineJGoView();
    jGoView.addViewListener( createViewListener());
    jGoView.setBackground( ViewConstants.VIEW_BACKGROUND_COLOR);
    add( jGoView, BorderLayout.NORTH);
    jGoView.validate();
    jGoView.setVisible( true);
    this.setVisible( true);
  }

  public void setState( PartialPlanViewState s) {
    super.setState( s);
    if(s == null) {
      return;
    }
    zoomFactor = s.getCurrentZoomFactor();
    boolean isSetState = true;
    zoomView( jGoView, isSetState, this);
  } // end setState

  Runnable runInit = new Runnable() {
      public void run() {
        init();
      }
    };

  /**
   * <code>init</code> - wait for instance to become displayable, determine
   *                     appropriate font metrics, and render the JGo timeline,
   *                     and slot widgets
   *
   *    These functions are not done in the constructor to avoid:
   *    "Cannot measure text until a JGoView exists and is part of a visible window".
   *    called by componentShown method on the JFrame
   *    JGoView.setVisible( true) must be completed -- use runInit in constructor
   */
  public void init() {
    handleEvent(ViewListener.EVT_INIT_BEGUN_DRAWING);
    // wait for TimelineView instance to become displayable
		if(!displayableWait()) {
			return;
		}

    this.computeFontMetrics( this);

    jGoDocument = jGoView.getDocument();
    jGoDocument.addDocumentListener( createDocListener());

    // create all nodes
    boolean isValid = renderTimelineAndSlotNodes();
    if (isValid) {
      if (! isStepButtonView) {
        expandViewFrame( viewFrame, (int) jGoView.getDocumentSize().getWidth(),
                         (int) jGoView.getDocumentSize().getHeight());
      }
      // print out info for created nodes
      // iterateOverJGoDocument(); // slower - many more nodes to go thru
      // iterateOverNodes();

      addStepButtons( jGoView);
      if (! isStepButtonView) {
        expandViewFrameForStepButtons( viewFrame, jGoView);
      }

      long stopTimeMSecs = System.currentTimeMillis();
      System.err.println( "   ... " + ViewConstants.TIMELINE_VIEW + " elapsed time: " +
                          (stopTimeMSecs -
                           PlanWorks.getPlanWorks().getViewRenderingStartTime
                           ( ViewConstants.TIMELINE_VIEW)) + " msecs.");
      startTimeMSecs = 0L;
    } else {
      try {
        ViewListener viewListener = null;
        viewSet.openView( this.getClass().getName(), viewListener).setClosed( true);
      } catch (PropertyVetoException excp) {
      }
    }

    handleEvent(ViewListener.EVT_INIT_ENDED_DRAWING);
  } // end init


  /**
   * <code>redraw</code> - called by Content Spec to apply user's content spec request.
   *                       setVisible(true | false)
   *                       according to the Content Spec enabled ids
   *
   */
  public void redraw() {
    Thread thread = new RedrawViewThread();
    thread.setPriority(Thread.MIN_PRIORITY);
    thread.start();
  }

  class RedrawViewThread extends Thread {

    public RedrawViewThread() {
    }  // end constructor

    public void run() {
      handleEvent(ViewListener.EVT_REDRAW_BEGUN_DRAWING);
      System.err.println( "Redrawing Constraint Network View ...");
      if (startTimeMSecs == 0L) {
        startTimeMSecs = System.currentTimeMillis();
      }
      try {
        ViewGenerics.setRedrawCursor( viewFrame);

        renderTimelineAndSlotNodes();
        addStepButtons( jGoView);
        // causes bottom view edge to creep off screen
        //       if (! isStepButtonView) {
        //         expandViewFrameForStepButtons( viewFrame, jGoView);
        //       }
      } finally {
        ViewGenerics.resetRedrawCursor( viewFrame);
      }
      long stopTimeMSecs = System.currentTimeMillis();
      System.err.println( "   ... " + ViewConstants.TIMELINE_VIEW + " elapsed time: " +
                          (stopTimeMSecs - startTimeMSecs) + " msecs.");
      startTimeMSecs = 0L;
      handleEvent(ViewListener.EVT_REDRAW_ENDED_DRAWING);
    } //end run

  } // end class RedrawViewThread

  private boolean renderTimelineAndSlotNodes() {
    jGoView.getDocument().deleteContents();

    validTokenIds = viewSet.getValidIds();
    displayedTokenIds = new ArrayList();
    timelineNodeList = new ArrayList();
    freeTokenNodeList = new ArrayList();

    boolean isValid = createTimelineAndSlotNodes();
    if (isValid) {
      boolean showDialog = true;
      isContentSpecRendered( ViewConstants.TIMELINE_VIEW, showDialog);
    }
    return isValid;
  } // end createTemporalExtentView

  /**
   * <code>getJGoView</code> - 
   *
   * @return - <code>JGoView</code> - 
   */
  public JGoView getJGoView()  {
    return jGoView;
  }

  /**
   * <code>getJGoDocument</code>
   *
   * @return - <code>JGoDocument</code> - 
   */
  public JGoDocument getJGoDocument()  {
    return this.jGoDocument;
  }

  /**
   * <code>getTimelineNodeList</code>
   *
   * @return - <code>List</code> - of TimelineNode
   */
  public List getTimelineNodeList() {
    return timelineNodeList;
  }

  /**
   * <code>getFreeTokenNodeList</code>
   *
   * @return - <code>List</code> - 
   */
  public List getFreeTokenNodeList() {
    return freeTokenNodeList;
  }

  /**
   * <code>getMouseOverNode</code> - node over which the mouse is moving - SlotNode/TokenNode
   *
   * @return - <code>JGoArea</code> - 
   */
  public JGoArea getMouseOverNode() {
    return mouseOverNode;
  }

  /**
   * <code>setMouseOverNode</code>
   *
   * @param mouseOverNode - <code>JGoArea</code> - 
   */
  public void setMouseOverNode( JGoArea mouseOverNode) {
    this.mouseOverNode = mouseOverNode;
  }

  /**
   * <code>isAutoSnapEnabled</code>
   *
   * @return - <code>boolean</code> - 
   */
  public boolean isAutoSnapEnabled() {
    return isAutoSnapEnabled;
  }

  private boolean createTimelineAndSlotNodes() {
    boolean isValid = true;
    int x = ViewConstants.TIMELINE_VIEW_X_INIT;
    int y = ViewConstants.TIMELINE_VIEW_Y_INIT;
    List objectList = partialPlan.getObjectList();
    Iterator objectIterator = objectList.iterator();
    while (objectIterator.hasNext()) {
      PwObject object = (PwObject) objectIterator.next();
      if(object.getObjectType() == DbConstants.O_TIMELINE) {
        x = ViewConstants.TIMELINE_VIEW_X_INIT;
        PwTimeline timeline = (PwTimeline) object;
        String timelineName = timeline.getName();
        String parentName = null;
        if(timeline.getParent() != null) {
          parentName = timeline.getParent().getName();
        }
        String timelineLabel = "";
        if(parentName != null) {
          timelineLabel += parentName + " : ";
        }
        timelineLabel += timelineName + "\ntimeline key=" + timeline.getId().toString();
        Color timelineColor = getTimelineColor(timeline.getId());
        TimelineViewTimelineNode timelineNode =
          new TimelineViewTimelineNode(timelineLabel, timeline, new Point(x, y), timelineColor, 
                                       this);
        timelineNodeList.add(timelineNode);
        jGoDocument.addObjectAtTail(timelineNode);
        x += timelineNode.getSize().getWidth();
        isValid = createSlotNodes(timeline, timelineNode, x, y, timelineColor);
        if(!isValid) {
          return isValid;
        }
        y += ViewConstants.TIMELINE_VIEW_Y_DELTA;
      }
    }

    return createFreeTokenNodes( x, y, isValid);

  } // end createTimelineAndSlotNodes

  private boolean createFreeTokenNodes( int x, int y, boolean isValid) {
    y += ViewConstants.TIMELINE_VIEW_Y_INIT;
    List tokenList = partialPlan.getTokenList();
    Iterator tokenIterator = tokenList.iterator();
    boolean isFreeToken = true, isDraggable = false;
    PwSlot slot = null;
    Color backgroundColor = ColorMap.getColor( ViewConstants.FREE_TOKEN_BG_COLOR);
    while (tokenIterator.hasNext()) {
      PwToken token = (PwToken) tokenIterator.next();
      if (token.isSlotted()) {
        continue;
      } else if ((! token.isSlotted()) && (! token.isFree())) {
        // resourceTransactions - not displayed, put in displayedTokenIds
        isTokenInContentSpec( token);
        continue;
      } else { // free tokens
        if (isTokenInContentSpec( token))  {
          // increment by half the label width, since x is center, not left edge
          int divisor = 2;
          if (x == ViewConstants.TIMELINE_VIEW_X_INIT) { divisor = 1; }
          x = x +  Math.max( SwingUtilities.computeStringWidth
                             ( this.fontMetrics, token.getPredicateName()) / divisor,
                             SwingUtilities.computeStringWidth
                             ( this.fontMetrics, "key=" + token.getId().toString()) / divisor);
          TimelineTokenNode freeTokenNode =
            new TimelineTokenNode( token, slot, new Point( x, y), backgroundColor,
                                   isFreeToken, isDraggable, this);
          freeTokenNodeList.add( freeTokenNode);
          jGoDocument.addObjectAtTail( freeTokenNode);
          x = x + (int) freeTokenNode.getSize().getWidth();
        }
      }
    }
    return isValid;
  } // end createFreeTokenNodes

  private boolean createSlotNodes( PwTimeline timeline, TimelineViewTimelineNode timelineNode,
                                   int x, int y, Color backgroundColor) {
    boolean isValid = computeTimeIntervalLabelSize( timeline);
    if (! isValid) {
      return isValid;
    }
    List slotList = timeline.getSlotList();
    Iterator slotIterator = slotList.iterator();
    SlotNode previousSlotNode = null;
    SlotNode slotNode = null;
    boolean isFirstSlot = true;
    while (slotIterator.hasNext()) {
      PwSlot slot = (PwSlot) slotIterator.next();
      // overloaded tokens on slot - not displayed, put in displayedTokenIds
      List tokenList = slot.getTokenList();
//       for (int i = 1, n = tokenList.size(); i < n; i++) {
//         isTokenInContentSpec( (PwToken) tokenList.get( i));
//       }
      tokenList.remove(slot.getBaseToken());
      for(int i = 0; i < tokenList.size(); i++) {
        isTokenInContentSpec((PwToken) tokenList.get(i));
      }
      boolean isLastSlot = (! slotIterator.hasNext());
      PwToken token = slot.getBaseToken();
      if ((token == null) && (isFirstSlot || isLastSlot)) {
        // discard leading and trailing empty slots (planworks/test/data/emptySlots)
      } else {
        // check for empty slots - always show them
        if ((token == null) ||
            (token != null) && isTokenInContentSpec( token)) {
          String slotNodeLabel = getSlotNodeLabel( token, slot, isFirstSlot);
          slotNode = new SlotNode( slotNodeLabel, slot, timeline, new Point( x, y),
                                   previousSlotNode, isFirstSlot, isLastSlot,
                                   backgroundColor, this);
          timelineNode.addToSlotNodeList( slotNode);
          // System.err.println( "createTimelineAndSlotNodes: SlotNode x " + x + " y " + y);
          jGoDocument.addObjectAtTail( slotNode);
          previousSlotNode = slotNode;
          // x += slotNode.getSize().getWidth();
          // SlotNode code alters location due to Content Spec filtering
          // System.err.println( "old x " + (x + (int) slotNode.getSize().getWidth()));
          x = (int) (slotNode.getLocation().getX() + slotNode.getSize().getWidth()) +
            ViewConstants.TIMELINE_VIEW_INSET_SIZE - 2;
          // System.err.println( "new x " + x);
          isFirstSlot = false;
        }
      }
    }
    return isValid;
  } // end createSlotNodes


  // perform two integrity checks:
  //   1) two successive empty slots must not occur
  //   2) earliest start times must be monotonically increasing
  // while computing time interval max label size
  private boolean computeTimeIntervalLabelSize( PwTimeline timeline) {
    slotLabelMinLength = ViewConstants.TIMELINE_VIEW_EMPTY_NODE_LABEL_LEN;
    List slotList = timeline.getSlotList();
    ListIterator slotIterator = slotList.listIterator();
    PwSlot previousSlot = null;
    SlotNode slotNode = null;
    boolean foundEmptySlot = false;
    int earliestStartTime = DbConstants.MINUS_INFINITY_INT;
    while (slotIterator.hasNext()) {
      PwSlot slot = (PwSlot) slotIterator.next();
      PwToken token = slot.getBaseToken();
      if (token == null) {
        if (foundEmptySlot) {
          String message = "Two successive empty slots found in timeline '" +
            timeline.getName() + "' (id = " + timeline.getId() + ")";
          JOptionPane.showMessageDialog( PlanWorks.getPlanWorks(), message,
                                         "Timeline View Exception",
                                         JOptionPane.ERROR_MESSAGE);
          System.err.println( message);
          return false;
        } else {
          foundEmptySlot = true;
        }
      } else {
        foundEmptySlot = false;
      }
      boolean isLastSlot = (! slotIterator.hasNext());
      PwDomain startTimeIntervalDomain = slot.getStartTime();
      PwDomain endTimeIntervalDomain = slot.getEndTime();
      if ((endTimeIntervalDomain != null) &&
          ((endTimeIntervalDomain.toString().length() +
            ViewConstants.TIME_INTERVAL_STRINGS_OVERLAP_OFFSET) >
           slotLabelMinLength)) {
        slotLabelMinLength = endTimeIntervalDomain.toString().length() +
          ViewConstants.TIME_INTERVAL_STRINGS_OVERLAP_OFFSET;
      }
      if (startTimeIntervalDomain != null) {
        if (startTimeIntervalDomain.getLowerBoundInt() >= earliestStartTime) {
          earliestStartTime = startTimeIntervalDomain.getLowerBoundInt();
        } else {
          PwToken previousToken = previousSlot.getBaseToken();
          outputNonMonotonicError( slot, previousSlot, token, previousToken, timeline);
          return false;
        }
      }
      previousSlot = slot;
    }
    return true;
  } // end computeTimeIntervalLabelSize

  private void outputNonMonotonicError( PwSlot slot, PwSlot previousSlot, PwToken token,
                                        PwToken previousToken, PwTimeline timeline) {
    String previousTokenIdString = "", tokenIdString = "";
    String previousPredicateName = "-empty-", predicateName = "-empty-";
    // check for empty slots
    if (previousToken != null) {
      previousTokenIdString = previousToken.getId().toString();
      previousPredicateName = previousToken.getPredicateName();
    }
    if (token != null) {
      tokenIdString = token.getId().toString();
      predicateName = token.getPredicateName();
    }
    String message = "Earliest start times are not monotonically increasing " +
      "in timeline '" + timeline.getName() + "' (id = " + timeline.getId() + ")" +
      "\npreviousPredicate = '" + previousPredicateName +
      "', slotId = " + previousSlot.getId() + ", tokenId = " + previousTokenIdString + 
      ", start = " + previousSlot.getStartTime() + ", end = " + previousSlot.getEndTime() +
      "\npredicate = '" + predicateName + "', slotId = " + slot.getId() +
      ", tokenId = " + tokenIdString + ", start = " + slot.getStartTime() + ", end = " + 
      slot.getEndTime();
    JOptionPane.showMessageDialog( PlanWorks.getPlanWorks(), message,
                                   "Timeline View Exception",
                                   JOptionPane.ERROR_MESSAGE);
    System.err.println( message);
  } // end outputNonMonotonicError

  // make all timeline nodes the same width
  private int computeTimelineNodesWidth( List timelineList, String objectName) {
    int maxNodeWidth = 0;
    Iterator timelineIterator = timelineList.iterator();
    while (timelineIterator.hasNext()) {
      PwTimeline timeline = (PwTimeline) timelineIterator.next();
      String timelineName = timeline.getName();
      String timelineNodeName = objectName + " : " + timelineName;
      String timelineKey = "timeline key=" + timeline.getId().toString();
      int nodeWidth = Math.max( SwingUtilities.computeStringWidth( this.fontMetrics,
                                                                   timelineNodeName),
                                SwingUtilities.computeStringWidth( this.fontMetrics,
                                                                   timelineKey));
      if (nodeWidth > maxNodeWidth) {
        maxNodeWidth = nodeWidth;
      }
    }
    return maxNodeWidth + 2 *  ViewConstants.TIMELINE_VIEW_INSET_SIZE;
  } // end computeTimelineNodesWidth


  /**
   * <code>getSlotNodeLabel</code> - pad slot label with blanks so that time
   *                       interval lables underneath, do not overlap with
   *                       adjacent slots
   *
   * @param token - <code>PwToken</code> - 
   * @param slot - <code>PwSlot</code> - 
   * @param  isFirstSlot - <code>boolean</code> -  
   * @return - <code>String</code> - 
   */
  public String getSlotNodeLabel( PwToken token, PwSlot slot, boolean isFirstSlot) {
    StringBuffer label = null;
    String keyValue = "\nslot key=" + slot.getId().toString();
    if (token == null) { // empty slot
      label = new StringBuffer( ViewConstants.TIMELINE_VIEW_EMPTY_NODE_LABEL);
    } else {
      label = new StringBuffer( token.getPredicateName());
      label.append( " (").append( String.valueOf( slot.getTokenList().size()));
      label.append( ")");
    }
    int labelMinLength = slotLabelMinLength;
    PwDomain startTimeIntervalDomain = slot.getStartTime();
    if ((! startTimeIntervalDomain.getLowerBound().equals
         ( startTimeIntervalDomain.getUpperBound())) && isFirstSlot) {
      // start interval is [1234 1235], rather than {1234},
      // first slot has left alignment to left edge of slot
      labelMinLength = (int) (labelMinLength * LABEL_MIN_LEN_FIRST_SLOT_FACTOR);
    }
    int nodeLength = Math.max( label.length(), keyValue.length());
//     System.err.println( "getSlotNodeLabel nodeLength " + nodeLength +
//                         " labelMinLength " + labelMinLength);
//     System.err.println( "getSlotNodeLabel label B '" + label + "'");
    if (nodeLength < labelMinLength) { 
      boolean prepend = true;
      for (int i = 0, n = labelMinLength - nodeLength; i < n; i++) {
        if (prepend) {
          label.insert( 0, " ");
        } else {
          label.append( " ");
        }
        prepend = (! prepend);
      }
    }
//     System.err.println( "getSlotNodeLabel label A '" + label + "'");
    return label.toString() + keyValue;
  } // end getSlotNodeLabel

  private void iterateOverNodes() {
    int numTimelineNodes = timelineNodeList.size();
    //System.err.println( "iterateOverNodes: numTimelineNodes " + numTimelineNodes);
    Iterator timelineIterator = timelineNodeList.iterator();
    while (timelineIterator.hasNext()) {
      TimelineViewTimelineNode timelineNode = (TimelineViewTimelineNode) timelineIterator.next();
      //System.err.println( "name '" + timelineNode.getTimelineName() + "' location " +
      //                    timelineNode.getLocation());
      int numSlotNodes = timelineNode.getSlotNodeList().size();
      //System.err.println( "numSlotNodes " + numSlotNodes); 
      Iterator slotIterator = timelineNode.getSlotNodeList().iterator();
      while (slotIterator.hasNext()) {
        SlotNode slotNode = (SlotNode) slotIterator.next();
        //System.err.println( "name '" + slotNode.getPredicateName() + "' location " +
        //                    slotNode.getLocation());
        //System.err.println( "startInterval " + slotNode.getStartTimeIntervalString());
        if (! slotIterator.hasNext()) {
          //System.err.println( "endInterval " + slotNode.getEndTimeIntervalString());
        }
      }
    }
  } // end iterateOverNodes


  private void iterateOverJGoDocument() {
    JGoListPosition position = jGoDocument.getFirstObjectPos();
    int cnt = 0;
    while (position != null) {
      JGoObject object = jGoDocument.getObjectAtPos( position);
      position = jGoDocument.getNextObjectPosAtTop( position);
      //System.err.println( "iterateOverJGoDoc: position " + position +
      //                    " className " + object.getClass().getName());
      if (object instanceof SlotNode) {
        SlotNode slotNode = (SlotNode) object;

      }
      cnt += 1;
//       if (cnt > 100) {
//         break;
//       }
    }
    //System.err.println( "iterateOverJGoDoc: cnt " + cnt);
  } // end iterateOverJGoDocument


  /**
   * <code>TimelineJGoView</code> - subclass JGoView to add doBackgroundClick
   *
   */
  class TimelineJGoView extends JGoView {

    /**
     * <code>TimelineJGoView</code> - constructor 
     *
     */
    public TimelineJGoView() {
      super();
    }

    /**
     * <code>doBackgroundClick</code> - Mouse-Right pops up menu:
     *                                 1) snap to active token
     *
     * @param modifiers - <code>int</code> - 
     * @param docCoords - <code>Point</code> - 
     * @param viewCoords - <code>Point</code> - 
     */
    public void doBackgroundClick( int modifiers, Point docCoords, Point viewCoords) {
      if (MouseEventOSX.isMouseLeftClick( modifiers, PlanWorks.isMacOSX())) {
        // do nothing
      } else if (MouseEventOSX.isMouseRightClick( modifiers, PlanWorks.isMacOSX())) {
        mouseRightPopupMenu( viewCoords);
      }
    } // end doBackgroundClick

  } // end class TimelineJGoView


  private void mouseRightPopupMenu( Point viewCoords) {
    String partialPlanName = partialPlan.getPartialPlanName();
    PwPlanningSequence planSequence = PlanWorks.getPlanWorks().getPlanSequence( partialPlan);
    JPopupMenu mouseRightPopup = new JPopupMenu();

    String className = PlanWorks.getViewClassName( ViewConstants.TEMPORAL_EXTENT_VIEW);
    if (viewSet.viewExists( className)) {
      if (! isAutoSnapEnabled) {
        JMenuItem enableAutoSnapItem = new JMenuItem( "Enable Auto Snap");
        createEnableAutoSnapItem( enableAutoSnapItem);
        mouseRightPopup.add( enableAutoSnapItem);
      } else {
        JMenuItem disableAutoSnapItem = new JMenuItem( "Disable Auto Snap");
        createDisableAutoSnapItem( disableAutoSnapItem);
        mouseRightPopup.add( disableAutoSnapItem);
      }
      mouseRightPopup.addSeparator();
    }

    JMenuItem nodeByKeyItem = new JMenuItem( "Find by Key");
    createNodeByKeyItem( nodeByKeyItem);
    mouseRightPopup.add( nodeByKeyItem);

    createOpenViewItems( partialPlan, partialPlanName, planSequence, mouseRightPopup,
                         ViewConstants.TIMELINE_VIEW);

    JMenuItem overviewWindowItem = new JMenuItem( "Overview Window");
    createOverviewWindowItem( overviewWindowItem, this, viewCoords);
    mouseRightPopup.add( overviewWindowItem);

    JMenuItem raiseContentSpecItem = new JMenuItem( "Raise Content Filter");
    createRaiseContentSpecItem( raiseContentSpecItem);
    mouseRightPopup.add( raiseContentSpecItem);
    
    if (((PartialPlanViewSet) this.getViewSet()).getActiveToken() != null) {
      JMenuItem activeTokenItem = new JMenuItem( "Snap to Active Token");
      createActiveTokenItem( activeTokenItem);
      mouseRightPopup.add( activeTokenItem);
    }

    this.createZoomItem( jGoView, zoomFactor, mouseRightPopup, this);

    if (doesViewFrameExist( ViewConstants.NAVIGATOR_VIEW)) {
      mouseRightPopup.addSeparator();
      JMenuItem closeWindowsItem = new JMenuItem( "Close Navigator Views");
      createCloseNavigatorWindowsItem( closeWindowsItem);
      mouseRightPopup.add( closeWindowsItem);
    }
    createAllViewItems( partialPlan, partialPlanName, planSequence, mouseRightPopup);

    NodeGenerics.showPopupMenu( mouseRightPopup, this, viewCoords);
  } // end mouseRightPopupMenu

  private void createEnableAutoSnapItem( JMenuItem enableAutoSnapItem) {
    enableAutoSnapItem.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent evt) {
          isAutoSnapEnabled = true;
        }
      });
  } // end createNodeByKeyItem

  private void createDisableAutoSnapItem( JMenuItem disableAutoSnapItem) {
    disableAutoSnapItem.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent evt) {
          isAutoSnapEnabled = false;
        }
      });
  } // end createNodeByKeyItem

  private void createNodeByKeyItem( JMenuItem nodeByKeyItem) {
    nodeByKeyItem.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent evt) {
          AskNodeByKey nodeByKeyDialog =
            new AskNodeByKey( "Find by Key", "key (int)", TimelineView.this);
          Integer nodeKey = nodeByKeyDialog.getNodeKey();
          if (nodeKey != null) {
            // System.err.println( "createNodeByKeyItem: nodeKey " + nodeKey.toString());
            PwToken tokenToFind = partialPlan.getToken( nodeKey);
            if (tokenToFind != null) {
              boolean isByKey = true;
              findAndSelectToken( tokenToFind, isByKey);
            } else {
              PwSlot slotToFind = partialPlan.getSlot( nodeKey);
              if (slotToFind != null) {
                findAndSelectSlot( slotToFind);
              }
            }
          }
        }
      });
  } // end createNodeByKeyItem

  private void createActiveTokenItem( JMenuItem activeTokenItem) {
    activeTokenItem.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent evt) {
          PwToken activeToken =
            ((PartialPlanViewSet) TimelineView.this.getViewSet()).getActiveToken();
          if (activeToken != null) {
            boolean isByKey = false;
            findAndSelectToken( activeToken, isByKey);
          }
        }
      });
  } // end createActiveTokenItem

  private void findAndSelectToken( PwToken tokenToFind, boolean isByKey) {
    boolean isTokenFound = false, isHighlightNode = true;
    Iterator timelineNodeListItr = timelineNodeList.iterator();
    foundIt:
    while (timelineNodeListItr.hasNext()) {
      TimelineViewTimelineNode timelineNode = (TimelineViewTimelineNode) timelineNodeListItr.next();
      Iterator slotNodeListItr = timelineNode.getSlotNodeList().iterator();
      while (slotNodeListItr.hasNext()) {
        SlotNode slotNode = (SlotNode) slotNodeListItr.next();
        List tokenList = slotNode.getSlot().getTokenList();
        if (tokenList == null) { // empty slot
          continue;
        }
        Iterator tokenItr = tokenList.iterator();
        while (tokenItr.hasNext()) {
          PwToken token = (PwToken) tokenItr.next();
          if (token.getId().equals( tokenToFind.getId())) {
            System.err.println( "TimelineView found token: " +
                                tokenToFind.getPredicateName() +
                                " (key=" + tokenToFind.getId().toString() + ")");
            NodeGenerics.focusViewOnNode( slotNode, isHighlightNode, jGoView);
            // secondary nodes do not apply here
            isTokenFound = true;
            break foundIt;
          }
        }
      }
    }
    if (! isTokenFound) {
      Iterator freeTokenNodeItr = freeTokenNodeList.iterator();
      while (freeTokenNodeItr.hasNext()) {
        TokenNode freeTokenNode = (TokenNode) freeTokenNodeItr.next();
        if (freeTokenNode.getToken().getId().equals( tokenToFind.getId())) {
          System.err.println( "TimelineView found token: " +
                              tokenToFind.getPredicateName() +                   
                              " (key=" + tokenToFind.getId().toString() + ")");
          NodeGenerics.focusViewOnNode( freeTokenNode, isHighlightNode, jGoView);
          // secondary nodes do not apply here
          isTokenFound = true;
          break;
        }
      }
    }
    if (! isTokenFound) {
      // Content Spec filtering may cause this to happen
      String message = "Token " + tokenToFind.getPredicateName() +
        " (key=" + tokenToFind.getId().toString() + ") not found.";
      JOptionPane.showMessageDialog( PlanWorks.getPlanWorks(), message,
                                     "Token Not Found in TimelineView",
                                     JOptionPane.ERROR_MESSAGE);
      System.err.println( message);
    }
  } // end findAndSelectToken


  private void findAndSelectSlot( PwSlot slotToFind) {
    boolean isSlotFound = false;
    boolean isHighlightNode = true;
    Iterator timelineNodeListItr = timelineNodeList.iterator();
    foundIt:
    while (timelineNodeListItr.hasNext()) {
      TimelineViewTimelineNode timelineNode = (TimelineViewTimelineNode) timelineNodeListItr.next();
      Iterator slotNodeListItr = timelineNode.getSlotNodeList().iterator();
      while (slotNodeListItr.hasNext()) {
        SlotNode slotNode = (SlotNode) slotNodeListItr.next();
        if (slotNode.getSlot().getId().equals( slotToFind.getId())) {
          System.err.println( "TimelineView found slot: " +
                              slotNode.getPredicateName() +
                              " (key=" + slotToFind.getId().toString() + ")");
          NodeGenerics.focusViewOnNode( slotNode, isHighlightNode, jGoView);
          // secondary nodes do not apply here
          isSlotFound = true;
          break foundIt;
        }
      }
    }
    if (! isSlotFound) {
      // Content Spec filtering may cause this to happen
      String message = "Slot (key=" + slotToFind.getId().toString() + ") not found.";
      JOptionPane.showMessageDialog( PlanWorks.getPlanWorks(), message,
                                     "Slot Not Found in TimelineView",
                                     JOptionPane.ERROR_MESSAGE);
      System.err.println( message);
    }
  } // end findAndSelectSlot


  private void createOverviewWindowItem( JMenuItem overviewWindowItem,
                                         final TimelineView timelineView,
                                         final Point viewCoords) {
    overviewWindowItem.addActionListener( new ActionListener() { 
        public void actionPerformed( ActionEvent evt) {
          VizViewOverview currentOverview =
            ViewGenerics.openOverviewFrame( ViewConstants.TIMELINE_VIEW, partialPlan,
                                            timelineView, viewSet, jGoView, viewCoords);
          if (currentOverview != null) {
            overview = currentOverview;
          }
        }
      });
  } // end createOverviewWindowItem

} // end class TimelineView
 



