// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: LayeredDigraphAutoLayout.java,v 1.1 2003-07-16 01:15:43 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 15July03
//

package gov.nasa.arc.planworks.viz.views.tokenNetwork;

import java.util.Date;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoDocument;
import com.nwoods.jgo.JGoLayer;
import com.nwoods.jgo.JGoView;
import com.nwoods.jgo.layout.JGoLayeredDigraphAutoLayout;


/**
 * <code>LayeredDigraphAutoLayout</code> - 
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *         NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class LayeredDigraphAutoLayout extends JGoLayeredDigraphAutoLayout {

  private long startTimeMSecs;


  public LayeredDigraphAutoLayout( JGoDocument jGoDocument, long startTimeMSecs) {
    super( jGoDocument);
    this.startTimeMSecs = startTimeMSecs;
    setDirectionOption( JGoLayeredDigraphAutoLayout.LD_DIRECTION_DOWN);
    setColumnSpacing( getColumnSpacing() / 4);
    setLayerSpacing( getLayerSpacing() / 4);
    setCycleRemoveOption( JGoLayeredDigraphAutoLayout.LD_CYCLEREMOVE_GREEDY);
    setInitializeOption( JGoLayeredDigraphAutoLayout.LD_INITIALIZE_NAIVE);

    // int NlayerSpacing, int NcolumnSpacing, int NdirectionOption, int NcycleremoveOption, int NlayeringOption, int NinitializeOption, int Niterations, int NaggressiveOption) 

  } // end constructor


  public void progressUpdate( double progress) {
    System.err.println( "LayeredDigraphAutoLayout progress: " + progress);
    if (progress == 1.0) {
      long stopTimeMSecs = (new Date()).getTime();
      System.err.println( "   ... elapsed time: " +
                          (stopTimeMSecs - startTimeMSecs) + " msecs.");
    }
  } // end progressUpdate

} // end class LayeredDigraphAutoLayout
