// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: NodeGenerics.java,v 1.21 2004-03-09 01:48:28 taylor Exp $
//
// PlanWorks
//
// Will Taylor -- started 17sep03
//

package gov.nasa.arc.planworks.viz.nodes;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoArea;
import com.nwoods.jgo.JGoView;

// PlanWorks/java/lib/JGo/Classier.jar
import com.nwoods.jgo.examples.BasicNode;
import com.nwoods.jgo.examples.TextNode;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.DbConstants;
import gov.nasa.arc.planworks.db.PwDomain;
import gov.nasa.arc.planworks.db.PwEnumeratedDomain;
import gov.nasa.arc.planworks.db.PwSlot;
import gov.nasa.arc.planworks.db.PwToken;
import gov.nasa.arc.planworks.db.PwVariable;
import gov.nasa.arc.planworks.util.Utilities;
import gov.nasa.arc.planworks.viz.VizView;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanView;
import gov.nasa.arc.planworks.viz.partialPlan.temporalExtent.TemporalNode;


/**
 * <code>NodeGenerics</code> - generic static methods for node handling
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *       NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class NodeGenerics {

  private NodeGenerics() {
  }

  /**
   * <code>showPopupMenu</code> - show pop up menu in component at viewCoords location
   *
   * @param popupMenu - <code>JPopupMenu</code> - 
   * @param component - <code>JComponent</code> - 
   * @param viewCoords - <code>Point</code> - 
   */
  public static void showPopupMenu( JPopupMenu popupMenu, JComponent component,
                                    Point viewCoords) {
    boolean isLocationAbsolute = false;
    Point popupPoint = Utilities.computeNestedLocation( viewCoords, component,
                                                        isLocationAbsolute);
    popupMenu.show( PlanWorks.getPlanWorks(), (int) popupPoint.getX(),
                    (int) popupPoint.getY());
  } // end showPopupMenu

  /**
   * <code>getShortestDuration</code>
   *
   * @param isFreeToken - <code>boolean</code> - 
   * @param baseToken - <code>PwToken</code> - 
   * @param endTimeIntervalDomain - <code>PwDomain</code> - 
   * @return - <code>String</code> - 
   */
  public static String getShortestDuration( boolean isFreeToken, PwToken baseToken,
                                            PwDomain startTimeIntervalDomain,
                                            PwDomain endTimeIntervalDomain) {
    PwVariable durationVariable = null;
    if (isFreeToken ||
        (baseToken == null)) { // empty slot
      int startUpper = startTimeIntervalDomain.getUpperBoundInt();
      int endUpper = endTimeIntervalDomain.getUpperBoundInt();
      int startLower = startTimeIntervalDomain.getLowerBoundInt();
      int endLower = endTimeIntervalDomain.getLowerBoundInt();
      int valueStart = Math.max( startLower, startUpper);
      if (valueStart == DbConstants.PLUS_INFINITY_INT) {
        valueStart = Math.min( startLower, startUpper);
      }
      if (valueStart == DbConstants.MINUS_INFINITY_INT) {
        valueStart = 0;
      }
      int valueEnd = Math.min( endLower, endUpper);
      if (valueEnd == DbConstants.MINUS_INFINITY_INT) {
        valueEnd = Math.max( endLower, endUpper);
      }
      if (valueEnd == DbConstants.PLUS_INFINITY_INT) {
        valueEnd = valueStart;
      }
      return String.valueOf( Math.abs( valueEnd - valueStart));
    } else {
      durationVariable = baseToken.getDurationVariable();
      if (durationVariable != null) {
        return durationVariable.getDomain().getLowerBound();
      } else {
        return "0";
      }
    }
  } // end getShortestDuration

  /**
   * <code>getLongestDuration</code>
   *
   * @param isFreeToken - <code>boolean</code> - 
   * @param baseToken - <code>PwToken</code> - 
   * @param startTimeIntervalDomain - <code>PwDomain</code> - 
   * @param endTimeIntervalDomain - <code>PwDomain</code> - 
   * @return - <code>String</code> - 
   */
  public static String getLongestDuration( boolean isFreeToken, PwToken baseToken,
                                           PwDomain startTimeIntervalDomain,
                                           PwDomain endTimeIntervalDomain) {
    PwVariable durationVariable = null;
    if (isFreeToken ||  
        (baseToken == null)) { // empty slot
      String upperBound = endTimeIntervalDomain.getUpperBound();
      String lowerBound = startTimeIntervalDomain.getLowerBound();
      if (upperBound.equals( DbConstants.PLUS_INFINITY)) {
        return DbConstants.PLUS_INFINITY;
      } else if (lowerBound.equals( DbConstants.MINUS_INFINITY)) {
        return DbConstants.PLUS_INFINITY;
      } else {
        return String.valueOf( endTimeIntervalDomain.getUpperBoundInt() -
                               startTimeIntervalDomain.getLowerBoundInt());      
      }
    } else {
      durationVariable = baseToken.getDurationVariable();
      if (durationVariable != null) {
        return durationVariable.getDomain().getUpperBound();
      } else {
        return "0";
      }
    }
  } // end getLongestDuration


  /**
   * <code>focusViewOnNode</code> - scroll view's scroll bars to put node in middle,
   *                                and optionally highlight node
   *
   * @param node - <code>JGoArea</code> - 
   * @param isHighlightNode - <code>boolean</code> - 
   * @param jGoView - <code>JGoView</code> - 
   */
  public static void focusViewOnNode( JGoArea node, boolean isHighlightNode, JGoView jGoView) {
    // System.err.println( "focusViewOnNode: loc " + node.getLocation().getX() +
    //                     " extent " + jGoView.getExtentSize().getWidth());
//     if (node instanceof TokenNode) {
//       System.err.println( "focusViewOnNode: token " +
//                           ((TokenNode) node).toString());
//     } else if (node instanceof ConstraintNode) {
//       System.err.println( "focusViewOnNode: constraint " +
//                           ((ConstraintNode) node).toString());
//     } else if (node instanceof VariableNode) {
//       System.err.println( "focusViewOnNode: variable " +
//                           ((VariableNode) node).toString());
//     }
    jGoView.getSelection().clearSelection();
    if (node != null) {
      jGoView.getHorizontalScrollBar().
        setValue( Math.max( 0,
                            (int) (node.getLocation().getX() -
                                   (jGoView.getExtentSize().getWidth() / 2))));
      jGoView.getVerticalScrollBar().
        setValue( Math.max( 0,
                            (int) (node.getLocation().getY() -
                                   (jGoView.getExtentSize().getHeight() / 2))));
      if (isHighlightNode) {
        jGoView.getSelection().extendSelection( node);
      }
    } else {
      jGoView.getHorizontalScrollBar().setValue( 0);
      jGoView.getVerticalScrollBar().setValue( 0);
    }
  } // end focusViewOnNode


  /**
   * <code>selectSecondaryNodes</code> - highlight the secondary nodes.
   *                        Assumes that focusViewOnNode is called first,
   *                        to set the primary selection
   *
   * @param nodeList - <code>List of JGoArea (e.g. TokenNode)</code> -
   * @param jGoView - <code>JGoView</code> - 
   */
  public static void selectSecondaryNodes( List nodeList, JGoView jGoView) {
    if (nodeList != null) {
      Iterator nodeIterator = nodeList.iterator();
      while (nodeIterator.hasNext()) {
        jGoView.getSelection().extendSelection( (JGoArea) nodeIterator.next());
      }
    }
  } // end selectSecondaryNodes


  /**
   * <code>mapTokensToTokenNodes</code> - given a list of tokens and token nodes,
   *                           return a subset of the token node list, which match
   *                           the token list
   *
   * @param tokenList - <code>List of PwToken</code> - 
   * @param viewTokenNodeList - <code>List of BasicNode</code> - TemporalNode/TokenNode
   * @return - <code>List of BasicNode</code> - 
   */
  public static List mapTokensToTokenNodes( List tokenList, List viewTokenNodeList) {
    List basicNodeList = new ArrayList();
    if (tokenList != null) {
      Iterator viewTokenNodeItr = viewTokenNodeList.iterator();
      while (viewTokenNodeItr.hasNext()) {
        BasicNode basicNode = (BasicNode) viewTokenNodeItr.next();
        PwToken token = null;
        if (basicNode instanceof TemporalNode) {
          token = ((TemporalNode) basicNode).getToken();
        } else if (basicNode instanceof TokenNode) {
          token = ((TokenNode) basicNode).getToken();
        } else {
          continue; // not a token node
        }
        Iterator tokenItr = tokenList.iterator();
        while (tokenItr.hasNext()) {
          if (((PwToken) tokenItr.next()).getId() == token.getId()) {
            basicNodeList.add( basicNode);
            break;
          }
        }
      }
    }
    return basicNodeList;
  } // end mapTokensToTokenNodes


  /**
   * <code>trimName</code> - trim name to fit in headerNode column
   *
   * @param name - <code>String</code> - 
   * @param headerNode - <code>TextNode</code> - 
   * @param vizView - <code>vizView</code> - 
   * @return - <code>String</code> - 
   */
  public static String trimName( String name, TextNode headerNode, VizView vizView) {
    int columnWidth = (int) headerNode.getSize().getWidth();
    // System.err.println(name);
    int nameWidth = SwingUtilities.computeStringWidth( vizView.getFontMetrics(), name);
    // System.err.println( " name " + name + " " + nameWidth + " columnWidth " + columnWidth);
    if (nameWidth > columnWidth) {
      int numTrimChars = ((nameWidth - columnWidth) /
                          vizView.getFontMetrics().charWidth( 'A')) + 1;
      // System.err.println( " numTrimChars " + numTrimChars);
      name = name.substring( 0, (name.length() - numTrimChars - 2)).concat( "..");
    }
    // System.err.println( " name " + name);
    return name;
  } // end trimName

  /**
   * <code>getSlotNodeToolTipText</code>
   *
   * @param slot - <code>PwSlot</code> - 
   * @param tip - <code>StringBuffer</code> - 
   */
  public static void getSlotNodeToolTipText( PwSlot slot, StringBuffer tip) {
    List tokenList = slot.getTokenList();
    tip.append( ((PwToken) tokenList.get( 0)).toString());
    tip.append( "<br>token key");
    if (tokenList.size() > 1) {
      tip.append( "s");
    }
    tip.append( "=");
    Iterator tokenItr = slot.getTokenList().iterator();
    while (tokenItr.hasNext()) {
      tip.append( ((PwToken) tokenItr.next()).getId().toString());
      if (tokenItr.hasNext()) {
        tip.append( ", ");
      }
    }
  } // end getSlotNodeToolTipText

  /**
   * <code>getVariableNodeToolTipText</code>
   *
   * @param variable - <code>PwVariable</code> - 
   * @param tip - <code>StringBuffer</code> - 
   */
  public static void getVariableNodeToolTipText( PwVariable variable,
                                                 PartialPlanView partialPlanView,
                                                 StringBuffer tip) {
    String typeName = variable.getType();
//     System.err.println( "getVariableNodeToolTipText id " + variable.getId().toString() +
//                         " typeName '" + typeName + "'");
    tip.append( typeName);
    if (typeName.equals( DbConstants.OBJECT_VAR)) {
      //String objectName = "_not_found_";
      //Integer objectId =
      //  Integer.valueOf( ((PwEnumeratedDomain) variable.getDomain()).getLowerBound());
      //Iterator objectIterator = partialPlanView.getPartialPlan().getObjectList().iterator();
      //while (objectIterator.hasNext()) {
      //  PwObject object = (PwObject) objectIterator.next();
      //  if (object.getId().equals( objectId)) {
      //    objectName = object.getName();
      //    break;
      //  }
      //}
      //tip.append ( ": ");
      //tip.append( objectName);
      tip.append("<br>");
      ListIterator objectNameIterator = 
        ((PwEnumeratedDomain) variable.getDomain()).getEnumeration().listIterator();;
      while(objectNameIterator.hasNext()) {
        String name = (String) objectNameIterator.next();
        // tip.append( name).append( ": ");
        tip.append( "value: ");
        tip.append( partialPlanView.getPartialPlan().getObjectIdByName( name));
        if (objectNameIterator.hasNext()) {
          tip.append("<br>");
        }
      }
    } else if (typeName.equals( DbConstants.PARAMETER_VAR)) {
      tip.append ( ": ");
//       System.err.println( "key " + variable.getId().toString() + 
//                           " paramName " + variable.getParameterNameList().get( 0));
      tip.append( variable.getParameterNameList().get( 0));
    }
  } // end getVariableNodeToolTipText


} // end class NodeGenerics

