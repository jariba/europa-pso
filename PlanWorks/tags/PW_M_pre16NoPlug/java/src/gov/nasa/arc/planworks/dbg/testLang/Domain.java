package gov.nasa.arc.planworks.dbg.testLang;

/*NOTE: the contract of Comparable is abused slightly by Domain, since there are
	different kinds of equality.  compareTo() return values are as follows:
	-2 : <=
	-1 : <
	 0 : =
	 1 : >
	 2 : >=
   3 : in
   4 : out
*/

public interface Domain {
	public Comparable getFirst();
	public Comparable getLast();
	public boolean isSingleton();
}
