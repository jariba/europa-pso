// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: DecisionView.java,v 1.2 2004-05-29 00:31:39 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 21may04
//

package gov.nasa.arc.planworks.viz.partialPlan.decision;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import javax.swing.BoxLayout;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.DbConstants;
import gov.nasa.arc.planworks.db.PwChoice;
import gov.nasa.arc.planworks.db.PwDecision;
import gov.nasa.arc.planworks.db.PwDomain;
import gov.nasa.arc.planworks.db.PwEntity;
import gov.nasa.arc.planworks.db.PwObject;
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.PwPlanningSequence;
import gov.nasa.arc.planworks.db.PwResource;
import gov.nasa.arc.planworks.db.PwResourceTransaction;
import gov.nasa.arc.planworks.db.PwSlot;
import gov.nasa.arc.planworks.db.PwTimeline;
import gov.nasa.arc.planworks.db.PwToken;
import gov.nasa.arc.planworks.db.impl.PwChoiceImpl;
import gov.nasa.arc.planworks.db.impl.PwDecisionImpl;
import gov.nasa.arc.planworks.mdi.MDIInternalFrame;
import gov.nasa.arc.planworks.util.ColorMap;
import gov.nasa.arc.planworks.util.MouseEventOSX;
import gov.nasa.arc.planworks.util.ResourceNotFoundException;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.ViewGenerics;
import gov.nasa.arc.planworks.viz.ViewListener;
import gov.nasa.arc.planworks.viz.VizViewOverview;
import gov.nasa.arc.planworks.viz.nodes.NodeGenerics;
import gov.nasa.arc.planworks.viz.nodes.TokenNode;
import gov.nasa.arc.planworks.viz.partialPlan.AskNodeByKey;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanView;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanViewSet;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanViewState;
import gov.nasa.arc.planworks.viz.partialPlan.navigator.NavigatorView;
import gov.nasa.arc.planworks.viz.viewMgr.ViewSet;
import gov.nasa.arc.planworks.viz.viewMgr.ViewableObject;


/**
 * <code>DecisionView</code> - render a partial plan's timelines and slots
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                  NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class DecisionView extends PartialPlanView {

  private static final int ROW_SEPARATOR_HEIGHT = 6;

  private PwPartialPlan partialPlan;
  private long startTimeMSecs;
  private ViewSet viewSet;
  private List decisionList; // element PwDecision
  private JTree decisionTree;
  private JScrollPane scrollPane;

  /**
   * <code>DecisionView</code> - constructor - 
   *                             Use SwingUtilities.invokeLater( runInit) to
   *                             properly render the JGo widgets
   *
   * @param partialPlan - <code>ViewableObject</code> - 
   * @param viewSet - <code>ViewSet</code> - 
   */
  public DecisionView( ViewableObject partialPlan,  ViewSet viewSet) {
    super( (PwPartialPlan) partialPlan, (PartialPlanViewSet) viewSet);
    decisionViewInit( (PwPartialPlan) partialPlan, viewSet);
    SwingUtilities.invokeLater( runInit);
  } // end constructor

  /**
   * <code>DecisionView</code> - constructor - no stepping, but order of constructors
   *                             must be maintained
   *
   * @param partialPlan - <code>ViewableObject</code> - 
   * @param viewSet - <code>ViewSet</code> - 
   * @param s - <code>PartialPlanViewState</code> - 
   */
  public DecisionView(ViewableObject partialPlan, ViewSet viewSet,
                      PartialPlanViewState s) {
    super( (PwPartialPlan) partialPlan, (PartialPlanViewSet) viewSet);
    decisionViewInit( (PwPartialPlan) partialPlan, viewSet);
    setState(s);
    SwingUtilities.invokeLater( runInit);
  }

  /**
   * <code>DecisionView</code> - constructor 
   *
   * @param partialPlan - <code>ViewableObject</code> - 
   * @param viewSet - <code>ViewSet</code> - 
   * @param viewListener - <code>ViewListener</code> - 
   */
  public DecisionView( ViewableObject partialPlan, ViewSet viewSet,
                       ViewListener viewListener) {
    super( (PwPartialPlan) partialPlan, (PartialPlanViewSet) viewSet);
    decisionViewInit( (PwPartialPlan) partialPlan, viewSet);
    if (viewListener != null) {
      addViewListener( viewListener);
    }
    SwingUtilities.invokeLater( runInit);
  }

  private void decisionViewInit(ViewableObject partialPlan, ViewSet viewSet) {
    this.partialPlan = (PwPartialPlan) partialPlan;
    this.viewSet = (PartialPlanViewSet) viewSet;
    decisionList = null;
    ViewListener viewListener = null;
    viewFrame = viewSet.openView( this.getClass().getName(), viewListener);
    // for PWTestHelper.findComponentByName
    this.setName( viewFrame.getTitle());

    setLayout( new BoxLayout( this, BoxLayout.Y_AXIS));
  }

  public void setState( PartialPlanViewState s) {
    super.setState( s);
    if(s == null) {
      return;
    }
  } // end setState

  Runnable runInit = new Runnable() {
      public final void run() {
        init();
      }
    };

  /**
   * <code>init</code> - wait for instance to become displayable, determine
   *                     appropriate font metrics, and render decisions
   *
   *    These functions are not done in the constructor to avoid:
   *    "Cannot measure text until a JGoView exists and is part of a visible window".
   *    called by componentShown method on the JFrame
   *    JGoView.setVisible( true) must be completed -- use runInit in constructor
   */
  public final void init() {
    handleEvent(ViewListener.EVT_INIT_BEGUN_DRAWING);
    // wait for DecisionView instance to become displayable
    if(!displayableWait()) {
      return;
    }

    this.computeFontMetrics( this);

    decisionTree = new DecisionTree( renderDecisions());

    scrollPane = new JScrollPane( decisionTree);
    add( scrollPane, BorderLayout.NORTH);
    this.setVisible( true);

//     System.err.println( "row cnt " + decisionTree.getRowCount() +
//                         " height " + decisionTree.getRowHeight());
    expandViewFrame( viewFrame, ViewConstants.DECISION_TREE_VIEW_WIDTH,
                     ViewConstants.DECISION_TREE_VIEW_HEIGHT);

    long stopTimeMSecs = System.currentTimeMillis();
    System.err.println( "   ... " + ViewConstants.DECISION_VIEW + " elapsed time: " +
                        (stopTimeMSecs -
                         PlanWorks.getPlanWorks().getViewRenderingStartTime
                         ( ViewConstants.DECISION_VIEW)) + " msecs.");
    startTimeMSecs = 0L;

    handleEvent(ViewListener.EVT_INIT_ENDED_DRAWING);
  } // end init

  /**
   * <code>getDecisionList</code> - constructor 
   *
   */
  public final List getDecisionList() {
    return decisionList;
  }

  private DefaultMutableTreeNode renderDecisions() {
    try {
      PwPlanningSequence planSequence = PlanWorks.getPlanWorks().getPlanSequence( partialPlan);
      decisionList = planSequence.getOpenDecisionsForStep( partialPlan.getStepNumber());
    } catch ( ResourceNotFoundException rnfExcep) {
      int index = rnfExcep.getMessage().indexOf( ":");
      JOptionPane.showMessageDialog
        (PlanWorks.getPlanWorks(), rnfExcep.getMessage().substring( index + 1),
         "Resource Not Found Exception", JOptionPane.ERROR_MESSAGE);
      System.err.println( rnfExcep);
      rnfExcep.printStackTrace();
    }

//     // test data => basic-model-res/step50
//     decisionList = new ArrayList();
//     PwDecisionImpl decisionImpl = null;
//     // object -- choice token
//     decisionImpl = new PwDecisionImpl( new Integer( 1486), 0, new Integer( 47), false,
//                                        partialPlan);
//     decisionImpl.makeChoices( "14878,0,163");
//     decisionList.add( decisionImpl);
//     // resource -- choice token
//     decisionImpl = new PwDecisionImpl( new Integer( 1487), 0, new Integer( 21), false,
//                                        partialPlan);
//     decisionImpl.makeChoices( "14879,0,717");
//     decisionList.add( decisionImpl);
//     // timeline -- choice token
//     decisionImpl = new PwDecisionImpl( new Integer( 1488), 0, new Integer( 19), false,
//                                        partialPlan);
//     decisionImpl.makeChoices( "14880,0,207");
//     decisionList.add( decisionImpl);
//     // interval token -- choice interval token
//     decisionImpl = new PwDecisionImpl( new Integer( 1550), 1, new Integer( 510), false,
//                                            partialPlan);
//     decisionImpl.makeChoices( "14881,0,600");
//     decisionList.add( decisionImpl);
//     // resource transaction -- choice value, interval tokenId
//     decisionImpl = new PwDecisionImpl( new Integer( 1615), 1, new Integer( 717), false,
//                                        partialPlan);
//     decisionImpl.makeChoices( "14882,1,600,10.0");
//     decisionList.add( decisionImpl);
//     // variable -- choice enum domain
//     decisionImpl = new PwDecisionImpl( new Integer( 1616), 2, new Integer( 722), false,
//                                        partialPlan);
//     decisionImpl.makeChoices( "14883,2,E,1 2 3");
//     decisionList.add( decisionImpl);
//     // variable -- choice int domain
//     decisionImpl = new PwDecisionImpl( new Integer( 1617), 2, new Integer( 720), false,
//                                        partialPlan);
//     decisionImpl.makeChoices( "14884,2,I,10,20");
//     decisionList.add( decisionImpl);
//     // close
//     decisionImpl = new PwDecisionImpl( new Integer( 1618), 0, new Integer( 21), false,
//                                        partialPlan);
//     decisionImpl.makeChoices( "14885,3");
//     decisionList.add( decisionImpl);

    DefaultMutableTreeNode top = new DefaultMutableTreeNode();
    Iterator decisionItr = decisionList.iterator();
    while (decisionItr.hasNext()) {
      PwDecision decision = (PwDecision) decisionItr.next();
      DecisionNode decisionNode = new DecisionNode( decision);
      top.add( decisionNode);

//       System.err.println( "\nid " + decision.getId() + " entityId " + decision.getEntityId() +
//                           " isUnit " + decision.isUnit() + " type " + decision.getType());

      List choiceList = decision.getChoices();
      // System.err.println( "  choices " + choiceList.size());
      Iterator choiceItr = choiceList.iterator();
      while (choiceItr.hasNext()) {
        PwChoice choice = (PwChoice) choiceItr.next();

        decisionNode.add( new ChoiceNode( choice));

        switch ( choice.getType()) {
        case DbConstants.C_TOKEN:
          System.err.println( "  DbConstants.C_TOKEN " + choice.toString());
          break;
        case DbConstants.C_VALUE:
          System.err.println( "  DbConstants.C_VALUE " + choice.toString());
          break;
        case DbConstants.C_DOMAIN:
          System.err.print( "  DbConstants.C_DOMAIN " + choice.toString());
          break;
        case DbConstants.C_CLOSE:
          System.err.println( "  DbConstants.C_CLOSE " + choice.toString());
          break;
        }
      } // end while choiceList
    } // end while decisionList
    return top;
  } // end renderDecisions


  class DecisionTree extends JTree {

    public DecisionTree( DefaultMutableTreeNode rootNode) {
      super( rootNode);
      setBackground( ViewConstants.VIEW_BACKGROUND_COLOR);
      setForeground( ColorMap.getColor( "black"));
      setFont( DecisionView.this.getFont());
      // causes node hierarchy to be lost
      // setRootVisible( false);

      // Enable tool tips.
      ToolTipManager.sharedInstance().registerComponent( this);

      setCellRenderer( new DecisionTreeCellRenderer());
      setRowHeight( fontMetrics.getHeight() + ROW_SEPARATOR_HEIGHT);

      MouseListener mouseListener = new MouseAdapter() {
          public void mousePressed( MouseEvent evt) {
            int selRow = DecisionTree.this.getRowForLocation( evt.getX(), evt.getY());
            TreePath selPath = DecisionTree.this.getPathForLocation( evt.getX(), evt.getY());
            if (selRow != -1) {
              if (evt.getClickCount() == 1) {
                if (MouseEventOSX.isMouseRightClick( evt.getModifiers(), PlanWorks.isMacOSX())) {
                  doSingleClick( selRow, selPath, evt.getPoint());
                }
              } else if (evt.getClickCount() == 2) {
                doDoubleClick( selRow, selPath, evt.getPoint());
              }
            } else {
              // background clicks
              if (evt.getClickCount() == 1) {
                if (MouseEventOSX.isMouseLeftClick( evt.getModifiers(),
                                                    PlanWorks.isMacOSX())) {
                  DecisionTree.this.getSelectionModel().clearSelection();
                } else if (MouseEventOSX.isMouseRightClick( evt.getModifiers(),
                                                            PlanWorks.isMacOSX())) {
                 mouseRightPopupMenu( evt.getPoint());
                }
              }
            }
          }
        };
      addMouseListener( mouseListener);
    } // end constructor

    public void doSingleClick( int selRow, TreePath selPath, Point viewCoords) {
      // System.err.println( "doSingleClick row " + selRow + " path " + selPath);
      Object lastComponent = selPath.getLastPathComponent() ;
      PwEntity entity = null;
      if (lastComponent instanceof DecisionView.DecisionNode) {
        PwDecision decision = ((DecisionView.DecisionNode) lastComponent).getDecision();
        // System.err.println( "doSingleClick popup: " + "Find " + decision.toString());
        if (decision.getType() == DbConstants.D_OBJECT) {
          if (partialPlan.getTimeline( decision.getEntityId()) != null) {
            entity = partialPlan.getTimeline( decision.getEntityId());
          } else if (partialPlan.getResource( decision.getEntityId()) != null) {
            entity = partialPlan.getResource( decision.getEntityId());
          } else {
            entity = partialPlan.getObject( decision.getEntityId());
          }
        } else if (decision.getType() == DbConstants.D_TOKEN) {
          entity = partialPlan.getToken( decision.getEntityId());
        } else if (decision.getType() == DbConstants.D_VARIABLE) {
          entity = partialPlan.getVariable( decision.getEntityId());
        }

      } else if (lastComponent instanceof DecisionView.ChoiceNode) {
        PwChoice choice = ((DecisionView.ChoiceNode) lastComponent).getChoice();
        if ((choice.getType() == DbConstants.C_TOKEN) ||
            (choice.getType() == DbConstants.C_VALUE)) {
          // System.err.println( "doSingleClick popup: " + "Find " + choice.toString());
          entity = partialPlan.getToken( choice.getTokenId());
        }
      }
      mouseRightEntityPopupMenu( entity, viewCoords);
    } // end doSingleClick

    public void doDoubleClick( int selRow, TreePath selPath, Point viewCoords) {
      // System.err.println( "doDoubleClick row " + selRow + " path " + selPath);
    } // end doDoubleClick


    public void mouseRightEntityPopupMenu( final PwEntity entity, final Point viewCoords) {
      if (entity == null) {
        return;
      }
      JPopupMenu mouseRightPopup = new JPopupMenu();
      String partialPlanName = partialPlan.getPartialPlanName();
      PwPlanningSequence planSequence = PlanWorks.getPlanWorks().getPlanSequence( partialPlan);

      createAnOpenViewFindItem( partialPlan, partialPlanName, planSequence, mouseRightPopup,
                                ViewConstants.CONSTRAINT_NETWORK_VIEW, entity.getId());

      JMenuItem navigatorItem = new JMenuItem( "Open Navigator View");
      navigatorItem.addActionListener( new ActionListener() {
          public void actionPerformed( ActionEvent evt) {
            String viewSetKey = DecisionView.this.getNavigatorViewSetKey();
            MDIInternalFrame navigatorFrame =
              DecisionView.this.openNavigatorViewFrame( viewSetKey);
            Container contentPane = navigatorFrame.getContentPane();
            contentPane.add( new NavigatorView( entity, partialPlan,
                                                DecisionView.this.getViewSet(), viewSetKey,
                                                navigatorFrame));
          }
        });
      mouseRightPopup.add( navigatorItem);

      if ((entity instanceof PwResource) || (entity instanceof PwResourceTransaction)) {
        createAnOpenViewFindItem( partialPlan, partialPlanName, planSequence, mouseRightPopup,
                                  ViewConstants.RESOURCE_PROFILE_VIEW, entity.getId());
        createAnOpenViewFindItem( partialPlan, partialPlanName, planSequence, mouseRightPopup,
                                  ViewConstants.RESOURCE_TRANSACTION_VIEW, entity.getId());
      }
      if (entity instanceof PwToken) {
        createAnOpenViewFindItem( partialPlan, partialPlanName, planSequence, mouseRightPopup,
                                  ViewConstants.TEMPORAL_EXTENT_VIEW, entity.getId());
      }
      if ((entity instanceof PwTimeline) || (entity instanceof PwToken)) {
        createAnOpenViewFindItem( partialPlan, partialPlanName, planSequence, mouseRightPopup,
                                  ViewConstants.TIMELINE_VIEW, entity.getId());
      }
      if (entity instanceof PwToken) {
        createAnOpenViewFindItem( partialPlan, partialPlanName, planSequence, mouseRightPopup,
                                  ViewConstants.TOKEN_NETWORK_VIEW, entity.getId());
      }
      ViewGenerics.showPopupMenu( mouseRightPopup, this, viewCoords);
    } // end mouseRightEntityPopupMenu


    // background Mouse-Right click
    public void mouseRightPopupMenu( Point viewCoords) {
      String partialPlanName = partialPlan.getPartialPlanName();
      PwPlanningSequence planSequence = PlanWorks.getPlanWorks().getPlanSequence( partialPlan);
      JPopupMenu mouseRightPopup = new JPopupMenu();

      JMenuItem findByKeyItem = new JMenuItem( "Find Decision by Key");
      createFindByKeyItem( findByKeyItem);
      mouseRightPopup.add( findByKeyItem);

      createOpenViewItems( partialPlan, partialPlanName, planSequence, mouseRightPopup,
                           ViewConstants.DECISION_VIEW);
      if (viewSet.doesViewFrameExist( ViewConstants.NAVIGATOR_VIEW)) {
        mouseRightPopup.addSeparator();
        JMenuItem closeWindowsItem = new JMenuItem( "Close Navigator Views");
        createCloseNavigatorWindowsItem( closeWindowsItem);
        mouseRightPopup.add( closeWindowsItem);
      }
      createAllViewItems( partialPlan, partialPlanName, planSequence, mouseRightPopup);

      ViewGenerics.showPopupMenu( mouseRightPopup, this, viewCoords);
    } // end mouseRightPopupMenu

    private void createFindByKeyItem( JMenuItem findByKeyItem) {
      findByKeyItem.addActionListener( new ActionListener() {
          public void actionPerformed( ActionEvent evt) {
            AskNodeByKey nodeByKeyDialog =
              new AskNodeByKey( "Find Decision by Key", "key (int)", DecisionView.this);
            PwDecision decision = (PwDecision) nodeByKeyDialog.getEntity();
            if (decision != null) {
              // System.err.println( "createFindByKeyItem: entity " + entity);
              findAndSelectDecision( decision);
            }
          }
        });
    } // end createFindByKeyItem

  } // end class DecisionTree

  private void findAndSelectDecision( PwDecision decisionToFind) {
    int maxRows = decisionTree.getRowCount();
    int foundRow = -1;
    for (int row = 0; row < maxRows; row++) {
      TreePath treePath = decisionTree.getPathForRow( row);
      // System.err.println( "row " + row + " treePath " + treePath);
      Object lastComponent = treePath.getLastPathComponent() ;
      if (lastComponent instanceof DecisionNode) {
        PwDecision decision = ((DecisionNode) lastComponent).getDecision();
        if (decision.getId().equals( decisionToFind.getId())) {
          foundRow = row;
          System.err.println( "DecisionView found decision: '" +
                              decisionToFind.toString() + "' (key=" +
                              decisionToFind.getId().toString() + ")");
          scrollPane.getVerticalScrollBar().
            setValue( Math.max( 0, (int) ((row * decisionTree.getRowHeight()) -
                                          (scrollPane.getViewport().getHeight() / 2))));
          decisionTree.getSelectionModel().clearSelection();
          decisionTree.getSelectionModel().addSelectionPath( treePath);
          decisionTree.repaint();
          break;
        }
      }
    }
    if (foundRow == -1) {
      String message = "Decision (key=" + decisionToFind.getId().toString() + ") not found.";
      JOptionPane.showMessageDialog( PlanWorks.getPlanWorks(), message,
                                     "Decision Not Found in DecisionView",
                                     JOptionPane.ERROR_MESSAGE);
      System.err.println( message);
      return;
    }
  } // end findAndSelectDecision

  class DecisionTreeCellRenderer extends DefaultTreeCellRenderer {

    public DecisionTreeCellRenderer() {
      super();
    }

    public Component getTreeCellRendererComponent( JTree tree, Object value, boolean selected,
                                                   boolean expanded, boolean leaf,
                                                   int row, boolean hasFocus) {
      super.getTreeCellRendererComponent( tree, value, selected, expanded, leaf, row, hasFocus);
      setBackgroundNonSelectionColor( ViewConstants.VIEW_BACKGROUND_COLOR);
      setBorderSelectionColor( ViewConstants.PRIMARY_SELECTION_COLOR);
      String toolTip = null;
      if (value instanceof DecisionView.DecisionNode) {
        PwDecision decision = ((DecisionView.DecisionNode) value).getDecision();
        int indx = decision.toString().indexOf( ";");
        toolTip = "Mouse-Right: View " + decision.toString().substring( indx + 2);
      } else if (value instanceof DecisionView.ChoiceNode) {
        PwChoice choice = ((DecisionView.ChoiceNode) value).getChoice();
        if (choice.getType() == DbConstants.C_TOKEN) {
          toolTip = "Mouse-Right: View " + choice.toString();
        } else if (choice.getType() == DbConstants.C_VALUE) {
          int indx = choice.toString().indexOf( ";");
          String choiceString = choice.toString().substring( 0, indx);
          if (choiceString.indexOf( "key=-1") == -1) {
            toolTip = "Mouse-Right: View " + choiceString;
          }
        }
      }
      setToolTipText( toolTip);

      return this;
    }

  } // end class DecisionTreeCellRenderer


  class DecisionNode extends DefaultMutableTreeNode {

    private PwDecision decision;
    
    public DecisionNode( PwDecision decision) {
      super( decision);
      this.decision = decision;
    }

    public final PwDecision getDecision() {
      return decision;
    }

  } // end class DecisionNode


  class ChoiceNode extends DefaultMutableTreeNode {

    private PwChoice choice;
    
    public ChoiceNode( PwChoice choice) {
      super( choice);
      this.choice = choice;
    }

    public final PwChoice getChoice() {
      return choice;
    }

  } // end class ChoiceNode


} // end class DecisionView
 



