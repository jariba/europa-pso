// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: VizView.java,v 1.17 2004-03-24 02:31:04 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 18May03
//

package gov.nasa.arc.planworks.viz;

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
import gov.nasa.arc.planworks.viz.partialPlan.temporalExtent.TemporalExtentView;
import gov.nasa.arc.planworks.viz.sequence.sequenceSteps.SequenceStepsView;
import gov.nasa.arc.planworks.viz.viewMgr.ViewSet;


/**
 * <code>VizView</code> - 
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *           NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class VizView extends JPanel {

  public static final String EVT_BEGUN_DRAWING = "drawingBegun";
  public static final String EVT_ENDED_DRAWING = "drawingEnded";
  public static final String EVT_JGO_VIEW_CHANGED = "jGoViewChanged";
  public static final String EVT_JGO_DOCUMENT_CHANGED = "jGoDocumentChanged";

  /**
   * constant <code>ZOOM_FACTORS</code>
   *
   */
  public static final int[] ZOOM_FACTORS = new int[] { 1, 2, 4, 8 };

  private static final String[] ZOOM_ITEM_NAMES = new String[] { "Full Size",
                                                                  "Zoom Out x 2",
                                                                  "Zoom Out x 4",
                                                                  "Zoom Out x 8" };

  protected ViewSet viewSet;
  protected Font font;
  protected FontMetrics fontMetrics;
  protected VizViewOverview overview;
  protected int zoomFactor;
  private List listenerList;

  /**
   * <code>VizView</code> - constructor 
   *
   * @param partialPlan - <code>PwPartialPlan</code> - 
   * @param viewSet - <code>ViewSet</code> - 
   */
  public VizView( ViewSet viewSet) {
    super();
    this.viewSet = viewSet;

    font = new Font( ViewConstants.TIMELINE_VIEW_FONT_NAME,
                     ViewConstants.TIMELINE_VIEW_FONT_STYLE,
                     ViewConstants.TIMELINE_VIEW_FONT_SIZE);
    fontMetrics = null;  // see computeFontMetrics
    overview = null;
    zoomFactor = 1;

    JGoText.setDefaultFontFaceName( "Monospaced");
    JGoText.setDefaultFontSize( ViewConstants.TIMELINE_VIEW_FONT_SIZE);
    listenerList = new LinkedList();
    // Utilities.printFontNames();
  }

  public void addViewListener(ViewListener l) {
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
   * <code>getFontMetrics</code> - called in "leaf" view class's init method
   *                       view must be displayable, before graphics is non-null
   *
   * @param view - <code>VizView</code> - 
   * @return - <code>FontMetrics</code> - 
   */
  protected void computeFontMetrics( VizView view) {
    Graphics graphics = ((JPanel) view).getGraphics();
    fontMetrics = graphics.getFontMetrics( font);
    graphics.dispose();
  } // end getFontMetrics


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
   * @param partialPlanIfLoaded - <code>PwPartialPlan</code> - StepElement may pass null
   * @param partialPlanName - <code>String</code> - 
   * @param planSequence - <code>PwPlanningSequence</code> - 
   * @param mouseRightPopup - <code>JPopupMenu</code> - 
   */
  public void createAllViewItems( PwPartialPlan partialPlanIfLoaded,
                                  String partialPlanName,
                                  PwPlanningSequence planSequence,
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
    createOpenAllItem( openAllItem, partialPlanIfLoaded, partialPlanName, planSequence);
    mouseRightPopup.add( openAllItem);

    if (partialPlanIfLoaded != null) {
      JMenuItem showAllItem = new JMenuItem( "Show All Views");
      createShowAllItem( showAllItem, partialPlanViewSet);
      mouseRightPopup.add( showAllItem);
    }    
  } // end createAllViewItems

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
        try {
          viewFrame.setIcon( false);
          viewFrame.setSelected( false);
          viewFrame.setSelected( true);
        } 
        catch ( PropertyVetoException pve){
        }
      }
      return o;
    }
  }

  private void createOpenAllItem( JMenuItem openAllItem,
                                  final PwPartialPlan partialPlanIfLoaded,
                                  final String partialPlanName,
                                  final PwPlanningSequence planSequence) {
    openAllItem.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent evt) {
          String seqUrl = planSequence.getUrl();
          String seqName = planSequence.getName();
          boolean isInvokeAndWait = true;
          Iterator viewListItr = PlanWorks.PARTIAL_PLAN_VIEW_LIST.iterator();
          while (viewListItr.hasNext()) {
            final String viewName = (String) viewListItr.next();
            final PartialPlanViewMenuItem viewItem =
              new PartialPlanViewMenuItem( viewName, seqUrl, seqName, partialPlanName);
            Thread thread = new CreatePartialPlanViewThread( viewName, viewItem, isInvokeAndWait);
            thread.setPriority(Thread.MIN_PRIORITY);
            thread.start();
          }
          // de-iconify any Navigator Views
          if (partialPlanIfLoaded != null) {
            PartialPlanViewSet partialPlanViewSet =
              (PartialPlanViewSet) viewSet.getViewManager().getViewSet( partialPlanIfLoaded);
            String navigatorWindowName = PlanWorks.NAVIGATOR_VIEW.replaceAll( " ", "");
            List windowKeyList = new ArrayList( partialPlanViewSet.getViews().keySet());
            CollectionUtils.lMap(new NavViewDeIcon(partialPlanViewSet, navigatorWindowName),
                                 windowKeyList);
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
    } else if (jGoView instanceof NavigatorView.NavigatorJGoView) {
      ((NavigatorView.NavigatorJGoView) jGoView).resetOpenNodes();
      ((NavigatorView) partialPlanView).setLayoutNeeded();
      isRedraw = true;
      hasStepButtons = false;
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

    jGoView.setScale( 1.0d / zoomFactor);
  } // end zoomView

  protected void handleEvent(String eventName, Object [] params) {
    Class [] paramTypes = new Class [params.length];
    for(int i = 0; i < params.length; i++) {
      paramTypes[i] = params[i].getClass();
    }
    for(Iterator it = listenerList.iterator(); it.hasNext();) {
      ViewListener l = (ViewListener) it.next();
      try {
        //System.err.println("Accessing method " + l.getClass().getName() + "." + eventName + "("
        //                   + paramTypes + ")");
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
      view.handleEvent(EVT_JGO_VIEW_CHANGED, params);
    }
    public void viewChanged(JGoViewEvent e) {
      Object [] params = {e};
      view.handleEvent(EVT_JGO_DOCUMENT_CHANGED, params);
    }
  }

  protected JGoDocumentListener createDocListener() {
    return new JGoListener(this);
  }

  protected JGoViewListener createViewListener() {
    return new JGoListener(this);
  }
} // end class VizView

