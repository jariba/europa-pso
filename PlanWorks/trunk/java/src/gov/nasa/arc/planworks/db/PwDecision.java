package gov.nasa.arc.planworks.db;

import java.util.List;

public interface PwDecision extends PwEntity {
	public int getType();
	public Integer getEntityId();
	public boolean isUnit();
	public List getChoices();
}
