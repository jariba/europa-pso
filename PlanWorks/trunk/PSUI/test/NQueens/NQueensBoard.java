
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.FontMetrics;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.*;
import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import psengine.*;


/**
 */
public class NQueensBoard 
    extends JComponent 
{
	protected Dimension preferredSize = new Dimension(400,600);

	protected int firstX_;
	protected int lastX_;
	protected int firstY_;
	protected int lastY_;
	protected int rectWidth_ = 4;
	protected int rectHeight_ =8;
	protected int gridSize_=50;
	protected Color gridColor;
	protected double zoomFactor_;
    protected PSEngine psengine_;
    protected int queenCnt_;
    
    protected BufferedImage queenImage_ = null;
    protected ImageObserver imageObserver_ = null;
	
    // TODO: also support static solution
	public NQueensBoard(PSEngine engine, int queenCnt) 
	{		
		psengine_ = engine;
		queenCnt_ = queenCnt;
		zoomFactor_=1.0;
		
		setBackground(Color.WHITE);
		setOpaque(true);
		this.addMouseMotionListener(new TGDMouseInputAdapter());
		this.addMouseListener(new TGDMouseInputAdapter());
	}
	
	public double getZoomFactor() { return zoomFactor_; }
	public void setZoomFactor(double f) { zoomFactor_=f; }
	
	public Dimension getPreferredSize() 
	{
		return preferredSize;
	}
	
	protected void paintComponent(Graphics g) 
	{
		Insets insets = getInsets();
		firstX_ = insets.left+20;
		firstY_ = insets.top+20;
		lastX_ = firstX_+(queenCnt_*gridSize()); 
		lastY_ = firstY_+(queenCnt_*gridSize()); 
		

		//Paint background if we're opaque.
		if (isOpaque()) {
			g.setColor(getBackground());
			g.fillRect(0, 0, getWidth(), getHeight());
		}
		
		g.setColor(Color.GRAY);		
		drawQueens(g);
		drawGrid(g,gridSize());
	}

	protected List getQueenVars()
	{
		List retval = new Vector();
		
		PSVariableList l = psengine_.getGlobalVariables();
		for (int i=0;i<queenCnt_;i++) {
			PSVariable v = l.get(i);
			Integer value = v.getSingletonValue().asInt(); 
			retval.add(value);
		}
		return retval;
	}
	
	protected void drawQueens(Graphics g)
	{
		g.setPaintMode();
		g.setColor(Color.BLUE);
		List l = getQueenVars();
		for (int col=0;col<l.size();col++) {
			Integer row = (Integer)l.get(col);
			drawQueen(g,col,row);
		}
	}
	
	boolean isInViolation(int col)
	{
		PSVariable v = psengine_.getGlobalVariables().get(col);
		return (v.getViolation() > 0);
	}
	
	String getViolationExpl(int col)
	{
		PSVariable v = psengine_.getGlobalVariables().get(col);
		return v.getViolationExpl();
	}
	
	int getQueenValue(int col)
	{
		PSVariable v = psengine_.getGlobalVariables().get(col);
        return v.getSingletonValue().asInt();	    
	}
	
	protected Image getQueenImage()
	{
	    if (queenImage_ == null) {
			try {
			    queenImage_ = ImageIO.read(new File("BlackChessQueen.gif"));
			} 
			catch (IOException e) {
				queenImage_ = null;
				System.err.println("Couldn't load queen image!");
			}	    	
	    }
	    
	    return queenImage_;
	}

	protected ImageObserver getImageObserver()
	{
	    if (imageObserver_ == null) {
			imageObserver_ = new ImageObserver() {
		        public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height)
		        {
		        	return true;
		        }			
			};	    	
	    }
	    
	    return imageObserver_;
	}
	
	protected void drawQueen(Graphics g,
			               int col,
			               int row)
	{
		int rectWidth = (int)(rectWidth_*zoomFactor_);
		int rectHeight = (int)(rectHeight_*zoomFactor_);
		
		int xCorner = getXCoord(col);
		int yCorner = getYCoord(row);

		boolean isInViolation = isInViolation(col); 
		if (isInViolation) {
			g.setColor(Color.RED);
			g.fillRect(
				xCorner, 
				yCorner, 
				gridSize(), 
				gridSize()
			);					
			g.setColor(Color.BLUE);
		}
				
		Image queen = getQueenImage();
		if (queen == null)
			return;
		
		ImageObserver observer = getImageObserver();
		
		g.drawImage(
	        queen, 
            xCorner+5,
            yCorner+5,
            gridSize()-10,
            gridSize()-10,
            isInViolation ? Color.RED : Color.WHITE,
            observer);           
	}
	
	protected int getXCoord(Integer i)
	{
		int value = i.intValue();
		return (int)(firstX_+(value*gridSize()));
	}
	
	protected int getYCoord(Integer i)
	{
		int value = i.intValue();
		return (int)(firstY_+(value*gridSize()));
	}
	
	//Draws a gridSpace x gridSpace grid using the current color.
	private void drawGrid(Graphics g, int gridSpace) 
	{
		//Draw vertical lines.
		int x = firstX_;
		while (x <= lastX_) {
			g.drawLine(x, firstY_, x, lastY_);
			x += gridSpace;
		}
		
		//Draw horizontal lines.
		// TODO: how to draw sequre boxes that account for X-Y differences
		int y = firstY_;
		while (y <= lastY_) {
			g.drawLine(firstX_, y, lastX_, y);
			y += gridSpace;
		}
	}	
	
	protected int gridSize() { return (int)(gridSize_*zoomFactor_); }
	
	private class TGDMouseInputAdapter
	    extends MouseInputAdapter
	{
		 protected void getRowCol(int col,int row)
		 {		 
		 }
		 
		 public void mouseMoved(MouseEvent e)
		 {
		 	int x = e.getX();
		 	int y = e.getY();
		 	
		 	if (x < firstX_ || x >= lastX_ || 
		 	    y < firstY_ || y >= lastY_) {
		 		System.out.println("Mouse outside of board");
		        return;
		 	}

		 	int col = (x-firstX_) / gridSize();
		 	int row = (y-firstY_) / gridSize();
		 	
		 	int queenValue = getQueenValue(col);
		 	int queen = (queenValue == row ? col : -1);

		 	String mouseInfo="";
		 	if (queen!=-1) {
		 		mouseInfo = "Queen "+queen+" : "+getViolationExpl(queen);
		 	}
		 	else {
		 		mouseInfo = "No Queen selected";
		 	}
		 	System.out.println(mouseInfo);		 	
		 }
		 
		 public void mouseClicked(MouseEvent e)
		 {
		 	int x = e.getX();
		 	int y = e.getY();
		 	
		 	if (x < firstX_ || x >= lastX_ || 
		 	    y < firstY_ || y >= lastY_) {
		 		System.out.println("Mouse outside of board");
		        return;
		 	}

		 	int col = (x-firstX_) / gridSize();
		 	int row = (y-firstY_) / gridSize();
		 	
		 	int queenValue = getQueenValue(col);
		 	if (row != queenValue) {
		 		PSVariable v = psengine_.getGlobalVariables().get(col);
		 		PSVarValue value = PSVarValue.getInstance(row);
		 		v.specifyValue(value);
		 		repaint();
		 	}
		 }		 
	}
}
