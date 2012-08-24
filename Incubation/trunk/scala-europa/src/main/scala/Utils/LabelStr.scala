package gov.nasa.arc.europa.utils;

import scala.collection.mutable.HashMap;
import scala.collection.mutable.Map;

object LabelStr {
  private var stringToKey: Map[String, Int] = new HashMap[String, Int]();
  private var keyToString: Map[Int, String] = new HashMap[Int, String]();
  private var key: Int = 0;
  private def nextKey: Int = {val retval = key; key = key + 1; return retval;}


  private def insert(s: String): Int = {
    stringToKey.get(s) match {
      case None => {
	val key = nextKey;
	keyToString += (key -> s)
	stringToKey += (s -> key)
	return key;
      }
      case Some(k) => k
      
    }
  }

  def size = stringToKey.size
  def getKey(s: String) = insert(s)
  def isString(s: String) = stringToKey contains s
  def isString(k: Int) = keyToString contains k
  def apply(k: Int) = new LabelStr(k)
  def apply(s: String = "") = new LabelStr(s)

  implicit def fromString(s: String): LabelStr = LabelStr(s)
  implicit def toString(l: LabelStr): String = l.toString
}

class LabelStr(k: Int) extends Ordered[LabelStr] {
  val key = k
  if(!LabelStr.isString(k))
    throw new IllegalArgumentException;

  def this(s: String = "") = {
    this(LabelStr.insert(s))
  }
  def this(s: LabelStr) = this(s.key)
  def this() = this("")

  def ==(o: LabelStr) = key == o.key;
  def !=(o: LabelStr) = key != o.key;

  override def toString: String = LabelStr.keyToString.get(key) match {
    case None => "__ErRoR!!11!!__"
    case Some(s: String) => s
  }

  def compare(o: LabelStr): Int = toString.compare(o.toString)

  def contains(o: LabelStr): Boolean = toString.indexOf(o.toString()) != -1
  def contains(s: String): Boolean = contains(new LabelStr(s))
  
  def countElements(delim: String): Int = toString.split(delim).length

  def getElement(i: Int, delim: String) = toString.split(delim)(i)
  def length = toString.length

}
