// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: TimelineView.java,v 1.1 2003-05-20 18:25:36 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 18May03
//

package gov.nasa.arc.planworks.viz.views.timeline;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Iterator;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoDocument;
import com.nwoods.jgo.JGoLink;
import com.nwoods.jgo.JGoView;


import gov.nasa.arc.planworks.db.PwObject;
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.PwSlot;
import gov.nasa.arc.planworks.db.PwTimeline;
import gov.nasa.arc.planworks.db.PwToken;
import gov.nasa.arc.planworks.util.ColorMap;
import gov.nasa.arc.planworks.viz.nodes.SlotNode;
import gov.nasa.arc.planworks.viz.nodes.TimelineNode;
import gov.nasa.arc.planworks.viz.views.VizView;

/**
 * <code>TimelineView</code> - 
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                  NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class TimelineView extends VizView {

  private JGoView timelineNodesView;
  private boolean isInitDone;


  public TimelineView( PwPartialPlan partialPlan) {
    super( partialPlan);

    isInitDone = false;
    init();
  } // end constructor


  /**
   * <code>init</code>render the JGo widgets
   *
   *    not done in constructor to avoid:
   *    "Cannot measure text until a JGoView exists and is part of a visible window".
   *    called by componentShown method on the JFrame
   */
  public void init() {
    if (! isInitDone) {
      isInitDone = true;
      setLayout( new BoxLayout( this, BoxLayout.Y_AXIS));

      timelineNodesView = new JGoView();
      timelineNodesView.setBackground( ColorMap.getColor( "lightGray"));
      add( timelineNodesView, BorderLayout.NORTH);
      timelineNodesView.validate();
      timelineNodesView.setVisible( true);

      createTimelineAndSlotNodes();
    }
  } // end init


  private void createTimelineAndSlotNodes() {
    JGoDocument doc = timelineNodesView.getDocument();
    int x = 10, y = 10, yDelta = 50;
    List objectList = partialPlan.getObjectList();
    Iterator objectIterator = objectList.iterator();
    while (objectIterator.hasNext()) {
      PwObject object = (PwObject) objectIterator.next();
      String objectName = object.getName();
      List timelineList = object.getTimelineList();
      Iterator timelineIterator = timelineList.iterator();
      while (timelineIterator.hasNext()) {
        PwTimeline timeline = (PwTimeline) timelineIterator.next();
        String timelineName = timeline.getName();
        String timelineKey = timeline.getKey();
        TimelineNode timelineNode =
          new TimelineNode( objectName + " : " + timelineName, timeline, this);
        x = 10;
        System.err.println( "createTimelineAndSlotNodes: TimelineNode x " + x + " y " + y);
        timelineNode.setLocation( x, y);
        doc.addObjectAtTail( timelineNode);
        x += timelineNode.getSize().getWidth();
        List slotList = timeline.getSlotList();
        Iterator slotIterator = slotList.iterator();
        while (slotIterator.hasNext()) {
          PwSlot slot = (PwSlot) slotIterator.next();
          String predicateName = "";
          PwToken token = (PwToken) slot.getTokenList().get( 0);
          // System.err.println( "createTimelineAndSlotNodes: " + token.getKey());
          // check for empty slot
          if (token != null) {
            predicateName = token.getPredicate().getName();
          }
          SlotNode slotNode = new SlotNode( predicateName, slot, this);
          System.err.println( "createTimelineAndSlotNodes: SlotNode x " + x + " y " + y);
          slotNode.setLocation( x, y);
          doc.addObjectAtTail( slotNode);
          x += slotNode.getSize().getWidth();
        }
        y += yDelta;
      }
    }
  } // end createTimelineAndSlotNodes



} // end class TimelineView
