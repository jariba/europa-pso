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

import org.space.oss.psim.CommandArg;
import org.space.oss.psim.CommandDescriptor;
import org.space.oss.psim.PSim;
import org.space.oss.psim.command.CommandArgValues;

public class CommandingDialog extends JPanel 
{
	private static final long serialVersionUID = 1L;

	protected PSim psim_;
	protected JComboBox cmdList_;
	protected CommandArgValues cmdArgValues_;
	protected JPanel cmdLauncherPanel_;
	protected JPanel cmdArgsPanel_;
	protected Map<String,JTextField> cmdArgTextValues_;
	
	public CommandingDialog(PSim psim)
	{
	    psim_ = psim;	
	    
	    setupLayout();
	}
	
	protected void setupLayout()
	{
    	JPanel panel = new JPanel(new FlowLayout());
    	
    	cmdList_ = new JComboBox(psim_.getCommandService().getCommandDictionary().toArray());
    	cmdList_.addItemListener(new ItemListener() {
    		public void itemStateChanged(ItemEvent e)  
    		{
    			if (e.getStateChange()==ItemEvent.SELECTED)
    		        resetArgsPanel();	
    		} 
    	});
    	

    	panel.add(new JScrollPane(cmdList_));
    	JButton optBtn = new JButton("Execute");
    	optBtn.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) 
    		{
    			cmdArgValues_ = new CommandArgValues();
    	        CommandDescriptor od = (CommandDescriptor)cmdList_.getSelectedItem();
    	        CommandArg args[]=od.getArgs();
    	        for (int i=0;i<args.length;i++) {
    	        	JTextField tf = cmdArgTextValues_.get(args[i].name);
    	        	cmdArgValues_.put(args[i].name,tf.getText());
    	        }
    			
    			executeCmd(
    					(CommandDescriptor)cmdList_.getSelectedItem(),
						cmdArgValues_);
    		} 
    	});
    	
    	panel.add(optBtn);
    	
    	cmdLauncherPanel_ = new JPanel(new BorderLayout());
    	cmdLauncherPanel_.add(BorderLayout.NORTH,panel);
    	cmdArgsPanel_=new JPanel(new BorderLayout());
    	cmdLauncherPanel_.add(BorderLayout.CENTER,cmdArgsPanel_);
    	
    	setLayout(new FlowLayout());
    	add(cmdLauncherPanel_);
    	
    	resetArgsPanel();
		
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
    
    protected void executeCmd(CommandDescriptor cd, CommandArgValues argValues)
    {
    	throw new RuntimeException("executeCmd not implemented");
    }
}
