// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: RuleNode.java,v 1.3 2004-01-12 19:46:34 taylor Exp $
//
// PlanWorks
//
// Will Taylor -- started 05dec03
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

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.PwSlot;
import gov.nasa.arc.planworks.db.PwToken;
import gov.nasa.arc.planworks.util.ColorMap;
import gov.nasa.arc.planworks.util.MouseEventOSX;
import gov.nasa.arc.planworks.util.Utilities;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.nodes.ExtendedBasicNode;
import gov.nasa.arc.planworks.viz.nodes.NodeGenerics;
import gov.nasa.arc.planworks.viz.sequence.SequenceView;


/**
 * <code>RuleNode</code> - JGo widget to render a model rule with a
 *                          label consisting of the rule type, and a tooltip
 *                          with param, value, and constraints
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *       NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class RuleNode extends ExtendedBasicNode {

  protected String paramName;
  protected String paramValue;
  protected String ruleName;
  protected Point ruleLocation;
  protected String nodeLabel;
  protected SequenceView sequenceView;

  public RuleNode( String paramName, String paramValue, String ruleName, Point ruleLocation,
                        Color backgroundColor, SequenceView sequenceView) {
    super(ViewConstants.DIAMOND);
    this.paramName = paramName;
    this.paramValue = paramValue;
    this.ruleName = ruleName;
    this.ruleLocation = ruleLocation;
    this.sequenceView = sequenceView;
    nodeLabel = ruleName;
    // System.err.println( "RuleNode: " + nodeLabel);
    configure( ruleLocation, backgroundColor, nodeLabel);
  } // end constructor

  private final void configure( Point ruleLocation, Color backgroundColor,
                                String nodeLabel) {
    boolean isRectangular = false;
    setLabelSpot( JGoObject.Center);
    initialize( ruleLocation, nodeLabel, isRectangular);

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
    return ruleName;
  }

  /**
   * <code>getParamName</code>
   *
   * @return - <code>String</code> - 
   */
  public String getParamName() {
    return paramName;
  }
 
  /**
   * <code>getParamValue</code>
   *
   * @return - <code>String</code> - 
   */
  public String getParamValue() {
    return paramValue;
  }

  /**
   * <code>equals</code> - override equals so nodeList.contains( ruleNode) works
   *
   * @param node - <code>Object</code> - 
   * @return - <code>boolean</code> - 
   */
  public boolean equals( Object node) {
    return (this.getName().equals( ((RuleNode) node).getName()));
  }

  /**
   * <code>getToolTipText</code>
   *
   * @return - <code>String</code> - 
   */
  public String getToolTipText() {
    StringBuffer tip = new StringBuffer( "<html>param: ");
    tip.append( paramName).append( "<br>");
    tip.append( "value: ").append( paramValue).append(  "<br>");
    tip.append( "constraints: ");
    tip.append( "</html>");
    return tip.toString();
  } // end getToolTipText

  /**
   * <code>getToolTipText</code> - when over 1/8 scale overview token node
   *
   * @param isOverview - <code>boolean</code> - 
   * @return - <code>String</code> - 
   */
  public String getToolTipText( boolean isOverview) {
    return null;
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
    // System.err.println( "RuleNode: doMouseClick obj class " +
    //                     obj.getTopLevelObject().getClass().getName());
    RuleNode ruleNode = (RuleNode) obj.getTopLevelObject();
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


    NodeGenerics.showPopupMenu( mouseRightPopup, sequenceView, viewCoords);
  } // end mouseRightPopupMenu



} // end class RuleNode
