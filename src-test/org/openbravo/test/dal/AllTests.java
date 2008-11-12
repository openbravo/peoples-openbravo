package org.openbravo.test.dal;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

    public static Test suite() {
	TestSuite suite = new TestSuite("Test for org.openbravo.test.dal");
	// $JUnit-BEGIN$
	// suite.addTestSuite(CompositeIdTest.class);
	suite.addTestSuite(HiddenUpdateTest.class);
	suite.addTestSuite(MappingGenerationTest.class);
	suite.addTestSuite(ValidationTest.class);
	suite.addTestSuite(DynamicEntityTest.class);
	suite.addTestSuite(DalTest.class);
	suite.addTestSuite(DalQueryTest.class);
	suite.addTestSuite(HqlTest.class);
	// $JUnit-END$
	return suite;
    }

}
