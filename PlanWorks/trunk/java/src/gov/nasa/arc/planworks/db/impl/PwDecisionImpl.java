package gov.nasa.arc.planworks.db.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import gov.nasa.arc.planworks.db.PwChoice;
import gov.nasa.arc.planworks.db.PwDecision;

public class PwDecisionImpl implements PwDecision {
	private Long ppId;
	private Integer id;
	private int type;
	private Integer entityId;
	private boolean unit;
	private List choiceList;

	public PwDecisionImpl(Long ppId, Integer id, int type, Integer entityId, boolean unit, String choiceStr) {
		this.id = id;
		this.type = type;
		this.entityId = entityId;
		this.unit = unit;
		choiceList = new ArrayList();
		String [] choices = choiceStr.split("\\x1e");
		for(int i = 0; i < choices.length; i++) {
			choiceList.add(new PwChoiceImpl(choices[i]));
		}
	}
	public final Integer getId(){return id;}
	public final int getType(){return type;}
	public final Integer getEntityId(){return entityId;}
	public final boolean isUnit(){return unit;}
	public final List getChoices(){return new ArrayList(choiceList);}
	public final String toOutputString() {
		StringBuffer retval = new StringBuffer(ppId.toString()).append("\t").append(id.toString()).append("\t").append(type);
		retval.append("\t").append(entityId.toString()).append("\t");
		if(unit) {
			retval.append(1);
		}
		else {
			retval.append(0);
		}
		retval.append("\t");
		for(Iterator i = choiceList.iterator(); i.hasNext();) {
			retval.append(((PwChoice)i.next()).toOutputString());
		}
		retval.append("\n");
		return retval.toString();
	}
}
