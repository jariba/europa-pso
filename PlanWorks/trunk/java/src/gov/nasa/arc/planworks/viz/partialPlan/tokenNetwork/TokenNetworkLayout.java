// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: TokenNetworkLayout.java,v 1.2 2003-09-30 19:18:56 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 15July03
//

package gov.nasa.arc.planworks.viz.partialPlan.tokenNetwork;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoDocument;

// PlanWorks/java/lib/JGo/JGoLayout.jar
import com.nwoods.jgo.layout.JGoLayeredDigraphAutoLayout;


/**
 * <code>TokenNetworkLayout</code> - 
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *         NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class TokenNetworkLayout extends JGoLayeredDigraphAutoLayout {

  private long startTimeMSecs;


  /**
   * <code>TokenNetworkLayout</code> - constructor 
   *
   * @param jGoDocument - <code>JGoDocument</code> - 
   * @param startTimeMSecs - <code>long</code> - 
   */
  public TokenNetworkLayout( JGoDocument jGoDocument, long startTimeMSecs) {
    super( jGoDocument);
    this.startTimeMSecs = startTimeMSecs;
    setDirectionOption( JGoLayeredDigraphAutoLayout.LD_DIRECTION_DOWN);
    setColumnSpacing( getColumnSpacing() / 4);
    setLayerSpacing( getLayerSpacing() / 4);
    setCycleRemoveOption( JGoLayeredDigraphAutoLayout.LD_CYCLEREMOVE_DFS);
    setInitializeOption( JGoLayeredDigraphAutoLayout.LD_INITIALIZE_NAIVE);

    // int NlayerSpacing, int NcolumnSpacing, int NdirectionOption, int NcycleremoveOption,
    //   int NlayeringOption, int NinitializeOption, int Niterations, int NaggressiveOption) 

  } // end constructor


  /**
   * <code>progressUpdate</code>
   *
   * @param progress - <code>double</code> - 
   */
  public void progressUpdate( double progress) {
    // System.err.println( "TokenNetworkLayout progress: " + progress);
    if (progress == 1.0) {
      long stopTimeMSecs = System.currentTimeMillis();
      System.err.println( "   ... elapsed time: " +
                          (stopTimeMSecs - startTimeMSecs) + " msecs.");
    }
  } // end progressUpdate

} // end class TokenNetworkLayout
