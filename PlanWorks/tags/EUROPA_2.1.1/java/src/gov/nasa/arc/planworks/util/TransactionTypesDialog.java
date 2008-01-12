// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: TransactionTypesDialog.java,v 1.3 2006-06-30 22:40:55 meboyce Exp $
//
package gov.nasa.arc.planworks.util;


import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent; 
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import gov.nasa.arc.planworks.PlanWorks;


/**
 * <code>TransactionTypesDialog</code> - have user specify which transaction types the
 *                             PartialPlanWriter should log
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *            NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class TransactionTypesDialog extends JDialog {

  private static final int DIALOG_WIDTH = 450;
  private static final int DIALOG_HEIGHT = 400;

  private int[] transTypeStates;
  private List checkBoxList;
  private final JOptionPane optionPane;
  private String btnString1;
  private String btnString2;

  /**
   * <code>TransactionTypesDialog</code> - constructor 
   *
   * @param planWorks - <code>PlanWorks</code> - 
   * @param transactionTypes - <code>String[]</code> - 
   * @param transactionTypeStates - <code>int[]</code> - 
   */
  public TransactionTypesDialog( final PlanWorks planWorks, final String[] transactionTypes,
                                 final int[] transactionTypeStates) {
    // modal dialog - blocks other activity
    super( planWorks, true);
    setTitle( "Transaction Types to Log");
    transTypeStates = new int[ transactionTypeStates.length ];

    btnString1 = "OK";
    btnString2 = "Cancel";
    // Object[] options = {btnString1, btnString2};
    Object[] options = {btnString1};
    JPanel dialogPanel = new JPanel();
    dialogPanel.setLayout( new GridLayout( transactionTypeStates.length, 1));

    checkBoxList = new ArrayList();
    for (int i = 0, n = transactionTypes.length; i < n; i++) {
      final JCheckBox transTypeCheckBox =
        new JCheckBox( transactionTypes[i], (transactionTypeStates[i] == 1) ? true : false);
      final int indx = i;
      transTypeCheckBox.addItemListener( new ItemListener() {
          public void itemStateChanged( ItemEvent evt) {
            if (evt.getStateChange() == evt.SELECTED) {
              transTypeCheckBox.setSelected( true);
            } else {
              transTypeCheckBox.setSelected( false);
            }
          }
        });

      checkBoxList.add( transTypeCheckBox);
      dialogPanel.add( transTypeCheckBox);
    }

    optionPane = new JOptionPane
      ( new JScrollPane( dialogPanel), JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION,
        null, options, options[0]);
    setContentPane( optionPane);
    setDefaultCloseOperation( DO_NOTHING_ON_CLOSE);
    addWindowListener( new WindowAdapter() {
        public void windowClosing(WindowEvent we) {
          optionPane.setValue( new Integer( JOptionPane.CLOSED_OPTION));
        }
      });

    addInputListener();

    // size dialog appropriately
    pack();
    // place it in center of JFrame
    Utilities.setPopupLocation( this, PlanWorks.getPlanWorks());
    setSize( DIALOG_WIDTH, DIALOG_HEIGHT);
    Utilities.setPopupLocation( this, PlanWorks.getPlanWorks());
    setVisible( true);
  } // end constructor


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
              for (int i = 0, n = checkBoxList.size(); i < n; i++) {
                if (((JCheckBox) checkBoxList.get( i)).isSelected()) {
                  transTypeStates[i] = 1;
                } else {
                  transTypeStates[i] = 0;
                }
              }
              // we're done; dismiss the dialog
              setVisible( false);

            } else { // user closed dialog or clicked cancel
              transTypeStates = null;
              setVisible( false);
            }
          }
        }
      });
  } // end addInputListener

  /**
   * <code>getTransactionTypeStates</code>
   *
   * @return - <code>int[]</code> - 
   */
  public int[] getTransactionTypeStates() {
    return transTypeStates;
  }

} // end class TransactionTypesDialog
