// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: TokenNetworkLayout.java,v 1.8 2004-08-05 00:24:30 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 15July03
//

package gov.nasa.arc.planworks.viz.partialPlan.tokenNetwork;

import java.util.List;
// import java.util.Properties;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoDocument;

// PlanWorks/java/lib/JGo/JGoLayout.jar
import com.nwoods.jgo.layout.JGoLayeredDigraphAutoLayout;
import com.nwoods.jgo.layout.JGoNetworkLink;
import com.nwoods.jgo.layout.JGoNetworkNode;

import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.nodes.TokenNode;


/**
 * <code>TokenNetworkLayout</code> - 
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *         NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class TokenNetworkLayout extends JGoLayeredDigraphAutoLayout {

  private static final String KEY_WIDTH = "Width";
  private static final String KEY_HEIGHT = "Height";

  private List rootNodes;

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
    // setInitializeOption( JGoLayeredDigraphAutoLayout.LD_INITIALIZE_NAIVE);
    setInitializeOption( JGoLayeredDigraphAutoLayout.LD_INITIALIZE_DFSIN);
    setLayeringOption( JGoLayeredDigraphAutoLayout.LD_LAYERING_OPTIMALLINKLENGTH);
    
    // int NlayerSpacing, int NcolumnSpacing, int NdirectionOption, int NcycleremoveOption,
    //   int NlayeringOption, int NinitializeOption, int Niterations, int NaggressiveOption) 

  } // end constructor


  /**
   * <code>TokenNetworkLayout</code> - constructor 
   *
   * @param jGoDocument - <code>JGoDocument</code> - 
   * @param rootNodes - <code>Object[]</code> - 
   * @param startTimeMSecs - <code>long</code> - 
   */
  public TokenNetworkLayout( JGoDocument jGoDocument, List rootNodes, long startTimeMSecs) {
    super( jGoDocument);
    this.rootNodes = rootNodes;
    this.startTimeMSecs = startTimeMSecs;
    setDirectionOption( JGoLayeredDigraphAutoLayout.LD_DIRECTION_DOWN);
    setColumnSpacing( getColumnSpacing() / 4);
    setLayerSpacing( getLayerSpacing() / 4);
    setCycleRemoveOption( JGoLayeredDigraphAutoLayout.LD_CYCLEREMOVE_DFS);
    // setInitializeOption( JGoLayeredDigraphAutoLayout.LD_INITIALIZE_NAIVE);
    setInitializeOption( JGoLayeredDigraphAutoLayout.LD_INITIALIZE_DFSIN);
    setLayeringOption( JGoLayeredDigraphAutoLayout.LD_LAYERING_OPTIMALLINKLENGTH);
    
    // int NlayerSpacing, int NcolumnSpacing, int NdirectionOption, int NcycleremoveOption,
    //   int NlayeringOption, int NinitializeOption, int Niterations, int NaggressiveOption) 

  } // end constructor


   protected void layoutNodesAndLinks() {
    JGoNetworkNode[] nodeArray = getNetwork().getNodeArray();
    JGoNetworkLink[] linkArray = getNetwork().getLinkArray();

//     System.err.println( "nodeArray");
//     for (int i = 0, n = nodeArray.length; i < n; i++) {
//       System.err.println( "  " + nodeArray[i].getJGoObject().getClass().getName());
//     }
//     System.err.println( "linkArray");
//     for (int i = 0, n = linkArray.length; i < n; i++) {
//       System.err.println( "  " + linkArray[i].getJGoObject());
//     }

    layoutNodes();
    layoutLinks();

//     // convert rootNodes 
//     List convertedRootNodes = new ArrayList();
//     for (int i = 0, n = rootNodes.size(); i < n; i++) {
//       TokenNode tokenNode = (TokenNode) rootNodes.get( i);
//       convertedRootNodes.add( mapTokenNodeToNetworkNode( tokenNode, nodeArray));
//     }

//     Properties properties = new Properties();
//     properties.setProperty( KEY_WIDTH, "500.0");
//     properties.setProperty( KEY_HEIGHT, "500.0");

//     new RadialTreeLayoutAlgorithm().perform( convertedRootNodes, nodeArray, properties);

  } // end layoutNodesAndLinks

  private JGoNetworkNode mapTokenNodeToNetworkNode( TokenNode tokenNode,
                                                    JGoNetworkNode[] nodeArray) {
    for (int j = 0, m = nodeArray.length; j < m; j++) {
      if (((TokenNode) ((JGoNetworkNode) nodeArray[j]).getJGoObject()).
          getToken().getId().equals( tokenNode.getToken().getId())) {
        return (JGoNetworkNode) nodeArray[j];
      }
    }
    System.err.println( "TokenNetworkLayout.mapTokenNodeToNetworkNode " +
                        "token Node " + tokenNode.getToken().getId() +
                        " not in network");
    return null;
  }



//   /**
//    * <code>progressUpdate</code>
//    *
//    * @param progress - <code>double</code> - 
//    */
//   public void progressUpdate( double progress) {
//     // System.err.println( "TokenNetworkLayout progress: " + progress);
//     if (progress == 1.0) {
//       long stopTimeMSecs = System.currentTimeMillis();
//       System.err.println( "   ... " + ViewConstants.TOKEN_NETWORK_VIEW + " elapsed time: " +
//                           (stopTimeMSecs - startTimeMSecs) + " msecs.");
//     }
//   } // end progressUpdate

} // end class TokenNetworkLayout
