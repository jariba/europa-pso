// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: NavNodeGenerics.java,v 1.2 2004-02-27 18:05:41 miatauro Exp $
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
import gov.nasa.arc.planworks.db.PwTimeline;
import gov.nasa.arc.planworks.db.PwToken;
import gov.nasa.arc.planworks.db.PwSlot;
import gov.nasa.arc.planworks.db.PwVariable;
import gov.nasa.arc.planworks.db.PwVariableContainer;
import gov.nasa.arc.planworks.util.ColorMap;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.nodes.BasicNodeLink;
import gov.nasa.arc.planworks.viz.nodes.ExtendedBasicNode;


/**
 * <code>NavNodeGenerics</code> - general static methods for NavNode classes
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                    NASA Ames Research Center - Code IC
 * @version 0.0
 */
public final class NavNodeGenerics {

  public static final String OBJECT_TO_OBJECT_LINK_TYPE = "OtoO";
  public static final String OBJECT_TO_TIMELINE_LINK_TYPE = "OtoTi";
  public static final String TIMELINE_TO_OBJECT_LINK_TYPE = "TitoO";
  public static final String TIMELINE_TO_TIMELINE_LINK_TYPE = "TitoTi";
  public static final String TIMELINE_TO_SLOT_LINK_TYPE = "TitoS";
  public static final String SLOT_TO_TOKEN_LINK_TYPE = "StoT";
  public static final String TOKEN_TO_TOKEN_LINK_TYPE = "TtoT";
  public static final String TOKEN_TO_VARIABLE_LINK_TYPE = "TtoV";
  public static final String VARIABLE_TO_CONSTRAINT_LINK_TYPE = "VtoC";
  public static final String OBJECT_TO_VARIABLE_LINK_TYPE = "OtoV";
  public static final String TIMELINE_TO_VARIABLE_LINK_TYPE = "TitoV";
 
  private NavNodeGenerics() {
  }

  /**
   * <code>addEntityNavNodes</code>
   *
   * @param navNode - <code>NavNode</code> - 
   * @param navigatorView - <code>NavigatorView</code> - 
   * @param isDebugPrint - <code>boolean</code> - 
   * @return - <code>boolean</code> - 
   */
  public static boolean addEntityNavNodes( final NavNode navNode,
                                           final NavigatorView navigatorView,
                                           final boolean isDebugPrint) {
    boolean areNodesChanged = false;
    // System.err.println( "parent(s) " + navNode.getParentEntityList());
    // System.err.println( "children " + navNode.getComponentEntityList());
    Iterator parentEntityItr = navNode.getParentEntityList().iterator();
    while (parentEntityItr.hasNext()) {
      PwEntity parentEntity = (PwEntity) parentEntityItr.next();
      navigatorView.addEntityNavNode( parentEntity, isDebugPrint);
      areNodesChanged = true;
    }
    Iterator childEntityItr = navNode.getComponentEntityList().iterator();
    while (childEntityItr.hasNext()) {
      PwEntity childEntity = (PwEntity) childEntityItr.next();
      navigatorView.addEntityNavNode( childEntity, isDebugPrint);
      areNodesChanged = true;
    }
    return areNodesChanged;
  } // end addEntityNavNodes

  /**
   * <code>removeEntityNavNodes</code>
   *
   * @param navNode - <code>NavNode</code> - 
   * @param navigatorView - <code>NavigatorView</code> - 
   * @param isDebugPrint - <code>boolean</code> - 
   * @return - <code>boolean</code> - 
   */
  public static boolean removeEntityNavNodes( final NavNode navNode,
                                              final NavigatorView navigatorView,
                                              final boolean isDebugPrint) {
    boolean areNodesChanged = false;
    Iterator parentEntityItr = navNode.getParentEntityList().iterator();
    while (parentEntityItr.hasNext()) {
      PwEntity parentEntity = (PwEntity) parentEntityItr.next();
      NavNode parentNavNode = getNavNode( parentEntity.getId(), navigatorView);
      if ((parentNavNode != null) && parentNavNode.inLayout() &&
          (parentNavNode.getLinkCount() == 0)) {
        removeNavNode( parentNavNode, isDebugPrint);
        areNodesChanged = true;
      }
    }
    Iterator childEntityItr = navNode.getComponentEntityList().iterator();
    while (childEntityItr.hasNext()) {
      PwEntity childEntity = (PwEntity) childEntityItr.next();
      NavNode childNavNode = getNavNode( childEntity.getId(), navigatorView);
      if ((childNavNode != null) && childNavNode.inLayout() &&
          (childNavNode.getLinkCount() == 0)) {
        removeNavNode( childNavNode, isDebugPrint);
        areNodesChanged = true;
      }
    }
    return areNodesChanged;
  } // removeEntityNavNodes

  /**
   * <code>getNavNode</code> 
   *
   * @param id - <code>Integer</code> - 
   * @param navigatorView - <code>NavigatorView</code> - 
   * @return - <code>NavNode</code> - 
   */
  public static NavNode getNavNode( final Integer id, final NavigatorView navigatorView) {
    return (NavNode) navigatorView.entityNavNodeMap.get( id);
  }

  /**
   * <code>removeNavNode</code>
   *
   * @param navNode - <code>NavNode</code> - 
   * @param isDebugPrint - <code>boolean</code> - 
   */
  public static void removeNavNode( final NavNode navNode, final boolean isDebugPrint) {
    if (isDebugPrint) {
      System.err.println( "remove " + navNode.getTypeName() + " NavNode = " + navNode.getId());
    }
    navNode.setInLayout( false);
    navNode.resetNode( isDebugPrint);
  } // end removeNavNode


  /**
   * <code>addParentToEntityNavLinks</code>
   *
   * @param navNode - <code>NavNode</code> - 
   * @param navigatorView - <code>NavigatorView</code> - 
   * @param isDebugPrint - <code>boolean</code> - 
   * @return - <code>boolean</code> - 
   */
  public static boolean addParentToEntityNavLinks( final NavNode navNode,
                                                   final NavigatorView navigatorView,
                                                   final boolean isDebugPrint) {
    boolean areLinksChanged = false;
    Iterator parentEntityItr = navNode.getParentEntityList().iterator();
    while (parentEntityItr.hasNext()) {
      PwEntity parentEntity = (PwEntity) parentEntityItr.next();
      NavNode parentNavNode =
        (NavNode) navigatorView.entityNavNodeMap.get( parentEntity.getId());
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
   * @param navNode - <code>NavNode</code> - 
   * @param navigatorView - <code>NavigatorView</code> - 
   * @param isDebugPrint - <code>boolean</code> - 
   * @return - <code>boolean</code> - 
   */
  public static boolean addEntityToChildNavLinks( final NavNode navNode,
                                                  final NavigatorView navigatorView,
                                                  final boolean isDebugPrint) {
    boolean areLinksChanged = false;
    Iterator childEntityItr = navNode.getComponentEntityList().iterator();
    while (childEntityItr.hasNext()) {
      PwEntity childEntity = (PwEntity) childEntityItr.next();
      NavNode childNavNode =
        (NavNode) navigatorView.entityNavNodeMap.get( childEntity.getId());
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
   * @param fromNavNode - <code>NavNode</code> - 
   * @param toNavNode - <code>NavNode</code> - 
   * @param linkType - <code>String</code> - 
   * @param sourceNode - <code>ExtendedBasicNode</code> - 
   * @return - <code>boolean</code> - 
   */
  public static boolean addNavLink( final NavNode fromNavNode, final NavNode toNavNode,
                                    final String linkType, final NavNode sourceNode,
                                    final NavigatorView navigatorView,
                                    final boolean isDebugPrint) {
    BasicNodeLink returnLink = null;
    String linkName = fromNavNode.getId().toString() + "->" + toNavNode.getId().toString();
    BasicNodeLink link = (BasicNodeLink) navigatorView.navLinkMap.get( linkName);
    if (link == null) {
      link = new BasicNodeLink( (ExtendedBasicNode) fromNavNode,
                                (ExtendedBasicNode) toNavNode, linkName);
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
   * @param navNode - <code>NavNode</code> - 
   * @param navigatorView - <code>NavigatorView</code> - 
   * @param isDebugPrint - <code>boolean</code> - 
   * @return - <code>boolean</code> - 
   */
  public static boolean removeParentToEntityNavLinks( final NavNode navNode,
                                                      final NavigatorView navigatorView,
                                                      final boolean isDebugPrint) { 
    boolean areLinksChanged = false;
    Iterator parentEntityItr = navNode.getParentEntityList().iterator();
    while (parentEntityItr.hasNext()) {
      PwEntity parentEntity = (PwEntity) parentEntityItr.next();
      NavNode parentNavNode =
        (NavNode) navigatorView.entityNavNodeMap.get( parentEntity.getId());
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
   * @param navNode - <code>NavNode</code> - 
   * @param navigatorView - <code>NavigatorView</code> - 
   * @param isDebugPrint - <code>boolean</code> - 
   * @return - <code>boolean</code> - 
   */
  public static boolean removeEntityToChildNavLinks( final NavNode navNode,
                                                     final NavigatorView navigatorView,
                                                     final boolean isDebugPrint) {
    boolean areLinksChanged = false;
    Iterator childEntityItr = navNode.getComponentEntityList().iterator();
    while (childEntityItr.hasNext()) {
      PwEntity childEntity = (PwEntity) childEntityItr.next();
      NavNode childNavNode =
        (NavNode) navigatorView.entityNavNodeMap.get( childEntity.getId());
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
   * @param fromNavNode - <code>NavNode</code> - 
   * @param toNavNode - <code>NavNode</code> - 
   * @param linkType - <code>String</code> - 
   * @return - <code>boolean</code> - 
   */
  public static boolean removeNavLink( final BasicNodeLink link, final NavNode fromNavNode,
                                       final NavNode toNavNode, final String linkType,
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
   * @param fromNavNode - <code>NavNode</code> - 
   * @param toNavNode - <code>NavNode</code> - 
   * @return - <code>String</code> - 
   */
  public static String getLinkType( final NavNode fromNavNode, final NavNode toNavNode) {
    if ((fromNavNode instanceof ModelClassNavNode) &&
        (toNavNode instanceof ModelClassNavNode)) {
      return OBJECT_TO_OBJECT_LINK_TYPE;
    } else if ((fromNavNode instanceof ModelClassNavNode) &&
               (toNavNode instanceof TimelineNavNode)) {
      return OBJECT_TO_TIMELINE_LINK_TYPE;
    } else if ((fromNavNode instanceof TimelineNavNode) &&
               (toNavNode instanceof ModelClassNavNode)) {
      return TIMELINE_TO_OBJECT_LINK_TYPE;
    } else if ((fromNavNode instanceof TimelineNavNode) &&
               (toNavNode instanceof TimelineNavNode)) {
      return TIMELINE_TO_TIMELINE_LINK_TYPE;
    } else if ((fromNavNode instanceof TimelineNavNode) &&
               (toNavNode instanceof SlotNavNode)) {
      return TIMELINE_TO_SLOT_LINK_TYPE;
    } else if ((fromNavNode instanceof SlotNavNode) &&
               (toNavNode instanceof TokenNavNode)) {
      return SLOT_TO_TOKEN_LINK_TYPE;
    } else if ((fromNavNode instanceof TokenNavNode) &&
               (toNavNode instanceof TokenNavNode)) {
      return TOKEN_TO_TOKEN_LINK_TYPE;
    } else if ((fromNavNode instanceof TokenNavNode) &&
               (toNavNode instanceof VariableNavNode)) {
      return TOKEN_TO_VARIABLE_LINK_TYPE;
    } else if ((fromNavNode instanceof VariableNavNode) &&
               (toNavNode instanceof ConstraintNavNode)) {
      return VARIABLE_TO_CONSTRAINT_LINK_TYPE;
    } else if ((fromNavNode instanceof ModelClassNavNode) &&
               (toNavNode instanceof VariableNavNode)) {
      return OBJECT_TO_VARIABLE_LINK_TYPE;
    } else if ((fromNavNode instanceof TimelineNavNode) &&
               (toNavNode instanceof VariableNavNode)) {
      return TIMELINE_TO_VARIABLE_LINK_TYPE;
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
    if (variableContainer instanceof PwTimeline) {
      variableColor =
        navigatorView.getTimelineColor( ((PwTimeline) variableContainer).getId());
    } else if (variableContainer instanceof PwToken) {
      PwToken token = (PwToken) variableContainer;
      variableColor = getTokenColor( token, navigatorView);
    } else if (variableContainer instanceof PwObject) {
      variableColor = ColorMap.getColor( ViewConstants.OBJECT_BG_COLOR);
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
    if (token.getSlotId() != null && !token.getSlotId().equals(DbConstants.noId)) {
      PwSlot slot = partialPlan.getSlot( token.getSlotId());
      tokenColor = navigatorView.getTimelineColor( slot.getTimelineId());
    } else if (token.getParentId() != null && !token.getParentId().equals(DbConstants.noId)) {
      tokenColor = navigatorView.getTimelineColor( token.getParentId());
    } else {
      tokenColor = ColorMap.getColor( ViewConstants.FREE_TOKEN_BG_COLOR);
    }
    return tokenColor;
  } // end getTokenColor


} // end class NavNodeGenerics
