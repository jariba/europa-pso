package gov.nasa.arc.europa.utils.test

import gov.nasa.arc.europa.utils.LabelStr

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers

class LabelTest extends FunSuite with ShouldMatchers {
  test("basic creation") {
    val lbl1 = new LabelStr("");
    val lbl2 = new LabelStr("This is a char*")
    val lbl3 = new LabelStr(lbl2.toString())

    assert(lbl3 === lbl2)

    val labelStr2 = "This is another char*"
    assert(!LabelStr.isString(labelStr2))
    val lbl4 = new LabelStr(labelStr2)
    assert(LabelStr.isString(labelStr2))
    assert(lbl4 != lbl2)

    val key = lbl2.key
    val lbl5 = new LabelStr(key)
    assert(lbl5 === lbl2)
    assert(LabelStr.isString(key))
    assert(!LabelStr.isString(Integer.MAX_VALUE))

    assert(lbl3 === lbl2)

  }

  test("element counting") {
    val lbl1 = new LabelStr("A 1B 1C 1D EFGH");
    assert(lbl1.countElements("1") == 4);
    assert(lbl1.countElements(" ") == 5);
    assert(lbl1.countElements("B") == 2);
    assert(lbl1.countElements(":") == 1);

    val lbl2 = new LabelStr("A:B:C:D:");
    assert(lbl2.countElements(":") == 4);

  }

  test("element access") {
    val lbl1 = new LabelStr("A 1B 1C 1D EFGH");
    val first = new LabelStr(lbl1.getElement(0, " "));
    assert(first === new LabelStr("A"));

    val last = new LabelStr(lbl1.getElement(3, "1"));
    assert(last === new LabelStr("D EFGH"));

  }
  test("comparison") {
    val lbl1 = new LabelStr("A");
    val lbl2 = new LabelStr("G");
    val lbl3 = new LabelStr("B");
    val lbl4 = new LabelStr("B");
    assert(lbl1 < lbl2);
    assert(lbl2 > lbl4);
    assert(lbl2 != lbl4);
    assert(lbl4 === lbl3);

    val lbl5 = new LabelStr("ABCDEFGH");

    assert(lbl5.contains("A"));
    assert(lbl5.contains("H"));
    assert(lbl5.contains("FG"));
    assert(lbl5.contains(lbl5));
    assert(!lbl5.contains("I"));

  }
  test("map insertion") { 
    val m: Map[LabelStr, Int] = Map(LabelStr("foo") -> 1, LabelStr("Bar") -> 9)
    m should contain key (LabelStr("foo"))    
    m should not contain key (LabelStr("argle"))
  }
}
