// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: ModelRulesView.java,v 1.3 2004-02-03 20:44:00 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 25nov03
//

package gov.nasa.arc.planworks.viz.sequence.modelRules;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoBrush;
import com.nwoods.jgo.JGoDocument;
import com.nwoods.jgo.JGoPen;
import com.nwoods.jgo.JGoText;
import com.nwoods.jgo.JGoView;

// PlanWorks/java/lib/JGo/Classier.jar
import com.nwoods.jgo.examples.BasicNode;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.DbConstants;
import gov.nasa.arc.planworks.db.PwModel;
import gov.nasa.arc.planworks.db.PwRule;
import gov.nasa.arc.planworks.db.PwPlanningSequence;
import gov.nasa.arc.planworks.util.ColorMap;
import gov.nasa.arc.planworks.util.MouseEventOSX;
import gov.nasa.arc.planworks.util.StringNameComparator;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.ViewGenerics;
import gov.nasa.arc.planworks.viz.VizViewOverview;
import gov.nasa.arc.planworks.viz.nodes.NodeGenerics;
import gov.nasa.arc.planworks.viz.sequence.SequenceView;
import gov.nasa.arc.planworks.viz.sequence.SequenceViewSet;
import gov.nasa.arc.planworks.viz.viewMgr.ViewableObject;
import gov.nasa.arc.planworks.viz.viewMgr.ViewSet;


/**
 * <code>ModelRulesView</code> - 
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                   NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class ModelRulesView extends SequenceView {

  private static final Color RULE_MEETS_COLOR = ColorMap.getColor( "blue");
  private static final Color RULE_MET_BY_COLOR = ColorMap.getColor( "plum");
  private static final Color RULE_CONTAINS_COLOR = ColorMap.getColor( "green3");
  private static final Color RULE_CONTAINED_BY_COLOR = ColorMap.getColor( "aquamarine");

  protected static final List RULE_TYPE_LIST;
  private static final List RULE_COLOR_LIST;


  static {
    RULE_TYPE_LIST = new ArrayList();
    RULE_TYPE_LIST.add( DbConstants.RULE_MEETS);
    RULE_TYPE_LIST.add( DbConstants.RULE_MET_BY);
    RULE_TYPE_LIST.add( DbConstants.RULE_CONTAINS);
    RULE_TYPE_LIST.add( DbConstants.RULE_CONTAINED_BY);

    RULE_COLOR_LIST = new ArrayList();
    RULE_COLOR_LIST.add( RULE_MEETS_COLOR);
    RULE_COLOR_LIST.add( RULE_MET_BY_COLOR);
    RULE_COLOR_LIST.add( RULE_CONTAINS_COLOR);
    RULE_COLOR_LIST.add( RULE_CONTAINED_BY_COLOR);
  } // end static
  
  private PwPlanningSequence planSequence;
  private PwModel model;
  private long startTimeMSecs;
  private ViewSet viewSet;
  private ModelRulesJGoView jGoView;
  private JGoDocument jGoDocument;
  private Graphics graphics;
  private FontMetrics fontMetrics;
  private Font font;

  private List predicateNodeList; // element PredicateNode
  private List predicateNameList; // element String
  private List linkList; // element RuleLink
  private List linkNameList; // element String

  /**
   * <code>ModelRulesView</code> - constructor 
   *
   * @param planSequence - <code>ViewableObject</code> - 
   * @param viewSet - <code>ViewSet</code> - 
   */
  public ModelRulesView( ViewableObject planSequence,  ViewSet viewSet) {
    super( (PwPlanningSequence) planSequence, (SequenceViewSet) viewSet);
    this.planSequence = (PwPlanningSequence) planSequence;
    this.viewSet = (SequenceViewSet) viewSet;

    this.startTimeMSecs = System.currentTimeMillis();
    // build model structure from MySQL db
    model = this.planSequence.getModel();

    setLayout( new BoxLayout( this, BoxLayout.Y_AXIS));

    jGoView = new ModelRulesJGoView();
    jGoView.setBackground( ViewConstants.VIEW_BACKGROUND_COLOR);
    add( jGoView, BorderLayout.NORTH);
    jGoView.validate();
    jGoView.setVisible( true);
    this.setVisible( true);

    SwingUtilities.invokeLater( runInit);
  } // end constructor


  /**
   * <code>getPlanSequence</code>
   *
   * @return - <code>PwPlanningSequence</code> - 
   */
  public PwPlanningSequence getPlanSequence() {
    return planSequence;
  }


  Runnable runInit = new Runnable() {
      public void run() {
        init();
      }
    };

  /**
   * <code>init</code> - wait for instance to become displayable, determine
   *                     appropriate font metrics, and render the JGo timeline,
   *                     and slot widgets
   *
   *    These functions are not done in the constructor to avoid:
   *    "Cannot measure text until a JGoView exists and is part of a visible window".
   *    called by componentShown method on the JFrame
   *    JGoView.setVisible( true) must be completed -- use runInit in constructor
   */
  public void init() {
    jGoView.setCursor( new Cursor( Cursor.WAIT_CURSOR));
    // wait for ModelRulesView instance to become displayable
    while (! this.isDisplayable()) {
      try {
        Thread.currentThread().sleep(50);
      } catch (InterruptedException excp) {
      }
      // System.err.println( "ModelRulesView displayable " + this.isDisplayable());
    }
    graphics = this.getGraphics();
    font = new Font( ViewConstants.TIMELINE_VIEW_FONT_NAME,
                     ViewConstants.TIMELINE_VIEW_FONT_STYLE,
                     ViewConstants.TIMELINE_VIEW_FONT_SIZE);
    // does nothing
    // jGoView.setFont( font);
    fontMetrics = graphics.getFontMetrics( font);

    // graphics.dispose();

    jGoDocument = jGoView.getDocument();

    boolean isRedraw = false;
    renderModelRules( isRedraw);

    expandViewFrame( viewSet.openView( this.getClass().getName()),
                     (int) jGoView.getDocumentSize().getWidth(),
                     (int) jGoView.getDocumentSize().getHeight());
    jGoView.setCursor( new Cursor( Cursor.DEFAULT_CURSOR));
  } // end init


  /**
   * <code>redraw</code>
   *
   */
  public void redraw() {
    Thread thread = new RedrawViewThread();
    thread.setPriority(Thread.MIN_PRIORITY);
    thread.start();
  }

  class RedrawViewThread extends Thread {

    public RedrawViewThread() {
    }  // end constructor

    public void run() {
      boolean isRedraw = true;
      renderModelRules( isRedraw);
    } //end run

  } // end class RedrawViewThread

  private void renderModelRules( boolean isRedraw) {
    if (isRedraw) {
      jGoView.setCursor( new Cursor( Cursor.WAIT_CURSOR));
      jGoDocument.deleteContents();
      System.err.println( "Redrawing Model Rules View ...");
      startTimeMSecs = System.currentTimeMillis();
      this.setVisible( false);
    }

    predicateNodeList = new ArrayList();
    predicateNameList = new ArrayList();
    linkList = new ArrayList();
    linkNameList = new ArrayList();

   // create all nodes
    createPredicateNodes();

    ModelRulesLayout layout = new ModelRulesLayout( jGoDocument, startTimeMSecs);
    layout.performLayout();

    Collections.sort( predicateNameList, new StringNameComparator());

    if (isRedraw) {
      this.setVisible( true);
      jGoView.setCursor( new Cursor( Cursor.DEFAULT_CURSOR));
    }
  } // end renderModelRules


  private void createPredicateNodes() {
    int x = ViewConstants.TIMELINE_VIEW_X_INIT;
    int y = (ViewConstants.TIMELINE_VIEW_Y_INIT * 2) +
      (ViewConstants.TIMELINE_VIEW_Y_DELTA * 2);
    Color backgroundColor = ColorMap.getColor( "chartreuse1");
      // ((PartialPlanViewSet) viewSet).getColorStream().getColor( timelineCnt);

    Iterator ruleItr = model.listRules().iterator();
    while (ruleItr.hasNext()) {
      PwRule rule = (PwRule) ruleItr.next();
      String fromPredicate = rule.getFromPredicate();
      String fromPredicateObject = rule.getFromPredicateObject();
      String fromPredicateAttribute = rule.getFromPredicateAttribute();
      List fromPredicateParams = rule.getFromPredicateParams();
      List fromPredicateParamValues = rule.getFromPredicateParamValues();
      if (fromPredicateParams.size() == 0) {
        fromPredicateParams.add( "noParams");
      }
      String toPredicate = rule.getToPredicate();
      String toPredicateObject = rule.getToPredicateObject();
      String toPredicateAttribute = rule.getToPredicateAttribute();
      List toPredicateParams = rule.getToPredicateParams();
      List toPredicateParamValues = rule.getToPredicateParamValues();
      String ruleType = rule.getType();
//       System.err.println( "ModelRulesView: fromPredicate " + fromPredicate +
//                           " toPredicate " + toPredicate + " ruleType " + ruleType);
      PredicateNode fromPredicateNode =
        createPredicateNode( fromPredicate, fromPredicateObject, fromPredicateAttribute,
                             fromPredicateParams, fromPredicateParamValues,
                             new Point( x, y), backgroundColor, rule);

      if (! fromPredicateNode.isExisting()) {
        x = (int) ( fromPredicateNode.getLocation().getX() +
                    (fromPredicateNode.getSize().getWidth() / 2));
        fromPredicateNode.setLocation( x, (int) fromPredicateNode.getLocation().getY());
        x += (fromPredicateNode.getSize().getWidth() / 2) +
          (ViewConstants.TIMELINE_VIEW_X_DELTA * 2);
      }
      PredicateNode toPredicateNode =
        createPredicateNode( toPredicate,toPredicateObject, toPredicateAttribute,
                             toPredicateParams, toPredicateParamValues,
                             new Point( x, y), backgroundColor, rule);
      if (! toPredicateNode.isExisting()) {
        x = (int) ( toPredicateNode.getLocation().getX() +
                    (toPredicateNode.getSize().getWidth() / 2));
        toPredicateNode.setLocation( x, (int) toPredicateNode.getLocation().getY());
        x += (toPredicateNode.getSize().getWidth() / 2) +
          (ViewConstants.TIMELINE_VIEW_X_DELTA * 2);
      }

//       createRuleLinkTriplets( fromPredicateNode, toPredicateNode, fromPredicateParams,
//                               fromPredicateParamValues, ruleType);
      createRuleLink( fromPredicateNode, toPredicateNode, ruleType);
    }
  } // end createPredicateNodes


  private PredicateNode createPredicateNode( String name, String object, String attribute,
                                             List params, List paramValues, Point location,
                                             Color backgroundColor, PwRule rule) {
//     System.err.println( "createPredicateNode: name " + name + " object " + object +
//                         " attribute " + attribute + " params " + params);
    PredicateNode predicateNode =
      new PredicateNode( name, object, attribute, params, paramValues, location,
                         backgroundColor, rule, this);
    int index = predicateNodeList.indexOf( predicateNode);
    if (index == -1) {
      predicateNode.setIsExisting( false);
      predicateNodeList.add( predicateNode);
      predicateNameList.add( name);
      jGoDocument.addObjectAtTail( predicateNode);
      return predicateNode;
    } else {
      return (PredicateNode) predicateNodeList.get( index);
    }
  } // end createPredicateNodes


  private void createRuleLinkTriplets( PredicateNode fromPredicateNode,
                                       PredicateNode toPredicateNode,
                                       List fromPredicateParams, List fromPredicateParamValues,
                                       String ruleType) {
    if ((fromPredicateNode == null) || (toPredicateNode == null)) {
      return;
    }
    String linkName = fromPredicateNode.getName() + "<-" + ruleType + "->" +
      toPredicateNode.getName();
    Iterator linkItr = linkNameList.iterator();
    while (linkItr.hasNext()) {
      if (linkName.equals( (String) linkItr.next())) {
        // System.err.println( "discard " + linkName + " type " + type);
        return;
      }
    }
    linkNameList.add( linkName);

    int ruleIndex = RULE_TYPE_LIST.indexOf( ruleType);
    if (ruleIndex != -1) {
      Point ruleNodeLocation = getRuleNodeLocation( fromPredicateNode, toPredicateNode, ruleType);
      Color ruleColor = (Color) RULE_COLOR_LIST.get( ruleIndex);
      for (int i = 0, n = fromPredicateParams.size(); i < n; i++) {
        String param = (String) fromPredicateParams.get( i);
        List paramValues = (List) fromPredicateParamValues.get( i);
        Point paramNodeLocation = ruleNodeLocation;
        ParamNode paramNode = new ParamNode( param, paramNodeLocation,
                                             ColorMap.getColor( "lightYellow"), this);
        jGoDocument.addObjectAtTail( paramNode);

        createRuleLink( fromPredicateNode, paramNode, ruleType);

        Iterator paramValueItr = paramValues.iterator();
        while (paramValueItr.hasNext()) {
          String paramValue = (String) paramValueItr.next();
          RuleNode ruleNode = new RuleNode( param, paramValue, ruleType, ruleNodeLocation,
                                            ruleColor, this);
          jGoDocument.addObjectAtTail( ruleNode);

          createRuleLink( paramNode, ruleNode, ruleType);

          createRuleLink( ruleNode, toPredicateNode, ruleType);
        }
      }
    } else {
      System.err.println( "createRuleLink: ruleType " + ruleType + " not handled");
    }
  } // end createRuleLinkTriplets

  private void createRuleLink( BasicNode fromNode, BasicNode toNode, String ruleType) {
    Color ruleColor = (Color) RULE_COLOR_LIST.get( RULE_TYPE_LIST.indexOf( ruleType));
    RuleLink link = new RuleLink( fromNode, toNode, ruleType);
    linkList.add( link);
    // arrow head
    link.setBrush( JGoBrush.makeStockBrush( ruleColor));
    // line
    link.setPen( new JGoPen( JGoPen.SOLID, 1, ruleColor));
    jGoDocument.addObjectAtTail( link);
  } // end crateRuleLink

  private Point getRuleNodeLocation( PredicateNode fromPredicateNode,
                                     PredicateNode toPredicateNode, String ruleType) {
    Point ruleNodeLocation = null;
    if (ruleType.equals( DbConstants.RULE_MEETS) ||
        ruleType.equals( DbConstants.RULE_CONTAINS)) {
      ruleNodeLocation = new Point( (int) (fromPredicateNode.getLocation().getX() +
                                           ((toPredicateNode.getLocation().getX() -
                                             fromPredicateNode.getLocation().getX()) / 2)),
                                    (int) (fromPredicateNode.getLocation().getY() +
                                           ((toPredicateNode.getLocation().getY() -
                                             fromPredicateNode.getLocation().getY()) / 2)));
    } else if (ruleType.equals( DbConstants.RULE_MET_BY) ||
               ruleType.equals( DbConstants.RULE_CONTAINED_BY)) {
      ruleNodeLocation = new Point( (int) (fromPredicateNode.getLocation().getX() -
                                           ((fromPredicateNode.getLocation().getX() -
                                             toPredicateNode.getLocation().getX()) / 2)),
                                    (int) (fromPredicateNode.getLocation().getY() -
                                           ((fromPredicateNode.getLocation().getY() -
                                             toPredicateNode.getLocation().getY()) / 2)));
    }
    return ruleNodeLocation;
  } // end getRuleNodeLocation

  /**
   * <code>getPredicateNode</code> - test name with equalsIgnoreCase
   *
   * @param nodeName - <code>String</code> - 
   * @return - <code>PredicateNode</code> - 
   */
  public PredicateNode getPredicateNode( String nodeName) {
    Iterator nodeItr = predicateNodeList.iterator();
    while (nodeItr.hasNext()) {
      PredicateNode predicateNode = (PredicateNode) nodeItr.next();
      if (nodeName.equalsIgnoreCase( predicateNode.getName())) {
        return predicateNode;
      }
    }
    return null;
  } // end getPredicateNode


  /**
   * <code>ModelRulesJGoView</code> - subclass JGoView to add doBackgroundClick
   *
   */
  class ModelRulesJGoView extends JGoView {

    /**
     * <code>ModelRulesJGoView</code> - constructor 
     *
     */
    public ModelRulesJGoView() {
      super();
    }

    /**
     * <code>doBackgroundClick</code> - Mouse-Right pops up menu:
     *                                 1) snap to active token
     *
     * @param modifiers - <code>int</code> - 
     * @param docCoords - <code>Point</code> - 
     * @param viewCoords - <code>Point</code> - 
     */
    public void doBackgroundClick( int modifiers, Point docCoords, Point viewCoords) {
      if (MouseEventOSX.isMouseLeftClick( modifiers, PlanWorks.isMacOSX())) {
        // do nothing
      } else if (MouseEventOSX.isMouseRightClick( modifiers, PlanWorks.isMacOSX())) {
        mouseRightPopupMenu( viewCoords);
      }
    } // end doBackgroundClick

  } // end class ModelRulesJGoView


  private void mouseRightPopupMenu( Point viewCoords) {
    JPopupMenu mouseRightPopup = new JPopupMenu();

    JMenuItem nodeByPredicateNameItem = new JMenuItem( "Find by Predicate Name");
    createNodeByPredicateNameItem( nodeByPredicateNameItem);
    mouseRightPopup.add( nodeByPredicateNameItem);

    JMenuItem overviewWindowItem = new JMenuItem( "Overview Window");
    createOverviewWindowItem( overviewWindowItem, this, viewCoords);
    mouseRightPopup.add( overviewWindowItem);

    NodeGenerics.showPopupMenu( mouseRightPopup, this, viewCoords);
  } // end mouseRightPopupMenu


  private void createNodeByPredicateNameItem( JMenuItem nodeByPredicateNameItem) {
    nodeByPredicateNameItem.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent evt) {
          Object[] options = new Object[predicateNameList.size()];
          for (int i = 0, n = predicateNameList.size(); i < n; i++) {
            options[i] = (String) predicateNameList.get( i);
          }
          Object response = JOptionPane.showInputDialog
            ( PlanWorks.getPlanWorks(), "", "Find by Predicate Name",
              JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
          if (response instanceof String) {
            PredicateNode predicateNode = getPredicateNode( (String) response);
            if (predicateNode != null) {
              boolean isHighlightNode = true;
              NodeGenerics.focusViewOnNode( predicateNode, isHighlightNode, jGoView);
              System.err.println( "ModelRulesView found predicate node: " +
                                  predicateNode.getName());
            }
          }
          // JOptionPane.showInputDialog returns null if user selected "cancel"
        }
      });
  } // end createNodeByPredicateNameItem

  private void createOverviewWindowItem( JMenuItem overviewWindowItem,
                                         final ModelRulesView modelRulesView,
                                         final Point viewCoords) {
    overviewWindowItem.addActionListener( new ActionListener() { 
        public void actionPerformed( ActionEvent evt) {
          VizViewOverview currentOverview =
            ViewGenerics.openOverviewFrame( PlanWorks.MODEL_RULES_VIEW, planSequence,
                                            modelRulesView, viewSet, jGoView, viewCoords);
          if (currentOverview != null) {
            overview = currentOverview;
          }
        }
      });
  } // end createOverviewWindowItem



} // end class ModelRulesView
