// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: AskTransactionObjectKey.java,v 1.1 2003-10-28 18:01:24 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 24oct03
//

package gov.nasa.arc.planworks.viz;

import java.util.Iterator;
import java.util.List;
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
import gov.nasa.arc.planworks.db.PwTransaction;
import gov.nasa.arc.planworks.util.Utilities;
import gov.nasa.arc.planworks.viz.ViewConstants;


/**
 * <code>AskTransactionObjectKey</code> - custom dialog to allow user to enter
 *           a value for a transaction object key, and check that it exists in 
 *           the list of transactions for the particular partial plan
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *           NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class AskTransactionObjectKey extends JDialog { 

  private List transactionList;
  private Integer objectKey;
  private int transactionListIndex;

  private String typedText = null;
  private JOptionPane optionPane;
  private JTextField textField;
  private String btnString1;
  private String btnString2;

  /**
   * <code>AskTransactionObjectKey</code> - constructor 
   *
   * @param partialPlan - <code>PwPartialPlan</code> - 
   */
  public AskTransactionObjectKey( List transactionList, String dialogTitle,
                                  String textFieldLabel) {
    // modal dialog - blocks other activity
    super( PlanWorks.planWorks, true);
    this.transactionList = transactionList;
    transactionListIndex = -1;

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
    Utilities.setPopUpLocation( this, PlanWorks.planWorks);
    setVisible( true);
  } // end constructor


  /**
   * <code>getObjectKey</code>
   *
   * @return objectKey - <code>Integer</code> - 
   */
  public Integer getObjectKey() {
    return objectKey;
  }

  /**
   * <code>getTransactionListIndex</code>
   *
   * @return - <code>int</code> - 
   */
  public int getTransactionListIndex() {
    return transactionListIndex;
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
                objectKey = new Integer( Integer.parseInt( typedText));
                // System.err.println( "AskTransactionObjectKey key " + typedText);
                if (! isValidObjectKey( objectKey)) {
                  JOptionPane.showMessageDialog
                    (PlanWorks.planWorks,
                     "Sorry, \"" + objectKey.toString() + "\" " + "isn't a valid object key.",
                     "Invalid object key", JOptionPane.ERROR_MESSAGE);
                } else {
                  // we're done; dismiss the dialog
                  setVisible( false);
                }
              } catch ( NumberFormatException except) {
                // text was invalid
                textField.selectAll();
                JOptionPane.showMessageDialog
                  (PlanWorks.planWorks,
                   "Sorry, \"" + typedText + "\" " + "isn't a valid response.\n" +
                   "Please enter a intger number",
                   "Invalid value for key", JOptionPane.ERROR_MESSAGE);
                objectKey = null; transactionListIndex = -1;
              }
            } else { // user closed dialog or clicked cancel
              objectKey = null; transactionListIndex = -1;
              setVisible( false);
            }
          }
        }
      });
  } // end addInputListener


  private boolean isValidObjectKey( Integer objectKey) {
    boolean isValid = false;
    int indx = 0;
    Iterator transItr = transactionList.iterator();
    while (transItr.hasNext()) {
      if (((PwTransaction) transItr.next()).getObjectId().equals( objectKey)) {
        transactionListIndex = indx;
        return true;
      }
      indx++;
    }
    return isValid;
  } // end isValidObjectKey


} // end class AskTransactionObjectKey

