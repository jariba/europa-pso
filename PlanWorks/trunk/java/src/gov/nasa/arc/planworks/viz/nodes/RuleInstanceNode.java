// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: RuleInstanceNode.java,v 1.9 2004-08-25 18:41:00 taylor Exp $
//
// PlanWorks
//
// Will Taylor -- started 07jun04
//

package gov.nasa.arc.planworks.viz.nodes;

import java.awt.Color;
import java.awt.Container;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoBrush;
import com.nwoods.jgo.JGoObject;
import com.nwoods.jgo.JGoText;
import com.nwoods.jgo.JGoView;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.PwPlanningSequence;
import gov.nasa.arc.planworks.db.PwRuleInstance;
import gov.nasa.arc.planworks.db.PwToken;
import gov.nasa.arc.planworks.mdi.MDIInternalFrame;
import gov.nasa.arc.planworks.util.MouseEventOSX;
import gov.nasa.arc.planworks.viz.OverviewToolTip;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.ViewGenerics;
import gov.nasa.arc.planworks.viz.ViewListener;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanView;
import gov.nasa.arc.planworks.viz.partialPlan.navigator.NavigatorView;
import gov.nasa.arc.planworks.viz.partialPlan.rule.RuleInstanceView;
import gov.nasa.arc.planworks.viz.viewMgr.ViewSet;


/**
 * <code>RuleInstanceNode</code> - JGo widget to render a rule 
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *       NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class RuleInstanceNode extends ExtendedBasicNode implements OverviewToolTip {

  private static final boolean IS_FONT_BOLD = false;
  private static final boolean IS_FONT_UNDERLINED = false;
  private static final boolean IS_FONT_ITALIC = false;
  private static final int TEXT_ALIGNMENT = JGoText.ALIGN_LEFT;
  private static final boolean IS_TEXT_MULTILINE = false;
  private static final boolean IS_TEXT_EDITABLE = false;

  protected PwRuleInstance ruleInstance;
  protected PartialPlanView partialPlanView;

  private TokenNode fromTokenNode;
  private List toTokenNodeList; // element TokenNode
  private String nodeLabel;
  private Color backgroundColor;
  private ViewListener viewListener;

  /**
   * <code>RuleInstanceNode</code> - constructor 
   *
   * @param ruleInstance - <code>PwRuleInstance</code> - 
   * @param fromTokenNode - <code>TokenNode</code> - 
   * @param toTokenNodeList - <code>List</code> - 
   * @param ruleLocation - <code>Point</code> - 
   * @param backgroundColor - <code>Color</code> - 
   * @param isDraggable - <code>boolean</code> - 
   * @param partialPlanView - <code>PartialPlanView</code> - 
   */
  public RuleInstanceNode( final PwRuleInstance ruleInstance, final TokenNode fromTokenNode,
                           final List toTokenNodeList, final Point ruleLocation,
                           final Color backgroundColor, final boolean isDraggable,
                           final PartialPlanView partialPlanView) { 
    super( ViewConstants.ELLIPSE);
    this.ruleInstance = ruleInstance;
    this.fromTokenNode = fromTokenNode;
    this.toTokenNodeList = toTokenNodeList;
    this.partialPlanView = partialPlanView;
    viewListener = null;

    this.backgroundColor = backgroundColor;

    StringBuffer labelBuf = new StringBuffer( "rule ");
    labelBuf.append( ruleInstance.getRuleId().toString());
    labelBuf.append( "\nkey=");
    labelBuf.append( ruleInstance.getId().toString());
    nodeLabel = labelBuf.toString();
    // System.err.println( "RuleInstanceNode: " + nodeLabel);

    configure( ruleLocation, backgroundColor, isDraggable);
  } // end constructor

  /**
   * <code>RuleInstanceNode</code> - constructor - for NodeShapes
   *
   * @param name - <code>String</code> - 
   * @param id - <code>Integer</code> - 
   * @param ruleLocation - <code>Point</code> - 
   * @param backgroundColor - <code>Color</code> - 
   */
  public RuleInstanceNode( final String name, final Integer id, final Point ruleLocation,
                           final Color backgroundColor) { 
    super( ViewConstants.ELLIPSE);
    this.ruleInstance = null;
    this.fromTokenNode = null;
    this.toTokenNodeList = null;
    this.partialPlanView = null;

    this.backgroundColor = backgroundColor;
    boolean isDraggable = false;
    StringBuffer labelBuf = new StringBuffer( name);
    labelBuf.append( "\nkey=");
    labelBuf.append( id.toString());
    nodeLabel = labelBuf.toString();

    configure( ruleLocation, backgroundColor, isDraggable);
  } // end constructor

  private final void configure( final Point ruleLocation, final Color backgroundColor,
                               final  boolean isDraggable) {
    setLabelSpot( JGoObject.Center);
    initialize( ruleLocation, nodeLabel);
    setBrush( JGoBrush.makeStockBrush( backgroundColor));  
    getLabel().setEditable( false);
    setDraggable( isDraggable);
    // do not allow user links
    getPort().setVisible( false);
    getLabel().setMultiline( true);
  } // end configure

  /**
   * <code>getPartialPlanView</code>
   *
   * @return - <code>PartialPlanView</code> - 
   */
  public final PartialPlanView getPartialPlanView() {
    return partialPlanView;
  }

  /**
   * <code>toString</code>
   *
   * @return - <code>String</code> - 
   */
  public final String toString() {
    return ruleInstance.getId().toString();
  }

  /**
   * <code>getToolTipText</code>
   *
   * @return - <code>String</code> - 
   */
  public String getToolTipText() {
    if (ruleInstance == null) {
      return null;
    }
    if (partialPlanView.getZoomFactor() > 1) {
      StringBuffer tip = new StringBuffer( "<html> ");
      tip.append( "rule ");
      tip.append( ruleInstance.getRuleId().toString());
      tip.append( "<br>key=");
      tip.append( ruleInstance.getId().toString());
      tip.append( "</html>");
      return tip.toString();
    }
    return null;
  } // end getToolTipText

  /**
   * <code>getToolTipText</code> - when over 1/8 scale overview rule node
   *                               implements OverviewToolTip
   * @param isOverview - <code>boolean</code> - 
   * @return - <code>String</code> - 
   */
  public String getToolTipText( final boolean isOverview) {
    StringBuffer tip = new StringBuffer( "<html> ");
    tip.append( "rule ");
    tip.append( ruleInstance.getRuleId().toString());
    tip.append( "<br>key=");
    tip.append( ruleInstance.getId().toString());
    tip.append( "</html>");
    return tip.toString();
  } // end getToolTipText

  /**
   * <code>getRuleInstance</code>
   *
   * @return - <code>PwRuleInstance</code> - 
   */
  public final PwRuleInstance getRuleInstance() {
    return ruleInstance;
  }

  /**
   * <code>getFromToken</code>
   *
   * @return - <code>PwToken</code> 
   */
  public final PwToken getFromToken() {
    if (fromTokenNode != null) {
      return fromTokenNode.getToken();
    } else {
      return partialPlanView.getPartialPlan().getToken( ruleInstance.getMasterId());
    }
  }

  /**
   * <code>getToTokenList</code>
   *
   * @return - <code>List</code> - of PwToken
   */
  public final List getToTokenList() {
    List toTokenList = new ArrayList();
    if (toTokenNodeList != null) {
      Iterator tokenNodeItr = toTokenNodeList.iterator();
      while (tokenNodeItr.hasNext()) {
        toTokenList.add( ((TokenNode) tokenNodeItr.next()).getToken());
      }
    } else {
      Iterator slaveItr = ruleInstance.getSlaveIdsList().iterator();
      while (slaveItr.hasNext()) {
        toTokenList.add( partialPlanView.getPartialPlan().getToken( (Integer) slaveItr.next()));
      }
    }
    return toTokenList;
  }

  /**
   * <code>addToTokenNodeList</code>
   *
   * @param tokenNode - <code>TokenNode</code> - 
   */
  public final void addToTokenNodeList( final TokenNode tokenNode) {
    toTokenNodeList.add( tokenNode);
  }

  /**
   * <code>doMouseClickWithListener</code> - called from PlanWorksGUITest
   *
   * @param modifiers - <code>int</code> - 
   * @param docCoords - <code>Point</code> - 
   * @param viewCoords - <code>Point</code> - 
   * @param view - <code>JGoView</code> - 
   * @param viewListener - <code>ViewListener</code> - 
   */
  public void doMouseClickWithListener( final int modifiers, final Point docCoords,
                                        final Point viewCoords, final JGoView view,
                                        final ViewListener listener) {
    viewListener = listener;
    doMouseClick( modifiers, docCoords, viewCoords, view);
  } // end doMouseClickWithListener

  /**
   * <code>doMouseClick</code>
   *
   * @param modifiers - <code>int</code> - 
   * @param docCoords - <code>Point</code> - 
   * @param viewCoords - <code>Point</code> - 
   * @param view - <code>JGoView</code> - 
   * @return - <code>boolean</code> - 
   */
  public boolean doMouseClick( final int modifiers, final Point docCoords,
                               final Point viewCoords, final JGoView view) {
    if (ruleInstance == null) {
      return false;
    }
    JGoObject obj = view.pickDocObject( docCoords, false);
     //System.err.println( "RuleInstanceNode: doMouseClick obj class " +
     //                    obj.getTopLevelObject().getClass().getName());
    RuleInstanceNode ruleInstanceNode = (RuleInstanceNode) obj.getTopLevelObject();
    if (MouseEventOSX.isMouseLeftClick( modifiers, PlanWorks.isMacOSX())) {
      // do nothing
    } else if (MouseEventOSX.isMouseRightClick( modifiers, PlanWorks.isMacOSX())) {
      mouseRightPopupMenu( ruleInstanceNode, viewCoords);
      return true;
    }
    return false;
  } // end doMouseClick   

  /**
   * <code>mouseRightPopupMenu</code>
   *
   * @param ruleInstanceNode - <code>RuleInstanceNode</code> - 
   * @param viewCoords - <code>Point</code> - 
   */
  public final void mouseRightPopupMenu( final RuleInstanceNode ruleInstanceNode,
                                         final Point viewCoords) {
    JPopupMenu mouseRightPopup = new JPopupMenu();

    if (ruleInstanceNode.getRuleInstance().getVariables().size() > 0) {
      PwPartialPlan partialPlan = partialPlanView.getPartialPlan();
      String partialPlanName = partialPlan.getPartialPlanName();
      PwPlanningSequence planSequence = PlanWorks.getPlanWorks().getPlanSequence( partialPlan);
      partialPlanView.createAnOpenViewFindItem
        ( partialPlan, partialPlanName, planSequence, mouseRightPopup,
          ViewConstants.CONSTRAINT_NETWORK_VIEW, ruleInstanceNode.getRuleInstance().getId());
    }
    JMenuItem navigatorItem = new JMenuItem( "Open Navigator View");
    navigatorItem.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent evt) {
          String viewSetKey = partialPlanView.getNavigatorViewSetKey();
          MDIInternalFrame navigatorFrame = partialPlanView.openNavigatorViewFrame( viewSetKey);
          Container contentPane = navigatorFrame.getContentPane();
          PwPartialPlan partialPlan = partialPlanView.getPartialPlan();
          contentPane.add( new NavigatorView( ruleInstanceNode.getRuleInstance(),
                                              partialPlan, partialPlanView.getViewSet(),
                                              viewSetKey, navigatorFrame));
        }
      });
    mouseRightPopup.add( navigatorItem);

    mouseRightPopup.add( ViewGenerics.createRuleInstanceViewItem
                         ( RuleInstanceNode.this, partialPlanView, viewListener));

    ViewGenerics.showPopupMenu( mouseRightPopup, partialPlanView, viewCoords);
  } // end mouseRightPopupMenu


} // end class RuleInstanceNode



