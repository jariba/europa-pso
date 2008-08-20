// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: PredicateNode.java,v 1.4 2004-03-27 00:30:47 taylor Exp $
//
// PlanWorks
//
// Will Taylor -- started 03dec03
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
import java.util.Iterator;
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
import gov.nasa.arc.planworks.db.PwRule;
import gov.nasa.arc.planworks.db.PwSlot;
import gov.nasa.arc.planworks.db.PwToken;
import gov.nasa.arc.planworks.util.ColorMap;
import gov.nasa.arc.planworks.util.MouseEventOSX;
import gov.nasa.arc.planworks.util.Utilities;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.nodes.NodeGenerics;
import gov.nasa.arc.planworks.viz.sequence.SequenceView;


/**
 * <code>PredicateNode</code> - JGo widget to render a predicate with a
 *                          label consisting of the predicate name, with a central port,
 *                          and a tooltip of params, guards, & duration
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *       NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class PredicateNode extends BasicNode {

  protected String predicateName;
  protected String objectName;
  protected String attributeName;
  protected List params;
  protected List paramValues;
  protected Point predicateLocation;
  protected String nodeLabel;
  protected PwRule rule;
  protected SequenceView sequenceView;
  protected boolean isExisting;

  public PredicateNode( String predicateName, String objectName, String attributeName,
                        List params, List paramValues, Point predicateLocation,
                        Color backgroundColor, PwRule rule, SequenceView sequenceView) {
    super();
    this.predicateName = predicateName;
    this.objectName = objectName;
    this.attributeName = attributeName;
    this.params = params;
    this.paramValues = paramValues;
    this.predicateLocation = predicateLocation;
    this.rule = rule;
    this.sequenceView = sequenceView;
    nodeLabel = predicateName;

    // System.err.println( "PredicateNode: " + nodeLabel);
    configure( predicateLocation, backgroundColor, nodeLabel);
  } // end constructor

  private final void configure( Point predicateLocation, Color backgroundColor,
                                String nodeLabel) {
    boolean isRectangular = true;
    setLabelSpot( JGoObject.Center);
    initialize( predicateLocation, nodeLabel, isRectangular);

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
    return predicateName;
  }

  /**
   * <code>getObjectName</code>
   *
   * @return - <code>String</code> - 
   */
  public String getObjectName() {
    return objectName;
  }

  /**
   * <code>getAttributeName</code>
   *
   * @return - <code>String</code> - 
   */
  public String getAttributeName() {
    return attributeName;
  }

  /**
   * <code>getParams</code>
   *
   * @return - <code>List</code> - 
   */
  public List getParams() {
    return params;
  }

  /**
   * <code>getParamValues</code>
   *
   * @return - <code>List</code> - 
   */
  public List getParamValues() {
    return paramValues;
  }

  /**
   * <code>equals</code> - override equals so nodeList.contains( predicateNode) works
   *
   * @param node - <code>Object</code> - 
   * @return - <code>boolean</code> - 
   */
  public boolean equals( Object node) {
    return (this.getName().equals( ((PredicateNode) node).getName()) &&
            this.getObjectName().equals( ((PredicateNode) node).getObjectName()) &&
            this.getAttributeName().equals( ((PredicateNode) node).getAttributeName()) &&
            this.getParams().equals( ((PredicateNode) node).getParams()) &&
            this.getParamValues().equals( ((PredicateNode) node).getParamValues()));
  }

  /**
   * <code>isExisting</code>
   *
   * @return - <code>boolean</code> - 
   */
  public boolean isExisting() {
    return isExisting;
  }

  /**
   * <code>setIsExisting</code>
   *
   * @param exists - <code>boolean</code> - 
   */
  public  void setIsExisting( boolean exists) {
    this.isExisting = exists;
  }

  /**
   * <code>getToolTipText</code>
   *
   * @return - <code>String</code> - 
   */
  public String getToolTipText() {
    StringBuffer tip = new StringBuffer( "<html>");
    tip = addPredicateNameAndParams( tip);
    tip.append( "<br>guards: ");
    tip.append( "<br>duration: [");
    return tip.toString();
  } // end getToolTipText


  private StringBuffer addPredicateNameAndParams( StringBuffer tip) {
    tip.append( predicateName).append( " ( ");
    for (int i = 0; i < paramValues.size() - 1; i++) {
      tip.append( (String) params.get( i)).append( " ");
      tip = addParamValues( (List) paramValues.get( i), tip);
      tip.append( ", ");
    }
    if (paramValues.size() > 0) {
      tip.append( (String) params.get( paramValues.size() - 1)).append( " ");
      tip = addParamValues( (List) paramValues.get( paramValues.size() - 1), tip);
    }
    tip.append( " )");
    return tip;
  } // end addPredicateNameAndParams

  private StringBuffer addParamValues( List valueList, StringBuffer tip) {
    tip.append( "{ ");
    for (int i = 0; i < valueList.size() - 1; i++) {
      tip.append( (String) valueList.get( i));
      tip.append( ", ");
    }
    if (valueList.size() > 0) {
      tip.append( (String) valueList.get( valueList.size() - 1));
    }
    tip.append( " }");
    return tip;
  } // end addParamValues

  /**
   * <code>getToolTipText</code> - when over 1/8 scale overview token node
   *
   * @param isOverview - <code>boolean</code> - 
   * @return - <code>String</code> - 
   */
  public String getToolTipText( boolean isOverview) {
    StringBuffer tip = new StringBuffer( "<html>");
    tip = addPredicateNameAndParams( tip);
    tip.append( "</html>");
    return tip.toString();
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
    // System.err.println( "PredicateNode: doMouseClick obj class " +
    //                     obj.getTopLevelObject().getClass().getName());
    PredicateNode predicateNode = (PredicateNode) obj.getTopLevelObject();
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



} // end class PredicateNode
