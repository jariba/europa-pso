// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: ResourceNameNode.java,v 1.1 2004-02-10 02:35:53 taylor Exp $
//
// PlanWorks
//
// Will Taylor -- started 04feb04
//

package gov.nasa.arc.planworks.viz.nodes;

import java.awt.Point;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoObject;
import com.nwoods.jgo.JGoText;
import com.nwoods.jgo.JGoView;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.PwResource;
import gov.nasa.arc.planworks.util.MouseEventOSX;


public class ResourceNameNode extends JGoText {

  private PwResource resource;


  public ResourceNameNode( final Point nameLoc, final PwResource resource) {
    super( nameLoc, resource.getName());
    this.resource = resource;
  }

  /**
   * <code>getToolTipText</code>
   *
   * @return - <code>String</code> - 
   */
  public final String getToolTipText() {
    StringBuffer tip = new StringBuffer( "<html>key = ");
    tip.append( resource.getId().toString());
    tip.append( "</html>");
    return tip.toString();
  } // end getToolTipText

    /**
     * <code>doMouseClick</code> - 
     *
     * @param modifiers - <code>int</code> - 
     * @param docCoords - <code>Point</code> - 
     * @param viewCoords - <code>Point</code> - 
     * @param view - <code>JGoView</code> - 
     * @return - <code>boolean</code> - 
     */
  public final boolean doMouseClick( final int modifiers, final Point docCoords,
                                     final Point viewCoords, final JGoView view) {
    JGoObject obj = view.pickDocObject( docCoords, false);
    //         System.err.println( "doMouseClick obj class " +
    //                             obj.getTopLevelObject().getClass().getName());
    ResourceNameNode resourceNameNode = (ResourceNameNode) obj.getTopLevelObject();
    if (MouseEventOSX.isMouseLeftClick( modifiers, PlanWorks.isMacOSX())) {
      // do nothing
    } else if (MouseEventOSX.isMouseRightClick( modifiers, PlanWorks.isMacOSX())) {
      //       mouseRightPopupMenu( viewCoords);
      //       return true;
    }
    return false;
  } // end doMouseClick   

} // end class ResourceNameNode
