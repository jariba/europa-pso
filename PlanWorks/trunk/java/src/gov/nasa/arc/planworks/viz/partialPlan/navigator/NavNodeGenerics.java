// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: NavNodeGenerics.java,v 1.11 2004-09-28 20:45:08 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 22feb04
//

package gov.nasa.arc.planworks.viz.partialPlan.navigator;

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
 * <code>NavNodeGenerics</code> - general static methods for NavNode classes
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                    NASA Ames Research Center - Code IC
 * @version 0.0
 */
public final class NavNodeGenerics {

  private NavNodeGenerics() {
  }

  /**
   * <code>addEntityNavNodes</code>
   *
   * @param navNode - <code>IncrementalNode</code> - 
   * @param navigatorView - <code>NavigatorView</code> - 
   * @param isDebugPrint - <code>boolean</code> - 
   * @return - <code>boolean</code> - 
   */
  public static boolean addEntityNavNodes( final IncrementalNode navNode,
                                           final NavigatorView navigatorView,
                                           final boolean isDebugPrint) {
    boolean areNodesChanged = false;
    // System.err.println( "parent(s) " + navNode.getParentEntityList());
    // System.err.println( "children " + navNode.getComponentEntityList());
    Iterator parentEntityItr = navNode.getParentEntityList().iterator();
    while (parentEntityItr.hasNext()) {
      PwEntity parentEntity = (PwEntity) parentEntityItr.next();
      if (navigatorView.entityNavNodeMap.get( parentEntity.getId()) == null) {
	  areNodesChanged = true;
      }
      navigatorView.addEntityNavNode( parentEntity, isDebugPrint);
    }
    Iterator childEntityItr = navNode.getComponentEntityList().iterator();
    while (childEntityItr.hasNext()) {
      PwEntity childEntity = (PwEntity) childEntityItr.next();
      // System.err.println( "addEntityNavNodes childEntity " + childEntity);
      if (navigatorView.entityNavNodeMap.get( childEntity.getId()) == null) {
	areNodesChanged = true;
      }
      navigatorView.addEntityNavNode( childEntity, isDebugPrint);
    }
    return areNodesChanged;
  } // end addEntityNavNodes

  /**
   * <code>removeEntityNavNodes</code>
   *
   * @param navNode - <code>IncrementalNode</code> - 
   * @param navigatorView - <code>NavigatorView</code> - 
   * @param isDebugPrint - <code>boolean</code> - 
   * @return - <code>boolean</code> - 
   */
  public static boolean removeEntityNavNodes( final IncrementalNode navNode,
                                              final NavigatorView navigatorView,
                                              final boolean isDebugPrint) {
    boolean areNodesChanged = false;
    Iterator parentEntityItr = navNode.getParentEntityList().iterator();
    while (parentEntityItr.hasNext()) {
      PwEntity parentEntity = (PwEntity) parentEntityItr.next();
      IncrementalNode parentNavNode = getNavNode( parentEntity.getId(), navigatorView);
      if (isDebugPrint) {
	System.err.println( "removeEntityNavNodes: parent id = " +
			    parentNavNode.getId() + " linkCount " +
			    parentNavNode.getLinkCount());
      }
      if ((parentNavNode != null) && parentNavNode.inLayout() &&
          (parentNavNode.getLinkCount() == 0)) {
        removeNavNode( parentNavNode, isDebugPrint);
        areNodesChanged = true;
      }
    }
    Iterator childEntityItr = navNode.getComponentEntityList().iterator();
    while (childEntityItr.hasNext()) {
      PwEntity childEntity = (PwEntity) childEntityItr.next();
      IncrementalNode childNavNode = getNavNode( childEntity.getId(), navigatorView);
      if (isDebugPrint) {
	System.err.println( "removeEntityNavNodes: child id = " +
			    childNavNode.getId() + " linkCount " +
			    childNavNode.getLinkCount());
      }
      if ((childNavNode != null) && childNavNode.inLayout() &&
          (childNavNode.getLinkCount() == 0)) {
        removeNavNode( childNavNode, isDebugPrint);
        areNodesChanged = true;
      }
    }
    if (isDebugPrint) {
      System.err.println( "removeEntityNavNodes: id = " + navNode.getId() +
			  " linkCount " + navNode.getLinkCount());
    }
    if (navNode.inLayout() && (navNode.getLinkCount() == 0)) {
      removeNavNode( navNode, isDebugPrint);
      areNodesChanged = true;
    }
    return areNodesChanged;
  } // removeEntityNavNodes

  /**
   * <code>getNavNode</code> 
   *
   * @param id - <code>Integer</code> - 
   * @param navigatorView - <code>NavigatorView</code> - 
   * @return - <code>IncrementalNode</code> - 
   */
  public static IncrementalNode getNavNode( final Integer id,
                                            final NavigatorView navigatorView) {
    return (IncrementalNode) navigatorView.entityNavNodeMap.get( id);
  }

  /**
   * <code>removeNavNode</code>
   *
   * @param navNode - <code>IncrementalNode</code> - 
   * @param isDebugPrint - <code>boolean</code> - 
   */
  public static void removeNavNode( final IncrementalNode navNode,
                                    final boolean isDebugPrint) {
    if (isDebugPrint) {
      System.err.println( "remove " + navNode.getTypeName() + " NavNode = " +
                          navNode.getId());
    }
    navNode.setInLayout( false);
    navNode.resetNode( isDebugPrint);
  } // end removeNavNode


  /**
   * <code>addParentToEntityNavLinks</code>
   *
   * @param navNode - <code>IncrementalNode</code> - 
   * @param navigatorView - <code>NavigatorView</code> - 
   * @param isDebugPrint - <code>boolean</code> - 
   * @return - <code>boolean</code> - 
   */
  public static boolean addParentToEntityNavLinks( final IncrementalNode navNode,
                                                   final NavigatorView navigatorView,
                                                   final boolean isDebugPrint) {
    boolean areLinksChanged = false;
    Iterator parentEntityItr = navNode.getParentEntityList().iterator();
    while (parentEntityItr.hasNext()) {
      PwEntity parentEntity = (PwEntity) parentEntityItr.next();
      IncrementalNode parentNavNode =
        (IncrementalNode) navigatorView.entityNavNodeMap.get( parentEntity.getId());
      if (parentNavNode != null) { 
        String linkType = getLinkType( parentNavNode, navNode);
        if (parentNavNode.inLayout() &&
            addNavLink( parentNavNode, navNode, linkType, navNode, navigatorView,
                        isDebugPrint)) {
          areLinksChanged = true;
        }
      }
    }
    return areLinksChanged;
  } // end addParentToEntityNavLinks

  /**
   * <code>addEntityToChildNavLinks</code>
   *
   * @param navNode - <code>IncrementalNode</code> - 
   * @param navigatorView - <code>NavigatorView</code> - 
   * @param isDebugPrint - <code>boolean</code> - 
   * @return - <code>boolean</code> - 
   */
  public static boolean addEntityToChildNavLinks( final IncrementalNode navNode,
                                                  final NavigatorView navigatorView,
                                                  final boolean isDebugPrint) {
    boolean areLinksChanged = false;
    Iterator childEntityItr = navNode.getComponentEntityList().iterator();
    while (childEntityItr.hasNext()) {
      PwEntity childEntity = (PwEntity) childEntityItr.next();
      IncrementalNode childNavNode =
        (IncrementalNode) navigatorView.entityNavNodeMap.get( childEntity.getId());
      String linkType = getLinkType( navNode, childNavNode);
      if (childNavNode.inLayout() &&
          addNavLink( navNode, childNavNode, linkType, navNode, navigatorView,
                      isDebugPrint)) {
        areLinksChanged = true;
      }
    }
    return areLinksChanged;
  } // end addEntityToChildNavLinks

  /**
   * <code>addNavLink</code>
   *
   * @param fromNavNode - <code>IncrementalNode</code> - 
   * @param toNavNode - <code>IncrementalNode</code> - 
   * @param linkType - <code>String</code> - 
   * @param sourceNode - <code>ExtendedBasicNode</code> - 
   * @return - <code>boolean</code> - 
   */
  public static boolean addNavLink( final IncrementalNode fromNavNode,
                                    final IncrementalNode toNavNode,
                                    final String linkType, final IncrementalNode sourceNode,
                                    final NavigatorView navigatorView,
                                    final boolean isDebugPrint) {
    BasicNodeLink returnLink = null;
    String linkName = fromNavNode.getId().toString() + "->" + toNavNode.getId().toString();
    BasicNodeLink link = (BasicNodeLink) navigatorView.navLinkMap.get( linkName);
    if (link == null) {
      link = new BasicNodeLink( (ExtendedBasicNode) fromNavNode,
                                (ExtendedBasicNode) toNavNode, linkName,
                                getLinkType( fromNavNode, toNavNode));
      link.setArrowHeads( false, true);
      returnLink = link;
      navigatorView.navLinkMap.put( linkName, link);
      if (isDebugPrint) {
        System.err.println( "add " + fromNavNode.getTypeName() + "=>" +
                            toNavNode.getTypeName() + " link " + linkName);
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
    fromNavNode.incrLinkCount();
    toNavNode.incrLinkCount();
    return navigatorView.addNavigatorLinkNew( (ExtendedBasicNode) fromNavNode,
                                              returnLink, linkType);
  } // end addNavLink


  /**
   * <code>removeParentToEntityNavLinks</code>
   *
   * @param navNode - <code>IncrementalNode</code> - 
   * @param navigatorView - <code>NavigatorView</code> - 
   * @param isDebugPrint - <code>boolean</code> - 
   * @return - <code>boolean</code> - 
   */
  public static boolean removeParentToEntityNavLinks( final IncrementalNode navNode,
                                                      final NavigatorView navigatorView,
                                                      final boolean isDebugPrint) { 
    boolean areLinksChanged = false;
    Iterator parentEntityItr = navNode.getParentEntityList().iterator();
    while (parentEntityItr.hasNext()) {
      PwEntity parentEntity = (PwEntity) parentEntityItr.next();
      IncrementalNode parentNavNode =
        (IncrementalNode) navigatorView.entityNavNodeMap.get( parentEntity.getId());
      if (parentNavNode != null) { 
        if (parentNavNode.inLayout()) {
          String linkName = parentNavNode.getId().toString() + "->" +
            navNode.getId().toString();
          BasicNodeLink link = (BasicNodeLink) navigatorView.navLinkMap.get( linkName);
          String linkType = getLinkType( parentNavNode, navNode);
          if ((link != null) && link.inLayout() &&
              removeNavLink( link, parentNavNode, navNode, linkType, isDebugPrint)) {
            areLinksChanged = true;
          }
        }
      }
    }
    return areLinksChanged;
  } // end addParentToEntityNavLinks

  /**
   * <code>removeEntityToChildNavLinks</code>
   *
   * @param navNode - <code>IncrementalNode</code> - 
   * @param navigatorView - <code>NavigatorView</code> - 
   * @param isDebugPrint - <code>boolean</code> - 
   * @return - <code>boolean</code> - 
   */
  public static boolean removeEntityToChildNavLinks( final IncrementalNode navNode,
                                                     final NavigatorView navigatorView,
                                                     final boolean isDebugPrint) {
    boolean areLinksChanged = false;
    Iterator childEntityItr = navNode.getComponentEntityList().iterator();
    while (childEntityItr.hasNext()) {
      PwEntity childEntity = (PwEntity) childEntityItr.next();
      IncrementalNode childNavNode =
        (IncrementalNode) navigatorView.entityNavNodeMap.get( childEntity.getId());
      if (childNavNode != null) { 
        if (childNavNode.inLayout()) {
          String linkName = navNode.getId().toString() + "->" +
            childNavNode.getId().toString();
          String linkType = getLinkType( navNode, childNavNode);
          BasicNodeLink link = (BasicNodeLink) navigatorView.navLinkMap.get( linkName);
          if ((link != null) && link.inLayout() &&
              removeNavLink( link, navNode, childNavNode, linkType, isDebugPrint)) {
            areLinksChanged = true;
          }
        }
      }
    }
    return areLinksChanged;
  } // end removeEntityToChildNavLinks


  /**
   * <code>removeNavLink</code>
   *
   * @param link - <code>BasicNodeLink</code> - 
   * @param fromNavNode - <code>IncrementalNode</code> - 
   * @param toNavNode - <code>IncrementalNode</code> - 
   * @param linkType - <code>String</code> - 
   * @return - <code>boolean</code> - 
   */
  public static boolean removeNavLink( final BasicNodeLink link,
                                       final IncrementalNode fromNavNode,
                                       final IncrementalNode toNavNode,
                                       final String linkType,
                                       final boolean isDebugPrint) {
    boolean areLinksChanged = false;
    link.decLinkCount();
    fromNavNode.decLinkCount();
    toNavNode.decLinkCount();
    if (isDebugPrint) {
      System.err.println( linkType + " dec link: " + link.toString() + " to " +
                          link.getLinkCount());
    }
    if (link.getLinkCount() == 0) {
      if (isDebugPrint) {
        System.err.println( "remove " + fromNavNode.getTypeName() + "=>" +
                            toNavNode.getTypeName() + ": " + link.toString());
      }
      link.setInLayout( false);
      areLinksChanged = true;
    }
    return areLinksChanged;
  } // end removeNavLink


  /**
   * <code>getLinkType</code>
   *
   * @param fromNavNode - <code>IncrementalNode</code> - 
   * @param toNavNode - <code>IncrementalNode</code> - 
   * @return - <code>String</code> - 
   */
  protected static String getLinkType( final IncrementalNode fromNavNode,
                                       final IncrementalNode toNavNode) {
    if ((fromNavNode instanceof ModelClassNavNode) &&
        (toNavNode instanceof ModelClassNavNode)) {
      return ViewConstants.OBJECT_TO_OBJECT_LINK_TYPE;
    } else if ((fromNavNode instanceof ModelClassNavNode) &&
               (toNavNode instanceof TimelineNavNode)) {
      return ViewConstants.OBJECT_TO_TIMELINE_LINK_TYPE;
    } else if ((fromNavNode instanceof ModelClassNavNode) &&
               (toNavNode instanceof ResourceNavNode)) {
      return ViewConstants.OBJECT_TO_RESOURCE_LINK_TYPE;
    } else if ((fromNavNode instanceof TimelineNavNode) &&
               (toNavNode instanceof ModelClassNavNode)) {
      return ViewConstants.TIMELINE_TO_OBJECT_LINK_TYPE;
    } else if ((fromNavNode instanceof TimelineNavNode) &&
               (toNavNode instanceof TimelineNavNode)) {
      return ViewConstants.TIMELINE_TO_TIMELINE_LINK_TYPE;
    } else if ((fromNavNode instanceof TimelineNavNode) &&
               (toNavNode instanceof SlotNavNode)) {
      return ViewConstants.TIMELINE_TO_SLOT_LINK_TYPE;
    } else if ((fromNavNode instanceof TimelineNavNode) &&
               (toNavNode instanceof ResourceNavNode)) {
      return ViewConstants.TIMELINE_TO_RESOURCE_LINK_TYPE;
    } else if ((fromNavNode instanceof SlotNavNode) &&
               (toNavNode instanceof TokenNavNode)) {
      return ViewConstants.SLOT_TO_TOKEN_LINK_TYPE;
    } else if ((fromNavNode instanceof TokenNavNode) &&
               (toNavNode instanceof TokenNavNode)) {
      return ViewConstants.TOKEN_TO_TOKEN_LINK_TYPE;
    } else if ((fromNavNode instanceof TokenNavNode) &&
               (toNavNode instanceof VariableNavNode)) {
      return ViewConstants.TOKEN_TO_VARIABLE_LINK_TYPE;
    } else if ((fromNavNode instanceof VariableNavNode) &&
               (toNavNode instanceof ConstraintNavNode)) {
      return ViewConstants.VARIABLE_TO_CONSTRAINT_LINK_TYPE;
    } else if ((fromNavNode instanceof ModelClassNavNode) &&
               (toNavNode instanceof VariableNavNode)) {
      return ViewConstants.OBJECT_TO_VARIABLE_LINK_TYPE;
    } else if ((fromNavNode instanceof TimelineNavNode) &&
               (toNavNode instanceof VariableNavNode)) {
      return ViewConstants.TIMELINE_TO_VARIABLE_LINK_TYPE;
    } else if ((fromNavNode instanceof ResourceNavNode) &&
               (toNavNode instanceof TokenNavNode)) {
      return ViewConstants.RESOURCE_TO_TOKEN_LINK_TYPE;
    } else if ((fromNavNode instanceof ResourceNavNode) &&
               (toNavNode instanceof VariableNavNode)) {
      return ViewConstants.RESOURCE_TO_VARIABLE_LINK_TYPE;
    } else if ((fromNavNode instanceof TokenNavNode) &&
               (toNavNode instanceof RuleInstanceNavNode)) {
      return ViewConstants.TOKEN_TO_RULE_INST_LINK_TYPE;
    } else if ((fromNavNode instanceof RuleInstanceNavNode) &&
               (toNavNode instanceof TokenNavNode)) {
      return ViewConstants.RULE_INST_TO_TOKEN_LINK_TYPE;
    } else if ((fromNavNode instanceof RuleInstanceNavNode) &&
               (toNavNode instanceof VariableNavNode)) {
      return ViewConstants.RULE_INST_TO_VARIABLE_LINK_TYPE;
    } else {
      System.err.println( "NavNodeGenerics.getLinkType: no link type for " +
                          fromNavNode + " => " + toNavNode);
      return null;
    }
  } // end getLinkType

  /**
   * <code>getVariableColor</code>
   *
   * @param variable - <code>PwVariable</code> - 
   * @param navigatorView - <code>NavigatorView</code> - 
   * @return - <code>Color</code> - 
   */
  public static Color getVariableColor( final PwVariable variable,
                                        final NavigatorView navigatorView) {
    // System.err.println( "NavNodeGenerics.getVariableColor variable " + variable +
    //                     " parent " + variable.getParent());
    Color variableColor = ColorMap.getColor( "black");
    PwVariableContainer variableContainer = variable.getParent();
    if (variableContainer instanceof PwToken) {
      PwToken token = (PwToken) variableContainer;
      variableColor = getTokenColor( token, navigatorView);
    } else if (variableContainer instanceof PwObject) {
      // PwTimeline & PwResource as well
      variableColor =
        navigatorView.getTimelineColor( ((PwObject) variableContainer).getId());
    } else if (variableContainer instanceof PwRuleInstance) {
      variableColor = ViewConstants.RULE_INSTANCE_BG_COLOR;
    } else {
      System.err.println( "\nNavNodeGenerics.getVariableColor variable " + variable +
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
   * @param navigatorView - <code>NavigatorView</code> - 
   * @return - <code>Color</code> - 
   */
  public static Color getTokenColor( final PwToken token, final NavigatorView navigatorView) {
    Color tokenColor = ColorMap.getColor( "black");
    PwPartialPlan partialPlan = navigatorView.getPartialPlan();
    // System.err.println( "getTokenColor getSlotId " + token.getSlotId());
    // System.err.println( "getTokenColor getTimelineId() " + token.getTimelineId());
    if (token.getSlotId() != null && !token.getSlotId().equals(DbConstants.NO_ID)) {
      PwSlot slot = partialPlan.getSlot( token.getSlotId());
      tokenColor = navigatorView.getTimelineColor( slot.getTimelineId());
    } else if (token.getParentId() != null && !token.getParentId().equals(DbConstants.NO_ID)) {
      tokenColor = navigatorView.getTimelineColor( token.getParentId());
    } else {
      tokenColor = ViewConstants.FREE_TOKEN_BG_COLOR;
    }
    return tokenColor;
  } // end getTokenColor


} // end class NavNodeGenerics
