package gov.nasa.arc.planworks.viz.views.test;

import java.util.Set;
import junit.extensions.jfcunit.*;
import gov.nasa.arc.planworks.proj.test.PwProjectTest;

public class TimelineViewTest extends JFCTestCase
{
		private JFCTestHelper helper;
		
		public TimelineViewTest(String test)
		{
				super(test);
		}

		public void setUp()
		{
				helper = new JFCTestHelper();
		}
		
		public void tearDown() throws Exception
		{
				helper.cleanUp(this);
				super.tearDown();
		}

		public void testMain()
		{
				Set windows;
				String [] args = new String [3];
				String jvm_ge_14 = new String("false");
				if(System.getProperty("java.version").compareTo("1.4.1") >= 0)
						jvm_ge_14 = new String("true");
				args[0] = System.getProperty("os.name");
				args[1] = jvm_ge_14;
				args[2] = System.getProperty("xml.files.dir");
				PwProjectTest.main(args);
				awtSleep();
				windows = helper.getWindows();
				assertEquals("Timeline window failed to open", 1, windows.size());
		}
}
