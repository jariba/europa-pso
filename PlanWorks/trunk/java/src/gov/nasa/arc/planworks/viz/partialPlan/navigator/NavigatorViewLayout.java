// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: NavigatorViewLayout.java,v 1.2 2004-08-16 22:01:01 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 06jan04
//

package gov.nasa.arc.planworks.viz.partialPlan.navigator;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoDocument;

// PlanWorks/java/lib/JGo/JGoLayout.jar
import com.nwoods.jgo.layout.JGoLayeredDigraphAutoLayout;
import com.nwoods.jgo.layout.JGoNetwork;


/**
 * <code>NavigatorViewLayout</code> - 
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *         NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class NavigatorViewLayout extends JGoLayeredDigraphAutoLayout {

  private long startTimeMSecs;

  /**
   * <code>ConstraintNetworkLayout</code> - constructor 
   *
   * @param jGoDocument - <code>JGoDocument</code> - 
   * @param startTimeMSecs - <code>long</code> - 
   */
  public NavigatorViewLayout( JGoDocument jGoDocument, long startTimeMSecs) {
    super( jGoDocument);
    this.startTimeMSecs = startTimeMSecs;
    setDirectionOption( JGoLayeredDigraphAutoLayout.LD_DIRECTION_DOWN);
    setColumnSpacing( getColumnSpacing() / 4);
    setLayerSpacing( getLayerSpacing() / 2);
    setCycleRemoveOption( JGoLayeredDigraphAutoLayout.LD_CYCLEREMOVE_DFS);
    setInitializeOption( JGoLayeredDigraphAutoLayout.LD_INITIALIZE_DFSIN);
    // setLayeringOption( JGoLayeredDigraphAutoLayout.LD_LAYERING_LONGESTPATHSINK);
    // setLayeringOption( JGoLayeredDigraphAutoLayout.LD_LAYERING_LONGESTPATHSOURCE);
    setLayeringOption( JGoLayeredDigraphAutoLayout.LD_LAYERING_OPTIMALLINKLENGTH);
    setAggressiveOption( JGoLayeredDigraphAutoLayout.LD_AGGRESSIVE_FALSE);
    setIterations( 4);

    // int NlayerSpacing, int NcolumnSpacing, int NdirectionOption, int NcycleremoveOption,
    //    int NlayeringOption, int NinitializeOption, int Niterations, int NaggressiveOption) 

  } // end constructor

  // preformance variations
//   protected void removeCycles() {
//     // no cycles to remove
//   }

//   protected void  reduceCrossings() {  // 5% faster
//   }

//   protected void  straightenAndPack() { // 10% slower
//   }

//   protected void layoutNodesAndLinks() { // very fast, but user must drag nodes to improve it
//   }

  /**
   * <code>progressUpdate</code>
   *
   * @param progress - <code>double</code> - 
   */
//   public void progressUpdate( double progress) {
//     // System.err.println( "NavigatorViewLayout progress: " + progress);
//     if (progress == 1.0) {
//       long stopTimeMSecs = System.currentTimeMillis();
//       System.err.println( "   ... elapsed time: " +
//                           (stopTimeMSecs - startTimeMSecs) + " msecs.");
//     }
//   } // end progressUpdate

} // end class NavigatorViewLayout
