//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: TokenTypeBox.java,v 1.2 2004-07-13 23:54:10 taylor Exp $
//
package gov.nasa.arc.planworks.viz.viewMgr.contentSpecWindow.partialPlan;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

public class TokenTypeBox extends JPanel {
  private JLabel view;
  private JRadioButton all;
  private JRadioButton slotted;
  private JRadioButton free;
  private JLabel tokens;
  public TokenTypeBox() {
    GridBagLayout gridBag = new GridBagLayout();
    GridBagConstraints c = new GridBagConstraints();
    ButtonGroup buttonGroup = new ButtonGroup();
    setLayout(gridBag);

    view = new JLabel("View");
    c.weightx = 1;
    c.gridx = 0;
    c.gridy = 0;
    gridBag.setConstraints(view, c);
    add(view);
    
    all = new JRadioButton("all", true);
    c.gridx++;
    gridBag.setConstraints(all, c);
    add(all);
    buttonGroup.add(all);
    
    slotted = new JRadioButton("slotted", false);
    c.gridx++;
    gridBag.setConstraints(slotted, c);
    add(slotted);
    buttonGroup.add(slotted);
    
    free = new JRadioButton("free", false);
    c.gridx++;
    gridBag.setConstraints(free, c);
    add(free);
    buttonGroup.add(free);

    tokens = new JLabel("tokens");
    c.gridx++;
    gridBag.setConstraints(tokens, c);
    add(tokens);
  }
  public int getValue() {
    if(all.isSelected()) {
      return 1;
    }
    else if(slotted.isSelected()) {
      return 0;
    }
    else if(free.isSelected()) {
      return -1;
    }
    return -2;
  }

  public void reset() {
    all.setSelected(true);
  }

  public JRadioButton getSlottedButton() {
    return slotted;
  }
  
  public JRadioButton getFreeTokensButton() {
    return free;
  }
  
}
