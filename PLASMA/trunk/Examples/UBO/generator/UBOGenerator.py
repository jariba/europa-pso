import sys
import StringIO

class Successor:
    def __init__(self,id,d):
        self.actId = id
        self.distance = d
    
    def toString(self):
        buf = StringIO.StringIO()
        print >>buf, '{%d,%d}' % (self.actId,self.distance), 
        return buf.getvalue()
    
class Activity:
    def __init__(self):
        self.id = -1
        self.duration = 0
        self.resUsages = []
        self.succs = []
    
    def toString(self):
        buf = StringIO.StringIO()
        
        print >>buf,'Activity(',self.id,self.duration,
        print >>buf,' Successors:',
        for succ in self.succs:
            buf.write(succ.toString())
            
        print >>buf, ' ResUsage:{',
        for i in xrange(len(self.resUsages)):
            print >>buf, '%d,' % (self.resUsages[i],),
        print >>buf,'}',
               
        print >>buf,')',
        return buf.getvalue()
    
class Resource:
    def __init__(self):
        self.id = -1
        self.capacity = 0
    
    def toString(self):
        buf = StringIO.StringIO()
        print >>buf,'Resource(',self.id,',',self.capacity,')',
        return buf.getvalue()
    
class Problem:
    def __init__(self):
        self.resources = [] 
        self.activities = []
    
    def readActivity(self,line):
        words = line.split()
        actId = int(words[0])
        succCnt = int(words[2])
        
        act = Activity()
        act.id = actId
        for j in xrange(succCnt):
            succ = Successor(int(words[3+j]),int(words[3+j+succCnt].lstrip('[').rstrip(']')))
            act.succs.append(succ)            
        return act                

    def readResourceUsage(self,line,resourceCnt):
        words = line.split()
        actIdx = int(words[0])
        act = p.activities[actIdx]
        act.duration = int(words[2])
        for j in xrange(resourceCnt):
            act.resUsages.append(int(words[j+3]))

    def readFromFile(self,filename):
        self.filename = filename
        file = open(filename)
        line = file.readline()
        words = line.split()
        actCnt = int(words[0])
        resourceCnt = int(words[1])
            
        for i in xrange(actCnt+2):
            line = file.readline()
            self.activities.append(self.readActivity(line))            

        for i in xrange(actCnt+2):
           line = file.readline()
           self.readResourceUsage(line,resourceCnt)

        line = file.readline()
        words = line.split()
        for i in xrange(resourceCnt):
            r = Resource()
            p.resources.append(r)
            r.id = i
            r.capacity = int(words[i])
        
    def toString(self):
        buf = StringIO.StringIO()
        print >>buf, 'Problem'        
        for r in self.resources:
            print >>buf,'    ',r.toString()  
        for a in self.activities:
            print >>buf,'    ',a.toString()  
        return buf.getvalue()
    
    def toNddl(self):
        buf = StringIO.StringIO()
        
        print >>buf,'// Original file:'+self.filename
        print >>buf,'''
#include "UBO-model.nddl"

PlannerConfig c = new PlannerConfig(0, 1000, +inf, +inf );

ProblemInstance problem = new ProblemInstance();

// Proven minimum duration for this problem is 45

int maxDuration;
int maxDurationPlusOne;

maxDuration.specify(60);
addEq(maxDuration,1,maxDurationPlusOne);
'''        
        for r in self.resources:
            print >>buf,'CapacityResource resource%d = new CapacityResource( 0.0 , %d.0 );' % (r.id,r.capacity)  
        
        i=0
        for act in self.activities:
            for j in xrange(len(act.resUsages)):
                if (act.resUsages[j] > 0):
                    # TODO: generate at least one allocation per activity?
                    print >>buf,'Allocation a%d = new Allocation( resource%d , %d , %d.0 );' % (i,j,act.id,act.resUsages[j])
                    i+=1
        print >>buf,''
            
        for act in self.activities:
            print >>buf,'ActivityTimeline at%d = new ActivityTimeline(%d);' % (act.id,act.id)
        print >>buf,''
                
        print >>buf,'close();'
        print >>buf,''
                
        for act in self.activities:
            print >>buf,'goal( problem.Activity activity%d );' % (act.id,)
            print >>buf,'eq( activity%d.duration, %d );' % (act.id,act.duration)
            print >>buf,'eq( activity%d.m_identifier, %d);' % (act.id,act.id)
            print >>buf,'eq( activity%d.timeline, at%d);' % (act.id,act.id)
            print >>buf,''
                
        for act in self.activities:
            for succ in act.succs:
                if (succ.distance >= 0):
                    predId = act.id
                    succId = succ.actId
                    lb = str(succ.distance)
                    ub = '+inf'
                else:
                    predId = succ.actId
                    succId = act.id
                    lb = '-inf'
                    ub = str(-succ.distance)    
                print >>buf,'temporalDistance( activity%d.start, [ %s %s ], activity%d.start );' % (predId,lb,ub,succId)
        print >>buf,''
                
                
        for act in self.activities:
            print >>buf,'precedes(0,activity%d.start);precedes(activity%d.end,maxDuration);' % (act.id,act.id)
        print >>buf,''
                    
        for act in self.activities:
            print >>buf,'activity%d.activate();' % (act.id,)
                    
        return buf.getvalue()
        

filename = sys.argv[1]
#"/home/javier/research/RCPSP/problems/testset_ubo10/psp2.sch"
print "filename is:"+filename        
p = Problem()   
p.readFromFile(filename)
print p.toString()    
print p.toNddl()    
        
         