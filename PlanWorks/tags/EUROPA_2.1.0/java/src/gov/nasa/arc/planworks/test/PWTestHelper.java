//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: PWTestHelper.java,v 1.19 2005-11-10 01:22:10 miatauro Exp $
//
package gov.nasa.arc.planworks.test;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Container;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.JTree;

import junit.extensions.jfcunit.JFCTestHelper;
import junit.extensions.jfcunit.eventdata.JComboBoxMouseEventData;
import junit.extensions.jfcunit.eventdata.JTreeMouseEventData;
import junit.extensions.jfcunit.eventdata.KeyEventData;
import junit.extensions.jfcunit.eventdata.MouseEventData;
import junit.extensions.jfcunit.eventdata.StringEventData;
import junit.extensions.jfcunit.finder.AbstractButtonFinder;
import junit.extensions.jfcunit.finder.ComponentFinder;
import junit.extensions.jfcunit.finder.DialogFinder;
import junit.extensions.jfcunit.finder.Finder;
import junit.extensions.jfcunit.finder.NamedComponentFinder;
import junit.extensions.jfcunit.finder.JFileChooserFinder;
import junit.extensions.jfcunit.finder.JMenuItemFinder;
import junit.framework.Assert;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoView;

import gov.nasa.arc.planworks.ConfigureAndPlugins;
import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.SequenceViewMenuItem;
import gov.nasa.arc.planworks.mdi.MDIDesktopPane;
import gov.nasa.arc.planworks.mdi.MDIInternalFrame;
import gov.nasa.arc.planworks.util.DirectoryChooser;
import gov.nasa.arc.planworks.util.Utilities;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.ViewGenerics;
import gov.nasa.arc.planworks.viz.ViewListener;
import gov.nasa.arc.planworks.viz.VizView;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanView;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanViewMenu;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanViewMenuItem;
import gov.nasa.arc.planworks.viz.partialPlan.ResourceView;
import gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork.ConstraintNetworkView;
//import gov.nasa.arc.planworks.viz.partialPlan.dbTransaction.DBTransactionView;    
import gov.nasa.arc.planworks.viz.partialPlan.decision.DecisionView;
import gov.nasa.arc.planworks.viz.partialPlan.resourceProfile.ResourceProfileView;
import gov.nasa.arc.planworks.viz.partialPlan.resourceTransaction.ResourceTransactionView;
import gov.nasa.arc.planworks.viz.partialPlan.rule.RuleInstanceView;
import gov.nasa.arc.planworks.viz.partialPlan.temporalExtent.TemporalExtentView;
import gov.nasa.arc.planworks.viz.partialPlan.timeline.TimelineView;
import gov.nasa.arc.planworks.viz.partialPlan.tokenNetwork.TokenNetworkView;
import gov.nasa.arc.planworks.viz.sequence.sequenceSteps.SequenceStepsView;
import gov.nasa.arc.planworks.viz.sequence.sequenceSteps.StepElement;
import gov.nasa.arc.planworks.viz.util.AskQueryTwoEntityKeysClasses;
import gov.nasa.arc.planworks.viz.util.EntityKeysBox;
import gov.nasa.arc.planworks.viz.viewMgr.contentSpecWindow.partialPlan.ContentSpecWindow;
//import gov.nasa.arc.planworks.viz.viewMgr.contentSpecWindow.sequence.SequenceQueryWindow;
import gov.nasa.arc.planworks.viz.viewMgr.ViewSet;


public abstract class PWTestHelper {

  public static final String GUI_TEST_DIR = "GUITest";
  public static final String SEQUENCE_NAME = "sequence";
  public static final String PROJECT1 = "testProject1";
  public static final String PROJECT2 = "testProject2";


  /**
   * <code>findMenu</code>
   *
   * @param name - <code>String</code> - 
   * @return - <code>JMenu</code> - 
   */
  public static JMenu findMenu(String name) {
    AbstractButtonFinder finder = new AbstractButtonFinder(name);
    return (JMenu) finder.find();
  }

  /**
   * <code>findMenuItem</code> - works only for static menus of any number of
   *                             items, or for dynamic menus of 1 item
   *
   * @param menuName - <code>String</code> - 
   * @param itemName - <code>String</code> - 
   * @param helper - <code>JFCTestHelper</code> - 
   * @return - <code>JMenuItem</code> - 
   */
  public static JMenuItem findMenuItem(String menuName, String itemName,
                                       JFCTestHelper helper, PlanWorksGUITest guiTest)
  throws Exception {
    JMenu parent = findMenu(menuName);
    // System.err.println( "findMenuItem parent " + parent);
    Assert.assertTrue(parent != null);
    if(parent == null) {
      return null;
    }
    helper.enterClickAndLeave(new MouseEventData( guiTest, parent));

    AbstractButtonFinder finder = new AbstractButtonFinder(itemName);
    // finder.setDebug( true);
    JMenuItem menuItem = (JMenuItem) finder.find();
    // System.err.println( "findMenuItem menuItem " + menuItem);
    return menuItem;
  }

  /**
   * <code>findDynamicMenuItem</code> - 
   *
   * @param menuName - <code>String</code> - 
   * @param itemName - <code>String</code> - 
   * @param helper - <code>JFCTestHelper</code> - 
   * @param guiTest - <code>PlanWorksGUITest</code> - 
   * @return - <code>JMenuItem</code> - 
   * @exception Exception if an error occurs
   */
  public static JMenuItem findDynamicMenuItem(String menuName, String itemName,
                                              JFCTestHelper helper,
                                              PlanWorksGUITest guiTest)
    throws Exception {
    JMenu parent = findMenu(menuName);
    // System.err.println( "findDynamicMenuItem parent " + parent);
    Assert.assertTrue(parent != null);
    if(parent == null) {
      return null;
    }
    helper.enterClickAndLeave(new MouseEventData( guiTest, parent));
    //     System.err.println( "findDynamicMenuItem clicked on parent itemName '" +
    //                         itemName + "'");
    for (int i = 0, n = parent.getMenuComponentCount(); i < n; i++) {
      Object item = parent.getMenuComponent( i);
      if (item != null) { // check for separators
        if (item instanceof JMenuItem) {
          JMenuItem menuItem = (JMenuItem) item;
          //       System.err.println( "  menu component i " + i + " " + menuItem.getText());
          if (menuItem.getText().equals( itemName)) {
            //         System.err.println( "  menuItem match " + menuItem.getText());
            return menuItem;
          }
        }
      }
    }
    return null;
  } // end findDynamicMenuItem

  /**
   * <code>findButton</code>
   *
   * @param name - <code>String</code> - 
   * @return - <code>AbstractButton</code> - 
   */
  public static AbstractButton findButton( String name) {
    AbstractButtonFinder finder = new AbstractButtonFinder( name);
    return (AbstractButton) finder.find();
  }

  /**
   * <code>findComponentByClass</code>
   *
   * @param componentClass - <code>Class</code> - 
   * @return - <code>Component</code> - 
   */
  public static Component findComponentByClass( Class componentClass) {
    ComponentFinder finder = new ComponentFinder( componentClass);
    // returns first one found
    return (Component) finder.find();
  }

  /**
   * <code>findComponentByName</code> - matches on getName() of component
   *
   * @param componentClass - <code>Class</code> - 
   * @param compLabel - <code>String</code> - 
   * @param matchOperation - <code>int</code> - Finder.OP_MATCH (regexp), OP_STARTSWITH,
   *                                            OP_ENDSWITH, OP_EQUALS, OP_CONTAINS
   * @return - <code>Component</code> - 
   */
  public static Component findComponentByName( Class componentClass, String compLabel,
                                               int matchOperation) {
    NamedComponentFinder finder =
      new NamedComponentFinder( componentClass, compLabel);
    finder.setOperation( matchOperation);
    // returns first one found that matches with operation
    return (Component) finder.find();
  }

  /**
   * <code>createProject</code>
   *
   * @param projectName - <code>String</code> - 
   * @param sequenceDirectory - <code>String</code> - 
   * @param helper - <code>JFCTestHelper</code> - 
   * @param guiTest - <code>PlanWorksGUITest</code> - 
   * @param planWorks - <code>PlanWorks</code> - 
   * @exception Exception if an error occurs
   */
  public static void createProject( String projectName, String sequenceDirectory,
                                    JFCTestHelper helper, PlanWorksGUITest guiTest,
                                    PlanWorks planWorks)
    throws Exception {
    System.err.println( "createProject sequenceDirectory " + sequenceDirectory);
    List nameValueList = new ArrayList();
    nameValueList.add( ConfigureAndPlugins.PROJECT_WORKING_DIR);
    nameValueList.add( sequenceDirectory);
    ConfigureAndPlugins.updateProjectConfigMap( "default", nameValueList);

    JMenuItem createItem =
      PWTestHelper.findMenuItem( PlanWorks.PROJECT_MENU, PlanWorks.CREATE_MENU_ITEM,
                                 helper, guiTest);
    // System.err.println("Found create menu item: " + createItem);
    Assert.assertNotNull( "'Project->Create' not found:", createItem);
    helper.enterClickAndLeave( new MouseEventData( guiTest, createItem));
    guiTest.flushAWT(); guiTest.awtSleep();

    JTextField field = (JTextField) PWTestHelper.findComponentByClass( JTextField.class);
    Assert.assertNotNull( "Could not find \"name (string)\" field", field);
//     field.setText( null);
//     helper.sendString( new StringEventData( guiTest, field, projectName));
    field.setText( projectName);
    helper.sendKeyAction( new KeyEventData( guiTest, field, KeyEvent.VK_ENTER));
    System.err.println( "'Project->Create' " + projectName);
    guiTest.flushAWT(); guiTest.awtSleep();
  } // end createProject

  /**
   * <code>addSequencesToProject</code>
   *
   * @param sequenceFileArray - <code>File[]</code> - 
   * @param helper - <code>JFCTestHelper</code> - 
   * @param guiTest - <code>PlanWorksGUITest</code> - 
   * @param planWorks - <code>PlanWorks</code> - 
   * @exception Exception if an error occurs
   */
  public static void addSequencesToProject( File [] sequenceFileArray, JFCTestHelper helper,
                                            PlanWorksGUITest guiTest, PlanWorks planWorks)
    throws Exception {
    
    JFileChooser fileChooser = null;
    // deprecated fileChooser = helper.getShowingJFileChooser( planWorks);
    fileChooser = (JFileChooser) new JFileChooserFinder( null).find( planWorks, 0);

    Assert.assertNotNull( "'Select Sequence Directory' dialog not found:", fileChooser);
    // Container projectSeqDialog = (Container) fileChooser;

    for (int i = 0, n = sequenceFileArray.length; i < n; i++) {
      System.err.println( "  i " + i + " sequenceFileArray " + sequenceFileArray[i].getName());
    }
    fileChooser.setSelectedFiles( sequenceFileArray);
    guiTest.flushAWT(); guiTest.awtSleep();

    JButton okButton = (JButton) PWTestHelper.findButton( "OK");
    Assert.assertNotNull("Could not find 'Select Sequence Directory' dialog \"OK\" button", okButton);
    System.err.println( "'Select Sequence Directory' dialog " + okButton.getText()); 
    helper.enterClickAndLeave( new MouseEventData( guiTest, okButton));
    guiTest.flushAWT(); guiTest.awtSleep();
  } // end addSequencesToProject

  /**
   * <code>openProject</code>
   *
   * @param projectName - <code>String</code> - 
   * @param helper - <code>JFCTestHelper</code> - 
   * @param guiTest - <code>PlanWorksGUITest</code> - 
   * @param planWorks - <code>PlanWorks</code> - 
   * @exception Exception if an error occurs
   */
  public static void openProject( String projectName, JFCTestHelper helper,
                                  PlanWorksGUITest guiTest, PlanWorks planWorks)
    throws Exception {
    JMenuItem openItem =
      PWTestHelper.findMenuItem( PlanWorks.PROJECT_MENU, PlanWorks.OPEN_MENU_ITEM,
                                 helper, guiTest);
    // System.err.println("Found open menu item: " + openItem);
    Assert.assertNotNull( "'Project->Open' not found:", openItem);
    Assert.assertTrue( "'Project->Open' should be enabled", (openItem.isEnabled() == true));
    helper.enterClickAndLeave( new MouseEventData( guiTest, openItem));
    guiTest.flushAWT(); guiTest.awtSleep();
    // try{Thread.sleep(2000);}catch(Exception e){}

    PWTestHelper.handleComboDialog( "Open Project", projectName, helper, guiTest);
  } // end openProject

  /**
   * <code>deleteProject</code>
   *
   * @param projectName - <code>String</code> - 
   * @param helper - <code>JFCTestHelper</code> - 
   * @param guiTest - <code>PlanWorksGUITest</code> - 
   * @exception Exception if an error occurs
   */
  public static void deleteProject( String projectName, JFCTestHelper helper,
                                    PlanWorksGUITest guiTest)
    throws Exception {
    JMenuItem deleteItem = 
      PWTestHelper.findMenuItem( PlanWorks.PROJECT_MENU, PlanWorks.DELETE_MENU_ITEM,
                                 helper, guiTest);
    // System.err.println("Found delete menu item: " + deleteItem);
    Assert.assertNotNull( "'Project->Delete' not found:", deleteItem);
    helper.enterClickAndLeave( new MouseEventData( guiTest, deleteItem));

//     JMenu projectMenu =  PWTestHelper.findMenu( PlanWorks.PROJECT_MENU);
//      Assert.assertNotNull( "'Project' menu not found:", projectMenu);
//     int [] menuElementIndices = new int [1];
//     menuElementIndices[0] = 0;
//     int numClicks = 1; boolean isPopupTrigger = true;
//     long sleepTime = 0l;
//     helper.enterClickAndLeave( new JMenuMouseEventData( guiTest, projectMenu,
//                                                         menuElementIndices,
//                                                         numClicks, MouseEvent.BUTTON1_MASK,
//                                                         isPopupTrigger, sleepTime));
    guiTest.flushAWT(); guiTest.awtSleep();
    PWTestHelper.handleComboDialog( "Delete Project", projectName, helper, guiTest);
  } // end deleteProject

  /**
   * <code>exitPlanWorks</code>
   *
   * @param helper - <code>JFCTestHelper</code> - 
   * @param guiTest - <code>PlanWorksGUITest</code> - 
   * @exception Exception if an error occurs
   */
  public static void exitPlanWorks( JFCTestHelper helper, PlanWorksGUITest guiTest)
    throws Exception {
    JMenuItem exitItem = 
      PWTestHelper.findMenuItem( PlanWorks.FILE_MENU, PlanWorks.EXIT_MENU_ITEM,
                                 helper, guiTest);
    // System.err.println("Found exit menu item: " + exitItem);
    Assert.assertNotNull( "'File->Exit' not found:", exitItem);
    helper.enterClickAndLeave( new MouseEventData( guiTest, exitItem));
    guiTest.flushAWT(); guiTest.awtSleep();
  } // end deleteProject

  /**
   * <code>deleteSequenceFromProject</code>
   *
   * @param sequenceUrl - <code>String</code> - 
   * @param helper - <code>JFCTestHelper</code> - 
   * @param guiTest - <code>PlanWorksGUITest</code> - 
   * @exception Exception if an error occurs
   */
  public static void deleteSequenceFromProject( String sequenceUrl, JFCTestHelper helper,
                                                PlanWorksGUITest guiTest)
    throws Exception {
    JMenuItem deleteItem = 
      PWTestHelper.findMenuItem( PlanWorks.PROJECT_MENU, PlanWorks.DELSEQ_MENU_ITEM,
                                 helper, guiTest);
    // System.err.println("Found delete menu item: " + deleteItem);
    Assert.assertNotNull( "'Project->Delete Sequence ...' not found:", deleteItem);
    helper.enterClickAndLeave( new MouseEventData( guiTest, deleteItem));
    guiTest.flushAWT(); guiTest.awtSleep();
    PWTestHelper.handleComboDialog( "Delete Sequence", sequenceUrl, helper, guiTest);
    guiTest.flushAWT(); guiTest.awtSleep();
  } // end deleteSequenceFromProject

  /**
   * <code>addPlanSequence</code>
   *
   * @param helper - <code>JFCTestHelper</code> - 
   * @param guiTest - <code>PlanWorksGUITest</code> - 
   * @param planWorks - <code>PlanWorks</code> - 
   * @exception Exception if an error occurs
   */
  public static void addPlanSequence( JFCTestHelper helper, PlanWorksGUITest guiTest,
                                      PlanWorks planWorks)
    throws Exception {
    JMenuItem addSeqItem =
      PWTestHelper.findMenuItem( PlanWorks.PROJECT_MENU, PlanWorks.ADDSEQ_MENU_ITEM,
                                 helper, guiTest);
    // System.err.println("Found addSeq menu item: " + addSeqItem);
    Assert.assertNotNull( "'Project->Add Sequence' not found:", addSeqItem);
    helper.enterClickAndLeave( new MouseEventData( guiTest, addSeqItem));
    guiTest.flushAWT(); guiTest.awtSleep();
  } // end addPlanSequence

  /**
   * <code>handleDialog</code>
   *
   * @param dialogName - <code>String</code> - 
   * @param buttonName - <code>String</code> - 
   * @param dialogMessage - <code>String</code> - 
   * @param helper - <code>JFCTestHelper</code> - 
   * @param guiTest - <code>PlanWorksGUITest</code> - 
   */
  public static void handleDialog( String dialogName, String buttonName, String dialogMessage,
                                   JFCTestHelper helper, PlanWorksGUITest guiTest)
    throws Exception {
    // deprecated List dialogs = helper.getShowingDialogs( dialogName);
    List dialogs = new DialogFinder( null).findAll( PlanWorks.getPlanWorks());

    // System.err.println( "dialogs size " + dialogs.size() + " " + dialogs);
    // assertEquals quietly exits ???
    // assertEquals(  dialogName + " dialog not found", 1, dialogs.size());
    Assert.assertTrue(  dialogName + " dialog not found", (1 == dialogs.size()));
    if (! dialogMessage.equals( "")) {
      JDialog dialog = (JDialog) dialogs.get( 0);
      String message = null;
      Container contentPane = dialog.getContentPane();
      for (int i = 0, n = contentPane.getComponentCount(); i < n; i++) {
        //       System.err.println( "dialog i " + i + " " +
        //                           contentPane.getComponent( i).getClass().getName());
        if (contentPane.getComponent(i) instanceof JOptionPane) {
          message = (String) ((JOptionPane) contentPane.getComponent(i)).getMessage();
          break;
        }
      }
      // System.err.println( "message '" + message + "' dialogMessage '" + dialogMessage + "'");
      String foundMsg = null;
      if (message.indexOf( dialogMessage) >= 0) {
        foundMsg = "found";
      }
      Assert.assertNotNull( "'" + dialogName + "' dialog does not contain '" + dialogMessage + "'",
                            foundMsg);
    }
    JButton button = (JButton) PWTestHelper.findButton( buttonName);
    System.err.println( "'" + dialogName + "' dialog " + button.getText());
    Assert.assertNotNull( "Could not find " + dialogName + " dialog \"" + buttonName +
                          "\" button", button);
    helper.enterClickAndLeave( new MouseEventData( guiTest, button));
    guiTest.flushAWT(); guiTest.awtSleep();
  } // end handleDialog

  /**
   * <code>handleComboDialog</code>
   *
   * @param dialogName - <code>String</code> - 
   * @param itemName - <code>String</code> - 
   * @param helper - <code>JFCTestHelper</code> - 
   * @param guiTest - <code>PlanWorksGUITest</code> - 
   * @exception Exception if an error occurs
   */
  public static void handleComboDialog( String dialogName, String itemName,
                                        JFCTestHelper helper, PlanWorksGUITest guiTest)
    throws Exception {
    // deprecated List dialogs = helper.getShowingDialogs( dialogName);
    List dialogs = new DialogFinder( null).findAll( PlanWorks.getPlanWorks());
    // System.err.println( "dialogs size " + dialogs.size() + " " + dialogs);
    // assertEquals(  dialogName + " dialog not found", 1, dialogs.size());
    Assert.assertTrue(  dialogName + " dialog not found", (1 == dialogs.size()));
    JDialog dialog = (JDialog) dialogs.get( 0);

    String selection = null;
    Container contentPane = dialog.getContentPane();
    String selectionValue = null;
    for (int i = 0, n = contentPane.getComponentCount(); i < n; i++) {
      // System.err.println( "dialog i " + i + " " +
      //                     contentPane.getComponent( i).getClass().getName());
      if (contentPane.getComponent(i) instanceof JOptionPane) {
        JOptionPane optionPane = (JOptionPane) contentPane.getComponent(i);
        Object[] selectionValues = (Object []) optionPane.getSelectionValues();
        for (int j = 0, m = selectionValues.length; j < m; j++) {
          // System.err.println( "value j " + j + " " + selectionValues[j].getClass().getName());
          String value = (String) selectionValues[j];
          if (value.equals( itemName)) {
            selectionValue = value;
            break;
          }
        }
        System.err.println( "handleComboDialog '" + dialogName + "' selected " +
                            selectionValue);
        optionPane.setInputValue( selectionValue);
        optionPane.setInitialSelectionValue( selectionValue);
        break;
      }
    }
    Assert.assertNotNull( itemName + " not found in combo dialog '" + dialogName + "'",
                          selectionValue);

    String buttonName = "OK";
    JButton button = (JButton) PWTestHelper.findButton( buttonName);
    System.err.println( "'" + dialogName + "' dialog " + button.getText());
    Assert.assertNotNull( "Could not find " + dialogName + " dialog \"" + buttonName +
                          "\" button", button);

    helper.enterClickAndLeave( new MouseEventData( guiTest, button));

    guiTest.flushAWT(); guiTest.awtSleep();
  } // end handleComboDialog

  public static void handleDialogValueEntry( String dialogName, String valueString,
                                             JFCTestHelper helper, PlanWorksGUITest guiTest) {
    JTextField field = (JTextField) PWTestHelper.findComponentByClass( JTextField.class);
    // System.err.println( "handleDialogValueEntry field " + field);
    Assert.assertNotNull( "Could not find entry field for '" + dialogName + "'", field);
    field.setText( null);
    helper.sendString( new StringEventData( guiTest, field, valueString));
    helper.sendKeyAction( new KeyEventData( guiTest, field, KeyEvent.VK_ENTER));
    System.err.println( dialogName + ": '" + valueString + "' entered");
    guiTest.flushAWT(); guiTest.awtSleep();
  } // end handleDialogValueEntry

  /**
   * <code>getPlanSequenceMenu</code>
   *
   * @return - <code>JMenu</code> - 
   * @exception Exception if an error occurs
   */
  public static JMenu getPlanSequenceMenu() throws Exception {
    JMenu  planSequenceMenu = PWTestHelper.findMenu( PlanWorks.PLANSEQ_MENU);
    Assert.assertNotNull("Failed to get \"Planning Sequence\" menu.", planSequenceMenu);
    Assert.assertTrue("Failed to get \"Planning Sequence\" menu.",
                      planSequenceMenu.getText().equals( "Planning Sequence"));
    // System.err.println( "planSequenceMenu " + planSequenceMenu);
    return planSequenceMenu;
  } // end getPlanSequenceMenu

  /**
   * <code>openSequenceStepsView</code>
   *
   * @param sequenceName - <code>String</code> - 
   * @param viewListener - <code>ViewListener</code> - 
   * @param helper - <code>JFCTestHelper</code> - 
   * @param guiTest - <code>PlanWorksGUITest</code> - 
   * @exception Exception if an error occurs
   */
  public static void openSequenceStepsView( String sequenceName, ViewListener viewListener,
                                            JFCTestHelper helper, PlanWorksGUITest guiTest)
    throws Exception {
    SequenceStepsView seqStepsView = null;
    System.err.println( "openSequenceStepsView sequenceName '" + sequenceName + "'");
    JMenuItem sequenceItem =
      PWTestHelper.findDynamicMenuItem( PlanWorks.PLANSEQ_MENU, sequenceName,
                                        helper, guiTest);
    // System.err.println( "getSequenceStepsView sequenceItem " + sequenceItem);
    Assert.assertNotNull( "'" + PlanWorks.PLANSEQ_MENU + "->" + sequenceName +
                          "' not found:", sequenceItem);
    ((SequenceViewMenuItem) sequenceItem).setViewListener( viewListener);
    helper.enterClickAndLeave( new MouseEventData( guiTest, sequenceItem));
    guiTest.flushAWT(); guiTest.awtSleep();
  } // end openSequenceStepsView

  /**
   * <code>getSequenceStepsView</code>
   *
   * @param sequenceName - <code>String</code> - 
   * @param helper - <code>JFCTestHelper</code> - 
   * @param guiTest - <code>PlanWorksGUITest</code> - 
   * @return - <code>SequenceStepsView</code> - 
   * @exception Exception if an error occurs
   */
  public static SequenceStepsView getSequenceStepsView( String sequenceName,
                                                        JFCTestHelper helper,
                                                        PlanWorksGUITest guiTest)
    throws Exception {
    String viewFindName = ViewConstants.SEQUENCE_STEPS_VIEW.replaceAll( " ", "") +
      " of " + sequenceName;
    System.err.println( "getSequenceStepsView viewFindName '" + viewFindName + "'");
    SequenceStepsView seqStepsView =
      (SequenceStepsView) PWTestHelper.findComponentByName( SequenceStepsView.class,
                                                            viewFindName,
                                                            Finder.OP_EQUALS);
    Assert.assertNotNull( viewFindName + " not found:", seqStepsView);
    System.err.println( "getSequenceStepsView found " + viewFindName);
    return seqStepsView;
  } // end getSequenceStepsView

  /**
   * <code>sequenceStepsViewStepSelection</code>
   *
   * @param seqStepsView - <code>SequenceStepsView</code> - 
   * @param stepNumber - <code>int</code> - 
   * @param viewMenuItemName - <code>String</code> - 
   */
  public static void seqStepsViewStepItemSelection( SequenceStepsView seqStepsView,
                                                    int stepNumber,
                                                    String viewMenuItemName,
                                                    List viewListenerList,
                                                    JFCTestHelper helper,
                                                    PlanWorksGUITest guiTest)
    throws Exception {
    // System.err.println( "seqStepsViewStepItemSelection " + seqStepsView.getName());
    StepElement stepElement =
      (StepElement) ((List) seqStepsView.getStepElementList().get( stepNumber)).get( 0);
    // 2nd arg to enterClickAndLeave must be of class Component
    // JGo objects are not
    // helper.enterClickAndLeave( new MouseEventData( this, stepElement, 1,
    //                                                MouseEvent.BUTTON3_MASK));
    stepElement.doMouseClickWithListener( MouseEvent.BUTTON3_MASK, stepElement.getLocation(),
                                          new Point( 0, 0), seqStepsView.getJGoView(),
                                          viewListenerList);
    guiTest.flushAWT(); guiTest.awtSleep();
    // try{Thread.sleep(2000);}catch(Exception e){}

    PWTestHelper.selectViewMenuItem( seqStepsView, viewMenuItemName, helper, guiTest);
  } // end sequenceStepsViewStepSelection

  /**
   * <code>seqStepsViewStepItemExists</code>
   *
   * @param seqStepsView - <code>SequenceStepsView</code> - 
   * @param stepNumber - <code>int</code> - 
   * @param viewMenuItemName - <code>String</code> - 
   * @param viewListenerList - <code>List</code> - 
   * @param helper - <code>JFCTestHelper</code> - 
   * @param guiTest - <code>PlanWorksGUITest</code> - 
   * @return - <code>boolean</code> - 
   * @exception Exception if an error occurs
   */
  public static boolean seqStepsViewStepItemExists( SequenceStepsView seqStepsView,
                                                    int stepNumber,
                                                    String viewMenuItemName,
                                                    List viewListenerList,
                                                    JFCTestHelper helper,
                                                    PlanWorksGUITest guiTest)
    throws Exception {
    // System.err.println( "seqStepsViewStepItemSelection " + seqStepsView.getName());
    StepElement stepElement =
      (StepElement) ((List) seqStepsView.getStepElementList().get( stepNumber)).get( 0);
    // 2nd arg to enterClickAndLeave must be of class Component
    // JGo objects are not
    // helper.enterClickAndLeave( new MouseEventData( this, stepElement, 1,
    //                                                MouseEvent.BUTTON3_MASK));
    stepElement.doMouseClickWithListener( MouseEvent.BUTTON3_MASK, stepElement.getLocation(),
                                          new Point( 0, 0), seqStepsView.getJGoView(),
                                          viewListenerList);
    guiTest.flushAWT(); guiTest.awtSleep();
    // try{Thread.sleep(2000);}catch(Exception e){}

    // PWTestHelper.selectViewMenuItem( seqStepsView, viewMenuItemName, helper, guiTest);
    return PWTestHelper.viewMenuItemExists( seqStepsView, viewMenuItemName, helper, guiTest);
  } // end sequenceStepsViewStepExists

  /**
   * <code>viewBackgroundItemSelection</code>
   *
   * @param view - <code>VizView</code> - 
   * @param viewMenuItemName - <code>String</code> - 
   * @param helper - <code>JFCTestHelper</code> - 
   * @param guiTest - <code>PlanWorksGUITest</code> - 
   * @exception Exception if an error occurs
   */
  public static void viewBackgroundItemSelection( VizView view, String viewMenuItemName,
                                                  JFCTestHelper helper,
                                                  PlanWorksGUITest guiTest)
    throws Exception {
    // System.err.println( "viewBackgroundItemSelection " + view.getName());
    // make view the selected frame so that jfcunit finds its popup menu rather
    // than that of any other frame which may be currently in focus (selected)
    ViewGenerics.raiseFrame( view.getViewFrame());
    JGoView jGoView = null;
     if (! (view instanceof DecisionView)) {
       if (view instanceof ResourceView) {
         jGoView = ((ResourceView) view).getJGoExtentView();
       } else {
         jGoView = view.getJGoView();
       }
      // 2nd arg to enterClickAndLeave must be of class Component
      // JGo objects are not
      // helper.enterClickAndLeave( new MouseEventData( this, stepElement, 1,
      //                                                MouseEvent.BUTTON3_MASK));
      jGoView.doBackgroundClick( MouseEvent.BUTTON3_MASK, view.getLocation(), new Point( 0, 0));
    } else {
      JTree decisionTree = ((DecisionView) view).getDecisionTree();
      boolean isPopupTrigger = true;
      helper.enterClickAndLeave( new MouseEventData( guiTest, decisionTree, 1, isPopupTrigger));
    }

    guiTest.flushAWT(); guiTest.awtSleep();
    // try{Thread.sleep(2000);}catch(Exception e){}

    PWTestHelper.selectViewMenuItem( view, viewMenuItemName, helper, guiTest);
  } // end viewBackgroundItemSelection

  /**
   * <code>viewBackgroundItemSelection</code>
   *
   * @param view - <code>VizView</code> - 
   * @param viewMenuItemName - <code>String</code> - 
   * @param viewListenerList - <code>List</code> - 
   * @param helper - <code>JFCTestHelper</code> - 
   * @param guiTest - <code>PlanWorksGUITest</code> - 
   * @exception Exception if an error occurs
   */
  public static void viewBackgroundItemSelection( VizView view, String viewMenuItemName,
                                                  List viewListenerList,
                                                  JFCTestHelper helper,
                                                  PlanWorksGUITest guiTest) throws Exception {
    // make view the selected frame so that jfcunit finds its popup menu rather
    // than that of any other frame which may be currently in focus (selected)
    ViewGenerics.raiseFrame( view.getViewFrame());
    JGoView jGoView = null;
    if (! (view instanceof DecisionView)) {
      if (view instanceof ResourceView) {
        jGoView = ((ResourceView) view).getJGoExtentView();
      } else {
        jGoView = view.getJGoView();
      }
      // 2nd arg to enterClickAndLeave must be of class Component
      // JGo objects are not
      // helper.enterClickAndLeave( new MouseEventData( this, stepElement, 1,
      //                                                MouseEvent.BUTTON3_MASK));
      ((PartialPlanView) view).setViewListenerList( viewListenerList);
      jGoView.doBackgroundClick( MouseEvent.BUTTON3_MASK, view.getLocation(), new Point( 0, 0));
    } else {
      JTree decisionTree = ((DecisionView) view).getDecisionTree();
      boolean isPopupTrigger = true;
      ((DecisionView) view).setViewListenerList( viewListenerList);
      helper.enterClickAndLeave( new MouseEventData( guiTest, decisionTree, 1, isPopupTrigger));
    }

    guiTest.flushAWT(); guiTest.awtSleep();
    // try{Thread.sleep(2000);}catch(Exception e){}

    PWTestHelper.selectViewMenuItem( view, viewMenuItemName, helper, guiTest);
  } // end viewBackgroundItemSelection

  /**
   * <code>viewBackgroundItemSelection</code>
   *
   * @param view - <code>VizView</code> - 
   * @param viewMenuItemName - <code>String</code> - 
   * @param location - <code>Point</code> - 
   * @param helper - <code>JFCTestHelper</code> - 
   * @param guiTest - <code>PlanWorksGUITest</code> - 
   * @exception Exception if an error occurs
   */
  public static void viewBackgroundItemSelection( VizView view, String viewMenuItemName,
                                                  Point location, JFCTestHelper helper,
                                                  PlanWorksGUITest guiTest)
    throws Exception {
    // make view the selected frame so that jfcunit finds its popup menu rather
    // than that of any other frame which may be currently in focus (selected)
    ViewGenerics.raiseFrame( view.getViewFrame());
    JGoView jGoView = view.getJGoView();
    jGoView.doBackgroundClick( MouseEvent.BUTTON3_MASK, location, new Point( 0, 0));
    guiTest.flushAWT(); guiTest.awtSleep();
    PWTestHelper.selectViewMenuItem( view, viewMenuItemName, helper, guiTest);
  } // end viewBackgroundItemSelection

  /**
   * <code>selectViewMenuItem</code>
   *
   * @param view - <code>VizView</code> - 
   * @param viewMenuItemName - <code>String</code> - 
   * @param helper - <code>JFCTestHelper</code> - 
   * @param guiTest - <code>PlanWorksGUITest</code> - 
   * @exception Exception if an error occurs
   */
  public static void selectViewMenuItem( VizView view, String viewMenuItemName,
                                         JFCTestHelper helper, PlanWorksGUITest guiTest)
    throws Exception {  
    guiTest.flushAWT(); guiTest.awtSleep();
    JPopupMenu popupMenu =
      (JPopupMenu) PWTestHelper.findComponentByClass( JPopupMenu.class);
    Assert.assertNotNull( "Failed to get \"" + popupMenu + "\" popupMenu.", popupMenu);
    JMenuItem viewMenuItem = PWTestHelper.getPopupViewMenuItem( viewMenuItemName, popupMenu);

    // deprecated JMenuItem viewMenuItem =
    //  (JMenuItem) helper.findComponent( new JMenuItemFinder( viewMenuItemName), 0);

    // try{Thread.sleep(2000);}catch(Exception e){}

    Assert.assertNotNull( viewMenuItemName + "' not found:", viewMenuItem); 
    System.err.println( "'" + view.getName() + "' viewMenuItem '" +
                        viewMenuItem.getText() + "'");
    helper.enterClickAndLeave( new MouseEventData( guiTest, viewMenuItem));
    guiTest.flushAWT(); guiTest.awtSleep();
  } // end selectViewMenuItem

  /**
   * <code>selectFrameMenuItem</code>
   *
   * @param frame - <code>MDIInternalFrame</code> - 
   * @param frameMenuItemName - <code>String</code> - 
   * @param helper - <code>JFCTestHelper</code> - 
   * @param guiTest - <code>PlanWorksGUITest</code> - 
   * @exception Exception if an error occurs
   */
  public static void selectFrameMenuItem( MDIInternalFrame frame, String frameMenuItemName,
                                         JFCTestHelper helper, PlanWorksGUITest guiTest)
    throws Exception {  
    guiTest.flushAWT(); guiTest.awtSleep();
    JPopupMenu popupMenu =
      (JPopupMenu) PWTestHelper.findComponentByClass( JPopupMenu.class);
    Assert.assertNotNull( "Failed to get \"" + popupMenu + "\" popupMenu.", popupMenu);
    JMenuItem frameMenuItem = PWTestHelper.getPopupViewMenuItem( frameMenuItemName, popupMenu);

    // deprecated JMenuItem frameMenuItem =
    //  (JMenuItem) helper.findComponent( new JMenuItemFinder( frameMenuItemName), 0);

    // try{Thread.sleep(2000);}catch(Exception e){}

    Assert.assertNotNull( frameMenuItemName + "' not found:", frameMenuItem); 
    System.err.println( "'" + frame.getTitle() + "' frameMenuItem '" +
                        frameMenuItem.getText() + "'");
    helper.enterClickAndLeave( new MouseEventData( guiTest, frameMenuItem));
    guiTest.flushAWT(); guiTest.awtSleep();
  } // end selectFrameMenuItem

  /**
   * <code>viewMenuItemExists</code>
   *
   * @param view - <code>VizView</code> - 
   * @param viewMenuItemName - <code>String</code> - 
   * @param helper - <code>JFCTestHelper</code> - 
   * @param guiTest - <code>PlanWorksGUITest</code> - 
   * @return - <code>boolean</code> - 
   * @exception Exception if an error occurs
   */
  public static boolean viewMenuItemExists( VizView view, String viewMenuItemName,
                                            JFCTestHelper helper, PlanWorksGUITest guiTest)
    throws Exception {  
    guiTest.flushAWT(); guiTest.awtSleep();
    JPopupMenu popupMenu =
      (JPopupMenu) PWTestHelper.findComponentByClass( JPopupMenu.class);
    Assert.assertNotNull( "Failed to get \"" + popupMenu + "\" popupMenu.", popupMenu);
    JMenuItem viewMenuItem = PWTestHelper.getPopupViewMenuItem( viewMenuItemName, popupMenu);

    // deprecated JMenuItem viewMenuItem =
    //  (JMenuItem) helper.findComponent( new JMenuItemFinder( viewMenuItemName), 0);

    return (viewMenuItem != null);
  } // end viewMenuItemExists

  /**
   * <code>getPopupViewMenuItem</code> - partial plan views and all views
   *
   * @param viewMenuItemName - <code>String</code> - 
   * @param popupMenu - <code>JPopupMenu</code> - 
   * @return - <code>JMenuItem</code> - 
   */
  public static JMenuItem getPopupViewMenuItem( String viewMenuItemName,
                                                JPopupMenu popupMenu) {
    JMenuItem viewMenuItem = null;
    for (int i = 0, n = popupMenu.getComponentCount(); i < n; i++) {
      // System.err.println( "popup i " + i + " " +
      //                    popupMenu.getComponent( i).getClass().getName());
      if (popupMenu.getComponent(i) instanceof JMenuItem) {
        if (((JMenuItem) popupMenu.getComponent(i)).getText().equals( viewMenuItemName)) {
          viewMenuItem = (JMenuItem) popupMenu.getComponent(i);
          break;
        }
      }
    }
    return viewMenuItem;
  } // end getPopupViewMenuItem

  /**
   * <code>getStackIndex</code> - index = 0 means that frame has focus
   *
   * @param internalFrame - <code>MDIInternalFrame</code> - 
   * @return - <code>int</code> - 
   */
  public static int getStackIndex( MDIInternalFrame internalFrame) {
    Container contentPane = PlanWorks.getPlanWorks().getContentPane();
    int stackIndex = -1;
    for (int i = 0, n = contentPane.getComponentCount(); i < n; i++) {
      Component component = (Component) contentPane.getComponent( i);
      // System.err.println( "PlanWorks i " + i + " " + component);
      if (component instanceof MDIDesktopPane) {
        JInternalFrame[] frames = ((MDIDesktopPane) component).getAllFrames();
        for (int j = 0, m = frames.length; j < m; j++) {
          // System.err.println( "j " + j + " " + ((MDIInternalFrame) frames[j]).getTitle());
          if (((MDIInternalFrame) frames[j]).getTitle().equals( internalFrame.getTitle())) {
            stackIndex = j;
            break;
          }
        }
        break;
      }
    }
    return stackIndex;
  } // end getStackIndex

  /**
   * <code>getInternalFramesByPrefixName</code>
   *
   * @param prefixName - <code>String</code> - 
   * @return - <code>List</code> - of MDIInternalFrame
   */
  public static List getInternalFramesByPrefixName( String prefixName) {
    List frameList = new ArrayList();
    Container contentPane = PlanWorks.getPlanWorks().getContentPane();
    for (int i = 0, n = contentPane.getComponentCount(); i < n; i++) {
      Component component = (Component) contentPane.getComponent( i);
      if (component instanceof MDIDesktopPane) {
        JInternalFrame[] frames = ((MDIDesktopPane) component).getAllFrames();
        for (int j = 0, m = frames.length; j < m; j++) {
          // System.err.println( "j " + j + " " + ((MDIInternalFrame) frames[j]).getTitle());
          MDIInternalFrame frame = (MDIInternalFrame) frames[j];
          if (frame.getTitle().startsWith( prefixName)) {
            frameList.add( frame);
          }
        }
        break;
      }
    }
    return frameList;
  } // end getInternalFramesByPrefixName

  /**
   * <code>getAllInternalFrames</code>
   *
   * @return - <code>List</code> - of MDIInternalFrame
   */
  public static List getAllInternalFrames() {
    List frameList = new ArrayList();
    Container contentPane = PlanWorks.getPlanWorks().getContentPane();
    for (int i = 0, n = contentPane.getComponentCount(); i < n; i++) {
      Component component = (Component) contentPane.getComponent( i);
      if (component instanceof MDIDesktopPane) {
        JInternalFrame[] frames = ((MDIDesktopPane) component).getAllFrames();
        for (int j = 0, m = frames.length; j < m; j++) {
          // System.err.println( "j " + j + " " + ((MDIInternalFrame) frames[j]).getTitle());
          MDIInternalFrame frame = (MDIInternalFrame) frames[j];
          frameList.add( frame);
        }
        break;
      }
    }
    return frameList;
  } // end getAllInternalFrames

  /**
   * <code>getSequenceQueryWindow</code>
   *
   * @param sequenceName - <code>String</code> - 
   * @param helper - <code>JFCTestHelper</code> - 
   * @param guiTest - <code>PlanWorksGUITest</code> - 
   * @return - <code>SequenceQueryWindow</code> - 
   * @exception Exception if an error occurs
   */
//   public static SequenceQueryWindow getSequenceQueryWindow( String sequenceName,
//                                                              JFCTestHelper helper,
//                                                              PlanWorksGUITest guiTest)
//     throws Exception {
//     String windowFindName = ViewConstants.SEQUENCE_QUERY_TITLE + " for " + sequenceName;
//     System.err.println( "getSequenceQueryWindow windowFindName '" + windowFindName + "'");
//     SequenceQueryWindow seqQueryWindow =
//       (SequenceQueryWindow)  PWTestHelper.findComponentByName( SequenceQueryWindow.class,
//                                                                windowFindName,
//                                                                 Finder.OP_EQUALS);
//      Assert.assertNotNull( windowFindName + " not found:", seqQueryWindow);
//     System.err.println( "getSequenceQueryWindow found " + windowFindName);
//     return seqQueryWindow;
//   } // end getSequenceQueryWindow

  /**
   * <code>getQueryResultsWindow</code>
   *
   * @param windowClass - <code>Class</code> - 
   * @param sequenceName - <code>String</code> - 
   * @param resultsWindowCount - <code>int</code> - 
   * @param helper - <code>JFCTestHelper</code> - 
   * @param guiTest - <code>PlanWorksGUITest</code> - 
   * @return - <code>VizView</code> - 
   * @exception Exception if an error occurs
   */
//   public static VizView getQueryResultsWindow( Class windowClass, String sequenceName,
//                                                int resultsWindowCount,
//                                                JFCTestHelper helper,
//                                                PlanWorksGUITest guiTest)
//     throws Exception {
//     String windowFindName = ViewConstants.SEQUENCE_QUERY_RESULTS_TITLE + " for " +
//       sequenceName + " - " + String.valueOf( resultsWindowCount);
//     System.err.println( "getQueryResultsWindow windowFindName '" + windowFindName + "'");
//     VizView queryResultsWindow =
//       (VizView)  PWTestHelper.findComponentByName( windowClass, windowFindName,
//                                                    Finder.OP_EQUALS);
//     Assert.assertNotNull( windowFindName + " not found:", queryResultsWindow);
//     System.err.println( "getQueryResultsWindow found " + windowFindName);
//     return queryResultsWindow;
//   } // end getSequenceQueryWindow

  /**
   * <code>selectComboBoxItem</code>
   *
   * @param seqQueryWindow - <code>SequenceQueryWindow</code> - 
   * @param comboBoxClass - <code>Class</code> - 
   * @param comboBoxItem - <code>String</code> - 
   * @param helper - <code>JFCTestHelper</code> - 
   * @param guiTest - <code>PlanWorksGUITest</code> - 
   */
//   public static void selectComboBoxItem( SequenceQueryWindow seqQueryWindow,
//                                          Class comboBoxClass, String comboBoxItem,
//                                          JFCTestHelper helper, PlanWorksGUITest guiTest)
//     throws Exception {
//     ComponentFinder finder = new ComponentFinder( comboBoxClass);
//     JComboBox comboBox = (JComboBox) finder.find( seqQueryWindow, 0);
//     Assert.assertNotNull( seqQueryWindow.getName() + " combo box " +
//                           comboBoxClass.getName() + " not found", comboBox);
//     int comboItemIndex = -1;
//     for (int i = 0, n = comboBox.getItemCount(); i < n; i++) {
//       Object item = comboBox.getItemAt( i);
//       if ((item instanceof String) && (((String) item).equals( comboBoxItem))) {
//         comboItemIndex = i;
//         break;
//       }
//     }
//     Assert.assertTrue( "Could not find " + comboBoxClass.getName() + " item " +
//                        comboBoxItem, (comboItemIndex >= 0));
// //     System.err.println( "getComboBoxItem comboBox " + comboBox + " comboItemIndex " +
// //                         comboItemIndex);
//     int numClicks = 1;
//     helper.enterClickAndLeave( new JComboBoxMouseEventData( guiTest, comboBox,
//                                                             comboItemIndex, numClicks));
//     System.err.println( "Selected '" + seqQueryWindow.getName() + "' comboBox '" +
//                         comboBoxItem + "'");
//     guiTest.flushAWT(); guiTest.awtSleep();
//   } // end selectComboBoxItem

  /**
   * <code>applySequenceQuery</code>
   *
   * @param seqQueryWindow - <code>SequenceQueryWindow</code> - 
   * @param viewListener - <code>ViewListener</code> - 
   * @param buttonName - <code>String</code> - 
   * @param helper - <code>JFCTestHelper</code> - 
   * @param guiTest - <code>PlanWorksGUITest</code> - 
   * @exception Exception if an error occurs
   */
//   public static void applySequenceQuery( SequenceQueryWindow seqQueryWindow,
//                                          ViewListener viewListener,
//                                          String buttonName, JFCTestHelper helper,
//                                          PlanWorksGUITest guiTest)
//     throws Exception {
//     ComponentFinder finder = new ComponentFinder( JButton.class);
//     JButton applyButton = null, button = null;
//     int index = 0;
//     while ((button = (JButton) finder.find( seqQueryWindow, index)) != null) {
//       // System.err.println( "button " + button);
//       if (button.getText().equals( buttonName)) {
//         applyButton = button;
//         break;
//       }
//       index++;
//     }
//     Assert.assertNotNull( "Could not find " + seqQueryWindow.getName() +
//                          " '" + buttonName + "' button", applyButton);
//     System.err.println( seqQueryWindow.getName() + " " + applyButton.getText());
//     seqQueryWindow.setViewListener( viewListener);
//     helper.enterClickAndLeave( new MouseEventData( guiTest, applyButton));
//     guiTest.flushAWT(); guiTest.awtSleep();
//   } // end applySequenceQuery


  /**
   * <code>setSequenceQueryField</code>
   *
   * @param seqQueryWindow - <code>SequenceQueryWindow</code> - 
   * @param fieldName - <code>String</code> - 
   * @param fieldValue - <code>String</code> - 
   * @param helper - <code>JFCTestHelper</code> - 
   * @param guiTest - <code>PlanWorksGUITest</code> - 
   * @exception Exception if an error occurs
   */
//   public static void setSequenceQueryField( SequenceQueryWindow seqQueryWindow,
//                                             String fieldName, String fieldValue,
//                                             JFCTestHelper helper,
//                                             PlanWorksGUITest guiTest)
//     throws Exception {
//     ComponentFinder finder = new ComponentFinder( JLabel.class);
//     JLabel label = null;
//     int index = 0;
//     while ((label = (JLabel) finder.find( seqQueryWindow, index)) != null) {
//       // System.err.println( "label " + label);
//       if (label.getText().equals( fieldName)) {
//         break;
//       }
//       index++;
//     }
//     finder = new ComponentFinder( JTextField.class);
//     JTextField field = (JTextField) finder.find( seqQueryWindow, index);
//     Assert.assertNotNull( "Could not find " + seqQueryWindow.getName() +
//                           " '" + fieldName + "' field", field);
//     System.err.println( seqQueryWindow.getName() + " " + label.getText() +
//                         " = " + fieldValue); 
//     field.setText( null);
//     helper.sendString( new StringEventData( guiTest, field, fieldValue));
//     helper.sendKeyAction( new KeyEventData( guiTest, field, KeyEvent.VK_ENTER));
//     guiTest.flushAWT(); guiTest.awtSleep();
//   } // end setSequenceQueryField


  /**
   * <code>getPartialPlanView</code>
   *
   * @param viewName - <code>String</code> - 
   * @param viewNameSuffix - <code>String</code> - 
   * @param guiTest - <code>PlanWorksGUITest</code> - 
   * @return - <code>PartialPlanView</code> - 
   */
  public static PartialPlanView getPartialPlanView( String viewName, String viewNameSuffix,
                                                    PlanWorksGUITest guiTest) throws Exception {
    if (viewName.equals( ViewConstants.CONSTRAINT_NETWORK_VIEW)) {
      String constraintNetworkViewTitle =
        ViewConstants.CONSTRAINT_NETWORK_VIEW.replaceAll( " ", "") + " of " + viewNameSuffix;
      ConstraintNetworkView constraintNetworkView =
        (ConstraintNetworkView) PWTestHelper.findComponentByName
        ( ConstraintNetworkView.class, constraintNetworkViewTitle, Finder.OP_EQUALS);
      guiTest.assertNotNullVerbose( constraintNetworkViewTitle + " not found",
                                    constraintNetworkView, "not ");
      return constraintNetworkView;
    }//  else if (viewName.equals( ViewConstants.DB_TRANSACTION_VIEW)) {
//       String dbTransactionViewName =
//         ViewConstants.DB_TRANSACTION_VIEW.replaceAll( " ", "") + " of " + viewNameSuffix;
//       DBTransactionView dbTransactionView =
//         (DBTransactionView) PWTestHelper.findComponentByName
//         ( DBTransactionView.class, dbTransactionViewName, Finder.OP_EQUALS);
//       guiTest.assertNotNullVerbose( dbTransactionViewName + " not found",
//                                     dbTransactionView, "not ");
//       return dbTransactionView;}
    else if (viewName.equals( ViewConstants.DECISION_VIEW)) {
      String decisionViewName =
        ViewConstants.DECISION_VIEW.replaceAll( " ", "") + " of " + viewNameSuffix;
      DecisionView decisionView =
        (DecisionView) PWTestHelper.findComponentByName
        ( DecisionView.class, decisionViewName, Finder.OP_EQUALS);
      guiTest.assertNotNullVerbose( decisionViewName + " not found",
                                    decisionView, "not ");
      return decisionView;
    } else if (viewName.equals( ViewConstants.RESOURCE_PROFILE_VIEW)) {
      String resourceProfileViewName =
        ViewConstants.RESOURCE_PROFILE_VIEW.replaceAll( " ", "") + " of " + viewNameSuffix;
      ResourceProfileView resourceProfileView =
        (ResourceProfileView) PWTestHelper.findComponentByName
        ( ResourceProfileView.class, resourceProfileViewName, Finder.OP_EQUALS);
      guiTest.assertNotNullVerbose( resourceProfileViewName + " not found",
                                    resourceProfileView, "not ");
      return resourceProfileView;
    } else if (viewName.equals( ViewConstants.RESOURCE_TRANSACTION_VIEW)) {
      String resourceTransactionViewName =
        ViewConstants.RESOURCE_TRANSACTION_VIEW.replaceAll( " ", "") + " of " + viewNameSuffix;
      ResourceTransactionView resourceTransactionView =
        (ResourceTransactionView) PWTestHelper.findComponentByName
        ( ResourceTransactionView.class, resourceTransactionViewName, Finder.OP_EQUALS);
      guiTest.assertNotNullVerbose( resourceTransactionViewName + " not found",
                                    resourceTransactionView, "not ");
      return resourceTransactionView;
    } else if (viewName.equals( ViewConstants.TEMPORAL_EXTENT_VIEW)) {
      String temporalExtentViewTitle =
        ViewConstants.TEMPORAL_EXTENT_VIEW.replaceAll( " ", "") + " of " + viewNameSuffix;
      TemporalExtentView temporalExtentView =
        (TemporalExtentView) PWTestHelper.findComponentByName
        ( TemporalExtentView.class, temporalExtentViewTitle, Finder.OP_EQUALS);
      guiTest.assertNotNullVerbose( temporalExtentViewTitle + " not found",
                                    temporalExtentView, "not ");
      return temporalExtentView;
    } else if (viewName.equals( ViewConstants.TIMELINE_VIEW)) {
      String timelineViewName =
        ViewConstants.TIMELINE_VIEW.replaceAll( " ", "") + " of " + viewNameSuffix;
      TimelineView timelineView =
        (TimelineView) PWTestHelper.findComponentByName
        ( TimelineView.class, timelineViewName, Finder.OP_EQUALS);
      guiTest.assertNotNullVerbose( timelineViewName + " not found", timelineView, "not ");
      return timelineView;
    } else if (viewName.equals( ViewConstants.TOKEN_NETWORK_VIEW)) {
      String tokenNetworkViewName =
        ViewConstants.TOKEN_NETWORK_VIEW.replaceAll( " ", "") + " of " + viewNameSuffix;
      TokenNetworkView tokenNetworkView =
        (TokenNetworkView) PWTestHelper.findComponentByName
        ( TokenNetworkView.class, tokenNetworkViewName, Finder.OP_EQUALS);
      guiTest.assertNotNullVerbose( tokenNetworkViewName + " not found",
                                    tokenNetworkView, "not ");
      return tokenNetworkView;
    } else if (viewName.equals( ViewConstants.RULE_INSTANCE_VIEW)) {
      String ruleInstanceViewName =
        ViewConstants.RULE_INSTANCE_VIEW.replaceAll( " ", "") + " of " + viewNameSuffix;
      RuleInstanceView ruleInstanceView =
        (RuleInstanceView) PWTestHelper.findComponentByName
        ( RuleInstanceView.class, ruleInstanceViewName, Finder.OP_EQUALS);
      guiTest.assertNotNullVerbose( "'" + ruleInstanceViewName + "' not found",
                                    ruleInstanceView, "not ");
      return ruleInstanceView;
    } else {
      return null;
    }
  } // end getPartialPlanView

  /**
   * <code>getContentSpecWindow</code>
   *
   * @param viewNameSuffix - <code>String</code> - 
   * @param helper - <code>JFCTestHelper</code> - 
   * @param guiTest - <code>PlanWorksGUITest</code> - 
   * @return - <code>ContentSpecWindow</code> - 
   * @exception Exception if an error occurs
   */
  public static ContentSpecWindow getContentSpecWindow( String viewNameSuffix,
                                                        JFCTestHelper helper,
                                                        PlanWorksGUITest guiTest)
    throws Exception {
    String windowFindName = ViewConstants.CONTENT_SPEC_TITLE + " for " + viewNameSuffix;
    System.err.println( "getContentSpecWindow windowFindName '" + windowFindName + "'");
    ContentSpecWindow contentSpecWindow =
      (ContentSpecWindow)  PWTestHelper.findComponentByName
      ( ContentSpecWindow.class, windowFindName,Finder.OP_EQUALS);
     Assert.assertNotNull( windowFindName + " not found:", contentSpecWindow);
    System.err.println( "getContentSpecWindow found " + windowFindName);
    return contentSpecWindow;
  } // end getContentSpecWindow


  /**
   * <code>openAllExistingViews</code>
   *
   * @param currentViewName - <code>String</code> - 
   * @param currentView - <code>PartialPlanView</code> - 
   * @param helper - <code>JFCTestHelper</code> - 
   * @param guiTest - <code>PlanWorksGUITest</code> - 
   */
  public static void openAllExistingViews( String currentViewName,  PartialPlanView currentView,
                                           JFCTestHelper helper, PlanWorksGUITest guiTest)
    throws Exception {
    Iterator viewItr = PlanWorks.PARTIAL_PLAN_VIEW_LIST.iterator();
    while (viewItr.hasNext()) {
      String viewName = (String) viewItr.next();
      if (! viewName.equals( currentViewName)) {
        ViewGenerics.raiseFrame( currentView.getViewFrame());
        PWTestHelper.viewBackgroundItemSelection( currentView, "Open " + viewName,
                                                  helper, guiTest);
        ViewSet viewSet = currentView.getViewSet();
         guiTest.assertTrueVerbose( viewName + " is not selected",
                                    (viewSet.getViewFrame( viewName).isSelected() == true),
                                    "not ");
      }
    }
  } // end openAllExistingViews

  /**
   * <code>selectWindow</code>
   *
   * @param viewName - <code>String</code> - 
   * @param helper - <code>JFCTestHelper</code> - 
   * @param guiTest - <code>PlanWorksGUITest</code> - 
   * @exception Exception if an error occurs
   */
  public static void selectWindow( String viewName, JFCTestHelper helper,
                                   PlanWorksGUITest guiTest) throws Exception {
    JMenuItem menuItem =
      PWTestHelper.findDynamicMenuItem( PlanWorks.WINDOW_MENU, viewName,
                                        helper, guiTest);
    helper.enterClickAndLeave( new MouseEventData( guiTest, menuItem));
    guiTest.flushAWT(); guiTest.awtSleep();
  } // end selectWindow

  /**
   * <code>setQueryField</code>
   *
   * @param field - <code>JTextField</code> - 
   * @param fieldValue - <code>String</code> - 
   * @param helper - <code>JFCTestHelper</code> - 
   * @param guiTest - <code>PlanWorksGUITest</code> - 
   * @exception Exception if an error occurs
   */
  public static void setQueryField( JTextField field, String fieldValue, JFCTestHelper helper,
                                    PlanWorksGUITest guiTest)
    throws Exception {
    System.err.println( "field set to " + fieldValue); 
    field.setText( null);
    helper.sendString( new StringEventData( guiTest, field, fieldValue));
    helper.sendKeyAction( new KeyEventData( guiTest, field, KeyEvent.VK_ENTER));
  } // end setQueryField

  /**
   * <code>findEntityPath</code>
   *
   * @param entityId1 - <code>Integer</code> - 
   * @param entityId2 - <code>Integer</code> - 
   * @param viewMenuItemName - <code>String</code> - 
   * @param partialPlanView - <code>PartialPlanView</code> - 
   * @param helper - <code>JFCTestHelper</code> - 
   * @param guiTest - <code>PlanWorksGUITest</code> - 
   * @exception Exception if an error occurs
   */
  public static void findEntityPath( Integer entityId1, Integer entityId2,
                                     String viewMenuItemName, PartialPlanView partialPlanView,
                                     JFCTestHelper helper, PlanWorksGUITest guiTest)
    throws Exception {
    PWTestHelper.viewBackgroundItemSelection( partialPlanView, viewMenuItemName, helper,
                                              guiTest);
    guiTest.flushAWT(); guiTest.awtSleep();

    MDIInternalFrame findEntityFrame =
      (MDIInternalFrame) PWTestHelper.findComponentByClass( MDIInternalFrame.class);
    Assert.assertNotNull( "Failed to get \"" + findEntityFrame + "\" findEntityFrame.",
                          findEntityFrame);
    Container contentPane = findEntityFrame.getContentPane();
    AskQueryTwoEntityKeysClasses entityPane = null;
    for (int i = 0, n = contentPane.getComponentCount(); i < n; i++) {
      //System.err.println( "i " + i + " " +
      //                    contentPane.getComponent( i).getClass().getName());
      if (contentPane.getComponent(i) instanceof AskQueryTwoEntityKeysClasses) {
        entityPane = (AskQueryTwoEntityKeysClasses) contentPane.getComponent(i);
        break;
      }
    }
    EntityKeysBox entityKeysBox = entityPane.getEntityKeysBox();
    JTextField startField = entityKeysBox.getStartValue();
    JTextField endField = entityKeysBox.getEndValue();
    System.err.println( "findEntityPath startField set to " + entityId1.toString());
    startField.setText( null);
    helper.sendString( new StringEventData( guiTest, startField, entityId1.toString()));
    System.err.println( "findEntityPath endField set to " + entityId2.toString());
    endField.setText( null);
    helper.sendString( new StringEventData( guiTest, endField, entityId2.toString()));

    PWTestHelper.buttonLeftClick( entityPane.getPathExistsButton(), "Does Path Exist");
    System.err.println( "findEntityPath: \"Does Path Exist\"");
    guiTest.flushAWT(); guiTest.awtSleep();
    PWTestHelper.selectJOptionPaneItem( "Entity Path Exists", "OK", helper, guiTest);

    PWTestHelper.buttonLeftClick( entityPane.getFindPathButton(), "Find Path");
    System.err.println( "findEntityPath: \"Find Path\"");
    guiTest.flushAWT(); guiTest.awtSleep();
    // need extra time for progress monitor to go away
    try{Thread.sleep(1000);}catch(Exception e){}
    PWTestHelper.selectJOptionPaneItem( "Found Entity Key Path", "OK", helper, guiTest);
  } // end findEntityPath

  /**
   * <code>buttonLeftClick</code>
   *
   * @param button - <code>JButton</code> - 
   * @param actionName - <code>String</code> - 
   */
  public static void buttonLeftClick( JButton button, String actionName) {
    ActionListener [] actionList = button.getActionListeners();
    ActionEvent evt = new ActionEvent( button, ActionEvent.ACTION_PERFORMED,
                                       actionName, (int) AWTEvent.MOUSE_EVENT_MASK);
    for (int i = 0, n = actionList.length; i < n; i++) {
      ((ActionListener) actionList[i]).actionPerformed( evt);
    }
  } // end buttonLeftClick

  /**
   * <code>selectJDialogItem</code>
   *
   * @param dialogName - <code>String</code> - 
   * @param dialogItemName - <code>String</code> - 
   * @param helper - <code>JFCTestHelper</code> - 
   * @param guiTest - <code>PlanWorksGUITest</code> - 
   * @exception Exception if an error occurs
   */
  public static void selectJOptionPaneItem( String optionPaneName, String optionPaneItemName,
                                        JFCTestHelper helper, PlanWorksGUITest guiTest)
    throws Exception {  
    guiTest.flushAWT(); guiTest.awtSleep();
    JOptionPane optionPane = (JOptionPane) PWTestHelper.findComponentByClass( JOptionPane.class);
    Assert.assertNotNull( "Failed to get \"" + optionPane + "\" optionPane.", optionPane);

    JButton optionPaneItem = null;
    for (int i = 0, n = optionPane.getComponentCount(); i < n; i++) {
      // System.err.println( "optionPane i " + i + " " +
      //                   optionPane.getComponent( i).getClass().getName());
      if ((i == 1) && (optionPane.getComponent( i) instanceof JPanel)) {
        JPanel buttonPanel = (JPanel) optionPane.getComponent( i);
        for (int j = 0, m = buttonPanel.getComponentCount(); j < m; j++) {
          // System.err.println( "buttonPanel j " + j + " " +
          //                    buttonPanel.getComponent( j).getClass().getName());
          if (buttonPanel.getComponent(j) instanceof JButton) {
            if (((JButton) buttonPanel.getComponent(j)).getText().equals( optionPaneItemName)) {
              optionPaneItem = (JButton) buttonPanel.getComponent(j);
              break;
            }
          }
        }
      }
    }
    Assert.assertNotNull( optionPaneItemName + "' not found:", optionPaneItem); 
    System.err.println( "'" + optionPaneName + "' optionPaneItem '" +
                        optionPaneItem.getText() + "'");
    helper.enterClickAndLeave( new MouseEventData( guiTest, optionPaneItem));
    guiTest.flushAWT(); guiTest.awtSleep();
  } // end selectViewMenuItem


} // end abstract class PWTestHelper

    

