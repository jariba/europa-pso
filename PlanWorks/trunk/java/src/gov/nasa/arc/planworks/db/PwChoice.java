package gov.nasa.arc.planworks.db;

public interface PwChoice extends PwEntity {
	public int getType();
	public Integer getTokenId();
	public double getValue();
	public PwDomain getDomain();
}
