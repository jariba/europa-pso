// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: NodeGenerics.java,v 1.7 2003-10-28 22:14:27 miatauro Exp $
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
import gov.nasa.arc.planworks.db.PwSlot;
import gov.nasa.arc.planworks.db.PwToken;
import gov.nasa.arc.planworks.db.PwVariable;
import gov.nasa.arc.planworks.util.Utilities;
import gov.nasa.arc.planworks.viz.VizView;
import gov.nasa.arc.planworks.viz.nodes.TokenNode;
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
    popupMenu.show( PlanWorks.planWorks, (int) popupPoint.getX(),
                    (int) popupPoint.getY());
  } // end showPopupMenu

  /**
   * <code>getStartEndIntervals</code>
   *
   * use startVariable for every token + the endVariable for the last one
   * if a slot is empty, and has no tokens, use the endVariable from
   * the previous slot node
   *
   * @param view - <code>VizView</code> - 
   * @param slot - <code>PwSlot</code> - 
   * @param previousSlot - <code>PwSlot</code> - 
   * @param isLastSlot - <code>boolean</code> - 
   * @param alwaysReturnEnd - <code>boolean</code> - 
   * @return - <code>PwDomain[]</code> - 
   */
  public static PwDomain[] getStartEndIntervals( VizView view, PwSlot slot,
                                                 PwSlot previousSlot,
                                                 boolean isLastSlot,
                                                 boolean alwaysReturnEnd) {
    PwDomain[] intervalArray = new PwDomain[2];
    PwDomain startIntervalDomain = null;
    PwDomain endIntervalDomain = null;
    PwVariable intervalVariable = null, lastIntervalVariable = null;
    PwToken baseToken = slot.getBaseToken();
    PwToken previousToken = null;
    if (previousSlot != null) {
      previousToken = previousSlot.getBaseToken();
    }
    PwDomain intervalDomain = null, lastIntervalDomain = null;
    if (baseToken == null) { // empty slot
      if ((previousToken == null) || // first slot
          (isLastSlot == true)) { // last slot
        intervalArray[0] = null;
        intervalArray[1] = null;
        return intervalArray;
     } else {
        // empty slot between filled slots
        intervalVariable = previousToken.getEndVariable();
      }
    } else if (isLastSlot || alwaysReturnEnd) {
      intervalVariable = baseToken.getStartVariable();
      lastIntervalVariable = baseToken.getEndVariable();
    } else {
      intervalVariable = baseToken.getStartVariable();      
    }

    if (intervalVariable == null) {
      startIntervalDomain = intervalDomain;
    } else {
      startIntervalDomain = intervalVariable.getDomain();
    }

    if ((lastIntervalVariable != null) || (lastIntervalDomain != null)) {
      if (lastIntervalVariable == null) {
        endIntervalDomain = lastIntervalDomain;
      } else {
        endIntervalDomain = lastIntervalVariable.getDomain();
      }
    }
    if (alwaysReturnEnd && (endIntervalDomain == null)) {
      endIntervalDomain = startIntervalDomain;
    }
//     System.err.println( "getStartEndIntervals: " + slot.getBaseToken());
//     System.err.println( "  startIntervalDomain " + startIntervalDomain.toString());
//     System.err.println( "  endIntervalDomain " + endIntervalDomain.toString());
    intervalArray[0] = startIntervalDomain;
    intervalArray[1] = endIntervalDomain;
    return intervalArray;
  } // end getStartEndIntervals

  /**
   * <code>getShortestDuration</code>
   *
   * @param slot - <code>PwSlot</code> - 
   * @param startTimeIntervalDomain - <code>PwDomain</code> - 
   * @param endTimeIntervalDomain - <code>PwDomain</code> - 
   * @return - <code>String</code> - 
   */
  public static String getShortestDuration( PwSlot slot,
                                            PwDomain startTimeIntervalDomain,
                                            PwDomain endTimeIntervalDomain) {
    PwVariable durationVariable = null;
    if ((slot == null) ||  // free token
        (slot.getBaseToken() == null)) { // empty slot
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
      durationVariable = slot.getBaseToken().getDurationVariable();
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
   * @param slot - <code>PwSlot</code> - 
   * @param startTimeIntervalDomain - <code>PwDomain</code> - 
   * @param endTimeIntervalDomain - <code>PwDomain</code> - 
   * @return - <code>String</code> - 
   */
  public static String getLongestDuration( PwSlot slot,
                                           PwDomain startTimeIntervalDomain,
                                           PwDomain endTimeIntervalDomain) {
    PwVariable durationVariable = null;
    if ((slot == null) ||  // free token
        (slot.getBaseToken() == null)) { // empty slot
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
      durationVariable = slot.getBaseToken().getDurationVariable();
      if (durationVariable != null) {
        return durationVariable.getDomain().getUpperBound();
      } else {
        return "0";
      }
    }
  } // end getLongestDuration


  /**
   * <code>focusViewOnNode</code> - scroll view's scroll bars to put node in middle
   *
   * @param node - <code>JGoArea</code> - 
   * @param jGoView - <code>JGoView</code> - 
   */
  public static void focusViewOnNode( JGoArea node, JGoView jGoView) {
    // System.err.println( "focusViewOnNode: loc " + node.getLocation().getX() +
    //                     " extent " + jGoView.getExtentSize().getWidth());
    jGoView.getHorizontalScrollBar().
      setValue( Math.max( 0,
                          (int) (node.getLocation().getX() -
                                 (jGoView.getExtentSize().getWidth() / 2))));
    jGoView.getVerticalScrollBar().
      setValue( Math.max( 0,
                          (int) (node.getLocation().getY() -
                                 (jGoView.getExtentSize().getHeight() / 2))));
    jGoView.getSelection().clearSelection();
    jGoView.getSelection().extendSelection( node);
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
   * @return - <code>String</code> - 
   */
  public static String trimName( String name, TextNode headerNode, VizView vizView) {
    int columnWidth = (int) headerNode.getSize().getWidth();
    System.err.println(name);
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


} // end class NodeGenerics

