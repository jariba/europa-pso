// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PartialPlanView.java,v 1.5 2003-11-06 00:02:18 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 25sept03
//

package gov.nasa.arc.planworks.viz.partialPlan;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.PwPlanningSequence;
import gov.nasa.arc.planworks.db.PwSlot;
import gov.nasa.arc.planworks.db.PwTimeline;
import gov.nasa.arc.planworks.db.PwToken;
import gov.nasa.arc.planworks.mdi.MDIInternalFrame;
// import gov.nasa.arc.planworks.util.Utilities;
import gov.nasa.arc.planworks.viz.VizView;
import gov.nasa.arc.planworks.viz.viewMgr.ViewSet;


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
   * <code>createChangeViewItem</code> - partial plan background Mouse-Right item
   *
   * @param changeViewItem - <code>JMenuItem</code> - 
   * @param partialPlan - <code>PwPartialPlan</code> - 
   * @param viewCoords - <code>Point</code> - 
   */
  public void createChangeViewItem( JMenuItem changeViewItem,
                                    final PwPartialPlan partialPlan,
                                    final Point viewCoords) {
    changeViewItem.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent evt) {
          String partialPlanName = partialPlan.getPartialPlanName();
          int stepNumber = partialPlan.getStepNumber();
          PwPlanningSequence planSequence =
            PlanWorks.planWorks.getPlanSequence( partialPlan);

          PartialPlanViewMenu mouseRightPopup = new PartialPlanViewMenu();
          JMenuItem header = new JMenuItem( "step" + stepNumber);
          mouseRightPopup.add( header);
          mouseRightPopup.addSeparator();

          mouseRightPopup.buildPartialPlanViewMenu( partialPlanName, planSequence);
          mouseRightPopup.show( PlanWorks.planWorks, (int) viewCoords.getX(),
                                (int) viewCoords.getY());
        }
      });
  } // end createChangeViewItem


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
            contentSpecWindow.setSelected( false);
            contentSpecWindow.setSelected( true);
          } catch (PropertyVetoException excp) {
          }
        }
      });
  } // end createRaiseContentSpecItem

  /**
   * <code>createAllViewItems</code>- partial plan background Mouse-Right items
   *
   * @param partialPlan - <code>PwPartialPlan</code> - 
   * @param mouseRightPopup - <code>JPopupMenu</code> - 
   */
  public void createAllViewItems( PwPartialPlan partialPlan, JPopupMenu mouseRightPopup) {
    mouseRightPopup.addSeparator();

    JMenuItem closeAllItem = new JMenuItem( "Close All Views");
    createCloseAllItem( closeAllItem);
    mouseRightPopup.add( closeAllItem);

    JMenuItem hideAllItem = new JMenuItem( "Hide All Views");
    createHideAllItem( hideAllItem);
    mouseRightPopup.add( hideAllItem);

    JMenuItem openAllItem = new JMenuItem( "Open All Views");
    createOpenAllItem( openAllItem, partialPlan);
    mouseRightPopup.add( openAllItem);
  } // end createAllViewItems

  private void createCloseAllItem( JMenuItem closeAllItem) {
    closeAllItem.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent evt) {
          PartialPlanView.this.viewSet.close();
        }
      });
  } // end createCloseAllItem
 
  private void createHideAllItem( JMenuItem hideAllItem) {
    hideAllItem.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent evt) {
          PartialPlanView.this.viewSet.iconify();
        }
      });
  } // end createHideAllItem
 
  private void createOpenAllItem( JMenuItem openAllItem, final PwPartialPlan partialPlan) {
    openAllItem.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent evt) {
          PwPlanningSequence planSequence =
            PlanWorks.planWorks.getPlanSequence( partialPlan);
          String seqUrl = planSequence.getUrl();
          String seqName = planSequence.getName();
          String partialPlanName = partialPlan.getPartialPlanName();

          boolean isInvokeAndWait = true;
          Iterator viewListItr = PlanWorks.PARTIAL_PLAN_VIEW_LIST.iterator();
          while (viewListItr.hasNext()) {
            final String viewName = (String) viewListItr.next();
            final PartialPlanViewMenuItem viewItem =
              new PartialPlanViewMenuItem( viewName, seqUrl, seqName, partialPlanName);
            new CreatePartialPlanViewThread( viewName, viewItem, isInvokeAndWait).start();
          }
        }
      });
  } // end createOpenAllItem
 
} // end class PartialPlanView

