// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: DBTransactionTableModel.java,v 1.2 2004-08-10 21:17:13 taylor Exp $
//
// PlanWorks
//
// Will Taylor -- started 14may04
//

package gov.nasa.arc.planworks.viz.util;

import java.util.Comparator;
import javax.swing.table.AbstractTableModel;


/**
 * <code>DBTransactionTableModel</code> - 
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class DBTransactionTableModel extends AbstractTableModel {

  private String[] columnNames;
  private Object[][] data;

    public static final Comparator INTEGER_COMPARATOR = new Comparator() {
        public int compare(Object o1, Object o2) {
	  Integer i1 = new Integer( (String) o1);
	  Integer i2 = new Integer( (String) o2);
	  return i1.compareTo( i2);
        }
        public boolean equals(Object o1, Object o2) {
	  Integer i1 = new Integer( (String) o1);
	  Integer i2 = new Integer( (String) o2);
	  return i1.equals( i2);
        }
    };

  /**
   * <code>DBTransactionTableModel</code> - constructor 
   *
   * @param columnNames - <code>String[]</code> - 
   * @param data - <code>Object[][]</code> - 
   */
  public DBTransactionTableModel( final String[] columnNames, final Object[][] data) {
    super();
    this.columnNames = columnNames;
    this.data = data;
  }

  /**
   * <code>getColumnCount</code>
   *
   * @return - <code>int</code> - 
   */
  public final int getColumnCount() {
    return columnNames.length;
  }

  /**
   * <code>getRowCount</code>
   *
   * @return - <code>int</code> - 
   */
  public final int getRowCount() {
    return data.length;
  }

  /**
   * <code>getColumnName</code>
   *
   * @param col - <code>int</code> - 
   * @return - <code>String</code> - 
   */
  public final String getColumnName( final int col) {
    return columnNames[col];
  }

  /**
   * <code>getValueAt</code>
   *
   * @param row - <code>int</code> - 
   * @param col - <code>int</code> - 
   * @return - <code>Object</code> - 
   */
  public final Object getValueAt( final int row, final int col) {
    return data[row][col];
  }

} // end class DBTransactionTableModel
