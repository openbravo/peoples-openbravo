package org.openbravo.test;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.openbravo.test.dal.DalQueryTest;
import org.openbravo.test.dal.DalTest;
import org.openbravo.test.dal.DynamicEntityTest;
import org.openbravo.test.dal.HiddenUpdateTest;
import org.openbravo.test.dal.HqlTest;
import org.openbravo.test.dal.MappingGenerationTest;
import org.openbravo.test.dal.ValidationTest;
import org.openbravo.test.expression.EvaluationTest;
import org.openbravo.test.model.OneToManyTest;
import org.openbravo.test.model.RuntimeModelTest;
import org.openbravo.test.security.AccessLevelTest;
import org.openbravo.test.security.AllowedOrganisationsTest;
import org.openbravo.test.security.EntityAccessTest;
import org.openbravo.test.security.WritableReadableOrganisationTest;
import org.openbravo.test.xml.EntityXMLExportTest;
import org.openbravo.test.xml.EntityXMLImportTestBusinessObject;
import org.openbravo.test.xml.EntityXMLImportTestReference;
import org.openbravo.test.xml.EntityXMLImportTestSingle;
import org.openbravo.test.xml.EntityXMLImportTestWarning;

public class AllTests {

    public static Test suite() {
	TestSuite suite = new TestSuite("Test for org.openbravo.test.dal");
	// $JUnit-BEGIN$
	// suite.addTestSuite(CompositeIdTest.class);

	// security
	suite.addTestSuite(EntityAccessTest.class);
	suite.addTestSuite(AccessLevelTest.class);
	suite.addTestSuite(AllowedOrganisationsTest.class);
	suite.addTestSuite(WritableReadableOrganisationTest.class);

	// dal
	suite.addTestSuite(HiddenUpdateTest.class);
	suite.addTestSuite(MappingGenerationTest.class);
	suite.addTestSuite(ValidationTest.class);
	suite.addTestSuite(DynamicEntityTest.class);
	suite.addTestSuite(DalTest.class);
	suite.addTestSuite(DalQueryTest.class);
	suite.addTestSuite(HqlTest.class);

	// model
	suite.addTestSuite(RuntimeModelTest.class);
	suite.addTestSuite(OneToManyTest.class);

	// expression
	suite.addTestSuite(EvaluationTest.class);

	// xml
	suite.addTestSuite(EntityXMLExportTest.class);
	suite.addTestSuite(EntityXMLImportTestBusinessObject.class);
	suite.addTestSuite(EntityXMLImportTestSingle.class);
	suite.addTestSuite(EntityXMLImportTestReference.class);
	suite.addTestSuite(EntityXMLImportTestWarning.class);

	// $JUnit-END$
	return suite;
    }

}
