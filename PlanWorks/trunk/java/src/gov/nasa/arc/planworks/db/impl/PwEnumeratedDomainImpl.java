// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwEnumeratedDomainImpl.java,v 1.2 2003-05-16 20:06:19 miatauro Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 15May03
//

package gov.nasa.arc.planworks.db.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import gov.nasa.arc.planworks.db.PwEnumeratedDomain;


/**
 * <code>PwEnumeratedDomainImpl</code> - 
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                         NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class PwEnumeratedDomainImpl extends PwDomainImpl implements PwEnumeratedDomain {

  private List enumeration; // element String

  public PwEnumeratedDomainImpl( String enumerationString) {
    this.enumeration = new ArrayList();
    StringTokenizer tokenizer = new StringTokenizer( enumerationString);
    while (tokenizer.hasMoreTokens()) {
      this.enumeration.add( tokenizer.nextToken());
    }
  } // end constructor

		public List getEnumeration() {
				return this.enumeration;	
		}
		
		public String toString(){
				StringBuffer enumBuf = new StringBuffer("{");
				for(int i = 0; i < enumeration.size() - 1; i++) {
						enumBuf.append((String) enumeration.get(i));
						enumBuf.append(", ");
				}
				enumBuf.append((String) enumeration.get(enumeration.size() - 1));
				enumBuf.append("}");
				return enumBuf.toString();
		}

} // end class PwEnumeratedDomainImpl
