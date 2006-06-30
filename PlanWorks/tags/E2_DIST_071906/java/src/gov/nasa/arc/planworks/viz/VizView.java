// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: VizView.java,v 1.44 2005-06-01 17:10:18 pdaley Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 18May03
//

package gov.nasa.arc.planworks.viz;

import java.awt.AWTEvent;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoDocumentEvent;
import com.nwoods.jgo.JGoDocumentListener;
import com.nwoods.jgo.JGoPen;
import com.nwoods.jgo.JGoSelection;
import com.nwoods.jgo.JGoText;
import com.nwoods.jgo.JGoView;
import com.nwoods.jgo.JGoViewEvent;
import com.nwoods.jgo.JGoViewListener;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.PwPlanningSequence;
import gov.nasa.arc.planworks.mdi.MDIInternalFrame;
import gov.nasa.arc.planworks.util.CollectionUtils;
import gov.nasa.arc.planworks.util.UnaryFunctor;
import gov.nasa.arc.planworks.viz.partialPlan.CreatePartialPlanViewThread;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanViewMenuItem;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanView;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanViewSet;
import gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork.ConstraintNetworkView;
import gov.nasa.arc.planworks.viz.partialPlan.navigator.NavigatorView;
import gov.nasa.arc.planworks.viz.partialPlan.timeline.TimelineView;
import gov.nasa.arc.planworks.viz.partialPlan.temporalExtent.TemporalExtentView;
import gov.nasa.arc.planworks.viz.partialPlan.tokenNetwork.TokenNetworkView;
import gov.nasa.arc.planworks.viz.sequence.sequenceSteps.SequenceStepsView;
import gov.nasa.arc.planworks.viz.util.FindEntityPath;
import gov.nasa.arc.planworks.viz.util.ProgressMonitorThread;
import gov.nasa.arc.planworks.viz.util.StepButton;
import gov.nasa.arc.planworks.viz.viewMgr.ViewSet;


/**
 * <code>VizView</code> - 
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *           NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class VizView extends JPanel {

  /**
   * constant <code>ZOOM_FACTORS</code>
   *
   */
  public static final int[] ZOOM_FACTORS = new int[] { 1, 2, 4, 8 };

  private static final String[] ZOOM_ITEM_NAMES = new String[] { "Full Size",
                                                                  "Zoom Out x 2",
                                                                  "Zoom Out x 4",
                                                                  "Zoom Out x 8" };
  private  List listenerList;

  protected ViewSet viewSet;
  protected Font font;
  protected FontMetrics fontMetrics;
  protected VizViewOverview overview;
  protected int zoomFactor;
  protected MDIInternalFrame viewFrame;

  /**
   * <code>VizView</code> - constructor 
   *
   * @param viewSet - <code>ViewSet</code> - 
   */
  public VizView( ViewSet viewSet) {
    super();
    this.viewSet = viewSet;

    font = new Font( ViewConstants.VIEW_FONT_NAME,
                     ViewConstants.VIEW_FONT_PLAIN_STYLE,
                     ViewConstants.VIEW_FONT_SIZE);
    fontMetrics = null;  // see computeFontMetrics
    overview = null;
    zoomFactor = 1;
    viewFrame = null;

    JGoText.setDefaultFontFaceName( "Monospaced");
    JGoText.setDefaultFontSize( ViewConstants.VIEW_FONT_SIZE);
    listenerList = new LinkedList();
    // Utilities.printFontNames();
  }

  public void addViewListener(ViewListener l) {
    // System.err.println( "VizView.addViewListener " + l);
    listenerList.add(l);
  }

  /**
   * <code>getViewSet</code>
   *
   * @return - <code>ViewSet</code> - 
   */
  public ViewSet getViewSet() {
    return viewSet;
  }

  /**
   * <code>getOverview</code>
   *
   * @return - <code>VizViewOverview</code> - 
   */
  public VizViewOverview getOverview() {
    return overview;
  }

  /**
   * <code>setOverview</code>
   *
   * @param overview - <code>VizViewOverview</code> - 
   */
  public void setOverview( VizViewOverview overview) {
    this.overview = overview;
  }

  /**
   * <code>getZoomFactor</code>
   *
   * @return - <code>int</code> - 
   */
  public final int getZoomFactor() {
    return zoomFactor;
  }

  /**
   * <code>getViewFrame</code>
   *
   * @return - <code>MDIInternalFrame</code> - 
   */
  public final MDIInternalFrame getViewFrame() {
    return viewFrame;
  }
      
  /**
   * <code>redraw</code> - each subclass of VizView will implement 
   *
   */  
  public void redraw() {
  }

  /**
   * <code>getJGoView</code> - each subclass of VizView will implement, as needed
   *
   */  
  public JGoView getJGoView() {
    return null;
  }

  /**
   * <code>closeView</code>
   *
   * @param view - <code>VizView</code> - 
   */
  public void closeView( VizView view ) {
    try {
//       ViewListener viewListener = null;
//       viewSet.openView( view.getClass().getName(), viewListener).setClosed( true);
      viewFrame.setClosed( true);
    } catch (PropertyVetoException excp) {
    }
  } // end closeView

  /**
   * <code>isContentSpecRendered</code> - each subclass of VizView will implement
   *
   * @param viewName - <code>String</code> - 
   * @param showDialog - <code>boolean</code> - 
   * @return - <code>boolean</code> - 
   */
  protected boolean isContentSpecRendered( String viewName, boolean showDialog) {
    return true;
  } // end isContentSpecRendered

  /**
   * <code>expandViewFrame</code> - expand up to size of PlanWorks frame
   *
   * @param viewClassName - <code>String</code> - 
   * @param maxViewWidth - <code>int</code> - 
   * @param maxViewHeight - <code>int</code> - 
   */
  protected void expandViewFrame( MDIInternalFrame viewFrame, int maxViewWidth,
                                  int maxViewHeight) {
    maxViewWidth = Math.min( maxViewWidth, 
                             (int) PlanWorks.getPlanWorks().getSize().getWidth() -
                             (int) viewFrame.getLocation().getX() -
                             ViewConstants.MDI_FRAME_DECORATION_WIDTH -
                             ViewConstants.FRAME_DECORATION_WIDTH); 
    maxViewHeight = Math.min( maxViewHeight, 
                              (int) PlanWorks.getPlanWorks().getSize().getHeight() -
                              (int) viewFrame.getLocation().getY() -
                              ViewConstants.MDI_FRAME_DECORATION_HEIGHT -
                              ViewConstants.FRAME_DECORATION_HEIGHT); 
    viewFrame.setSize( maxViewWidth + ViewConstants.MDI_FRAME_DECORATION_WIDTH,
                       maxViewHeight + ViewConstants.MDI_FRAME_DECORATION_HEIGHT);
  } // end expandViewFrame


  /**
   * <code>computeFontMetrics</code> - called in "leaf" view class's init method
   *                       view must be displayable, before graphics is non-null
   *
   * @param view - <code>VizView</code> - 
   */
  protected void computeFontMetrics( VizView view) {
    Graphics graphics = ((JPanel) view).getGraphics();
    fontMetrics = graphics.getFontMetrics( font);
    graphics.dispose();
  } // end computeFontMetrics


  /**
   * <code>getFontMetrics</code> - public so that node classes can have access
   *
   * @return - <code>FontMetrics</code> - 
   */
  public FontMetrics getFontMetrics()  {
    return fontMetrics;
  }

  /**
   * <code>createAllViewItems</code>
   *
   * @param partialPlanIfLoaded - <code>PwPartialPlan</code> - 
   * @param partialPlanName - <code>String</code> - 
   * @param planSequence - <code>PwPlanningSequence</code> - 
   * @param mouseRightPopup - <code>JPopupMenu</code> - 
   */
  public void createAllViewItems( PwPartialPlan partialPlanIfLoaded,
                                  String partialPlanName,
                                  PwPlanningSequence planSequence,
                                  JPopupMenu mouseRightPopup) {
    List viewListenerList = new ArrayList();
    for (int i = 0, n = PlanWorks.PARTIAL_PLAN_VIEW_LIST.size(); i < n; i++) {
      viewListenerList.add( null);
    }
     PartialPlanView partialPlanView = null;
    createAllViewItems( partialPlanIfLoaded, partialPlanName, planSequence,
                        viewListenerList, mouseRightPopup);
  } // end createAllViewItems 

  /**
   * <code>createAllViewItems</code>
   *
   * @param partialPlanIfLoaded - <code>PwPartialPlan</code> - StepElement may pass null
   * @param partialPlanName - <code>String</code> - 
   * @param planSequence - <code>PwPlanningSequence</code> - 
   * @param viewListenerList - <code>List</code> - 
   * @param mouseRightPopup - <code>JPopupMenu</code> - 
   */
  public void createAllViewItems( PwPartialPlan partialPlanIfLoaded,
                                  String partialPlanName,
                                  PwPlanningSequence planSequence,
                                  List viewListenerList,
                                  JPopupMenu mouseRightPopup) { 
    mouseRightPopup.addSeparator();

    PartialPlanViewSet partialPlanViewSet = null;
    if (partialPlanIfLoaded != null) {
      partialPlanViewSet =
        (PartialPlanViewSet) viewSet.getViewManager().getViewSet( partialPlanIfLoaded);
      if (partialPlanViewSet != null) {
        JMenuItem closeAllItem = new JMenuItem( "Close All Views");
        createCloseAllItem( closeAllItem, partialPlanViewSet);
        mouseRightPopup.add( closeAllItem);

        JMenuItem hideAllItem = new JMenuItem( "Hide All Views");
        createHideAllItem( hideAllItem, partialPlanViewSet);
        mouseRightPopup.add( hideAllItem);
      }
    }

    JMenuItem openAllItem = new JMenuItem( "Open All Views");
    createOpenAllItem( openAllItem, partialPlanIfLoaded, partialPlanName, planSequence,
                       viewListenerList);
    mouseRightPopup.add( openAllItem);

    if ((partialPlanIfLoaded != null) && (partialPlanViewSet != null)) {
      JMenuItem showAllItem = new JMenuItem( "Show All Views");
      createShowAllItem( showAllItem, partialPlanViewSet);
      mouseRightPopup.add( showAllItem);
    }    
  } // end createAllViewItems with viewListenerList

  private void createCloseAllItem( JMenuItem closeAllItem,
                                   final PartialPlanViewSet partialPlanViewSet) {
    closeAllItem.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent evt) {
          partialPlanViewSet.close();
        }
      });
  } // end createCloseAllItem
 
  private void createHideAllItem( JMenuItem hideAllItem,
                                  final PartialPlanViewSet partialPlanViewSet) {
    hideAllItem.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent evt) {
          partialPlanViewSet.iconify();
        }
      });
  } // end createHideAllItem
 
  private void createShowAllItem( JMenuItem showAllItem,
                                  final PartialPlanViewSet partialPlanViewSet) {
    showAllItem.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent evt) {
          partialPlanViewSet.show();
        }
      });
  } // end createShowAllItem
 
  class NavViewDeIcon implements UnaryFunctor {
    private PartialPlanViewSet viewSet;
    private String navName;
    public NavViewDeIcon(PartialPlanViewSet viewSet, String navName) {
      this.viewSet = viewSet;
      this.navName = navName;
    }
    public final Object func(Object o) {
      if(o instanceof String && ((String) o).indexOf(navName) >= 0) {
        MDIInternalFrame viewFrame = (MDIInternalFrame) viewSet.getView(o);
        ViewGenerics.raiseFrame( viewFrame);
      }
      return o;
    }
  }

  private void createOpenAllItem( JMenuItem openAllItem,
                                  final PwPartialPlan partialPlanIfLoaded,
                                  final String partialPlanName,
                                  final PwPlanningSequence planSequence,
                                  final List viewListenerList) {
    openAllItem.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent evt) {
          if (viewListenerList.size() != PlanWorks.PARTIAL_PLAN_VIEW_LIST.size()) {
            System.err.println( "VizView.createOpenAllItem: num view listeners not = " +
                                PlanWorks.PARTIAL_PLAN_VIEW_LIST.size());
            System.exit( -1);
          }
          String seqUrl = planSequence.getUrl();
          String seqName = planSequence.getName();
          Iterator viewListItr = PlanWorks.PARTIAL_PLAN_VIEW_LIST.iterator();
          Iterator viewListenerItr = viewListenerList.iterator();
          while (viewListItr.hasNext()) {
            final String viewName = (String) viewListItr.next();
            ViewListener viewListener = (ViewListener) viewListenerItr.next();
            final PartialPlanViewMenuItem viewItem =
              new PartialPlanViewMenuItem( viewName, seqUrl, seqName, partialPlanName,
                                           viewListener);
            Thread thread = new CreatePartialPlanViewThread( viewName, viewItem);
            thread.setPriority(Thread.MIN_PRIORITY);
            thread.start();
          }
          // de-iconify any Navigator Views
          if (partialPlanIfLoaded != null) {
            PartialPlanViewSet partialPlanViewSet =
              (PartialPlanViewSet) viewSet.getViewManager().getViewSet( partialPlanIfLoaded);
            String navigatorWindowName = ViewConstants.NAVIGATOR_VIEW.replaceAll( " ", "");
            if (partialPlanViewSet != null && partialPlanViewSet.getViews() != null) {
              List windowKeyList = new ArrayList( partialPlanViewSet.getViews().keySet());
              CollectionUtils.lMap(new NavViewDeIcon(partialPlanViewSet, navigatorWindowName),
                                   windowKeyList);
            }
          }
        }
      });
  } // end createOpenAllItem

  /**
   * <code>getZoomIndex</code>
   *
   * @param zoomFactor - <code>int</code> - 
   * @return - <code>int</code> - 
   */
  public int getZoomIndex( final int zoomFactor) {
    int zoomIndex = 0;
    for (int i = 0, n = ZOOM_FACTORS.length; i < n; i++) {
      if (ZOOM_FACTORS[i] == zoomFactor) {
        zoomIndex = i;
      }
    }
    return zoomIndex;
  } // end getZoomIndex

  /**
   * <code>getOpenJGoPenWidth</code>
   *
   * @param zoomFactor - <code>int</code> - 
   * @return - <code>int</code> - 
   */
  public int getOpenJGoPenWidth( final int zoomFactor) {
    return 2 + 2 * getZoomIndex( zoomFactor);
  }

  /**
   * <code>createZoomItem</code>
   *
   * @param jGoView - <code>JGoView</code> - 
   * @param currentZoomFactor - <code>int</code> - 
   * @param mouseRightPopup - <code>JPopupMenu</code> - 
   * @param partialPlanView - <code>VizView</code> - 
   */
  protected void createZoomItem( final JGoView jGoView, final int currentZoomFactor,
                                 JPopupMenu mouseRightPopup, final VizView partialPlanView) {
    JMenuItem zoomItem = new JMenuItem( "Zoom View");
    final int optionIndex = getZoomIndex( currentZoomFactor);

    zoomItem.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent evt) {
          Object[] options = new Object[ZOOM_FACTORS.length];
          for (int i = 0, n = ZOOM_FACTORS.length; i < n; i++) {
            options[i] = ZOOM_ITEM_NAMES[i];
          }
          Object response = JOptionPane.showInputDialog
            ( PlanWorks.getPlanWorks(), "", "Zoom View",
              JOptionPane.QUESTION_MESSAGE, null, options, options[optionIndex]);
          if (response instanceof String) {
            for (int i = 0, n = options.length; i < n; i++) {
              if (((String) options[i]).equals( response)) {
                // String zoomName = ZOOM_ITEM_NAMES[i];
                // System.err.println( zoomName);
                zoomFactor = ZOOM_FACTORS[i];
                boolean isSetState = false;
                zoomView( jGoView, isSetState, partialPlanView);
                break;
              }
            }
          }
        }
      });

    mouseRightPopup.add( zoomItem);
  } // createZoomItem

  /**
   * <code>zoomView</code>
   *
   * @param jGoView - <code>JGoView</code> - 
   * @param doRedraw - <code>boolean</code> - 
   * @param partialPlanView - <code>VizView</code> - 
   */
  protected void zoomView( final JGoView jGoView, final boolean isSetState,
                           final VizView partialPlanView) {
    int penWidth = getOpenJGoPenWidth( zoomFactor);
    JGoSelection selection = jGoView.getSelection();
    // for primary selection pen
    selection.setBoundingHandlePen
      ( new JGoPen( JGoPen.SOLID, penWidth, jGoView.getPrimarySelectionColor()));
    // for secondary selection pens
    selection.setBoundingHandlePenWidth( penWidth);
    boolean isRedraw = false, hasStepButtons = true;
    if (jGoView instanceof ConstraintNetworkView.ConstraintJGoView) {
      ((ConstraintNetworkView.ConstraintJGoView) jGoView).resetOpenNodes();
      ((ConstraintNetworkView) partialPlanView).setLayoutNeeded();
      isRedraw = true;
    } else if (jGoView instanceof TemporalExtentView.ExtentView) {
       isRedraw = true;
    } else if (jGoView instanceof TimelineView.TimelineJGoView) {
       isRedraw = true;
    } else if (jGoView instanceof NavigatorView.NavigatorJGoView) {
      ((NavigatorView.NavigatorJGoView) jGoView).resetOpenNodes();
      ((NavigatorView) partialPlanView).setLayoutNeeded();
      isRedraw = true;
      hasStepButtons = false;
    } else if (jGoView instanceof TokenNetworkView.TokenNetworkJGoView) {
      ((TokenNetworkView.TokenNetworkJGoView) jGoView).resetOpenNodes();
      ((TokenNetworkView) partialPlanView).setLayoutNeeded();
      isRedraw = true;
    } else if (jGoView instanceof SequenceStepsView.SequenceStepsJGoView) {
       hasStepButtons = false;
    }
    // keep backword/forward buttons the same size
    if (isRedraw && (! isSetState)) {
      partialPlanView.redraw();
    }
    if (hasStepButtons) {
      ((PartialPlanView) partialPlanView).removeStepButtons( jGoView);
      ((PartialPlanView) partialPlanView).addStepButtons( jGoView);
    }
    jGoView.getHorizontalScrollBar().setValue( 0);
    jGoView.getVerticalScrollBar().setValue( 0);

    // In new Timeline View, dont zoom vertical scale
    if (jGoView instanceof TimelineView.TimelineJGoView) {
      jGoView.setHorizontalScale( 1.0d / zoomFactor);
      //jGoView.setVerticalScale( 1.0d / zoomFactor);
      //jGoView.getHorizontalScale();
      //jGoView.getVerticalScale();
      //jGoView.setScale( 1.0d / zoomFactor);
    } else {
      jGoView.setScale( 1.0d / zoomFactor);
    }
  } // end zoomView

  protected void handleEvent(String eventName, Object [] params) {
    Class [] paramTypes = new Class [params.length];
    for(int i = 0; i < params.length; i++) {
      paramTypes[i] = params[i].getClass();
    }
    for(Iterator it = listenerList.iterator(); it.hasNext();) {
      ViewListener l = (ViewListener) it.next();
      try {
        // System.err.println("Accessing method " + l.getClass().getName() + "." + eventName + "("
        //                    + paramTypes + ")");
        l.getClass().getMethod(eventName, paramTypes).invoke(l, params);
        
      }
      catch(Exception e) {
        e.printStackTrace();
      }
    }
  }

  protected void handleEvent(String eventName) {
    Object [] params = {};
    handleEvent(eventName, params);
  }

  class JGoListener implements JGoDocumentListener, JGoViewListener {
    VizView view;
    public JGoListener(VizView view) {
      this.view = view;
    }
    public void documentChanged(JGoDocumentEvent e) {
      Object [] params = {e};
      view.handleEvent(ViewListener.EVT_JGO_VIEW_CHANGED, params);
    }
    public void viewChanged(JGoViewEvent e) {
      Object [] params = {e};
      view.handleEvent(ViewListener.EVT_JGO_DOCUMENT_CHANGED, params);
    }
  }

  protected JGoDocumentListener createDocListener() {
    return new JGoListener(this);
  }

  protected JGoViewListener createViewListener() {
    return new JGoListener(this);
  }

  /**
   * <code>createProgressMonitorThread</code>
   *
   * @param title - <code>String</code> - 
   * @param minValue - <code>int</code> - 
   * @param maxValue - <code>int</code> - 
   * @param monitoredThread - <code>Thread</code> - 
   * @param view - <code>JPanel</code> - 
   * @return - <code>ProgressMonitorThread</code> - 
   */
  protected ProgressMonitorThread  createProgressMonitorThread( String title, int minValue,
                                                                int maxValue,
                                                                Thread monitoredThread,
                                                                JPanel view) {
    ProgressMonitorThread thread =
      new ProgressMonitorThread( title, minValue, maxValue, monitoredThread, view);
    thread.setPriority(Thread.MAX_PRIORITY);
    thread.start();
    return thread;
  }

  /**
   * <code>createProgressMonitorThread</code>
   *
   * @param title - <code>String</code> - 
   * @param minValue - <code>int</code> - 
   * @param maxValue - <code>int</code> - 
   * @param monitoredThread - <code>Thread</code> - 
   * @param view - <code>JPanel</code> - 
   * @param findEntityPath - <code>FindEntityPath</code> - 
   * @return - <code>ProgressMonitorThread</code> - 
   */
  public ProgressMonitorThread  createProgressMonitorThread( String title, int minValue,
                                                             int maxValue, Thread monitoredThread,
                                                             JPanel view,
                                                             FindEntityPath findEntityPath) {
    ProgressMonitorThread thread =
      new ProgressMonitorThread( title, minValue, maxValue, monitoredThread,
                                 view, findEntityPath);
    thread.setPriority(Thread.MAX_PRIORITY);
    thread.start();
    return thread;
  }

  /**
   * <code>progressMonitorWait</code>
   *
   * @param vizView - <code>VizView</code> - 
   * @return - <code>boolean</code> - 
   */
  public boolean progressMonitorWait( ProgressMonitorThread pmThread, VizView vizView) {
    int maxCycles = ViewConstants.WAIT_NUM_CYCLES;
    int numCycles = maxCycles;
    while ((pmThread.getProgressMonitor() == null) && numCycles != 0) {
      try {
        Thread.currentThread().sleep( ViewConstants.WAIT_INTERVAL);
      }
      catch (InterruptedException ie) {}
      numCycles--;
      // System.err.println( "progressMonitorWait numCycles " + numCycles);
    }
    if (numCycles == 0) {
      System.err.println( "progressMonitorWait failed after " +
                          (ViewConstants.WAIT_INTERVAL * maxCycles) + " msec for " +
                          vizView.getClass().getName());
      try {
        throw new Exception();
      } catch (Exception e) { e.printStackTrace(); }
    }
    // System.err.println( "progressMonitorWait took " + (maxCycles - numCycles) + " numCycles");
    return numCycles != 0;
  } // end progressMonitorWait

  /**
   * <code>createStepAllViewItems</code>
   *
   * @param partialPlan - <code>PwPartialPlan</code> - 
   * @param mouseRightPopup - <code>JPopupMenu</code> - 
   */
  public void createStepAllViewItems( PwPartialPlan partialPlan, JPopupMenu mouseRightPopup) {
    List partialPlanViewList = getPartialPlanViewList( partialPlan);
    String stepBackwardTitle = "Step Backward All Views";
    JMenuItem stepBackwardItem = new JMenuItem( stepBackwardTitle);
    createStepBackwardItem( stepBackwardItem, partialPlanViewList);
    mouseRightPopup.add( stepBackwardItem);

    String stepForwardTitle = "Step Forward All Views";
    JMenuItem stepForwardItem = new JMenuItem( stepForwardTitle);
    createStepForwardItem( stepForwardItem, partialPlanViewList);
    mouseRightPopup.add( stepForwardItem);

  } // createStepAllViewItems

  /**
   * <code>getPartialPlanViewList</code>
   *
   * @param partialPlan - <code>PwPartialPlan</code> - 
   * @return - <code>List</code> - 
   */
  protected List getPartialPlanViewList( PwPartialPlan partialPlan) {
    List partialPlanViewList = new ArrayList();
    PartialPlanViewSet partialPlanViewSet =
      (PartialPlanViewSet) PlanWorks.getPlanWorks().getViewManager().getViewSet( partialPlan);
    if (partialPlanViewSet != null) {
      int numToReturn = 0; // return all
      List partialPlanViews = partialPlanViewSet.getPartialPlanViews( numToReturn);
      Iterator viewsItr = partialPlanViews.iterator();
      while (viewsItr.hasNext()) {
        PartialPlanView partialPlanView = (PartialPlanView) viewsItr.next();
        StepButton backwardButton = partialPlanView.getBackwardButton();
        // System.err.println( "partialPlanView " + partialPlanView);
        partialPlanViewList.add( partialPlanView);
      }
    }
    return partialPlanViewList;
  } // end getPartialPlanViewList

  /**
   * <code>createStepBackwardItem</code>
   *
   * @param stepBackwardItem - <code>JMenuItem</code> - 
   * @param partialPlanViewList - <code>List</code> - 
   */
  protected void createStepBackwardItem( JMenuItem stepBackwardItem, 
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

  /**
   * <code>createStepForwardItem</code>
   *
   * @param stepForwardItem - <code>JMenuItem</code> - 
   * @param partialPlanViewList - <code>List</code> - 
   */
  protected void createStepForwardItem( JMenuItem stepForwardItem, 
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


} // end class VizView

