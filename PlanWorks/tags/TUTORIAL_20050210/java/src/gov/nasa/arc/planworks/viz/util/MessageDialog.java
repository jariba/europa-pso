// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: MessageDialog.java,v 1.1 2004-08-06 20:05:29 taylor Exp $
//
package gov.nasa.arc.planworks.viz.util;

import java.awt.Point;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent; 
import java.beans.PropertyChangeListener;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.util.ColorMap;
import gov.nasa.arc.planworks.util.Utilities;


/**
 * <code>MessageDialog</code> - create non-modal message dialog
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
                    NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class MessageDialog extends JDialog {

  private static final int DIALOG_WIDTH = 300;
  private static final int DIALOG_HEIGHT = 200;

  private JOptionPane optionPane;
  private String btnString1;
  private JTextArea messageText;

  /**
   * <code>MessageDialog</code> - constructor 
   *
   * @param planWorks - <code>PlanWorks</code> - 
   */
  public MessageDialog ( final PlanWorks planWorks, final String title, final String message) {
    // non-modal dialog - does not blocks other activity
    super( planWorks, false);
    setTitle( title);
    messageText = new JTextArea( message);
    messageText.setLineWrap( true);
    messageText.setWrapStyleWord( true);
    messageText.setEditable( false);
    messageText.setBackground( this.getBackground());

    Object[] array = {new JScrollPane( messageText)};
    btnString1 = "OK";
    Object[] options = {btnString1};
    optionPane = new JOptionPane
      ( array, JOptionPane.INFORMATION_MESSAGE, JOptionPane.OK_OPTION, null, options, options[0]);
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

    addInputListener();

    // size dialog appropriately
    pack();
    setSize( DIALOG_WIDTH, DIALOG_HEIGHT);
    // place it in center of JFrame
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
	    // System.err.println( "value " + value.toString());
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
              // we're done; dismiss the dialog
              setVisible( false);
            } else { // user closed dialog or clicked cancel
              setVisible( false);
            }
          }
        }
      });
  } // end addInputListener


} // end class MessageDialog
