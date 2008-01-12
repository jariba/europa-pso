// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: AskQueryTwoEntityKeys.java,v 1.4 2004-08-21 00:31:58 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 04aug04
//

package gov.nasa.arc.planworks.viz.util;

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
import gov.nasa.arc.planworks.viz.VizView;


/**
 * <code>AskQueryTwoEntityKeys</code> - custom dialog to allow user to enter two
 *           entity keys, and check that they exist
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *           NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class AskQueryTwoEntityKeys extends JDialog { 

  private PwPartialPlan partialPlan;
  private VizView queryView;
  private Integer entityKey1;
  private Integer entityKey2;
  private String keyType1;
  private String keyType2;

  private String typedText1 = null;
  private String typedText2 = null;
  private JOptionPane optionPane;
  private JTextField textField1;
  private JTextField textField2;
  private String btnString1;
  private String btnString2;

  public AskQueryTwoEntityKeys( final String dialogTitle, final String keyType1,
				final String textFieldLabel1, final String keyType2,
				final String textFieldLabel2, 
				final PwPartialPlan partialPlan) {
    // modal dialog - blocks other activity
    super( PlanWorks.getPlanWorks(), true);
    this.partialPlan = partialPlan;
    this.keyType1 = keyType1;
    this.keyType2 = keyType2;
    setTitle( dialogTitle);
    final String msgString1 = textFieldLabel1;
    textField1 = new JTextField(10);
    final String msgString2 = textFieldLabel2;
    textField2 = new JTextField(10);
    Object[] array = {msgString1, textField1, msgString2, textField2};

    btnString1 = "Enter";
    btnString2 = "Cancel";
    Object[] options = {btnString1, btnString2};

    // current value
    textField1.setText( "");
    textField2.setText( "");
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

    textField1.addActionListener( new ActionListener() {
        public final void actionPerformed( final ActionEvent e) {
          optionPane.setValue( btnString1);
        }
      });
    textField2.addActionListener( new ActionListener() {
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
   * <code>getEntityKey1</code>
   *
   * @return entityKey1 - <code>Integer</code> - 
   */
  public final Integer getEntityKey1() {
    return entityKey1;
  }

  /**
   * <code>getEntityKey2</code>
   *
   * @return entityKey2 - <code>Integer</code> - 
   */
  public final Integer getEntityKey2() {
    return entityKey2;
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
              typedText1 = textField1.getText();
              typedText2 = textField2.getText();
              try {
                entityKey1 = new Integer( Integer.parseInt( typedText1));
              } catch ( NumberFormatException except) {
                // text was invalid
                textField1.selectAll();
                JOptionPane.showMessageDialog
                  ( PlanWorks.getPlanWorks(),
                   "Sorry, \"" + typedText1 + "\" " + "isn't a valid response.\n" +
                   "Please enter a integer number",
                   "Invalid value for key", JOptionPane.ERROR_MESSAGE);
                entityKey1 = null;
              }
              try {
                entityKey2 = new Integer( Integer.parseInt( typedText2));
              } catch ( NumberFormatException except) {
                // text was invalid
                textField2.selectAll();
                JOptionPane.showMessageDialog
                  ( PlanWorks.getPlanWorks(),
                   "Sorry, \"" + typedText2 + "\" " + "isn't a valid response.\n" +
                   "Please enter a integer number",
                   "Invalid value for key", JOptionPane.ERROR_MESSAGE);
                entityKey2 = null;
              }
	      // System.err.println( "AskQueryTwoEntityKeys key1 " + typedText1 +
	      //			  " key2 " + typedText2);
	      if (! isValidEntityKey( entityKey1, keyType1)) {
		JOptionPane.showMessageDialog
		  ( PlanWorks.getPlanWorks(),
		    "Sorry, \"" + entityKey1.toString() + "\" " +
		    "is not a valid " + keyType1 + " key.",
		    "Invalid " + keyType1 + " key", JOptionPane.ERROR_MESSAGE);
                entityKey1 = null;
	      }
	      if (! isValidEntityKey( entityKey2, keyType2)) {
		JOptionPane.showMessageDialog
		  ( PlanWorks.getPlanWorks(),
		    "Sorry, \"" + entityKey2.toString() + "\" " +
		    "is not a valid " + keyType2 + " key.",
		    "Invalid " + keyType2 + " key", JOptionPane.ERROR_MESSAGE);
                entityKey2 = null;
	      }
	      if ((entityKey1 != null) && (entityKey2 != null)) {
		// we're done; dismiss the dialog
		setVisible( false);
	      }
            } else { // user closed dialog or clicked cancel
              entityKey1 = null;
              entityKey2 = null;
              setVisible( false);
            }
          }
        }
      });
  } // end addInputListener


  private boolean isValidEntityKey( final Integer entityKey, final String keyType) {
    if (keyType.equals( "variable")) {
      return (partialPlan.getVariable( entityKey) != null);
    } else if (keyType.equals( "token")) {
      return (partialPlan.getToken( entityKey) != null);
      // NavigatorView does not render resource transactions
    } else if (keyType.equals( "entity")) {
      return ((partialPlan.getEntity( entityKey) != null) &&
	      (partialPlan.getResourceTransaction( entityKey) == null));
    } else {
      System.err.println( "AskQueryTwoEntityKeys.isValidEntityKey: keyType " + keyType +
			  " not handled");
      System.exit( -1);
      return true;
    }
  } // end isValidEntityKey


} // end class AskQueryTwoEntityKeys

   
