// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: TransactionField.java,v 1.4 2003-11-18 23:54:15 taylor Exp $
//
// PlanWorks
//
// Will Taylor -- started 14oct03
//

package gov.nasa.arc.planworks.viz.nodes;

import java.awt.Color;
import java.awt.Insets;
import java.awt.Point;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoBrush;
import com.nwoods.jgo.JGoObject;
import com.nwoods.jgo.JGoView;

// PlanWorks/java/lib/JGo/Classier.jar
import com.nwoods.jgo.examples.TextNode;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.PwPlanningSequence;
import gov.nasa.arc.planworks.util.MouseEventOSX;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.ViewGenerics;
import gov.nasa.arc.planworks.viz.VizView;
import gov.nasa.arc.planworks.viz.viewMgr.ViewableObject;


/**
 * <code>TransactionField</code> - JGo widget to render an field of a transaction entry

 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *       NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class TransactionField extends TextNode {

  // top left bottom right
  private static final Insets NODE_INSETS =
    new Insets( ViewConstants.TIMELINE_VIEW_INSET_SIZE_HALF,
                ViewConstants.TIMELINE_VIEW_INSET_SIZE,
                ViewConstants.TIMELINE_VIEW_INSET_SIZE_HALF,
                ViewConstants.TIMELINE_VIEW_INSET_SIZE);

  private String fieldName;
  private ViewableObject viewableObject; // PwPartialPlan or PwPlanningSequence
  private VizView vizView;
  private boolean fieldIsStepNumber;

  /**
   * <code>TransactionField</code> - constructor 
   *
   * @param fieldName - <code>String</code> - 
   * @param location - <code>Point</code> - 
   * @param alignment - <code>int</code> - 
   * @param bgColor - <code>Color</code> - 
   * @param viewableObject - <code>ViewableObject</code> - 
   */
  public TransactionField( String fieldName, Point location, int alignment, Color bgColor,
                           ViewableObject viewableObject) {
    super( fieldName);
    this.fieldName = fieldName;
    this.viewableObject = viewableObject;
    fieldIsStepNumber = false;
    this.vizView = null;

    configure( location, alignment, bgColor);
  } // end constructor


  /**
   * <code>TransactionField</code> - constructor 
   *
   * @param fieldName - <code>String</code> - 
   * @param location - <code>Point</code> - 
   * @param alignment - <code>int</code> - 
   * @param bgColor - <code>Color</code> - 
   * @param viewableObject - <code>ViewableObject</code> - 
   * @param vizView - <code>VizView</code> - 
   */
  public TransactionField( String fieldName, Point location, int alignment, Color bgColor,
                           ViewableObject viewableObject, VizView vizView) {
    super( fieldName);
    this.fieldName = fieldName;
    this.viewableObject = viewableObject;
    fieldIsStepNumber = true;
    this.vizView = vizView;

    configure( location, alignment, bgColor);
  } // end constructor


  private void configure( Point location, int alignment, Color bgColor) {
    setBrush( JGoBrush.makeStockBrush( bgColor));
    getLabel().setEditable( false);
    getLabel().setBold( false);
    getLabel().setMultiline( false);
    getLabel().setAlignment( alignment);
    setDraggable( false);
    // do not allow user links
    getTopPort().setVisible( false);
    getLeftPort().setVisible( false);
    getBottomPort().setVisible( false);
    getRightPort().setVisible( false);
    setLocation( (int) location.getX(), (int) location.getY());
    // setInsets( NODE_INSETS);
  } // end configureTransactionField


  /**
   * <code>getToolTipText</code>
   *
   * @return - <code>String</code> - 
   */
  public String getToolTipText() {
    return "";
  } // end getToolTipText

  /**
   * <code>doMouseClick</code> - 
   *
   * @param modifiers - <code>int</code> - 
   * @param docCoords - <code>Point</code> - 
   * @param viewCoords - <code>Point</code> - 
   * @param view - <code>JGoView</code> - 
   * @return - <code>boolean</code> - 
   */
  public boolean doMouseClick( int modifiers, Point docCoords, Point viewCoords,
                               JGoView view) {
    JGoObject obj = view.pickDocObject( docCoords, false);
    //         System.err.println( "doMouseClick obj class " +
    //                             obj.getTopLevelObject().getClass().getName());
    TransactionField transactionField = (TransactionField) obj.getTopLevelObject();
    if (MouseEventOSX.isMouseLeftClick( modifiers, PlanWorks.isMacOSX())) {

    } else if (MouseEventOSX.isMouseRightClick( modifiers, PlanWorks.isMacOSX())) {
      if (fieldIsStepNumber && (viewableObject instanceof PwPlanningSequence)) {
        ViewGenerics.partialPlanViewsPopupMenu( Integer.parseInt( fieldName),
                                                (PwPlanningSequence) viewableObject,
                                                vizView, viewCoords);
        return true;
      }
    }
    return false;
  } // end doMouseClick   



} // end class TransactionField
