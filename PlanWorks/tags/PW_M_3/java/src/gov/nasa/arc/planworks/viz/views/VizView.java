// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: VizView.java,v 1.9 2003-07-15 16:13:12 miatauro Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 18May03
//

package gov.nasa.arc.planworks.viz.views;

import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.PwSlot;
import gov.nasa.arc.planworks.db.PwTimeline;
import gov.nasa.arc.planworks.db.PwToken;


/**
 * <code>VizView</code> - 
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *           NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class VizView extends JPanel {

  protected PwPartialPlan partialPlan;
  protected List validTokenIds;
  protected List displayedTokenIds;


  /**
   * <code>VizView</code> - constructor 
   *
   * @param partialPlan - <code>PwPartialPlan</code> - 
   */
  public VizView( PwPartialPlan partialPlan) {
    super();
    this.partialPlan = partialPlan;
    validTokenIds = null;
    displayedTokenIds = null;
  }

  /**
   * <code>redraw</code> - each subclass of VizView will implement redraw()
   *
   */
  public void redraw() {
  }


  /**
   * <code>isContentSpecRendered</code>
   *
   * @param viewName - <code>String</code> - 
   * @param showDialog - <code>boolean</code> - 
   * @return - <code>boolean</code> - 
   */
  public boolean isContentSpecRendered( String viewName, boolean showDialog) {
    Iterator validIterator = validTokenIds.iterator();
    List unDisplayedIds = new ArrayList();
    List extraDisplayedIds = new ArrayList();
    StringBuffer message = new StringBuffer();
    boolean error = false;

    while (validIterator.hasNext()) {
      Integer key = (Integer) validIterator.next();
      if (displayedTokenIds.indexOf( key) == -1) {
        unDisplayedIds.add( key);
      }
    }
    Iterator displayedIterator = displayedTokenIds.iterator();
    while(displayedIterator.hasNext()) {
      Integer key = (Integer) displayedIterator.next();
      if(validTokenIds.indexOf(key) == -1) {
        extraDisplayedIds.add(key);
      }
    }
    message.append("\n");
    if(extraDisplayedIds.size() != 0) {
      if(showDialog) {
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
    if(error) {
      if(showDialog) {
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
  public boolean isTimelineInContentSpec( PwTimeline timeline) {
    List slotList = timeline.getSlotList();
    Iterator slotIterator = slotList.iterator();
    while (slotIterator.hasNext()) {
      PwSlot slot = (PwSlot) slotIterator.next();
      List tokenList = slot.getTokenList();
      if (tokenList.size() > 0) {
        Iterator tokenIterator = tokenList.iterator();
        while (tokenIterator.hasNext()) {
          PwToken token = (PwToken) tokenIterator.next();
          Integer key = token.getKey();
          if (validTokenIds.indexOf( key) >= 0) {
            if (displayedTokenIds.indexOf( key) == -1) {
              displayedTokenIds.add( key);
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
  public boolean isSlotInContentSpec( PwSlot slot) {
    boolean foundMatch = false;
    List tokenList = slot.getTokenList();
    if (tokenList.size() > 0) {
      Iterator tokenIterator = tokenList.iterator();
      while (tokenIterator.hasNext()) {
        PwToken token = (PwToken) tokenIterator.next();
        Integer key = token.getKey();
        if (validTokenIds.indexOf( key) >= 0) {
          foundMatch = true;
          if (displayedTokenIds.indexOf( key) == -1) {
            displayedTokenIds.add( key);
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
  public boolean isTokenInContentSpec( PwToken token) {
    Integer key = token.getKey();
    if (validTokenIds.indexOf( key) >= 0) {
      if (displayedTokenIds.indexOf( key) == -1) {
        displayedTokenIds.add( key);
      }
      return true;
    } else {
      return false;
    }
  } // end isTokenInContentSpec



} // end class VizView

