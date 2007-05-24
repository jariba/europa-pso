// Tabu Search NQueens Solver

import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;

class TSNQueensSolver
{
	protected PSEngine psengine_;
	protected Map tabuList_;
	protected int tabuTenure_=5;
	protected int curIteration_=0;
	protected int queenCnt_;
	
    public TSNQueensSolver(int n,PSEngine engine)
    {
        psengine_ = engine;	
        tabuList_ = new HashMap();
        queenCnt_ = n;
    }

    void init()
    {
        tabuList_.clear();
    	PSVariableList l = psengine_.getGlobalVariables();	
    	
    	for (int i=0;i<N;i++) {
        	PSVarValue value = PSVarValue.getInstance((int)(Math.random()*(queenCnt_-1)));
    		v = l.get(i);
    		v.specifyValue(value);
    	}    		        
    }
    
    void restart()
    {
    	init();
    	print("Restarted!");
    }
    
    PSVariable getQueenWithMaxViolation()
    {
    	double maxViolation = 0;
    	int maxVar=0;
    	
    	PSVariableList l = psengine_.getGlobalVariables();	
    	
    	for (int i=0;i<N;i++) {
    		PSVariable v = l.get(i);
    		if (v.getViolation() > maxViolation) {
    			maxVar = i;
    			maxViolation = v.getViolation();
    		}
    	}    			
    	
    	return l.get(maxVar);
    }

    static class Move
        implements Comparable
    {
    	public int slot_;
    	public double violation_;
    	
        public Move(int slot,double violation)
        {
        	slot_ = slot;
        	violation_ = violation;
        }
    	public int compareTo(Object obj) 
    	{
    		Move rhs = (Move)obj;

    		if (violation_ < rhs.violation_)
    			return -1;
    		else if (violation_ > rhs.violation_)
    			return 1;
    		else 
    			return 0;
    	}        
    }
    
    SortedSet getMoves(PSVariable queen,int curPos)
    {
    	SortedSet moves = new TreeSet();
    	
        for (int i=0;i<N;i++) {
        	if (i != curPos) {
        		PSVarValue value = PSVarValue.getInstance(i);
        		queen.specifyValue(value);
        		double v = psengine_.getViolation();
        		moves.add(new Move(i,v));
        	}
        }
        
        return moves;
    }

    public void solve(int maxIter)
    {
    	init();
    	
    	int bestIter = curIteration_;
        double bestCost = psengine.getViolation();
        
    	for (int i=0;psengine_.getViolation() > 0 && i < maxIter;i++) {
    	    PSVariable queenToMove = getQueenWithMaxViolation();
    		int curPos = queenToMove.getSingletonValue().asInt();
    	    SortedSet moves = getMoves(queenToMove,curPos);
    	    
    	    boolean moved = false;
            Iterator it = moves.iterator();
            while (it.hasNext() && !moved) {
            	moved = makeMove(queenToMove,curPos,(Move)it.next(),false);
            }
            
            if (!moved) {
            	print("Forced move!");
            	makeMove(queenToMove,curPos,(Move)moves.first(),true);
                // TODO: restart?
            }
            
            double cost = psengine.getViolation();
            if (cost < bestCost) {
            	bestCost = cost;
            	bestIter = curIteration_;
            }
            
	        print(i+":"+queensToString());            
            curIteration_++;
            
            if (curIteration_-bestIter > 50) {
            	restart();
            	bestIter = curIteration_;
            }
    	}
    }
    
    void addToTabuList(PSVariable queenToMove,int orig,int dest)
    {
        String key = queenToMove.getName() + "_" +orig+"_"+dest;
        tabuList_.put(key,curIteration_+tabuTenure_);        
    }
    
    boolean isTabu(PSVariable queenToMove,int orig,int dest)
    {
        String key = queenToMove.getName() + "_" +orig+"_"+dest;
        Integer iteration = (Integer)tabuList_.get(key);
        if (iteration == null)
        	return false;
        
        return (iteration.intValue() > curIteration_);
    }
    
    boolean makeMove(PSVariable queenToMove,int curPos,Move m,boolean force)
    {
    	if (force || !isTabu(queenToMove,curPos,m.slot_)) {
		    PSVarValue value = PSVarValue.getInstance(m.slot_);
		    queenToMove.specifyValue(value);
		    print("Moved queen "+queenToMove.getName()+" from "+curPos+" to "+m.slot_); 
	        addToTabuList(queenToMove,curPos,m.slot_);
	        return true;
    	}
    	
    	return false;
    }
}

