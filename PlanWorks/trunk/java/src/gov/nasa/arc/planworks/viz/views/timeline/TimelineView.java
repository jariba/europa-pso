// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: TimelineView.java,v 1.27 2003-08-06 01:20:15 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 18May03
//

package gov.nasa.arc.planworks.viz.views.timeline;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoDocument;
import com.nwoods.jgo.JGoListPosition;
import com.nwoods.jgo.JGoSelection;
import com.nwoods.jgo.JGoObject;
import com.nwoods.jgo.JGoText;
import com.nwoods.jgo.JGoView;


import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.PwObject;
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.PwSlot;
import gov.nasa.arc.planworks.db.PwTimeline;
import gov.nasa.arc.planworks.db.PwToken;
import gov.nasa.arc.planworks.util.ColorMap;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.nodes.SlotNode;
import gov.nasa.arc.planworks.viz.nodes.TimelineNode;
import gov.nasa.arc.planworks.viz.nodes.TokenNode;
import gov.nasa.arc.planworks.viz.viewMgr.ViewSet;
import gov.nasa.arc.planworks.viz.views.VizView;

/**
 * <code>TimelineView</code> - render a partial plan's timelines and slots
 *                JPanel->VizView->TimelineView
 *                JComponent->JGoView
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                  NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class TimelineView extends VizView {

  private PwPartialPlan partialPlan;
  private long startTimeMSecs;
  private ViewSet viewSet;
  private JGoView jGoView;
  private String viewName;
  private JGoDocument jGoDocument;
  private JGoSelection jGoSelection;
  // timelineNodeList & tmpTimelineNodeList used by JFCUnit test case
  private List timelineNodeList; // element TimelineNode
  private List freeTokenNodeList; // element TokenNode
  private List tmpTimelineNodeList; // element TimelineNode
  private Font font;
  private FontMetrics fontMetrics;
  private int slotLabelMinLength;
  private int maxViewWidth;
  private int maxViewHeight;

  /**
   * <code>TimelineView</code> - constructor - called by ViewSet.openTimelineView.
   *                             Use SwingUtilities.invokeLater( runInit) to
   *                             properly render the JGo widgets
   *
   * @param partialPlan - <code>PwPartialPlan</code> - 
   * @param startTimeMSecs - <code>long</code> - 
   * @param viewSet - <code>ViewSet</code> - 
   */
  public TimelineView( PwPartialPlan partialPlan, long startTimeMSecs, ViewSet viewSet) {
    super( partialPlan);

    this.partialPlan = partialPlan;
    this.startTimeMSecs = startTimeMSecs;
    this.viewSet = viewSet;
    viewName = "timelineView";
    this.timelineNodeList = new ArrayList();
    this.freeTokenNodeList = new ArrayList();
    this.tmpTimelineNodeList = new ArrayList();
    maxViewWidth = PlanWorks.INTERNAL_FRAME_WIDTH;
    maxViewHeight = PlanWorks.INTERNAL_FRAME_HEIGHT;

    setLayout( new BoxLayout( this, BoxLayout.Y_AXIS));
    slotLabelMinLength = ViewConstants.TIMELINE_VIEW_EMPTY_NODE_LABEL_LEN;

    jGoView = new JGoView();
    jGoSelection = new JGoSelection( jGoView);
    jGoView.setBackground( ColorMap.getColor( "lightGray"));
    add( jGoView, BorderLayout.NORTH);
    jGoView.validate();
    jGoView.setVisible( true);
    font = new Font( ViewConstants.TIMELINE_VIEW_FONT_NAME,
                     ViewConstants.TIMELINE_VIEW_FONT_STYLE,
                     ViewConstants.TIMELINE_VIEW_FONT_SIZE);
    jGoView.setFont( font);
    this.setVisible( true);

    SwingUtilities.invokeLater( runInit);
  } // end constructor

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
    // wait for TimelineView instance to become displayable
    while (! this.isDisplayable()) {
      try {
        Thread.currentThread().sleep(50);
      } catch (InterruptedException excp) {
      }
      // System.err.println( "timelineView displayable " + this.isDisplayable());
    }
    Graphics graphics = ((JPanel) this).getGraphics();
    fontMetrics = graphics.getFontMetrics( font);
    graphics.dispose();

    jGoDocument = jGoView.getDocument();

    // create all nodes
    createTimelineAndSlotNodes();
    // setVisible( true | false) depending on ContentSpec
    setNodesVisible();
    expandViewFrame( viewSet, viewName, maxViewWidth, maxViewHeight);

    // print out info for created nodes
    // iterateOverJGoDocument(); // slower - many more nodes to go thru
    // iterateOverNodes();

    long stopTimeMSecs = (new Date()).getTime();
    System.err.println( "   ... elapsed time: " +
                        (stopTimeMSecs - startTimeMSecs) + " msecs.");
  } // end init


  /**
   * <code>redraw</code> - called by Content Spec to apply user's content spec request.
   *                       setVisible(true | false)
   *                       according to the Content Spec enabled keys
   *
   */
  public void redraw() {
    // setVisible(true | false) depending on keys
    setNodesVisible();
    expandViewFrame( viewSet, viewName, maxViewWidth, maxViewHeight);
  } // end redraw

  /**
   * <code>getJGoDocument</code>
   *
   * @return - <code>JGoDocument</code> - 
   */
  public JGoDocument getJGoDocument()  {
    return this.jGoDocument;
  }

  /**
   * <code>getFontMetrics</code>
   *
   * @return - <code>FontMetrics</code> - 
   */
  public FontMetrics getFontMetrics()  {
    return fontMetrics;
  }

  /**
   * <code>getSlotLabelMinLength</code> - pad labels with blanks up to min size,
   *                           initially that of "empty" label, then base it on
   *                           length of time interval string.  This prevents
   *                           time interval strings of adjacent slots from
   *                           overlaying each other.
   *
   * @return - <code>int</code> - 
   */
  public int getSlotLabelMinLength() {
    return slotLabelMinLength;
  }

  /**
   * <code>setSlotLabelMinLength</code>
   *
   * @param minLength - <code>int</code> - 
   */
  public void setSlotLabelMinLength( int minLength) {
    this.slotLabelMinLength = minLength;
  }

  /**
   * <code>getTimelineNodeList</code>
   *
   * @return - <code>List</code> - of TimelineNode
   */
  public List getTimelineNodeList() {
    return timelineNodeList;
  }

  public List getFreeTokenNodeList() {
    return freeTokenNodeList;
  }
  private void createTimelineAndSlotNodes() {
    int x = ViewConstants.TIMELINE_VIEW_X_INIT;
    int y = ViewConstants.TIMELINE_VIEW_Y_INIT;
    List objectList = partialPlan.getObjectList();
    Iterator objectIterator = objectList.iterator();
    int objectCnt = 0;
    while (objectIterator.hasNext()) {
      PwObject object = (PwObject) objectIterator.next();
      String objectName = object.getName();
      List timelineList = object.getTimelineList();
      int timelineNodeWidth = computeTimelineNodesWidth( timelineList, objectName);
      Iterator timelineIterator = timelineList.iterator();
      while (timelineIterator.hasNext()) {
        x = ViewConstants.TIMELINE_VIEW_X_INIT;
        slotLabelMinLength = ViewConstants.TIMELINE_VIEW_EMPTY_NODE_LABEL_LEN;
        PwTimeline timeline = (PwTimeline) timelineIterator.next();
        String timelineName = timeline.getName();
        String timelineNodeName = objectName + " : " + timelineName;
        TimelineNode timelineNode =
          new TimelineNode( timelineNodeName, timeline, new Point( x, y),
                            objectCnt, this);
        tmpTimelineNodeList.add( timelineNode);
        // System.err.println( "createTimelineAndSlotNodes: TimelineNode x " + x + " y " + y);
        jGoDocument.addObjectAtTail( timelineNode);
        timelineNode.setSize( timelineNodeWidth,
                              (int) timelineNode.getSize().getHeight());
        x += timelineNode.getSize().getWidth();

        createSlotNodes( timeline, timelineNode, x, y, objectCnt);

        y += ViewConstants.TIMELINE_VIEW_Y_DELTA;
      }
      objectCnt += 1;
    }
    // free tokens
    y += ViewConstants.TIMELINE_VIEW_Y_INIT;
    List freeTokenList = partialPlan.getFreeTokenList();
    // System.err.println( "freeTokenList " + freeTokenList);
    Iterator freeTokenItr = freeTokenList.iterator();
    boolean isFreeToken = true, isDraggable = false;
    objectCnt = -1;
    while (freeTokenItr.hasNext()) {
      TokenNode freeTokenNode = new TokenNode( (PwToken) freeTokenItr.next(),
                                               new Point( x, y), objectCnt,
                                               isFreeToken, isDraggable,
                                               viewName, this);
      freeTokenNodeList.add( freeTokenNode);
      jGoDocument.addObjectAtTail( freeTokenNode);
      x += freeTokenNode.getSize().getWidth() + ViewConstants.TIMELINE_VIEW_X_INIT;
    }
    timelineNodeList = tmpTimelineNodeList;
  } // end createTimelineAndSlotNodes

  private void createSlotNodes( PwTimeline timeline, TimelineNode timelineNode,
                                int x, int y, int objectCnt) {
    List slotList = timeline.getSlotList();
    Iterator slotIterator = slotList.iterator();
    PwToken previousToken = null;
    SlotNode slotNode = null;
    while (slotIterator.hasNext()) {
      PwSlot slot = (PwSlot) slotIterator.next();
      PwToken token = null;
      if (slot.getTokenList().size() > 0) {
        token = (PwToken) slot.getTokenList().get( 0);
      }
      String slotNodeLabel = getSlotNodeLabel( token);
      boolean isLastSlot = (! slotIterator.hasNext());
      slotNode = new SlotNode( slotNodeLabel, slot, new Point( x, y), previousToken,
                               isLastSlot, objectCnt, this);
      timelineNode.addToSlotNodeList( slotNode);
      // System.err.println( "createTimelineAndSlotNodes: SlotNode x " + x + " y " + y);
      jGoDocument.addObjectAtTail( slotNode);
      previousToken = token;
      x += slotNode.getSize().getWidth();
    }
    int maxHeight = y + ViewConstants.TIMELINE_VIEW_Y_INIT;
    if (maxHeight > maxViewHeight) {
      maxViewHeight = maxHeight;
    }
    JGoText endIntervalTextObject = slotNode.getEndTimeIntervalObject();
    if (endIntervalTextObject != null) {
      int maxWidth = (int) endIntervalTextObject.getLocation().getX() +
        (int) endIntervalTextObject.getSize().getWidth() +
        ViewConstants.TIMELINE_VIEW_X_INIT;
      if (maxWidth > maxViewWidth) {
        maxViewWidth = maxWidth;
      }
    }
  } // end createSlotNodes


  // make all timeline nodes the same width
  private int computeTimelineNodesWidth( List timelineList, String objectName) {
    int maxNodeWidth = 0;
    Iterator timelineIterator = timelineList.iterator();
    while (timelineIterator.hasNext()) {
      PwTimeline timeline = (PwTimeline) timelineIterator.next();
      String timelineName = timeline.getName();
      String timelineNodeName = objectName + " : " + timelineName;
      int nodeWidth = SwingUtilities.computeStringWidth( this.getFontMetrics(),
                                                         timelineNodeName);
      if (nodeWidth > maxNodeWidth) {
        maxNodeWidth = nodeWidth;
      }
    }
    return maxNodeWidth + 2 *  ViewConstants.TIMELINE_VIEW_INSET_SIZE;
  } // end computeTimelineNodesWidth


  // pad labels with blanks up to min size -- initally that of "empty" label
  // then base it on  length of time interval string
  private String getSlotNodeLabel( PwToken token) {
    String predicateName = ViewConstants.TIMELINE_VIEW_EMPTY_NODE_LABEL;
    // check for non-empty slot
    if (token != null) {
      predicateName = token.getPredicate().getName();
    }
    StringBuffer label = new StringBuffer( predicateName);
    if (predicateName.length() < slotLabelMinLength) {
      boolean prepend = true;
      for (int i = 0, n = slotLabelMinLength - predicateName.length(); i < n; i++) {
        if (prepend) {
          label.insert( 0, " ");
        } else {
          label.append( " ");
        }
        prepend = (! prepend);
      }
    }
    return label.toString();
  } // end getSlotNodeLabel


  // is timelines and slots are in content spec, in terms of their tokens,
  // set them visible
  private void setNodesVisible() {
    // print content spec
    // System.err.println( "TimelineView - contentSpec");
    // viewSet.printSpec();
    validTokenIds = viewSet.getValidTokenIds();
    displayedTokenIds = new ArrayList();
    Iterator timelineIterator = timelineNodeList.iterator();
    Integer key = null;
    while (timelineIterator.hasNext()) {
      TimelineNode timelineNode = (TimelineNode) timelineIterator.next();
      if (isTimelineInContentSpec( timelineNode.getTimeline())) {
        timelineNode.setVisible( true);
      } else {
        timelineNode.setVisible( false);
      }
      List slotList = timelineNode.getSlotNodeList();
      int numSlotNodes = slotList.size();
      for (int i = 0, n = slotList.size(); i < n; i++) {
        SlotNode slotNode = (SlotNode) slotList.get( i);
        List timeLabels = slotNode.getTimeIntervalLabels();
        //if(timeLabels.size() == 1) {
        //  System.err.println("Labels: " + timeLabels + " " + 
        //                     ((JGoText)timeLabels.get(0)).getText());
        //}
        if (isSlotInContentSpec( slotNode.getSlot())) {
          slotNode.setVisible( true);

//           System.err.println( "views " + slotNode.getView() + " and " + jGoView);
//           System.err.println( "docs " + slotNode.getDocument() + " and " + jGoView.getDocument());
//           System.err.println( "redirect " + slotNode.redirectSelection());
//           JGoObject object = jGoSelection.extendSelection( slotNode);
//           System.err.println( "setNodesVisible: object " + slotNode);
//           jGoSelection.showHandles( slotNode);

          ((JGoText) timeLabels.get( 0)).setVisible( true);
          if (i == n - 1 && timeLabels.size() > 1) {
            ((JGoText) timeLabels.get( 1)).setVisible( true);
          }
        } else {
          // System.err.println("Setting slot " + slotNode.getSlot().getKey() + " invisible");
          slotNode.setVisible( false);
          boolean visibleValue = false;
          // display interval time label, if previous slot is not being displayed
          if (i > 0) {
            SlotNode prevSlotNode = (SlotNode) slotList.get( i - 1);
            if (isSlotInContentSpec( prevSlotNode.getSlot())) {
              visibleValue = true;
            }
            ((JGoText) timeLabels.get( 0)).setVisible( visibleValue);
            if (i == n - 1 && timeLabels.size() > 1) {
              ((JGoText) timeLabels.get( 1)).setVisible( false);
            }
          } else {
            ((JGoText) timeLabels.get( 0)).setVisible( false);
          }
        }
      }
    }
    setFreeTokensVisible();
    boolean showDialog = true;
    isContentSpecRendered( "Timeline View", showDialog);
  } // end setNodesVisible

  private void setFreeTokensVisible() {
    Iterator freeTokenNodeItr = freeTokenNodeList.iterator();
    while (freeTokenNodeItr.hasNext()) {
      TokenNode node = (TokenNode) freeTokenNodeItr.next();
      // System.err.println( "setFreeTokensVisible " + node.getToken().getKey());
      if (isTokenInContentSpec( node.getToken())) {
        node.setVisible( true);
      } else {
        node.setVisible( false);
      }
    }
  } // end setFreeTokensVisible

  private void iterateOverNodes() {
    int numTimelineNodes = timelineNodeList.size();
    System.err.println( "iterateOverNodes: numTimelineNodes " + numTimelineNodes);
    Iterator timelineIterator = timelineNodeList.iterator();
    while (timelineIterator.hasNext()) {
      TimelineNode timelineNode = (TimelineNode) timelineIterator.next();
      System.err.println( "name '" + timelineNode.getTimelineName() + "' location " +
                          timelineNode.getLocation());
      int numSlotNodes = timelineNode.getSlotNodeList().size();
      System.err.println( "numSlotNodes " + numSlotNodes); 
      Iterator slotIterator = timelineNode.getSlotNodeList().iterator();
      while (slotIterator.hasNext()) {
        SlotNode slotNode = (SlotNode) slotIterator.next();
        System.err.println( "name '" + slotNode.getPredicateName() + "' location " +
                            slotNode.getLocation());
        System.err.println( "startInterval " + slotNode.getStartTimeIntervalString());
        if (! slotIterator.hasNext()) {
          System.err.println( "endInterval " + slotNode.getEndTimeIntervalString());
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
      System.err.println( "iterateOverJGoDoc: position " + position +
                          " className " + object.getClass().getName());
      if (object instanceof SlotNode) {
        SlotNode slotNode = (SlotNode) object;

      }
      cnt += 1;
//       if (cnt > 100) {
//         break;
//       }
    }
    System.err.println( "iterateOverJGoDoc: cnt " + cnt);
  } // end iterateOverJGoDocument



} // end class TimelineView
 
