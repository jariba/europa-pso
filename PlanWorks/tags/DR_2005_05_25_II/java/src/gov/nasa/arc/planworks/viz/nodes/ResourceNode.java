// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: ResourceNode.java,v 1.3 2004-06-16 22:09:10 taylor Exp $
//
// PlanWorks
//
package gov.nasa.arc.planworks.viz.nodes;

import java.awt.Color;
import java.awt.Point;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoBrush;
import com.nwoods.jgo.JGoObject;

import gov.nasa.arc.planworks.db.PwResource;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanView;

public class ResourceNode extends ExtendedBasicNode {
  
  protected PwResource resource;
  protected PartialPlanView partialPlanView;
  protected String nodeLabel;
  protected boolean isDebug;
  protected Color backgroundColor;

  /**
   * <code>ResourceNode</code> - constructor 
   *
   * @param resource - <code>PwResource</code> - 
   * @param resourceLocation - <code>Point</code> - 
   * @param backgroundColor - <code>Color</code> - 
   * @param isDraggable - <code>boolean</code> - 
   * @param partialPlanView - <code>PartialPlanView</code> - 
   */
  public ResourceNode( PwResource resource, Point resourceLocation, Color backgroundColor,
                       boolean isDraggable, PartialPlanView partialPlanView) { 
    super( ViewConstants.PINCHED_HEXAGON);
    this.resource = resource;
    this.partialPlanView = partialPlanView;
    this.backgroundColor = backgroundColor;
    isDebug = false;
    // isDebug = true;
    StringBuffer labelBuf = new StringBuffer( resource.getName());
    labelBuf.append( "\nkey=").append( resource.getId().toString());
    nodeLabel = labelBuf.toString();
    // System.err.println( "ResourceNavNode: " + nodeLabel);

    configure( resourceLocation, backgroundColor, isDraggable);
  } // end constructor

  /**
   * <code>ResourceNode</code> - constructor - for NodeShapes
   *
   * @param name - <code>String</code> - 
   * @param id - <code>Integer</code> - 
   * @param resourceLocation - <code>Point</code> - 
   * @param backgroundColor - <code>Color</code> - 
   */
  public ResourceNode( String name, Integer id, Point resourceLocation, Color backgroundColor) { 
    super( ViewConstants.PINCHED_HEXAGON);
    this.resource = null;
    this.partialPlanView = null;
    this.backgroundColor = backgroundColor;
    boolean isDraggable = false;
    isDebug = false;
    // isDebug = true;
    StringBuffer labelBuf = new StringBuffer( name);
    labelBuf.append( "\nkey=").append( id.toString());
    nodeLabel = labelBuf.toString();
    // System.err.println( "ResourceNavNode: " + nodeLabel);

    configure( resourceLocation, backgroundColor, isDraggable);
  } // end constructor

  private final void configure( final Point resourceLocation, final Color backgroundColor,
                                final boolean isDraggable) {
    setLabelSpot( JGoObject.Center);
    initialize( resourceLocation, nodeLabel);
    setBrush( JGoBrush.makeStockBrush( backgroundColor));  
    getLabel().setEditable( false);
    setDraggable( isDraggable);
    // do not allow user links
    getPort().setVisible( false);
    getLabel().setMultiline( true);
  } // end configure

  public boolean equals( ResourceNode node) {
    return (this.getResource().getId().equals( node.getResource().getId()));
  }
  
  public PwResource getResource() {
    return resource;
  }
  
  public PartialPlanView getPartialPlanView() {
    return partialPlanView;
  }

  public String toString() {
    return resource.getId().toString();
  }

  public Color getColor(){return backgroundColor;}

}
