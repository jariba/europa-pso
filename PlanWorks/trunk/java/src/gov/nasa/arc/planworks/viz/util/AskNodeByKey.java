// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: AskNodeByKey.java,v 1.4 2004-08-21 00:31:58 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 07oct03
//

package gov.nasa.arc.planworks.viz.util;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent; 
import java.beans.PropertyChangeListener; 
import java.util.Iterator;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.PwDecision;
import gov.nasa.arc.planworks.db.PwEntity;
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.util.Utilities;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanView;
import gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork.ConstraintNetworkView;
import gov.nasa.arc.planworks.viz.partialPlan.decision.DecisionView;
import gov.nasa.arc.planworks.viz.partialPlan.navigator.NavigatorView;
import gov.nasa.arc.planworks.viz.partialPlan.resourceProfile.ResourceProfileView;
import gov.nasa.arc.planworks.viz.partialPlan.resourceTransaction.ResourceTransactionView;
import gov.nasa.arc.planworks.viz.partialPlan.temporalExtent.TemporalExtentView;
import gov.nasa.arc.planworks.viz.partialPlan.timeline.TimelineView;
import gov.nasa.arc.planworks.viz.partialPlan.tokenNetwork.TokenNetworkView;


/**
 * <code>AskNodeByKey</code> - custom dialog to allow user to enter
 *           a value for a token/slot/constraint/variable node key, and check that it exists.
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *           NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class AskNodeByKey extends JDialog { 

  private static final int KEY_NOT_VALID = -1;
  private static final int KEY_NOT_AVAILABLE = 0;
  private static final int KEY_FOUND = 1;

  private PartialPlanView partialPlanView;
  private Integer nodeKey;
  private PwEntity entity;

  private String typedText = null;
  private JOptionPane optionPane;
  private JTextField textField;
  private String btnString1;
  private String btnString2;

  /**
   * <code>AskNodeByKey</code> - constructor 
   *
   * @param dialogTitle - <code>String</code> - 
   * @param textFieldLabel - <code>String</code> - 
   * @param partialPlanView - <code>PartialPlanView</code> - 
   */
  public AskNodeByKey( String dialogTitle, String textFieldLabel,
                       PartialPlanView partialPlanView) {
    // modal dialog - blocks other activity
    super( PlanWorks.getPlanWorks(), true);
    this.partialPlanView = partialPlanView;
    nodeKey = null;
    entity = null;

    setTitle( dialogTitle);
    final String msgString1 = textFieldLabel;
    textField = new JTextField(10);
    Object[] array = {msgString1, textField};

    btnString1 = "Enter";
    btnString2 = "Cancel";
    Object[] options = {btnString1, btnString2};

    // current value
    textField.setText( "");
    optionPane = new JOptionPane
      ( array, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION,
        null, options, options[0]);
    setContentPane( optionPane);
    setDefaultCloseOperation( DO_NOTHING_ON_CLOSE);
    addWindowListener( new WindowAdapter() {
        public void windowClosing(WindowEvent we) {
          /*
           * Instead of directly closing the window,
           * we're going to change the JOptionPane's
           * value property.
           */
          optionPane.setValue( new Integer( JOptionPane.CLOSED_OPTION));
        }
      });

    textField.addActionListener( new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          optionPane.setValue( btnString1);
        }
      });

    addInputListener();

    // size dialog appropriately
    pack();
    setBackground( ViewConstants.VIEW_BACKGROUND_COLOR);
    Utilities.setPopupLocation( this, PlanWorks.getPlanWorks());
    setVisible( true);
  } // end constructor


  /**
   * <code>getNodeKey</code>
   *
   * @return nodeKey - <code>Integer</code> - 
   */
  public Integer getNodeKey() {
    return nodeKey;
  }

  /**
   * <code>getEntity</code> - for DecisionView and PwDecision
   *
   * @return entity - <code>PwEntity</code> - 
   */
  public PwEntity getEntity() {
    return entity;
  }

  private void addInputListener() {
    optionPane.addPropertyChangeListener( new PropertyChangeListener() {
        public void propertyChange( PropertyChangeEvent e) {
          String prop = e.getPropertyName();
          if (isVisible() && (e.getSource() == optionPane) &&
              (prop.equals(JOptionPane.VALUE_PROPERTY) ||
               prop.equals(JOptionPane.INPUT_VALUE_PROPERTY))) {
            Object value = optionPane.getValue();

            if (value == JOptionPane.UNINITIALIZED_VALUE) {
              //ignore reset
              return;
            }
            // Reset the JOptionPane's value.
            // If you don't do this, then if the user
            // presses the same button next time, no
            // property change event will be fired.
            optionPane.setValue( JOptionPane.UNINITIALIZED_VALUE);

            if (value.equals( btnString1)) {
              typedText = textField.getText();
              try {
                nodeKey = new Integer( Integer.parseInt( typedText));
                // System.err.println( "AskNodeByKey key " + typedText);
                if (isNodeKeyValid( nodeKey) == KEY_NOT_VALID) {
                  JOptionPane.showMessageDialog
                    (PlanWorks.getPlanWorks(),
                     "Sorry, \"" + nodeKey.toString() + "\" " + "is not a valid key.",
                     "Invalid Key", JOptionPane.ERROR_MESSAGE);
                } else if (isNodeKeyValid( nodeKey) == KEY_NOT_AVAILABLE) {
                  JOptionPane.showMessageDialog
                    (PlanWorks.getPlanWorks(),
                     "Sorry, \"" + nodeKey.toString() + "\" " +
                     "is a valid key, but is not available in this view.",
                     "Key Not Available", JOptionPane.ERROR_MESSAGE);
                } else {
                  // we're done; dismiss the dialog
                  setVisible( false);
                }
              } catch ( NumberFormatException except) {
                // text was invalid
                textField.selectAll();
                JOptionPane.showMessageDialog
                  (PlanWorks.getPlanWorks(),
                   "Sorry, \"" + typedText + "\" " + "isn't a valid response.\n" +
                   "Please enter a intger number",
                   "Invalid value for key", JOptionPane.ERROR_MESSAGE);
                nodeKey = null;
              }
            } else { // user closed dialog or clicked cancel
              nodeKey = null;
              setVisible( false);
            }
          }
        }
      });
  } // end addInputListener

  private int isNodeKeyValid( Integer nodeKey) {
    int keyStatus = KEY_NOT_VALID; 
    PwPartialPlan partialPlan = partialPlanView.getPartialPlan();
    if (partialPlan.getEntity( nodeKey ) != null) {
      keyStatus = KEY_NOT_AVAILABLE; 
    }
    if (partialPlanView instanceof TemporalExtentView) {
      if (partialPlan.getToken( nodeKey) != null) {
        return KEY_FOUND;  //valid and available
      }
    } else if (partialPlanView instanceof TokenNetworkView) {
      if ((partialPlan.getToken( nodeKey) != null) ||
          (partialPlan.getRuleInstance( nodeKey) != null)) {
        return KEY_FOUND;
      }
    } else if (partialPlanView instanceof TimelineView) {
      if ((partialPlan.getToken( nodeKey) != null) ||
          (partialPlan.getSlot( nodeKey) != null) ||
          (partialPlan.getTimeline( nodeKey) != null)) {
        return KEY_FOUND;
      }
    } else if (partialPlanView instanceof ConstraintNetworkView) {
      if ((partialPlan.getToken( nodeKey) != null) ||
          (partialPlan.getVariable( nodeKey) != null) ||
          (partialPlan.getConstraint( nodeKey) != null) ||
          (partialPlan.getRuleInstance( nodeKey) != null) ||
          (partialPlan.getObject(nodeKey) != null)) {
        return KEY_FOUND;
      }
    } else if (partialPlanView instanceof ResourceProfileView) {
      if (partialPlan.getResource( nodeKey) != null) {
        return KEY_FOUND; 
      }
    } else if (partialPlanView instanceof ResourceTransactionView) {
      if ((partialPlan.getResource( nodeKey) != null) ||
          (partialPlan.getResourceTransaction( nodeKey) != null)) {
        return KEY_FOUND;  
      }
    } else if (partialPlanView instanceof DecisionView) {
      Iterator decisionItr = ((DecisionView) partialPlanView).getDecisionList().iterator();
      while (decisionItr.hasNext()) {
        PwEntity decision = (PwDecision) decisionItr.next();
        if (decision.getId().equals( nodeKey)) {
          entity = decision;
          return KEY_FOUND; 
        }
      }
    } else if (partialPlanView instanceof NavigatorView) {
      if (partialPlan.getEntity( nodeKey) != null) {
        return KEY_FOUND;
      }
    } else {
      System.err.println( "AskNodeByKey.isNodeKeyValid: " + partialPlanView +
                          " not handled");
      System.exit( -1);
    }
    return keyStatus;
  } // end isNodeKeyValid



} // end class AskNodeByKey

