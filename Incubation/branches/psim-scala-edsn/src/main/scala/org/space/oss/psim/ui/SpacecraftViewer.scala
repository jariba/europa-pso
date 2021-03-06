package org.space.oss.psim.ui

import javax.swing.JPanel
import java.awt.BorderLayout
import org.space.oss.psim.Spacecraft
import org.space.oss.psim.PSimObserver
import javax.swing.table.AbstractTableModel
import javax.swing.JTabbedPane
import javax.swing.JScrollPane
import javax.swing.JTable
import org.space.oss.psim.spacecraft.ExecutedCommand

class SpacecraftViewer(sc:Spacecraft) extends JPanel {
  val cmdTraceTable = new JTable(new CommandTraceTM(sc))
  
  def init() {
	  val tp = new JTabbedPane()
	  tp.add("Command Trace",makeCommandTracePane())
	  
	  setLayout(new BorderLayout())
	  add(tp) //new JScrollPane(tp))		  
  }
  
  def makeCommandTracePane(): JPanel = {
    val p = new JPanel(new BorderLayout())
    p.add(new JScrollPane(cmdTraceTable))
    p
  }

  def scrollCmdTrace(str: String) {
      var row = -1
      for (i <- 0 until cmdTraceTable.getRowCount)
        for (j <- 0 until cmdTraceTable.getColumnCount)
          if (cmdTraceTable.getValueAt(i,j).toString.contains(str)) 
            row = i
      
      if (row != -1) {
    	  cmdTraceTable.scrollRectToVisible(cmdTraceTable.getCellRect(cmdTraceTable.getRowCount-1, 0, false))
    	  cmdTraceTable.scrollRectToVisible(cmdTraceTable.getCellRect(row, 0, false))
    	  this.repaint()
      }
  }
  
  class CommandTraceTM(sc:Spacecraft) extends AbstractTableModel with PSimObserver {
		sc.addObserver(this)

		override def getColumnCount: Int =  1
		override def getRowCount(): Int = sc.getCommandTrace.length
		override def getValueAt(row:Int, col:Int): Object = {
			if (col == 0)
				sc.getCommandTrace(row).toString()
			else
			  throw new RuntimeException("Unexpected col number:"+col);
		}

		override def getColumnName(col:Int):String = {
			if (col == 0)
				"Command"
			else	
				throw new RuntimeException("Unexpected col number:"+col);			
		}
		
		override def getColumnClass(columnIndex: Int) = classOf[String]

		override def handleEvent(eventType: Int, event:Any) 
		{
		    if (eventType == Spacecraft.EXECUTED_COMMAND) {
		        //println("received event:"+event)
		        fireTableDataChanged();
		    }
		}		
	}
  
}