// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: ModelRulesView.java,v 1.1 2003-12-03 02:29:51 taylor Exp $
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
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoArea;
import com.nwoods.jgo.JGoBrush;
import com.nwoods.jgo.JGoDocument;
import com.nwoods.jgo.JGoPen;
import com.nwoods.jgo.JGoRectangle;
import com.nwoods.jgo.JGoText;
import com.nwoods.jgo.JGoView;

// PlanWorks/java/lib/JGo/Classier.jar
import com.nwoods.jgo.examples.BasicNode;

import gov.nasa.arc.planworks.db.PwModel;
import gov.nasa.arc.planworks.db.PwRule;
import gov.nasa.arc.planworks.db.PwPlanningSequence;
import gov.nasa.arc.planworks.util.ColorMap;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.sequence.SequenceView;
import gov.nasa.arc.planworks.viz.sequence.SequenceViewSet;
import gov.nasa.arc.planworks.viz.viewMgr.ViewableObject;
import gov.nasa.arc.planworks.viz.viewMgr.ViewSet;


public class ModelRulesView extends SequenceView {

  private static final String RULE_MEETS = "meets";
  private static final String RULE_MET_BY = "met-by";

  private static final Color RULE_MEETS_COLOR = ColorMap.getColor( "blue");
  private static final Color RULE_MET_BY_COLOR = ColorMap.getColor( "green3");

  private static final List RULE_TYPE_LIST;
  private static final List RULE_COLOR_LIST;


  static {
    RULE_TYPE_LIST = new ArrayList();
    RULE_TYPE_LIST.add( RULE_MEETS);
    RULE_TYPE_LIST.add( RULE_MET_BY);

    RULE_COLOR_LIST = new ArrayList();
    RULE_COLOR_LIST.add( RULE_MEETS_COLOR);
    RULE_COLOR_LIST.add( RULE_MET_BY_COLOR);
  } // end static
  
  private PwPlanningSequence planSequence;
  private PwModel model;
  private long startTimeMSecs;
  private ViewSet viewSet;
  private JGoView jGoView;
  private JGoDocument jGoDocument;
  private Graphics graphics;
  private FontMetrics fontMetrics;
  private Font font;

  private List nodeList; // element PredicateNode
  private List linkList; // element RuleLink
  private List linkNameList; // element String

  public ModelRulesView( ViewableObject planSequence,  ViewSet viewSet) {
    super( (PwPlanningSequence) planSequence, (SequenceViewSet) viewSet);
    this.planSequence = (PwPlanningSequence) planSequence;
    this.viewSet = (SequenceViewSet) viewSet;

    this.startTimeMSecs = System.currentTimeMillis();
    // build model structure from MySQL db
    model = this.planSequence.getModel();

    linkList = new ArrayList();
    linkNameList = new ArrayList();

    setLayout( new BoxLayout( this, BoxLayout.Y_AXIS));

    jGoView = new JGoView();
    jGoView.setBackground( ViewConstants.VIEW_BACKGROUND_COLOR);
    add( jGoView, BorderLayout.NORTH);
    jGoView.validate();
    jGoView.setVisible( true);
    this.setVisible( true);

    SwingUtilities.invokeLater( runInit);
  } // end constructor


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
  } // end init


  /**
   * <code>redraw</code>
   *
   */
  public void redraw() {
    new RedrawViewThread().start();
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

    nodeList = new ArrayList();
    linkList = new ArrayList();
    linkNameList = new ArrayList();

   // create all nodes
    createPredicateNodes();

    if (isRedraw) {
      this.setVisible( true);
      jGoView.setCursor( new Cursor( Cursor.DEFAULT_CURSOR));
    }
  } // end renderModelRules


  private void createPredicateNodes() {
    int x = ViewConstants.TIMELINE_VIEW_X_INIT;
    int y = ViewConstants.TIMELINE_VIEW_Y_INIT * 2;
    Color backgroundColor = ColorMap.getColor( "chartreuse1");
      // ((PartialPlanViewSet) viewSet).getColorStream().getColor( timelineCnt);

    Iterator ruleItr = model.listRules().iterator();
    while (ruleItr.hasNext()) {
      PwRule rule = (PwRule) ruleItr.next();
      String fromPredicate = rule.getFromPredicate();
      String toPredicate = rule.getToPredicate();
      String ruleType = rule.getType();
//       System.err.println( "ModelRulesView: fromPredicate " + fromPredicate +
//                           " toPredicate " + toPredicate + " ruleType " + ruleType);
      PredicateNode fromPredicateNode =
        createPredicateNode( fromPredicate, new Point( x, y), backgroundColor);

      if (x == ViewConstants.TIMELINE_VIEW_X_INIT) {
        x = (int) ( ViewConstants.TIMELINE_VIEW_X_INIT +
                    (fromPredicateNode.getSize().getWidth() / 2));
        fromPredicateNode.setLocation( x, (int) fromPredicateNode.getLocation().getY());
      }
      if (! fromPredicateNode.isExisting()) {
        x += fromPredicateNode.getSize().getWidth() + ViewConstants.TIMELINE_VIEW_Y_DELTA;
      }
      PredicateNode toPredicateNode =
        createPredicateNode( toPredicate, new Point( x, y), backgroundColor);
      if (! toPredicateNode.isExisting()) {
        x += toPredicateNode.getSize().getWidth() + ViewConstants.TIMELINE_VIEW_Y_DELTA;
      }

      createRuleLink( fromPredicateNode, toPredicateNode, ruleType);
    }
  } // end createPredicateNodes


  private PredicateNode createPredicateNode( String name, Point location,
                                             Color backgroundColor) {
    PredicateNode predicateNode =
      new PredicateNode( name, location, backgroundColor, this);
    if (! nodeList.contains( predicateNode)) {
      predicateNode.setIsExisting( false);
      nodeList.add( predicateNode);
      jGoDocument.addObjectAtTail( predicateNode);
      return predicateNode;
    } else {
      Iterator nodeItr = nodeList.iterator();
      while (nodeItr.hasNext()) {
        PredicateNode node = (PredicateNode) nodeItr.next();
        if (node.getName().equals( name)) {
          node.setIsExisting( true);
          return node;
        }
      }
      return null;
    }
  } // end createPredicateNodes


  private void createRuleLink( PredicateNode fromPredicateNode, PredicateNode toPredicateNode,
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
      Color ruleColor = (Color) RULE_COLOR_LIST.get( ruleIndex);
      RuleLink link = new RuleLink( fromPredicateNode, toPredicateNode);
      linkList.add( link);
      link.setMidLabel( new JGoText( ruleType));
      // arrow head
      link.setBrush( JGoBrush.makeStockBrush( ruleColor));
      // line
      link.setPen( new JGoPen( JGoPen.SOLID, 1, ruleColor));
      // label background
      ((JGoText) link.getMidLabel()).setBkColor( ruleColor);
      ((JGoText) link.getMidLabel()).setDraggable( false);

      jGoDocument.addObjectAtTail( link);
    } else {
      System.err.println( "createRuleLink: ruleType " + ruleType + " not handled");
    }
  } // end createRuleLink



} // end class ModelRulesView
