// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwChoiceImpl.java,v 1.4 2004-07-16 22:54:44 taylor Exp $
//
package gov.nasa.arc.planworks.db.impl;

import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import gov.nasa.arc.planworks.db.DbConstants;
import gov.nasa.arc.planworks.db.PwChoice;
import gov.nasa.arc.planworks.db.PwDomain;

public class PwChoiceImpl implements PwChoice {
  private Integer id;
  private int type;
  private Integer tokenId;
  private double value;
  private PwDomain dom;

  public PwChoiceImpl(String info) {
    // System.err.println( "PwChoiceImpl info '" + info + "'");
    StringTokenizer strTok = new StringTokenizer(info, ",");
    id = Integer.valueOf(strTok.nextToken());
    type = Integer.parseInt(strTok.nextToken());
    switch(type) {
    case DbConstants.C_TOKEN:
      tokenId = Integer.valueOf(strTok.nextToken());
      break;
    case DbConstants.C_VALUE:
      tokenId = Integer.valueOf(strTok.nextToken());
      if (tokenId.intValue() != -1) {
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
  }

  public final int getType(){return type;}
  public final Integer getId(){return id;}
  public final Integer getTokenId(){return tokenId;}
  public final double getValue(){return value;}
  public final PwDomain getDomain(){return dom;}

  public final String toString() {
    switch(type) {
    case DbConstants.C_TOKEN:
      return "Token key=" + tokenId.toString();
    case DbConstants.C_VALUE:
      StringBuffer buf = new StringBuffer( "Token key=");
      buf.append( tokenId.toString()).append( "; ");
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
    case DbConstants.C_TOKEN:
      retval.append(tokenId.toString());
      break;
    case DbConstants.C_VALUE:
      retval.append(tokenId.toString()).append(",").append(value);
      break;
    case DbConstants.C_DOMAIN:
      if(dom instanceof PwEnumeratedDomainImpl) {
        retval.append("E,");
        List enum = ((PwEnumeratedDomainImpl)dom).getEnumeration();
        for(Iterator it = enum.iterator(); it.hasNext();) {
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
}
