// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: TimelineView.java,v 1.3 2003-05-27 21:24:48 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 18May03
//

package gov.nasa.arc.planworks.viz.views.timeline;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoDocument;
// import com.nwoods.jgo.JGoListPosition;
// import com.nwoods.jgo.JGoObject;
import com.nwoods.jgo.JGoView;


import gov.nasa.arc.planworks.db.PwObject;
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.PwSlot;
import gov.nasa.arc.planworks.db.PwTimeline;
import gov.nasa.arc.planworks.db.PwToken;
import gov.nasa.arc.planworks.util.ColorMap;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.nodes.SlotNode;
import gov.nasa.arc.planworks.viz.nodes.TimelineNode;
import gov.nasa.arc.planworks.viz.views.VizView;

/**
 * <code>TimelineView</code> - 
 *                JPanel->VizView->TimelineView
 *                JComponent->JGoView
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                  NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class TimelineView extends VizView {

  private JGoView jGoView;
  private JGoDocument jGoDoc;
  private List timelineNodeList; // element TimelineNode

  private Font font;
  private FontMetrics fontMetrics;
  private int slotLabelMinLength;


  public TimelineView( PwPartialPlan partialPlan) {
    super( partialPlan);
    this.timelineNodeList = new ArrayList();

    setLayout( new BoxLayout( this, BoxLayout.Y_AXIS));
    slotLabelMinLength = ViewConstants.TIMELINE_VIEW_EMPTY_NODE_LABEL_LEN;

    jGoView = new JGoView();
    jGoView.setBackground( ColorMap.getColor( "lightGray"));
    add( jGoView, BorderLayout.NORTH);
    jGoView.validate();
    jGoView.setVisible( true);
    font = new Font( ViewConstants.TIMELINE_VIEW_FONT_NAME,
                     ViewConstants.TIMELINE_VIEW_FONT_STYLE,
                     ViewConstants.TIMELINE_VIEW_FONT_SIZE);
    jGoView.setFont( font);

    SwingUtilities.invokeLater( runInit);
  } // end constructor

  Runnable runInit = new Runnable() {
      public void run() {
        init();
      }
    };

  /**
   * <code>init</code>render the JGo widgets
   *
   *    not done in constructor to avoid:
   *    "Cannot measure text until a JGoView exists and is part of a visible window".
   *    called by componentShown method on the JFrame
   *    JGoView.setVisible( true) must be completed -- use runInit in constructor
   */
  public void init() {
    Graphics graphics = ((JPanel) this).getGraphics();
    fontMetrics = graphics.getFontMetrics( font);
    graphics.dispose();

    jGoDoc = jGoView.getDocument();

    createTimelineAndSlotNodes();

    // iterateOverJGoDoc();
    iterateOverNodes();

  } // end init


  /**
   * <code>getJGoView</code>
   *
   * @return - <code>JGoView</code> - 
   */
  public JGoView getJGoView()  {
    return this.jGoView;
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
   * <code>getSlotLabelMinLength</code>
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

  private void createTimelineAndSlotNodes() {
    JGoDocument jGoDoc = jGoView.getDocument();
    int x = ViewConstants.TIMELINE_VIEW_X_INIT;
    int y = ViewConstants.TIMELINE_VIEW_Y_INIT;
    List objectList = partialPlan.getObjectList();
    Iterator objectIterator = objectList.iterator();
    while (objectIterator.hasNext()) {
      PwObject object = (PwObject) objectIterator.next();
      String objectName = object.getName();
      List timelineList = object.getTimelineList();
      Iterator timelineIterator = timelineList.iterator();
      while (timelineIterator.hasNext()) {
        x = ViewConstants.TIMELINE_VIEW_X_INIT;
        PwTimeline timeline = (PwTimeline) timelineIterator.next();
        String timelineName = timeline.getName();
        String timelineKey = timeline.getKey();
        TimelineNode timelineNode =
          new TimelineNode( objectName + " : " + timelineName, timeline,
                            new Point( x, y), this);
        timelineNodeList.add( timelineNode);
        // System.err.println( "createTimelineAndSlotNodes: TimelineNode x " + x + " y " + y);
        jGoDoc.addObjectAtTail( timelineNode);
        x += timelineNode.getSize().getWidth();
        List slotList = timeline.getSlotList();
        Iterator slotIterator = slotList.iterator();
        PwToken previousToken = null;
        while (slotIterator.hasNext()) {
          PwSlot slot = (PwSlot) slotIterator.next();
          PwToken token = (PwToken) slot.getTokenList().get( 0);
          String slotNodeLabel = getSlotNodeLabel( token);
          boolean isLastSlot = (! slotIterator.hasNext());
          SlotNode slotNode =
            new SlotNode( slotNodeLabel, slot, new Point( x, y), previousToken,
                          isLastSlot, this);
          timelineNode.addToSlotNodeList( slotNode);
          // System.err.println( "createTimelineAndSlotNodes: SlotNode x " + x + " y " + y);
          jGoDoc.addObjectAtTail( slotNode);
          previousToken = token;
          x += slotNode.getSize().getWidth();
        }
        y += ViewConstants.TIMELINE_VIEW_Y_DELTA;
      }
    }
  } // end createTimelineAndSlotNodes


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


  public void iterateOverNodes() {
    int numTimelineNodes = timelineNodeList.size();
    System.err.println( "iterateOverNodes: numTimelineNodes " + numTimelineNodes);
    Iterator timelineIterator = timelineNodeList.iterator();
    while (timelineIterator.hasNext()) {
      TimelineNode timelineNode = (TimelineNode) timelineIterator.next();
      System.err.println( "name '" + timelineNode.getTimelineName() + "' location " +
                          timelineNode.getTimelineLocation());
      int numSlotNodes = timelineNode.getSlotNodeList().size();
      System.err.println( "numSlotNodes " + numSlotNodes); 
      Iterator slotIterator = timelineNode.getSlotNodeList().iterator();
      while (slotIterator.hasNext()) {
        SlotNode slotNode = (SlotNode) slotIterator.next();
        System.err.println( "name '" + slotNode.getPredicateName() + "' location " +
                            slotNode.getSlotLocation());
        System.err.println( "startInterval " + slotNode.getStartTimeIntervalString());
        if (! slotIterator.hasNext()) {
          System.err.println( "endInterval " + slotNode.getEndTimeIntervalString());
        }
      }
    }
  } // end iterateOverNodes



//   public void iterateOverJGoDoc() {
//     JGoListPosition position = jGoDoc.getFirstObjectPos();
//     JGoListPosition lastPosition = jGoDoc.getLastObjectPos();
//     JGoObject object = jGoDoc.getObjectAtPos( position);
//     System.err.println( "iterateOverJGoDoc: position " + position + " lastPosition " +
//                         lastPosition + " className " + object.getClass().getName());
//     position = jGoDoc.getNextObjectPos( position);
//     int cnt = 0;
//     while (! position.equals( lastPosition)) {
//       object = jGoDoc.getObjectAtPos( position);
//       System.err.println( "iterateOverJGoDoc: position " + position + " className " +
//                           object.getClass().getName());
//       position = jGoDoc.getNextObjectPos( position);
//       cnt += 1;
//       if (cnt > 100) {
//         break;
//       }
//     }
//   } // end iterateOverJGoDoc



} // end class TimelineView
 
