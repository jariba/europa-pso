// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: ModelRulesLayout.java,v 1.1 2003-12-12 01:23:06 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 05dec03
//

package gov.nasa.arc.planworks.viz.sequence.modelRules;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoDocument;

// PlanWorks/java/lib/JGo/JGoLayout.jar
import com.nwoods.jgo.layout.JGoLayeredDigraphAutoLayout;


/**
 * <code>ModelRulesLayout</code> - 
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *         NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class ModelRulesLayout extends JGoLayeredDigraphAutoLayout {

  private long startTimeMSecs;


  /**
   * <code>ModelRulesLayout</code> - constructor 
   *
   * @param jGoDocument - <code>JGoDocument</code> - 
   * @param startTimeMSecs - <code>long</code> - 
   */
  public ModelRulesLayout( JGoDocument jGoDocument, long startTimeMSecs) {
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
    // System.err.println( "ModelRulesLayout progress: " + progress);
    if (progress == 1.0) {
      long stopTimeMSecs = System.currentTimeMillis();
      System.err.println( "   ... elapsed time: " +
                          (stopTimeMSecs - startTimeMSecs) + " msecs.");
    }
  } // end progressUpdate

} // end class ModelRulesLayout
