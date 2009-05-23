// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: AskQueryTwoEntityKeysClasses.java,v 1.2 2004-10-07 20:19:14 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 16aug04
//

package gov.nasa.arc.planworks.viz.util;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.PwEntity;
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.mdi.MDIInternalFrame;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.partialPlan.FindEntityPathAdapter;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanView;
import gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork.ConstraintNetworkView;
import gov.nasa.arc.planworks.viz.partialPlan.navigator.NavigatorView;
import gov.nasa.arc.planworks.viz.partialPlan.tokenNetwork.TokenNetworkView;

/**
 * <code>AskQueryTwoEntityKeysClasses</code> - custom panel to allow user to enter two
 *           entity keys, check that they exist, and select which classes can be
 *           included in the path
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *           NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class AskQueryTwoEntityKeysClasses extends JPanel { 

  private static final int WINDOW_WIDTH = 300;
  private static final int WINDOW_HEIGHT= 200;
  
  private MDIInternalFrame twoEntityKeysWindow;
  private PwPartialPlan partialPlan;
  private PartialPlanView partialPlanView;
  private EntityKeysBox entityKeysBox;
  private ClassGroupBox classGroupBox;
  private MaxLengthBox maxLengthBox;
  private JButton findPathButton;
  private JButton pathExistsButton;
  private JButton cancelButton;

  private List pathClasses;


  public AskQueryTwoEntityKeysClasses( final MDIInternalFrame twoEntityKeysWindow,
                                       final PwPartialPlan partialPlan,
                                       final PartialPlanView partialPlanView) {
    this.twoEntityKeysWindow = twoEntityKeysWindow;
    this.partialPlan = partialPlan;
    this.partialPlanView = partialPlanView;
    pathClasses = new ArrayList();

    setBackground( ViewConstants.VIEW_BACKGROUND_COLOR);

    GridBagLayout gridBag = new GridBagLayout();
    GridBagConstraints c = new GridBagConstraints();
    setLayout(gridBag);
    
    c.weightx = 0;
    c.weighty = 0;
    c.gridx = 0;
    c.gridy = 0;

    entityKeysBox = new EntityKeysBox( partialPlan, partialPlanView);
    c.gridy++;
    gridBag.setConstraints( entityKeysBox, c);
    add( entityKeysBox);

    JLabel findPathLabel = new JLabel( "through");
    c.gridy++;
    gridBag.setConstraints( findPathLabel, c);
    add( findPathLabel);

    Map classNames = getClassNames( partialPlanView);
    classGroupBox = new ClassGroupBox( twoEntityKeysWindow, classNames);
    c.gridy++;
    gridBag.setConstraints( classGroupBox, c);
    add( classGroupBox);

    maxLengthBox = new MaxLengthBox();
    c.gridy++;
    gridBag.setConstraints( maxLengthBox, c);
    add( maxLengthBox);

    GridBagLayout buttonGridBag = new GridBagLayout();
    GridBagConstraints buttonConstraints = new GridBagConstraints();
    JPanel buttonPanel = new JPanel(buttonGridBag);

    buttonConstraints.weightx = 0;
    buttonConstraints.weighty = 0;
    buttonConstraints.gridx = 0;
    buttonConstraints.gridy = 0;

    findPathButton = new JButton("Find Path");
    findPathButton.addActionListener(new FindButtonListener( partialPlanView, this));
    buttonGridBag.setConstraints(findPathButton, buttonConstraints);
    buttonPanel.add(findPathButton);

    pathExistsButton = new JButton("Does Path Exist");
    pathExistsButton.addActionListener(new FindButtonListener( partialPlanView, this));
    buttonConstraints.gridx++;
    buttonGridBag.setConstraints(pathExistsButton, buttonConstraints);
    buttonPanel.add(pathExistsButton);
    
    cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(new FindButtonListener( partialPlanView, this));
    buttonConstraints.gridx++;
    buttonGridBag.setConstraints(cancelButton, buttonConstraints);
    buttonPanel.add(cancelButton);
    
    //c.gridx++;
    c.gridy++;
    gridBag.setConstraints(buttonPanel, c);
    add(buttonPanel);
    // findPathButton.doClick();

    twoEntityKeysWindow.setLocation
      ( (int) ((PlanWorks.getPlanWorks().getSize().getWidth() / 2) - (WINDOW_WIDTH / 2)),
        (int) ((PlanWorks.getPlanWorks().getSize().getHeight() / 2) - (WINDOW_HEIGHT / 2)));
  } // end constructor

  public final MDIInternalFrame getWindowFrame() {
    return twoEntityKeysWindow;
  }

  public final EntityKeysBox getEntityKeysBox() {
    return entityKeysBox;
  }

  public final JButton getFindPathButton() {
    return findPathButton;
  }

  public final JButton getPathExistsButton() {
    return pathExistsButton;
  }

  class FindButtonListener implements ActionListener {

    private AskQueryTwoEntityKeysClasses keysClassesWindow;
    private PartialPlanView partialPlanView;

    public FindButtonListener( PartialPlanView partialPlanView,
                              AskQueryTwoEntityKeysClasses keysClassesWindow) {
      this.partialPlanView = partialPlanView;
      this.keysClassesWindow = keysClassesWindow;
    }

    public void actionPerformed(ActionEvent ae) {
      boolean doPathExists = false;
      if (ae.getActionCommand().equals("Find Path")) {
        doPathExists = false;
      } else if (ae.getActionCommand().equals("Does Path Exist")) {
        doPathExists = true;
      } else if (ae.getActionCommand().equals("Cancel")) {
        keysClassesWindow.getWindowFrame().dispose();
        return;
      }
      List keys = null, classes = null;
      List classObjects = new ArrayList();
      int maxPathLength = 0;
      try {
        keys = keysClassesWindow.entityKeysBox.getValue();
//           for (int i = 0, n = keys.size(); i < n; i++) {
//             System.err.println( "keys i=" + i + " " + (Integer) keys.get( i));
//           }
        classes = keysClassesWindow.classGroupBox.getValues();
        for (int i = 0, n = classes.size(); i < n; i++) {
          Object object = (Object) classes.get( i);
          if (object instanceof String) {
            // System.err.println( "classes i=" + i + " " + (String) classes.get( i));
          } else if (object instanceof Class) {
            classObjects.add( (Class) object);
            // System.err.println( "classes i=" + i + " " + ((Class) classes.get( i)).getName());
          }
        }
        if (classObjects.size() == 0) {
          // All Classes was selected
          classObjects.addAll( getClassNames( partialPlanView).values());
        }
        classObjects = validateClassObjects( classObjects, (Integer) keys.get( 0));
        classObjects = validateClassObjects( classObjects, (Integer) keys.get( 1));

//         for (int i = 0, n = classObjects.size(); i < n; i++) {
//           Object object = (Object) classObjects.get( i);
//           if (object instanceof Class) {
//             System.err.println( "2 classObjects i=" + i + " " +
//                                 ((Class) classObjects.get( i)).getName());
//           }
//         }

        maxPathLength = ((Integer) keysClassesWindow.maxLengthBox.getValue().get( 0)).intValue();
        if (maxPathLength != Integer.MAX_VALUE) {
          maxPathLength++;
        }

      } catch(IllegalArgumentException e){
        return;
      }

      if (partialPlanView instanceof FindEntityPathAdapter) {
        ((FindEntityPathAdapter) partialPlanView).invokeFindEntityPathClasses
          ( (Integer) keys.get( 0), (Integer) keys.get( 1), classObjects, doPathExists,
            maxPathLength, keysClassesWindow.getWindowFrame());
      } else {
        System.err.println( "FindButtonListener: partialPlanView " + partialPlanView +
                            " not handled");
      }
    } // end actionPerformed

  } // end class FindButtonListener

  private List validateClassObjects( List classes, Integer key)
    throws IllegalArgumentException {
    PwEntity entity = partialPlan.getEntity( key);
    boolean foundClass = false;
    for(Iterator it = classes.iterator(); it.hasNext();) {
      Class temp = (Class) it.next();
      if((foundClass = temp.isInstance( entity))) {
        break;
      }
    }
    if (! foundClass) {
//       String className = entity.getClass().getName();
//       int indx = className.lastIndexOf( ".Pw");
//       JOptionPane.showMessageDialog( PlanWorks.getPlanWorks(),
//                                      "Valid class list must contain end type '" +
//                                      className.substring( indx + 3, className.length() - 4) +
//                                      "'", "Error!", JOptionPane.ERROR_MESSAGE);
//       throw new IllegalArgumentException();
      Class [] interfaceClasses = entity.getClass().getInterfaces();
      String implClassName = entity.getClass().getName();
      int indx = implClassName.lastIndexOf( ".Pw");
      String matchString = implClassName.substring( indx + 3, implClassName.length() - 4);
      Class interfaceClass = null;
      for (int i = 0, n = interfaceClasses.length; i < n; i++) {
        interfaceClass = (Class) interfaceClasses[i];
        if (interfaceClass.getName().indexOf( matchString) >= 0) {
          System.err.println( "validateClassObjects: added " + interfaceClass.getName());
          classes.add( interfaceClass);
          break;
        }
      }
    }
    return classes;
  } // end validateClassObjects

  private Map getClassNames( PartialPlanView partialPlanView) {
    Map classNamesMap = new HashMap();
    Iterator classNameItr = null;
    if (partialPlanView instanceof ConstraintNetworkView) {
          classNameItr = ViewConstants.CONSTRAINT_NETWORK_VIEW_ENTITY_CLASSES.iterator();
    } else if (partialPlanView instanceof NavigatorView) {
          classNameItr = ViewConstants.NAVIGATOR_VIEW_ENTITY_CLASSES.iterator();
    } else if (partialPlanView instanceof TokenNetworkView) {
          classNameItr = ViewConstants.TOKEN_NETWORK_VIEW_ENTITY_CLASSES.iterator();
    } else {
      System.err.println( "getClassNames: partialPlanView " + partialPlanView +
                          " not handled");
    }
    while (classNameItr.hasNext()) {
      Class entityClass = (Class) classNameItr.next();
      String className = entityClass.getName();
      int indx = className.lastIndexOf( ".Pw");
      classNamesMap.put( className.substring( indx + 3), entityClass);
    }
    return classNamesMap;
  } // end getClassNames

  

} // end class AskQueryTwoEntityKeysClasses

   
