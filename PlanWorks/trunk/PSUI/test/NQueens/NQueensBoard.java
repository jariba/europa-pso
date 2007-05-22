
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.FontMetrics;
import java.awt.Insets;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseEvent;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.*;
import javax.swing.*;
import psengine.*;


/**
 */
public class NQueensBoard 
    extends JComponent 
{
	protected Dimension preferredSize = new Dimension(400,600);

	protected int firstX_;
	protected int firstY_;
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
		//Add a border of 5 pixels at the left and bottom,
		//and 1 pixel at the top and right.
		setBorder(BorderFactory.createMatteBorder(1,5,5,1,
				Color.BLUE));
		
		setBackground(Color.WHITE);
		setOpaque(true);
		this.addMouseMotionListener(new TGDMouseMotionAdapter());
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
		firstX_ = insets.left+100;
		firstY_ = insets.top+20;

		//Paint background if we're opaque.
		if (isOpaque()) {
			g.setColor(getBackground());
			g.fillRect(0, 0, getWidth(), getHeight());
		}
		
		g.setColor(Color.GRAY);
		
		// Draw option descriptions
		FontMetrics metrics = g.getFontMetrics();		 
		firstX_ = 10; 
			
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
			drawQueen(g,col,row-1);
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
		Insets insets = getInsets();
		// hack! : harcoded lastX and lastY for now
		int lastX = firstX_+(queenCnt_*gridSpace); //getWidth() - insets.right;
		int lastY = firstY_+(5*gridSpace); //getHeight() - insets.bottom;
		
		//Draw vertical lines.
		int x = firstX_;
		while (x <= lastX) {
			g.drawLine(x, firstY_, x, lastY);
			x += gridSpace;
		}
		
		//Draw horizontal lines.
		// TODO: how to draw sequre boxoes that account for X-Y differences
		int y = firstY_;
		while (y <= lastY) {
			g.drawLine(firstX_, y, lastX, y);
			y += gridSpace;
		}
	}	
	
	protected int gridSize() { return (int)(gridSize_*zoomFactor_); }
	
	private class TGDMouseMotionAdapter
	    extends MouseMotionAdapter
	{
		 public void mouseMoved(MouseEvent e)
		 {
		 	int x = e.getX();
		 	int y = e.getY();
		 	
		 	int queen=-1;
		 	
		 	// TODO: Find queen that overlaps (x,y), if any
		 	
		 	/*
		 	double minDist=Double.MAX_VALUE;
            for (int i=0;i<solution_.getTourCnt();i++) {
            	List tour = solution_.getTour(i);
            	for (int j=0;j<tour.size();j++) {
            		TSPCity c = (TSPCity)tour.get(j);
                    double dist = getDist(x,y,getXCoord(c.x),getYCoord(c.y)); 
                    if (dist<5 && dist<minDist) {
                    	minDist = dist;
                    	closestCity=c;
                    }
            	}
            }
		 	*/
		 	
		 	if (queen==-1) {
		 		//dialog_.setMouseInfo("");
		 		System.out.println("No Queen selected");
		 	}
		 	else {
		 		//dialog_.setMouseInfo(closestCar.toString());
		 		System.out.println(getViolationExpl(queen));
		 	}
		 }
		 
		 protected double getDist(int x1,int y1,int x2,int y2)
		 {
		     return Math.pow(Math.pow(x1-x2,2)+Math.pow(y1-y2,2),0.5);	
		 }
	}
}
