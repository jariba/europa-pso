package gov.nasa.arc.planworks.viz.nodes;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoBrush;
import com.nwoods.jgo.JGoObject;
import com.nwoods.jgo.JGoPen;
import com.nwoods.jgo.JGoView;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.PwObject;
import gov.nasa.arc.planworks.db.PwVariableContainer;
import gov.nasa.arc.planworks.util.ColorMap;
import gov.nasa.arc.planworks.util.MouseEventOSX;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.nodes.ExtendedBasicNode;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanView;

public class ObjectNode extends ExtendedBasicNode {
  protected PwObject object;
  protected PartialPlanView partialPlanView;
  protected String nodeLabel;
  protected boolean isDebug;
  protected Color backgroundColor;

  public ObjectNode( final PwObject object, final Point objectLocation, final Color backgroundColor,
                     final boolean isDraggable, final PartialPlanView partialPlanView) { 
    super( ViewConstants.LEFT_TRAPEZOID);
    this.object = object;
    this.backgroundColor = backgroundColor;
    this.partialPlanView = partialPlanView;

    isDebug = false;
    // isDebug = true;
    StringBuffer labelBuf = new StringBuffer( object.getName());
    labelBuf.append( "\nkey=").append( object.getId().toString());
    nodeLabel = labelBuf.toString();

    configure( objectLocation, backgroundColor, isDraggable);

  } // end constructor

   private final void configure( Point objectLocation, Color backgroundColor,
                                boolean isDraggable) {
    setLabelSpot( JGoObject.Center);
    initialize( objectLocation, nodeLabel);
    setBrush( JGoBrush.makeStockBrush( backgroundColor));  
    getLabel().setEditable( false);
    setDraggable( isDraggable);
    // do not allow user links
    getPort().setVisible( false);
    getLabel().setMultiline( true);
  } // end configure 

  public boolean equals( ObjectNode node) {
    return (this.getObject().getId().equals( node.getObject().getId()));
  }

  public PwObject getObject() {
    return object;
  }

  public PartialPlanView getPartialPlanView() {
    return partialPlanView;
  }

  public Color getBackgroundColor() {
    return backgroundColor;
  }

  public String toString() {
    return object.getId().toString();
  }

  public Color getColor() {return backgroundColor;}
}
