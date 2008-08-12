// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: Algorithms.java,v 1.7 2004-07-27 21:58:07 taylor Exp $
//
// PlanWorks -- 
//
// Conor McGann -- started 21July03
//

package gov.nasa.arc.planworks.util;

import java.util.List;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.TreeMap;
import java.util.Collection;
import java.util.Collections;


public class Algorithms
{
  public static List betterAllocateRows(int horizonStart, int horizonEnd, List extents) {
    ArrayList source = new ArrayList(extents);
    ArrayList rows = new ArrayList();
    rows.add(new Row(0));
    Collections.sort(source, new ExtentComparator());
    Iterator extentIterator = source.listIterator();
    while(extentIterator.hasNext()) {
      Extent e = (Extent) extentIterator.next();
      int i, n;
      for(i = 0, n = rows.size(); i < n; i++) {
        if(e.getStart() > ((Row)rows.get(i)).getLatestEnd()) {
          ((Row)rows.get(i)).add(e);
          break;
        }
      }
      if(i == rows.size()) {
        Row newRow = new Row(rows.size());
        newRow.add(e);
        rows.add(newRow);
      }
      System.err.println("[" + e.getStart() + ".." + e.getEnd() + "]: " + e.getRow());
    }
    return source;
  }
    /**
     * Will allocate a row number to every extent in the list that may overlap the horizon. Row
     * numbers are assumed to begin at 0. This algorithm ensures that no temporal overlap occurs
     * on any given row and that the minimum number of rows are required. Extents whcih do not
     * overlap the horizon defined by horizonStart and horizonEnd will be given a row of -1.
     * @param horizonStart Will ignore rows that cannot occur at or after this time.
     * @param horizonEnd Will ignore rows that cannot occur at or before this time.
     * @param extents A list of objects implementing the Extent interface whose rows are to be set (thus may be changed)
     * @return A susbet of extents which intersected the time horizon, and for which rows have been assigned.
     */
    public static List allocateRows(final int horizonStart, final int horizonEnd, 
                                    final List extents)
    {
	List results = new ArrayList();
	LinkedList source = new LinkedList(extents);
	Collections.sort(source, new ExtentComparator());

	// Check the sort worked
	if(DEBUG_ON){
	    System.out.println("Debug version");
	    int max=0; // Not safe for negative horizon start value.
	    for(Iterator it = source.iterator();it.hasNext();){
		Extent extent = (Extent) it.next();
		if(max > extent.getStart()){
		    System.out.println("Bad sort");
		    System.exit(-1);
		}
		max = extent.getStart();
	    }
	}

	int lastCount = extents.size();
	int rowCount = 0;

	while(source.size() > 0){
	    boolean passedHorizon=false;
	    int currentTime = horizonStart;
	    Iterator it = source.iterator();
	    while (currentTime <= horizonEnd && it.hasNext()) {
		Extent extent = (Extent) it.next();

		// If it is totally outside the horizon - ret rid of it and set row to NO_ROW
		if(extent.getEnd() < horizonStart || extent.getStart() > horizonEnd){
		    System.out.println("Removing element " + extent.toString() +
                                       " - outside horizon");
                    System.out.println("  start " + extent.getStart() + " end " +
                                       extent.getEnd() + " horizonStart " + horizonStart +
                                       " horizonEnd " + horizonEnd);
		    extent.setRow(NO_ROW);
		    it.remove();
		    continue;
		}

		// If it overlaps already placed extents, then just skip it
		if(extent.getStart() < currentTime && passedHorizon) // Also check to make sure it is not initial entry
		    continue;

		// Otherwise, we will insert it in this row, and remove it from further consideration
		extent.setRow(rowCount);
		results.add(extent);
		it.remove();

		// Update currentTime
		currentTime = extent.getEnd();

		// Indicate we have inserted something and thus passed the Horizon
		passedHorizon = true;
	    }
	    rowCount++;

	    if(DEBUG_ON){
		if(source.size() >= lastCount){
		    System.out.println("Bug in the loop - should be converging but isn't");
		    System.exit(-1);
		}
		lastCount = source.size();
	    }
	}
	return results;
    }

    public static final int NO_ROW = -1;

    public static boolean DEBUG_ON = false;

    public static void main(String[] arg)
    {
	System.out.println("Starting");
	DEBUG_ON = true;
	List extents = new ArrayList();
	extents.add(new ExtentForTesting(10, 20));
	extents.add(new ExtentForTesting(0, 10)); // Case of partial intersection with horizon
	extents.add(new ExtentForTesting(10, 20));
	extents.add(new ExtentForTesting(0, 1)); // Should be kicked out because of horizon
	extents.add(new ExtentForTesting(45, 80)); 
	extents.add(new ExtentForTesting(0, 10)); // Case of partial intersection with horizon
	extents.add(new ExtentForTesting(101, 102)); // Should be kicked out because of horizon - to late
	extents.add(new ExtentForTesting(99, 100));
	extents.add(new ExtentForTesting(99, 110));
	extents.add(new ExtentForTesting(2, 8));
	extents.add(new ExtentForTesting(1, 2)); // Case of partial intersection with horizon, right on the edge
	List results = allocateRows(2, 100, extents);

	for(Iterator it = extents.iterator();it.hasNext();){
	    ExtentForTesting extent = (ExtentForTesting) it.next();
	    System.out.println(extent.toString());
	}

	if(results.size() != 9 || ((ExtentForTesting)results.get(results.size() -1)).getRow() != 2){
	    System.out.println("Test Failed to produce expected results");
	    System.exit(-1);
	}

	System.out.println("Test succeded");
    }
}

class Row {
  private int n, latestEnd;
  public Row(int rownum) {
    n = rownum;
    latestEnd = Integer.MIN_VALUE;
  }
  public void add(Extent e) {
    e.setRow(n);
    latestEnd = e.getEnd();
  }
  public int getLatestEnd() {
      return latestEnd;
  }
}


/**
 * Utility class to compare extents
 */
class ExtentComparator implements Comparator
{
    public int compare(Object obj1, Object obj2)
    {
	Extent e1 = (Extent) obj1;
	Extent e2 = (Extent) obj2;
	if(e1.getStart() < e2.getStart()){
	    return -1;
	} else if (e1.getStart() > e2.getStart()){
	    return 1;
	} else {
	    return 0;
	}
    }

}
/**
 * Private class for testing. Instead just implement interface on top of
 * own exiting object.
 */
class ExtentForTesting implements Extent
{
    public String toString(){
	String result = "<" + m_row + ", " + m_start + ", " + m_end + ">";
	return result;
    }

    public ExtentForTesting(int start, int end)
    {
	if(start >= end)
	    System.exit(-1); // The situation is hopeless - go home.

	m_start = start;
	m_end = end;
	m_row = Algorithms.NO_ROW;
    }

    public int getStart(){return m_start;}
    public int getEnd(){return m_end;}
    public void setRow(int row){m_row = row;}
    public int getRow(){return m_row;}

    private int m_start;
    private int m_end;
    private int m_row;
}
