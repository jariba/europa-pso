// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PartialPlanView.java,v 1.10 2003-12-20 00:46:04 miatauro Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 25sept03
//

package gov.nasa.arc.planworks.viz.partialPlan;

import java.awt.Point;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.PwPlanningSequence;
import gov.nasa.arc.planworks.db.PwSlot;
import gov.nasa.arc.planworks.db.PwTimeline;
import gov.nasa.arc.planworks.db.PwToken;
import gov.nasa.arc.planworks.mdi.MDIInternalFrame;
import gov.nasa.arc.planworks.util.ResourceNotFoundException;
// import gov.nasa.arc.planworks.util.Utilities;
import gov.nasa.arc.planworks.viz.ViewGenerics;
import gov.nasa.arc.planworks.viz.VizView;
import gov.nasa.arc.planworks.viz.VizViewOverview;
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

  public void createSteppingItems(JPopupMenu mouseRightPopup) {
    JMenuItem nextItem = new JMenuItem("Step forward");
    JMenuItem prevItem = new JMenuItem("Step backward");

    nextItem.addActionListener(new StepForwardListener(this));
    prevItem.addActionListener(new StepBackwardListener(this));
    mouseRightPopup.add(nextItem);
    mouseRightPopup.add(prevItem);
  }
  class StepForwardListener implements ActionListener {
    private PartialPlanView view;
    public StepForwardListener(PartialPlanView view) {
      this.view = view;
    }
    public void actionPerformed(ActionEvent e) {
      ViewSet viewSet = view.getViewSet();
      MDIInternalFrame viewFrame = viewSet.getViewByClass(view.getClass());
      ViewManager viewManager = viewSet.getViewManager();
      int prevStepNumber = getPartialPlan().getStepNumber();
      PwPartialPlan nextStep;
      try {
        nextStep = PlanWorks.planWorks.getCurrentProject().
          getPlanningSequence(getPartialPlan().getSequenceUrl()).
          getNextPartialPlan(prevStepNumber);
      }
      catch(IndexOutOfBoundsException ibe) {
        JOptionPane.showMessageDialog(PlanWorks.planWorks, "Attempted to step beyond last step.",
                                      "Step Exception", JOptionPane.ERROR_MESSAGE);
        ibe.printStackTrace();
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
        nextStep.setName(seqName[0] + System.getProperty("file.separator") + "step" + (prevStepNumber + 1));
      }
      MDIInternalFrame nextViewFrame = viewManager.openView(nextStep, view.getClass().getName());
      try {
        nextViewFrame.setBounds(viewFrame.getBounds());
        nextViewFrame.setNormalBounds(viewFrame.getNormalBounds());
        nextViewFrame.setSelected(true);
        VizViewOverview overview = null;
        VizView view = null;
        Container contentPane = viewFrame.getContentPane();
        for(int i = 0; i < contentPane.getComponentCount(); i++) {
          if(contentPane.getComponent(i) instanceof VizView) {
            view = (VizView) contentPane.getComponent(i);
            overview = view.getOverview();
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
          VizViewOverview nextOverview = 
            ViewGenerics.openOverviewFrame("Temporary View", nextStep, nextView, 
                                           viewManager.getViewSet(nextStep), nextJGoView, 
                                           new Point(30, 30));
          nextView.setOverview(nextOverview);
        }
        viewSet.removeViewFrame(viewFrame);
        viewFrame.setClosed(true);
      }
      catch(Exception ack){ack.printStackTrace();}
    }
  }
  class StepBackwardListener implements ActionListener {
    private PartialPlanView view;
    public StepBackwardListener(PartialPlanView view) {
      this.view = view;
    }
    public void actionPerformed(ActionEvent e) {
      ViewSet viewSet = view.getViewSet();
      MDIInternalFrame viewFrame = viewSet.getViewByClass(view.getClass());
      ViewManager viewManager = viewSet.getViewManager();
      int nextStepNumber = getPartialPlan().getStepNumber();
      PwPartialPlan prevStep;
      try {
        prevStep = PlanWorks.planWorks.getCurrentProject().
          getPlanningSequence(getPartialPlan().getSequenceUrl()).
          getPrevPartialPlan(nextStepNumber);
      }
      catch(IndexOutOfBoundsException ibe) {
        JOptionPane.showMessageDialog(PlanWorks.planWorks, "Attempted to step beyond first step.",
                                      "Step Exception", JOptionPane.ERROR_MESSAGE);

        ibe.printStackTrace();
        return;
      }
      catch(ResourceNotFoundException rnfe) {
        JOptionPane.showMessageDialog(PlanWorks.planWorks, rnfe.getMessage(),
                                      "ResourceNotFoundException", JOptionPane.ERROR_MESSAGE);
        rnfe.printStackTrace();
        return;
      }
      if(prevStep.getName() == null) {
        String [] title = viewFrame.getTitle().split("\\s+");
        String [] seqName = title[title.length - 1].split(System.getProperty("file.separator"));
        prevStep.setName(seqName[0] + System.getProperty("file.separator") + "step" + (nextStepNumber - 1));
      }
      MDIInternalFrame prevView = viewManager.openView(prevStep, view.getClass().getName());
      try {
        if(viewFrame.isMaximum()) {
          prevView.setNormalBounds(viewFrame.getNormalBounds());
          //prevView.setMaximum(true);
        }
        else {
          prevView.setBounds(viewFrame.getBounds());
          //prevView.setMaximum(false);
        }
        prevView.setIcon(viewFrame.isIcon());
        prevView.setSelected(true);
        viewSet.removeViewFrame(viewFrame);
        viewFrame.setClosed(true);
      }
      catch(Exception ack){ack.printStackTrace();}
    }
  }
} // end class PartialPlanView

