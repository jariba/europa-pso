import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

public class PlanGen
{
  public static KeyManager keyManager;
  public PlanGen(String [] args)
  {
    keyManager = new KeyManager();
    PlanGenState.parseArgs(args);
  }
  public void generatePlans()
  {
    for(int i = 0; i < PlanGenState.nsequences; i++)
      {
        // ensure that PlanGenState.destDir exists
        File projDir = new File(PlanGenState.destDir);
        if (! projDir.exists()) {
          if (! projDir.mkdir()) {
            System.err.println("Failed to create destination directory " + i + 
                               ": '" + projDir.getName() + "'  I wish Java had errno.");
            System.exit(-1);
          }
        }
        StringBuffer dirBuf = (new StringBuffer(PlanGenState.destDir)).append("/seq").append(i);
        File seqDir = new File(dirBuf.toString());
        if(!seqDir.exists())
          {
            try
              {
                if(!seqDir.mkdir())
                  {
                    System.err.println("Failed to create sequence directory " + i + 
                                       ": '" + seqDir.getName() +
                                       "'  I wish Java had errno.");
                    System.exit(-1);
                  }
              }
            catch(SecurityException se)
              {
                System.err.println(se);
                System.exit(-1);
              }
          }
        int nsteps = PlanGenState.nsteps;
        if(PlanGenState.stepRange)
          nsteps = PlanGenState.getRandInRange(PlanGenState.stepRangeLo, PlanGenState.stepRangeHi);
        for(int j = 0; j < nsteps; j++)
          {
            keyManager.reset();
            (new PartialPlan()).writeXML(dirBuf.toString().concat("/step").concat(Integer.toString(j)));
          }
      }
  }
  public static void main(String [] args)
  {
    PlanGen planGen = new PlanGen(args);
    planGen.generatePlans();
  }
}

abstract class PlanGenState
{
  public static int horizonHi, horizonLo;
  public static int nsequences = 1, nsteps = 1, stepRangeHi = 1, stepRangeLo = 1;
  public static boolean stepRange = false;
  public static int nobjs = 1, objRangeHi = 1, objRangeLo = 1;
  public static boolean objRange = false;
  public static int ntimelines = 1, timelineRangeHi = 1, timelineRangeLo = 1;
  public static boolean timelineRange = false;
  public static int nslots = 1, slotRangeHi = 1, slotRangeLo = 1;
  public static boolean slotRange = false;
  public static int ntokens = 1, tokenRangeHi = 1, tokenRangeLo = 1;
  public static boolean tokenRange = false;
  public static int nfreeTokens = 1, freeTokenRangeHi = 1, freeTokenRangeLo = 1;
  public static boolean freeTokenRange = false;
  public static int npredicates = 1, predicateRangeHi = 1, predicateRangeLo = 1;
  public static boolean predicateRange = false;
  public static int nparams = 1, paramRangeHi = 1, paramRangeLo = 1;
  public static boolean paramRange = false;
  public static int ntokenRelations = 1, tokenRelationRangeHi = 1, tokenRelationRangeLo = 1;
  public static boolean tokenRelationRange = false;
  public static String destDir = "./";

  public static void parseArgs(String [] args)
  {
    boolean seenHorizon = false;
    boolean seenNumSteps = false;
    boolean seenRangeSteps = false;
    boolean seenNumObjs = false;
    boolean seenRangeObjs = false;
    boolean seenNumTimelines = false;
    boolean seenRangeTimelines = false;
    boolean seenNumSlots = false;
    boolean seenRangeSlots = false;
    boolean seenNumTokens = false;
    boolean seenRangeTokens = false;
    boolean seenNumFreeTokens = false;
    boolean seenRangeFreeTokens = false; //free range tokens!
    boolean seenNumPredicates = false;
    boolean seenRangePredicates = false;
    boolean seenNumParams = false;
    boolean seenRangeParams = false;
    boolean seenNumTokenRelations = false;
    boolean seenRangeTokenRelations = false;

    for(int i = 0; i < args.length; i++)
      {
        if(args[i].startsWith("--"))
          {
            if(args[i].equals("--usage"))
              usage();
            else if(args[i].equals("--horizon"))
              {
                try
                  {
                    horizonLo = Integer.parseInt(args[i+1]);
                    horizonHi = Integer.parseInt(args[i+2]);
                  }
                catch(Exception e){System.out.println("Invalid horizon argument.");usage();}
                i += 2;
                seenHorizon = true;
              }
            else if(args[i].equals("--dest"))
              {
                try{destDir = args[i+1];}
                catch(Exception e){System.out.println("Invalid destination argument.");usage();}
                i++;
              }
            else if(args[i].equals("--nseq"))
              {
                try{nsequences = Integer.parseInt(args[i+1]);}
                catch(Exception e){System.out.println("Invalid number of sequences."); usage();}
                i++;
              }
            else if(args[i].equals("--nsteps"))
              {
                if(seenRangeSteps)
                  {
                    System.out.println("Cannot have both a number and range of steps.");
                    usage();
                  }
                else if(seenNumSteps)
                  {
                    System.out.println("Duplicate nsteps argument.  Ignoring.");
                    continue;
                  }
                try{nsteps = Integer.parseInt(args[i+1]);}
                catch(Exception e){System.out.println("Invalid number of steps."); usage();}
                seenNumSteps = true;
                i++;
              }
            else if(args[i].equals("--steprange"))
              {
                if(seenRangeSteps)
                  {
                    System.out.println("Duplicate steprange argument.  Ignoring.");
                    continue;
                  }
                else if(seenNumSteps)
                  {
                    System.out.println("Cannot have both a number and range of steps.");
                    usage();
                  }
                try
                  {
                    stepRangeLo = Integer.parseInt(args[i+1]);
                    stepRangeHi = Integer.parseInt(args[i+2]);
                  }
                catch(Exception e){System.out.println("Invalid step range."); usage();}
                seenRangeSteps = true;
                stepRange = true;
                i += 2;
              }
            else if(args[i].equals("--nobjs"))
              {
                if(seenNumObjs)
                  {
                    System.out.println("Duplicate object argument.  Ignoring.");
                    continue;
                  }
                else if(seenRangeObjs)
                  {
                    System.out.println("Cannot have both a number and range of object.");
                    usage();
                  }
                try{nobjs = Integer.parseInt(args[i+1]);}
                catch(Exception e){System.out.println("Invalid number of objects."); usage();}
                seenNumObjs = true;
                i++;
              }
            else if(args[i].equals("--objrange"))
              {
                if(seenNumObjs)
                  {
                    System.out.println("Cannot have both a number and range of objects.");
                    usage();
                  }
                else if(seenRangeObjs)
                  {
                    System.out.println("Duplicate object range argument.  Ignoring.");
                    continue;
                  }
                try
                  {
                    objRangeLo = Integer.parseInt(args[i+1]);
                    objRangeHi = Integer.parseInt(args[i+2]);
                  }
                catch(Exception e){System.out.println("Invalid range of objects."); usage();}
                seenRangeObjs = true;
                objRange = true;
                i += 2;
              }
            else if(args[i].equals("--ntimelines"))
              {
                if(seenNumTimelines)
                  {
                    System.out.println("Duplicate timelines argument.  Ignoring.");
                    continue;
                  }
                else if(seenRangeTimelines)
                  {
                    System.out.println("Cannot have both a number and range of timelines.");
                    usage();
                  }
                try{ntimelines = Integer.parseInt(args[i+1]);}
                catch(Exception e){System.out.println("Invalid number of timelines."); usage();}
                seenNumTimelines = true;
                i++;
              }
            else if(args[i].equals("--timelinerange"))
              {
                if(seenNumTimelines)
                  {
                    System.out.println("Cannot have both a number and range of timelines.");
                    usage();
                  }
                else if(seenRangeTimelines)
                  {
                    System.out.println("Duplicate timeline range argument.  Ignoring.");
                    continue;
                  }
                try
                  {
                    timelineRangeLo = Integer.parseInt(args[i+1]);
                    timelineRangeHi = Integer.parseInt(args[i+2]);
                  }
                catch(Exception e){System.out.println("Invalid timeline range."); usage();}
                seenRangeTimelines = true;
                timelineRange = true;
                i += 2;
              }
            else if(args[i].equals("--nslots"))
              {
                if(seenNumSlots)
                  {
                    System.out.println("Duplicate slots argument.  Ignoring.");
                    continue;
                  }
                else if(seenRangeSlots)
                  {
                    System.out.println("Cannot have both a number and range of slots.");
                    usage();
                  }
                try{nslots = Integer.parseInt(args[i+1]);}
                catch(Exception e){System.out.println("Invalid number of slots."); usage();}
                seenNumTimelines = true;
                i++;
              }
            else if(args[i].equals("--slotrange"))
              {
                if(seenNumSlots)
                  {
                    System.out.println("Cannot have both a number and range of slots");
                    usage();
                  }
                else if(seenRangeSlots)
                  {
                    System.out.println("Duplicate slot range argument.  Ignoring.");
                    continue;
                  }
                try
                  {
                    slotRangeLo = Integer.parseInt(args[i+1]);
                    slotRangeHi = Integer.parseInt(args[i+2]);
                  }
                catch(Exception e){System.out.println("Invalid slot range."); usage();}
                seenRangeSlots = true;
                slotRange = true;
                i += 2;
              }
            else if(args[i].equals("--ntokens"))
              {
                if(seenNumTokens)
                  {
                    System.out.println("Duplicate token argument.  Ignoring.");
                    continue;
                  }
                else if(seenRangeTokens)
                  {
                    System.out.println("Cannot have both a number and range of tokens.");
                    usage();
                  }
                try{ntokens = Integer.parseInt(args[i+1]);}
                catch(Exception e){System.out.println("Invalid number of tokens."); usage();}
                seenNumTokens = true;
                i++;
              }
            else if(args[i].equals("--tokenrange"))
              {
                if(seenNumTokens)
                  {
                    System.out.println("Cannot have both a number and range of tokens.");
                    usage();
                  }
                else if(seenRangeTokens)
                  {
                    System.out.println("Duplicate token range argument.  Ignoring.");
                    continue;
                  }
                try
                  {
                    tokenRangeLo = Integer.parseInt(args[i+1]);
                    tokenRangeHi = Integer.parseInt(args[i+2]);
                  }
                catch(Exception e){System.out.println("Invalid token range"); usage();}
                seenRangeTokens = true;
                tokenRange = true;
                i += 2;
              }
            else if(args[i].equals("--nfreetokens"))
              {
                if(seenNumFreeTokens)
                  {
                    System.out.println("Duplicate free tokens argument.  Ignoring.");
                    continue;
                  }
                else if(seenRangeFreeTokens)
                  {
                    System.out.println("Cannot have both a number and range of free tokens.");
                    usage();
                  }
                try{nfreeTokens = Integer.parseInt(args[i+1]);}
                catch(Exception e){System.out.println("Invalid number of free tokens."); usage();}
                seenNumFreeTokens = true;
                i++;
              }
            else if(args[i].equals("--freetokenrange"))
              {
                if(seenNumFreeTokens)
                  {
                    System.out.println("Cannot have both a number and range of free tokens.");
                    usage();
                  }
                else if(seenRangeFreeTokens)
                  {
                    System.out.println("Duplicate free token range argument.  Ignoring.");
                    continue;
                  }
                try
                  {
                    freeTokenRangeLo = Integer.parseInt(args[i+1]);
                    freeTokenRangeHi = Integer.parseInt(args[i+2]);
                  }
                catch(Exception e){System.out.println("Invalid free token range"); usage();}
                seenRangeFreeTokens = true;
                freeTokenRange = true;
                i += 2;
              }
            else if(args[i].equals("--npredicates"))
              {
                if(seenNumPredicates) 
                  {
                    System.out.println("Duplicate predicates argument.  Ignoring.");
                    continue;
                  }
                else if(seenRangePredicates)
                  {
                    System.out.println("Cannot have both a number and range of predicates.");
                    usage();
                  }
                try{npredicates = Integer.parseInt(args[i+1]);}
                catch(Exception e){System.out.println("Invalid number of predicates"); usage();}
                seenNumPredicates = true;
                i++;
              }
            else if(args[i].equals("--predicaterange"))
              {
                if(seenNumPredicates)
                  {
                    System.out.println("Cannot have both a number and range of predicates.");
                    usage();
                  }
                else if(seenRangePredicates)
                  {
                    System.out.println("Duplicate predicate range argument.  Ignoring.");
                    continue;
                  }
                try
                  {
                    predicateRangeLo = Integer.parseInt(args[i+1]);
                    predicateRangeHi = Integer.parseInt(args[i+2]);
                  }
                catch(Exception e){System.out.println("Invalid range of predicates"); usage();}
                seenRangePredicates = true;
                predicateRange = true;
                i += 2;
              }
            else if(args[i].equals("--nparams"))
              {
                if(seenNumParams)
                  {
                    System.out.println("Duplicate params argument.  Ignoring.");
                    continue;
                  }
                else if(seenRangeParams)
                  {
                    System.out.println("Cannot have both a number and range of params.");
                    usage();
                  }
                try{nparams = Integer.parseInt(args[i+1]);}
                catch(Exception e){System.out.println("Invalid number of params"); usage();}
                seenNumParams = true;
                i++;
              }
            else if(args[i].equals("--paramrange"))
              {
                if(seenNumParams)
                  {
                    System.out.println("Cannot have both a number and range of params.");
                    usage();
                  }
                else if(seenRangeParams)
                  {
                    System.out.println("Duplicae param range argument.  Ignoring.");
                    continue;
                  }
                try
                  {
                    paramRangeLo = Integer.parseInt(args[i+1]);
                    paramRangeHi = Integer.parseInt(args[i+2]);
                  }
                catch(Exception e){System.out.println("Invalid range of params."); usage();}
                seenRangeParams = true;
                paramRange = true;
                i += 2;
              }
            else if(args[i].equals("--ntokenrelations"))
              {
                if(seenNumTokenRelations)
                  {
                    System.out.println("Duplicate token relation argument.  Ignoring.");
                    continue;
                  }
                else if(seenRangeTokenRelations)
                  {
                    System.out.println("Cannot have both a number and range of token relations.");
                    usage();
                  }
                try{ntokenRelations = Integer.parseInt(args[i+1]);}
                catch(Exception e){System.out.println("Invalid number of token relations."); usage();}
                seenNumTokenRelations = true;
                i++;
              }
            else if(args[i].equals("--tokenrelationrange"))
              {
                if(seenNumTokenRelations)
                  {
                    System.out.println("Cannot have both a number and range of token relations.");
                    usage();
                  }
                else if(seenRangeTokenRelations)
                  {
                    System.out.println("Duplicate token relation range argument.  Ignoring.");
                    continue;
                  }
                try
                  {
                    tokenRelationRangeLo = Integer.parseInt(args[i+1]);
                    tokenRelationRangeHi = Integer.parseInt(args[i+2]);
                  }
                catch(Exception e){System.out.println("Invalid range of token relations."); usage();}
                seenRangeTokenRelations = true;
                tokenRelationRange = true;
                i += 2;
              }
          }
        else
          {
            System.out.println("Unrecognized argument: " + args[i]);
            usage();
          }
      }
    if(!seenHorizon)
      {
        System.out.println("horizon is a required arg.");
        usage();
      }
    if(nsequences < 1)
      {
        System.out.println("The number of sequences must be a natural number.");
        usage();
      }
    if(nsequences == 1 && stepRange)
      {
        System.out.println("It is useless to specify a range of steps per sequence if you've only got one sequence.");
        usage();
      }
    testNumOrRange(stepRange, nsteps, stepRangeLo, stepRangeHi);
    testNumOrRange(objRange, nobjs, objRangeLo, objRangeHi);
    testNumOrRange(timelineRange, ntimelines, timelineRangeLo, timelineRangeHi);
    testNumOrRange(slotRange, nslots, slotRangeLo, slotRangeHi);
    testNumOrRange(tokenRange, ntokens, tokenRangeLo, tokenRangeHi);
    testNumOrRange(freeTokenRange, nfreeTokens, freeTokenRangeLo, freeTokenRangeHi);
    printState();
  }

  /*this is fun.  some explanation: i need a random integer between lo and hi, inclusive.  so 
    first i need to get the least power of 10 greater than hi (henceforth LP10GH).  that's what all
    the interesting Integer.toString.length stuff is about.  when i multiply Math.random() by it, i
    get a double such that 0 <= double <= LP10GH.  I then add (low - 1) so my number is now
    (low - 1) <= double <= LP10GH + (low - 1), then get it mod hi, which ensures 
    (lo - 1) <= double <= (hi - 1), then add 1.  i love mod.*/
  /*what i once thought was clever, wasn't so much, so i'm going a different way*/
  public static int getRandInRange(int low, int hi)
  {
    if(low == hi)
      return low;
    if(low > hi)
      {
        low ^= hi;
        hi ^= low;
        low ^= hi;
      }
    /*
      boolean lowIsZero = low == 0;
      if(lowIsZero) {
      low++;
      hi++;
      }
      int retval = 
      (int) ((Math.round(Math.random() * Math.pow(10, Integer.toString(hi).length()+1)) + 
      (low - 1)) % hi) + 1;
      if(lowIsZero) {
      retval--;
      }
      return retval;*/
    return (((int)(Math.random() * Math.pow(10, Integer.toString(hi).length()+1))) % (hi - low + 1)) + low;
  }

  private static void testNumOrRange(boolean range, int num, int rangeLo, int rangeHi)
  {
    if(range)
      {
        if(rangeLo < 0 || rangeHi < 1 || rangeLo > rangeHi)
          usage();
      }
    else if(num < 0)
      usage();
  }
  public static void usage()
  {
    System.out.println("Usage:");
    System.out.println("java PlanGen [--usage | --dest <destination> --horizon <low> <high> --nseq <num>\n[--nsteps <num> | --steprange <low> <high>]\n[--nobjs <num> | --objrange <low> <high>]\n[--ntimelines <num> | --timelinerange <low> <high>]\n[--nslots <num> | --slotrange <low> <high>]\n[--ntokens <num> | --tokenrange <low> <high>]\n[--nfreetokens <num> | --freetokenrange <low> <high>]\n[--npredicates <num> --predicaterange <low> <high>]\n[--nparams <num> | --paramrange <low> <high>]\n");
    System.out.println("OPTIONS:\n");
    System.out.println("--usage");
    System.out.println("  Prints this message and exits.");
    System.out.println("With the exception of --horizon, all of the following arguments are optional:");
    System.out.println("--dest <destination>");
    System.out.println("  Specifies the destination directory for the planning sequence(s)");
    System.out.println("--horizion <low> <high>");
    System.out.println("  Specifies the planning horizon to be between <low> and <high>, inclusive>");
    System.out.println("--nseq <num>");
    System.out.println("  Specifies the number of planning sequences to generate.");
    System.out.println("--nsteps <num> or --steprange <low> <high>");
    System.out.println("  Specifies the number of steps (or the possible range of steps) to generate per\n  planning sequence.");
    System.out.println("--nobjs <num> or --objrange <low> <high>");
    System.out.println("  Specifies the number of objects to generate per planning step.");
    System.out.println("--ntimelines <num> or --timelinerange <low> <high>");
    System.out.println("  Specifies the number of timelines to generate per object.");
    System.out.println("--nslots <num> or --slotrange <low> <high>");
    System.out.println("  Specifies the number of slots to generate per timeline.");
    System.out.println("--ntokens <num> or --tokenrange <low> <high>");
    System.out.println("  Specifies the number of tokens to generate per slot.");
    System.out.println("--nfreetokens <num> or --freetokenrange <low> <high>");
    System.out.println("  Specifies the number of free tokens (tokens not in a slot) to generate per\n  planning step.");
    System.out.println("--npredicates <num> or --predicaterange <low> <high>");
    System.out.println("  Specifies the number of predicates to generate per planning step.");
    System.out.println("--nparams <num> or --paramrange <low> <high>");
    System.out.println("  Specifies the number of parameters that each predicate has.");
    System.exit(0);
  }
  public static void printState()
  {
    System.out.println("Destination: " + destDir);
    System.out.println("Planning horizon: from " + horizonLo + " to " + horizonHi);
    System.out.println("Sequences: " + nsequences);
    if(stepRange)
      System.out.println("Steps: from " + stepRangeLo + " to " + stepRangeHi);
    else
      System.out.println("Steps: " + nsteps);
    if(objRange)
      System.out.println("Objects: from " + objRangeLo + " to " + objRangeHi);
    else
      System.out.println("Objects: " + nobjs);
    if(timelineRange)
      System.out.println("Timelines: from " + timelineRangeLo + " to " + timelineRangeHi);
    else
      System.out.println("Timelines: " + ntimelines);
    if(slotRange)
      System.out.println("Slots: from " + slotRangeLo + " to " + slotRangeHi);
    else
      System.out.println("Slots: " + nslots);
    if(tokenRange)
      System.out.println("Tokens: from " + tokenRangeLo + " to " + tokenRangeHi);
    else
      System.out.println("Tokens: " + ntokens);
    if(freeTokenRange)
      System.out.println("Free Tokens: from " + freeTokenRangeLo + " to " + freeTokenRangeHi);
    else
      System.out.println("Free Tokens: " + nfreeTokens);
    if(predicateRange)
      System.out.println("Predicates: from " + predicateRangeLo + " to " + predicateRangeHi);
    else
      System.out.println("Predicates: " + npredicates);
    if(paramRange)
      System.out.println("Parameters: from " + paramRangeLo + " to " + paramRangeHi);
    else
      System.out.println("Parameters: " + nparams);
    if(tokenRelationRange)
      System.out.println("Token Relations: from " + tokenRelationRangeLo + " to " + tokenRelationRangeHi);
    else
      System.out.println("Token Relations: " + ntokenRelations);
  }
}

class KeyManager
{
  private int key;
  private HashMap partialPlans, tokens, objects, timelines, slots, tokenRelations, variables,
    constraints, predicates, params;

  public static final String defObject = "K-1";
  public static final String defTimeline = "K-2";
  public static final String defSlot = "K-3";
  public static final String defToken1 = "K-4";
  public static final String defToken2 = "K-5";
  public static final String defTokenRelation = "K-6";
  public static final String defDurationVar = "K1";
  public static final String defRejectVar = "K2";
  public static final String defStartVar = "K-9";
  public static final String defEndVar = "K-10";
  public static final String defParamVar = "K-11";
  public static final String defConstraint = "K-12";
  public static final String defPredicate = "K-13";
  public static final String defParameter = "K-14";
  public KeyManager()
  {
    key = 3;
    partialPlans = new HashMap();
    tokens = new HashMap();
    objects = new HashMap();
    timelines = new HashMap();
    slots = new HashMap();
    tokenRelations = new HashMap();
    variables = new HashMap();
    constraints = new HashMap();
    predicates = new HashMap();
    params = new HashMap();
  }
  public void reset()
  {
    key = 3;
    partialPlans.clear();
    tokens.clear();
    objects.clear();
    timelines.clear();
    slots.clear();
    tokenRelations.clear();
    variables.clear();
    constraints.clear();
    predicates.clear();
  }
  public String getKey()
  {
    key++;
    return (new StringBuffer("K")).append(key-1).toString();
  }
  public String getKeyForPartialPlan(PartialPlan pp)
  {
    String pKey = getKey();
    partialPlans.put(pKey, pp);
    return pKey;
  }
  public String getKeyForToken(Token t)
  {
    String tKey = getKey();
    tokens.put(tKey, t);
    return tKey;
  }
  public String getKeyForObject(PwObject po)
  {
    String pKey = getKey();
    objects.put(pKey, po);
    return pKey;
  }
  public String getKeyForTimeline(Timeline t)
  {
    String tKey = getKey();
    timelines.put(tKey, t);
    return tKey;
  }
  public String getKeyForSlot(Slot s)
  {
    String sKey = getKey();
    slots.put(sKey, s);
    return sKey;
  }
  public String getKeyForTokenRelation(TokenRelation t)
  {
    String tKey = getKey();
    tokenRelations.put(tKey, t);
    return tKey;
  }
  public String getKeyForVariable(Variable v)
  {
    String vKey = getKey();
    variables.put(vKey, v);
    return vKey;
  }
  public String getKeyForConstraint(Constraint c)
  {
    String cKey = getKey();
    constraints.put(cKey, c);
    return cKey;
  }
  public String getKeyForPredicate(Predicate p)
  {
    String pKey = getKey();
    predicates.put(pKey, p);
    return pKey;
  }
  public String getKeyForParam(Param p)
  {
    String pKey = getKey();
    params.put(pKey, p);
    return pKey;
  }
  public PartialPlan getPartialPlan(String key)
  {
    return (PartialPlan) partialPlans.get(key);
  }
  public Token getToken(String key)
  {
    return (Token) tokens.get(key);
  }
  public Set getAllTokenIds()
  {
    return tokens.keySet();
  }
  public PwObject getObject(String key)
  {
    return (PwObject) objects.get(key);
  }
  public Collection getAllObjects()
  {
    return objects.values();
  }
  public Set getAllObjectIds() {
    return objects.keySet();
  }
  public Timeline getTimeline(String key)
  {
    return (Timeline) timelines.get(key);
  }
  public int getNumTimelines() {
    return timelines.size();
  }
  public Slot getSlot(String key)
  {
    return (Slot) slots.get(key);
  }
  public TokenRelation getTokenRelation(String key)
  {
    return (TokenRelation) tokenRelations.get(key);
  }
  public Collection getAllTokenRelations()
  {
    return tokenRelations.values();
  }
  public Variable getVariable(String key)
  {
    return (Variable) variables.get(key);
  }
  public Collection getAllVariables()
  {
    return variables.values();
  }
  public Constraint getConstraint(String key)
  {
    return (Constraint) constraints.get(key);
  }
  public Collection getAllConstraints()
  {
    return constraints.values();
  }
  public Predicate getPredicate(String key)
  {
    return (Predicate) predicates.get(key);
  }
  public String getRandomPredicateId()
  {
    Set keys = predicates.keySet();
    int index = PlanGenState.getRandInRange(0, keys.size()-1);
    return (String) (keys.toArray())[index];
  }
  public Collection getAllPredicates()
  {
    return predicates.values();
  }
  public Param getParam(String key)
  {
    return (Param) params.get(key);
  }
  //public Collection getAllPredicates
}


class PartialPlan
{
  private String key;
  private ArrayList freeTokenIds; 

  public PartialPlan()
  {
    key = PlanGen.keyManager.getKeyForPartialPlan(this);
    freeTokenIds = new ArrayList();
    
    int npredicates = (PlanGenState.predicateRange ? 
                       PlanGenState.getRandInRange(PlanGenState.predicateRangeLo, 
                                                   PlanGenState.predicateRangeHi) :
                       PlanGenState.npredicates);
    System.err.println("Generating predicates...");
    for(int i = 0; i < npredicates; i++)
      new Predicate(i);

    int nobjs = PlanGenState.nobjs;
    if(PlanGenState.objRange)
      nobjs = PlanGenState.getRandInRange(PlanGenState.objRangeLo, PlanGenState.objRangeHi);

    System.err.println("Generating objects...");
    for(int i = 0; i < nobjs; i++)
      new PwObject(i);

    int nfreeTokens = PlanGenState.nfreeTokens;
    if(PlanGenState.freeTokenRange)
      nfreeTokens = PlanGenState.getRandInRange(PlanGenState.freeTokenRangeLo, 
                                                PlanGenState.freeTokenRangeHi);
    System.err.println("Generating free tokens...");
    for(int i = 0; i < nfreeTokens; i++)
      freeTokenIds.add((new Token(i, true)).getId());
    
    Vector availableMasters = new Vector(PlanGen.keyManager.getAllTokenIds());
    Vector availableSlaves = new Vector(availableMasters);
    if(availableMasters.size() > 1)
      {
        System.err.println("Generating token relations...");
        int numSupremeMasters = PlanGenState.getRandInRange(1, PlanGen.keyManager.getNumTimelines());
        ArrayList supremeMasters = new ArrayList(numSupremeMasters);
        for(int i = 0; i < numSupremeMasters; i++) {
          int masterIndex = PlanGenState.getRandInRange(0, availableMasters.size()-1);
          if(!PlanGen.keyManager.getToken((String)availableMasters.get(masterIndex)).isFreeToken())
            {
              supremeMasters.add(availableMasters.get(i));
              availableSlaves.remove(availableMasters.get(i));
            }
          else {
            i--;
          }
        }
        while(availableMasters.size() != 0 && availableSlaves.size() != 0) {
          int masterIndex = PlanGenState.getRandInRange(0, availableMasters.size()-1);
          String masterKey = (String) availableMasters.get(masterIndex);
          if(PlanGen.keyManager.getToken(masterKey).isFreeToken()) {
            availableMasters.removeElement(masterKey);
            continue;
          }
          if(!supremeMasters.contains(masterKey)) {
            availableSlaves.remove(masterKey);
          }
          availableMasters.remove(masterKey);
          int numSlaves = PlanGenState.getRandInRange(1, (availableSlaves.size() > 3 ? 4 :
                                                          availableSlaves.size()));
          String [] slaveKeys = new String [numSlaves];
          for(int i = 0; i < numSlaves; i++) {
            slaveKeys[i] = (String) availableSlaves.get(PlanGenState.getRandInRange(0,
                                                                                    availableSlaves.size()-1));
            availableSlaves.remove(slaveKeys[i]);
          }
          for(int i = 0; i < numSlaves; i++) {
            new TokenRelation(masterKey, slaveKeys[i]);
          }
          //if(!supremeMasters.contains(masterKey)) {
          //  availableSlaves.add(masterKey);
          //}
        }
      }
  }
  public void writeXML(String outfile)
  {
    try
      {
        FileWriter output = new FileWriter(outfile.concat(".xml"));
        String xml = toXML();
        output.write(xml, 0, xml.length());
        output.close();
      }
    catch(IOException e)
      {
        System.err.println(e);
        System.exit(-1);
      }
    
  }
  public String toXML()
  {
    StringBuffer xmlBuf = (new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<!DOCTYPE PartialPlan SYSTEM 'PlanDb.dtd'>\n<PartialPlan key=\"")).append(key).append("\" model=\"Nemo.ddl\">\n");
    //free tokens
    for(int i = 0; i < freeTokenIds.size(); i++) 
      xmlBuf.append(PlanGen.keyManager.getToken((String)freeTokenIds.get(i)).toXML());
    //objects->timelines->slots->tokens
    Collection objects = PlanGen.keyManager.getAllObjects();
    Iterator objectIterator = objects.iterator();
    while(objectIterator.hasNext())
      xmlBuf.append(((PwObject)objectIterator.next()).toXML());
    //default object->timeline->slot->token
    xmlBuf.append("  <Object key=\"").append(PlanGen.keyManager.defObject).append("\" name=\"Zip\">\n");
    xmlBuf.append("    <Timeline key=\"").append(PlanGen.keyManager.defTimeline).append("\" name=\"Naught\">\n");
    xmlBuf.append("      <Slot key=\"").append(PlanGen.keyManager.defSlot).append("\">\n");
    xmlBuf.append("        <Token key=\"").append(PlanGen.keyManager.defToken1).append("\" isValueToken=\"N\" predicateId=\"").append(PlanGen.keyManager.defPredicate).append("\" startVarId=\"").append(PlanGen.keyManager.defStartVar).append("\" endVarId=\"").append(PlanGen.keyManager.defEndVar).append("\" durationVarId=\"").append(PlanGen.keyManager.defDurationVar).append("\" objectVarId=\"").append(PlanGen.keyManager.defObject).append("\" rejectVarId=\"").append(PlanGen.keyManager.defRejectVar).append("\" tokenRelationIds=\"").append(PlanGen.keyManager.defTokenRelation).append("\" paramVarIds=\"").append(PlanGen.keyManager.defParamVar).append("\" slotId=\"").append(PlanGen.keyManager.defSlot).append("\"/>\n");
    xmlBuf.append("        <Token key=\"").append(PlanGen.keyManager.defToken2).append("\" isValueToken=\"N\" predicateId=\"").append(PlanGen.keyManager.defPredicate).append("\" startVarId=\"").append(PlanGen.keyManager.defStartVar).append("\" endVarId=\"").append(PlanGen.keyManager.defEndVar).append("\" durationVarId=\"").append(PlanGen.keyManager.defDurationVar).append("\" objectVarId=\"").append(PlanGen.keyManager.defObject).append("\" rejectVarId=\"").append(PlanGen.keyManager.defRejectVar).append("\" tokenRelationIds=\"").append(PlanGen.keyManager.defTokenRelation).append("\" paramVarIds=\"").append(PlanGen.keyManager.defParamVar).append("\" slotId=\"").append(PlanGen.keyManager.defSlot).append("\"/>\n");
    xmlBuf.append("      </Slot>\n");
    xmlBuf.append("    </Timeline>\n");
    xmlBuf.append("  </Object>\n");
    //variables
    Collection variables = PlanGen.keyManager.getAllVariables();
    Iterator variableIterator = variables.iterator();
    while(variableIterator.hasNext())
      xmlBuf.append(((Variable)variableIterator.next()).toXML());
    //default variables (start, end, reject, duration, parameter)
    xmlBuf.append("  <Variable key=\"").append(PlanGen.keyManager.defDurationVar).append("\" type=\"DURATION_VAR\" constraintIds=\"").append(PlanGen.keyManager.defConstraint).append("\" paramId=\"").append(PlanGen.keyManager.defParameter).append("\">\n    <IntervalDomain type=\"INTEGER_SORT\" lowerBound=\"0\" upperBound=\"_plus_infinity_\"/>\n  </Variable>");
    xmlBuf.append("  <Variable key=\"").append(PlanGen.keyManager.defRejectVar).append("\" type=\"REJECT_VAR\" constraintIds=\"").append(PlanGen.keyManager.defConstraint).append("\" paramId=\"").append(PlanGen.keyManager.defParameter).append("\">\n    <EnumeratedDomain><![CDATA[True]]></EnumeratedDomain>\n  </Variable>");
    xmlBuf.append("  <Variable key=\"").append(PlanGen.keyManager.defStartVar).append("\" type=\"START_VAR\" constraintIds=\"").append(PlanGen.keyManager.defConstraint).append("\" paramId=\"").append(PlanGen.keyManager.defParameter).append("\">\n    <EnumeratedDomain><![CDATA[0]]></EnumeratedDomain>\n  </Variable>\n");
    xmlBuf.append("  <Variable key=\"").append(PlanGen.keyManager.defEndVar).append("\" type=\"END_VAR\" constraintIds=\"").append(PlanGen.keyManager.defConstraint).append("\" paramId=\"").append(PlanGen.keyManager.defParameter).append("\">\n    <EnumeratedDomain><![CDATA[_plus_infinity_]]></EnumeratedDomain>\n  </Variable>");
    xmlBuf.append("  <Variable key=\"").append(PlanGen.keyManager.defParamVar).append("\" type=\"PARAMETER_VAR\" constraintIds=\"").append(PlanGen.keyManager.defConstraint).append("\" paramId=\"").append(PlanGen.keyManager.defParameter).append("\">\n    <EnumeratedDomain><![CDATA[0]]></EnumeratedDomain>\n  </Variable>");
    //constraints 
    Collection constraints = PlanGen.keyManager.getAllConstraints();
    Iterator constraintIterator = constraints.iterator();
    while(constraintIterator.hasNext())
      xmlBuf.append(((Constraint)constraintIterator.next()).toXML());
    //default constraint
    xmlBuf.append("  <Constraint key=\"").append(PlanGen.keyManager.defConstraint).append("\" type=\"TEMPORAL\" name=\"eq\" variableIds=\"").append(PlanGen.keyManager.defStartVar).append(" ").append(PlanGen.keyManager.defEndVar).append("\"/>\n");
    //tokenRelations
    Collection tokenRelations = PlanGen.keyManager.getAllTokenRelations();
    Iterator tokenRelationIterator = tokenRelations.iterator();
    while(tokenRelationIterator.hasNext())
      xmlBuf.append(((TokenRelation)tokenRelationIterator.next()).toXML());
    //default tokenRelation
    xmlBuf.append("  <TokenRelation key=\"").append(PlanGen.keyManager.defTokenRelation).append("\" masterToken=\"").append(PlanGen.keyManager.defToken1).append("\" slaveToken=\"").append(PlanGen.keyManager.defToken2).append("\"/>\n");
    //predicates
    Collection predicates = PlanGen.keyManager.getAllPredicates();
    Iterator predicateIterator = predicates.iterator();
    while(predicateIterator.hasNext())
      xmlBuf.append(((Predicate)predicateIterator.next()).toXML());
    //default predicate/parameter
    xmlBuf.append("  <Predicate key=\"").append(PlanGen.keyManager.defPredicate).append("\" name=\"blank\">\n    <Parameter key=\"").append(PlanGen.keyManager.defParameter).append("\" name=\"nix\"/>\n  </Predicate>\n");
    xmlBuf.append("</PartialPlan>");
    return xmlBuf.toString();
  }
}

class PwObject //conor ought to *love* this...
{
  public static String currObject;
  private String key, name;
  private ArrayList timelineIds;
  public PwObject(int num)
  {
    name = (new StringBuffer("Object ")).append(num).toString();
    key = PlanGen.keyManager.getKeyForObject(this);
    currObject = key;
    timelineIds = new ArrayList();

    int ntimelines = (PlanGenState.timelineRange ? 
                      PlanGenState.getRandInRange(PlanGenState.timelineRangeLo, 
                                                  PlanGenState.timelineRangeHi) : 
                      PlanGenState.ntimelines);
    System.err.println("Generating timelines...");
    for(int i = 0; i < ntimelines; i++)
      timelineIds.add((new Timeline(i, key)).getId());
    /*Object [] tokenIds = PlanGen.keyManager.getAllTokenIds().toArray();
    if(tokenIds.length > 1)
      {
        for(int i = 0; i < tokenIds.length + 5; i++)
          {
            int index1 = PlanGenState.getRandInRange(0, tokenIds.length-1);
            int index2 = PlanGenState.getRandInRange(0, tokenIds.length-1);
            while(index2 == index1)
              index2 = PlanGenState.getRandInRange(0, tokenIds.length-1);
            new TokenRelation((String) tokenIds[index1], (String) tokenIds[index2]);
          }
          }*/
  }
  public String toXML()
  {
    StringBuffer xmlBuf = (new StringBuffer("  <Object key=\"")).append(key).append("\" name=\"").append(name).append("\">\n");
    for(int i = 0; i < timelineIds.size(); i++)
      xmlBuf.append(PlanGen.keyManager.getTimeline((String)timelineIds.get(i)).toXML());
    xmlBuf.append("  </Object>\n");
    return xmlBuf.toString();
  }
}

class Timeline
{
  private String key, name;
  private ArrayList slotIds;
  public Timeline(int num, String objKey)
  {
    name = (new StringBuffer("Timeline ")).append(num).toString();
    key = PlanGen.keyManager.getKeyForTimeline(this);
    slotIds = new ArrayList();
    int nslots = (PlanGenState.slotRange ? PlanGenState.getRandInRange(PlanGenState.slotRangeLo,
                                                                       PlanGenState.slotRangeHi) :
                  PlanGenState.nslots);
    for(int i = 0; i < nslots; i++)
      slotIds.add((new Slot(i, nslots, objKey)).getId());
    //i could/should make this prettier
    for(int i = 1; i < slotIds.size()-1; i++)
      {
        String tokenA = PlanGen.keyManager.getSlot((String)slotIds.get(0)).getFirstTokenId();
        String tokenB = PlanGen.keyManager.getSlot((String)slotIds.get(i+1)).getFirstTokenId();
        if(tokenA == null)
          {
            continue;
          }
        if(tokenB == null) {
          i++;
          continue;
        }
        new Constraint(PlanGen.keyManager.getToken(tokenA).getEndVarId(),
                       PlanGen.keyManager.getToken(tokenB).getStartVarId(),
                       "le", "TEMPORAL");
      }
  }
  public String getId(){return key;}
  public String toXML()
  {
    StringBuffer xmlBuf = (new StringBuffer("    <Timeline key=\"")).append(key).append("\" name=\"").append(name).append("\">\n");
    for(int i = 0; i < slotIds.size(); i++)
      xmlBuf.append(PlanGen.keyManager.getSlot((String)slotIds.get(i)).toXML());
    xmlBuf.append("    </Timeline>\n");
    return xmlBuf.toString();
  }
}

class Slot
{
  private String key;
  private ArrayList tokenIds;
  public Slot(int num, int nslots, String objKey)
  {
    key = PlanGen.keyManager.getKeyForSlot(this);
    tokenIds = new ArrayList();
    int ntokens = (PlanGenState.tokenRange ? PlanGenState.getRandInRange(PlanGenState.tokenRangeLo,
                                                                         PlanGenState.tokenRangeHi)
                   : PlanGenState.ntokens);
    int timeRangeLo = (int) Math.floor(((PlanGenState.horizonHi - PlanGenState.horizonLo)/nslots) *
                                       num);
    int timeRangeHi = timeRangeLo + (int) 
      Math.floor((PlanGenState.horizonHi - PlanGenState.horizonLo)/nslots);
    if(num == nslots)
      timeRangeHi = PlanGenState.horizonHi;
    ArrayList startVariableIds = new ArrayList();
    ArrayList endVariableIds = new ArrayList();
    String predicateKey = PlanGen.keyManager.getRandomPredicateId();
    for(int i = 0; i < ntokens; i++)
      {
        Token token = new Token(i, timeRangeLo, timeRangeHi, key, objKey, predicateKey);
        tokenIds.add(token.getId());
        startVariableIds.add(token.getStartVarId());
        endVariableIds.add(token.getEndVarId());
      }
    for(int i = 0; i < ntokens-1; i++)
        new Constraint((String) startVariableIds.get(i), (String) startVariableIds.get(i+1),
                       "eq", "TEMPORAL");
  }
  public String getId(){return key;}
  public String getFirstTokenId() {
    if(tokenIds.size() == 0) {
      return null;
    }
    return (String)tokenIds.get(0);
  }
  public String toXML()
  {
    StringBuffer xmlBuf = (new StringBuffer("      <Slot key=\"")).append(key).append("\">\n");
    for(int i = 0; i < tokenIds.size(); i++)
      xmlBuf.append(PlanGen.keyManager.getToken((String)tokenIds.get(i)).toXML());
    xmlBuf.append("      </Slot>\n");
    return xmlBuf.toString();
  }
}

class Token
{
  private String key, startVarId, endVarId, slotId, predicateId, objectVarId;
  private ArrayList tokenRelationIds, paramVarIds;
  boolean bornFree = false;
  public Token(int num, int rangeLo, int rangeHi, String slotKey, String objKey, 
               String predicateKey)
  {
    key = PlanGen.keyManager.getKeyForToken(this);
    slotId = slotKey;
    tokenRelationIds = new ArrayList();
    paramVarIds = new ArrayList();
    predicateId = predicateKey;
    //predicateId = PlanGen.keyManager.getRandomPredicateId();
    List paramIds = PlanGen.keyManager.getPredicate(predicateId).getParamIds();
    paramVarIds = new ArrayList(paramIds.size());
    for(int i = 0; i < paramIds.size(); i++) {
      int paramValue = PlanGenState.getRandInRange(0, 10000);
      paramVarIds.add((new Variable(paramValue, paramValue, "PARAMETER_VAR", 
                                    (String)paramIds.get(i))).getId());
    }
    if(rangeLo == PlanGenState.horizonLo)
      startVarId = (new Variable(rangeLo, rangeLo, "START_VAR")).getId();
    else
      startVarId = (new Variable(rangeLo, rangeLo + 1, "START_VAR")).getId();
    if(rangeHi == PlanGenState.horizonHi)
      endVarId = (new Variable(rangeHi, rangeHi, "END_VAR")).getId();
    else
      endVarId = (new Variable(rangeHi - 1, rangeHi, "END_VAR")).getId();
    objectVarId = (new Variable(objKey, "OBJECT_VAR")).getId();
  }
  public Token(int num, boolean free)
  {
    key = PlanGen.keyManager.getKeyForToken(this);
    slotId = null;
    tokenRelationIds = new ArrayList();
    paramVarIds = new ArrayList();
    predicateId = PlanGen.keyManager.getRandomPredicateId();
    List paramIds = PlanGen.keyManager.getPredicate(predicateId).getParamIds();
    paramVarIds = new ArrayList(paramIds.size());
    for(int i = 0; i < paramIds.size(); i++) {
      int paramValue = PlanGenState.getRandInRange(0, 10000);
      paramVarIds.add((new Variable(paramValue, paramValue, "PARAMETER_VAR",
                                    (String) paramIds.get(i))).getId());
    }
    int startLo = PlanGenState.getRandInRange(PlanGenState.horizonLo, PlanGenState.horizonHi);
    int startHi = PlanGenState.getRandInRange(startLo, startLo + 10);
    int endLo = PlanGenState.getRandInRange(startLo, PlanGenState.horizonHi);
    int endHi = PlanGenState.getRandInRange(endLo, PlanGenState.horizonHi);
    startVarId = (new Variable(startLo, startHi, "START_VAR")).getId();
    endVarId = (new Variable(endLo, endHi, "END_VAR")).getId();
    StringBuffer objVarEnum = new StringBuffer();
    Iterator objIdIterator = PlanGen.keyManager.getAllObjectIds().iterator();
    while(objIdIterator.hasNext()) {
      objVarEnum.append((String)objIdIterator.next());
      if(objIdIterator.hasNext()) {
        objVarEnum.append(", ");
      }
    }
    objectVarId = (new Variable(objVarEnum.toString(), "OBJECT_VAR")).getId();
    bornFree = true;
  }
  public String getStartVarId(){return startVarId;}
  public String getEndVarId(){return endVarId;}
  public String getId(){return key;}
  public boolean isFreeToken(){return bornFree;}
  public void addRelation(String key)
  {
    tokenRelationIds.add(key);
  }
  public String toXML()
  {
    StringBuffer xmlBuf = (new StringBuffer("        <Token key=\"")).append(key).append("\" isValueToken=\"Y\" predicateId=\"").append(predicateId).append("\" startVarId=\"");
    xmlBuf.append(startVarId);
    xmlBuf.append("\" endVarId=\"");
    xmlBuf.append(endVarId);
    xmlBuf.append("\" durationVarId=\"").append(PlanGen.keyManager.defDurationVar).append("\" objectVarId=\"");
    xmlBuf.append(objectVarId);
    xmlBuf.append("\" rejectVarId=\"").append(PlanGen.keyManager.defRejectVar).append("\" tokenRelationIds=\"");
    if(tokenRelationIds.size() == 0)
      xmlBuf.append(PlanGen.keyManager.defTokenRelation);
    else
      for(int i = 0; i < tokenRelationIds.size(); i++)
        xmlBuf.append((String)tokenRelationIds.get(i)).append(" ");
    xmlBuf.append("\" paramVarIds=\"");
    if(paramVarIds.size() == 0) {
      xmlBuf.append(PlanGen.keyManager.defParamVar);
    }
    else {
      for(int i = 0; i < paramVarIds.size(); i++) {
        xmlBuf.append((String)paramVarIds.get(i)).append(" ");
      }
    }
    xmlBuf.append("\" slotId=\"");
    if(slotId == null)
      xmlBuf.append(PlanGen.keyManager.defSlot);
    else
      xmlBuf.append(slotId);
    xmlBuf.append("\" />\n");
    return xmlBuf.toString();
  }
}

class Variable
{
  private int low, hi;
  private String key, paramId, type, enumeration;
  private ArrayList constraintIds;
  public Variable(int low, int hi, String type)
  {
    key = PlanGen.keyManager.getKeyForVariable(this);
    this.low = low;
    this.hi = hi;
    this.type = type;
    constraintIds = new ArrayList();
    paramId = null;
    enumeration = null;
  }
  public Variable(int low, int hi, String type, String paramId)
  {
    this(low, hi, type);
    this.paramId = paramId;
  }
  public Variable(String enumeration, String type) {
    key = PlanGen.keyManager.getKeyForVariable(this);
    this.low = -1;
    this.hi = -2;
    this.paramId = null;
    this.type = type;
    this.enumeration = enumeration;
    constraintIds = new ArrayList();
  }
  public void addConstraint(String key)
  {
    constraintIds.add(key);
  }
  public String getId(){return key;}
  public String toXML()
  {
    StringBuffer xmlBuf = new StringBuffer("  <Variable key=\"").append(key).append("\" type=\"").append(type).append("\" constraintIds=\"");
    if(constraintIds.size() == 0)
      xmlBuf.append("K-3\" ");
    else
      {
        for(int i = 0; i < constraintIds.size(); i++)
          xmlBuf.append((String)constraintIds.get(i)).append(" ");
        xmlBuf.append("\" ");
      }
    xmlBuf.append("paramId=\"");
    if(paramId == null)
      xmlBuf.append("K-2\">\n");
    else
      xmlBuf.append(paramId).append("\">\n");
    if(hi == low)
      xmlBuf.append("    <EnumeratedDomain><![CDATA[").append(low).append("]]></EnumeratedDomain>\n");
    else if(hi < 0 && low < 0) {
      xmlBuf.append("    <EnumeratedDomain><![CDATA[").append(enumeration).append("]]></EnumeratedDomain>\n");
    }
    else
      xmlBuf.append("    <IntervalDomain type=\"INTEGER_SORT\" lowerBound=\"").append(low).append("\" upperBound=\"").append(hi).append("\" />\n");
    xmlBuf.append("  </Variable>\n");
    return xmlBuf.toString();
  }
}

class Predicate
{
  private String key, name;
  private ArrayList paramIds;
  public Predicate(int num)
  {
    key = PlanGen.keyManager.getKeyForPredicate(this);
    name = (new StringBuffer("Predicate ")).append(num).toString();
    int nparams = (PlanGenState.paramRange ? PlanGenState.getRandInRange(PlanGenState.paramRangeLo,
                                                                         PlanGenState.paramRangeHi)
                   : PlanGenState.nparams);
    paramIds = new ArrayList(nparams);
    for(int i = 0; i < nparams; i++)
      paramIds.add((new Param(i)).getId());
  }
  public List getParamIds()
  {
    return paramIds;
  }
  public String toXML()
  {
    StringBuffer xmlBuf = (new StringBuffer("  <Predicate key=\"")).append(key).append("\" name=\"").append(name).append("\">\n");
    for(int i = 0; i < paramIds.size(); i++)
      xmlBuf.append(PlanGen.keyManager.getParam((String)paramIds.get(i)).toXML());
    xmlBuf.append("  </Predicate>\n");
    return xmlBuf.toString();
  }
}

class Param  //Param for sale.  Any offers.  I'd like a bit of param, please.
{
  private String key, name;
  public Param(int num)
  {
    key = PlanGen.keyManager.getKeyForParam(this);
    name = (new StringBuffer("Param ")).append(num).append(" ").append(key).toString();
  }
  public String getId(){return key;}
  //i've got no StringBuffers to tie me down, to make me smile, to make me frown...
  public String toXML()
  {
    return "    <Parameter key=\"".concat(key).concat("\" name=\"").concat(name).concat("\"/>\n");
  }
}

class Constraint
{
  private String key, name, type, var1Id, var2Id;
  public Constraint(String var1Id, String var2Id, String name, String type)
  {
    key = PlanGen.keyManager.getKeyForConstraint(this);
    this.name = name;
    this.type = type;
    this.var1Id = var1Id;
    this.var2Id = var2Id;
    PlanGen.keyManager.getVariable(var1Id).addConstraint(key);
    PlanGen.keyManager.getVariable(var2Id).addConstraint(key);
  }
  public String toXML()
  {
    return (new StringBuffer("  <Constraint key=\"")).append(key).append("\" type=\"").append(type).append("\" name=\"").append(name).append("\" variableIds=\"").append(var1Id).append(" ").append(var2Id).append("\"/>\n").toString();
  }
}

class TokenRelation
{
  private String key, masterId, slaveId;
  public TokenRelation(String masterId, String slaveId)
  {
    key = PlanGen.keyManager.getKeyForTokenRelation(this);
    this.masterId = masterId;
    this.slaveId = slaveId;
    PlanGen.keyManager.getToken(masterId).addRelation(key);
    PlanGen.keyManager.getToken(slaveId).addRelation(key);
  }
  public String toXML()
  {
    return (new StringBuffer("  <TokenRelation key=")).append("\"").append(key).append("\" masterToken=\"").append(masterId).append("\" slaveToken=\"").append(slaveId).append("\"/>\n").toString();
  }
}
