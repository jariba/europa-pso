// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: TokenNetworkLayout.java,v 1.4 2004-03-27 00:30:46 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 15July03
//

package gov.nasa.arc.planworks.viz.partialPlan.tokenNetwork;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import javax.swing.undo.AbstractUndoableEdit;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoDocument;
import com.nwoods.jgo.JGoObject;

// PlanWorks/java/lib/JGo/JGoLayout.jar
import com.nwoods.jgo.layout.JGoLayeredDigraphAutoLayout;
import com.nwoods.jgo.layout.JGoNetworkLink;
import com.nwoods.jgo.layout.JGoNetworkNode;

import org.jgraph.JGraph;
import org.jgraph.graph.AbstractCellView;
import org.jgraph.graph.CellHandle;
import org.jgraph.graph.CellMapper;
import org.jgraph.graph.CellView;
import org.jgraph.graph.CellViewRenderer;
import org.jgraph.graph.ConnectionSet;
import org.jgraph.graph.GraphContext;
import org.jgraph.graph.GraphLayoutCache;
import org.jgraph.graph.GraphModel;
import org.jgraph.graph.ParentMap;
import org.jgraph.graph.ParentMap.Entry;
import org.jgraph.layout.RadialTreeLayoutAlgorithm;

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

  private Object[] rootNodes;
  private ParentMap parentMap;
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
   * @param parentMap - <code>ParentMap</code> - 
   * @param startTimeMSecs - <code>long</code> - 
   */
  public TokenNetworkLayout( JGoDocument jGoDocument, Object[] rootNodes,
                             ParentMap parentMap, long startTimeMSecs) {
    super( jGoDocument);
    this.rootNodes = rootNodes;
    this.parentMap = parentMap;
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


  // protected void layoutNodesAndLinks() {
  protected void layoutNodesAndLinksNotUsed() {
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

    // layoutNodes();
    // layoutLinks();
    System.err.println( "views");
    JGraph graph = new JGraph();
    GraphModel graphModel = graph.getModel();
    CellMapper mapper = new GraphLayoutCache( graphModel, graph);
    CellView[] views = new CellView [ nodeArray.length ];
    for (int i = 0, n = nodeArray.length; i < n; i++) {
      views[i] = new TokenCellView( nodeArray[i], graph, mapper);
      System.err.println( "TokenCellView " + views[i]);
    }
    ConnectionSet connectionSet = new ConnectionSet();
    connectionSet.addConnections( views);

    System.err.println( "rootNodes");
    // convert rootNodes & parentMap from TokenNode => TokenCellView
    Object[] convertedRootNodes = new Object [rootNodes.length];
    for (int i = 0, n = rootNodes.length; i < n; i++) {
      TokenNode tokenNode = (TokenNode) rootNodes[i];
      convertedRootNodes[i] =
        new TokenCellView( mapTokenNodeToNetworkNode( tokenNode, nodeArray),
                           graph, mapper);
      System.err.println( "TokenCellView " + convertedRootNodes[i]);
    }
    System.err.println( "ParentMap");
    ParentMap convertedParentMap = new ParentMap();
    Iterator parentItr = parentMap.entries();
    while (parentItr.hasNext()) {
      ParentMap.Entry entry = (ParentMap.Entry) parentItr.next();
      TokenNode childNode = (TokenNode) entry.getChild();
      TokenNode parentNode = (TokenNode) entry.getParent();
      TokenCellView childCell =
        new TokenCellView( mapTokenNodeToNetworkNode( childNode, nodeArray), graph, mapper);
      TokenCellView parentCell =
        new TokenCellView( mapTokenNodeToNetworkNode( parentNode, nodeArray), graph, mapper);
      System.err.println( "TokenCellView child " + childCell);
      System.err.println( "TokenCellView parent " + parentCell);
      convertedParentMap.addEntry(childCell, parentCell);
    }
    Map attributeMap = new HashMap();
    AbstractUndoableEdit[] edits = new AbstractUndoableEdit [] { };

    graphModel.insert( convertedRootNodes, attributeMap, connectionSet,
                       convertedParentMap, edits);

    boolean applyToAll = true;
    Properties properties = new Properties();
    properties.setProperty( KEY_WIDTH, "50.0");
    properties.setProperty( KEY_HEIGHT, "50.0");

    new RadialTreeLayoutAlgorithm().perform( graph, applyToAll, properties);
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


    
  public class TokenCellView extends AbstractCellView {

    private JGoObject jGoObject;

    public TokenCellView( JGoNetworkNode networkNode, JGraph graph, CellMapper mapper) {
      super( networkNode, graph, mapper);
      System.err.println( "TokenCellView.networkNode " + networkNode);
      jGoObject = networkNode.getJGoObject();
      System.err.println( "TokenCellView.jGoObject " + networkNode.getJGoObject());
    }

    public Rectangle2D getBounds() {
      return (Rectangle2D) jGoObject.getBoundingRect();
    }

    public CellHandle getHandle( GraphContext context) {
      return null;
    }

    public CellViewRenderer getRenderer() {
      return null;
    }

    public JGoObject getJGoObject() {
      return jGoObject;
    }

  } // end class TokenCellView




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
