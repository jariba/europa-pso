// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: AskQueryEntityKey.java,v 1.1 2004-08-07 01:18:30 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 24oct03
//

package gov.nasa.arc.planworks.viz.util;

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
import gov.nasa.arc.planworks.db.PwTokenQuery;
import gov.nasa.arc.planworks.db.PwVariableQuery;
import gov.nasa.arc.planworks.db.PwDBTransaction;
import gov.nasa.arc.planworks.util.Utilities;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.VizView;
import gov.nasa.arc.planworks.viz.partialPlan.dbTransaction.DBTransactionView;
import gov.nasa.arc.planworks.viz.sequence.sequenceQuery.DBTransactionQueryView;
import gov.nasa.arc.planworks.viz.sequence.sequenceQuery.StepQueryView;
import gov.nasa.arc.planworks.viz.sequence.sequenceQuery.TokenQueryView;
import gov.nasa.arc.planworks.viz.sequence.sequenceQuery.VariableQueryView;


/**
 * <code>AskQueryEntityKey</code> - custom dialog to allow user to enter
 *           a value for a query results entity key, and check that it exists in 
 *           the list of all entitys
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *           NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class AskQueryEntityKey extends JDialog { 

  private List entityList;
  private VizView queryView;
  private Integer entityKey;
  private int entityListIndex;
  private String keyType;

  private String typedText = null;
  private JOptionPane optionPane;
  private JTextField textField;
  private String btnString1;
  private String btnString2;

  /**
   * <code>AskQueryEntityKey</code> - constructor 
   *
   * @param entityList - <code>List</code> - 
   * @param dialogTitle - <code>String</code> - 
   * @param textFieldLabel - <code>String</code> - 
   * @param queryView - <code>VizView</code> - 
   */
  public AskQueryEntityKey( final List entityList, final String dialogTitle,
                            final String textFieldLabel, final VizView queryView) {
    // modal dialog - blocks other activity
    super( PlanWorks.getPlanWorks(), true);
    this.entityList = entityList;
    this.queryView = queryView;
    entityListIndex = -1;

    if ((queryView instanceof StepQueryView) ||
        (queryView instanceof DBTransactionQueryView) ||
        (queryView instanceof DBTransactionView)) {
      keyType = "entity";
    } else if (queryView instanceof TokenQueryView) {
      keyType = "token";
    } else if (queryView instanceof VariableQueryView) {
      keyType = "variable";
    }
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
        public final void windowClosing( final WindowEvent we) {
          /*
           * Instead of directly closing the window,
           * we're going to change the JOptionPane's
           * value property.
           */
          optionPane.setValue( new Integer( JOptionPane.CLOSED_OPTION));
        }
      });

    textField.addActionListener( new ActionListener() {
        public final void actionPerformed( final ActionEvent e) {
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
   * <code>getEntityKey</code>
   *
   * @return entityKey - <code>Integer</code> - 
   */
  public final Integer getEntityKey() {
    return entityKey;
  }

  /**
   * <code>getEntityListIndex</code>
   *
   * @return - <code>int</code> - 
   */
  public final int getEntityListIndex() {
    return entityListIndex;
  }

  private void addInputListener() {
    optionPane.addPropertyChangeListener( new PropertyChangeListener() {
        public final void propertyChange( final PropertyChangeEvent e) {
          String prop = e.getPropertyName();
          if (isVisible() && (e.getSource() == optionPane) &&
              (prop.equals( JOptionPane.VALUE_PROPERTY) ||
               prop.equals( JOptionPane.INPUT_VALUE_PROPERTY))) {
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
                entityKey = new Integer( Integer.parseInt( typedText));
                // System.err.println( "AskQueryEntityKey key " + typedText);
                if (! isValidEntityKey( entityKey)) {
                  JOptionPane.showMessageDialog
                    ( PlanWorks.getPlanWorks(),
                     "Sorry, \"" + entityKey.toString() + "\" " +
                     "is not a valid " + keyType + " key.",
                     "Invalid " + keyType + " key", JOptionPane.ERROR_MESSAGE);
                } else {
                  // we're done; dismiss the dialog
                  setVisible( false);
                }
              } catch ( NumberFormatException except) {
                // text was invalid
                textField.selectAll();
                JOptionPane.showMessageDialog
                  ( PlanWorks.getPlanWorks(),
                   "Sorry, \"" + typedText + "\" " + "isn't a valid response.\n" +
                   "Please enter a intger number",
                   "Invalid value for key", JOptionPane.ERROR_MESSAGE);
                entityKey = null; entityListIndex = -1;
              }
            } else { // user closed dialog or clicked cancel
              entityKey = null; entityListIndex = -1;
              setVisible( false);
            }
          }
        }
      });
  } // end addInputListener


  private boolean isValidEntityKey( final Integer entityKey) {
    boolean isValid = false;
    int indx = 0;
    Iterator objItr = entityList.iterator();
    if ((queryView instanceof StepQueryView) ||
        (queryView instanceof DBTransactionQueryView) ||
        (queryView instanceof DBTransactionView)) {
      while (objItr.hasNext()) {
        if (((PwDBTransaction) objItr.next()).getEntityId().equals( entityKey)) {
          entityListIndex = indx;
          return true;
        }
        indx++;
      }
      return isValid;
    } else if (queryView instanceof TokenQueryView) {
      while (objItr.hasNext()) {
        if (((PwTokenQuery) objItr.next()).getId().equals( entityKey)) {
          entityListIndex = indx;
          return true;
        }
        indx++;
      }
      return isValid;
    } else if (queryView instanceof VariableQueryView) {
      while (objItr.hasNext()) {
        if (((PwVariableQuery) objItr.next()).getId().equals( entityKey)) {
          entityListIndex = indx;
          return true;
        }
        indx++;
      }
      return isValid;
    } else {
      System.err.println( "AskQueryEntityKey.isValidEntityKey not handled");
      System.exit( -1);
    }
    return false;
  } // end isValidEntityKey


} // end class AskQueryEntityKey

   
