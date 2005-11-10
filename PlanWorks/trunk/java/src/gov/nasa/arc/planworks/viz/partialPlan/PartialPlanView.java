// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PartialPlanView.java,v 1.58 2005-11-10 01:22:12 miatauro Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 25sept03
//

package gov.nasa.arc.planworks.viz.partialPlan;

import java.awt.Color;
import java.awt.Point;
import java.awt.Container;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JOptionPane;
import javax.swing.JScrollBar;
import javax.swing.SwingConstants;
    
import com.nwoods.jgo.JGoView;
import com.nwoods.jgo.JGoViewEvent;
import com.nwoods.jgo.JGoViewListener;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.PwPlanningSequence;
import gov.nasa.arc.planworks.db.PwSlot;
import gov.nasa.arc.planworks.db.PwTimeline;
import gov.nasa.arc.planworks.db.PwToken;
import gov.nasa.arc.planworks.mdi.MDIDesktopFrame;
import gov.nasa.arc.planworks.mdi.MDIInternalFrame;
import gov.nasa.arc.planworks.util.CollectionUtils;
import gov.nasa.arc.planworks.util.ColorMap;
import gov.nasa.arc.planworks.util.UnaryFunctor;
import gov.nasa.arc.planworks.util.CreatePartialPlanException;
import gov.nasa.arc.planworks.util.ResourceNotFoundException;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.ViewGenerics;
import gov.nasa.arc.planworks.viz.ViewListener;
import gov.nasa.arc.planworks.viz.VizView;
import gov.nasa.arc.planworks.viz.VizViewOverview;
import gov.nasa.arc.planworks.viz.sequence.sequenceSteps.SequenceStepsView;
import gov.nasa.arc.planworks.viz.sequence.sequenceSteps.StepElement;
import gov.nasa.arc.planworks.viz.util.StepButton;
import gov.nasa.arc.planworks.viz.viewMgr.ViewSet;
import gov.nasa.arc.planworks.viz.viewMgr.ViewManager;
import gov.nasa.arc.planworks.viz.viewMgr.contentSpecWindow.partialPlan.ContentSpecWindow;
import gov.nasa.arc.planworks.viz.partialPlan.timeline.TimelineView;


/**
 * <code>PartialPlanView</code> - 
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *           NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class PartialPlanView extends VizView {

  private static final String [] BACKWARD_BUTTONS_LABELS =
  { "<", "< <\n< <", "< < < <\n< < < <\n< < < <\n< < < <",
    "< < < < < < < <\n< < < < < < < <\n< < < < < < < <\n< < < < < < < <\n< < < < < < < <\n< < < < < < < <\n< < < < < < < <\n< < < < < < < <" };
  private static final String [] FORWARD_BUTTONS_LABELS =
  { ">", "> >\n> >", "> > > >\n> > > >\n> > > >\n> > > >",
    "> > > > > > > >\n> > > > > > > >\n> > > > > > > >\n> > > > > > > >\n> > > > > > > >\n> > > > > > > >\n> > > > > > > >\n> > > > > > > >" };

  private static final String [] HZ_ZOOM_BACKWARD_BUTTONS_LABELS =
  { "<", "< <", "< < < <", "< < < < < < < <" };
  private static final String [] HZ_ZOOM_FORWARD_BUTTONS_LABELS =
  { ">", "> >", "> > > >", "> > > > > > > >" };


  protected PwPartialPlan partialPlan;
  protected List validTokenIds;
  protected List displayedTokenIds;
  protected String viewName; 
  protected List viewListenerList; // element ViewListener

  private StepButton backwardButton;
  private StepButton forwardButton;
  private ButtonAdjustmentListener horizontalAdjustmentListener;
  private ButtonAdjustmentListener verticalAdjustmentListener;
  private ButtonViewListener buttonViewListener;

  private String stringViewSetKey; // key for viewSet hash map - NavigatorView,
                                   // VizViewOverview, & VizViewRuleView

  private List highlightLinksList;  // for FindPath

  /**
   * <code>PartialPlanView</code> - constructor 
   *
   * @param partialPlan - <code>PwPartialPlan</code> - 
   * @param viewSet - <code>ViewSet</code> - 
   */
  public PartialPlanView( PwPartialPlan partialPlan, ViewSet viewSet) {
    super( viewSet);
    this.partialPlan = partialPlan;
    validTokenIds = null;
    displayedTokenIds = null;
    stringViewSetKey = null;
    backwardButton = null;
    forwardButton = null;
    horizontalAdjustmentListener = null;
    verticalAdjustmentListener = null;
    buttonViewListener = null;
    viewName = null;
    viewListenerList = new ArrayList();
    for (int i = 0, n = PlanWorks.PARTIAL_PLAN_VIEW_LIST.size(); i < n; i++) {
      viewListenerList.add( null);
    }
    highlightLinksList = null;
  }

  /**
   * <code>getPartialPlan</code>
   *
   * @return - <code>PwPartialPlan</code> - 
   */
  public PwPartialPlan getPartialPlan() {
    return partialPlan;
  }

  public PartialPlanViewState getState() {
    return new PartialPlanViewState(this);
  }

  public void setState(PartialPlanViewState state) {
    if(state == null) {
      return;
    }
    if ((state.getContentSpec() == null) || (viewSet.getContentSpecWindow() == null)) {
      return;
    }
    partialPlan.setContentSpec(state.getContentSpec());
    Container contentPane = this.viewSet.getContentSpecWindow().getContentPane();
    for(int i = 0; i < contentPane.getComponentCount(); i++) {
      if(contentPane.getComponent(i) instanceof ContentSpecWindow) {
        ContentSpecWindow csw = (ContentSpecWindow) contentPane.getComponent(i);
        csw.getSpec().resetSpecFromPlan();
        csw.buildFromSpec();
      }
    }
  }

  /**
   * <code>getBackwardButton</code>
   *
   * @return - <code>StepButton</code> - 
   */
  public final StepButton getBackwardButton() {
    return backwardButton;
  }

  /**
   * <code>getForwardButton</code>
   *
   * @return - <code>StepButton</code> - 
   */
  public final StepButton getForwardButton() {
    return forwardButton;
  }

  /**
   * <code>getViewName</code>
   *
   * @return - <code>String</code> - 
   */
  public final String getViewName() {
    return viewName;
  }

  /**
   * <code>getViewListenerList</code>
   *
   * @return - <code>List</code> - 
   */
  public final List getViewListenerList() {
    return viewListenerList;
  }

  /**
   * <code>setViewListenerList</code> - called from PlanWorksGUITest
   *
   * @param viewListenerList - <code>List</code> - 
   */
  public final void setViewListenerList( List viewListenerList) {
    this.viewListenerList = viewListenerList;
  } // end setViewListenerList

  /**
   * <code>getHighlightLinksList</code>
   *
   * @return - <code>List</code> - 
   */
  public List getHighlightLinksList() {
    return highlightLinksList;
  }

  /**
   * <code>setHighlightLinksList</code>
   *
   * @param linkList - <code>List</code> - 
   */
  public void setHighlightLinksList( List linkList) {
    highlightLinksList = linkList;
  }

  /**
   * <code>isContentSpecRendered</code>
   *
   * @param viewName - <code>String</code> - 
   * @param showDialog - <code>boolean</code> - 
   * @return - <code>boolean</code> - 
   */
  protected boolean isContentSpecRendered( String viewName, boolean showDialog) {
    Iterator validIterator = validTokenIds.iterator();
    List unDisplayedIds = new ArrayList();
    List extraDisplayedIds = new ArrayList();
    StringBuffer message = new StringBuffer();
    boolean error = false;

    while (validIterator.hasNext()) {
      Integer id = (Integer) validIterator.next();
      if (displayedTokenIds.indexOf( id) == -1) {
        unDisplayedIds.add( id);
      }
    }
    Iterator displayedIterator = displayedTokenIds.iterator();
    while (displayedIterator.hasNext()) {
      Integer id = (Integer) displayedIterator.next();
      if (validTokenIds.indexOf(id) == -1) {
        extraDisplayedIds.add(id);
      }
    }
    message.append("\n");
    if (extraDisplayedIds.size() != 0) {
      if (showDialog) {
        message.append(viewName).append(": invalidTokenIds ").append(extraDisplayedIds.toString());
        message.append(" displayed.");
      }
      error = true;
    }
    if (unDisplayedIds.size() != 0) {
      if (showDialog) {
        message.append(viewName).append(": validTokenIds ").append(unDisplayedIds.toString());
        message.append(" not displayed.");
      }
      error = true;
    }
    if (error) {
      if (showDialog) {
        JOptionPane.showMessageDialog(PlanWorks.getPlanWorks(), message.toString(),
                                      "View Rendering Exception", JOptionPane.ERROR_MESSAGE);
      }
      return false;
    }
    return true;
  } // end isContentSpecRendered

  /**
   * <code>isTimelineInContentSpec</code> - does timeline have a least one token
   *
   * @param timeline - <code>PwTimeline</code> - 
   * @return - <code>boolean</code> - 
   */
  protected boolean isTimelineInContentSpec( PwTimeline timeline) {
    List slotList = timeline.getSlotList();
    Iterator slotIterator = slotList.iterator();
    while (slotIterator.hasNext()) {
      PwSlot slot = (PwSlot) slotIterator.next();
      List tokenList = slot.getTokenList();
      if (tokenList.size() > 0) {
        Iterator tokenIterator = tokenList.iterator();
        while (tokenIterator.hasNext()) {
          PwToken token = (PwToken) tokenIterator.next();
          Integer id = token.getId();
          if (validTokenIds.indexOf( id) >= 0) {
            if (displayedTokenIds.indexOf( id) == -1) {
              displayedTokenIds.add( id);
            }
            return true;
          }
        }
        continue;
      } else {
        // empty slot
        continue;
      }
    }
    return false;
  } // end isTimelineInContentSpec

  /**
   * <code>isSlotInContentSpec</code> - is one of slot's tokens in content spec
   *
   * @param slot - <code>PwSlot</code> - 
   * @return - <code>boolean</code> - 
   */
  protected boolean isSlotInContentSpec( PwSlot slot) {
    boolean foundMatch = false;
    List tokenList = slot.getTokenList();
    if (tokenList.size() > 0) {
      Iterator tokenIterator = tokenList.iterator();
      while (tokenIterator.hasNext()) {
        PwToken token = (PwToken) tokenIterator.next();
        Integer id = token.getId();
        if (validTokenIds.indexOf( id) >= 0) {
          foundMatch = true;
          if (displayedTokenIds.indexOf( id) == -1) {
            displayedTokenIds.add( id);
          }
        }
      }
      if (foundMatch) {
        return true;
      } else {
        return false;
      }
    } else {
      return true;
    }
  } // end isSlotInContentSpec

  /**
   * <code>isTokenInContentSpec</code> - is token in content spec
   *
   * @param token - <code>PwToken</code> - 
   * @return - <code>boolean</code> - 
   */
  protected boolean isTokenInContentSpec( PwToken token) {
    Integer id = token.getId();
    if (validTokenIds.indexOf( id) >= 0) {
      if (displayedTokenIds.indexOf( id) == -1) {
        displayedTokenIds.add( id);
      }
      return true;
    } else {
      return false;
    }
  } // end isTokenInContentSpec

  public int addStepButtons(JGoView view) {
    if ((backwardButton != null) || (forwardButton != null)) {
      removeStepButtons( view);
    }
    int zoomIndex = getZoomIndex( zoomFactor);
    // timeline view zooms horizontal axis only, so step button padding is
    // for that axis only
    String backwardLabelText;
    String forwardLabelText;    
    if (PartialPlanView.this instanceof TimelineView) {
      backwardLabelText = HZ_ZOOM_BACKWARD_BUTTONS_LABELS[zoomIndex];
      forwardLabelText = HZ_ZOOM_FORWARD_BUTTONS_LABELS[zoomIndex];    
    } else {
      backwardLabelText = BACKWARD_BUTTONS_LABELS[zoomIndex];
      forwardLabelText = FORWARD_BUTTONS_LABELS[zoomIndex];    
    }
    Rectangle viewRect = view.getViewRect();
    Point backwardButtonPt = new Point((int)viewRect.getX(), 
                                       (int)(viewRect.getY() + viewRect.getHeight()));
    backwardButton = new StepButton(backwardButtonPt, ColorMap.getColor("khaki2"), "<",
                                   "Step backward.");
    backwardButton.getLabel().setMultiline( true);
    backwardButton.setText( backwardLabelText);
    backwardButton.setLocation((int)(viewRect.getX() + backwardButton.getSize().getWidth()),
                                (int)(viewRect.getY() + viewRect.getHeight() - 
                                     backwardButton.getSize().getHeight()));

    Point forwardButtonPt = new Point((int)(viewRect.getX() + 
                                       backwardButton.getSize().getWidth() + 10),
                                      (int)(viewRect.getY() + viewRect.getHeight()));
    forwardButton = new StepButton(forwardButtonPt, ColorMap.getColor("khaki2"), ">", 
                                  "Step forward.");
    forwardButton.getLabel().setMultiline( true);
    forwardButton.setText( forwardLabelText);
    forwardButton.setLocation((int)(backwardButton.getLocation().getX() + 
                                    backwardButton.getWidth()),
                              (int)(backwardButton.getLocation().getY()));
    view.getDocument().addObjectAtTail(backwardButton);
    view.getDocument().addObjectAtTail(forwardButton);
    //view.addObjectAtTail(backwardButton);
    //view.addObjectAtTail(forwardButton);

    if (view.getHorizontalScrollBar() != null) {
      view.getHorizontalScrollBar().
        addAdjustmentListener(new ButtonAdjustmentListener(view, backwardButton, forwardButton));
    }
    if (view.getVerticalScrollBar() != null) {
      view.getVerticalScrollBar().
        addAdjustmentListener(new ButtonAdjustmentListener(view, backwardButton, forwardButton));
    }
    view.addViewListener(new ButtonViewListener(view, backwardButton, forwardButton));
    backwardButton.addActionListener(new StepButtonListener(-1, view, this));
    forwardButton.addActionListener(new StepButtonListener(1, view, this));
    return (int) (viewRect.getY() + viewRect.getHeight());
  }

  public void removeStepButtons( JGoView view) {
    if (view.getHorizontalScrollBar() != null) {
      view.getHorizontalScrollBar().removeAdjustmentListener( horizontalAdjustmentListener);
    }
    if (view.getVerticalScrollBar() != null) {
      view.getVerticalScrollBar().removeAdjustmentListener( verticalAdjustmentListener);
    }
    view.removeViewListener( buttonViewListener);
    view.getDocument().removeObject( backwardButton);
    view.getDocument().removeObject( forwardButton);
    backwardButton = null;
    forwardButton = null;
  }

  class StepButtonListener implements ActionListener {
    private int dir;
    private JGoView view;
    private PartialPlanView pView;
    public StepButtonListener(int dir, JGoView view, PartialPlanView pView) {
      this.dir = dir;
      this.view = view;
      this.pView = pView;
    }
    public void actionPerformed(ActionEvent e) {
      //  view.getSelection().clearSelection();
      ViewSet viewSet = pView.getViewSet();
      MDIInternalFrame viewFrame = viewSet.getViewByClass(pView.getClass());
      ViewManager viewManager = viewSet.getViewManager();
      int currStepNumber = getPartialPlan().getStepNumber();
      PwPartialPlan nextStep = null;
      try {
	PwPlanningSequence planSequence = PlanWorks.getPlanWorks().getCurrentProject().
	  getPlanningSequence(getPartialPlan().getSequenceUrl());
	int newStepNumber = (dir == 1) ? (currStepNumber + 1) : (currStepNumber - 1);
	String newPlanName = "step" + newStepNumber;
	if(planSequence.doesPartialPlanExist(newPlanName)) {
	  // ok
	} else {
	  String viewTitle = pView.getName();
	  int indx = viewTitle.indexOf( "/");
	  JOptionPane.showMessageDialog( PlanWorks.getPlanWorks(),
					 viewTitle.substring( 0, indx) + "/" + newPlanName,
					 "No View Available", 
					 JOptionPane.ERROR_MESSAGE);
	  return;
	}

        if(dir == 1) {
          nextStep = planSequence.getNextPartialPlan(currStepNumber);
        }
        else if(dir == -1) {
          nextStep = planSequence.getPrevPartialPlan(currStepNumber);
        }
      }
      catch(IndexOutOfBoundsException ibe) {
        String message = "This is a bug";
        if(dir == 1) {
          message = "Attempted to step beyond last step.";
        }
        else if(dir == -1) {
          message = "Attempted to step beyond first step.";
        }
        JOptionPane.showMessageDialog(PlanWorks.getPlanWorks(), message, "Step Exception", 
                                      JOptionPane.ERROR_MESSAGE);
        // ibe.printStackTrace();
        return;
      }
      catch(ResourceNotFoundException rnfe) {
        JOptionPane.showMessageDialog(PlanWorks.getPlanWorks(), rnfe.getMessage(),
                                      "ResourceNotFoundException", JOptionPane.ERROR_MESSAGE);

        // rnfe.printStackTrace();
        return;
      }
      catch (CreatePartialPlanException cppExcep) {
        String msg = "User Canceled Create Partial Plan";
        System.err.println( msg);
        return;
      }
      if(nextStep.getName() == null) {
        String [] title = viewFrame.getTitle().split("\\s+", 3);
        String [] seqName = title[title.length - 1].split(System.getProperty("file.separator"));
        nextStep.setName(seqName[0] + System.getProperty("file.separator") + "step" +
                         (currStepNumber + dir));
      }
      PartialPlanViewState partialPlanViewState = PartialPlanView.this.getState();
      if (viewSet.getContentSpecWindow() != null) {
	// pass ContentSpec window location to next view frame
	Point specWindowLocation = new Point( viewSet.getContentSpecWindow().getLocation());
	partialPlanViewState.setContentSpecWindowLocation( specWindowLocation);
      }
      PlanWorks.getPlanWorks().setViewRenderingStartTime( System.currentTimeMillis(), 
                                                          pView.getViewName());
//       MDIInternalFrame nextViewFrame = viewManager.openView(nextStep, pView.getClass().getName(),
//                                                             partialPlanViewState);
      ViewListener viewListener =
        (ViewListener)  viewListenerList.get( PlanWorks.PARTIAL_PLAN_VIEW_LIST.indexOf
                                              ( pView.getViewName()));
      MDIInternalFrame nextViewFrame = viewManager.openView(nextStep, pView.getClass().getName(),
                                                            partialPlanViewState, viewListener);
      boolean isMoveValid = moveSequenceStepsViewHighlight( nextStep, pView.getViewName());
      if (! isMoveValid) {
        String message = "sequence step element index not found";
        JOptionPane.showMessageDialog( PlanWorks.getPlanWorks(), message,
                                       "Move Sequence Step Error",
                                       JOptionPane.ERROR_MESSAGE);
        System.err.println( "StepButtonListener: " + message);
      }
      try {
        nextViewFrame.setBounds(viewFrame.getBounds());
        nextViewFrame.setNormalBounds(viewFrame.getNormalBounds());
        nextViewFrame.setSelected(true);
        VizViewOverview overview = null;
        VizView vView = null;
        Container contentPane = viewFrame.getContentPane();
        for(int i = 0; i < contentPane.getComponentCount(); i++) {
          if(contentPane.getComponent(i) instanceof VizView) {
            vView = (VizView) contentPane.getComponent(i);
            overview = vView.getOverview();
            break;
          }
        }
        if(overview != null) {
          VizView nextView = null;
          JGoView nextJGoView = null;
          JGoView temp = null;
          contentPane = nextViewFrame.getContentPane();
          for(int i = 0; i < contentPane.getComponentCount(); i++) {
            if(contentPane.getComponent(i) instanceof VizView) {
              nextView = (VizView) contentPane.getComponent(i);
              //Container subPane = nextView.getContentPane();
              for(int j = 0; j < nextView.getComponentCount(); j++) {
                if(nextView.getComponent(i) instanceof JGoView) {
                  nextJGoView = (JGoView) nextView.getComponent(i);
                  break;
                }
              }
              break;
            }
          }
          MDIInternalFrame overviewFrame = null;
          Container parent = overview.getParent();
          while(!(parent instanceof MDIInternalFrame)) {
            parent = parent.getParent();
          }
          overviewFrame = (MDIInternalFrame) parent;
          String [] viewName = nextViewFrame.getTitle().split(" of ");
          VizViewOverview nextOverview = 
            ViewGenerics.openOverviewFrame(viewName[0], nextStep, nextView, 
                                           viewManager.getViewSet(nextStep), nextJGoView, 
                                           overviewFrame.getLocation());
          nextView.setOverview(nextOverview);
         
          MDIInternalFrame nextOverviewFrame = null;
          
          parent = nextOverview.getParent();
          while(!(parent instanceof MDIInternalFrame)) {
            parent = parent.getParent();
          }
          nextOverviewFrame = (MDIInternalFrame) parent;

          nextOverviewFrame.setBounds(overviewFrame.getBounds());
          nextOverviewFrame.setNormalBounds(overviewFrame.getBounds());
          
        }
        viewSet.removeViewFrame(viewFrame);
        viewFrame.setClosed(true);
      }
      catch(Exception ack){ack.printStackTrace();}
    }
  } // end  class StepButtonListener 


  private boolean moveSequenceStepsViewHighlight( PwPartialPlan nextStep, String viewName) {
    boolean isColorIndxFound = true;
    int currentStepNumber = getPartialPlan().getStepNumber();
    int nextStepNumber = nextStep.getStepNumber();
    System.err.println("Stepping " + viewName + " " + currentStepNumber + " -> " +
                       nextStepNumber + " ...");
    MDIInternalFrame seqStepsViewFrame =
      PlanWorks.getPlanWorks().getSequenceStepsViewFrame( getPartialPlan().getSequenceUrl());
    SequenceStepsView sequenceStepsView = null;
    Container contentPane = seqStepsViewFrame.getContentPane();
    for (int i = 0, n = contentPane.getComponentCount(); i < n; i++) {
      if (contentPane.getComponent(i) instanceof SequenceStepsView) {
        sequenceStepsView = (SequenceStepsView) contentPane.getComponent( i);
        break;
      }
    }
    if (sequenceStepsView != null) {
      if (sequenceStepsView.getJGoView().getSelection() != null) {
        sequenceStepsView.getJGoView().getSelection().clearSelection();
      }
      StepElement selectedStepElement = sequenceStepsView.getSelectedStepElement();
      List currentElementList =
        (List) sequenceStepsView.getStepElementList().get( currentStepNumber);
      // make sure that the selectedStepElement is in currentElementList
      int stepTypeIndex = -1;
      for (int i = 0, n = currentElementList.size(); i < n; i++) {
        StepElement stepElement = (StepElement) currentElementList.get( i);
        if (stepElement.equals( selectedStepElement)) {
          stepTypeIndex = i;
          break;
        }
      }
      if (stepTypeIndex == -1) {
        Color stepDbBgColor = selectedStepElement.getDbBgColor();
        if (stepDbBgColor.equals( SequenceStepsView.TOKENS_BG_COLOR)) {
          stepTypeIndex = 0;
        } else if (stepDbBgColor.equals( SequenceStepsView.VARIABLES_BG_COLOR)) {
          stepTypeIndex = 1;
        } else if (stepDbBgColor.equals( SequenceStepsView.DB_CONSTRAINTS_BG_COLOR)) {
          stepTypeIndex = 2;
        }
        if (stepTypeIndex == -1) {
          return false;
        } 
      }
      List nextElementList =
        (List) sequenceStepsView.getStepElementList().get( nextStepNumber);
      StepElement nextElement = (StepElement) nextElementList.get( stepTypeIndex);
      sequenceStepsView.getJGoView().getSelection().extendSelection( nextElement);
      sequenceStepsView.setSelectedStepElement( nextElement);
    }
    return isColorIndxFound;
  } // end moveSequenceStepsViewHighlight


  class ButtonAdjustmentListener implements AdjustmentListener {
    private JGoView view;
    private StepButton back;
    private StepButton forward;
    public ButtonAdjustmentListener(JGoView view, StepButton back, StepButton forward) {
      this.view = view;
      this.back = back;
      this.forward = forward;
    }
    public void adjustmentValueChanged(AdjustmentEvent evt) {
      if ((back != null) && (forward != null)) {
        Rectangle viewRect = view.getViewRect();
        //view.getSelection().clearSelection();
        back.setLocation((int)(viewRect.getX() + back.getSize().getWidth()),
                         (int)(viewRect.getY() + viewRect.getHeight() -
                               back.getSize().getHeight()));
        forward.setLocation((int)(back.getLocation().getX() + back.getWidth()),
                            (int)(back.getLocation().getY()));
        //       System.err.println( "adjustmentValueChanged " +
        //                           ((JScrollBar) evt.getSource()).getOrientation());
        // ResourceProfileView & ResourceTransactionView have two vertical
        // scroll bars to keep at equal length
        boolean isRedraw = false, isScrollBarAdjustment = true;
        if ((((JScrollBar) evt.getSource()).getOrientation() == SwingConstants.VERTICAL) &&
            (! ((JScrollBar) evt.getSource()).getValueIsAdjusting()) &&
            (PartialPlanView.this instanceof ResourceView)) {
          // System.err.println( "adjustmentValueChanged  call equalize");
          ((ResourceView) PartialPlanView.this).equalizeViewWidthsAndHeights
            ( (int) (viewRect.getY() + viewRect.getHeight()), isRedraw, isScrollBarAdjustment);
        }
      }
    }
  } // class ButtonAdjustmentListener

  class ButtonViewListener implements JGoViewListener {
    private JGoView view;
    private StepButton back;
    private StepButton forward;
    public ButtonViewListener(JGoView view, StepButton back, StepButton forward) {
      this.view = view;
      this.back = back;
      this.forward = forward;
    }
    public void viewChanged(JGoViewEvent e) {
      //if(e.getHint() == JGoViewEvent.POSITION_CHANGED || e.getHint() == JGoViewEvent.CHANGED) {
        Rectangle viewRect = view.getViewRect();
        //view.getSelection().clearSelection();
        back.setLocation((int)(viewRect.getX() + back.getSize().getWidth()),
                         (int)(viewRect.getY() + viewRect.getHeight() -
                               back.getSize().getHeight()));
        forward.setLocation((int)(back.getLocation().getX() + back.getWidth()),
                            (int)(back.getLocation().getY()));
        //}
    }
  } // end class ButtonViewListener

  /**
   * <code>expandViewFrameForStepButtons</code>
   *
   * @param viewFrame - <code>MDIInternalFrame</code> - 
   */
  protected void expandViewFrameForStepButtons( MDIInternalFrame viewFrame, JGoView jGoView) {
    viewFrame.setSize( (int) viewFrame.getSize().getWidth(),
                       (int) (viewFrame.getSize().getHeight() +
                              forwardButton.getSize().getHeight()));
    forwardButton.setLocation( (int) forwardButton.getLocation().getX(),
                               (int) (forwardButton.getLocation().getY() +
                                      forwardButton.getSize().getHeight()));
    backwardButton.setLocation( (int) backwardButton.getLocation().getX(),
                                (int) (backwardButton.getLocation().getY() +
                                       backwardButton.getSize().getHeight()));
    if (jGoView.getVerticalScrollBar() != null) {
      jGoView.getVerticalScrollBar().setValueIsAdjusting( true);
    }
  } // end expandViewFrameForStepButtons

  /**
   * <code>createOpenViewItems</code> - partial plan background Mouse-Right item
   *                                    all views except current
   *
   * @param partialPlan - <code>PwPartialPlan</code> - 
   * @param partialPlanName - <code>String</code> - 
   * @param planSequence - <code>PwPlanningSequence</code> - 
   * @param mouseRightPopup - <code>JPopupMenu</code> - 
   * @param currentViewName - <code>String</code> - 
   */
  public void createOpenViewItems( final PwPartialPlan partialPlan,
                                   final String partialPlanName,
                                   final PwPlanningSequence planSequence,
                                   JPopupMenu mouseRightPopup, final String currentViewName) {
    List viewListenerList = new ArrayList();
    for (int i = 0, n = PlanWorks.PARTIAL_PLAN_VIEW_LIST.size(); i < n; i++) {
      viewListenerList.add( null);
    }
    createOpenViewItems( partialPlan, partialPlanName, planSequence, mouseRightPopup,
                         viewListenerList, currentViewName);
  } // end createOpenViewItems

  /**
   * <code>createOpenViewItems</code> - partial plan background Mouse-Right item
   *                                    all views except current
   *
   * @param partialPlan - <code>PwPartialPlan</code> - 
   * @param partialPlanName - <code>String</code> - 
   * @param planSequence - <code>PwPlanningSequence</code> - 
   * @param mouseRightPopup - <code>JPopupMenu</code> - 
   * @param viewListenerList - <code>List</code> - 
   * @param currentViewName - <code>String</code> - 
   */
  public void createOpenViewItems( final PwPartialPlan partialPlan,
                                   final String partialPlanName,
                                   final PwPlanningSequence planSequence,
                                   JPopupMenu mouseRightPopup,
                                   final List viewListenerList,
                                   final String currentViewName) {
    if (viewListenerList.size() != PlanWorks.PARTIAL_PLAN_VIEW_LIST.size()) {
      System.err.println( "createOpenViewItems: num view listeners not = " +
                          PlanWorks.PARTIAL_PLAN_VIEW_LIST.size());
      System.exit( -1);
    }
    PartialPlanViewMenu viewMenu = new PartialPlanViewMenu();
    Iterator viewNamesItr = PlanWorks.PARTIAL_PLAN_VIEW_LIST.iterator();
    Iterator viewListenerItr = viewListenerList.iterator();
    while (viewNamesItr.hasNext()) {
      String viewName = (String) viewNamesItr.next();
      if (! viewName.equals( currentViewName)) {
        PartialPlanViewMenuItem openViewItem =
          viewMenu.createOpenViewItem( viewName, partialPlanName, planSequence,
                                       (ViewListener) viewListenerItr.next());
        mouseRightPopup.add( openViewItem);
      }
    }
  } // end createOpenViewItems - viewListenerList

  public void createAnOpenViewFindItem( final PwPartialPlan partialPlan,
                                        final String partialPlanName,
                                        final PwPlanningSequence planSequence,
                                        JPopupMenu mouseRightPopup, final String viewName,
                                        final Integer idToFind) {
    PartialPlanViewMenu viewMenu = new PartialPlanViewMenu();
    ViewListener viewListener = null;
    PartialPlanViewMenuItem openViewItem =
      viewMenu.createOpenViewFindItem( viewName, partialPlanName, planSequence, viewListener,
                                       viewSet, idToFind);
    if (openViewItem != null) {
      mouseRightPopup.add( openViewItem);
    }
  } // end createAnOpenViewFindItem

  /**
   * <code>createRaiseContentSpecItem</code> - partial plan background Mouse-Right item
   *
   * @param raiseContentSpecItem - <code>JMenuItem</code> - 
   */
  public void createRaiseContentSpecItem( JMenuItem raiseContentSpecItem) {
    raiseContentSpecItem.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent evt) {
          MDIInternalFrame contentSpecWindow = viewSet.getContentSpecWindow();
          ViewGenerics.raiseFrame( contentSpecWindow);
        }
      });
  } // end createRaiseContentSpecItem

  /**
   * <code>getNavigatorViewSetKey</code>
   *
   * @return - <code>String</code> - 
   */
  public String getNavigatorViewSetKey() {
    ((PartialPlanViewSet) viewSet).incrNavigatorFrameCnt();
    return new String( ViewConstants.NAVIGATOR_VIEW.replaceAll( " ", "") + "-" +
                       ((PartialPlanViewSet) viewSet).getNavigatorFrameCnt());
  }

  /**
   * <code>openNavigatorViewFrame</code>
   *
   * @return - <code>MDIInternalFrame</code> - 
   */
  public MDIInternalFrame openNavigatorViewFrame( String viewSetKey) {
    String viewName = ViewConstants.NAVIGATOR_VIEW.replaceAll( " ", "");
    String rootNavigatorViewName = viewName + " of " + partialPlan.getName();
    int indx = viewSetKey.indexOf( "-") + 1;
    String navigatorViewName =
      rootNavigatorViewName.concat(" - ").concat( viewSetKey.substring( indx));
    MDIInternalFrame navigatorFrame = 
      ((MDIDesktopFrame) PlanWorks.getPlanWorks()).createFrame( navigatorViewName,
                                                                viewSet, true, true,
                                                                true, true);
    viewSet.getViews().put( viewSetKey, navigatorFrame);
    return navigatorFrame;
  } // end openNavigatorViewFrame


  class ViewClose implements UnaryFunctor {
    private PartialPlanViewSet viewSet;
    private String viewName;
    public ViewClose(PartialPlanViewSet viewSet, String viewName) {
      this.viewSet = viewSet;
      this.viewName = viewName;
    }
    public final Object func(Object o) {
      if(o instanceof String && ((String) o).indexOf(viewName) >= 0) {
        MDIInternalFrame viewFrame = 
          (MDIInternalFrame) viewSet.getView(o);
        try {
          viewFrame.setClosed(true);
        } 
        catch ( PropertyVetoException pve){
        }
      }
      return o;
    }
  } // end class ViewClose


  /**
   * <code>createCloseNavigatorWindowsItem</code>
   *
   * @param closeWindowsItem - <code>JMenuItem</code> - 
   */
  public void createCloseNavigatorWindowsItem( JMenuItem closeWindowsItem) {
    closeWindowsItem.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent evt) {
          String navigatorWindowName = ViewConstants.NAVIGATOR_VIEW.replaceAll( " ", "");
          List windowKeyList = new ArrayList( viewSet.getViews().keySet());
          CollectionUtils.lMap(new ViewClose((PartialPlanViewSet) viewSet, navigatorWindowName),
                               windowKeyList);
          ((PartialPlanViewSet) viewSet).setNavigatorFrameCnt( 0);
        }
      });
  } // end createCloseNavigatorWindowsItem


  /**
   * <code>getRuleViewSetKey</code>
   *
   * @return - <code>String</code> - 
   */
  public String getRuleViewSetKey() {
    ((PartialPlanViewSet) viewSet).incrRuleFrameCnt();
    return new String( ViewConstants.RULE_INSTANCE_VIEW.replaceAll( " ", "") + "-" +
                       ((PartialPlanViewSet) viewSet).getRuleFrameCnt());
  }

  /**
   * <code>openRuleViewFrame</code>
   *
   * @return - <code>MDIInternalFrame</code> - 
   */
  public MDIInternalFrame openRuleViewFrame( String viewSetKey) {
    String rootRuleViewName = ViewConstants.RULE_INSTANCE_VIEW.replaceAll( " ", "") +
      " of " + partialPlan.getName();
    int indx = viewSetKey.indexOf( "-") + 1;
    String ruleViewName =
      rootRuleViewName.concat(" - ").concat( viewSetKey.substring( indx));
    MDIInternalFrame ruleFrame = 
      ((MDIDesktopFrame) PlanWorks.getPlanWorks()).createFrame( ruleViewName,
                                                                viewSet, true, true,
                                                                true, true);
    viewSet.getViews().put( viewSetKey, ruleFrame);
    return ruleFrame;
  } // end openRuleViewFrame

  /**
   * <code>createCloseRuleWindowsItem</code>
   *
   * @param closeWindowsItem - <code>JMenuItem</code> - 
   */
  public void createCloseRuleWindowsItem( JMenuItem closeWindowsItem) {
    closeWindowsItem.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent evt) {
          List windowKeyList = new ArrayList( viewSet.getViews().keySet());
          CollectionUtils.lMap(new ViewClose( (PartialPlanViewSet) viewSet,
                                              ViewConstants.RULE_INSTANCE_VIEW.
                                              replaceAll( " ", "")),
                               windowKeyList);
          ((PartialPlanViewSet) viewSet).setRuleFrameCnt( 0);
        }
      });
  } // end createCloseRuleWindowsItem

  /**
   * <code>getUnaryResourceProfileViewSetKey</code>
   *
   * @param resourceName - <code>String</code> - 
   * @return - <code>String</code> - 
   */
  public String getUnaryResourceProfileViewSetKey( String resourceName) {
    ((PartialPlanViewSet) viewSet).incrUnaryResourceFrameCnt( resourceName);
    return new String(  ViewConstants.RESOURCE_PROFILE_VIEW.replaceAll( " ", "") +
                        "-" + resourceName + "-" +
                        ((PartialPlanViewSet) viewSet).getUnaryResourceFrameCnt( resourceName));
  } // end getUnaryResourceProfileViewSetKey

  /**
   * <code>openUnaryResourceProfileViewFrame</code>
   *
   * @param viewSetKey - <code>String</code> - 
   * @return - <code>MDIInternalFrame</code> - 
   */
  public MDIInternalFrame openUnaryResourceProfileViewFrame( String viewSetKey) {
    String rootUnaryResourceProfileViewName =
      ViewConstants.RESOURCE_PROFILE_VIEW.replaceAll( " ", "") + " of " +
      partialPlan.getName();
    int indx = viewSetKey.indexOf( "-") + 1;
    String unaryResourceProfileViewName =
      rootUnaryResourceProfileViewName.concat(" - ").
      concat( viewSetKey.substring( indx).replaceAll( "-", " - "));
    MDIInternalFrame unaryResourceProfileFrame = 
      ((MDIDesktopFrame) PlanWorks.getPlanWorks()).createFrame( unaryResourceProfileViewName,
                                                                viewSet, true, true,
                                                                true, true);
    viewSet.getViews().put( viewSetKey, unaryResourceProfileFrame);
    return unaryResourceProfileFrame;
  } // end openUnaryResourceProfileViewFrame

  /**
   * <code>getTimelineColor</code>
   *
   * @param timelineId - <code>Integer</code> - 
   * @param timelineIndexMap - <code>Map</code> - 
   * @return - <code>Color</code> - 
   */
  public Color getTimelineColor( Integer timelineId){//, Map timelineIndexMap) {
    return ((PartialPlanViewSet) viewSet).getColorStream().getColor(timelineId);
  }

  public List getDefaultLinkTypes(){return null;}
} // end class PartialPlanView

