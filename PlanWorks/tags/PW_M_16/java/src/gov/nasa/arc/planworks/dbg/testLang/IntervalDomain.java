package gov.nasa.arc.planworks.dbg.testLang;

/*NOTE: the contract of Comparable is abused slightly by Domain, since there are
	different kinds of equality.  compareTo() return values are as follows:
	-2 : <
	-1 : <=
	 0 : =
	 1 : >=
	 2 : >
*/

public class IntervalDomain implements Domain {
	private Comparable lowerBound;
	private Comparable upperBound;
	public IntervalDomain(Comparable lowerBound, Comparable upperBound) {
		if(lowerBound.compareTo(upperBound) <= 0) {
			this.lowerBound = lowerBound;
			this.upperBound = upperBound;
		}
		else
			throw new IllegalArgumentException("Upper bound of interval domain must be >= lower bound.");
	}
	public Comparable getFirst() {
		return lowerBound;
	}
	public Comparable getLast() {
		return upperBound;
	}

	public boolean isSingleton() {
		return lowerBound.compareTo(upperBound) == 0;
	}

  public String toString() {
    StringBuffer retval = new StringBuffer("[");
    retval.append(lowerBound.toString()).append("..").append(upperBound.toString()).append("]");
    return retval.toString();
  }
}
