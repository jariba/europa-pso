// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: AskNodeByKey.java,v 1.5 2004-05-21 21:39:05 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 07oct03
//

package gov.nasa.arc.planworks.viz.partialPlan;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent; 
import java.beans.PropertyChangeListener; 
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.util.Utilities;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork.ConstraintNetworkView;
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

  private PartialPlanView partialPlanView;
  private Integer nodeKey;

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
                if (! isNodeKeyValid( nodeKey)) {
                  JOptionPane.showMessageDialog
                    (PlanWorks.getPlanWorks(),
                     "Sorry, \"" + nodeKey.toString() + "\" " + "is not a valid key.",
                     "Invalid Key", JOptionPane.ERROR_MESSAGE);
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

  private boolean isNodeKeyValid( Integer nodeKey) {
    boolean isValid = false;
    PwPartialPlan partialPlan = partialPlanView.getPartialPlan();
    if ((partialPlanView instanceof TemporalExtentView) ||
        (partialPlanView instanceof TokenNetworkView)) {
      if (partialPlan.getToken( nodeKey) != null) {
        return true;
      }
    } else if (partialPlanView instanceof TimelineView) {
      if ((partialPlan.getToken( nodeKey) != null) ||
          (partialPlan.getSlot( nodeKey) != null)) {
         return true;
      }
    } else if (partialPlanView instanceof ConstraintNetworkView) {
      if ((partialPlan.getToken( nodeKey) != null) ||
          (partialPlan.getVariable( nodeKey) != null) ||
          (partialPlan.getConstraint( nodeKey) != null) ||
          (partialPlan.getObject(nodeKey) != null)) {
        return true;
      }
    } else if (partialPlanView instanceof ResourceProfileView) {
      if (partialPlan.getResource( nodeKey) != null) {
        return true;
      }
    } else if (partialPlanView instanceof ResourceTransactionView) {
      if ((partialPlan.getResource( nodeKey) != null) ||
          (partialPlan.getResourceTransaction( nodeKey) != null)) {
        return true;
      }
    } else {
      System.err.println( "AskNodeByKey.isNodeKeyValid: " + partialPlanView +
                          " not handled");
      System.exit( -1);
    }
    return isValid;
  } // end isNodeKeyValid



} // end class AskNodeByKey

