//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: ContentSpecWindow.java,v 1.1 2003-10-02 17:33:41 taylor Exp $
//
package gov.nasa.arc.planworks.viz.viewMgr.contentSpecWindow.sequence;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import gov.nasa.arc.planworks.db.util.ContentSpec;
import gov.nasa.arc.planworks.db.util.SequenceContentSpec;
import gov.nasa.arc.planworks.mdi.MDIInternalFrame;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanViewSet;

/**
 * <code>ContentSpecWindow</code> -
 *                      JPanel->ContentSpecWindow
 * @author <a href="mailto:miatauro@email.arc.nasa.gov">Michael Iatauro</a>
 * The complete, displayable window for defining a content specification for the associated 
 * partial plan.  Provides input fields for specifying timelines, constraints, predicates,
 * variables by type, and time intervals.
 */

public class ContentSpecWindow extends JPanel {

  protected SequenceContentSpec contentSpec;

  private static boolean queryTestExists;
  /**
   * <code>ContentSpecWindow
   * Constructs the entire content specification window.
   * @param window The <code>MDIInternalFrame</code> to which this pane is added.  Used for the 
   *               <code>pack()</code> method.
   * @param contentSpec The ContentSpec with which this window is associated.  Instantiated in
   *                    ViewSet
   */
  public ContentSpecWindow(MDIInternalFrame window, ContentSpec contentSpec) {
    this.contentSpec = (SequenceContentSpec) contentSpec;
    queryTestExists = false;

    
    GridBagLayout gridBag = new GridBagLayout();
    GridBagConstraints c = new GridBagConstraints();
    setLayout(gridBag);
    
    c.weightx = 0;
    c.weighty = 0;
    c.gridx = 0;
    c.gridy = 0;


    GridBagLayout buttonGridBag = new GridBagLayout();
    GridBagConstraints buttonConstraints = new GridBagConstraints();
    JPanel buttonPanel = new JPanel(buttonGridBag);

    buttonConstraints.weightx = 0;
    buttonConstraints.weighty = 0;
    buttonConstraints.gridx = 0;
    buttonConstraints.gridy = 0;

    JButton valueButton = new JButton("Apply Spec");
    valueButton.addActionListener(new SpecButtonListener(this));
    buttonGridBag.setConstraints(valueButton, buttonConstraints);
    buttonPanel.add(valueButton);

    JButton resetButton = new JButton("Reset Spec");
    resetButton.addActionListener(new SpecButtonListener(this));
    buttonConstraints.gridx++;
    buttonGridBag.setConstraints(resetButton, buttonConstraints);
    buttonPanel.add(resetButton);
    
    //c.gridx++;
    c.gridy++;
    gridBag.setConstraints(buttonPanel, c);
    add(buttonPanel);
    buildFromSpec();
  }

  private void buildFromSpec() {
    List currentSpec = contentSpec.getCurrentSpec();
    if(currentSpec.size() == 0) {
      return;
    }
  }

  /**
   * <code>SpecButtonListener</code> -
   *                       ActionListener->SpecButtonListener
   * The listener that provides the apply and reset functionality for the buttons.
   */
  class SpecButtonListener implements ActionListener {
    private ContentSpecWindow specWindow;
    public SpecButtonListener(ContentSpecWindow specWindow) {
      this.specWindow = specWindow;
    }
    public void actionPerformed(ActionEvent ae) {
      if(ae.getActionCommand().equals("Apply Spec")) {
        StringBuffer output = new StringBuffer();
        System.err.println("Applying Specification...");
        try {
          List specList = new ArrayList();
          specWindow.contentSpec.applySpec(specList);
        }
        catch(Exception e){
          System.err.println(e);
          e.printStackTrace();
        }
        System.err.println("Done applying Specification.");
        specWindow.contentSpec.printSpec();
      }
      else if(ae.getActionCommand().equals("Reset Spec")) {
        
        try{
          specWindow.contentSpec.resetSpec();
        }catch(Exception e){}
      }
    }
  }
}
