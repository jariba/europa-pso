// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: RescaleProfileDialog.java,v 1.2 2004-09-15 22:26:49 taylor Exp $
//
package gov.nasa.arc.planworks.viz.partialPlan.resourceProfile;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.beans.PropertyChangeEvent; 
import java.beans.PropertyChangeListener;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.PwResource;
import gov.nasa.arc.planworks.util.ColorMap;
import gov.nasa.arc.planworks.util.Utilities;


/**
 * <code>RescaleProfileDialog</code> - create JOptionPane for user to enter
 *                                     rescaling values for Resource Profile
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
                    NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class RescaleProfileDialog extends JDialog {

  private static final int DIMENSION_FIELD_WIDTH = 8;

  private JOptionPane optionPane;
  private double maxY;
  private JTextField maxYField;
  private double minY;
  private JTextField minYField;
  private double maxX;
  private JTextField maxXField;
  private double minX;
  private JTextField minXField;

  private String okButton;
  private String resetButton;
  private String cancelButton;

  private double levelMin;
  private double levelMax;
  private double timeMax;
  private double timeMin;
 
  /**
   * <code>RescaleProfileDialog</code> - constructor 
   *
   * @param planWorks - <code>PlanWorks</code> - 
   * @param resource - <code>PwResource</code> - 
   * @param resourceProfileView - <code>ResourceProfileView</code> - 
   * @param initialTimeScaleEnd - <code>double</code> - 
   * @param unaryResource - <code>PwResource</code> - 
   * @param profileScalingList - <code>List</code> - 
   */
  public RescaleProfileDialog ( final PlanWorks planWorks, final PwResource resource,
                                final ResourceProfileView resourceProfileView,
                                final double initialTimeScaleEnd,
                                final PwResource unaryResource,
                                final List profileScalingList) {
    // modal dialog - blocks other activity
    super( planWorks, true);
    setTitle( "Rescale '" + resource.getName() + "' Resource Profile");

    double minMax [] = ResourceProfile.getResourceMinMax( resource);
    levelMin = minMax[0];
    levelMax = minMax[1];
    timeMin = (double) resource.getHorizonStart();
    // latestEndTime = resource.getHorizonEnd();
    // latestEndTime may be PLUS_INFINITY in planner model
    timeMax = initialTimeScaleEnd;
    final JLabel maxYLabel = new JLabel( "maximum Y value (<= " + levelMax + ")");
    maxYField = new JTextField( DIMENSION_FIELD_WIDTH);
    final JLabel minYLabel = new JLabel( "minimum Y value (>= " + levelMin + ")");
    minYField = new JTextField( DIMENSION_FIELD_WIDTH);
    final JLabel maxXLabel = new JLabel( "maximum X value (<= " + timeMax + ")");
    maxXField = new JTextField( DIMENSION_FIELD_WIDTH);
    final JLabel minXLabel = new JLabel( "minimum X value (>= " + timeMin + ")");
    minXField = new JTextField( DIMENSION_FIELD_WIDTH);

    // current values
    if ( unaryResource == null) {
      maxYField .setText( String.valueOf( levelMax));
      minYField .setText( String.valueOf( levelMin));
      maxXField .setText( String.valueOf( timeMax));
      minXField .setText( String.valueOf( timeMin));
    } else {
      maxYField .setText( ((Double) profileScalingList.get( 0)).toString());
      minYField .setText( ((Double) profileScalingList.get( 1)).toString());
      maxXField .setText( ((Double) profileScalingList.get( 2)).toString());
      minXField .setText( ((Double) profileScalingList.get( 3)).toString());
    }
    okButton = "OK";
    resetButton = "Reset";
    cancelButton = "Cancel";
    Object[] okResetOptions = new Object[2];
    Object[] okResetCancelOptions = new Object[3];
    if ( unaryResource == null) {
      okResetOptions[0] = okButton;
      okResetOptions[1] = cancelButton;
    } else {
      okResetCancelOptions[0] = okButton;
      okResetCancelOptions[1] = resetButton;
      okResetCancelOptions[2] = cancelButton;
    }

    JPanel dialogPanel = new JPanel();
    GridBagLayout gridBag = new GridBagLayout();
    GridBagConstraints c = new GridBagConstraints();
    dialogPanel.setLayout( gridBag);
    
    c.weightx = 0;
    c.weighty = 0;
    c.gridx = 0;
    c.gridy = 0;

    c.gridy++;
    gridBag.setConstraints( maxYLabel, c);
    dialogPanel.add( maxYLabel);
    c.gridy++;
    gridBag.setConstraints( maxYField, c);
    dialogPanel.add( maxYField);

    c.gridy++;
    gridBag.setConstraints( minYLabel, c);
    dialogPanel.add( minYLabel);
    c.gridy++;
    gridBag.setConstraints( minYField, c);
    dialogPanel.add( minYField);

    c.gridy++;
    gridBag.setConstraints( maxXLabel, c);
    dialogPanel.add( maxXLabel);
    c.gridy++;
    gridBag.setConstraints( maxXField, c);
    dialogPanel.add( maxXField);

    c.gridy++;
    gridBag.setConstraints( minXLabel, c);
    dialogPanel.add( minXLabel);
    c.gridy++;
    gridBag.setConstraints( minXField, c);
    dialogPanel.add( minXField);

    if ( unaryResource == null) {
      optionPane = new JOptionPane
        ( dialogPanel, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION,
          null, okResetOptions, okResetOptions[0]);
    } else {
      optionPane = new JOptionPane
        ( dialogPanel, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION,
          null, okResetCancelOptions, okResetCancelOptions[0]);
    }
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
    // place it in center of JFrame
    Utilities.setPopupLocation( this, PlanWorks.getPlanWorks());
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

            if (value.equals( okButton)) {
              if (handleTextFieldValues()) {
                return;
              }
              // we're done; dismiss the dialog
              setVisible( false);
            } else if (value.equals( resetButton)) {
              maxYField.setText( String.valueOf( levelMax));
              minYField.setText( String.valueOf( levelMin));
              maxXField.setText( String.valueOf( timeMax));
              minXField.setText( String.valueOf( timeMin));
              if (handleTextFieldValues()) {
                return;
              }
              // we're done; dismiss the dialog
              setVisible( false);
            } else { // user closed dialog or clicked cancel
              maxY = -1.0d; minY = -1.0d;
              maxX = -1.0d; minX = -1.0d;
              setVisible( false);
            }
          }
        }
      });
  } // end addInputListener

  private boolean handleTextFieldValues() {
    boolean haveSeenError = false;
    double maxYTemp = 0.0d, minYTemp = 0.0d, maxXTemp = 0.0d, minXTemp = 0.0d;
    try {
      maxYTemp = (new Double( maxYField.getText().trim())).doubleValue();
    } catch (NumberFormatException excep) {
      showFormatErrorDialog( excep);
      haveSeenError = true;
    }
    try {
      minYTemp = (new Double( minYField.getText().trim())).doubleValue();
    } catch (NumberFormatException excep) {
      showFormatErrorDialog( excep);
      haveSeenError = true;
    }
    try {
      maxXTemp = (new Double( maxXField.getText().trim())).doubleValue();
    } catch (NumberFormatException excep) {
      showFormatErrorDialog( excep);
      haveSeenError = true;
    }
    try {
      minXTemp = (new Double( minXField.getText().trim())).doubleValue();
    } catch (NumberFormatException excep) {
      showFormatErrorDialog( excep);
      haveSeenError = true;
    }

    if (maxY != maxYTemp) {
      if ((maxYTemp > levelMax) || (maxYTemp <= levelMin)) {
        showRangeErrorDialog( "maximum Y value", maxYTemp, levelMin, levelMax);
        haveSeenError = true;
      } else {
        maxY = maxYTemp;
      }
    }
    if (minY != minYTemp) {
      if ((minYTemp < levelMin) || (minYTemp >= levelMax)) {
        showRangeErrorDialog( "minimum Y value", minYTemp, levelMin, levelMax);
        haveSeenError = true;
      } else {
        minY = minYTemp;
      }
    }
    if (maxX != maxXTemp) {
      if ((maxXTemp > timeMax) || (maxXTemp <= timeMin)) {
        showRangeErrorDialog( "maximum X value", maxXTemp, timeMin, timeMax);
        haveSeenError = true;
      } else {
        maxX = maxXTemp;
      }
    }
    if (minX != minXTemp) {
      if ((minXTemp < timeMin) || (minXTemp >= timeMax)) {
        showRangeErrorDialog( "minimum X value", minXTemp, timeMin, timeMax);
        haveSeenError = true;
      } else {
        minX = minXTemp;
      }
    }

    if (minY >= maxY) {
      JOptionPane.showMessageDialog
        ( PlanWorks.getPlanWorks(),
          "minimum Y value (" + minY + ") is >= maximum Y value(" + maxY + ")",
          "Invalid Values", JOptionPane.ERROR_MESSAGE);
      haveSeenError = true;
    }
    if (minX >= maxX) {
      JOptionPane.showMessageDialog
        ( PlanWorks.getPlanWorks(),
          "minimum X value (" + minX + ") is >= maximum X value(" + maxX + ")",
          "Invalid Values", JOptionPane.ERROR_MESSAGE);
      haveSeenError = true;
    }
    return haveSeenError;
  } // end handleTextFieldValues

  private void showRangeErrorDialog( String valueName, double value, double minValue,
                                     double maxValue) {
    JOptionPane.showMessageDialog
      ( PlanWorks.getPlanWorks(),
        valueName + ": " + value + " not > " + minValue + " and <= " + maxValue,
        "Invalid Value", JOptionPane.ERROR_MESSAGE);
  } // end showErrorDialog

  private void showFormatErrorDialog( NumberFormatException excep) {
    JOptionPane.showMessageDialog
      ( PlanWorks.getPlanWorks(), excep.getMessage(),
        "Invalid Input", JOptionPane.ERROR_MESSAGE);
  } // end showFormatErrorDialog

  /**
   * <code>getMaxY</code>
   *
   * @return - <code>Double</code> - 
   */
  public Double getMaxY() {
    return new Double( maxY);
  }

  /**
   * <code>getMinY</code>
   *
   * @return - <code>Double</code> - 
   */
  public Double getMinY() {
    return new Double( minY);
  }

  /**
   * <code>getMaxX</code>
   *
   * @return - <code>Double</code> - 
   */
  public Double getMaxX() {
    return new Double( maxX);
  }

  /**
   * <code>getMinX</code>
   *
   * @return - <code>Double</code> - 
   */
  public Double getMinX() {
    return new Double( minX);
  }


} // end class RescaleProfileDialog

 
