//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: SequenceContentSpec.java,v 1.2 2004-02-03 19:22:41 miatauro Exp $
//
package gov.nasa.arc.planworks.db.util;

import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.StringTokenizer;
import gov.nasa.arc.planworks.db.PwPlanningSequence;
import gov.nasa.arc.planworks.db.PwToken;
import gov.nasa.arc.planworks.db.util.MySQLDB;
import gov.nasa.arc.planworks.util.UniqueSet;
import gov.nasa.arc.planworks.viz.viewMgr.RedrawNotifier;
import gov.nasa.arc.planworks.viz.viewMgr.ViewableObject;

/*
 * <code>ContentSpec</code> -
 * @author <a href="mailto:miatauro@email.arc.nasa.gov">Michael Iatauro</a>
 * The content specification class.  Interfaces directely with the database to determine which ids
 * are/should be in the current specification, and provides a method for exposing that information
 * to other classes (VizView through ViewSet).
 */

public class SequenceContentSpec implements ContentSpec {
  private UniqueSet validTokenIds;
  private List currentSpec;
  private PwPlanningSequence planSequence;
  private RedrawNotifier redrawNotifier;


  /**
   * Creates the ContentSpec object, then makes a query 
   *
   * @param <code>planSequence</code> the sequence object constrained by this object
   * @param <code>redrawNotifier</code> an interface to inform views that they need to re-draw
   */

  public SequenceContentSpec( final ViewableObject planSequence, 
                              final RedrawNotifier redrawNotifier)  {
    this.planSequence = (PwPlanningSequence) planSequence;
    this.redrawNotifier = redrawNotifier;
    this.validTokenIds = new UniqueSet();

    // queryValidTokens();

    currentSpec = this.planSequence.getContentSpec();

//     currentSpec.add( null); // timeline
//     currentSpec.add( null); // predicate
//     currentSpec.add( null); // time interval
//     currentSpec.add( new Boolean( false)); // merge
//     currentSpec.add( new Integer( ALL)); // tokenTypes

    applySpec(currentSpec);
  }

  /**
   * Sets all tokens valid
   */
  public void resetSpec() {
    validTokenIds.clear();
    currentSpec.clear();

    // queryValidTokens();

    redrawNotifier.notifyRedraw();
  }
  
  /**
   * Get all token ids
   */
//   private void queryValidTokens() {
//     try {
//       ResultSet validTokens = MySQLDB.queryDatabase(TOKENID_QUERY.concat(partialPlanId.toString()));
//       while(validTokens.next()) {
//         validTokenIds.add(new Integer(validTokens.getInt(TOKENID)));
//       }
//     }
//     catch(SQLException sqle) {
//     }
//   }

  /**
   * Get the list of ids for tokens conforming to the specification
   *
   * @return List - valid token ids
   */
  public List getValidIds(){return validTokenIds;}

  public void printSpec() {
    System.err.println("Allowable tokens: ");
    ListIterator tokenIdIterator = validTokenIds.listIterator();
    while(tokenIdIterator.hasNext()) {
      System.err.println(((Integer)tokenIdIterator.next()).toString());
    }
  }
  /**
   * Given the parametes specified by the user in the ContentSpecWindow, constructs the entire
   * specification of valid ids through a database query, then informs the windows
   * goverend by this spec that they need to redraw themselves to the new specification.
   */
  public void applySpec(final List spec) throws NumberFormatException {


    redrawNotifier.notifyRedraw();
  }

  
  public List getCurrentSpec() {
    return new ArrayList(currentSpec);
  }
}
