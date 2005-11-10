//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: MDIDynamicMenuBar.java,v 1.18 2005-11-10 01:22:09 miatauro Exp $
//
package gov.nasa.arc.planworks.mdi;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.beans.VetoableChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.SequenceViewMenuItem;
import gov.nasa.arc.planworks.db.DbConstants;
import gov.nasa.arc.planworks.util.Utilities;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.ViewGenerics;


/**
 * <code>MDIDynamicMenuBar</code> -
 *                      JMenuBar->MDIDynamicMenuBar
 *                      MDIMenu->MDIDynamicMenuBar
 * A menubar with dynamic menus.  The menus are dependent on which 
 * MDIInternalFrame has focus.  The menubar also has constant menus, which 
 * are not dependent on the selected frame.
 */

public class MDIDynamicMenuBar extends JMenuBar implements MDIMenu {

  private static final int NUM_SEQUENCE_WINDOWS = 2;

  private ArrayList constantMenus = new ArrayList(3);
  private ArrayList windows = new ArrayList();
  private TileCascader tileCascader;
  private JMenu windowMenu;
  private JMenu pluginMenu;
  private JMenu helpMenu;
    /**
     * creates a new MDIDynamicMenuBar and registers itself with the MDIDesktop
     * @param desktop the MDIDesktop to which the MDIDynamicMenuBar is added.
     */
  public MDIDynamicMenuBar(final TileCascader tileCascader) {
    super();
    for(int i = 0; i < getMenuCount(); i++) {
      constantMenus.add(getMenu(i));
    }
    this.tileCascader = tileCascader;
    buildWindowMenu();
    buildPlugInMenu();
    buildHelpMenu();
    this.setVisible(true);
  }
  /**
   * Creates a new MDIDynamicMenuBar with a set of initial menus that may or may not be constant.
   * @param initialMenus An array of JMenus to display initially.
   * @param constant Determines whether or not the initialMenus are considered constant.
   */
  public MDIDynamicMenuBar(final JMenu [] initialMenus, final boolean constant, 
                           final TileCascader tileCascader) {
    super();
    for(int i = 0; i < initialMenus.length; i++) {
      if(constant) {
        constantMenus.add(initialMenus[i]);
      }
      add(initialMenus[i]);
    }
    this.tileCascader = tileCascader;
    buildWindowMenu();
    buildPlugInMenu();
    buildHelpMenu();
    this.setVisible(true);
  }
  /**
   * Creates a new MDIDynamicMenuBar with a set of initial constant menus and a set of initial
   * volatile menus.
   * @param constantMenus An array of JMenus to be treated as constant.
   * @param initialMenus An array of JMenus to be treated as volatile.
   */
  public MDIDynamicMenuBar(final JMenu [] constantMenus, final JMenu [] initialMenus, 
                           final TileCascader tileCascader) {
    super();
    this.constantMenus = new ArrayList(constantMenus.length);
    for(int i = 0; i < constantMenus.length; i++) {
      this.constantMenus.add(constantMenus[i]);
      this.add(constantMenus[i]);
    }
    if(initialMenus != null) {
      for(int i = 0; i < initialMenus.length; i++) {
        this.add(initialMenus[i]);
      }
    }
    this.tileCascader = tileCascader;
    buildWindowMenu();
    buildPlugInMenu();
    buildHelpMenu();
    this.setVisible(true);
  }
  /**
   * Removes all of the currently displayed JMenus, adds the constant menus back, then adds the
   * frame's associated menues.  Called when an MDIInternalFrame is selected.
   * @param frame the MDIFrame that was just selected.
   */
  public void notifyActivated(final MDIFrame frame) {
    removeAll();
    if(constantMenus != null) {
      for(int i = 0; i < constantMenus.size(); i++) {
        add((JMenu)constantMenus.get(i));
      }
    }
    JMenu [] temp = frame.getAssociatedMenus();
    if(temp != null) {
      for(int i = 0; i < temp.length; i++) {
        add(temp[i]);
      }
    }
    add(windowMenu);
    add( helpMenu);
    //repaint(getVisibleRect());
    validate();
  }
  /**
   * Adds a JMenu to the list of constant menus.
   * @param constantMenu The menu to be added.
   */
  public void addConstantMenu(final JMenu constantMenu) {
    if(constantMenus == null) {
      // constantMenus = new ArrayList(3);
      constantMenus = new ArrayList(4);
    }
    constantMenus.add(constantMenu);
    this.add(constantMenu);
  }
  /**
   * Adds an array of JMenus to the list of constant menus.
   * @param constantMenus The array of menus to be added.
   */
  public void addConstantMenus(final JMenu [] constantMenus) {
    if(this.constantMenus == null) {
      this.constantMenus = new ArrayList(constantMenus.length);
    }
    for(int i = 0; i < constantMenus.length; i++) {
      addConstantMenu(constantMenus[i]);
    }
  }
  /**
   * Adds a JMenu to the MDIDynamicMenuBar as a volatile menu.
   * @param menu The menu to be added.
   */
  public void addMenu(final JMenu menu) {
    this.add(menu);
  }
  /**
   * Adds an array of JMenus to the menu bar as volatile menus.
   * @param menus The menus to be added.
   */
  public void addMenus(final JMenu [] menus) {
    for(int i = 0; i < menus.length; i++) {
      this.add(menus[i]);
    }
  }
  public void remove(final Component c) {
    if(c == null) {
      return;
    }
    super.remove(c);
    if(constantMenus.contains(c)) {
      constantMenus.remove(constantMenus.indexOf(c));
    }
  }
  public void addWindow(final MDIInternalFrame frame) {
    windows.add(frame);
    buildWindowMenu();
    buildPlugInMenu();
    buildHelpMenu();
  }

  public JMenu getPlugInMenu() {
    return pluginMenu;
  }

  private void buildWindowMenu() {
    if(windowMenu != null) {
      windowMenu.removeAll();
      remove(windowMenu);
//     }
//     else {
//       windowMenu = new JMenu( PlanWorks.WINDOW_MENU);
    }
    JMenuItem tileItem = new JMenuItem( PlanWorks.TILE_WINDOWS_MENU_ITEM);
    JMenuItem cascadeItem = new JMenuItem( PlanWorks.CASCADE_WINDOWS_MENU_ITEM);
    tileItem.addActionListener(new TileActionListener(tileCascader));
    cascadeItem.addActionListener(new CascadeActionListener(tileCascader));
    windowMenu = new JMenu( PlanWorks.WINDOW_MENU);
    windowMenu.add(tileItem);
    windowMenu.add(cascadeItem);
    windowMenu.addSeparator();
    windowMenu.addSeparator();
    windowMenu.validate();

    ListIterator windowIterator = sortWindows( windows).listIterator();
    while(windowIterator.hasNext()) {
      Object object = (Object) windowIterator.next();
      if (object instanceof MDIInternalFrame) {
        MDIInternalFrame frame = (MDIInternalFrame) object;
        JMenuItem temp = new JMenuItem(frame.getTitle());
        temp.addActionListener(new SelectedActionListener(frame));
        windowMenu.add(temp);
      } else {
        windowMenu.addSeparator();
      }
    }
    if(windows.size() == 0) {
      windowMenu.setEnabled(false);
    }
    super.add(windowMenu);
    windowMenu.validate();
    validate();
  } // end buildWindowMenu

  private void buildHelpMenu() {
    if(helpMenu != null) {
      helpMenu.removeAll();
      remove( helpMenu);
    }
    JMenuItem shapesItem = new JMenuItem( PlanWorks.NODE_SHAPES_MENU_ITEM);
    shapesItem.addActionListener( new NodeShapesActionListener( shapesItem));
    helpMenu = new JMenu( PlanWorks.HELP_MENU);
    helpMenu.add( shapesItem);
    helpMenu.validate();
    super.add (helpMenu);
    helpMenu.validate();
    validate();
  } // end buildHelpMenu

  private void buildPlugInMenu() {
    if(pluginMenu != null) {
      pluginMenu.removeAll();
      remove( pluginMenu);
    }
    pluginMenu = new JMenu( PlanWorks.PLUGIN_MENU);
    super.add (pluginMenu);
    pluginMenu.validate();
    validate();
  } // end buildPlugInMenu

  private List sortWindows( List  windows) {
    if (windows.size() == 0) { return windows; }
    List itemSeqNames =  new ArrayList();
    List sortedWindows = new ArrayList();
//     for (int i = 0, n = windows.size(); i < n; i++) {
//       System.err.println( "windows i " + i + " " +
//                           ((MDIInternalFrame) windows.get( i)).getTitle());
//     }
    List planSeqNames = PlanWorks.getPlanWorks().getCurrentProject().listPlanningSequences();
    Collections.sort( planSeqNames, new SeqNameComparator());
    ListIterator seqNameItr = planSeqNames.listIterator();
    boolean isFirst = true;
    while (seqNameItr.hasNext()) {
      String seqName = getMenuItemSeqName( Utilities.getUrlLeaf( (String) seqNameItr.next()),
                                           itemSeqNames);
      itemSeqNames.add( seqName);
      // System.err.println( "seqName " + seqName);
      List windowsForSeq = getWindowsForSeq( seqName, windows);
      if (windowsForSeq.size() > 0) {
        if (isFirst) {
          isFirst = false;
        } else {
          sortedWindows.add( null); // new sequence marker for JMenu separator
          sortedWindows.add( null); // new sequence marker for JMenu separator
        }
        Collections.sort( windowsForSeq, new StepNumberComparator());
        int numSeqWindows = windowsForSeq.size();
        // pick off SequenceQuery & Sequence Steps view
        int windowsWithStepIndx = Math.min( NUM_SEQUENCE_WINDOWS, numSeqWindows);
        for (int i = 0, n = windowsWithStepIndx; i < n; i++) {
          sortedWindows.add ( (MDIInternalFrame) windowsForSeq.get( i));
        }

        sortedWindows = sortWindowsForSeqByView( seqName, windowsForSeq, sortedWindows,
                                                 windowsWithStepIndx);

      } // end if (windowsForSeq.size() > 0)
    } // end while (seqNameItr.
//     for (int i = 0, n = sortedWindows.size(); i < n; i++) {
//       Object object = (Object) sortedWindows.get( i);
//       if (object instanceof MDIInternalFrame) {
//         System.err.println( "sortedWindows i " + i + " " +
//                             ((MDIInternalFrame) sortedWindows.get( i)).getTitle());
//       } else {
//         System.err.println( "sortedWindows i " + i + " separator");
//       }
//     }
    return sortedWindows;
  } // end sortWindows

  private List getWindowsForSeq( String seqName, List windows) {
//     System.err.println( "\ngetWindowsForSeq seqName " + seqName);
    List windowsForSeq = new ArrayList();
    ListIterator windowsItr = windows.listIterator();
    while (windowsItr.hasNext()) {
      MDIInternalFrame frame = (MDIInternalFrame) windowsItr.next();
      String frameTitle = frame.getTitle();
//       System.err.println( " frameTitle " + frameTitle);
      int indx1 = frameTitle.indexOf( seqName);
      int indx2 = frameTitle.indexOf( (seqName + "/"));
      int indx3 = frameTitle.indexOf( (seqName + " - "));
      boolean passedTest = false;
      if ((indx1 >= 0) && (seqName.length() == frameTitle.substring( indx1).length())) {
//         System.err.println( "  passed test1");
        // SequenceQuery for Rover & SequenceQuery for Rover (1)
        passedTest = true;
      } else if (indx2 >= 0) {
//         System.err.println( "  passed test2");
        // ContentFilter for Rover/step20 & Rover (1)/step20 & NavigatorView for k9 (1)/step6 - 1
        passedTest = true;
      } else if (indx3 >= 0) {
//         System.err.println( "  passed test3");
        // QueryResults for Rover - 1 & QueryResults for Rover (1) - 1
        passedTest = true;
      }
      if (passedTest) {
        windowsForSeq.add( frame);
      }
    }
    return windowsForSeq;
  } // end getWindowsForSeq

      // QueryResults for Rover - 1

  private List sortWindowsForSeqByView( String seqName, List windowsForSeq, List sortedWindows,
                                        int windowsWithStepIndx) {
    int numSeqWindows = windowsForSeq.size();
    String currentStepStr = "";
    int currentIndx = windowsWithStepIndx, windowCnt = NUM_SEQUENCE_WINDOWS;
    boolean isFirst1 = true;
    while (windowCnt < numSeqWindows) {
//           System.err.println( "currentIndx " + currentIndx + " numSeqWindows " + numSeqWindows);
      List viewsAtStep = new ArrayList();
      for (int i = currentIndx; i < numSeqWindows; i++) {
        MDIInternalFrame frame = (MDIInternalFrame) windowsForSeq.get( i);
        String frameTitle = frame.getTitle();
        //String stepStr = ViewConstants.SEQUENCE_QUERY_RESULTS_TITLE;
	String stepStr = "";
        int indx1 = frameTitle.indexOf( "step");
        if (indx1 > 0) {
          stepStr = frameTitle.substring( indx1 + 4);
          int indx2 = stepStr.indexOf( " ");
          if (indx2 > 0) {
            stepStr = stepStr.substring( 0, indx2);
          }
        }
//             System.err.println( "frameTitle " + frameTitle + " stepStr '" + stepStr +
//                                 "' currentStepStr '" + currentStepStr + "'" + " isFirst1 " +
//                                 isFirst1);
        if (! stepStr.equals( currentStepStr)) {
          currentStepStr = stepStr;
          if (isFirst1) {
            isFirst1 = false;
          } else {
            currentIndx = i;
            break;
          }
        }
        viewsAtStep.add( frame);
        windowCnt++;
      } // end for
//           System.err.println( "viewsAtStep.size() " + viewsAtStep.size());
      sortedWindows.add( null); // new step marker for JMenu separator
      Collections.sort( viewsAtStep, new ViewNameComparator( seqName));
      for (int i = 0, n = viewsAtStep.size(); i < n; i++) {
        sortedWindows.add ( (MDIInternalFrame) viewsAtStep.get( i));
      }
    } // end while
    return sortedWindows;
  } // end sortWindowsForSeqByView

  private String getMenuItemSeqName( String seqName, List itemSeqNames) {
      int nameCount = 0;
    // System.err.println( "getMenuItemSeqName: seqName " + seqName);
    // check for e.g. monkey1066690986042
    String newSeqName = seqName.substring( 0, seqName.length() - DbConstants.LONG_INT_LENGTH);
    for (int i = 0; i < itemSeqNames.size(); i++) {
      String itemSeqName = (String) itemSeqNames.get( i);
      int index = itemSeqName.indexOf(" (");
      if (index != -1) {
        itemSeqName = itemSeqName.substring( 0, index);
      }
      if (itemSeqName.equals( newSeqName)) {
        nameCount++;
      }
    }
    if (nameCount > 0) {
      newSeqName = newSeqName.concat(" (").concat( Integer.toString( nameCount)).concat(")");
    }
    // System.err.println( "   newSeqName " + newSeqName);
    return newSeqName;
  } // end getMenuItemSeqName


   private class SeqNameComparator implements Comparator {
    public SeqNameComparator() {
    }
    public final int compare( final Object o1, final Object o2) {
      String s1 = Utilities.getUrlLeaf((String) o1);
      String s2 = Utilities.getUrlLeaf((String) o2);
      return s1.compareTo(s2);
    }
    public final boolean equals( final Object o1, final Object o2) {
      String s1 = Utilities.getUrlLeaf((String)o1);
      String s2 = Utilities.getUrlLeaf((String)o2);
      return s1.equals(s2);
    }
  }
 

  private class StepNumberComparator implements Comparator {
    public StepNumberComparator() {
    }
    public final int compare( final Object o1, final Object o2) {
      Integer stepNumberInt1 = getStepNumberInt( ((MDIInternalFrame) o1).getTitle());
      Integer stepNumberInt2 = getStepNumberInt( ((MDIInternalFrame) o2).getTitle());
      return stepNumberInt1.compareTo( stepNumberInt2);
    }
    public final boolean equals( final Object o1, final Object o2) {
      Integer stepNumberInt1 = getStepNumberInt( ((MDIInternalFrame) o1).getTitle());
      Integer stepNumberInt2 = getStepNumberInt( ((MDIInternalFrame) o2).getTitle());
      return stepNumberInt1.equals( stepNumberInt2);
    }

    private Integer getStepNumberInt( String title) {
      int stepNumber = -1;
      int indx = title.indexOf( "/step");
      if (indx >= 0) {
        String stepNumberStr = title.substring( indx + 5);
        int indx2 = stepNumberStr.indexOf( " ");
        if (indx2 > 0) {
          stepNumberStr = stepNumberStr.substring( 0, indx2);
        }
        stepNumber = Integer.parseInt( stepNumberStr);
      }
      return new Integer( stepNumber);
    } // end getStepNumberInt

  } // end class StepNumberComparator


  private class ViewNameComparator implements Comparator {
    private String planName;
    public ViewNameComparator( String planName) {
      this.planName = planName;
    }
    public final int compare( final Object o1, final Object o2) {
      String viewName1 = getViewName( ((MDIInternalFrame) o1).getTitle());
      String viewName2 = getViewName( ((MDIInternalFrame) o2).getTitle());
      return viewName1.compareTo( viewName2);
    }
    public final boolean equals( final Object o1, final Object o2) {
      String viewName1 = getViewName( ((MDIInternalFrame) o1).getTitle());
      String viewName2 = getViewName( ((MDIInternalFrame) o2).getTitle());
      return viewName1.equals( viewName2);
    }

    private String getViewName( String title) {
      String viewName = "";
      int indx = title.indexOf( planName);
      if (indx >= 0) {
        viewName = title.substring( 0, indx);
      }
      return viewName;
    } // end getViewName

  } // end class StepNumberComparator


  public void notifyDeleted(final MDIFrame frame) {
    windows.remove(frame);
    buildWindowMenu();
    buildPlugInMenu();
    buildHelpMenu();
  }

  public void add(final JButton button){
  }

  public JMenu add(final JMenu menu) {
    remove(windowMenu);
    remove(pluginMenu);
    remove(helpMenu);
    super.add(menu);
    if(windowMenu != null) {
      super.add(windowMenu);
    }
    if(pluginMenu != null) {
      super.add(pluginMenu);
    }
    if(helpMenu != null) {
      super.add(helpMenu);
    }
    validate();
    return menu;
  }

  /**
   * <code>clearMenu</code>
   *
   * @param menuName - <code>String</code> - 
   * @return - <code>JMenu</code> - 
   */
  public JMenu clearMenu( final String menuName, final int numProjects) {
    // clear out previous project's menu contents and return menu root
    for (int i = 0, n = getMenuCount(); i < n; i++) {
      if (((JMenu) getMenu( i)).getText().equals( menuName)) {
        JMenu menu = (JMenu) getMenu( i);
        if (numProjects == 0) {
          remove( menu);
          menu = null;
        } else {
          menu.removeAll();
        }
        validate();
        repaint();
        return menu;
      }
    }
    return null;
  } // end clearMenu


  /**
   * <code>disableMenu</code>
   *
   * @param menuName - <code>String</code> - 
   * @return - <code>JMenu</code> - 
   */
  public JMenu disableMenu( final String menuName) {
    JMenu menu = null;
    for (int i = 0; i < getMenuCount(); i++) {
      if (getMenu(i) != null &&
         getMenu(i).getText().equals( menuName)) {
        menu = getMenu(i);
      }
    }
    if(menu != null) {
      menu.setEnabled(false);
    }
    return menu;
  } // end disableMenu


  /**
   * <code>enableMenu</code>
   *
   * @param menu - <code>JMenu</code> - 
   */
  public void enableMenu( final JMenu menu) {
    if (menu != null && menu.getItemCount() != 0) {
      menu.setEnabled(true);
    }
  } // end enableMenu

  /**
   * <code>getPlanSeqItem</code>
   *
   * @param seqUrl - <code>String</code> - 
   * @return - <code>JMenuItem</code> - 
   */
  public JMenuItem getPlanSeqItem( String seqUrl) {
    for (int i = 0, n = getMenuCount(); i < n; i++) {
      if (((JMenu) getMenu( i)).getText().equals( PlanWorks.PLANSEQ_MENU)) {
        JMenu menu = (JMenu) getMenu( i);
        for (int j = 0, m = menu.getItemCount(); j < m; j++) {
          JMenuItem menuItem = (JMenuItem) menu.getItem( j);
          if (menuItem instanceof SequenceViewMenuItem) {
            if (((SequenceViewMenuItem) menuItem).getSeqUrl().equals( seqUrl)) {
              return menuItem;
            }
          }
        }
      }
    }
    return null;
  } // end getPlanSeqItem



} // end class MDIDynamicMenuBar


class SelectedActionListener implements ActionListener {
  private MDIInternalFrame frame;
  public SelectedActionListener(MDIInternalFrame frame) {
    this.frame = frame;
  }
  public void actionPerformed(ActionEvent e) {
    try {
      frame.setIcon(false);
      frame.setSelected(true);
    }
    catch(Exception f){}
  }
} // end class SelectedActionListener

class TileActionListener implements ActionListener {
  private TileCascader tiler;
  public TileActionListener(TileCascader tiler) {
    this.tiler = tiler;
  }
  public void actionPerformed(ActionEvent e) {
    tiler.tileWindows();
  }
} // end class TileActionListener


class CascadeActionListener implements ActionListener {
  private TileCascader cascader;
  public CascadeActionListener(TileCascader cascader) {
    this.cascader = cascader;
  }
  public void actionPerformed(ActionEvent e) {
    cascader.cascadeWindows();
  }
} // end class CascadeActionListener

class NodeShapesActionListener implements ActionListener {
  private JMenuItem menuItem;
  public NodeShapesActionListener( JMenuItem menuItem) {
    this.menuItem = menuItem;
  }
  public void actionPerformed( ActionEvent evt) {
    menuItem.setEnabled( false);
    ViewGenerics.openNodeShapesView( menuItem);
  }
} // end NodeShapesActionListener





