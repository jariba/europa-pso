//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: PWTestHelper.java,v 1.3 2004-04-09 23:11:24 taylor Exp $
//
package gov.nasa.arc.planworks.test;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import junit.extensions.jfcunit.JFCTestHelper;
import junit.extensions.jfcunit.eventdata.KeyEventData;
import junit.extensions.jfcunit.eventdata.MouseEventData;
import junit.extensions.jfcunit.eventdata.StringEventData;
import junit.extensions.jfcunit.finder.AbstractButtonFinder;
import junit.extensions.jfcunit.finder.ComponentFinder;
import junit.extensions.jfcunit.finder.NamedComponentFinder;
import junit.framework.Assert;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.DbConstants;
import gov.nasa.arc.planworks.db.impl.PwPlanningSequenceImpl;
import gov.nasa.arc.planworks.db.impl.PwPartialPlanImpl;
import gov.nasa.arc.planworks.db.util.FileUtils;
import gov.nasa.arc.planworks.util.ResourceNotFoundException;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanViewMenu;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanViewMenuItem;
import gov.nasa.arc.planworks.viz.sequence.sequenceSteps.SequenceStepsView;


public abstract class PWTestHelper {

  public static final String GUI_TEST_DIR = "GUITest";

  public static List buildTestData( int numSequences, int numSteps, PlanWorks planWorks) {
    String guiSequencesUrl = System.getProperty( "planworks.test.data.dir") +
      System.getProperty( "file.separator") + GUI_TEST_DIR;
    File guiSequencesUrlFile = new File( guiSequencesUrl);
    boolean success = false;
    if (guiSequencesUrlFile.isDirectory()) {
      success = FileUtils.deleteDir( guiSequencesUrlFile);
      if (! success) {
        System.err.println( "PWTestHelper.buildTestData: deleting '" + guiSequencesUrl +
                            "' failed"); System.exit( -1);
      }
    }
    success = guiSequencesUrlFile.mkdir();
    if (! success) {
      System.err.println( "PWTestHelper.buildTestData: creating '" + guiSequencesUrl +
                          "' failed"); System.exit( -1);
    }
    List sequenceUrls = new ArrayList();
    for (int i = 0; i < numSequences; i++) {
      sequenceUrls.add( createSequence( guiSequencesUrl, numSteps));
    }
    return sequenceUrls;
  } // end buildTestData

  private static String createSequence( String guiSequencesUrl, int numSteps) {
    boolean forTesting = true;
    int entityId = 0;
    Long sequenceId = new Long( System.currentTimeMillis());
    String sequenceIdString = sequenceId.toString();
    String sequenceName = "sequence" + sequenceIdString;
    String sequenceUrl = guiSequencesUrl + System.getProperty( "file.separator") + sequenceName;
    System.err.println( "sequenceUrl " + sequenceUrl);
    writeDirectory( sequenceUrl);
    PwPlanningSequenceImpl planSequence = null;
    try {
      planSequence = new PwPlanningSequenceImpl( sequenceUrl, sequenceId, forTesting);
    } catch (ResourceNotFoundException rnfe) {
      System.err.println( rnfe.getMessage());
      System.exit( -1);
    }

    for (int stepNum = 0; stepNum < numSteps; stepNum++) {
      planSequence.addPartialPlan( createPartialPlan( planSequence, sequenceUrl,
                                                      stepNum, entityId), forTesting);
    }

    String [] planSequenceContent = planSequence.toOutputString();
    writePlanSequenceFile( sequenceUrl, DbConstants.SEQ_PP_STATS, planSequenceContent[0]);
    writePlanSequenceFile( sequenceUrl, DbConstants.SEQ_FILE, planSequenceContent[1]);
    writePlanSequenceFile( sequenceUrl, DbConstants.SEQ_TRANSACTIONS, planSequenceContent[2]);
    return sequenceUrl;
  } // end createSequence

  private static PwPartialPlanImpl createPartialPlan( PwPlanningSequenceImpl planSequence,
                                                      String sequenceUrl, int stepNum,
                                                      int entityId) {
    String model = "basic-model";
    Long partialPlanId = new Long( System.currentTimeMillis());
    String partialPlanName = "step" + String.valueOf( stepNum);
    String partialPlanUrl = sequenceUrl + System.getProperty( "file.separator") +
      partialPlanName;
    System.err.println( "partialPlanUrl " + partialPlanUrl);
    writeDirectory( partialPlanUrl);
    PwPartialPlanImpl partialPlan = null;
    try {
      partialPlan = new PwPartialPlanImpl( sequenceUrl, partialPlanName, planSequence,
                                           partialPlanId, model);
      partialPlan.setName( partialPlanUrl);
    } catch (ResourceNotFoundException rnfe) {
      System.err.println( rnfe.getMessage());
      System.exit( -1);
    }

    int numTokens = 3, numVariables = 15, numConstraints = 15, numTransactions = 0;
//     for (int nToken = 0; nToken < numTokens; nToken++) {
//       PwTokenImpl token = new PwTokenImpl();
//       partialPlan.addToken( new Integer( ++entityId), token);
//     }

    writePartialPlanFile( partialPlanUrl, partialPlanName, DbConstants.PP_CONSTRAINTS_EXT,
                          "");
    writePartialPlanFile( partialPlanUrl, partialPlanName, DbConstants.PP_CONSTRAINT_VAR_MAP_EXT,
                          "");
    writePartialPlanFile( partialPlanUrl, partialPlanName, DbConstants.PP_RESOURCE_INSTANTS_EXT,
                          "");
    writePartialPlanFile( partialPlanUrl, partialPlanName, DbConstants.PP_OBJECTS_EXT,
                          "");
    writePartialPlanFile( partialPlanUrl, partialPlanName, DbConstants.PP_PARTIAL_PLAN_EXT,
                          partialPlan.toOutputString());
    writePartialPlanFile( partialPlanUrl, partialPlanName, DbConstants.PP_TOKEN_RELATIONS_EXT,
                          "");
    writePartialPlanFile( partialPlanUrl, partialPlanName, DbConstants.PP_TOKENS_EXT,
                          "");
    writePartialPlanFile( partialPlanUrl, partialPlanName, DbConstants.PP_VARIABLES_EXT,
                          "");
    return partialPlan;
  } // end createPartialPlan

  private static void writeDirectory( String dir) {
    File dirFile = new File( dir);
    boolean success = dirFile.mkdir();
    if (! success) {
      System.err.println( "PWTestHelper.buildTestData: creating '" + dir +
                          "' failed");
      System.exit( -1);
    }
  } // end writeDirectory

  private static void writePlanSequenceFile( String sequenceUrl, String fileExtension,
                                             String content) {
    String fileName = sequenceUrl + System.getProperty( "file.separator") + fileExtension;
    try {
      BufferedWriter out = new BufferedWriter( new FileWriter( fileName));
      out.write( content);
      out.close();
    } catch (IOException e) {
    }
  } // end writePlanSequenceFile

  private static void writePartialPlanFile( String partialPlanUrl, String partialPlanName,
                                            String fileExtension, String content) {
    String fileName = partialPlanUrl + System.getProperty( "file.separator") +
      partialPlanName + "." + fileExtension;
    BufferedWriter out = null;
    try {
      out = new BufferedWriter( new FileWriter( fileName));
      out.write( content);
      out.close();
    } catch (IOException e) {
    }
  } // end writePartialPlanFile




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
   * <code>findMenuItem</code>
   *
   * @param menuName - <code>String</code> - 
   * @param itemName - <code>String</code> - 
   * @param helper - <code>JFCTestHelper</code> - 
   * @return - <code>JMenuItem</code> - 
   */
  public static JMenuItem findMenuItem(String menuName, String itemName,
                                       JFCTestHelper helper, PlanWorksGUITest guiTest) {
    JMenu parent = findMenu(menuName);
    Assert.assertTrue(parent != null);
    if(parent == null) {
      return null;
    }
    helper.enterClickAndLeave(new MouseEventData( guiTest, parent));
    AbstractButtonFinder finder = new AbstractButtonFinder(itemName);
    return (JMenuItem) finder.find();
  }

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
    return (Component) finder.find();
  }

  /**
   * <code>findComponentByName</code>
   *
   * @param componentClass - <code>Class</code> - 
   * @param compLabel - <code>String</code> - 
   * @return - <code>Component</code> - 
   */
  public static Component findComponentByName( Class componentClass, String compLabel) {
    NamedComponentFinder finder =
      new NamedComponentFinder( componentClass, compLabel);
    return (Component) finder.find();
  }

  public static void createProject( String projectName, String sequenceDirectory,
                                    File [] sequenceFileArray, JFCTestHelper helper,
                                    PlanWorksGUITest guiTest, PlanWorks planWorks)
    throws Exception {
    JMenuItem createItem =
      PWTestHelper.findMenuItem( PlanWorks.PROJECT_MENU, PlanWorks.CREATE_MENU_ITEM,
                                 helper, guiTest);
    // System.err.println("Found create menu item: " + createItem);
    Assert.assertNotNull( "'Project->Create' not found:", createItem);
    helper.enterClickAndLeave( new MouseEventData( guiTest, createItem));
    guiTest.flushAWT(); guiTest.awtSleep();

    planWorks.getSequenceDirChooser().setCurrentDirectory( new File( sequenceDirectory));
    planWorks.getSequenceDirChooser().setSelectedFiles( sequenceFileArray);

    JTextField field = (JTextField) PWTestHelper.findComponentByClass( JTextField.class);
    // System.err.println( "createProject field " + field);
    Assert.assertNotNull( "Could not find \"name (string)\" field", field);
    field.setText( null);
    helper.sendString( new StringEventData( guiTest, field, projectName));
    helper.sendKeyAction( new KeyEventData( guiTest, field, KeyEvent.VK_ENTER));
    
    JFileChooser fileChooser = null;
    fileChooser = helper.getShowingJFileChooser( planWorks);
    Assert.assertNotNull( "Select Sequence Directory Dialog not found:", fileChooser);
    Container projectSeqDialog = (Container) fileChooser;

    JButton okButton = (JButton) PWTestHelper.findButton( "OK");
    System.err.println( "createProject " + okButton.getText());
    Assert.assertNotNull("Could not find projectSeqDialog \"OK\" button", okButton);
    helper.enterClickAndLeave( new MouseEventData( guiTest, okButton));
    guiTest.flushAWT(); guiTest.awtSleep();
  } // end createProject

  /**
   * <code>deleteProject</code>
   *
   * @param helper - <code>JFCTestHelper</code> - 
   * @param guiTest - <code>PlanWorksGUITest</code> - 
   * @exception Exception if an error occurs
   */
  public static void deleteProject( JFCTestHelper helper, PlanWorksGUITest guiTest)
    throws Exception {
    JMenuItem deleteItem =
      PWTestHelper.findMenuItem( PlanWorks.PROJECT_MENU, PlanWorks.DELETE_MENU_ITEM,
                                 helper, guiTest);
    // System.err.println("Found delete menu item: " + deleteItem);
    Assert.assertNotNull( "'Project->Delete' not found:", deleteItem);
    helper.enterClickAndLeave( new MouseEventData( guiTest, deleteItem));
    guiTest.flushAWT(); guiTest.awtSleep();

    PWTestHelper.handleDialog( "Delete Project", "OK", "", helper, guiTest);
  } // end deleteProject

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
    List dialogs = helper.getShowingDialogs( dialogName);
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
      if (foundMsg == null) {
        System.err.println( dialogName + " dialog does not contain '" + dialogMessage + "'");
      }
      Assert.assertNotNull( dialogName + " dialog does not contain '" + dialogMessage + "'",
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
    SequenceStepsView seqStepsView = null;
    JMenuItem sequenceItem =
      PWTestHelper.findMenuItem( PlanWorks.PLANSEQ_MENU, sequenceName, helper, guiTest);
    Assert.assertNotNull( "'" + PlanWorks.PLANSEQ_MENU + "->" + sequenceName + "' not found:",
                          sequenceItem);
    helper.enterClickAndLeave( new MouseEventData( guiTest, sequenceItem));
    guiTest.flushAWT(); guiTest.awtSleep();
    seqStepsView =
      (SequenceStepsView) PWTestHelper.findComponentByClass( SequenceStepsView.class);
    Assert.assertNotNull( "SequenceStepsView not found:", seqStepsView);
    return seqStepsView;
  }

  /**
   * <code>getViewMenuItem</code>
   *
   * @param viewMenuItemName - <code>String</code> - 
   * @param popupMenu - <code>PartialPlanViewMenu</code> - 
   * @return - <code>PartialPlanViewMenuItem</code> - 
   */
  public static PartialPlanViewMenuItem getPopupViewMenuItem( String viewMenuItemName,
                                                              PartialPlanViewMenu popupMenu) {
    PartialPlanViewMenuItem viewMenuItem = null;
    for (int i = 0, n = popupMenu.getComponentCount(); i < n; i++) {
      // System.err.println( "popup i " + i + " " +
      //                    popupMenu.getComponent( i).getClass().getName());
      if (popupMenu.getComponent(i) instanceof PartialPlanViewMenuItem) {
        if (((PartialPlanViewMenuItem) popupMenu.getComponent(i)).getText().equals
            ( viewMenuItemName)) {
          viewMenuItem = (PartialPlanViewMenuItem) popupMenu.getComponent(i);
          break;
        }
      }
    }
    return viewMenuItem;
  }

} // end abstract class PWTestHelper

    

