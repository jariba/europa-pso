// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: AskTokenByKey.java,v 1.1 2003-10-08 19:10:28 taylor Exp $
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
import gov.nasa.arc.planworks.viz.ViewConstants;


/**
 * <code>AskTokenByKey</code> - custom dialog to allow user to enter
 *           a value for a token key
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *           NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class AskTokenByKey extends JDialog { 

  private PwPartialPlan partialPlan;
  private String typedText = null;
  private Integer tokenKey;
  private JOptionPane optionPane;
  private JTextField textField;
  private String btnString1;
  private String btnString2;

  /**
   * <code>AskTokenByKey</code> - constructor 
   *
   * @param partialPlan - <code>PwPartialPlan</code> - 
   */
  public AskTokenByKey( PwPartialPlan partialPlan) {
    // model dialog - blocks other activity
    super( PlanWorks.planWorks, true);
    this.partialPlan = partialPlan;
    setTitle( "Find Token by Key");

    final String msgString1 = "key (int)";
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
    setLocation( (int) (PlanWorks.planWorks.getLocation().getX() +
                        (PlanWorks.planWorks.getSize().getWidth() / 2) -
                        (this.getSize().getWidth() / 2)),
                 (int) (PlanWorks.planWorks.getLocation().getY() +
                        (PlanWorks.planWorks.getSize().getHeight() / 2) -
                        (this.getSize().getHeight() / 2)));
    setVisible( true);
  } // end constructor


  /**
   * <code>getTokenKey</code>
   *
   * @return tokenKey - <code>Integer</code> - 
   */
  public Integer getTokenKey() {
    return tokenKey;
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
                tokenKey = new Integer( Integer.parseInt( typedText));
                // System.err.println( "key " + typedText);
                if (partialPlan.getToken( tokenKey) == null) {
                  JOptionPane.showMessageDialog
                    (PlanWorks.planWorks,
                     "Sorry, \"" + tokenKey.toString() + "\" " + "isn't a valid token key.",
                     "Invalid token key", JOptionPane.ERROR_MESSAGE);
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
                tokenKey = null;
              }
            } else { // user closed dialog or clicked cancel
              tokenKey = null;
              setVisible( false);
            }
          }
        }
      });
  } // end addInputListener

} // end class AskTokenByKey

