// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: SequenceStepsView.java,v 1.27 2004-05-08 01:44:17 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 24sep03
//

package gov.nasa.arc.planworks.viz.sequence.sequenceSteps;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import javax.swing.BoxLayout;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoBrush;
import com.nwoods.jgo.JGoDocument;
import com.nwoods.jgo.JGoEllipse;
import com.nwoods.jgo.JGoObject;
import com.nwoods.jgo.JGoPen;
import com.nwoods.jgo.JGoText;
import com.nwoods.jgo.JGoView;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.CreateSequenceViewThread;
import gov.nasa.arc.planworks.SequenceViewMenuItem;
import gov.nasa.arc.planworks.db.PwListener;
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.PwPlanningSequence;
import gov.nasa.arc.planworks.mdi.MDIInternalFrame;
import gov.nasa.arc.planworks.util.ColorMap;
import gov.nasa.arc.planworks.util.MouseEventOSX;
import gov.nasa.arc.planworks.util.ResourceNotFoundException;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.ViewGenerics;
import gov.nasa.arc.planworks.viz.ViewListener;
import gov.nasa.arc.planworks.viz.VizViewOverview;
import gov.nasa.arc.planworks.viz.nodes.NodeGenerics;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanView;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanViewSet;
import gov.nasa.arc.planworks.viz.sequence.SequenceView;
import gov.nasa.arc.planworks.viz.sequence.SequenceViewSet;
import gov.nasa.arc.planworks.viz.util.StepButton;
import gov.nasa.arc.planworks.viz.viewMgr.ViewableObject;
import gov.nasa.arc.planworks.viz.viewMgr.ViewSet;


/**
 * <code>SequenceStepsView</code> - render a histogram of plan data base size
 *                        over sequence steps
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                  NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class SequenceStepsView extends SequenceView {

  /**
   * constant <code>DB_TOKENS</code>
   *
   */
  protected static final String DB_TOKENS = "numTokens";

  /**
   * constant <code>DB_VARIABLES</code>
   *
   */
  protected static final String DB_VARIABLES = "numVariables";

  /**
   * constant <code>DB_CONSTRAINTS</code>
   *
   */
  protected static final String DB_CONSTRAINTS = "numConstraints";

  /**
   * constant <code>TOKENS_BG_COLOR</code>
   *
   */
  public static final Color TOKENS_BG_COLOR = ColorMap.getColor( "seaGreen1");

  /**
   * constant <code>VARIABLES_BG_COLOR</code>
   *
   */
  public static final Color VARIABLES_BG_COLOR = ColorMap.getColor( "skyBlue");

  /**
   * constant <code>DB_CONSTRAINTS_BG_COLOR</code>
   *
   */
  public static final Color DB_CONSTRAINTS_BG_COLOR = ColorMap.getColor( "lightYellow");

  private long startTimeMSecs;
  private PwPlanningSequence planSequence;
  private ViewSet viewSet;
  private SequenceStepsJGoView jGoView;
  private JGoDocument document;
  private Graphics graphics;
  private FontMetrics fontMetrics;
  private Font font;
  private float heightScaleFactor;
  private List stepElementList;
  private List stepRepresentedList;
  private List statusIndicatorList;
  private List stepNumberList;

  /**
   * variable <code>selectedStepElement</code>
   *
   */
  protected StepElement selectedStepElement;

  /**
   * <code>SequenceChangeListener</code> - 
   *
   */
  class SequenceChangeListener implements PwListener {

    private SequenceStepsView view;

    /**
     * <code>SequenceChangeListener</code> - constructor 
     *
     * @param view - <code>SequenceStepsView</code> - 
     */
    public SequenceChangeListener( final SequenceStepsView view) {
      this.view = view;
    }

    /**
     * <code>fireEvent</code>
     *
     * @param evtName - <code>String</code> - 
     */
    public final void fireEvent( final String evtName) {
      if (evtName.equals( PwPlanningSequence.EVT_PP_ADDED) ||
          evtName.equals( PwPlanningSequence.EVT_PP_REMOVED)) {
        //         System.err.println( "SequenceStepsView.fireEvent  view.redraw()");
        view.redraw();
      }
    }
  }

  /**
   * <code>SequenceStepsView</code> - constructor 
   *                             Use SwingUtilities.invokeLater( runInit) to
   *                             properly render the JGo widgets
   *
   * @param planSequence - <code>ViewableObject</code> - 
   * @param viewSet - <code>ViewSet</code> - 
   */
  public SequenceStepsView( final ViewableObject planSequence,  final ViewSet viewSet) {
    super( (PwPlanningSequence) planSequence, (SequenceViewSet) viewSet);
    sequenceStepsInit( planSequence, viewSet);
  }

  /**
   * <code>SequenceStepsView</code> - constructor 
   *
   * @param planSequence - <code>ViewableObject</code> - 
   * @param viewSet - <code>ViewSet</code> - 
   * @param viewListener - <code>ViewListener</code> - 
   */
  public SequenceStepsView( final ViewableObject planSequence,  final ViewSet viewSet,
                            final ViewListener viewListener) {
    super( (PwPlanningSequence) planSequence, (SequenceViewSet) viewSet);
    sequenceStepsInit( planSequence, viewSet);
    if (viewListener != null) {
      addViewListener( viewListener);
    }
  }

  private void sequenceStepsInit( final ViewableObject planSequence,  final ViewSet viewSet) {
    this.planSequence = (PwPlanningSequence) planSequence;
    this.viewSet = (SequenceViewSet) viewSet;
    statusIndicatorList = new ArrayList();
    stepRepresentedList = new ArrayList();
    stepNumberList = new ArrayList();
    setLayout( new BoxLayout( this, BoxLayout.Y_AXIS));

    stepElementList = new ArrayList();
    selectedStepElement = null;

    jGoView = new SequenceStepsJGoView();
    jGoView.setBackground( ViewConstants.VIEW_BACKGROUND_COLOR);
    add( jGoView, BorderLayout.NORTH);
    jGoView.validate();
    jGoView.setVisible( true);
    this.setVisible( true);
    ViewListener viewListener = null;
    viewFrame = viewSet.openView( this.getClass().getName(), viewListener);
    // for PWTestHelper.findComponentByName
    this.setName( viewFrame.getTitle());

    ((PwPlanningSequence) planSequence).addListener( new SequenceChangeListener( this));

    SwingUtilities.invokeLater( runInit);
  } // end constructor


  Runnable runInit = new Runnable() {
      public final void run() {
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
    handleEvent(ViewListener.EVT_INIT_BEGUN_DRAWING);
    // wait for ConstraintNetworkView instance to become displayable
    while (! this.isDisplayable()) {
      try {
        Thread.currentThread().sleep( 50);
      } catch (InterruptedException excp) {
      }
      // System.err.println( "constraintNetworkView displayable " + this.isDisplayable());
    }
    graphics = this.getGraphics();
    font = new Font( ViewConstants.TIMELINE_VIEW_FONT_NAME,
                     ViewConstants.TIMELINE_VIEW_FONT_STYLE,
                     ViewConstants.TIMELINE_VIEW_FONT_SIZE);
    // does nothing
    // jGoView.setFont( font);
    fontMetrics = graphics.getFontMetrics( font);

    // graphics.dispose();

    document = jGoView.getDocument();

    heightScaleFactor = computeHeightScaleFactor();
    renderHistogram();

    expandViewFrame( viewFrame,
                     (int) jGoView.getDocumentSize().getWidth(),
                     (int) jGoView.getDocumentSize().getHeight());
    long stopTimeMSecs = System.currentTimeMillis();
    System.err.println( "   ... " + ViewConstants.SEQUENCE_STEPS_VIEW + " elapsed time: " +
                        (stopTimeMSecs -
                         PlanWorks.getPlanWorks().getViewRenderingStartTime()) + " msecs.");
    startTimeMSecs = 0L;
    handleEvent(ViewListener.EVT_INIT_ENDED_DRAWING);
  } // end init


  /**
   * <code>redraw</code>
   *
   */
  public final void redraw() {
    Thread thread = new RedrawViewThread();
    thread.setPriority( Thread.MIN_PRIORITY);
    thread.start();
  }

  class RedrawViewThread extends Thread {

    public RedrawViewThread() {
    }  // end constructor

    public final void run() {
      redrawView();
    } //end run

  } // end class RedrawViewThread

  private void redrawView() {
    handleEvent(ViewListener.EVT_REDRAW_BEGUN_DRAWING);
    System.err.println( "Redrawing Sequence Steps View ...");
    if (startTimeMSecs == 0L) {
      startTimeMSecs = System.currentTimeMillis();
    }
    try {
      ViewGenerics.setRedrawCursor( viewFrame);

      //document.deleteContents();
      for (Iterator it = statusIndicatorList.listIterator(); it.hasNext();) {
        document.removeObject( (JGoObject) it.next());
      }
      for(Iterator it = stepNumberList.listIterator(); it.hasNext();) {
        document.removeObject((JGoObject) it.next());
      }
      statusIndicatorList.clear();
    
      renderHistogram();

      expandViewFrame( viewFrame,
                       (int) jGoView.getDocumentSize().getWidth(),
                       (int) jGoView.getDocumentSize().getHeight());

    } finally {
      ViewGenerics.resetRedrawCursor( viewFrame);
    }
    long stopTimeMSecs = System.currentTimeMillis();
    System.err.println( "   ... " + ViewConstants.SEQUENCE_STEPS_VIEW + " elapsed time: " +
                        (stopTimeMSecs - startTimeMSecs) + " msecs.");
    startTimeMSecs = 0L;
    handleEvent(ViewListener.EVT_REDRAW_ENDED_DRAWING);
  } // end redrawView


  /**
   * <code>getPlanningSequence</code>
   *
   * @return - <code>PwPlanningSequence</code> - 
   */
  public final PwPlanningSequence getPlanningSequence() {
    return planSequence;
  }

  /**
   * <code>getJGoDocument</code>
   *
   * @return - <code>JGoDocument</code> - 
   */
  public final JGoDocument getJGoDocument()  {
    return this.document;
  }

  /**
   * <code>getJGoView</code> - needed for PlanWorksTest
   *
   * @return - <code>JGoView</code> - 
   */
  public final JGoView getJGoView()  {
    return jGoView;
  }

  /**
   * <code>getFontMetrics</code>
   *
   * @return - <code>FontMetrics</code> - 
   */
  public final FontMetrics getFontMetrics()  {
    return fontMetrics;
  }

  /**
   * <code>getStepElementList</code>
   *
   * @return - <code>List</code> - of List of StepElement
   */
  public final List getStepElementList() {
    return stepElementList;
  }

  /**
   * <code>getSelectedStepElement</code>
   *
   * @return - <code>StepElement</code> - 
   */
  public final StepElement getSelectedStepElement() {
    return selectedStepElement;
  }

  /**
   * <code>setSelectedStepElement</code>
   *
   * @param element - <code>StepElement</code> - 
   */
  public final void setSelectedStepElement( final StepElement element) {
    selectedStepElement = element;
  }

  private float computeHeightScaleFactor() {
    int maxDbSize = 0;
    Iterator sizeItr = planSequence.getPlanDBSizeList().iterator();
    while (sizeItr.hasNext()) {
      int [] planDbSizes = (int[]) sizeItr.next();
      int dbSize = planDbSizes[0] + planDbSizes[1] + planDbSizes[2];
//       System.err.println( "dbSize " + dbSize + " " + planDbSizes[0] +
//                           " " + planDbSizes[1] + " " + planDbSizes[2]);
      if (dbSize > maxDbSize) {
        maxDbSize = dbSize;
      }
    }
//     System.err.println( "computeHeightScaleFactor: " +
//                         ViewConstants.STEP_VIEW_Y_MAX / (float) maxDbSize +
//                         " maxDbSize " + maxDbSize);
    return ViewConstants.STEP_VIEW_Y_MAX / (float) maxDbSize;
  } // end computeHeightScaleFactor

  private void renderHistogram() {
    // System.err.println( "stepCount " + planSequence.getStepCount());
    // System.err.println( "stepNumbers " + planSequence.getPartialPlanNamesList());
    
    int x = ViewConstants.STEP_VIEW_X_INIT;
    int stepNumber = 0;
    Iterator sizeItr = planSequence.getPlanDBSizeList().iterator();
    while (sizeItr.hasNext()) {
      int y = ViewConstants.STEP_VIEW_Y_INIT;
      
      addStepStatusIndicator( stepNumber, x, document);
      
      String partialPlanName = "step".concat( String.valueOf( stepNumber));
      int [] planDbSizes = (int[]) sizeItr.next();
      if(stepRepresentedList.contains(partialPlanName)) {
        ArrayList elementList = (ArrayList) stepElementList.get(stepNumber);
        for(int i = 0; i < 3; i++) {
          int height = Math.max(1, (int) (planDbSizes[i] * heightScaleFactor));
          StepElement elem = (StepElement) elementList.get(i);
          elem.setHeight(height);
          elem.setLocation(x, y);
          y += height;
        }
      }
      if(!stepRepresentedList.contains( partialPlanName)) {
        int height = Math.max( 1, (int) (planDbSizes[0] * heightScaleFactor));
        List elementList = new ArrayList();
        StepElement stepElement = new StepElement( x, y, height, DB_TOKENS,
                                                   planDbSizes[0], TOKENS_BG_COLOR,
                                                   partialPlanName, planSequence, this);
        elementList.add( stepElement);
        document.addObjectAtTail( stepElement);
        y += height;
        
        height = Math.max( 1, (int) (planDbSizes[1] * heightScaleFactor));
        stepElement = new StepElement( x, y, height, DB_VARIABLES,
                                       planDbSizes[1], VARIABLES_BG_COLOR,
                                       partialPlanName, planSequence, this);
        elementList.add( stepElement);
        document.addObjectAtTail( stepElement);
        y += height;
        
        height = Math.max( 1, (int) (planDbSizes[2] * heightScaleFactor));
        stepElement = new StepElement( x, y, height, DB_CONSTRAINTS,
                                       planDbSizes[2], DB_CONSTRAINTS_BG_COLOR,
                                       partialPlanName, planSequence, this);
        elementList.add( stepElement);
        document.addObjectAtTail( stepElement);
        y += height;

        stepElementList.add( elementList);
        // display step number for every 10th step
//         if ((stepNumber % 10) == 0) {
//           JGoText textObject = new JGoText( new Point( x, y + 4), String.valueOf( stepNumber));
//           textObject.setResizable( false);
//           textObject.setEditable( false);
//           textObject.setDraggable( false);
//           textObject.setBkColor( ViewConstants.VIEW_BACKGROUND_COLOR);
//           document.addObjectAtTail( textObject);
//        }
        stepRepresentedList.add( partialPlanName);
      }
      if ((stepNumber % 10) == 0) {
        JGoText textObject = new JGoText( new Point( x, y + 4), String.valueOf( stepNumber));
        textObject.setResizable( false);
        textObject.setEditable( false);
        textObject.setDraggable( false);
        textObject.setBkColor( ViewConstants.VIEW_BACKGROUND_COLOR);
        stepNumberList.add(textObject);
        document.addObjectAtTail( textObject);
      }
      x += ViewConstants.STEP_VIEW_STEP_WIDTH;
      stepNumber++;
    }
  } // end renderHistogram

  private void addStepStatusIndicator( final int stepNum, final int x, final JGoDocument doc) {
    JGoEllipse statusIndicator = new JGoEllipse(new Point(x + 4, 
                                                          ViewConstants.STEP_VIEW_Y_INIT - 6),
                                                new Dimension(4, 4));
    statusIndicator.setDraggable( false);
    statusIndicator.setResizable( false);
    statusIndicator.setSelectable( false);
    Color color = (planSequence.isPartialPlanInDb(stepNum) ? ColorMap.getColor("green3") :
                   (planSequence.isPartialPlanInFilesystem(stepNum) ? ColorMap.getColor("yellow") :
                    ColorMap.getColor("red")));
    statusIndicator.setPen( JGoPen.Null);
    statusIndicator.setBrush( JGoBrush.makeStockBrush( color));
    doc.addObjectAtTail( statusIndicator);
    statusIndicatorList.add( statusIndicator);
  }


  /**
   * <code>SequenceStepsJGoView</code> - subclass JGoView to add doBackgroundClick
   *
   */
  public class SequenceStepsJGoView extends JGoView {

    /**
     * <code>SequenceStepsJGoView</code> - constructor 
     *
     */
    public SequenceStepsJGoView() {
      super();
    }

    /**
     * <code>doBackgroundClick</code> - Mouse-Right pops up menu:
     *
     * @param modifiers - <code>int</code> - 
     * @param docCoords - <code>Point</code> - 
     * @param viewCoords - <code>Point</code> - 
     */
    public final void doBackgroundClick( final int modifiers, final Point docCoords,
                                         final Point viewCoords) {
      if (MouseEventOSX.isMouseLeftClick( modifiers, PlanWorks.isMacOSX())) {
        // do nothing
      } else if (MouseEventOSX.isMouseRightClick( modifiers, PlanWorks.isMacOSX())) {

        mouseRightPopupMenu( viewCoords);

      }
    } // end doBackgroundClick

  } // end class SequenceStepsJGoView

  private void mouseRightPopupMenu( final Point viewCoords) {
    JPopupMenu mouseRightPopup = new JPopupMenu();

//     JMenuItem modelRulesViewItem = new JMenuItem( "Open Model Rules View");
//     createModelRulesViewItem( modelRulesViewItem, this, viewCoords);
//     mouseRightPopup.add( modelRulesViewItem);

    JMenuItem overviewWindowItem = new JMenuItem( "Overview Window");
    createOverviewWindowItem( overviewWindowItem, this, viewCoords);
    mouseRightPopup.add( overviewWindowItem);

    JMenuItem refreshItem = new JMenuItem("Refresh");
    createRefreshItem( refreshItem, this);
    mouseRightPopup.add( refreshItem);

    List partialPlanViewList = null;
    if ((selectedStepElement != null) &&
        ((partialPlanViewList = getPartialPlanViewList()).size() != 0)) {
      String stepTitle = selectedStepElement.getPartialPlanName() + " Active Views";
      String stepBackwardTitle = "Step Backward " + stepTitle;
      JMenuItem stepBackwardItem = new JMenuItem( stepBackwardTitle);
      createStepBackwardItem( stepBackwardItem, partialPlanViewList);
      mouseRightPopup.add( stepBackwardItem);

      String stepForwardTitle = "Step Forward " + stepTitle;
      JMenuItem stepForwardItem = new JMenuItem( stepForwardTitle);
      createStepForwardItem( stepForwardItem, partialPlanViewList);
      mouseRightPopup.add( stepForwardItem);
    }

    createZoomItem( jGoView, zoomFactor, mouseRightPopup, this);

    createCloseHideShowViewItems( mouseRightPopup);

    NodeGenerics.showPopupMenu( mouseRightPopup, this, viewCoords);
  } // end mouseRightPopupMenu


  private void createOverviewWindowItem( JMenuItem overviewWindowItem,
                                         final SequenceStepsView sequenceStepsView,
                                         final Point viewCoords) {
    overviewWindowItem.addActionListener( new ActionListener() { 
        public final void actionPerformed( final ActionEvent evt) {
          VizViewOverview currentOverview =
            ViewGenerics.openOverviewFrame( ViewConstants.SEQUENCE_STEPS_VIEW, planSequence,
                                            sequenceStepsView, viewSet, jGoView, viewCoords);
          if (currentOverview != null) {
            overview = currentOverview;
          }
        }
      });
  } // end createOverviewWindowItem


//   private void createModelRulesViewItem( JMenuItem modelRulesViewItem,
//                                          final SequenceStepsView sequenceStepsView,
//                                          final Point viewCoords) {
//     modelRulesViewItem.addActionListener( new ActionListener() { 
//         public final void actionPerformed( ActionEvent evt) {
//           String seqName = planSequence.getName();
//           SequenceViewMenuItem modelRulesItem =
//             new SequenceViewMenuItem( seqName, planSequence.getUrl(), seqName, viewListener);
//           Thread thread = new CreateSequenceViewThread(PlanWorks.MODEL_RULES_VIEW,
//                                                        modelRulesItem);
//           thread.setPriority( Thread.MIN_PRIORITY);
//           thread.start();
//         }
//       });
//   } // end createModelRulesViewItem

  private void createRefreshItem( JMenuItem refreshItem, 
                                  final SequenceStepsView sequenceStepsView) {
    refreshItem.addActionListener( new ActionListener() {
        public final void actionPerformed( final ActionEvent evt) {
          System.err.println( "Refreshing planning sequence...");
          planSequence.refresh();
          System.err.println( "Redrawing sequence steps view...");
          heightScaleFactor = computeHeightScaleFactor();
          redraw();
          System.err.println( "   ... Done.");
        }
      });
  }

  private List getPartialPlanViewList() {
    List partialPlanViewList = new ArrayList();
    int currentStep = selectedStepElement.getStepNumber();
    // System.err.println( "createStepBackwardItem currentStep " + currentStep);
    PwPartialPlan partialPlan = null;
    try {
      partialPlan = planSequence.getPartialPlan( currentStep);
    } catch (IndexOutOfBoundsException excp) {
    } catch (ResourceNotFoundException excpR) {
    }
    PartialPlanViewSet partialPlanViewSet =
      (PartialPlanViewSet) PlanWorks.getPlanWorks().getViewManager().getViewSet( partialPlan);
    if (partialPlanViewSet != null) {
      int numToReturn = 0; // return all
      List partialPlanViews = partialPlanViewSet.getPartialPlanViews( numToReturn);
      Iterator viewsItr = partialPlanViews.iterator();
      while (viewsItr.hasNext()) {
        PartialPlanView partialPlanView = (PartialPlanView) viewsItr.next();
        StepButton backwardButton = partialPlanView.getBackwardButton();
        // DBTransactionView does not have step buttons
        if (backwardButton != null) {
          // System.err.println( "partialPlanView " + partialPlanView);
          partialPlanViewList.add( partialPlanView);
        }
      }
    }
    return partialPlanViewList;
  } // end getPartialPlanViewList

  private void createStepBackwardItem( JMenuItem stepBackwardItem, 
                                       final List partialPlanViewList) {
    stepBackwardItem.addActionListener( new ActionListener() {
        public final void actionPerformed( final ActionEvent evt) {
          Iterator viewsItr = partialPlanViewList.iterator();
          while (viewsItr.hasNext()) {
            PartialPlanView partialPlanView = (PartialPlanView) viewsItr.next();
            StepButton backwardButton = partialPlanView.getBackwardButton();
            ListIterator actionList =
              backwardButton.getActionListeners().listIterator();
            ActionEvent e =
              new ActionEvent( backwardButton, ActionEvent.ACTION_PERFORMED, "LeftClick", 
                               (int) AWTEvent.MOUSE_EVENT_MASK);
            while (actionList.hasNext()) {
              ((ActionListener) actionList.next()).actionPerformed( e);
            }
          }
        }
      });
  } // end createStepBackwardItem

  private void createStepForwardItem( JMenuItem stepForwardItem, 
                                      final List partialPlanViewList) {
    stepForwardItem.addActionListener( new ActionListener() {
        public final void actionPerformed( final ActionEvent evt) {
          Iterator viewsItr = partialPlanViewList.iterator();
          while (viewsItr.hasNext()) {
            PartialPlanView partialPlanView = (PartialPlanView) viewsItr.next();
            StepButton forwardButton = partialPlanView.getForwardButton();
            ListIterator actionList =
              forwardButton.getActionListeners().listIterator();
            ActionEvent e =
              new ActionEvent( forwardButton, ActionEvent.ACTION_PERFORMED, "LeftClick", 
                               (int) AWTEvent.MOUSE_EVENT_MASK);
            while (actionList.hasNext()) {
              ((ActionListener) actionList.next()).actionPerformed( e);
            }
          }
        }
      });
  } // end createStepForwardItem


} // end class SequenceStepsView
