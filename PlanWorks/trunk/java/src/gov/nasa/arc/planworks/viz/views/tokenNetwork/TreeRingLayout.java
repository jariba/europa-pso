package gov.nasa.arc.planworks.viz.views.tokenNetwork;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;

import gov.nasa.arc.planworks.util.OneToManyMap;
import gov.nasa.arc.planworks.util.UniqueSet;
import gov.nasa.arc.planworks.viz.nodes.TokenNode;
import gov.nasa.arc.planworks.viz.nodes.TokenLink;

import com.nwoods.jgo.JGoDocument;
import com.nwoods.jgo.JGoLayer;
import com.nwoods.jgo.JGoView;

public class TreeRingLayout {
  OneToManyMap relationMap;
  ActualLayout layout;
  public TreeRingLayout(List tokenLinkList, int dx, int dy) {
    relationMap = new OneToManyMap();
    ListIterator tokenLinkIterator = tokenLinkList.listIterator();
    UniqueSet supremeMasters = new UniqueSet();
    UniqueSet slaves = new UniqueSet();
    while(tokenLinkIterator.hasNext()) {
      TokenLink link = (TokenLink) tokenLinkIterator.next();
      TokenNode master = link.getFromTokenNode();
      TokenNode slave = link.getToTokenNode();
      supremeMasters.add(master);
      slaves.add(slave);
      relationMap.put(master, slave);
    }
    supremeMasters.removeAll(slaves);
    System.err.println("Laying out...");
    layout = new ActualLayout(relationMap);
    layout.layout(supremeMasters, dx, dy);
    System.err.println("Done!");
  }
  public void ensureAllPositive() {
    layout.ensureAllPositive();
  }
  public int getNumRings(){return layout.getNumRings();}
  public double getRingRadius(int index){return layout.getRingRadius(index);}
  public Point getRingCenter(){return layout.getRingCenter();}
}
class ActualLayout {
  private static final double TWOPI = 2 * Math.PI;
  private static final double PITWO = Math.PI/2.;
  private static final double RADMIN = 10.;
  private Vector rings;
  private OneToManyMap relationMap;
  public ActualLayout(OneToManyMap relationMap) {
    rings = new Vector();
    this.relationMap = relationMap;
  }
  public void layout(List rootNodes, int dx, int dy) {
    System.err.println(dx + " " + dy);
    Ring centerRing = new Ring(0.);
    rings.add(centerRing);
    if(rootNodes.size() == 0) {
      rings.remove(centerRing);
      throw new IllegalArgumentException();
    }
    else if(rootNodes.size() == 1) {
      centerRing.add(new TreeNode((TokenNode)rootNodes.get(0), Math.PI, PITWO));
    }
    else if(rootNodes.size() > 1) {
      TokenNode temp = new TokenNode();
      TreeNode supremeMaster = new TreeNode(temp, Math.PI, PITWO);
      relationMap.put(temp, rootNodes);
      centerRing.add(supremeMaster);
    }
    centerRing.positionNodes(0., dx, dy);
    nextRingLayout(centerRing.getNodes(), dx, dy, centerRing.getWidest() + RADMIN);
  }
  private void nextRingLayout(List masterNodes, int dx, int dy, double minRadius) {
    if(masterNodes.size() == 0) {
      return;
    }
    Ring nextRing = new Ring();
    rings.add(nextRing);
    int numSlaves = 0;
    for(int i = 0, n = masterNodes.size(); i < n; i++) {
      List slaveList = (List)relationMap.get(((TreeNode) masterNodes.get(i)).getTokenNode());
      if(slaveList != null) {
        numSlaves += slaveList.size();
      }
    }
    if(numSlaves == 0) {
      rings.remove(nextRing);
      return;
    }
    System.err.println("Number of master nodes: " + masterNodes.size());
    //numSlaves = ((numSlaves < 5) ? 5 : numSlaves);
    //double vizRange = Math.PI/numSlaves;
    for(int i = 0, n = masterNodes.size(); i < n; i++) {
      TreeNode master = (TreeNode) masterNodes.get(i);
      List slaves = (List)relationMap.get(master.getTokenNode());
      if(slaves == null) {
        continue;
      }
      //numSlaves = ((slaves.size() < 5) ? 5 : slaves.size());
      double vizRange = Math.PI/6;
      double masterPosition = master.getTheta();
      if((slaves.size() & 1) == 1) {
        nextRing.add(new TreeNode((TokenNode)slaves.get(0), masterPosition, vizRange));
        slaves.remove(0);
      }
      double rangeIncrement = master.getVisibilityWidth()/slaves.size();
      boolean switcher = false;
      double k = 1.;
      for(int j = 0, m = slaves.size(); j < m; j++, k += 0.5) {
        double position =
          ((switcher ^= true) ? -rangeIncrement : rangeIncrement) * Math.floor(k);
        nextRing.add(new TreeNode((TokenNode)slaves.get(j),
                                  masterPosition + position, vizRange));
      }
    }
    double radius = (nextRing.getNodes().size() * nextRing.getWidest())/TWOPI;
    radius = Math.max(radius, minRadius);
    System.err.println("Ring radius " + radius);
    nextRing.positionNodes(radius, dx, dy);
    radius += Math.max(RADMIN, nextRing.getWidest());
    nextRingLayout(nextRing.getNodes(), dx, dy, radius);
  }
  public void ensureAllPositive() {
    double leastNegativeX = 0.;
    double leastNegativeY = 0.;
    Iterator ringIterator = rings.iterator();
    while(ringIterator.hasNext()) {
      Ring ring = (Ring) ringIterator.next();
      ListIterator nodeIterator = ring.getNodes().listIterator();
      while(nodeIterator.hasNext()) {
        TreeNode node = (TreeNode) nodeIterator.next();
        if(node.getX() < leastNegativeX) {
          leastNegativeX = node.getX();
        }
        if(node.getY() < leastNegativeY) {
          leastNegativeY = node.getY();
        }
      }
    }
    System.err.println(leastNegativeX + ", " + leastNegativeY);
    leastNegativeX = Math.abs(leastNegativeX);
    leastNegativeY = Math.abs(leastNegativeY);
    ringIterator = rings.iterator();
    while(ringIterator.hasNext()) {
      Ring ring = (Ring) ringIterator.next();
      ListIterator nodeIterator = ring.getNodes().listIterator();
      while(nodeIterator.hasNext()) {
        TreeNode node = (TreeNode) nodeIterator.next();
        node.setX(node.getX() + leastNegativeX);
        node.setY(node.getY() + leastNegativeY);
        if(node.getTokenNode().getToken() != null) {
          System.err.println(node.getTokenNode().getToken().getKey() + ": (" + node.getX() + ", " +
                             node.getY() + ") " + node.getThetaDegrees() + " " + node.getVisibilityWidthDegrees());
        }
      }
    }
  }
  public int getNumRings(){return rings.size();}
  public double getRingRadius(int index){return ((Ring)rings.get(index)).getRadius();}
  public Point getRingCenter() {
    Ring centerRing = (Ring) rings.get(0);
    TreeNode centerNode = (TreeNode) centerRing.getNodes().get(0);
    System.err.println(centerNode.getX() + ", " + centerNode.getY());
    return new Point((int)centerNode.getX(), (int)centerNode.getY());
  }
}
class Ring {
  private double radius, widest;
  private ArrayList nodes;
  public Ring() {
    radius = 0;
    widest = 0;
    nodes = new ArrayList();
  }
  public Ring(double radius) {
    this();
    this.radius = radius;
  }
  public void setRadius(double radius) {
    this.radius = radius;
  }
  public void add(TreeNode node) {
    nodes.add(node);
    if(node.getTokenNode().getSize().getWidth() > widest) {
      widest = node.getTokenNode().getSize().getWidth();
    }
  }
  public double getRadius() {return radius;}
  public List getNodes() {
    return nodes;
  }
  public double getWidest() {
    return widest;
  }
  public void positionNodes(double r, int dx, int dy) {
    setRadius(r);
    System.err.println("Radius: " + radius);
    double rsquared = Math.pow(radius, 2.);
    ListIterator nodeIterator = nodes.listIterator();
    while(nodeIterator.hasNext()) {
      TreeNode treeNode = (TreeNode) nodeIterator.next();
      treeNode.calculatePosition(radius, dx, dy);
    }
  }
}
class TreeNode {
  private double x, y, theta, visibility;
  private TokenNode token;
  public TreeNode(TokenNode token, double theta, double visibility) {
    this.token = token;
    this.theta = theta;
    this.visibility = visibility;
    this.x = this.y = 0.;
  }
  public void setTheta(double newTheta) {
    this.theta = newTheta;
  }
  public void calculatePosition(double r, int dx, int dy) {
    if(token.getToken() != null) {
      System.err.println(token.getToken().getKey());
    }
    System.err.println("theta: " + getThetaDegrees());
    x = r * Math.cos(theta);
    y = r * Math.sin(theta);
    x -= (token.getSize().getWidth()/2);
    y -= (token.getSize().getHeight()/2);
    x += dx;
    y += dy;
    //i hope this doesn't hurt...
    if(token.getToken() == null) {
      return;
    }
    token.setLocation((int)x, (int)y);
  }
  public double getVisibilityStart() {return theta - visibility;}
  public double getVisibilityEnd() {return theta + visibility;}
  public double getVisibilityWidth() {return /*2. * */visibility;}
  public double getVisibilityWidthDegrees(){return getVisibilityWidth() * (180/Math.PI);}
  public double getTheta(){return theta;}
  public double getThetaDegrees(){return theta * (180/Math.PI);}
  public double getX() {return x;}
  public double getY() {return y;}
  public void setX(double x){this.x = x;}
  public void setY(double y){this.y = y;}
  public TokenNode getTokenNode() {return token;}
}
