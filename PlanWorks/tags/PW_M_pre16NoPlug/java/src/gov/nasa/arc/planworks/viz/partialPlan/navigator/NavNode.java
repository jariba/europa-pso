// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: NavNode.java,v 1.2 2004-03-16 02:24:11 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 20feb04
//

package gov.nasa.arc.planworks.viz.partialPlan.navigator;

import java.util.List;


/**
 * Describe interface <code>NavNode</code> here.
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *              NASA Ames Research Center - Code IC
 * @version 0.0
 */
public interface NavNode {

  /**
   * <code>getId</code>
   *
   * @return - <code>Integer</code> - 
   */
  public Integer getId();

  /**
   * <code>getTypeName</code>
   *
   * @return - <code>String</code> - 
   */
  public String getTypeName();

  /**
   * <code>incrLinkCount</code>
   *
   */
  public void incrLinkCount();

  /**
   * <code>decLinkCount</code>
   *
   */
  public void decLinkCount();

  /**
   * <code>getLinkCount</code>
   *
   * @return - <code>int</code> - 
   */
  public int getLinkCount();

  /**
   * <code>inLayout</code>
   *
   * @return - <code>boolean</code> - 
   */
  public boolean inLayout();

  /**
   * <code>setInLayout</code>
   *
   * @param value - <code>boolean</code> - 
   */
  public void setInLayout( boolean value);

  /**
   * <code>resetNode</code>
   *
   * @param isDebug - <code>boolean</code> - 
   */
  public void resetNode( boolean isDebug);

  /**
   * <code>getParentEntityList</code>
   *
   * @return - <code>List</code> - of PwEntity
   */
  public List getParentEntityList();

  /**
   * <code>getComponentEntityList</code>
   *
   * @return - <code>List</code> - of PwEntity
   */
  public List getComponentEntityList();


} // end interface NavNode
