// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: NodeShapes.java,v 1.3 2004-07-27 21:58:08 taylor Exp $
//
// PlanWorks
//
// Will Taylor -- started 15June04
//

package gov.nasa.arc.planworks.viz;

import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoView;
import com.nwoods.jgo.JGoText;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.util.ColorMap;
import gov.nasa.arc.planworks.util.SwingWorker;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.ViewGenerics;
import gov.nasa.arc.planworks.viz.nodes.ObjectNode;
import gov.nasa.arc.planworks.viz.nodes.ResourceNode;
import gov.nasa.arc.planworks.viz.nodes.RuleInstanceNode;
import gov.nasa.arc.planworks.viz.nodes.TimelineNode;
import gov.nasa.arc.planworks.viz.nodes.TokenNode;
import gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork.ConstraintNode;
import gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork.VariableNode;
import gov.nasa.arc.planworks.viz.partialPlan.navigator.SlotNavNode;
import gov.nasa.arc.planworks.viz.partialPlan.timeline.SlotNode;


public  class NodeShapes extends JPanel {

  private static final int VIEW_X_INIT = 90;
  private static final int VIEW_Y_INIT = 40;
  private static final int VIEW_X_DELTA = 170;
  private static final int VIEW_Y_DELTA = 60;

  private JFrame nodeShapesFrame;
  private JGoView jGoView;
  private int idInt;
  private JMenuItem menuItem;

  public NodeShapes( JFrame nodeShapesFrame, JMenuItem menuItem) {
    super();
    this.nodeShapesFrame = nodeShapesFrame;
    this.menuItem = menuItem;
    idInt = 0;

    setLayout( new BoxLayout( this, BoxLayout.Y_AXIS));
    jGoView = new JGoView();
    jGoView.setBackground( ViewConstants.VIEW_BACKGROUND_COLOR);
    add( jGoView, BorderLayout.NORTH);
    jGoView.validate();
    jGoView.setVisible( true);
    this.setVisible( true);

    JGoText.setDefaultFontFaceName( "Monospaced");
    JGoText.setDefaultFontSize( ViewConstants.VIEW_FONT_SIZE);

    // Closes from title bar 
    nodeShapesFrame.addWindowListener( new WindowAdapter() {
        public final void windowClosing( final WindowEvent e) {
          NodeShapes.this.menuItem.setEnabled( true);
          PlanWorks.getPlanWorks().setNodeShapesFrame( null);
        }});

    // SwingUtilities.invokeLater( runInit);
    final SwingWorker worker = new SwingWorker() {
        public Object construct() {
          init();
          return null;
        }
    };
    worker.start();  
  } // end constructor

 
//   Runnable runInit = new Runnable() {
//       public void run() {
//         init();
//       }
//     };

  /**
   * <code>init</code> - wait for instance to become displayable, determine
   *                     appropriate font metrics, and render the JGo widgets
   *
   *    These functions are not done in the constructor to avoid:
   *    "Cannot measure text until a JGoView exists and is part of a visible window".
   *    called by componentShown method on the JFrame
   *    JGoView.setVisible( true) must be completed -- use SwingWorker in constructor
   */
  public void init() {
    // wait to become displayable
    if (! ViewGenerics.displayableWait( NodeShapes.this)) {
      return;
    }
    int x = VIEW_X_INIT;
    int y = VIEW_Y_INIT;
    // node locations at center
    TokenNode tokenNode = new TokenNode( "Merged Interval Token", new Integer( ++idInt),
                                         new Point( x, y), ColorMap.getColor( "green"));
    jGoView.getDocument().addObjectAtTail( tokenNode);

    x = x + VIEW_X_DELTA;
    tokenNode = new TokenNode( "Free Interval Token", new Integer( ++idInt),
                               new Point( x, y), ViewConstants.FREE_TOKEN_BG_COLOR);
    jGoView.getDocument().addObjectAtTail( tokenNode);

    x = x + VIEW_X_DELTA;
    tokenNode = new TokenNode( "Resource Transaction", new Integer( ++idInt),
                               new Point( x, y), ColorMap.getColor( "aquamarine"));
    jGoView.getDocument().addObjectAtTail( tokenNode);

    x = VIEW_X_INIT;
    y = y + VIEW_Y_DELTA;
    ObjectNode objectNode = new ObjectNode( "Object", new Integer( ++idInt),
                                            new Point( x, y),
                                            ColorMap.getColor( "yellow"));
    jGoView.getDocument().addObjectAtTail( objectNode);

    x = x + VIEW_X_DELTA;
    ResourceNode resourceNode = new ResourceNode( "Resource", new Integer( ++idInt),
                                                  new Point( x, y),
                                                  ColorMap.getColor( "seaGreen1"));
    jGoView.getDocument().addObjectAtTail( resourceNode);

    x = x + VIEW_X_DELTA;
    TimelineNode timelineNode = new TimelineNode( "Timeline", new Integer( ++idInt),
                                                  new Point( x, y),
                                                  ColorMap.getColor( "rosyBrown"));
    jGoView.getDocument().addObjectAtTail( timelineNode);

    x = VIEW_X_INIT;
    y = y + VIEW_Y_DELTA;
    RuleInstanceNode ruleInstanceNode =
      new RuleInstanceNode( "RuleInstance", new Integer( ++idInt),new Point( x, y),
                            ViewConstants.RULE_INSTANCE_BG_COLOR);
    jGoView.getDocument().addObjectAtTail( ruleInstanceNode);

    x = x + VIEW_X_DELTA;
    SlotNavNode slotNavNode = new SlotNavNode( "Slot (Navigator)", new Integer( ++idInt),
                                               new Point( x, y),
                                               ColorMap.getColor( "red"));
    jGoView.getDocument().addObjectAtTail( slotNavNode);

    x = x + VIEW_X_DELTA;
    StringBuffer labelBuf = new StringBuffer( "Slot (Timeline)");
    labelBuf.append( "\nkey=").append( (new Integer( ++idInt)).toString());
    String nodeLabel = labelBuf.toString();
    SlotNode slotNode = new SlotNode( nodeLabel, new Point( x, y),
                                      ColorMap.getColor( "skyBlue"));
    // node location at upper left corner
    slotNode.setLocation( x - ((int) (slotNode.getSize().getWidth() / 2)),
                          y - ((int) (slotNode.getSize().getHeight() / 2)));
    jGoView.getDocument().addObjectAtTail( slotNode);


    x = VIEW_X_INIT;
    y = y + VIEW_Y_DELTA;
    ConstraintNode constraintNode = new ConstraintNode( "Constraint", new Integer( ++idInt),
                                                        new Point( x, y),
                                                        ColorMap.getColor( "green"));
    jGoView.getDocument().addObjectAtTail( constraintNode);

    x = x + VIEW_X_DELTA;
    VariableNode variableNode = new VariableNode( "Variable", new Integer( ++idInt),
                                                  new Point( x, y),
                                                  ColorMap.getColor( "cyan"));
    jGoView.getDocument().addObjectAtTail( variableNode);

    x = VIEW_X_INIT;
    y = y + VIEW_Y_DELTA / 2;
    String text = "This window is external to the PlanWorks desktop";
    JGoText textObject = new JGoText( new Point( x, y), text);
    textObject.setResizable( false);
    textObject.setEditable( false);
    textObject.setDraggable( false);
    textObject.setSelectable( false);
    textObject.setBkColor( ViewConstants.VIEW_BACKGROUND_COLOR);
    jGoView.getDocument().addObjectAtTail( textObject);

//     int maxViewWidth = (int) jGoView.getDocumentSize().getWidth();
//     int maxViewHeight = (int) jGoView.getDocumentSize().getHeight();
//     nodeShapesFrame.setSize( maxViewWidth + ViewConstants.MDI_FRAME_DECORATION_WIDTH,
//                              maxViewHeight + ViewConstants.MDI_FRAME_DECORATION_HEIGHT);
  } // end init
  

} // end class NodeShapes
