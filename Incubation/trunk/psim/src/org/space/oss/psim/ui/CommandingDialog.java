package org.space.oss.psim.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import org.space.oss.psim.Command;
import org.space.oss.psim.CommandArg;
import org.space.oss.psim.CommandDescriptor;
import org.space.oss.psim.CommandService;
import org.space.oss.psim.GroundStation;
import org.space.oss.psim.command.CommandArgValues;
import org.space.oss.psim.command.CommandImpl;

public class CommandingDialog extends JPanel 
{
	private static final long serialVersionUID = 1L;

	protected CommandService cmdService_;
	protected JComboBox cmdList_;
	protected JComboBox gsList_;
	protected CommandArgValues cmdArgValues_;
	protected JPanel cmdLauncherPanel_;
	protected JPanel cmdArgsPanel_;
	protected Map<String,JTextField> cmdArgTextValues_;
	
	public CommandingDialog(CommandService cmd)
	{
	    cmdService_ = cmd;	
	    
	    setupLayout();
	}
	
	protected void setupLayout()
	{
    	JPanel topPanel = new JPanel(new FlowLayout());
    	cmdList_ = new JComboBox(cmdService_.getCommandDictionary().toArray());
    	cmdList_.addItemListener(new ItemListener() {
    		public void itemStateChanged(ItemEvent e)  
    		{
    			if (e.getStateChange()==ItemEvent.SELECTED)
    		        resetArgsPanel();	
    		} 
    	});
    	topPanel.add(new JScrollPane(cmdList_));
    	
    	JPanel bottomPanel = new JPanel(new FlowLayout());  

    	JPanel gsPanel = new JPanel(new FlowLayout());
    	gsList_ = new JComboBox(cmdService_.getGroundStations().toArray());
    	gsPanel.add(new JScrollPane(gsList_));
    	bottomPanel.add(gsPanel);
        
    	//JPanel btnPanel = new JPanel(new FlowLayout());
    	JButton btn = new JButton("Execute");
    	btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) 
    		{
				collectCmdArgValues();
    			executeCmd(
    				(GroundStation)gsList_.getSelectedItem(),
    				(CommandDescriptor)cmdList_.getSelectedItem(),
					cmdArgValues_);
    		} 
    	});
    	bottomPanel.add(btn);
    	
    	btn = new JButton("Queue");
    	btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) 
    		{
				collectCmdArgValues();
    			queueCmd(
    				(GroundStation)gsList_.getSelectedItem(),
    				(CommandDescriptor)cmdList_.getSelectedItem(),
					cmdArgValues_);
    		} 
    	});
    	bottomPanel.add(btn);
    	//bottomPanel.add(btnPanel);
    	    	
    	cmdLauncherPanel_ = new JPanel(new BorderLayout());
    	cmdLauncherPanel_.add(BorderLayout.NORTH,topPanel);
    	cmdArgsPanel_=new JPanel(new BorderLayout());
    	cmdLauncherPanel_.add(BorderLayout.CENTER,cmdArgsPanel_);
    	cmdLauncherPanel_.add(BorderLayout.SOUTH,bottomPanel);
    	
    	setLayout(new FlowLayout());
    	add(cmdLauncherPanel_);
    	
    	resetArgsPanel();	
	}
	
	protected void collectCmdArgValues()
	{
		cmdArgValues_ = new CommandArgValues();
        CommandDescriptor od = (CommandDescriptor)cmdList_.getSelectedItem();
        CommandArg args[]=od.getArgs();
        for (int i=0;i<args.length;i++) {
        	JTextField tf = cmdArgTextValues_.get(args[i].name);
        	cmdArgValues_.put(args[i].name,tf.getText());
        }		
	}
	
    void resetArgsPanel()
    {
    	cmdArgTextValues_ = new HashMap<String, JTextField>();
        CommandDescriptor od = (CommandDescriptor)cmdList_.getSelectedItem();
        CommandArg args[]=od.getArgs();
        JPanel p = new JPanel(new GridLayout(args.length,2));
        for (int i=0;i<args.length;i++) {
        	p.add(new JLabel(args[i].description));
        	JTextField tf =new JTextField(args[i].defaultValue); 
        	p.add(tf);
        	cmdArgTextValues_.put(args[i].name,tf);
        }
        cmdArgsPanel_.removeAll();
        cmdArgsPanel_.add(BorderLayout.CENTER,p);
        cmdLauncherPanel_.revalidate();
    }	
    
    protected void executeCmd(GroundStation gs, CommandDescriptor cd, CommandArgValues argValues)
    {
    	Command c = makeCommand(cd,argValues);
    	gs.sendCommand(c);
    }
    
    protected void queueCmd(GroundStation gs, CommandDescriptor cd, CommandArgValues argValues)
    {
    	Command c = makeCommand(cd,argValues);
    	gs.queueCommand(c);
    }    
    
    protected Command makeCommand(CommandDescriptor cd, CommandArgValues argValues)
    {
    	return new CommandImpl(cd.getName(),argValues);
    }
}
