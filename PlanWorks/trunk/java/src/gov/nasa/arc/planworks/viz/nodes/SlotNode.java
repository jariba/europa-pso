// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// PlanWorks
//
// Will Taylor -- started 18may03
//

package gov.nasa.arc.planworks.viz.nodes;

import java.awt.Container;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGo3DRect;
import com.nwoods.jgo.JGoBrush;
import com.nwoods.jgo.JGoObject;
import com.nwoods.jgo.JGoPort;
import com.nwoods.jgo.JGoText;
import com.nwoods.jgo.JGoView;

// PlanWorks/java/lib/JGo/Classier.jar
import com.nwoods.jgo.examples.TextNode;

import gov.nasa.arc.planworks.db.PwSlot;
import gov.nasa.arc.planworks.util.ColorMap;
import gov.nasa.arc.planworks.viz.views.VizView;
import gov.nasa.arc.planworks.viz.views.timeline.TimelineView;


/**
 * <code>SlotNode</code> - 
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *       NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class SlotNode extends TextNode {

  // top left bottom right
  private static final Insets NODE_INSETS =
    new Insets( TimelineNode.INSET_SIZE_HALF, TimelineNode.INSET_SIZE,
                TimelineNode.INSET_SIZE_HALF, TimelineNode.INSET_SIZE);
  private String predicateName;
  private PwSlot slot;
  private VizView view;


  /**
   * <code>SlotNode</code> - constructor 
   *
   * @param predicateName - <code>String</code> - 
   * @param slot - <code>PwSlot</code> - 
   * @param view - <code>VizView</code> - 
   */
  public SlotNode( String predicateName, PwSlot slot, VizView view) {
    super( predicateName);
    this.predicateName = predicateName;
    this.slot = slot;
    this.view = view;
    System.err.println( "SlotNode: predicateName " + predicateName);
    configure();
  } // end constructor


  private final void configure() {
    setBrush( JGoBrush.makeStockBrush( ColorMap.getColor( "gray60")));  
    getLabel().setEditable( false);
    setDraggable( false);
    // do not allow links
    getTopPort().setVisible( false);
    getLeftPort().setVisible( false);
    getBottomPort().setVisible( false);
    getRightPort().setVisible( false);
    setInsets( NODE_INSETS);


    // retrieveTimeIntervals();

    // retrieveTokenNameAndParams();

  } // end configure



} // end class SlotNode

