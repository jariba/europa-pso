package org.ops.ui.util;

import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.lang.reflect.Method;

import javax.swing.JTable;
import javax.swing.table.TableModel;
import javax.swing.table.AbstractTableModel;

public class Util 
{
    public static JTable makeTable(List l,String fields[])
    {
    	JTable table =  new JTable(Util.makeTableModel(l,fields));
    	return table;
    }
    
    public static TableModel makeTableModel(List l, String fields[]) 
    {
        return new ListTableModel(l,fields);	
    }
    
    protected static class ListTableModel
        extends AbstractTableModel
    {
		private static final long serialVersionUID = -4844388715602653749L;

		protected List data_;
		protected List<String> columnNames_;
		protected List<Method> columnMethods_;
		
		public ListTableModel(List l,String fields[])
    	{
			data_ = new Vector();
			columnNames_ = new Vector<String>();
			columnMethods_ = new Vector<Method>();

			if (l == null) 
				return;
			
			data_ = l;
			if (fields != null) {
			    columnNames_ = Arrays.asList(fields);
			    mapFields(fields);
			}
			else {
				mapFields();
			}
    	}

		protected void mapFields(String fields[])
		{
			if (data_.size() == 0)
				return;
			
			Class c = data_.get(0).getClass();
			
			int i=0;
			try {
			    for (String s : fields) {
			    	String name = (s.equals("toString") ? s : "get"+s);
			    	columnMethods_.add(c.getMethod(name, (Class[])null));
			    	i++;
			    }
			}
			catch (Exception e) {
				// TODO: use logger
				System.err.println("Error mapping field "+fields[i]+" : "+e.getMessage());
			}
		}

		protected void mapFields()
		{
			columnNames_ = new Vector<String>();
			
			if (data_.size() == 0)
				return;

			Class c = data_.get(0).getClass();			
			
			try {
			    for (Method m : c.getMethods()) { 
			    	Class paramTypes[] = m.getParameterTypes();
			    	if (m.getName().startsWith("get") && 
			    		(paramTypes==null || paramTypes.length==0)) {
			    		columnMethods_.add(m);
			    		columnNames_.add(m.getName().substring(3));
			    	}
			    }
			}
			catch (Exception e) {
				// TODO: use logger
				System.err.println("Error mapping field : "+e.getMessage());
			}
		}
		
		public int getColumnCount() {  return columnNames_.size(); }
		public String getColumnName(int columnIndex) { return columnNames_.get(columnIndex); }
		
		public int getRowCount() { return data_.size(); }

		public Object getValueAt(int rowIndex, int columnIndex) 
		{
			try {
		        Object o = data_.get(rowIndex);
		        Method m = columnMethods_.get(columnIndex);
		        
		        return m.invoke(o, (Object[])null);
			}
			catch (Exception e) {
				return e.getMessage();				
			}
		}
    }
    
    /*
     * Transform SWIG generated lists into Java lists
     * parameter list must support size() and get(int idx)
     */
    public static List<Object> SWIGList(Object list)
    {
    	try {
    	    List<Object> retval = new Vector<Object>();
    	
    	    Method m = list.getClass().getMethod("size", (Class[])null);
    	    int size = (Integer)m.invoke(list,(Object[])null);
    	    m = list.getClass().getMethod("get", new Class[]{int.class});
    	    Object args[] = new Object[1];
    	    for (int i=0; i<size;i++) {
    	    	args[0] = i;
    	    	retval.add(m.invoke(list, args));
    	    }
    	    	
        	return retval;
    	}
    	catch (Exception e) {
    		throw new RuntimeException(e);
    	}
    }
}
