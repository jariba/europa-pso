package gov.nasa.arc.planworks.dbg.testLang;

public class TestLangRuntimeException extends Exception {
  public TestLangRuntimeException(final String message) {
    super(message);
  }
  public TestLangRuntimeException(final Throwable other) {
    super(other);
  }
}
