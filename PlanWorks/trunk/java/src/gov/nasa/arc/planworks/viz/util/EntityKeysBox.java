//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: EntityKeysBox.java,v 1.2 2004-08-23 22:07:41 taylor Exp $
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
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanView;
import gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork.ConstraintNetworkView;
import gov.nasa.arc.planworks.viz.partialPlan.navigator.NavigatorView;
import gov.nasa.arc.planworks.viz.partialPlan.tokenNetwork.TokenNetworkView;
import gov.nasa.arc.planworks.viz.viewMgr.contentSpecWindow.partialPlan.ContentSpecElement;

/**
 * <code>EntityKeysBox</code> -
 *            JPanel->EntityKeysBox
 *            ContentSpecElement->EntityKeysBox
 * @author <a href="mailto:miatauro@email.arc.nasa.gov">Michael Iatauro</a>
 * A box for specifying time intervals.  This does not inherit from the SpecBox
 * class because it has special input concerns.
 */

public class EntityKeysBox extends JPanel implements ContentSpecElement {

  private static final Pattern valuePattern = Pattern.compile("\\d+");

  private JTextField startValue, endValue;
  private PwPartialPlan partialPlan;
  private PartialPlanView partialPlanView;

  public EntityKeysBox( PwPartialPlan partialPlan, PartialPlanView partialPlanView) {
    this.partialPlan = partialPlan;
    this.partialPlanView = partialPlanView;

    setBackground( ViewConstants.VIEW_BACKGROUND_COLOR);

    GridBagLayout gridBag = new GridBagLayout();
    GridBagConstraints c = new GridBagConstraints();
    setLayout(gridBag);
    c.weightx = 0.5;
    c.gridy = 0;

    JLabel label1 = new JLabel("  Start Key  ");
    c.gridx++;
    gridBag.setConstraints(label1, c);
    add(label1);

    startValue = new JTextField(5);
    c.gridx++;
    gridBag.setConstraints(startValue, c);
    add(startValue);
    
    JLabel endLabel = new JLabel("  End Key  ");
    c.gridx++;
    gridBag.setConstraints(endLabel, c);
    add(endLabel);
    
    endValue = new JTextField(5);
    c.gridx++;
    gridBag.setConstraints(endValue, c);
    add(endValue);

   }
  /**
   * Gets the start and end time.
   * @return <code>List</code> containing the value of the EntityKeysBox.
   */
  public List getValue() throws IllegalArgumentException {
    ArrayList retval = new ArrayList();
    if(startValue.getText().trim().equals("") || endValue.getText().trim().equals("")) {
      JOptionPane.showMessageDialog( PlanWorks.getPlanWorks(),
                                     "Both start and end keys must be filled in.",
                                    "Error!", JOptionPane.ERROR_MESSAGE);
      throw new IllegalArgumentException();
    }
    if(!valuePattern.matcher(startValue.getText().trim()).matches()) {
      JOptionPane.showMessageDialog( PlanWorks.getPlanWorks(),
                                     "Invalid start key format, must be only digits: '" +
                                     startValue.getText().trim() + "'",
                                    "Error!", JOptionPane.ERROR_MESSAGE);
      throw new IllegalArgumentException();
    }
    if(!valuePattern.matcher(endValue.getText().trim()).matches()) {
      JOptionPane.showMessageDialog( PlanWorks.getPlanWorks(),
                                     "Invalid end key format, must be only digits: '" +
                                     endValue.getText().trim() + "'",
                                    "Error!", JOptionPane.ERROR_MESSAGE);
      throw new IllegalArgumentException();
    }
    Integer startKey = new Integer(startValue.getText().trim());
    Integer endKey = new Integer(endValue.getText().trim());

    validateViewKey( startKey, partialPlanView);
    validateViewKey( endKey, partialPlanView);

    retval.add(startKey);
    retval.add(endKey);
    return retval;
  }
  /**
   * Resets the values input by the user.
   */
  public void reset() {
    startValue.setText("");
    endValue.setText("");
  }

  public JTextField getStartValue() {
    return startValue;
  }
  public JTextField getEndValue() {
    return endValue;
  }

  private void validateViewKey( Integer entityKey, PartialPlanView partialPlanView)
    throws IllegalArgumentException {
    boolean isKeyValid = false;
    if (partialPlanView instanceof ConstraintNetworkView) {
      if ((partialPlan.getToken( entityKey) != null) ||
          (partialPlan.getVariable( entityKey) != null) ||
          (partialPlan.getConstraint( entityKey) != null) ||
          (partialPlan.getRuleInstance( entityKey) != null) ||
          (partialPlan.getObject(entityKey) != null)) {
        isKeyValid = true;
      }
    } else if (partialPlanView instanceof NavigatorView) {
      if (partialPlan.getEntity( entityKey) != null) {
        isKeyValid = true;
      }
    } else if (partialPlanView instanceof TokenNetworkView) {
      if ((partialPlan.getToken( entityKey) != null) ||
          (partialPlan.getRuleInstance( entityKey) != null)) {
        isKeyValid = true;
      }
    } else {
      System.err.println( "EntityKeysBox.getValue: partialPlanView " + partialPlanView +
                          " not handled");
    }
    if (! isKeyValid ) {
      JOptionPane.showMessageDialog( PlanWorks.getPlanWorks(),
                                     entityKey.toString() + " is not a valid key",
                                     "Error!", JOptionPane.ERROR_MESSAGE);
      throw new IllegalArgumentException();
    }
  } // end validateViewKey


} // end class EntityKeysBox
