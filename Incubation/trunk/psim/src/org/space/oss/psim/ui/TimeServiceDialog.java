package org.space.oss.psim.ui;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.space.oss.psim.TimeService;
import org.space.oss.psim.TimeServiceObserver;

public class TimeServiceDialog extends JPanel 
	implements TimeServiceObserver
{
	private static final long serialVersionUID = 1L;

	protected TimeService timeService_;
	protected JLabel currentTimeLbl_; 
	protected JTextField newTime_;
	protected JPanel mainPanel_;
	
	public TimeServiceDialog(TimeService ts)
	{
		timeService_ = ts;
		
		setLayout(new FlowLayout());
		
		mainPanel_ = new JPanel(new GridLayout(2,2));
		
		currentTimeLbl_ = new JLabel("");
		mainPanel_.add(new JLabel("Current Time:"));
		mainPanel_.add(currentTimeLbl_);
		
	
		JButton btn = new JButton("Set Current Time:");
		newTime_ = new JTextField("");
		mainPanel_.add(btn);
		mainPanel_.add(newTime_);
		
		btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) 
    		{
				long newTime = 0;
				
				// TODO: parse long and date
				try {
					newTime = Long.parseLong(newTime_.getText());
				}
				catch (NumberFormatException ex) {
					// TODO: log error
					System.err.print("ERROR: failed to parse time from: "+newTime_.getText());
					return;
				}
				
				timeService_.setCurrentTime(newTime);
				newTime_.setText("");
    		}
		});
		
		add(mainPanel_);
		
		handleCurrentTime(timeService_.getCurrentTime());
		timeService_.addObserver(this);
	}

	public void dispose()
	{
		timeService_.removeObserver(this);
	}
	
	@Override
	public void handleCurrentTime(long t) 
	{
		currentTimeLbl_.setText(formatTime(t));
		mainPanel_.revalidate();
	}
	
	protected String formatTime(long t)
	{
		return Long.toString(t);
	}
}
