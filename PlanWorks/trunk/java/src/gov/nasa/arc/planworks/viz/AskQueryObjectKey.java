// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: AskQueryObjectKey.java,v 1.1 2003-12-20 01:54:49 taylor Exp $
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

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoView;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.PwTokenQuery;
import gov.nasa.arc.planworks.db.PwVariableQuery;
import gov.nasa.arc.planworks.db.PwTransaction;
import gov.nasa.arc.planworks.util.Utilities;
import gov.nasa.arc.planworks.viz.ViewConstants;


/**
 * <code>AskQueryObjectKey</code> - custom dialog to allow user to enter
 *           a value for a query results object key, and check that it exists in 
 *           the list of all objects
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *           NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class AskQueryObjectKey extends JDialog { 

  private List objectList;
  private JGoView headerView;
  private Integer objectKey;
  private int objectListIndex;
  private String keyType;

  private String typedText = null;
  private JOptionPane optionPane;
  private JTextField textField;
  private String btnString1;
  private String btnString2;

  public AskQueryObjectKey( List objectList, String dialogTitle, String textFieldLabel,
                            JGoView headerView) {
    // modal dialog - blocks other activity
    super( PlanWorks.planWorks, true);
    this.objectList = objectList;
    this.headerView = headerView;
    objectListIndex = -1;

    if ((headerView instanceof StepHeaderView) ||
        (headerView instanceof TransactionHeaderView)) {
      keyType = "object";
    } else if (headerView instanceof TokenQueryHeaderView) {
      keyType = "token";
    } else if (headerView instanceof VariableQueryHeaderView) {
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
   * <code>getObjectListIndex</code>
   *
   * @return - <code>int</code> - 
   */
  public int getObjectListIndex() {
    return objectListIndex;
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
                // System.err.println( "AskQueryObjectKey key " + typedText);
                if (! isValidObjectKey( objectKey)) {
                  JOptionPane.showMessageDialog
                    (PlanWorks.planWorks,
                     "Sorry, \"" + objectKey.toString() + "\" " +
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
                  (PlanWorks.planWorks,
                   "Sorry, \"" + typedText + "\" " + "isn't a valid response.\n" +
                   "Please enter a intger number",
                   "Invalid value for key", JOptionPane.ERROR_MESSAGE);
                objectKey = null; objectListIndex = -1;
              }
            } else { // user closed dialog or clicked cancel
              objectKey = null; objectListIndex = -1;
              setVisible( false);
            }
          }
        }
      });
  } // end addInputListener


  private boolean isValidObjectKey( Integer objectKey) {
    boolean isValid = false;
    int indx = 0;
    Iterator objItr = objectList.iterator();
    if ((headerView instanceof StepHeaderView) ||
        (headerView instanceof TransactionHeaderView)) {
      while (objItr.hasNext()) {
        if (((PwTransaction) objItr.next()).getObjectId().equals( objectKey)) {
          objectListIndex = indx;
          return true;
        }
        indx++;
      }
      return isValid;
    } else if (headerView instanceof TokenQueryHeaderView) {
      while (objItr.hasNext()) {
        if (((PwTokenQuery) objItr.next()).getId().equals( objectKey)) {
          objectListIndex = indx;
          return true;
        }
        indx++;
      }
      return isValid;
    } else if (headerView instanceof VariableQueryHeaderView) {
      while (objItr.hasNext()) {
        if (((PwVariableQuery) objItr.next()).getId().equals( objectKey)) {
          objectListIndex = indx;
          return true;
        }
        indx++;
      }
      return isValid;
    } else {
      System.err.println( "AskQueryObjectKey.isValidObjectKey not handled");
      System.exit( -1);
    }
    return false;
  } // end isValidObjectKey


} // end class AskQueryObjectKey

   
