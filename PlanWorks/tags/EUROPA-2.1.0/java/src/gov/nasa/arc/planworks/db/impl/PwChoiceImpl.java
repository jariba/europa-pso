// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwChoiceImpl.java,v 1.8 2004-12-06 22:03:24 pdaley Exp $
//
package gov.nasa.arc.planworks.db.impl;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import gov.nasa.arc.planworks.db.DbConstants;
import gov.nasa.arc.planworks.db.PwChoice;
import gov.nasa.arc.planworks.db.PwDomain;
import gov.nasa.arc.planworks.db.PwPartialPlan;

public class PwChoiceImpl implements PwChoice {
  private Integer id;
  private int type;
  private Integer entityId;
  private double value;
  private PwDomain dom;
  private PwPartialPlan partialPlan;

  public PwChoiceImpl(String info, PwPartialPlan partialPlan) {
    // System.err.println( "PwChoiceImpl info '" + info + "'");
    StringTokenizer strTok = new StringTokenizer(info, ",");
    id = Integer.valueOf(strTok.nextToken());
    type = Integer.parseInt(strTok.nextToken());
    switch(type) {
    case DbConstants.C_OBJECT:
      entityId = Integer.valueOf(strTok.nextToken());
      break;
    case DbConstants.C_VALUE:
      entityId = Integer.valueOf(strTok.nextToken());
      if (entityId.intValue() != -1) {
        value = Double.parseDouble(strTok.nextToken());
      }
      break;
    case DbConstants.C_DOMAIN:
      String type = strTok.nextToken();
      if(type.equals("E")) {
        dom = new PwEnumeratedDomainImpl(strTok.nextToken());
      }
      else if(type.equals("I")) {
        dom = new PwIntervalDomainImpl("REAL_SORT", strTok.nextToken(), strTok.nextToken());
      }
      else {
        throw new IllegalArgumentException();
      }
      break;
    case DbConstants.C_CLOSE:
      break;
    }
    this.partialPlan = partialPlan;
  }

  public final int getType(){return type;}
  public final Integer getId(){return id;}
  public final Integer getEntityId(){return entityId;}
  public final double getValue(){return value;}
  public final PwDomain getDomain(){return dom;}

  public final String toString() {
    switch(type) {
    case DbConstants.C_OBJECT:
      StringBuffer buf = new StringBuffer( "");
      if (partialPlan.getResource( entityId) != null) {
        buf.append( "Resource ");
      } else if (partialPlan.getTimeline( entityId) != null) {
        buf.append( "Timeline ");
      } else if (partialPlan.getObject( entityId) != null) {
        buf.append( "Object ");
      }
      return buf.append( "key=").append( entityId.toString()).toString();
    case DbConstants.C_VALUE:
      buf = new StringBuffer( "");
      if (entityId.intValue() != -1) {
	buf.append( "Token key=");
	buf.append( entityId.toString()).append( "; ");
      }
      if (value == 2.0) {
        buf.append( "activate");
      } else if (value == 3.0) {
        buf.append( "merge");
      } else {
        buf.append( "value=").append( String.valueOf( value));
      }
      return buf.toString();
    case DbConstants.C_DOMAIN:
      if (dom instanceof PwEnumeratedDomainImpl) {
        return ((PwEnumeratedDomainImpl) dom).toString();
      } else if (dom instanceof PwIntervalDomainImpl) {
        return ((PwIntervalDomainImpl) dom).toString();
      }
    case DbConstants.C_CLOSE:
      return "Close";
    }
    return "<not-found>";
  }


  public final String toOutputString() {
    StringBuffer retval = new StringBuffer(id.toString()).append(",").append(type).append(",");
    switch(type) {
    case DbConstants.C_OBJECT:
      retval.append(entityId.toString());
      break;
    case DbConstants.C_VALUE:
      retval.append(entityId.toString()).append(",").append(value);
      break;
    case DbConstants.C_DOMAIN:
      if(dom instanceof PwEnumeratedDomainImpl) {
        retval.append("E,");
        List enumer = ((PwEnumeratedDomainImpl)dom).getEnumeration();
        for(Iterator it = enumer.iterator(); it.hasNext();) {
          retval.append(it.next().toString()).append(" ");
        }
      }
      else if(dom instanceof PwIntervalDomainImpl) {
        retval.append("I,").append(dom.getLowerBound()).append(",").append(dom.getUpperBound());
      }
      else {
        throw new IllegalStateException();
      }
      break;
    case DbConstants.C_CLOSE:
      retval.append("CLOSE");
      break;
    }
    return retval.toString();
  }

  public List getNeighbors(){return null;}
  public List getNeighbors(List classes){return null;}
  public List getNeighbors(List classes, Set ids){return null;}
  public List getNeighbors(List classes, List linkTypes){return null;}
}
