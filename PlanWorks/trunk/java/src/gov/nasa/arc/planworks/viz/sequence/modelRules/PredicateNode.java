// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: PredicateNode.java,v 1.1 2003-12-03 02:29:51 taylor Exp $
//
// PlanWorks
//
// Will Taylor -- started 20june03
//

package gov.nasa.arc.planworks.viz.sequence.modelRules;

import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoBrush;
import com.nwoods.jgo.JGoObject;
import com.nwoods.jgo.JGoPen;
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
import gov.nasa.arc.planworks.viz.nodes.NodeGenerics;
import gov.nasa.arc.planworks.viz.sequence.SequenceView;


/**
 * <code>PredicateNode</code> - JGo widget to render a predicate with a
 *                          label consisting of the predicate name.
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *       NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class PredicateNode extends BasicNode {

  protected String predicateName;
  protected Point predicateLocation;
  protected String nodeLabel;
  protected SequenceView sequenceView;
  protected boolean isExisting;
  protected int linkCnt;


  /**
   * <code>PredicateNode</code> - constructor 
   *
   * @param predicateName - <code>String</code> - 
   * @param predicateLocation - <code>Point</code> - 
   * @param backgroundColor - <code>Color</code> - 
   * @param sequenceView - <code>SequenceView</code> - 
   */
  public PredicateNode( String predicateName, Point predicateLocation, Color backgroundColor,
                        SequenceView sequenceView) {
    super();
    this.predicateName = predicateName;
    this.predicateLocation = predicateLocation;
    this.sequenceView = sequenceView;
    nodeLabel = predicateName;
    linkCnt = 0;
    
    // System.err.println( "PredicateNode: " + nodeLabel);
    boolean isDraggable = true;
    configure( predicateLocation, backgroundColor, isDraggable);
  } // end constructor

  private final void configure( Point predicateLocation, Color backgroundColor,
                                boolean isDraggable) {
    boolean isRectangular = true;
    setLabelSpot( JGoObject.Center);
    initialize( predicateLocation, nodeLabel, isRectangular);
    setBrush( JGoBrush.makeStockBrush( backgroundColor));  
    getLabel().setEditable( false);
    setDraggable( isDraggable);
    // do not allow user links
    getPort().setVisible( false);
    getLabel().setMultiline( true);
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
   * <code>equals</code> - override equals so nodeList.contains( predicateNode) works
   *
   * @param node - <code>Object</code> - 
   * @return - <code>boolean</code> - 
   */
  public boolean equals( Object node) {
    return (this.getName().equals( ((PredicateNode) node).getName()));
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
   * <code>getLinkCnt</code>
   *
   * @return - <code>int</code> - 
   */
  public int getLinkCnt() {
    return linkCnt;
  }

  /**
   * <code>incrLinkCnt</code>
   *
   */
  public void incrLinkCnt() {
    linkCnt++;
  }

  /**
   * <code>getToolTipText</code>
   *
   * @return - <code>String</code> - 
   */
  public String getToolTipText() {
    return null;
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
    // System.err.println( "PredicateNode: doMouseClick obj class " +
    //                     obj.getTopLevelObject().getClass().getName());
    PredicateNode predicateNode = (PredicateNode) obj.getTopLevelObject();
    if (MouseEventOSX.isMouseLeftClick( modifiers, PlanWorks.isMacOSX())) {
      // do nothing
    } else if (MouseEventOSX.isMouseRightClick( modifiers, PlanWorks.isMacOSX())) {

      // mouseRightPopupMenu( viewCoords);

      return true;
    }
    return false;
  } // end doMouseClick   

  private void mouseRightPopupMenu( Point viewCoords) {
    JPopupMenu mouseRightPopup = new JPopupMenu();


    NodeGenerics.showPopupMenu( mouseRightPopup, sequenceView, viewCoords);
  } // end mouseRightPopupMenu



} // end class PredicateNode
