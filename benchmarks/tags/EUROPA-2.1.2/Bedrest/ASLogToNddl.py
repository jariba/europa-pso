import sys
import re
import StringIO


def cleanArg(arg):
    return arg.strip('"), ')

actTypes = []
for type in open('SubjectActivities.txt'):
    actTypes.append(type.strip('\n'))

def getValidActType(t):
    for type in actTypes:
        if t.startswith(type):
            return type;
    return None    
        

containers = {}
activities = {}
containerType ='Subject' # TODO: pass as parameter?

class Container:
    def __init__(self,name):
        self.name = name
        self.activities = []
        
    def toNddl(self):
        buf = StringIO.StringIO()
        print >>buf,'%s %s = new %s();' % (containerType,self.name,containerType)
        return buf.getvalue()                         
    
class Activity:
    def __init__(self,container,name,type,duration):
        self.container = container
        self.name = name
        self.type = type
        self.duration = duration
        self.start = '0'        
                    
    def toNddl(self):
        buf = StringIO.StringIO()
        print >>buf,'goal(%s.%s %s);' % (self.container.name,self.type,self.name)
        print >>buf,'eq(%s.duration,%s);' % (self.name,self.duration)
        print >>buf,'eq(%s.start,%s);' % (self.name,self.start)
        return buf.getvalue()         

    def toActEntry(self):
        buf = StringIO.StringIO()
        print >>buf,'goals.add(new ActivityEntry("%s","%s","%s",%s,%s));' % (self.container.name,self.type,self.name,self.start,self.duration)
        return buf.getvalue()         
                            
filename = sys.argv[1]
methodRE = re.compile('.[\w\s]+\(')
argsRE = re.compile('["\w\s"]+[,|\)]')
for line in open(filename):
    if (methodRE.search(line)):
        methodName = methodRE.findall(line)[0].lstrip('.').rstrip('(')
        methodArgs = argsRE.findall(line)

        for i in xrange(len(methodArgs)):
            methodArgs[i] = cleanArg(methodArgs[i]) # TODO: sometimes we may want to keep the quotes
        # print methodName,methodArgs
        
        if (methodName == 'registerContainer'):
            containerName = methodArgs[0].replace(' ','')
            lastContainer = Container(containerName)
            containers[containerName] = lastContainer
            
        if (methodName == 'registerActivity'):
            actName = methodArgs[0]
            actType = getValidActType(methodArgs[2])
            if (actType != None):
                actDuration = methodArgs[1]
                act = Activity(lastContainer,actName,actType,actDuration)
                activities[actName] = act 
                lastContainer.activities.append(act)
            else:
                print '// Skipped unrecognized act type:'+methodArgs[2]
                
        if (methodName == 'setActivityStartTime'):
            actName = methodArgs[0]
            act = activities.get(actName) 
            if (act != None):
                actStart = methodArgs[1]
                act.start = actStart
            
print '// File generated from '+filename
print '''
#include "Y-model.nddl"

PlannerConfig plannerConfiguration = new PlannerConfig(-10, +100000000, 10000, 10000);
System system = new System();
ContainerObj Containers = new ContainerObj();
'''            

for c in containers.itervalues():
    print c.toNddl().strip('\n')
    
print 'close();'
print ''
print '// Generated '+str(len(activities))+' activities'
print ''


#for c in containers.itervalues():
#    for act in c.activities:
#        print act.toNddl()

goalsFile = open('goals.bsh','w+')                
for c in containers.itervalues():
    for act in c.activities:
        print >>goalsFile, act.toActEntry()
        