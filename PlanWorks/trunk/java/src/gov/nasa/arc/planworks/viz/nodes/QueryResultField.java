// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: QueryResultField.java,v 1.2 2004-04-22 19:26:22 taylor Exp $
//
// PlanWorks
//
// Will Taylor -- started 18dec03
//

package gov.nasa.arc.planworks.viz.nodes;

import java.awt.Color;
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
import gov.nasa.arc.planworks.viz.ViewGenerics;
import gov.nasa.arc.planworks.viz.ViewListener;
import gov.nasa.arc.planworks.viz.VizView;
import gov.nasa.arc.planworks.viz.viewMgr.ViewableObject;


/**
 * <code>QueryResultField</code> - JGo widget to render a field of a query results entry

 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *       NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class QueryResultField extends TextNode {

  private String fieldName;
  private ViewableObject viewableObject; // PwPlanningSequence
  private VizView vizView;
  private boolean fieldIsStepNumber;
  private ViewListener viewListener;

  /**
   * <code>QueryResultField</code> - constructor 
   *
   * @param fieldName - <code>String</code> - 
   * @param location - <code>Point</code> - 
   * @param alignment - <code>int</code> - 
   * @param bgColor - <code>Color</code> - 
   * @param viewableObject - <code>ViewableObject</code> - 
   */
  public QueryResultField( String fieldName, Point location, int alignment, Color bgColor,
                           ViewableObject viewableObject) {
    super( fieldName);
    this.fieldName = fieldName;
    this.viewableObject = viewableObject;
    fieldIsStepNumber = false;
    this.vizView = null;
    this.viewListener = null;

    configure( location, alignment, bgColor);
  } // end constructor


  /**
   * <code>QueryResultField</code> - constructor 
   *
   * @param fieldName - <code>String</code> - 
   * @param location - <code>Point</code> - 
   * @param alignment - <code>int</code> - 
   * @param bgColor - <code>Color</code> - 
   * @param viewableObject - <code>ViewableObject</code> - 
   * @param vizView - <code>VizView</code> - 
   */
  public QueryResultField( String fieldName, Point location, int alignment, Color bgColor,
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
  } // end configure


  /**
   * <code>getToolTipText</code>
   *
   * @return - <code>String</code> - 
   */
  public String getToolTipText() {
    if (fieldIsStepNumber) {
      return "M-R: Open partial plan views";
    } else {
      return null;
    }
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
    if (MouseEventOSX.isMouseLeftClick( modifiers, PlanWorks.isMacOSX())) {

    } else if (MouseEventOSX.isMouseRightClick( modifiers, PlanWorks.isMacOSX())) {
      if (fieldIsStepNumber && (viewableObject instanceof PwPlanningSequence)) {
        ViewGenerics.partialPlanViewsPopupMenu( Integer.parseInt( fieldName),
                                                (PwPlanningSequence) viewableObject,
                                                vizView, viewCoords, viewListener);
        return true;
      }
    }
    return false;
  } // end doMouseClick   



} // end class QueryResultField
