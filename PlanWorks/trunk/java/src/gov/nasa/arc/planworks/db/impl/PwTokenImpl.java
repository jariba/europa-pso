// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwTokenImpl.java,v 1.2 2003-05-16 18:22:13 miatauro Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 15May03
//

package gov.nasa.arc.planworks.db.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import gov.nasa.arc.planworks.db.PwToken;
import gov.nasa.arc.planworks.db.util.XmlDBeXist;


/**
 * <code>PwTokenImpl</code> - Java mapping of XML structure
 *                       /PartialPlan/Object/Timeline/Slot/Token
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                         NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class PwTokenImpl implements PwToken {

  private String key;
  private String isValueToken;
  private String predicateId;
  private String startVarId;
  private String endVarId;
  private String durationVarId;
  private String objectVarId;
  private String rejectVarId;
  private List tokenRelationIds; // element String
  private List paramVarIds; // element String
  private String slotId;
  private PwPredicateImpl predicate;
	private PwVariableImpl startVar;
	private PwVariableImpl endVar;
  private List paramVarsList; // element String


  public PwTokenImpl( List attributeList, PwPartialPlanImpl partialPlan,
                      String collectionName) {
    if (attributeList.size() != XmlDBeXist.NUM_TOKEN_ATTRIBUTES) {
      System.err.println( "Token constructor: attribute list does not contain 11 items" +
                          " => " + attributeList);
      try {
        throw new Exception();
      } catch (Exception e) { e.printStackTrace(); }
      System.exit( 0);
    }
    for (int i = 0, n = attributeList.size(); i < n; i++) {
      switch ( i) {
      case 0: this.key = (String) attributeList.get( 0); break;
      case 1: this.isValueToken = (String) attributeList.get( 1); break;
      case 2: this.predicateId = (String) attributeList.get( 2); break;
      case 3: this.startVarId = (String) attributeList.get( 3); break;
      case 4: this.endVarId = (String) attributeList.get( 4); break;
      case 5: this.durationVarId = (String) attributeList.get( 5); break;
      case 6: this.objectVarId = (String) attributeList.get( 6); break;
      case 7: this.rejectVarId = (String) attributeList.get( 7); break;
      case 8:
        this.tokenRelationIds = new ArrayList();
        StringTokenizer tokenizer = new StringTokenizer( (String) attributeList.get( 8));
        while (tokenizer.hasMoreTokens()) {
          this.tokenRelationIds.add( tokenizer.nextToken());
        }
        break;
      case 9:
        this.paramVarIds = new ArrayList();
        tokenizer = new StringTokenizer( (String) attributeList.get( 9));
        while (tokenizer.hasMoreTokens()) {
          this.paramVarIds.add( tokenizer.nextToken());
        }
        break;
      case 10: this.slotId = (String) attributeList.get( 10); break;
      }
    }

    predicate = partialPlan.getPredicate( predicateId, collectionName);
    paramVarsList = new ArrayList ();
    Iterator paramVarIdsIterator = paramVarIds.iterator();
    while (paramVarIdsIterator.hasNext()) {
      String paramVarId = (String) paramVarIdsIterator.next();
      PwVariableImpl paramVariable =
        partialPlan.getVariable( paramVarId, collectionName);
      paramVarsList.add( paramVariable.getDomain().toString());
    }
  } // end constructor

	public PwPredicate getPredicate()
	{
		return predicate;
	}

	public PwVariable getStartVariable()
	{
	}

} // end class PwTokenImpl

