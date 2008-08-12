// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwDecisionImpl.java,v 1.8 2004-09-30 22:03:01 miatauro Exp $
//
package gov.nasa.arc.planworks.db.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import gov.nasa.arc.planworks.db.DbConstants;
import gov.nasa.arc.planworks.db.PwChoice;
import gov.nasa.arc.planworks.db.PwDecision;
import gov.nasa.arc.planworks.db.PwPartialPlan;

public class PwDecisionImpl implements PwDecision {
  
  private Integer id;
  private int type;
  private Integer entityId;
  private boolean unit;
  private List choiceList;
  private PwPartialPlan partialPlan;

  public PwDecisionImpl(Integer id, int type, Integer entityId, boolean unit,
                        PwPartialPlan partialPlan) {
    this.id = id;
    this.type = type;
    this.entityId = entityId;
    this.unit = unit;
    this.partialPlan = partialPlan;
    choiceList = new ArrayList();
  }

  public final Integer getId(){return id;}
  public final int getType(){return type;}
  public final Integer getEntityId(){return entityId;}
  public final boolean isUnit(){return unit;}
  public final List getChoices(){return new ArrayList(choiceList);}
  public final void makeChoices(final String choiceStr) {
    // System.err.println( "PwDecisionImpl: " + this.toString());
    // System.err.println( "PwDecisionImpl: choiceStr " + choiceStr);
    String [] choices = choiceStr.split("\\x1e");
    for(int i = 0; i < choices.length; i++) {
      choiceList.add( new PwChoiceImpl( choices[i], partialPlan));
    }
  }

  public final String toString() {
    StringBuffer buf =  new StringBuffer( "key=");
    buf.append( id.toString()).append( "; ");
    // System.err.println( "PwDecisionImpl: key = " + id.toString() + " type " + type);
    if ((type == DbConstants.D_OBJECT) || (type == DbConstants.D_TOKEN)) {
      buf.append( "Token ");
    } else if (type == DbConstants.D_VARIABLE) {
      buf.append( "Variable ");
    }
    buf.append( "key=").append( entityId.toString());
    return buf.toString();
  }

  public final String toOutputString() {
    StringBuffer retval = new StringBuffer(partialPlan.getId().toString()).append("\t");
    retval.append(id.toString()).append("\t");
    retval.append(type).append("\t");
    retval.append(entityId.toString()).append("\t");
    if(unit) {
      retval.append(1);
    }
    else {
      retval.append(0);
    }
    retval.append("\t");
    if (choiceList.size() > 0) {
      for(Iterator i = choiceList.iterator(); i.hasNext();) {
        retval.append(((PwChoice)i.next()).toOutputString());
      }
    } else {
      retval.append("\\N");
    }
    retval.append("\n");
    return retval.toString();
  }

  public List getNeighbors(){return null;}
  public List getNeighbors(List classes){return null;}
  public List getNeighbors(List classes, Set ids){return null;}
  public List getNeighbors(List classes, List linkTypes){return null;}

}
