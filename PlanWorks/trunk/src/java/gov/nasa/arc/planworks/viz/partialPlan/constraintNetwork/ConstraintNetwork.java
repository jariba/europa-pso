// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: ConstraintNetwork.java,v 1.3 2004-03-12 23:22:57 miatauro Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 07aug03
//

package gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoObject;

// PlanWorks/java/lib/JGo/Classier.jar
import com.nwoods.jgo.examples.BasicNode;

// PlanWorks/java/lib/JGo/JGoLayout.jar
import com.nwoods.jgo.layout.JGoNetwork;
import com.nwoods.jgo.layout.JGoNetworkLink;
import com.nwoods.jgo.layout.JGoNetworkNode;

import gov.nasa.arc.planworks.viz.nodes.BasicNodeLink;

/**
 * <code>ConstraintNetwork</code> - subclass JGoNetwork
 *                 derived from com.nwoods.jgo.examples.FamilyNetwork;
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *         NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class ConstraintNetwork extends JGoNetwork {

  /**
   * <code>ConstraintNetwork</code> - constructor 
   *
   */
  public ConstraintNetwork() {
    super();
  } // end constructor

  /**
   * <code>addConstraintNode</code>
   *
   * @param pGoObject - <code>JGoObject</code> - 
   */
  public void addConstraintNode( JGoObject pGoObject) {
    JGoNetworkNode pNetworkNode = findNode( pGoObject);
    if (pNetworkNode == null) {
      pNetworkNode = new JGoNetworkNode( this, pGoObject);
    }
    addNode( pNetworkNode);
  } // end addConstraintNode


  /**
   * <code>removeConstraintNode</code>
   *
   * @param pGoObject - <code>JGoObject</code> - 
   */
  public void removeConstraintNode( JGoObject pGoObject) {
    JGoNetworkNode node = findNode( pGoObject);
    deleteNode( node);
  } // end removeConstraintNode


  /**
   * <code>addConstraintLink</code>
   *
   * @param pGoObject - <code>JGoObject</code> - 
   * @param pFromObject - <code>JGoObject</code> - 
   * @param pToObject - <code>JGoObject</code> - 
   */
  public void addConstraintLink( JGoObject pGoObject, JGoObject pFromObject,
                                 JGoObject pToObject) {
    JGoNetworkLink pNetworkLink = findLink( pGoObject);
    if (pNetworkLink == null) {
      JGoNetworkNode pFromNode = (JGoNetworkNode) getGoObjToNodeMap().get( pFromObject);
      JGoNetworkNode pToNode = (JGoNetworkNode) getGoObjToNodeMap().get( pToObject);
      pNetworkLink = linkNodes( pFromNode, pToNode, pGoObject);
//       System.err.println( "ADD_CONSTRAINT_LINK: from " +
//                           ((BasicNode) pFromNode.getJGoObject()).getLabel().getText() +
//                           " to " +
//                           ((BasicNode) pToNode.getJGoObject()).getLabel().getText());
    }
  } // end addConstraintLink

  /**
   * <code>removeConstraintLink</code>
   *
   * @param pGoObject - <code>JGoObject</code> - 
   */
  public void removeConstraintLink( JGoObject pGoObject) {
    JGoNetworkLink link = findLink( pGoObject);
    deleteLink( link);
  } // end removeConstraintLink

  /**
   * <code>validateConstraintNetwork</code>
   *
   */
  public void validateConstraintNetwork() {
    JGoNetworkNode[] nodeArray = getNodeArray();
    for (int i = 0, n = nodeArray.length; i < n; i++) {
      String nodeName =
        ((BasicNode) ((JGoNetworkNode) nodeArray[i]).getJGoObject()).getLabel().getText();
      System.err.println( "network nodeName " + nodeName);
    }
    JGoNetworkLink[] linkArray = getLinkArray();
    for (int i = 0, n = linkArray.length; i < n; i++) {
      String linkName =
        ((BasicNodeLink) ((JGoNetworkLink) linkArray[i]).getJGoObject()).toString();
      System.err.println( "network linkName " + linkName);
    }


  } // end validateConstraintNetwork


} // end class ConstraintNetwork
