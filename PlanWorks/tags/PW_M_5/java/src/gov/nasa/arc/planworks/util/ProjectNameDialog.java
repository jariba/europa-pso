// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: ProjectNameDialog.java,v 1.3 2003-07-12 01:36:33 taylor Exp $
//
package gov.nasa.arc.planworks.util;

import java.awt.Point;
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


/**
 * <code>ProjectNameDialog</code> - create JOptionPane for user to enter project name
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
                    NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class ProjectNameDialog extends JDialog {

  private String typedText = null;
  private JOptionPane optionPane;
  private JTextField textField;
  private String btnString1;
  private String btnString2;

  /**
   * <code>ProjectNameDialog</code> - constructor 
   *
   * @param planWorks - <code>PlanWorks</code> - 
   */
  public ProjectNameDialog ( PlanWorks planWorks) {
    // modal dialog - blocks other activity
    super( planWorks, true);
    setTitle( "Create Project");
    final String msgString1 = "name (string)";
    textField = new JTextField( 30);
    Object[] array = {msgString1, textField};
    btnString1 = "Enter";
    btnString2 = "Cancel";
    Object[] options = {btnString1, btnString2};
    // current value
    if (planWorks.getCurrentProjectName().equals( "")) {
      textField.setText( System.getProperty( "default.project.name"));
    } else {
      textField.setText( planWorks.getCurrentProjectName());
    }
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
        public void actionPerformed( ActionEvent e) {
          optionPane.setValue( btnString1);
        }
      });

    addInputListener();

    // size dialog appropriately
    pack();
    // place it in center of JFrame
    Point planWorksLocation = planWorks.getLocation();
    setLocation( (int) (planWorksLocation.getX() +
                        planWorks.getSize().getWidth() / 2 -
                        this.getPreferredSize().getWidth() / 2),
                 (int) (planWorksLocation.getY() +
                        planWorks.getSize().getHeight() / 2 -
                        this.getPreferredSize().getHeight() / 2));
    setBackground( ColorMap.getColor( "gray60"));
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
              typedText = textField.getText();
              // we're done; dismiss the dialog
              setVisible( false);
            } else { // user closed dialog or clicked cancel
              typedText = null;
              setVisible( false);
            }
          }
        }
      });
  } // end addInputListener

    /**
     * <code>getTypedText</code> - get user entered project url
     *
     * @return - <code>String</code> - 
     */
  public String getTypedText() {
    return typedText;
  }

} // end class ProjectNameDialog
