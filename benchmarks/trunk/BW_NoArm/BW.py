import os
import sys
import StringIO

class Block:
    def __init__(self,n):
        self.name = n

    def toString(self):
        return self.name

    def toNddl(self):
        buf = StringIO.StringIO()
        print >>buf, 'Block block'+self.name+' = new Block("Block '+self.name+'");',
            
        return buf.getvalue()
    
class Predicate:
    def __init__(self,n,a):
        self.name = n
        self.args = a

    def toToken(self):
        blockName = 'block'+self.args[0]
        if (self.name=='CLEAR'):
            return blockName+".top.Clear"
        if (self.name=='ONTABLE'):
            return blockName+".bottom.OnTable"
        if (self.name=='ON'):
            return blockName+".bottom.On"
        
    def toString(self):
        buf = StringIO.StringIO()
        print >>buf, self.name, ' ', self.args,
            
        return buf.getvalue()
            
    def toNddl(self,type,tokenName):
        buf = StringIO.StringIO()
        print >>buf, type+'('+self.toToken()+' '+tokenName+');',
        if (self.name=='ON'):
            print >>buf, '\neq('+tokenName+'.bottomBlock,block'+self.args[1]+');',
            
        return buf.getvalue()

class Fact(Predicate):
    def __init__(self,n,a):
        Predicate.__init__(self, n, a)

    def toNddl(self,tokenName):
        buf = StringIO.StringIO()
        print >>buf, Predicate.toNddl(self,'fact',tokenName)
        print >>buf, tokenName+'.start.specify(startHorizon);'
        print >>buf, tokenName+'.activate();'
            
        return buf.getvalue()

class Goal(Predicate):
    def __init__(self,n,a):
        Predicate.__init__(self, n, a)        

    def toNddl(self,tokenName):
        buf = StringIO.StringIO()
        print >>buf, Predicate.toNddl(self,'goal',tokenName)
        print >>buf, tokenName+'.start >= startHorizon;'
        print >>buf, tokenName+" meets problemGoal;"
        print >>buf, tokenName+'.activate();'
            
        return buf.getvalue()
        
class Problem:
    def __init__(self):
        self.name = 'noname'
        self.blocks = []
        self.facts = []
        self.goals = []
        
    def readFromFile(self,filename):
        self.filename = filename
        file = open(filename)
        try:
            lines = file.readlines()
            
            i = 0 # skip the :domain line
            
            # Get the problem name
            words = lines[i].split()
            self.name = words[2].rstrip(')')
            i+=1
            
            # Get the Blocks
            i+=1  # skip the :domain line
            words = lines[i].strip('(:objects').rstrip('- block)\n').split()
            for w in words:
                self.readBlock(w.rstrip(')'))
            i+=1
            
            # Get the Facts
            while (not lines[i].startswith('(:goal')):
                self.readFacts(lines[i].split('('))
                i+=1
            
            while (i < len(lines)):
                self.readGoals(lines[i].split('('))
                i+=1
                               
        finally:
            file.close()

    def readBlock(self,b):
        self.blocks.append(Block(b))
        
    def readFacts(self,words):
        for w in words:
            self.readFact(cleanPredicate(w))
        
    def readGoals(self,words):
        for w in words:
            self.readGoal(cleanPredicate(w))

    def readFact(self,f):
        words = f.split()
        if (len(words)>0 and isPredicate(words[0])):
            self.facts.append(Fact(words[0],words[1:]))
        
    def readGoal(self,g):
        words = g.split()
        if (len(words)>0 and isPredicate(words[0])):
            self.goals.append(Goal(words[0],words[1:]))
              
    def toString(self):
        buf = StringIO.StringIO()
        print >>buf, 'Problem ', self.name
        
        print >>buf, 'Blocks:'
        for b in self.blocks:
            print>>buf, b.toString()
            
        print >>buf, 'Facts:'
        for f in self.facts:
            print>>buf, f.toString()
            
        print >>buf, 'Goals:'
        for g in self.goals:
            print>>buf, g.toString()
            
        return buf.getvalue()

    def toNddl(self):
        buf = StringIO.StringIO()

        print >>buf,'// Original file:'+self.filename
        print >>buf,'''
#include "BW_NoArm-model.nddl"

int startHorizon, endHorizon;
startHorizon.specify(0);
endHorizon.specify(1000);
'''
        print >>buf, '// BLOCKS'
        for b in self.blocks:
            print>>buf, b.toNddl()
        
        print>>buf, '\nclose();\n'    
        
        i=0
        print >>buf, '// FACTS'
        for f in self.facts:
            print>>buf, f.toNddl('fact'+str(i))
            i+=1
        
        i=0    
        print >>buf, '// GOALS'
        for g in self.goals:
            print>>buf, g.toNddl('goal'+str(i))
            i+=1
           
        print>>buf, 'problemGoal.start >= startHorizon;'  
        print>>buf, 'problemGoal.end <= endHorizon;'  
        print>>buf, 'problemGoal.activate();'
            
        # TODO: 
        # - declare problemGoal
        # - ensure that Bottom.On has a subgoal: bottomBlock.top.NotClear
            
        return buf.getvalue()
            
def cleanPredicate(p):
    return p.rstrip('\n').rstrip(' ').rstrip(')')

def isPredicate(p):
    return p=='ON' or p=='ONTABLE' or p=='CLEAR' 

        