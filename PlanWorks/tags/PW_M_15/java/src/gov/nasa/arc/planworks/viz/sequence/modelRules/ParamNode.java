// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: ParamNode.java,v 1.3 2004-05-21 21:39:09 taylor Exp $
//
// PlanWorks
//
// Will Taylor -- started 08dec03
//

package gov.nasa.arc.planworks.viz.sequence.modelRules;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoBrush;
import com.nwoods.jgo.JGoImage;
import com.nwoods.jgo.JGoObject;
import com.nwoods.jgo.JGoPen;
import com.nwoods.jgo.JGoRectangle;
import com.nwoods.jgo.JGoText;
import com.nwoods.jgo.JGoView;

// PlanWorks/java/lib/JGo/Classier.jar
import com.nwoods.jgo.examples.BasicNode;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.PwSlot;
import gov.nasa.arc.planworks.db.PwToken;
import gov.nasa.arc.planworks.util.ColorMap;
import gov.nasa.arc.planworks.util.MouseEventOSX;
import gov.nasa.arc.planworks.util.Utilities;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.ViewGenerics;
import gov.nasa.arc.planworks.viz.nodes.NodeGenerics;
import gov.nasa.arc.planworks.viz.sequence.SequenceView;


/**
 * <code>ParamNode</code> - JGo widget to render a model rule with a
 *                          label consisting of the rule type, and a tooltip
 *                          with param, value, and constraints
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *       NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class ParamNode extends BasicNode {

  protected String paramName;
  protected Point paramLocation;
  protected String nodeLabel;
  protected SequenceView sequenceView;

  /**
   * <code>ParamNode</code> - constructor 
   *
   * @param paramName - <code>String</code> - 
   * @param paramLocation - <code>Point</code> - 
   * @param backgroundColor - <code>Color</code> - 
   * @param sequenceView - <code>SequenceView</code> - 
   */
  public ParamNode( String paramName, Point paramLocation,
                        Color backgroundColor, SequenceView sequenceView) {
    super();
    this.paramName = paramName;
    this.paramLocation = paramLocation;
    this.sequenceView = sequenceView;
    nodeLabel = "or";
    // System.err.println( "ParamNode: " + nodeLabel);
    configure( paramLocation, backgroundColor, nodeLabel);
  } // end constructor

  private final void configure( Point paramLocation, Color backgroundColor,
                                String nodeLabel) {
    boolean isRectangular = false;
    setLabelSpot( JGoObject.Center);
    initialize( paramLocation, nodeLabel, isRectangular);

    setBrush( JGoBrush.makeStockBrush( backgroundColor));  
    setDraggable( true);
    getLabel().setSelectable( false);
    getLabel().setEditable( false);
    getLabel().setMultiline( false);
    getLabel().setDraggable( false);
    // do not allow user links
    getPort().setVisible( false);
  } // end configure

  /**
   * <code>getName</code>
   *
   * @return - <code>String</code> - 
   */
  public String getName() {
    return paramName;
  }

  /**
   * <code>equals</code> - override equals so nodeList.contains( paramNode) works
   *
   * @param node - <code>Object</code> - 
   * @return - <code>boolean</code> - 
   */
  public boolean equals( Object node) {
    return (this.getName().equals( ((ParamNode) node).getName()));
  }

  /**
   * <code>getToolTipText</code>
   *
   * @return - <code>String</code> - 
   */
  public String getToolTipText() {
    StringBuffer tip = new StringBuffer( "<html>param: ");
    tip.append( paramName).append( "</html>");
    return tip.toString();
  } // end getToolTipText

  /**
   * <code>getToolTipText</code> - when over 1/8 scale overview token node
   *
   * @param isOverview - <code>boolean</code> - 
   * @return - <code>String</code> - 
   */
  public String getToolTipText( boolean isOverview) {
    return nodeLabel;
  } // end getToolTipText


  /**
   * <code>doMouseClick</code> - Mouse-Right: Set Active Token
   *
   * @param modifiers - <code>int</code> - 
   * @param dc - <code>Point</code> - 
   * @param vc - <code>Point</code> - 
   * @param view - <code>JGoView</code> - 
   * @return - <code>boolean</code> - 
   */
  public boolean doMouseClick( int modifiers, Point docCoords, Point viewCoords,
                               JGoView view) {
    JGoObject obj = view.pickDocObject( docCoords, false);
    // System.err.println( "ParamNode: doMouseClick obj class " +
    //                     obj.getTopLevelObject().getClass().getName());
    ParamNode paramNode = (ParamNode) obj.getTopLevelObject();
    if (MouseEventOSX.isMouseLeftClick( modifiers, PlanWorks.isMacOSX())) {
      // do nothing
    } else if (MouseEventOSX.isMouseRightClick( modifiers, PlanWorks.isMacOSX())) {

      // mouseRightPopupMenu( viewCoords);

      // return true;
    }
    return false;
  } // end doMouseClick   

  private void mouseRightPopupMenu( Point viewCoords) {
    JPopupMenu mouseRightPopup = new JPopupMenu();


    ViewGenerics.showPopupMenu( mouseRightPopup, sequenceView, viewCoords);
  } // end mouseRightPopupMenu



} // end class ParamNode
