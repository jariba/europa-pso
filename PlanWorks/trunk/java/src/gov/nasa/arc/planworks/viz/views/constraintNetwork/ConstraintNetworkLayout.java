// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: ConstraintNetworkLayout.java,v 1.1 2003-08-06 01:20:15 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 28July03
//

package gov.nasa.arc.planworks.viz.views.constraintNetwork;

import java.awt.Cursor;
import java.util.Date;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoDocument;
import com.nwoods.jgo.layout.JGoLayeredDigraphAutoLayout;

import gov.nasa.arc.planworks.PlanWorks;

/**
 * <code>ConstraintNetworkLayout</code> - subclass JGoLayeredDigraphAutoLayout
 *               to layout constraint network
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *         NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class ConstraintNetworkLayout extends JGoLayeredDigraphAutoLayout {

  private long startTimeMSecs;


  /**
   * <code>ConstraintNetworkLayout</code> - constructor 
   *
   * @param jGoDocument - <code>JGoDocument</code> - 
   * @param startTimeMSecs - <code>long</code> - 
   */
  public ConstraintNetworkLayout( JGoDocument jGoDocument, long startTimeMSecs) {
    super( jGoDocument);
    this.startTimeMSecs = startTimeMSecs;
    setDirectionOption( JGoLayeredDigraphAutoLayout.LD_DIRECTION_DOWN);
    setColumnSpacing( getColumnSpacing() / 4);
    setLayerSpacing( getLayerSpacing() / 2);
    setCycleRemoveOption( JGoLayeredDigraphAutoLayout.LD_CYCLEREMOVE_DFS);
    setInitializeOption( JGoLayeredDigraphAutoLayout.LD_INITIALIZE_DFSIN);
    // setLayeringOption( JGoLayeredDigraphAutoLayout.LD_LAYERING_LONGESTPATHSINK);
    setLayeringOption( JGoLayeredDigraphAutoLayout.LD_LAYERING_LONGESTPATHSOURCE);

    // int NlayerSpacing, int NcolumnSpacing, int NdirectionOption, int NcycleremoveOption, int NlayeringOption, int NinitializeOption, int Niterations, int NaggressiveOption) 

  } // end constructor


  /**
   * <code>progressUpdate</code>
   *
   * @param progress - <code>double</code> - 
   */
  public void progressUpdate( double progress) {
    System.err.println( "ConstraintNetworkLayout progress: " + progress);
    if (progress == 1.0) {
      long stopTimeMSecs = (new Date()).getTime();
      System.err.println( "   ... elapsed time: " +
                          (stopTimeMSecs - startTimeMSecs) + " msecs.");
      PlanWorks.planWorks.getGlassPane().setCursor( new Cursor( Cursor.DEFAULT_CURSOR));

    }
  } // end progressUpdate

} // end class ConstraintNetworkLayout
