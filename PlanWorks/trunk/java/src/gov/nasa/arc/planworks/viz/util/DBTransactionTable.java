// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: DBTransactionTable.java,v 1.1 2004-05-21 21:39:11 taylor Exp $
//
// PlanWorks
//
// Will Taylor -- started 14may04
//

package gov.nasa.arc.planworks.viz.util;

import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.PwPlanningSequence;
import gov.nasa.arc.planworks.util.ColorMap;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.ViewGenerics;
import gov.nasa.arc.planworks.viz.ViewListener;
import gov.nasa.arc.planworks.viz.VizView;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanView;
import gov.nasa.arc.planworks.viz.sequence.SequenceView;


/**
 * <code>DBTransactionTable</code> - 
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *            NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class DBTransactionTable extends JTable {

  private static final String COLUMN_TOOL_TIP;

  static {
    StringBuffer columnToolTipBuf = new StringBuffer( "<html> ");
    columnToolTipBuf.append( "Mouse-Left: sortAscending, sortDescending, ");
    columnToolTipBuf.append( "noSort (clear other sorts)");
    columnToolTipBuf.append( "<br>");
    columnToolTipBuf.append( "Control-Mouse-Left: sortAscending, sortDescending, ");
    columnToolTipBuf.append( "noSort (do not clear other sorts)");
    columnToolTipBuf.append( "</html>");
    COLUMN_TOOL_TIP = columnToolTipBuf.toString();
  }

  private VizView vizView;
  private int stepNumberColumn;

  private int selectedRow;
  private int selectedCol;
  private Font headerFont;
  private FontMetrics headerFontMetrics;
  private TableSorter sorter;


  /**
   * <code>DBTransactionTable</code> - constructor 
   *
   * @param sorter - <code>TableSorter</code> - 
   * @param stepNumberColumn - <code>int</code> - 
   * @param vizView - <code>VizView</code> - 
   */
  public DBTransactionTable( final TableSorter sorter, final int stepNumberColumn,
                             final VizView vizView) {
    super( sorter);
    this.sorter = sorter;
    this.stepNumberColumn = stepNumberColumn;
    this.vizView = vizView;

    headerFont = new Font( ViewConstants.VIEW_FONT_NAME,
                           ViewConstants.VIEW_FONT_BOLD_STYLE,
                           ViewConstants.VIEW_FONT_SIZE);
    Graphics graphics = ((JPanel) vizView).getGraphics();
    headerFontMetrics = graphics.getFontMetrics( headerFont);
    graphics.dispose();

    // set column widths
    TableModel model = getModel();
    String[] columnNames = new String[model.getColumnCount()];
    for (int i = 0, n = model.getColumnCount(); i < n; i++) {
      columnNames[i] = model.getColumnName( i);
    }
    int charWidth = headerFontMetrics.charWidth( 'A');
    TableColumn column = null;
    for (int i = 0; i < columnNames.length; i++) {
      column = getColumnModel().getColumn( i);
      column.setPreferredWidth
        ( SwingUtilities.computeStringWidth( headerFontMetrics, columnNames[i]) + charWidth);
    }
    setBackground( ViewConstants.VIEW_BACKGROUND_COLOR);
    setForeground( ColorMap.getColor( "black"));
    //setGridColor( ColorMap.getColor( "white"));
    setFont( vizView.getFont());
    // turn off auto resizing of column widths
    setAutoResizeMode( JTable.AUTO_RESIZE_OFF);
    getTableHeader().setReorderingAllowed( false);
    // select single cells only
    setColumnSelectionAllowed( true);
    setRowSelectionAllowed( true);
    setCellSelectionEnabled( true);
    setSelectionMode( ListSelectionModel.SINGLE_SELECTION);
    
    getTableHeader().setDefaultRenderer( new TransactionHeaderRenderer( vizView, this));

    setDefaultRenderer( Object.class, new TransactionCellRenderer( vizView, this));

    // Ask to be notified of selection changes.
    selectedRow = -1; selectedCol = -1;
    ListSelectionModel rowSM = getSelectionModel();
    rowSM.addListSelectionListener( new DBTransTableRowListener( this));
    ListSelectionModel colSM = getColumnModel().getSelectionModel();
    colSM.addListSelectionListener( new DBTransTableColListener( this) );
  } // end constructor

  /**
   * <code>getTableSorter</code>
   *
   * @return - <code>TableSorter</code> - 
   */
  public final TableSorter getTableSorter() {
    return sorter;
  }

  /**
   * <code>getSelectedRow</code>
   *
   * @return - <code>int</code> - 
   */
  public final int getSelectedRow() {
    return selectedRow;
  }

  /**
   * <code>setSelectedRow</code>
   *
   * @param row - <code>int</code> - 
   */
  public final void setSelectedRow( final int row) {
    selectedRow = row;
  }

  /**
   * <code>getSelectedCol</code>
   *
   * @return - <code>int</code> - 
   */
  public final int getSelectedCol() {
    return selectedCol;
  }

  /**
   * <code>setSelectedCol</code>
   *
   * @param col - <code>int</code> - 
   */
  public final void setSelectedCol( final int col) {
    selectedCol = col;
  }

  /**
   * <code>getStepNumberColumn</code>
   *
   * @return - <code>int</code> - 
   */
  public final int getStepNumberColumn() {
    return stepNumberColumn;
  }

  /**
   * <code>getVizView</code>
   *
   * @return - <code>VizView</code> - 
   */
  public final VizView getVizView() {
    return vizView;
  }

  /**
   * <code>getHeaderFont</code>
   *
   * @return - <code>Font</code> - 
   */
  public final Font getHeaderFont() {
    return headerFont;
  }

  /**
   * <code>createDefaultTableHeader</code> - Implement table header tool tips.
   *
   * @return - <code>JTableHeader</code> - 
   */
  protected final JTableHeader createDefaultTableHeader() {
    return new JTableHeader( columnModel) {
        public final String getToolTipText( final MouseEvent evt) {
          String tip = null;
          Point p = evt.getPoint();
          int index = columnModel.getColumnIndexAtX( p.x);
          int realIndex = columnModel.getColumn( index).getModelIndex();
          // return columnToolTips[realIndex];
          return COLUMN_TOOL_TIP;
        }
      };
  } // end createDefaultTableHeader


  /**
   * <code>getToolTipText</code> - 
   *
   * @param evt - <code>MouseEvent</code> - table cell tool tips
   * @return - <code>String</code> - 
   */
  public final String getToolTipText ( final MouseEvent evt) {
    String tip = null;
    Point p = evt.getPoint();
    int rowIndex = rowAtPoint( p); 
    int colIndex = columnAtPoint( p);
    int realColIndex = convertColumnIndexToModel( colIndex);
    Object currentValue = getValueAt( rowIndex, colIndex);
    if (realColIndex == stepNumberColumn) {
      tip = "Mouse-Left: Open partial plan views for step " + currentValue;
    } 
    return tip;
  } 


  // Mouse-Left on a cell
  class DBTransTableRowListener implements ListSelectionListener {

    private DBTransactionTable table;

    public DBTransTableRowListener( final DBTransactionTable table) {
      super();
      this.table = table;
    }

    public final void valueChanged( final ListSelectionEvent evt) {
      //Ignore extra messages.
      if (evt.getValueIsAdjusting()) { return; }

      ListSelectionModel lsm = (ListSelectionModel) evt.getSource();
      if (lsm.isSelectionEmpty()) {
        // table.setSelectedRow( -1);
      } else {
        int selRow = lsm.getMinSelectionIndex();
        table.setSelectedRow( selRow);
        int selCol = table.getSelectedCol();
        // System.out.println( "Row listener row " + selRow + " Col " + selCol);
        if (selCol != -1) {
          String currentValue = (String) table.getValueAt( selRow, selCol);
          // System.out.println( "currentValue " + currentValue);
          if (selCol == table.getStepNumberColumn()) {
            // System.out.println( "Row popup menu for step " + currentValue);
            popupMenuForStepNumberColumn( currentValue, table);
            table.setSelectedRow( -1);
          }
        }
      }
    }
  } // end class DBTransRowTableListener


  // Mouse-Left on a cell
  class DBTransTableColListener implements ListSelectionListener {

    private DBTransactionTable table;

    public DBTransTableColListener( final DBTransactionTable table) {
      super();
      this.table = table;
    }

      public final void valueChanged( final ListSelectionEvent evt) {
      //Ignore extra messages.
        if (evt.getValueIsAdjusting()) { return; }

      ListSelectionModel lsm = (ListSelectionModel) evt.getSource();
      if (lsm.isSelectionEmpty()) {
        // table.setSelectedCol( -1);
      } else {
        int selCol = lsm.getMinSelectionIndex();
        table.setSelectedCol( selCol);
        int selRow = table.getSelectedRow();
        // System.out.println( "Col listener row " + selRow + " selCol " + selCol);
        if (selRow != -1) {
          String currentValue = (String) table.getValueAt( selRow, selCol);
          // System.out.println( "currentValue " + currentValue);
          if (selCol == table.getStepNumberColumn()) {
            // System.out.println( "Col popup menu for step " + currentValue);
            popupMenuForStepNumberColumn( currentValue, table);
            table.setSelectedCol( -1);
          }
        }
      }
    }
  } // end class DBTransColTableListener


  private void popupMenuForStepNumberColumn( final String stepNumberStr,
                                             final DBTransactionTable table) {
    VizView vizView = table.getVizView();
    ViewListener viewListener = null;
    PwPlanningSequence planSequence = null;
    if (vizView instanceof PartialPlanView) {
      planSequence = PlanWorks.getPlanWorks().getPlanSequence
        ( ((PartialPlanView) vizView).getPartialPlan());
    } else if (vizView instanceof SequenceView) {
      planSequence = ((SequenceView) vizView).getPlanSequence();
    }
    ViewGenerics.partialPlanViewsPopupMenu
      ( Integer.parseInt( stepNumberStr), planSequence,
        vizView, new Point( 0, 0), viewListener);
//     table.setSelectedRow( -1);
//     table.setSelectedCol( -1);
  } // end popupMenuForStepNumberColumn

  // set header font and background color
  class TransactionHeaderRenderer extends DefaultTableCellRenderer {

    private VizView vizView;

    public TransactionHeaderRenderer( final VizView vizView, final DBTransactionTable table) {
      super();
      this.vizView = vizView;

      setOpaque( true);
      setHorizontalAlignment( CENTER);
      setVerticalAlignment( CENTER);
      setForeground( ColorMap.getColor( "black"));
      setBackground( ColorMap.getColor( "gray60"));
      setFont( table.getHeaderFont());
      // top, left, bottom, right
      setBorder( BorderFactory.createCompoundBorder
                 ( UIManager.getBorder( "TableHeader.cellBorder"),
                   BorderFactory.createEmptyBorder( 0, 2, 0, 0)));
    } // end constructor 
  
    protected final void setValue( final Object value) {
      setText( (value == null) ? "" : value.toString());
    }

    /**
     * <code>getTableCellRendererComponent</code>
     *
     * @param table - <code>JTable</code> - 
     * @param value - <code>Object</code> - 
     * @param isSelected - <code>boolean</code> - 
     * @param hasFocus - <code>boolean</code> - 
     * @param row - <code>int</code> - 
     * @param column - <code>int</code> - 
     * @return - <code>Component</code> - 
     */
    public final Component getTableCellRendererComponent( final JTable table, final Object value,
                                                          final boolean isSelected,
                                                          final boolean hasFocus, final int row,
                                                          final int column) {
      setValue( value);
      return this;
    } // end getTableCellRendererComponent

  } // end class TransactionHeaderRenderer


  // set selected color
  class TransactionCellRenderer extends DefaultTableCellRenderer {

    private VizView vizView;

    public TransactionCellRenderer( final VizView vizView, final DBTransactionTable table) {
      super();
      this.vizView = vizView;

      setOpaque( true);
      setHorizontalAlignment( LEFT);
      setVerticalAlignment( CENTER);
      setForeground( ColorMap.getColor( "black"));
      setBackground( ViewConstants.VIEW_BACKGROUND_COLOR);
      setFont( vizView.getFont());
      // top, left, bottom, right
//       setBorder( BorderFactory.createCompoundBorder
//                  ( UIManager.getBorder( "TableHeader.cellBorder"),
//                    BorderFactory.createEmptyBorder( 0, 2, 0, 0)));
    } // end constructor 
  
    protected final void setValue( final Object value) {
      setText( (value == null) ? "" : value.toString());
    }

    /**
     * <code>getTableCellRendererComponent</code>
     *
     * @param table - <code>JTable</code> - 
     * @param value - <code>Object</code> - 
     * @param isSelected - <code>boolean</code> - 
     * @param hasFocus - <code>boolean</code> - 
     * @param row - <code>int</code> - 
     * @param column - <code>int</code> - 
     * @return - <code>Component</code> - 
     */
    public final Component getTableCellRendererComponent( final JTable table, final Object value,
                                                          final boolean isSelected,
                                                          final boolean hasFocus, final int row,
                                                          final int column) {
      if (isSelected) {
        // System.err.println( "row " + row + " col " + column + " is selected");
        setBorder( new LineBorder( ViewConstants.PRIMARY_SELECTION_COLOR, 2));
      } else {
        setBorder( new LineBorder( ViewConstants.VIEW_BACKGROUND_COLOR));    
      }
      setValue( value);
      return this;
    } // end getTableCellRendererComponent

  } // end class TransactionCellRenderer



} // end class DBTransactionTable


