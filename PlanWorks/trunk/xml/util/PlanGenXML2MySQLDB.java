import java.io.*;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Stack;
import java.util.StringTokenizer;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;

public class PlanGenXML2MySQLDB {
  private String seqDir;

  public static void main(String [] args) {
    PlanGenXML2MySQLDB converter = new PlanGenXML2MySQLDB();
    if(args.length == 0) {
      usage(null);
    }
    if(args.length != 2 && args.length != 1) {
      usage("Incorrect number of arguments.");
    }
    for(int i = 0; i < args.length; i++) { 
      if(args[i].equals("--input")) {
        i++;
        converter.setSeqDir(args[i]);
      }
      else if(args[i].equals("--usage") || args[i].equals("--help")) {
        usage(null);
      }
      else {
        usage("Invalid argument: " + args[i]);
      }
    }
    converter.convert();
  }
  private static void usage(String error) {
    if(error != null) {
      System.err.println("Error: " + error);
    }
    System.err.println("Usage: java PlanGenXML2MySQLDB --input <sequence dir>\n");
    System.exit(-1);
  }
  public void setSeqDir(String seqDir) {
    this.seqDir = seqDir;
  }
  public void convert() {
    System.err.println("Converting partial plans in " + seqDir);
    File dir = new File(seqDir);
    if(!dir.isDirectory()) {
      usage("Argument \"input\" is not a directory.");
    }
    if(!dir.canRead() || !dir.canWrite()) {
      usage("Must have read/write privileges in directory.");
    }
    File [] planFileList = dir.listFiles(new FilenameFilter () 
      {
        public boolean accept(File dir, String name) {
          return (name.indexOf(".xml") != -1);
        }
      });
    if(planFileList.length == 0) {
      usage("Directory is not a Planning Sequence directory.");
    }
    try {
      SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
      for(int i = 0; i < planFileList.length; i++) {
        System.err.println("Converting " + planFileList[i].getName());
        parser.parse(planFileList[i], new PlanGenXMLParser(planFileList[i]));
      }
    }
    catch(Exception e) {
      System.err.println(e.getMessage());
      e.printStackTrace();
    }
  }
}

class PlanGenXMLParser extends DefaultHandler {
  private File inputFile;
  private Stack state = new Stack();
  private String partialPlanName;
  private DbPartialPlan currPartialPlan;
  private DbTimeline currTimeline;
  private DbSlot currSlot;
  private DbVariable currVariable;
  private DbEnumeratedDomain currEnumDomain;
  private DbPredicate currPredicate;
  private DbObject currObject;
  private ArrayList objects;
  private ArrayList timelines;
  private ArrayList tokens;
  private ArrayList variables;
  private ArrayList intervalDomains;
  private ArrayList enumeratedDomains;
  private ArrayList predicates;
  private ArrayList parameters;
  private ArrayList constraints;
  private ArrayList slots;
  private DbParamVarTokenMap pvtm;
  private DbTokenRelations tr;
  private DbConstraintVarMap cvm;
  private int genericId = 1;
  private final Integer IN_PARTIALPLAN = new Integer(1);
  private final Integer IN_OBJECT = new Integer(2);
  private final Integer IN_TIMELINE = new Integer(4);
  private final Integer IN_SLOT = new Integer(8);
  private final Integer IN_TOKEN = new Integer(16);
  private final Integer IN_VARIABLE = new Integer(32);
  private final Integer IN_TOKENRELATION = new Integer(64);
  private final Integer IN_PREDICATE = new Integer(128);
  private final Integer IN_CONSTRAINT = new Integer(256);
  private final Integer IN_INTERVALDOMAIN = new Integer(512);
  private final Integer IN_ENUMERATEDDOMAIN = new Integer(1024);
  private final Integer IN_PARAMETER = new Integer(2048);
  
  public PlanGenXMLParser(File inputFile) {
    super();
    state.push(new Integer(0));
    this.inputFile = inputFile;
    partialPlanName = inputFile.getName().substring(0, inputFile.getName().indexOf(".xml"));
    objects = new ArrayList();
    timelines = new ArrayList();
    tokens = new ArrayList();
    variables = new ArrayList();
    intervalDomains = new ArrayList();
    enumeratedDomains = new ArrayList();
    predicates = new ArrayList();
    parameters = new ArrayList();
    constraints = new ArrayList();
    slots = new ArrayList();
  }
  public void startDocument() throws SAXException {
  }
  public void characters(char [] ch, int start, int length) throws SAXException {
    if(!state.peek().equals(IN_ENUMERATEDDOMAIN)) {
      throw new SAXException("Unexpected character data");
    }
    String domain = new String(ch, start, length);
    if(currVariable.getType().equals("OBJECT_VAR")) {
      domain = domain.replaceAll("K", "");
      domain = domain.replaceAll(",", "");
    }
    currEnumDomain.setDomain(domain);
  }
  public void endDocument() throws SAXException {
    System.err.println("Processing finished.  Outputting...");
    try {
      File partialPlanDir = new File(inputFile.getParent().concat(System.getProperty("file.separator")).concat(partialPlanName));
      System.err.println("Creating partial plan output directory " + partialPlanDir.getName());
      partialPlanDir.mkdir();
      System.err.println("Outputting constraints...");
      File outputFile = new File(partialPlanDir.toString().concat(System.getProperty("file.separator")).concat(partialPlanName).concat(".constraints"));
      outputFile.createNewFile();
      FileWriter outputWriter = new FileWriter(outputFile);
      for(int i = 0; i < constraints.size(); i++) {
        String constraint = ((DbConstraint)constraints.get(i)).toDbString();
        outputWriter.write(constraint, 0, constraint.length());
      }
      outputWriter.close();
      System.err.println("Outputting the constraint/variable map...");
      outputFile = new File(partialPlanDir.toString().concat(System.getProperty("file.separator")).concat(partialPlanName).concat(".constraintVarMap"));
      outputFile.createNewFile();
      outputWriter = new FileWriter(outputFile);
      String constraintVarMap = cvm.toDbString();
      outputWriter.write(constraintVarMap, 0, constraintVarMap.length());
      outputWriter.close();
      System.err.println("Outputting enumerated domains...");
      outputFile = new File(partialPlanDir.toString().concat(System.getProperty("file.separator")).concat(partialPlanName).concat(".enumeratedDomains"));
      outputFile.createNewFile();
      outputWriter = new FileWriter(outputFile);
      for(int i = 0; i < enumeratedDomains.size(); i++) {
        String enumDomain = ((DbEnumeratedDomain)enumeratedDomains.get(i)).toDbString();
        outputWriter.write(enumDomain, 0, enumDomain.length());
      }
      outputWriter.close();
      System.err.println("Outputting interval domains...");
      outputFile = new File(partialPlanDir.toString().concat(System.getProperty("file.separator")).concat(partialPlanName).concat(".intervalDomains"));
      outputFile.createNewFile();
      outputWriter = new FileWriter(outputFile);
      for(int i = 0; i < intervalDomains.size(); i++) {
        String intDomain = ((DbIntervalDomain)intervalDomains.get(i)).toDbString();
        outputWriter.write(intDomain, 0, intDomain.length());
      }
      outputWriter.close();
      System.err.println("Outputting objects...");
      outputFile = new File(partialPlanDir.toString().concat(System.getProperty("file.separator")).concat(partialPlanName).concat(".objects"));
      outputFile.createNewFile();
      outputWriter = new FileWriter(outputFile);
      for(int i = 0; i < objects.size(); i++) {
        String object = ((DbObject)objects.get(i)).toDbString();
        outputWriter.write(object, 0, object.length());
      }
      outputWriter.close();
      System.err.println("Outputting parameters...");
      outputFile = new File(partialPlanDir.toString().concat(System.getProperty("file.separator")).concat(partialPlanName).concat(".parameters"));
      outputFile.createNewFile();
      outputWriter = new FileWriter(outputFile);
      for(int i = 0; i < parameters.size(); i++) {
        String parameter = ((DbParameter)parameters.get(i)).toDbString();
        outputWriter.write(parameter, 0, parameter.length());
      }
      outputWriter.close();
      System.err.println("Outputting the parameter/variable/token map.");
      outputFile = new File(partialPlanDir.toString().concat(System.getProperty("file.separator")).concat(partialPlanName).concat(".paramVarTokenMap"));
      outputFile.createNewFile();
      outputWriter = new FileWriter(outputFile);
      String paramVarTokenMap = pvtm.toDbString();
      outputWriter.write(paramVarTokenMap, 0, paramVarTokenMap.length());
      outputWriter.close();
      System.err.println("Outputting the partial plan...");
      outputFile = new File(partialPlanDir.toString().concat(System.getProperty("file.separator")).concat(partialPlanName).concat(".partialPlan"));
      outputFile.createNewFile();
      outputWriter = new FileWriter(outputFile);
      String pp = currPartialPlan.toDbString();
      outputWriter.write(pp, 0, pp.length());
      outputWriter.close();
      System.err.println("Outputting predicates...");
      outputFile = new File(partialPlanDir.toString().concat(System.getProperty("file.separator")).concat(partialPlanName).concat(".predicates"));
      outputFile.createNewFile();
      outputWriter = new FileWriter(outputFile);
      for(int i = 0; i < predicates.size(); i++) {
        String predicate = ((DbPredicate)predicates.get(i)).toDbString();
        outputWriter.write(predicate, 0, predicate.length());
      }
      outputWriter.close();
      System.err.println("Outputting slots...");
      outputFile = new File(partialPlanDir.toString().concat(System.getProperty("file.separator")).concat(partialPlanName).concat(".slots"));
      outputFile.createNewFile();
      outputWriter = new FileWriter(outputFile);
      for(int i = 0; i < slots.size(); i++) {
        String slot = ((DbSlot)slots.get(i)).toDbString();
        outputWriter.write(slot, 0, slot.length());
      }
      outputWriter.close();
      System.err.println("Outputting timelines...");
      outputFile = new File(partialPlanDir.toString().concat(System.getProperty("file.separator")).concat(partialPlanName).concat(".timelines"));
      outputFile.createNewFile();
      outputWriter = new FileWriter(outputFile);
      for(int i = 0; i < timelines.size(); i++) {
        String timeline = ((DbTimeline)timelines.get(i)).toDbString();
        outputWriter.write(timeline, 0, timeline.length());
      }
      outputWriter.close();
      System.err.println("Outputting token relations...");
      outputFile = new File(partialPlanDir.toString().concat(System.getProperty("file.separator")).concat(partialPlanName).concat(".tokenRelations"));
      outputFile.createNewFile();
      outputWriter = new FileWriter(outputFile);
      String tokenRelations = tr.toDbString();
      outputWriter.write(tokenRelations, 0, tokenRelations.length());
      outputWriter.close();
      System.err.println("Outputting tokens...");
      outputFile = new File(partialPlanDir.toString().concat(System.getProperty("file.separator")).concat(partialPlanName).concat(".tokens"));
      outputFile.createNewFile();
      outputWriter = new FileWriter(outputFile);
      for(int i = 0; i < tokens.size(); i++) {
        if(tokens.get(i) == null) {
          System.err.println("null token " + i);
        }
        String token = ((DbToken)tokens.get(i)).toDbString();
        outputWriter.write(token, 0, token.length());
      }
      outputWriter.close();
      System.err.println("Outputting variables...");
      outputFile = new File(partialPlanDir.toString().concat(System.getProperty("file.separator")).concat(partialPlanName).concat(".variables"));
      outputFile.createNewFile();
      outputWriter = new FileWriter(outputFile);
      for(int i = 0; i < variables.size(); i++) {
        String variable = ((DbVariable)variables.get(i)).toDbString();
        outputWriter.write(variable, 0, variable.length());
      }
      outputWriter.close();
      System.err.println("Done.");
    }
    catch(Exception e) {
      e.printStackTrace();
      System.exit(-1);
    }
  }
  public void endElement(String uri, String localName, String qName) throws SAXException{
    state.pop();
  }
  public void startElement(String uri, String localName, String qname, Attributes attributes) 
  throws SAXException {
    if(qname.equals("PartialPlan")) {
      if(!((Integer)state.peek()).equals(new Integer(0))) {
        throw new SAXException("Unexpected PartialPlan element");
      }
      state.push(IN_PARTIALPLAN);
      String model = null;
      for(int i = 0; i < attributes.getLength(); i++) {
        if(attributes.getQName(i).indexOf("model") != -1) {
          model = attributes.getValue(i);
        }
      }
      currPartialPlan = new DbPartialPlan(model, partialPlanName);
      pvtm = new DbParamVarTokenMap(currPartialPlan.getId());
      tr = new DbTokenRelations(currPartialPlan.getId());
      cvm = new DbConstraintVarMap(currPartialPlan.getId());
    }
    else if(qname.equals("Object")) {
      if(!((Integer)state.peek()).equals(IN_PARTIALPLAN)) {
        throw new SAXException("Unexpected Object element.");
      }
      state.push(IN_OBJECT);
      String name = null;
      Integer id = null;
      for(int i = 0; i < attributes.getLength(); i++) {
        if(attributes.getQName(i).indexOf("key") != -1) {
          id = new Integer(attributes.getValue(i).substring(1));
          if(id.compareTo(new Integer(0)) == -1) {
            return;
          }
        }
        else if(attributes.getQName(i).indexOf("name") != -1) {
          name = attributes.getValue(i);
        }
        currObject = new DbObject(name, id, currPartialPlan.getId());
        objects.add(currObject);
      }
    }
    else if(qname.equals("Timeline")) {
      if(!((Integer)state.peek()).equals(IN_OBJECT)) {
        throw new SAXException("Unexpected Timeline element.");
      }
      state.push(IN_TIMELINE);
      String name = null;
      Integer id = null;
      for(int i = 0; i < attributes.getLength(); i++) {
        if(attributes.getQName(i).indexOf("name") != -1) {
          name = attributes.getValue(i);
        }
        else if(attributes.getQName(i).indexOf("key") != -1) {
          id = new Integer(attributes.getValue(i).substring(1));
          if(id.compareTo(new Integer(0)) == -1) {
            return;
          }
        }
      }
      currTimeline = new DbTimeline(name, id, currObject.getId(), currPartialPlan.getId());
      timelines.add(currTimeline);
    }
    else if(qname.equals("Slot")) {
      if(!((Integer)state.peek()).equals(IN_TIMELINE)) {
        throw new SAXException("Unexpected Slot element.");
      }
      state.push(IN_SLOT);
      Integer id = null;
      for(int i = 0; i < attributes.getLength(); i++) {
        if(attributes.getQName(i).indexOf("key") != -1) {
          id = new Integer(attributes.getValue(i).substring(1));
          if(id.compareTo(new Integer(0)) == -1) {
            return;
          }
        }
      }
      currSlot = new DbSlot(id, currTimeline.getId(), currObject.getId(), currPartialPlan.getId());
      slots.add(currSlot);
    }
    else if(qname.equals("Token")) {
      if(!((Integer)state.peek()).equals(IN_SLOT) && !((Integer)state.peek()).equals(IN_PARTIALPLAN)) {
        throw new SAXException("Unexpected Token element.");
      }
      byte isFreeToken = 0;
      if(state.peek().equals(IN_PARTIALPLAN)) {
        isFreeToken = 1;
      }
      state.push(IN_TOKEN);
      Integer id = null;
      byte isValueToken = 0;
      Integer startVar = null;
      Integer endVar = null;
      Integer durationVar = null;
      Integer rejectVar = null;
      Integer predicate = null;
      Integer objectVar = null;
      
      for(int i = 0; i < attributes.getLength(); i++) {
        if(attributes.getQName(i).indexOf("key") != -1) {
          id = new Integer(attributes.getValue(i).substring(1));
          if(id.compareTo(new Integer(0)) == -1) {
            return;
          }
        }
        else if(attributes.getQName(i).indexOf("isValueToken") != -1) {
          isValueToken = (attributes.getValue(i).equals("Y") ? (byte)1 : (byte)0);
        }
        else if(attributes.getQName(i).indexOf("predicateId") != -1) {
          predicate = new Integer(attributes.getValue(i).substring(1));
          if(predicate.compareTo(new Integer(0)) == -1) {
            predicate = null;
          }
        }
        else if(attributes.getQName(i).indexOf("startVarId") != -1) {
          startVar = new Integer(attributes.getValue(i).substring(1));
          if(startVar.compareTo(new Integer(0)) == -1) {
            startVar = null;
          }
        }
        else if(attributes.getQName(i).indexOf("endVarId") != -1) {
          endVar = new Integer(attributes.getValue(i).substring(1));
          if(endVar.compareTo(new Integer(0)) == -1) {
            endVar = null;
          }
        }
        else if(attributes.getQName(i).indexOf("durationVarId") != -1) {
          durationVar = new Integer(attributes.getValue(i).substring(1));
          if(durationVar.compareTo(new Integer(0)) == -1) {
            durationVar = null;
          }
        }
        else if(attributes.getQName(i).indexOf("rejectVarId") != -1) {
          rejectVar = new Integer(attributes.getValue(i).substring(1));
          if(rejectVar.compareTo(new Integer(0)) == -1) {
            rejectVar = null;
          }
        }
        else if(attributes.getQName(i).indexOf("paramVarIds") != -1) {
          StringTokenizer strTok = new StringTokenizer(attributes.getValue(i));
          while(strTok.hasMoreTokens()) {
            Integer pvk = new Integer(strTok.nextToken().substring(1));
            if(pvk.compareTo(new Integer(0)) != -1) {
              pvtm.relateTokenVariable(id, pvk);
            }
          }
        }
        else if(attributes.getQName(i).indexOf("objectVarId") != -1) {
          objectVar = new Integer(attributes.getValue(i).substring(1));
          if(objectVar.compareTo(new Integer(0)) == -1) {
            objectVar = null;
          }
        }
      }
      if(currTimeline != null && currObject != null && currSlot != null) {
        tokens.add(new DbToken(id, isFreeToken, isValueToken, startVar, endVar, durationVar,
                               rejectVar, objectVar, predicate, currSlot.getId(),
                               currTimeline.getId(), currObject.getId(), currPartialPlan.getId()));
      }
      else {
        tokens.add(new DbToken(id, isFreeToken, isValueToken, startVar, endVar, durationVar,
                               rejectVar, objectVar, predicate, null, null, null, 
                               currPartialPlan.getId()));
      }
    }
    else if(qname.equals("Variable")) {
      if(!state.peek().equals(IN_PARTIALPLAN)) {
        throw new SAXException("Unexpected Variable element.");
      }
      state.push(IN_VARIABLE);
      Integer id = null;
      String type = null;
      for(int i = 0; i < attributes.getLength(); i++) {
        if(attributes.getQName(i).indexOf("key") != -1) {
          id = new Integer(attributes.getValue(i).substring(1));
          if(id.compareTo(new Integer(0)) == -1) {
            return;
          }
        }
        else if(attributes.getQName(i).indexOf("type") != -1) {
          type = attributes.getValue(i);
        }
        else if(attributes.getQName(i).indexOf("constraintIds") != -1) {
          StringTokenizer strTok = new StringTokenizer(attributes.getValue(i));
          while(strTok.hasMoreTokens()) {
            Integer constraintId = new Integer(strTok.nextToken().substring(1));
            if(constraintId.compareTo(new Integer(0)) != -1) {
              cvm.addConstraint(constraintId, id);
            }
          }
        }
        else if(attributes.getQName(i).indexOf("paramId") != -1) {
          Integer pid = new Integer(attributes.getValue(i).substring(1));
          if(pid.compareTo(new Integer(0)) == -1) {
            pid = null;
            continue;
          }
          if(pid == null) {
            continue;
          }
          pvtm.relateVariableParam(id, pid);
        }
      }
      currVariable = new DbVariable(id, type, currPartialPlan.getId());
      variables.add(currVariable);
    }
    else if(qname.equals("IntervalDomain")) {
      if(!state.peek().equals(IN_VARIABLE)) {
        throw new SAXException("Unexpected IntervalDomain element");
      }
      state.push(IN_INTERVALDOMAIN);
      Integer id = new Integer(genericId++);
      String type = null;
      String lowerBound = null;
      String upperBound = null;
      for(int i = 0; i < attributes.getLength(); i++) {
        if(attributes.getQName(i).indexOf("type") != -1) {
          type = attributes.getValue(i);
        }
        else if(attributes.getQName(i).indexOf("lowerBound") != -1) {
          lowerBound = attributes.getValue(i);
        }
        else if(attributes.getQName(i).indexOf("upperBound") != -1) {
          upperBound = attributes.getValue(i);
        }
      }
      currVariable.setDomain("IntervalDomain", id);
      intervalDomains.add(new DbIntervalDomain(id, type, lowerBound, upperBound, 
                                               currPartialPlan.getId()));
    }
    else if(qname.equals("EnumeratedDomain")) {
      if(!((Integer)state.peek()).equals(IN_VARIABLE)) {
        throw new SAXException("Unexpected EnumeratedDomain element.");
      }
      state.push(IN_ENUMERATEDDOMAIN);
      Integer id = new Integer(genericId++);
      currVariable.setDomain("EnumeratedDomain", id);
      currEnumDomain = new DbEnumeratedDomain(id, currPartialPlan.getId());
      enumeratedDomains.add(currEnumDomain);
    }
    else if(qname.equals("TokenRelation")) {
      if(!state.peek().equals(IN_PARTIALPLAN)) {
        throw new SAXException("Unexpected TokenRelation element.");
      }
      state.push(IN_TOKENRELATION);
      Integer id = null;
      Integer tokenAId = null;
      Integer tokenBId = null;
      for(int i = 0; i < attributes.getLength(); i++) {
        if(attributes.getQName(i).indexOf("key") != -1) {
          id = new Integer(attributes.getValue(i).substring(1));
          if(id.compareTo(new Integer(0)) == -1) {
            return;
          }
        }
        else if(attributes.getQName(i).indexOf("masterToken") != -1) {
          tokenAId = new Integer(attributes.getValue(i).substring(1));
        }
        else if(attributes.getQName(i).indexOf("slaveToken") != -1) {
          tokenBId = new Integer(attributes.getValue(i).substring(1));
        }
      }
      tr.relateTokens(id, tokenAId, tokenBId);
    }
    else if(qname.equals("Constraint")) {
      if(!state.peek().equals(IN_PARTIALPLAN)) {
        throw new SAXException("Unexpected Constraint element.");
      }
      state.push(IN_CONSTRAINT);
      Integer id = null;
      String name = null;
      String type = null;
      for(int i = 0; i < attributes.getLength(); i++) {
        if(attributes.getQName(i).indexOf("key") != -1) {
          id = new Integer(attributes.getValue(i).substring(1));
          if(id.compareTo(new Integer(0)) == -1) {
            return;
          }
        }
        else if(attributes.getQName(i).indexOf("name") != -1) {
          name = attributes.getValue(i);
        }
        else if(attributes.getQName(i).indexOf("type") != -1) {
          type = attributes.getValue(i);
        }
      }
      constraints.add(new DbConstraint(id, name, type, currPartialPlan.getId()));
    }
    else if(qname.equals("Predicate")) {
      if(!((Integer)state.peek()).equals(IN_PARTIALPLAN)) {
        throw new SAXException("Unexpected Predicate element.");
      }
      state.push(IN_PREDICATE);
      Integer id = null;
      String name = null;
      for(int i = 0; i < attributes.getLength(); i++) {
        if(attributes.getQName(i).indexOf("key") != -1) {
          id = new Integer(attributes.getValue(i).substring(1));
          if(id.compareTo(new Integer(0)) == -1) {
            return;
          }
        }
        else if(attributes.getQName(i).indexOf("name") != -1) {
          name = attributes.getValue(i);
        }
      }
      currPredicate = new DbPredicate(id, name, currPartialPlan.getId());
      predicates.add(currPredicate);
    }
    else if(qname.equals("Parameter")) {
      if(!((Integer)state.peek()).equals(IN_PREDICATE)) {
        throw new SAXException("Unexpected Parameter element.");
      }
      state.push(IN_PARAMETER);
      Integer id = null;
      String name = null;
      for(int i = 0; i < attributes.getLength(); i++) {
        if(attributes.getQName(i).indexOf("key") != -1) {
          id = new Integer(attributes.getValue(i).substring(1));
          if(id.compareTo(new Integer(0)) == -1) {
            return;
          }
        }
        else if(attributes.getQName(i).indexOf("name") != -1) {
          name = attributes.getValue(i);
        }
      }
      parameters.add(new DbParameter(id, name, currPredicate.getId(), currPartialPlan.getId()));
    }
  }
  public void warning(SAXParseException spe) {
    System.err.println("SAX Warning: " + spe);
  }
}

class DbPartialPlan {
  private Long id;
  private String model;
  private String name;
  public DbPartialPlan(String model, String name) {
    id = new Long(System.currentTimeMillis());
    this.model = model;
    this.name = name;
  }
  public Long getId(){return id;}
  public String toDbString() {
    StringBuffer retval = new StringBuffer(name);
    retval.append("\t").append(id.toString()).append("\t").append(model).append("\t").append("-1");
    retval.append("\t").append("NULL").append("\t").append("NULL").append("\n");
    return retval.toString();
  }
}
  
class DbObject {
  private String name;
  private Integer id;
  private Long ppId;
  public DbObject(String name, Integer id, Long ppId) {
    this.name = name;
    this.id = id;
    this.ppId = ppId;
  }
  public Integer getId(){return id;}
  public String toDbString() {
    StringBuffer retval = new StringBuffer(id.toString());
    retval.append("\t").append(ppId.toString()).append("\t").append(name).append("\n");
    return retval.toString();
  }
}

class DbTimeline {
  private String name;
  private Integer id;
  private Integer oid;
  private Long ppId;
  public DbTimeline(String name, Integer id, Integer oid, Long ppId) {
    this.name = name;
    this.id = id;
    this.oid = oid;
    this.ppId = ppId;
  }
  public Integer getId(){return id;}
  public String toDbString() {
    StringBuffer retval = new StringBuffer(id.toString());
    retval.append("\t").append(oid.toString()).append("\t").append(ppId.toString()).append("\t");
    retval.append(name).append("\n");
    return retval.toString();
  }
}

class DbSlot {
  private Integer id;
  private Integer tid;
  private Integer oid;
  private Long ppId;
  public DbSlot(Integer id, Integer tid, Integer oid, Long ppId) {
    this.id = id;
    this.tid = tid;
    this.oid = oid;
    this.ppId = ppId;
  }
  public Integer getId() {return id;}
  public String toDbString() {
    StringBuffer retval = new StringBuffer(id.toString());
    retval.append("\t").append(tid.toString()).append("\t").append(ppId.toString()).append("\t");
    retval.append(oid.toString()).append("\n");
    return retval.toString();
  }
}

class DbParamVarTokenMap {
  private ArrayList tokens;
  private ArrayList variables;
  private ArrayList parameters;
  private Long ppId;
  public DbParamVarTokenMap(Long ppId) {
    this.ppId = ppId;
    tokens = new ArrayList();
    variables = new ArrayList();
    parameters = new ArrayList();
  }
  public void relateTokenVariable(Integer tokenId, Integer variableId) {
    if(tokens.indexOf(tokenId) == -1) {
      tokens.add(tokenId);
    }
    variables.ensureCapacity(tokens.size());
    variables.set(tokens.indexOf(tokenId), variableId);
  }
  public void relateVariableParam(Integer variableId, Integer paramId) {
    if(variables.indexOf(variableId) == -1) {
      variables.add(variableId);
    }
    int oldSize = parameters.size();
    parameters.ensureCapacity(variables.size());
    for(int i = oldSize; i < variables.size(); i++) {
      parameters.add(null);
    }
    parameters.set(variables.indexOf(variableId), paramId);
  }
  public String toDbString() {
    StringBuffer retval = new StringBuffer();
    for(int i = 0, n = tokens.size(); i < n; i++) {
      retval.append(variables.get(i).toString()).append("\t").append(tokens.get(i).toString());
      retval.append("\t").append(parameters.get(i)).append("\t").append(ppId.toString());
      retval.append("\n");
    }
    return retval.toString();
  }
}

class DbTokenRelations {
  private ArrayList relationIds;
  private ArrayList tokenAs;
  private ArrayList tokenBs;
  private Long ppId;
  public DbTokenRelations(Long ppId) {
    this.ppId = ppId;
    this.relationIds = new ArrayList();
    this.tokenAs = new ArrayList();
    this.tokenBs = new ArrayList();
  }
  public void relateTokens(Integer relationId, Integer tokenAId, Integer tokenBId) {
    relationIds.add(relationId);
    tokenAs.add(tokenAId);
    tokenBs.add(tokenBId);
  }
  public String toDbString() {
    StringBuffer retval = new StringBuffer();
    for(int i = 0, n = relationIds.size(); i < n; i++) {
      retval.append(ppId.toString()).append("\t").append(tokenAs.get(i).toString()).append("\t");
      retval.append(tokenBs.get(i).toString()).append("\tCAUSAL\t");
      retval.append(relationIds.get(i).toString()).append("\n");
    }
    return retval.toString();
  }
}

class DbToken {
  private Integer id;
  private byte isFreeToken;
  private byte isValueToken;
  private Integer startVar;
  private Integer endVar;
  private Integer durationVar;
  private Integer rejectVar;
  private Integer objectVar;
  private Integer predicate;
  private Integer sid;
  private Integer oid;
  private Integer tid;
  private Long ppId;
  public DbToken(Integer id, byte isFreeToken, byte isValueToken, Integer startVar, Integer endVar,
                 Integer durationVar, Integer rejectVar, Integer objectVar, Integer predicate, 
                 Integer sid, Integer oid, Integer tid, Long ppId) {
    this.id = id;
    this.isFreeToken = isFreeToken;
    this.isValueToken = isValueToken;
    this.startVar = startVar;
    this.endVar = endVar;
    this.durationVar = durationVar;
    this.rejectVar = rejectVar;
    this.predicate = predicate;
    this.sid = sid;
    this.oid = oid;
    this.tid = tid;
    this.ppId = ppId;
    this.objectVar = objectVar;
  }
  public String toDbString() {
    StringBuffer retval = new StringBuffer(id.toString());
    retval.append("\t");
    if(sid == null) {
      retval.append("NULL").append("\t");
    }
    else {
      retval.append(sid.toString()).append("\t");
    }
    retval.append(ppId.toString()).append("\t").append(Byte.toString(isFreeToken)).append("\t");
    retval.append(Byte.toString(isValueToken)).append("\t");
    if(startVar == null) {
      retval.append("NULL");
    }
    else {
      retval.append(startVar.toString());
    }
    retval.append("\t");
    if(endVar == null) {
      retval.append("NULL");
    }
    else {
      retval.append(endVar.toString());
    }
    retval.append("\t");
    if(durationVar == null) {
      retval.append("NULL");
    }
    else {
      retval.append(durationVar.toString());
    }
    retval.append("\t");
    if(rejectVar == null) {
      retval.append("NULL");
    }
    else {
      retval.append(rejectVar.toString());
    }
    retval.append("\t");
    if(predicate == null) {
      retval.append("NULL");
    }
    else {
      retval.append(predicate.toString());
    }
    retval.append("\t");
    if(oid == null) {
      retval.append("NULL");
    }
    else {
      retval.append(oid.toString());
    }
    retval.append("\t");
    if(tid == null) {
      retval.append("NULL");
    }
    else {
      retval.append(tid.toString());
    }
    retval.append("\t");
    if(objectVar == null) {
      retval.append("NULL");
    }
    else {
      retval.append(objectVar.toString());
    }
    retval.append("\n");
    return retval.toString();
  }
}

class DbVariable {
  private Integer id;
  private Long ppId;
  private String varType;
  private String domainType;
  private Integer domainId;
  public DbVariable(Integer id, String varType, Long ppId){
    this.id = id;
    this.varType = varType;
    this.ppId = ppId;
  }
  public Integer getId(){return id;}
  public String getType() { return varType;}
  public void setDomain(String type, Integer id) {
    this.domainType = type;
    this.domainId = id;
  }
  public String toDbString() {
    StringBuffer retval = new StringBuffer(id.toString());
    retval.append("\t").append(ppId.toString()).append("\t").append(domainType).append("\t");
    if(domainId == null) {
      retval.append("NULL");
    }
    else {
      retval.append(domainId.toString());
      }
    retval.append("\t").append(varType).append("\n");
    return retval.toString();
  }
}

class DbConstraintVarMap {
  private Long ppId;
  private ArrayList constraintIds;
  private ArrayList variableIds;
  public DbConstraintVarMap(Long ppId) {
    this.ppId = ppId;
    constraintIds = new ArrayList();
    variableIds = new ArrayList();
  }
  public void addConstraint(Integer constraintId, Integer variableId) {
    constraintIds.add(constraintId);
    variableIds.add(variableId);
  }
  public String toDbString() {
    StringBuffer retval = new StringBuffer();
    for(int i = 0, n = constraintIds.size(); i < n; i++) {
      retval.append(constraintIds.get(i).toString()).append("\t");
      retval.append(variableIds.get(i).toString()).append("\t").append(ppId.toString()).append("\n");
    }
    return retval.toString();
  }
}

class DbIntervalDomain {
  private Long ppId;
  private Integer id;
  private String type;
  private String lowerBound;
  private String upperBound;
  public DbIntervalDomain(Integer id, String type, String lowerBound, String upperBound,
                          Long ppId) {
    this.id = id;
    this.type = type;
    this.lowerBound = lowerBound;
    this.upperBound = upperBound;
    this.ppId = ppId;
  }
  public String toDbString() {
    StringBuffer retval = new StringBuffer(id.toString());
    retval.append("\t").append(ppId.toString()).append("\t").append(lowerBound).append("\t");
    retval.append(upperBound).append("\t").append(type).append("\n");
    return retval.toString();
  }
}

class DbEnumeratedDomain {
  private Long ppId;
  private Integer id;
  private String domain;
  public DbEnumeratedDomain(Integer id, Long ppId) {
    this.id = id;
    this.ppId = ppId;
  }
  public void setDomain(String domain) {
    this.domain = domain;
  }
  public String toDbString() {
    StringBuffer retval = new StringBuffer(id.toString());
    retval.append("\t").append(ppId.toString()).append("\t").append(domain).append("\n");
    return retval.toString();
  }
}
  
class DbConstraint {
  private Long ppId;
  private Integer id;
  private String name;
  private String type;
  public DbConstraint(Integer id, String name, String type, Long ppId) {
    this.id = id;
    this.name = name;
    this.type = type;
    this.ppId = ppId;
  }
  public String toDbString() {
    StringBuffer retval = new StringBuffer(id.toString());
    retval.append("\t").append(ppId.toString()).append("\t").append(name).append("\t");
    retval.append(type).append("\n");
    return retval.toString();
  }
}

class DbPredicate {
  private Long ppId;
  private Integer id;
  private String name;
  public DbPredicate(Integer id, String name, Long ppId) {
    this.id = id;
    this.name = name;
    this.ppId = ppId;
  }
  public Integer getId(){return id;}
  public String toDbString() {
    StringBuffer retval = new StringBuffer(id.toString());
    retval.append("\t").append(name).append("\t").append(ppId.toString()).append("\n");
    return retval.toString();
  }
}

class DbParameter {
  private Long ppId;
  private Integer id;
  private String name;
  private Integer pid;
  public DbParameter(Integer id, String name, Integer pid, Long ppId) {
    this.id = id;
    this.name = name;
    this.pid = pid;
    this.ppId = ppId;
  }
  public String toDbString() {
    StringBuffer retval = new StringBuffer(id.toString());
    retval.append("\t").append(pid.toString()).append("\t").append(ppId.toString());
    retval.append("\t").append(name).append("\n");
    return retval.toString();
  }
}
