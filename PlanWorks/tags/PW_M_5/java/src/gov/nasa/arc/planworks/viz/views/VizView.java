// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: VizView.java,v 1.16 2003-09-02 21:49:17 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 18May03
//

package gov.nasa.arc.planworks.viz.views;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoText;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.PwDomain;
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.PwSlot;
import gov.nasa.arc.planworks.db.PwTimeline;
import gov.nasa.arc.planworks.db.PwToken;
import gov.nasa.arc.planworks.mdi.MDIInternalFrame;
// import gov.nasa.arc.planworks.util.Utilities;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.viewMgr.ViewSet;


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
    
    JGoText.setDefaultFontFaceName("Monospaced");
    JGoText.setDefaultFontSize( ViewConstants.TIMELINE_VIEW_FONT_SIZE);

    // Utilities.printFontNames();
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
    while(displayedIterator.hasNext()) {
      Integer id = (Integer) displayedIterator.next();
      if(validTokenIds.indexOf(id) == -1) {
        extraDisplayedIds.add(id);
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
   * <code>expandViewFrame</code> - expand up to size of PlanWorks frame
   *
   * @param viewSet - <code>ViewSet</code> - 
   * @param viewName - <code>String</code> - 
   * @param maxViewWidth - <code>int</code> - 
   * @param maxViewHeight - <code>int</code> - 
   */
  protected void expandViewFrame( ViewSet viewSet, String viewName, int maxViewWidth,
                                  int maxViewHeight) {
    MDIInternalFrame viewFrame = null;
    if (viewName.equals( "timelineView")) {
      viewFrame = viewSet.openTimelineView( 0L);
    } else if (viewName.equals( "tokenNetworkView")) {
      viewFrame = viewSet.openTokenNetworkView( 0L);
    } else if (viewName.equals( "temporalExtentView")) {
      viewFrame = viewSet.openTemporalExtentView( 0L);
    } else if (viewName.equals( "constraintNetworkView")) {
      viewFrame = viewSet.openConstraintNetworkView( 0L);
    } else if (viewName.equals( "temporalNetworkView")) {
      JOptionPane.showMessageDialog
        (PlanWorks.planWorks, viewName, "View Not Supported", 
         JOptionPane.INFORMATION_MESSAGE);
    } else {
      JOptionPane.showMessageDialog
        (PlanWorks.planWorks, viewName, "View Not Supported", 
         JOptionPane.INFORMATION_MESSAGE);
    }
    maxViewWidth = Math.min( maxViewWidth,
                             (int) PlanWorks.planWorks.getSize().getWidth() -
                             (int) viewFrame.getLocation().getX() -
                             ViewConstants.MDI_FRAME_DECORATION_WIDTH -
                             ViewConstants.FRAME_DECORATION_WIDTH); 
    maxViewHeight = Math.min( maxViewHeight,
                              (int) PlanWorks.planWorks.getSize().getHeight() -
                              (int) viewFrame.getLocation().getY() -
                              ViewConstants.MDI_FRAME_DECORATION_HEIGHT -
                              ViewConstants.FRAME_DECORATION_HEIGHT); 
    viewFrame.setSize( maxViewWidth + ViewConstants.MDI_FRAME_DECORATION_WIDTH,
                       maxViewHeight + ViewConstants.MDI_FRAME_DECORATION_HEIGHT);
  } // end expandViewFrame


} // end class VizView

