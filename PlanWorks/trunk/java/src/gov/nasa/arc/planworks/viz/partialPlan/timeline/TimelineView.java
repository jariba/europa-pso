// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: TimelineView.java,v 1.21 2003-12-20 01:54:51 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 18May03
//

package gov.nasa.arc.planworks.viz.partialPlan.timeline;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

// PlanWorks/java/lib/JGo/JGo.jar
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
import gov.nasa.arc.planworks.util.ColorMap;
import gov.nasa.arc.planworks.util.MouseEventOSX;
import gov.nasa.arc.planworks.util.Utilities;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.ViewGenerics;
import gov.nasa.arc.planworks.viz.VizViewOverview;
import gov.nasa.arc.planworks.viz.nodes.NodeGenerics;
import gov.nasa.arc.planworks.viz.nodes.TokenNode;
import gov.nasa.arc.planworks.viz.partialPlan.AskNodeByKey;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanView;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanViewSet;
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

  private PwPartialPlan partialPlan;
  private long startTimeMSecs;
  private ViewSet viewSet;
  private TimelineJGoView jGoView;
  private JGoDocument jGoDocument;
  // timelineNodeList & tmpTimelineNodeList used by JFCUnit test case
  private List timelineNodeList; // element TimelineNode
  private List freeTokenNodeList; // element TokenNode
  private List tmpTimelineNodeList; // element TimelineNode
  private int slotLabelMinLength;

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
    this.partialPlan = (PwPartialPlan) partialPlan;
    this.startTimeMSecs = System.currentTimeMillis();
    this.viewSet = (PartialPlanViewSet) viewSet;

    setLayout( new BoxLayout( this, BoxLayout.Y_AXIS));
    slotLabelMinLength = ViewConstants.TIMELINE_VIEW_EMPTY_NODE_LABEL_LEN;

    jGoView = new TimelineJGoView();
    jGoView.setBackground( ViewConstants.VIEW_BACKGROUND_COLOR);
    add( jGoView, BorderLayout.NORTH);
    jGoView.validate();
    jGoView.setVisible( true);
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
    jGoView.setCursor( new Cursor( Cursor.WAIT_CURSOR));
    // wait for TimelineView instance to become displayable
    while (! this.isDisplayable()) {
      try {
        Thread.currentThread().sleep(50);
      } catch (InterruptedException excp) {
      }
      // System.err.println( "timelineView displayable " + this.isDisplayable());
    }
    this.computeFontMetrics( this);

    jGoDocument = jGoView.getDocument();

    // create all nodes
    renderTimelineAndSlotNodes();

    expandViewFrame( viewSet.openView( this.getClass().getName()),
                     (int) jGoView.getDocumentSize().getWidth(),
                     (int) jGoView.getDocumentSize().getHeight());

    // print out info for created nodes
    // iterateOverJGoDocument(); // slower - many more nodes to go thru
    // iterateOverNodes();

    long stopTimeMSecs = System.currentTimeMillis();
    System.err.println( "   ... elapsed time: " +
                        (stopTimeMSecs - startTimeMSecs) + " msecs.");
    jGoView.setCursor( new Cursor( Cursor.DEFAULT_CURSOR));
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
      renderTimelineAndSlotNodes();
    } //end run

  } // end class RedrawViewThread

  private void renderTimelineAndSlotNodes() {
    jGoView.getDocument().deleteContents();

    validTokenIds = viewSet.getValidIds();
    displayedTokenIds = new ArrayList();
    timelineNodeList = null;
    freeTokenNodeList = new ArrayList();
    tmpTimelineNodeList = new ArrayList();

    createTimelineAndSlotNodes();

    boolean showDialog = true;
    isContentSpecRendered( PlanWorks.TIMELINE_VIEW, showDialog);
  } // end createTemporalExtentView

  /**
   * <code>getJGoDocument</code>
   *
   * @return - <code>JGoDocument</code> - 
   */
  public JGoDocument getJGoDocument()  {
    return this.jGoDocument;
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

  /**
   * <code>getFreeTokenNodeList</code>
   *
   * @return - <code>List</code> - 
   */
  public List getFreeTokenNodeList() {
    return freeTokenNodeList;
  }

  private void createTimelineAndSlotNodes() {
    int x = ViewConstants.TIMELINE_VIEW_X_INIT;
    int y = ViewConstants.TIMELINE_VIEW_Y_INIT;
    List objectList = partialPlan.getObjectList();
    Iterator objectIterator = objectList.iterator();
    int timelineCnt = 0;
    while (objectIterator.hasNext()) {
      PwObject object = (PwObject) objectIterator.next();
      String objectName = object.getName();
      List timelineList = object.getTimelineList();
      int timelineNodeWidth = computeTimelineNodesWidth( timelineList, objectName);
      Iterator timelineIterator = timelineList.iterator();
      while (timelineIterator.hasNext()) {
        x = ViewConstants.TIMELINE_VIEW_X_INIT;
        PwTimeline timeline = (PwTimeline) timelineIterator.next();
        if (isTimelineInContentSpec( timeline)) {
          String timelineName = timeline.getName();
          String timelineLabel = objectName + " : " + timelineName + 
                                  "\ntimeline key=" + timeline.getId().toString();
          Color timelineColor =
            ((PartialPlanViewSet) viewSet).getColorStream().getColor( timelineCnt);
          TimelineNode timelineNode =
            new TimelineNode( timelineLabel, timeline, new Point( x, y), timelineColor);
          tmpTimelineNodeList.add( timelineNode);
          // System.err.println( "createTimelineAndSlotNodes: TimelineNode x " + x + " y " + y);
          jGoDocument.addObjectAtTail( timelineNode);
          if (timelineNodeWidth > timelineNode.getSize().getWidth()) {
            timelineNode.setSize( timelineNodeWidth, (int) timelineNode.getSize().getHeight());
          }
          x += timelineNode.getSize().getWidth(); 
          createSlotNodes( timeline, timelineNode, x, y, timelineColor);
          y += ViewConstants.TIMELINE_VIEW_Y_DELTA; 
        }
        timelineCnt += 1;
      }
    }
    y += ViewConstants.TIMELINE_VIEW_Y_INIT;
    List freeTokenList = partialPlan.getFreeTokenList();
    Iterator freeTokenItr = freeTokenList.iterator();
    boolean isFreeToken = true, isDraggable = false;
    Color backgroundColor = ColorMap.getColor( ViewConstants.FREE_TOKEN_BG_COLOR);
    PwSlot slot = null;
    while (freeTokenItr.hasNext()) {
      PwToken freeToken = (PwToken) freeTokenItr.next();
      if (isTokenInContentSpec( freeToken))  {
        // increment by half the label width, since x is center, not left edge
        x = x +  SwingUtilities.computeStringWidth( this.fontMetrics,
                                                    freeToken.getPredicate().getName()) / 2;
        TokenNode freeTokenNode = new TokenNode( freeToken, slot, new Point( x, y),
                                                 backgroundColor, isFreeToken, isDraggable, this);
        freeTokenNodeList.add( freeTokenNode);
        jGoDocument.addObjectAtTail( freeTokenNode);
        x = x + (int) freeTokenNode.getSize().getWidth();
      }
    }
    timelineNodeList = tmpTimelineNodeList;
  } // end createTimelineAndSlotNodes

  private void createSlotNodes( PwTimeline timeline, TimelineNode timelineNode,
                                int x, int y, Color backgroundColor) {
    computeTimeIntervalLabelSize( timeline);

    List slotList = timeline.getSlotList();
    Iterator slotIterator = slotList.iterator();
    SlotNode previousSlotNode = null;
    SlotNode slotNode = null;
    boolean isFirstSlot = true, alwaysReturnEnd = false;
    while (slotIterator.hasNext()) {
      PwSlot slot = (PwSlot) slotIterator.next();
      // overloaded tokens on slot - not displayed, put in displayedTokenIds
      List tokenList = slot.getTokenList();
      for (int i = 1, n = tokenList.size(); i < n; i++) {
        isTokenInContentSpec( (PwToken) tokenList.get( i));
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
          slotNode = new SlotNode( slotNodeLabel, slot, new Point( x, y), previousSlotNode,
                                   isFirstSlot, isLastSlot, backgroundColor, this);
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
  } // end createSlotNodes


  // perform two integrity checks:
  //   1) two successive empty slots must not occur
  //   2) earliest start times must be monotonically increasing
  // while computing time interval max label size
  private void computeTimeIntervalLabelSize( PwTimeline timeline) {
    slotLabelMinLength = ViewConstants.TIMELINE_VIEW_EMPTY_NODE_LABEL_LEN;
    List slotList = timeline.getSlotList();
    Iterator slotIterator = slotList.iterator();
    PwSlot previousSlot = null;
    SlotNode slotNode = null;
    boolean alwaysReturnEnd = true, foundEmptySlot = false;
    int earliestStartTime = DbConstants.MINUS_INFINITY_INT;
    while (slotIterator.hasNext()) {
      PwSlot slot = (PwSlot) slotIterator.next();
      PwToken token = slot.getBaseToken();
      if (token == null) {
        if (foundEmptySlot) {
          String message = "Two successive empty slots found in timeline '" +
            timeline.getName() + "' (id = " + timeline.getId() + ")";
          JOptionPane.showMessageDialog( PlanWorks.planWorks, message,
                                         "Timeline View Exception",
                                         JOptionPane.ERROR_MESSAGE);
          System.err.println( message);
          System.exit( 1);
        } else {
          foundEmptySlot = true;
        }
      } else {
        foundEmptySlot = false;
      }
      boolean isLastSlot = (! slotIterator.hasNext());
      PwDomain[] intervalArray =
        NodeGenerics.getStartEndIntervals( this, slot, previousSlot, isLastSlot,
                                           alwaysReturnEnd);
      PwDomain startTimeIntervalDomain = intervalArray[0];
      PwDomain endTimeIntervalDomain = intervalArray[1];
      if ((endTimeIntervalDomain != null) &&
          ((endTimeIntervalDomain.toString().length() +
            ViewConstants.TIME_INTERVAL_STRINGS_OVERLAP_OFFSET) >
           slotLabelMinLength)) {
        slotLabelMinLength = endTimeIntervalDomain.toString().length() +
          ViewConstants.TIME_INTERVAL_STRINGS_OVERLAP_OFFSET;
      }
//       System.err.println( "computeTimeIntervalLabelSize: start " +
//                           startTimeIntervalDomain.toString() + " earliestStartTime " +
//                           earliestStartTime );
//       System.err.println( "computeTimeIntervalLabelSize: end " +
//                           endTimeIntervalDomain.toString());
      if (startTimeIntervalDomain != null) {
        if (startTimeIntervalDomain.getLowerBoundInt() >= earliestStartTime) {
          earliestStartTime = startTimeIntervalDomain.getLowerBoundInt();
        } else {
          String message = "Earliest start times are not monotonically increasing " +
            "in timeline '" + timeline.getName() + "' (id = " + timeline.getId() + ")";
          JOptionPane.showMessageDialog( PlanWorks.planWorks, message,
                                         "Timeline View Exception",
                                         JOptionPane.ERROR_MESSAGE);
          System.err.println( message);
          System.exit( 1);
        }
      }
      previousSlot = slot;
    }
  } // end computeTimeIntervalLabelSize


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
      label = new StringBuffer( token.getPredicate().getName());
      label.append( " (").append( String.valueOf( slot.getTokenList().size()));
      label.append( ")");
    }
    int labelMinLength = slotLabelMinLength;
    // assuming start interval is {1234}, rather than [1234 1235], comment next lines
//     if (isFirstSlot) { // because of left alignment to left edge of slot
//       labelMinLength *= 1.5;
//     }
    int nodeLength = Math.max( label.length(), keyValue.length());
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
    return label.toString() + keyValue;
  } // end getSlotNodeLabel


  // is timelines and slots are in content spec, in terms of their tokens,
  // set them visible
//   private void setNodesVisible() {
//     // print content spec
//     // System.err.println( "TimelineView - contentSpec");
//     // viewSet.printSpec();
//     validTokenIds = viewSet.getValidIds();
//     displayedTokenIds = new ArrayList();
//     Iterator timelineIterator = timelineNodeList.iterator();
//     Integer id = null;
//     while (timelineIterator.hasNext()) {
//       TimelineNode timelineNode = (TimelineNode) timelineIterator.next();
//       if (isTimelineInContentSpec( timelineNode.getTimeline())) {
//         timelineNode.setVisible( true);
//       } else {
//         timelineNode.setVisible( false);
//       }
//       List slotList = timelineNode.getSlotNodeList();
//       int numSlotNodes = slotList.size();
//       for (int i = 0, n = slotList.size(); i < n; i++) {
//         SlotNode slotNode = (SlotNode) slotList.get( i);
//         if (isSlotInContentSpec( slotNode.getSlot())) {
//           slotNode.setVisible( true);
//           if (slotNode.getStartTimeIntervalObject() != null) {
//             slotNode.getStartTimeIntervalObject().setVisible( true);
//           }
//           if ((i == n - 1) && (slotNode.getEndTimeIntervalObject() != null)) {
//             slotNode.getEndTimeIntervalObject().setVisible( true);
//           }
//         } else {
//           // System.err.println("Setting slot " + slotNode.getSlot().getId() + " invisible");
//           slotNode.setVisible( false);
//           boolean visibleValue = false;
//           // display interval time label, if previous slot is not being displayed
//           if (i > 0) {
//             SlotNode prevSlotNode = (SlotNode) slotList.get( i - 1);
//             if (isSlotInContentSpec( prevSlotNode.getSlot())) {
//               visibleValue = true;
//             }
//             if (slotNode.getStartTimeIntervalObject() != null) {
//               slotNode.getStartTimeIntervalObject().setVisible( visibleValue);
//             }
//           } else {
//             slotNode.getStartTimeIntervalObject().setVisible( false);
//           }
//           if ((i == n - 1) && (slotNode.getEndTimeIntervalObject() != null)) {
//             slotNode.getEndTimeIntervalObject().setVisible( false);
//           }
//         }
//       }
//     }
//     setFreeTokensVisible();
//     boolean showDialog = true;
//     isContentSpecRendered( "Timeline View", showDialog);
//   } // end setNodesVisible

//   private void setFreeTokensVisible() {
//     Iterator freeTokenNodeItr = freeTokenNodeList.iterator();
//     while (freeTokenNodeItr.hasNext()) {
//       TokenNode node = (TokenNode) freeTokenNodeItr.next();
//       // System.err.println( "setFreeTokensVisible " + node.getToken().getId());
//       if (isTokenInContentSpec( node.getToken())) {
//         node.setVisible( true);
//       } else {
//         node.setVisible( false);
//       }
//     }
//   } // end setFreeTokensVisible

  private void iterateOverNodes() {
    int numTimelineNodes = timelineNodeList.size();
    //System.err.println( "iterateOverNodes: numTimelineNodes " + numTimelineNodes);
    Iterator timelineIterator = timelineNodeList.iterator();
    while (timelineIterator.hasNext()) {
      TimelineNode timelineNode = (TimelineNode) timelineIterator.next();
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
    PwPlanningSequence planSequence = PlanWorks.planWorks.getPlanSequence( partialPlan);
    JPopupMenu mouseRightPopup = new JPopupMenu();

    createSteppingItems(mouseRightPopup);

    JMenuItem nodeByKeyItem = new JMenuItem( "Find by Key");
    createNodeByKeyItem( nodeByKeyItem);
    mouseRightPopup.add( nodeByKeyItem);

    createOpenViewItems( partialPlan, partialPlanName, planSequence, mouseRightPopup,
                         PlanWorks.TIMELINE_VIEW);

    JMenuItem overviewWindowItem = new JMenuItem( "Overview Window");
    createOverviewWindowItem( overviewWindowItem, this, viewCoords);
    mouseRightPopup.add( overviewWindowItem);

    JMenuItem raiseContentSpecItem = new JMenuItem( "Raise Content Filter");
    createRaiseContentSpecItem( raiseContentSpecItem);
    mouseRightPopup.add( raiseContentSpecItem);
    
    JMenuItem activeTokenItem = new JMenuItem( "Snap to Active Token");
    createActiveTokenItem( activeTokenItem);
    mouseRightPopup.add( activeTokenItem);

    createAllViewItems( partialPlan, partialPlanName, planSequence, mouseRightPopup);

    NodeGenerics.showPopupMenu( mouseRightPopup, this, viewCoords);
  } // end mouseRightPopupMenu


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
      TimelineNode timelineNode = (TimelineNode) timelineNodeListItr.next();
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
                                tokenToFind.getPredicate().getName() +
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
                              tokenToFind.getPredicate().getName() +                   
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
      String message = "Token " + tokenToFind.getPredicate().getName() +
        " (key=" + tokenToFind.getId().toString() + ") not found.";
      JOptionPane.showMessageDialog( PlanWorks.planWorks, message,
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
      TimelineNode timelineNode = (TimelineNode) timelineNodeListItr.next();
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
      JOptionPane.showMessageDialog( PlanWorks.planWorks, message,
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
            ViewGenerics.openOverviewFrame( PlanWorks.TIMELINE_VIEW, partialPlan,
                                            timelineView, viewSet, jGoView, viewCoords);
          if (currentOverview != null) {
            overview = currentOverview;
          }
        }
      });
  } // end createOverviewWindowItem

} // end class TimelineView
 



