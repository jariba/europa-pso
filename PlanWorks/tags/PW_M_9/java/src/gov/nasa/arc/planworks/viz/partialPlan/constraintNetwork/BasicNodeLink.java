// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: BasicNodeLink.java,v 1.1 2003-09-25 23:52:44 taylor Exp $
//
// PlanWorks
//
// Will Taylor -- started 28july03
//

package gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoLabeledLink;
import com.nwoods.jgo.JGoText;

// PlanWorks/java/lib/JGo/Classier.jar
import com.nwoods.jgo.examples.BasicNode;


/**
 * <code>BasicNodeLink</code> - JGo widget to render a link with a
 *                          label between two BasicNode nodes
 *             Object->JGoObject->JGoArea->JGoLabeledLink->BasicNodeLink
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *       NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class BasicNodeLink extends JGoLabeledLink {

  private BasicNode fromNode;
  private BasicNode toNode;
  private String linkName;
  private boolean inLayout;
  private int linkCount;
  private boolean isDebug;

  /**
   * <code>BasicNodeLink</code> - constructor 
   *
   * @param fromNode - <code>BasicNode</code> - 
   * @param toNode - <code>BasicNode</code> - 
   * @param linkName - <code>String</code> - 
   */
  public BasicNodeLink( BasicNode fromNode, BasicNode toNode, String linkName) {
    super( fromNode.getPort(), toNode.getPort());
    this.fromNode = fromNode;
    this.toNode = toNode;
    this.linkName = linkName;
    inLayout = false;
    resetLink( false);

    isDebug = false;
    // isDebug = true;
    
    this.setArrowHeads( false, false); // fromArrowHead toArrowHead
    // do no allow user to select and move links
    this.setRelinkable( false);
  } // end constructor

  /**
   * <code>getFromNode</code>
   *
   * @return - <code>BasicNode</code> - 
   */
  public BasicNode getFromNode() {
    return this.fromNode;
  }

  /**
   * <code>getToNode</code>
   *
   * @return - <code>BasicNode</code> - 
   */
  public BasicNode getToNode() {
    return this.toNode;
  }

  /**
   * <code>getLinkName</code>
   *
   * @return - <code>String</code> - 
   */
  public String getLinkName() {
    return linkName;
  }

  /**
   * <code>inLayout</code>
   *
   * @return - <code>boolean</code> - 
   */
  public boolean inLayout() {
    return inLayout;
  }

  /**
   * <code>setInLayout</code>
   *
   * @param value - <code>boolean</code> - 
   */
  public void setInLayout( boolean value) {
    inLayout = value;
  }

  /**
   * <code>toString</code>
   *
   * @return - <code>String</code> - 
   */
  public String toString() {
    return linkName;
  }

  /**
   * <code>equals</code>
   *
   * @param link - <code>BasicNodeLink</code> - 
   * @return - <code>boolean</code> - 
   */
  public boolean equals( BasicNodeLink link) {
    return (this.linkName.equals( link.getLinkName()));
  }

  /**
   * <code>incrLinkCount</code>
   *
   */
  public void incrLinkCount() {
    linkCount++;
    if (isDebug) {
      setMidLabel( new JGoText( String.valueOf( linkCount)));
    }
  }

  /**
   * <code>decLinkCount</code>
   *
   */
  public void decLinkCount() {
    linkCount--;
    if (isDebug) {
      setMidLabel( new JGoText( String.valueOf( linkCount)));
    }
  }

  /**
   * <code>getLinkCount</code>
   *
   * @return - <code>int</code> - 
   */
  public int getLinkCount() {
    return linkCount;
  }

  /**
   * <code>resetLink</code> - when closed by token close traversal
   *
   * @param isDebug - <code>boolean</code> - 
   */
  public void resetLink( boolean isDebug) {
    if (isDebug && (linkCount != 0)) {
      System.err.println( "reset link: " + linkName +
                          "; linkCount != 0: " + linkCount);
    }
    linkCount = 0;
    if (isDebug) {
      setMidLabel( new JGoText( String.valueOf( linkCount)));
    }
  } // end resetLink



} // end class BasicNodeLink
