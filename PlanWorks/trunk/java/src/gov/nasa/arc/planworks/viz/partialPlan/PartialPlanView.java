// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PartialPlanView.java,v 1.15 2004-01-02 18:58:58 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 25sept03
//

package gov.nasa.arc.planworks.viz.partialPlan;

import java.awt.Point;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JPopupMenu;
import javax.swing.JOptionPane;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import com.nwoods.jgo.JGoView;
import com.nwoods.jgo.JGoViewEvent;
import com.nwoods.jgo.JGoViewListener;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.PwPlanningSequence;
import gov.nasa.arc.planworks.db.PwSlot;
import gov.nasa.arc.planworks.db.PwTimeline;
import gov.nasa.arc.planworks.db.PwToken;
import gov.nasa.arc.planworks.mdi.MDIInternalFrame;
import gov.nasa.arc.planworks.util.ColorMap;
import gov.nasa.arc.planworks.util.ResourceNotFoundException;
// import gov.nasa.arc.planworks.util.Utilities;
import gov.nasa.arc.planworks.viz.ViewGenerics;
import gov.nasa.arc.planworks.viz.VizView;
import gov.nasa.arc.planworks.viz.VizViewOverview;
import gov.nasa.arc.planworks.viz.util.JGoButton;
import gov.nasa.arc.planworks.viz.viewMgr.ViewSet;
import gov.nasa.arc.planworks.viz.viewMgr.ViewManager;


/**
 * <code>PartialPlanView</code> - 
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *           NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class PartialPlanView extends VizView {

  protected PwPartialPlan partialPlan;
  protected List validTokenIds;
  protected List displayedTokenIds;

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
  }

  /**
   * <code>getPartialPlan</code>
   *
   * @return - <code>PwPartialPlan</code> - 
   */
  public PwPartialPlan getPartialPlan() {
    return partialPlan;
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
        JOptionPane.showMessageDialog(PlanWorks.planWorks, message.toString(),
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

  protected void addStepButtons(JGoView view) {
    Rectangle viewRect = view.getViewRect();
    Point backwardButtonPt = new Point((int)viewRect.getX(), 
                                       (int)(viewRect.getY() + viewRect.getHeight()));
    JGoButton backwardButton = new JGoButton(backwardButtonPt, ColorMap.getColor("khaki2"), "<",
                                             "Step backward.");
    backwardButton.setLocation((int)(viewRect.getX() + backwardButton.getSize().getWidth()),
                               (int)(viewRect.getY() + viewRect.getHeight() - 
                                     backwardButton.getSize().getHeight()));

    Point forwardButtonPt = new Point((int)(viewRect.getX() + 
                                       backwardButton.getSize().getWidth() + 10),
                                      (int)(viewRect.getY() + viewRect.getHeight()));
    JGoButton forwardButton = new JGoButton(forwardButtonPt, ColorMap.getColor("khaki2"), ">", 
                                            "Step forward.");
    forwardButton.setLocation((int)(backwardButton.getLocation().getX() + 
                                    backwardButton.getWidth()),
                              (int)(backwardButton.getLocation().getY()));
    view.getDocument().addObjectAtTail(backwardButton);
    view.getDocument().addObjectAtTail(forwardButton);
    //view.addObjectAtTail(backwardButton);
    //view.addObjectAtTail(forwardButton);

     view.getHorizontalScrollBar().
       addAdjustmentListener(new ButtonAdjustmentListener(view, backwardButton, forwardButton));
     view.getVerticalScrollBar().
       addAdjustmentListener(new ButtonAdjustmentListener(view, backwardButton, forwardButton));

    view.addViewListener(new ButtonViewListener(view, backwardButton, forwardButton));
    backwardButton.addActionListener(new StepButtonListener(-1, view, this));
    forwardButton.addActionListener(new StepButtonListener(1, view, this));
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
      view.getSelection().clearSelection();
      ViewSet viewSet = pView.getViewSet();
      MDIInternalFrame viewFrame = viewSet.getViewByClass(pView.getClass());
      ViewManager viewManager = viewSet.getViewManager();
      int currStepNumber = getPartialPlan().getStepNumber();
      PwPartialPlan nextStep = null;
      try {
        if(dir == 1) {
          nextStep = PlanWorks.planWorks.getCurrentProject().
            getPlanningSequence(getPartialPlan().getSequenceUrl()).
            getNextPartialPlan(currStepNumber);
        }
        else if(dir == -1) {
          nextStep = PlanWorks.planWorks.getCurrentProject().
            getPlanningSequence(getPartialPlan().getSequenceUrl()).
            getPrevPartialPlan(currStepNumber);
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
        JOptionPane.showMessageDialog(PlanWorks.planWorks, message, "Step Exception", 
                                      JOptionPane.ERROR_MESSAGE);
        // ibe.printStackTrace();
        return;
      }
      catch(ResourceNotFoundException rnfe) {
        JOptionPane.showMessageDialog(PlanWorks.planWorks, rnfe.getMessage(),
                                      "ResourceNotFoundException", JOptionPane.ERROR_MESSAGE);

        rnfe.printStackTrace();
        return;
      }
      if(nextStep.getName() == null) {
        String [] title = viewFrame.getTitle().split("\\s+");
        String [] seqName = title[title.length - 1].split(System.getProperty("file.separator"));
        nextStep.setName(seqName[0] + System.getProperty("file.separator") + "step" + (currStepNumber + dir));
      }
      MDIInternalFrame nextViewFrame = viewManager.openView(nextStep, pView.getClass().getName());
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
  }

  class ButtonAdjustmentListener implements AdjustmentListener {
    private JGoView view;
    private JGoButton back;
    private JGoButton forward;
    public ButtonAdjustmentListener(JGoView view, JGoButton back, JGoButton forward) {
      this.view = view;
      this.back = back;
      this.forward = forward;
    }
    public void adjustmentValueChanged(AdjustmentEvent e) {
      Rectangle viewRect = view.getViewRect();
      view.getSelection().clearSelection();
      back.setLocation((int)(viewRect.getX() + back.getSize().getWidth()),
                       (int)(viewRect.getY() + viewRect.getHeight() -
                             back.getSize().getHeight()));
      forward.setLocation((int)(back.getLocation().getX() + back.getWidth()),
                          (int)(back.getLocation().getY()));
    }
  }
  class ButtonViewListener implements JGoViewListener {
    private JGoView view;
    private JGoButton back;
    private JGoButton forward;
    public ButtonViewListener(JGoView view, JGoButton back, JGoButton forward) {
      this.view = view;
      this.back = back;
      this.forward = forward;
    }
    public void viewChanged(JGoViewEvent e) {
      if(e.getHint() == JGoViewEvent.POSITION_CHANGED) {
        Rectangle viewRect = view.getViewRect();
        view.getSelection().clearSelection();
        back.setLocation((int)(viewRect.getX() + back.getSize().getWidth()),
                         (int)(viewRect.getY() + viewRect.getHeight() -
                               back.getSize().getHeight()));
        forward.setLocation((int)(back.getLocation().getX() + back.getWidth()),
                            (int)(back.getLocation().getY()));
      }
    }
  }
  /**
   * <code>createOpenViewItems</code> - partial plan background Mouse-Right item
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
                                   JPopupMenu mouseRightPopup, String currentViewName) {
    PartialPlanViewMenu viewMenu = new PartialPlanViewMenu();
    Iterator viewNamesItr = PlanWorks.planWorks.PARTIAL_PLAN_VIEW_LIST.iterator();
    while (viewNamesItr.hasNext()) {
      String viewName = (String) viewNamesItr.next();
      if (! viewName.equals( currentViewName)) {
        PartialPlanViewMenuItem openViewItem =
          viewMenu.createOpenViewItem( viewName, partialPlanName, planSequence);
        mouseRightPopup.add( openViewItem);
      }
    }
  } // end createOpenViewItems

  /**
   * <code>createRaiseContentSpecItem</code> - partial plan background Mouse-Right item
   *
   * @param raiseContentSpecItem - <code>JMenuItem</code> - 
   */
  public void createRaiseContentSpecItem( JMenuItem raiseContentSpecItem) {
    raiseContentSpecItem.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent evt) {
          MDIInternalFrame contentSpecWindow = viewSet.getContentSpecWindow();
          // bring window to the front
          try {
            // in case content spec window was iconified
            if (contentSpecWindow.isIcon()) {
              contentSpecWindow.setIcon( false);
            }
            contentSpecWindow.setSelected( false);
            contentSpecWindow.setSelected( true);
          } catch (PropertyVetoException excp) {
          }
        }
      });
  } // end createRaiseContentSpecItem


} // end class PartialPlanView

