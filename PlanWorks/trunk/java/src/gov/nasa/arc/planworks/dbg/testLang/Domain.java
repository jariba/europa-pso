package gov.nasa.arc.planworks.dbg.testLang;

public interface Domain {
	public Comparable getFirst();
	public Comparable getLast();
	public boolean isSingleton();
}
