//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: MaxLengthBox.java,v 1.1 2004-08-21 00:32:00 taylor Exp $
//
package gov.nasa.arc.planworks.viz.util;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.viewMgr.contentSpecWindow.partialPlan.ContentSpecElement;

/**
 * <code>MaxLengthBox</code> -
 *            JPanel->MaxLengthBox
 *            ContentSpecElement->MaxLengthBox
 * @author <a href="mailto:miatauro@email.arc.nasa.gov">Michael Iatauro</a>
 * A box for specifying time intervals.  This does not inherit from the SpecBox
 * class because it has special input concerns.
 */

public class MaxLengthBox extends JPanel implements ContentSpecElement {

  private static final Pattern valuePattern = Pattern.compile("\\d+");

  private JTextField maxLength;

  public MaxLengthBox() {

    setBackground( ViewConstants.VIEW_BACKGROUND_COLOR);

    GridBagLayout gridBag = new GridBagLayout();
    GridBagConstraints c = new GridBagConstraints();
    setLayout(gridBag);
    c.weightx = 0.5;
    c.gridy = 0;

    JLabel label1 = new JLabel( "maximum path length  ");
    c.gridx++;
    gridBag.setConstraints(label1, c);
    add(label1);

    maxLength = new JTextField(5);
    c.gridx++;
    gridBag.setConstraints(maxLength, c);
    add(maxLength);
    
   }
  /**
   * Gets the start and end time.
   * @return <code>List</code> containing the value of the MaxLengthBox.
   */
  public List getValue() throws IllegalArgumentException {
    ArrayList retval = new ArrayList();
    if(maxLength.getText().trim().equals("")) {
      retval.add( new Integer( Integer.MAX_VALUE));
      return retval;
    }
    if(!valuePattern.matcher(maxLength.getText().trim()).matches()) {
      JOptionPane.showMessageDialog( PlanWorks.getPlanWorks(),
                                     "Invalid maximum length format, must be only digits: '" +
                                     maxLength.getText().trim() + "'",
                                    "Error!", JOptionPane.ERROR_MESSAGE);
      throw new IllegalArgumentException();
    }
    retval.add( new Integer( maxLength.getText().trim()));
    return retval;
  }
  /**
   * Resets the values input by the user.
   */
  public void reset() {
    maxLength.setText("");
  }

  public JTextField getMaxLength() {
    return maxLength;
  }



} // end class MaxLengthBox
