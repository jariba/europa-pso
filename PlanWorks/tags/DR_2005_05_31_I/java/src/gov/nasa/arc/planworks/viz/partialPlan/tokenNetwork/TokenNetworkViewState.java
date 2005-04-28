// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: TokenNetworkViewState.java,v 1.1 2004-08-05 00:24:31 taylor Exp $
//
package gov.nasa.arc.planworks.viz.partialPlan.tokenNetwork;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import gov.nasa.arc.planworks.viz.nodes.BasicNodeLink;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanViewState;

public class TokenNetworkViewState extends PartialPlanViewState {

  private List modTokens;
  private List modRuleInstances;
  private List modLinks;

  public TokenNetworkViewState(TokenNetworkView view) {
    super(view);
    modTokens = new LinkedList();
    modRuleInstances = new LinkedList();
    modLinks = new LinkedList();

    ListIterator nodeIterator = view.getTokenNodeKeyList().listIterator();
    while (nodeIterator.hasNext()) {
      TokenNetworkTokenNode node =
        (TokenNetworkTokenNode) view.getTokenNode( (Integer) nodeIterator.next());
      if (node.inLayout()) {
        // System.err.println( "TokenNetworkViewState: tokenId " + node.getToken().getId());
        modTokens.add( new ModNode( node.getToken().getId(), node.areNeighborsShown(),
                                    node.getLinkCount()));
      }
    }

    nodeIterator = view.getRuleInstanceNodeKeyList().listIterator();
    while (nodeIterator.hasNext()) {
      TokenNetworkRuleInstanceNode node =
        (TokenNetworkRuleInstanceNode) view.getRuleInstanceNode( (Integer) nodeIterator.next());
      if (node.inLayout()) {
        // System.err.println( "TokenNetworkViewState: ruleInstanceId " +
        //                     node.getRuleInstance().getId());
        modRuleInstances.add (  new ModNode( node.getRuleInstance().getId(),
                                             node.areNeighborsShown(), node.getLinkCount()));
      }
    }

    ListIterator linkIterator = (new ArrayList( view.tokNetLinkMap.keySet())).listIterator();
    while (linkIterator.hasNext()) {
      String linkName = (String) linkIterator.next();
      BasicNodeLink link = (BasicNodeLink) view.tokNetLinkMap.get( linkName);
      if (link.inLayout()) {
	modLinks.add( new ModLink( linkName, link.getLinkCount()));
      }
    }

  } // end constructor

  public List getModTokens(){
    return modTokens;
  }

  public List getModRuleInstances(){
    return modRuleInstances;
  }

  public List getModLinks(){
    return modLinks;
  }


  public class ModNode {

    private Integer id;
    private boolean areNeighborsShown;
    private int linkCount;
    
    public ModNode( Integer id, boolean areNeighborsShown, int linkCount) {
      this.id = id;
      this.areNeighborsShown = areNeighborsShown;
      this.linkCount = linkCount;
    }

    public final Integer getId() {
      return id;
    }

    public final boolean getAreNeighborsShown() {
      return areNeighborsShown;
    }

    public final int getLinkCount() {
      return linkCount;
    }
  } // end class ModNode


  public class ModLink {

    private String linkName;
    private int linkCount;

    public ModLink( String linkName, int linkCount) {
      this.linkName = linkName;
      this.linkCount = linkCount;
    }

    public final String getLinkName() {
      return linkName;
    }

    public final int getLinkCount() {
      return linkCount;
    }
  } // end class ModLink


} // end class TokenNetworkViewState

