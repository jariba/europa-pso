package org.space.oss.psim.ui

import java.awt.FlowLayout
import java.awt.GridLayout
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.text.SimpleDateFormat
import java.util.Date
import javax.swing.{JButton,JLabel,JPanel,JTextField}
import org.space.oss.psim.PSimEventManager
import org.space.oss.psim.Start

class EventManagerDialog(val eventManager:PSimEventManager) extends JPanel {
	val mainPanel:JPanel = new JPanel(new GridLayout(2,2))
	val formatter:SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z")
	val stopTime:JTextField = new JTextField("")
	val currentTime = new JLabel(formatter.format(eventManager.getCurrentTime))
	
	def init() { 
		setLayout(new FlowLayout())
			
		val btn = new JButton("Play to Time:")
		//val days = 4
		//val msecsPerDay = 24*60*60*1000
		val st = new Date(eventManager.getCurrentTime+100)
		stopTime.setText(formatter.format(st))
		mainPanel.add(new JLabel("Current Time:")) ; mainPanel.add(currentTime)
		mainPanel.add(btn) ; mainPanel.add(stopTime)
		
		btn.addActionListener(new ActionListener() {
			override def actionPerformed(e:ActionEvent) 
    		{
			  runSimulation
    		}
		})
				
		add(mainPanel)
		eventManager.psim.getTimeService.addObserver(handleCurrentTime)
	}
	
	def runSimulation() {
	  try {
	    val t = formatter.parse(stopTime.getText()).getTime()
	    eventManager.maxTime = t
	    eventManager ! Start
	  }
	  catch {
	    case e:Exception =>
	      e.printStackTrace
	  }
	}
	
	def handleCurrentTime(t:Long) {
		currentTime.setText(formatter.format(t));
		mainPanel.revalidate();
	}
}
