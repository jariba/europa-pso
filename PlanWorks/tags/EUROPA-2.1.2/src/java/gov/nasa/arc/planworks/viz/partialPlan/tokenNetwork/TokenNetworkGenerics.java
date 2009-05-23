// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: TokenNetworkGenerics.java,v 1.6 2006-10-03 16:14:17 miatauro Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 22feb04
//

package gov.nasa.arc.planworks.viz.partialPlan.tokenNetwork;

import java.awt.Color;
import java.util.Iterator;

import gov.nasa.arc.planworks.db.DbConstants;
import gov.nasa.arc.planworks.db.PwEntity;
import gov.nasa.arc.planworks.db.PwObject;
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.PwRuleInstance;
import gov.nasa.arc.planworks.db.PwTimeline;
import gov.nasa.arc.planworks.db.PwToken;
import gov.nasa.arc.planworks.db.PwSlot;
import gov.nasa.arc.planworks.db.PwVariable;
import gov.nasa.arc.planworks.db.PwVariableContainer;
import gov.nasa.arc.planworks.util.ColorMap;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.nodes.BasicNodeLink;
import gov.nasa.arc.planworks.viz.nodes.ExtendedBasicNode;
import gov.nasa.arc.planworks.viz.nodes.IncrementalNode;


/**
 * <code>TokenNetworkGenerics</code> - general static methods for IncrementalNode classes
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                    NASA Ames Research Center - Code IC
 * @version 0.0
 */
public final class TokenNetworkGenerics {

  private TokenNetworkGenerics() {
  }

  /**
   * <code>addEntityTokNetNodes</code>
   *
   * @param tokNetNode - <code>IncrementalNode</code> - 
   * @param tokenNetworkView - <code>TokenNetworkView</code> - 
   * @param isDebugPrint - <code>boolean</code> - 
   * @return - <code>boolean</code> - 
   */
  public static boolean addEntityTokNetNodes( final IncrementalNode tokNetNode,
                                           final TokenNetworkView tokenNetworkView,
                                           final boolean isDebugPrint) {
    boolean areNodesChanged = false;
    // System.err.println( "addEntityTokNetNodes entityNode " + tokNetNode.getId());
    Iterator parentEntityItr = tokNetNode.getParentEntityList().iterator();
    while (parentEntityItr.hasNext()) {
      PwEntity parentEntity = (PwEntity) parentEntityItr.next();
      // System.err.println( "   addEntityTokNetNodes parentEntity " + parentEntity.getId());
      tokenNetworkView.addEntityTokNetNode( parentEntity, isDebugPrint);
      areNodesChanged = true;
    }
    Iterator childEntityItr = tokNetNode.getComponentEntityList().iterator();
    while (childEntityItr.hasNext()) {
      PwEntity childEntity = (PwEntity) childEntityItr.next();
      // System.err.println( "   addEntityTokNetNodes childEntity " + childEntity.getId());
      tokenNetworkView.addEntityTokNetNode( childEntity, isDebugPrint);
      areNodesChanged = true;
    }
    return areNodesChanged;
  } // end addEntityTokNetNodes

  /**
   * <code>removeEntityTokNetNodes</code>
   *
   * @param tokNetNode - <code>IncrementalNode</code> - 
   * @param tokenNetworkView - <code>TokenNetworkView</code> - 
   * @param isDebugPrint - <code>boolean</code> - 
   * @return - <code>boolean</code> - 
   */
  public static boolean removeEntityTokNetNodes( final IncrementalNode tokNetNode,
                                              final TokenNetworkView tokenNetworkView,
                                              final boolean isDebugPrint) {
    boolean areNodesChanged = false;
    Iterator parentEntityItr = tokNetNode.getParentEntityList().iterator();
    while (parentEntityItr.hasNext()) {
      PwEntity parentEntity = (PwEntity) parentEntityItr.next();
      IncrementalNode parentTokNetNode = getTokNetNode( parentEntity.getId(), tokenNetworkView);
      if (isDebugPrint) {
	System.err.println( "removeEntityTokNetNodes: parent id = " +
			    parentTokNetNode.getId() + " linkCount " +
			    parentTokNetNode.getLinkCount());
      }
      if ((parentTokNetNode != null) && parentTokNetNode.inLayout() &&
          (parentTokNetNode.getLinkCount() == 0)) {
        removeTokNetNode( parentTokNetNode, tokenNetworkView, isDebugPrint);
        areNodesChanged = true;
      }
    }
    Iterator childEntityItr = tokNetNode.getComponentEntityList().iterator();
    while (childEntityItr.hasNext()) {
      PwEntity childEntity = (PwEntity) childEntityItr.next();
      IncrementalNode childTokNetNode = getTokNetNode( childEntity.getId(), tokenNetworkView);
      if (isDebugPrint) {
	System.err.println( "removeEntityTokNetNodes: child id = " +
			    childTokNetNode.getId() + " linkCount " +
			    childTokNetNode.getLinkCount());
      }
      if ((childTokNetNode != null) && childTokNetNode.inLayout() &&
          (childTokNetNode.getLinkCount() == 0)) {
        removeTokNetNode( childTokNetNode, tokenNetworkView, isDebugPrint);
        areNodesChanged = true;
      }
    }
    if (isDebugPrint) {
      System.err.println( "removeEntityTokNetNodes: id = " + tokNetNode.getId() +
			  " linkCount " + tokNetNode.getLinkCount());
    }
    if (tokNetNode.inLayout() && (tokNetNode.getLinkCount() == 0)) {
      removeTokNetNode( tokNetNode, tokenNetworkView, isDebugPrint);
      areNodesChanged = true;
    }
    return areNodesChanged;
  } // removeEntityTokNetNodes

  /**
   * <code>getTokNetNode</code> 
   *
   * @param id - <code>Integer</code> - 
   * @param tokenNetworkView - <code>TokenNetworkView</code> - 
   * @return - <code>IncrementalNode</code> - 
   */
  public static IncrementalNode getTokNetNode( final Integer id,
                                            final TokenNetworkView tokenNetworkView) {
    return (IncrementalNode) tokenNetworkView.entityTokNetNodeMap.get( id);
  }

  /**
   * <code>removeTokNetNode</code>
   *
   * @param tokNetNode - <code>IncrementalNode</code> - 
   * @param isDebugPrint - <code>boolean</code> - 
   */
  public static void removeTokNetNode( final IncrementalNode tokNetNode,
                                       final TokenNetworkView tokenNetworkView,
                                       final boolean isDebugPrint) {
    if (isDebugPrint) {
      System.err.println( "remove " + tokNetNode.getTypeName() + " TokNetNode = " +
                          tokNetNode.getId());
    }
      System.err.println( "remove " + tokNetNode.getTypeName() + " TokNetNode = " +
                          tokNetNode.getId());
    tokNetNode.setInLayout( false);
    tokNetNode.resetNode( isDebugPrint);
    tokenNetworkView.getJGoView().removeObject( (ExtendedBasicNode) tokNetNode);
  } // end removeTokNetNode


  /**
   * <code>addParentToEntityTokNetLinks</code>
   *
   * @param tokNetNode - <code>IncrementalNode</code> - 
   * @param tokenNetworkView - <code>TokenNetworkView</code> - 
   * @param isDebugPrint - <code>boolean</code> - 
   * @return - <code>boolean</code> - 
   */
  public static boolean addParentToEntityTokNetLinks( final IncrementalNode tokNetNode,
                                                   final TokenNetworkView tokenNetworkView,
                                                   final boolean isDebugPrint) {
    boolean areLinksChanged = false;
    Iterator parentEntityItr = tokNetNode.getParentEntityList().iterator();
    while (parentEntityItr.hasNext()) {
      PwEntity parentEntity = (PwEntity) parentEntityItr.next();
      IncrementalNode parentTokNetNode =
        (IncrementalNode) tokenNetworkView.entityTokNetNodeMap.get( parentEntity.getId());
      if (parentTokNetNode != null) { 
        String linkType = getLinkType( parentTokNetNode, tokNetNode);
        if (parentTokNetNode.inLayout() &&
            addTokNetLink( parentTokNetNode, tokNetNode, linkType, tokNetNode, tokenNetworkView,
                        isDebugPrint)) {
          areLinksChanged = true;
        }
      }
    }
    return areLinksChanged;
  } // end addParentToEntityTokNetLinks

  /**
   * <code>addEntityToChildTokNetLinks</code>
   *
   * @param tokNetNode - <code>IncrementalNode</code> - 
   * @param tokenNetworkView - <code>TokenNetworkView</code> - 
   * @param isDebugPrint - <code>boolean</code> - 
   * @return - <code>boolean</code> - 
   */
  public static boolean addEntityToChildTokNetLinks( final IncrementalNode tokNetNode,
                                                  final TokenNetworkView tokenNetworkView,
                                                  final boolean isDebugPrint) {
    boolean areLinksChanged = false;
    Iterator childEntityItr = tokNetNode.getComponentEntityList().iterator();
    while (childEntityItr.hasNext()) {
      PwEntity childEntity = (PwEntity) childEntityItr.next();
      IncrementalNode childTokNetNode =
        (IncrementalNode) tokenNetworkView.entityTokNetNodeMap.get( childEntity.getId());
      String linkType = getLinkType( tokNetNode, childTokNetNode);
      if (childTokNetNode.inLayout() &&
          addTokNetLink( tokNetNode, childTokNetNode, linkType, tokNetNode, tokenNetworkView,
                      isDebugPrint)) {
        areLinksChanged = true;
      }
    }
    return areLinksChanged;
  } // end addEntityToChildTokNetLinks

  /**
   * <code>addTokNetLink</code>
   *
   * @param fromTokNetNode - <code>IncrementalNode</code> - 
   * @param toTokNetNode - <code>IncrementalNode</code> - 
   * @param linkType - <code>String</code> - 
   * @param sourceNode - <code>ExtendedBasicNode</code> - 
   * @return - <code>boolean</code> - 
   */
  public static boolean addTokNetLink( final IncrementalNode fromTokNetNode,
                                    final IncrementalNode toTokNetNode,
                                    final String linkType, final IncrementalNode sourceNode,
                                    final TokenNetworkView tokenNetworkView,
                                    final boolean isDebugPrint) {
    BasicNodeLink returnLink = null;
    String linkName = fromTokNetNode.getId().toString() + "->" + toTokNetNode.getId().toString();
    BasicNodeLink link = (BasicNodeLink) tokenNetworkView.tokNetLinkMap.get( linkName);
    if (link == null) {
      link = new BasicNodeLink( (ExtendedBasicNode) fromTokNetNode,
                                (ExtendedBasicNode) toTokNetNode, linkName, linkType);
      link.setArrowHeads( false, true);
      returnLink = link;
      tokenNetworkView.tokNetLinkMap.put( linkName, link);
      if (isDebugPrint) {
        System.err.println( "add " + fromTokNetNode.getTypeName() + "=>" +
                            toTokNetNode.getTypeName() + " link " + linkName);
      }
    } else {
      if (! link.inLayout()) {
        link.setInLayout( true);
      }
      link.incrLinkCount();
      if (isDebugPrint) {
        System.err.println( linkType + "1 incr link: " + link.toString() + " to " +
                            link.getLinkCount());
      }
    }
    fromTokNetNode.incrLinkCount();
    toTokNetNode.incrLinkCount();
    return tokenNetworkView.addTokenNetworkLinkNew( (ExtendedBasicNode) fromTokNetNode,
                                                    returnLink, linkType);
  } // end addTokNetLink


  /**
   * <code>removeParentToEntityTokNetLinks</code>
   *
   * @param tokNetNode - <code>IncrementalNode</code> - 
   * @param tokenNetworkView - <code>TokenNetworkView</code> - 
   * @param isDebugPrint - <code>boolean</code> - 
   * @return - <code>boolean</code> - 
   */
  public static boolean removeParentToEntityTokNetLinks( final IncrementalNode tokNetNode,
                                                      final TokenNetworkView tokenNetworkView,
                                                      final boolean isDebugPrint) { 
    boolean areLinksChanged = false;
    Iterator parentEntityItr = tokNetNode.getParentEntityList().iterator();
    while (parentEntityItr.hasNext()) {
      PwEntity parentEntity = (PwEntity) parentEntityItr.next();
      IncrementalNode parentTokNetNode =
        (IncrementalNode) tokenNetworkView.entityTokNetNodeMap.get( parentEntity.getId());
      if (parentTokNetNode != null) { 
        if (parentTokNetNode.inLayout()) {
          String linkName = parentTokNetNode.getId().toString() + "->" +
            tokNetNode.getId().toString();
          BasicNodeLink link = (BasicNodeLink) tokenNetworkView.tokNetLinkMap.get( linkName);
          String linkType = getLinkType( parentTokNetNode, tokNetNode);
          if ((link != null) && link.inLayout() &&
              removeTokNetLink( link, parentTokNetNode, tokNetNode, linkType, isDebugPrint)) {
            areLinksChanged = true;
          }
        }
      }
    }
    return areLinksChanged;
  } // end addParentToEntityTokNetLinks

  /**
   * <code>removeEntityToChildTokNetLinks</code>
   *
   * @param tokNetNode - <code>IncrementalNode</code> - 
   * @param tokenNetworkView - <code>TokenNetworkView</code> - 
   * @param isDebugPrint - <code>boolean</code> - 
   * @return - <code>boolean</code> - 
   */
  public static boolean removeEntityToChildTokNetLinks( final IncrementalNode tokNetNode,
                                                     final TokenNetworkView tokenNetworkView,
                                                     final boolean isDebugPrint) {
    boolean areLinksChanged = false;
    Iterator childEntityItr = tokNetNode.getComponentEntityList().iterator();
    while (childEntityItr.hasNext()) {
      PwEntity childEntity = (PwEntity) childEntityItr.next();
      IncrementalNode childTokNetNode =
        (IncrementalNode) tokenNetworkView.entityTokNetNodeMap.get( childEntity.getId());
      if (childTokNetNode != null) { 
        if (childTokNetNode.inLayout()) {
          String linkName = tokNetNode.getId().toString() + "->" +
            childTokNetNode.getId().toString();
          String linkType = getLinkType( tokNetNode, childTokNetNode);
          BasicNodeLink link = (BasicNodeLink) tokenNetworkView.tokNetLinkMap.get( linkName);
          if ((link != null) && link.inLayout() &&
              removeTokNetLink( link, tokNetNode, childTokNetNode, linkType, isDebugPrint)) {
            areLinksChanged = true;
          }
        }
      }
    }
    return areLinksChanged;
  } // end removeEntityToChildTokNetLinks


  /**
   * <code>removeTokNetLink</code>
   *
   * @param link - <code>BasicNodeLink</code> - 
   * @param fromTokNetNode - <code>IncrementalNode</code> - 
   * @param toTokNetNode - <code>IncrementalNode</code> - 
   * @param linkType - <code>String</code> - 
   * @return - <code>boolean</code> - 
   */
  public static boolean removeTokNetLink( final BasicNodeLink link,
                                          final IncrementalNode fromTokNetNode,
                                          final IncrementalNode toTokNetNode,
                                          final String linkType,
                                          final boolean isDebugPrint) {
    boolean areLinksChanged = false;
    link.decLinkCount();
    fromTokNetNode.decLinkCount();
    toTokNetNode.decLinkCount();
    if (isDebugPrint) {
      System.err.println( linkType + " dec link: " + link.toString() + " to " +
                          link.getLinkCount());
    }
    if (link.getLinkCount() == 0) {
      if (isDebugPrint) {
        System.err.println( "remove " + fromTokNetNode.getTypeName() + "=>" +
                            toTokNetNode.getTypeName() + ": " + link.toString());
      }
      link.setInLayout( false);
      areLinksChanged = true;
    }
    return areLinksChanged;
  } // end removeTokNetLink


  /**
   * <code>getLinkType</code>
   *
   * @param fromTokNetNode - <code>IncrementalNode</code> - 
   * @param toTokNetNode - <code>IncrementalNode</code> - 
   * @return - <code>String</code> - 
   */
  protected static String getLinkType( final IncrementalNode fromTokNetNode,
                                       final IncrementalNode toTokNetNode) {
    if ((fromTokNetNode instanceof TokenNetworkTokenNode) &&
        (toTokNetNode instanceof TokenNetworkTokenNode)) {
      return ViewConstants.TOKEN_TO_TOKEN_LINK_TYPE;
    } else if ((fromTokNetNode instanceof TokenNetworkTokenNode) &&
               (toTokNetNode instanceof TokenNetworkRuleInstanceNode)) {
      return ViewConstants.TOKEN_TO_RULE_INST_LINK_TYPE;
    } else if ((fromTokNetNode instanceof TokenNetworkRuleInstanceNode) &&
               (toTokNetNode instanceof TokenNetworkTokenNode)) {
      return ViewConstants.RULE_INST_TO_TOKEN_LINK_TYPE;
    } else {
      System.err.println( "TokenNetworkGenerics.getLinkType: no link type for " +
                          fromTokNetNode + " => " + toTokNetNode);
      return null;
    }
  } // end getLinkType

  /**
   * <code>getVariableColor</code>
   *
   * @param variable - <code>PwVariable</code> - 
   * @param tokenNetworkView - <code>TokenNetworkView</code> - 
   * @return - <code>Color</code> - 
   */
  public static Color getVariableColor( final PwVariable variable,
                                        final TokenNetworkView tokenNetworkView) {
    // System.err.println( "TokenNetworkGenerics.getVariableColor variable " + variable +
    //                     " parent " + variable.getParent());
    Color variableColor = ColorMap.getColor( "black");
    PwVariableContainer variableContainer = variable.getParent();
    if (variableContainer instanceof PwToken) {
      PwToken token = (PwToken) variableContainer;
      variableColor = getTokenColor( token, tokenNetworkView);
    } else if (variableContainer instanceof PwObject) {
      // PwTimeline & PwResource as well
      variableColor =
        tokenNetworkView.getTimelineColor( ((PwObject) variableContainer).getId());
    } else if (variableContainer instanceof PwRuleInstance) {
      variableColor = ViewConstants.RULE_INSTANCE_BG_COLOR;
    } else {
      System.err.println( "\nTokenNetworkGenerics.getVariableColor variable " + variable +
                          " not handled");
      try {
        throw new Exception();
      } catch (Exception e) { e.printStackTrace(); }
    }
    return variableColor;
  } // end getVariableColor

  
  /**
   * <code>getTokenColor</code>
   *
   * @param token - <code>PwToken</code> - 
   * @param tokenNetworkView - <code>TokenNetworkView</code> - 
   * @return - <code>Color</code> - 
   */
  public static Color getTokenColor( final PwToken token, final TokenNetworkView tokenNetworkView) {
    Color tokenColor = ColorMap.getColor( "black");
    PwPartialPlan partialPlan = tokenNetworkView.getPartialPlan();
    System.err.println( "getTokenColor getSlotId " + token.getSlotId());
    System.err.println( "getTokenColor getTimelineId() " + token.getParentId());
    if (token.getSlotId() != null && !token.getSlotId().equals(DbConstants.NO_ID)) {
      PwSlot slot = partialPlan.getSlot( token.getSlotId());
      tokenColor = tokenNetworkView.getTimelineColor( slot.getTimelineId());
    } else if (token.getParentId() != null && !token.getParentId().equals(DbConstants.NO_ID)) {
      tokenColor = tokenNetworkView.getTimelineColor( token.getParentId());
    } else {
      tokenColor = ViewConstants.FREE_TOKEN_BG_COLOR;
    }
    return tokenColor;
  } // end getTokenColor


} // end class TokenNetworkGenerics
