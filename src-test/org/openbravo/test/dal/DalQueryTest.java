/*
 * 
 * Copyright (C) 2001-2008 Openbravo S.L. Licensed under the Apache Software
 * License version 2.0 You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package org.openbravo.test.dal;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.criterion.Expression;
import org.openbravo.base.exception.OBSecurityException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.model.ad.domain.ModelImplementation;
import org.openbravo.model.ad.domain.ModelImplementationMapping;
import org.openbravo.model.common.businesspartner.Category;
import org.openbravo.model.common.businesspartner.CategoryAccounts;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.materialmgmt.transaction.MaterialTransaction;
import org.openbravo.test.base.BaseTest;

/**
 * Test different parts of the dal api.
 * 
 * Note the testcases assume that they are run in the order defined in this
 * class.
 * 
 * @author mtaal
 */

public class DalQueryTest extends BaseTest {

    public void testDalFirstWhereLeftJoinClause() {
	setErrorOccured(true);
	setUserContext("100");
	final String where = "as mo left join mo.callout left join mo.reference left join mo.form left join mo.process left join mo.workflow left join mo.tab where mo.callout.module.id='0' or mo.reference.module.id='0' or mo.form.module.id='0' or mo.process.module.id='0' or mo.workflow.module.id='0' or mo.tab.module.id='0'";
	final OBQuery<ModelImplementation> obq = OBDal.getInstance()
		.createQuery(ModelImplementation.class, where);
	assertTrue(obq.list().size() > 0);
	setErrorOccured(false);
    }

    public void testDalExtraJoinWhereLeftJoinClause() {
	setErrorOccured(true);
	setUserContext("100");
	final String where = "as mom left join mom."
		+ ModelImplementationMapping.PROPERTY_MODELIMPLEMENTATION
		+ " as mo left join mo."
		+ ModelImplementation.PROPERTY_CALLOUT
		+ " left join mo."
		+ ModelImplementation.PROPERTY_REFERENCE
		+ " left join mo."
		+ ModelImplementation.PROPERTY_FORM
		+ " left join mo."
		+ ModelImplementation.PROPERTY_PROCESS
		+ " left join mo."
		+ ModelImplementation.PROPERTY_WORKFLOW
		+ " left join mo."
		+ ModelImplementation.PROPERTY_TAB
		+ " where mo.callout.module.id='0' or mo.reference.module.id='0' or mo.form.module.id='0' or mo.process.module.id='0' or mo.workflow.module.id='0' or mo.tab.module.id='0'";
	final OBQuery<ModelImplementationMapping> obq = OBDal.getInstance()
		.createQuery(ModelImplementationMapping.class, where);
	assertTrue(obq.list().size() > 0);
	setErrorOccured(false);
    }

    public void testDalWhereLeftJoinClause() {
	setErrorOccured(true);
	setUserContext("100");
	final String where = "as mo left join mo.callout left join mo.reference where mo.callout.module.id='0' or mo.reference.module.id='0'";
	final OBQuery<ModelImplementation> obq = OBDal.getInstance()
		.createQuery(ModelImplementation.class, where);
	assertTrue(obq.list().size() > 0);
	setErrorOccured(false);
    }

    public void testDalOtherWhereLeftJoinClause() {
	setErrorOccured(true);
	setUserContext("100");
	final String where = "as mo left join mo.callout left join mo.reference where (mo.callout.module.id='0' or mo.reference.module.id='0') and exists(from ADUser where id<>'0')";
	final OBQuery<ModelImplementation> obq = OBDal.getInstance()
		.createQuery(ModelImplementation.class, where);
	assertTrue(obq.list().size() > 0);
	setErrorOccured(false);
    }

    public void testDalAnOtherWhereLeftJoinClause() {
	setErrorOccured(true);
	setUserContext("100");
	final String where = "exists(from ADUser where id<>'0')";
	final OBQuery<ModelImplementation> obq = OBDal.getInstance()
		.createQuery(ModelImplementation.class, where);
	assertTrue(obq.list().size() > 0);
	setErrorOccured(false);
    }

    // creates a new BPGroup, test simple save, BPGroup is removed in next test
    public void testCreateBPGroup() {
	setErrorOccured(true);
	setUserContext("1000001");
	final Category bpg = OBProvider.getInstance().get(Category.class);
	bpg.setDefault(true);
	bpg.setDescription("testdescription");
	bpg.setName("testname");
	bpg.setValue("testvalue");
	OBDal.getInstance().save(bpg);
	setErrorOccured(false);
    }

    // query for the BPGroup again and remove it
    public void testRemoveBPGroup() {
	setErrorOccured(true);
	setUserContext("1000001");
	final OBQuery<Category> obQuery = OBDal.getInstance().createQuery(
		Category.class,
		Category.PROPERTY_NAME + "='testname' or "
			+ Category.PROPERTY_VALUE + "='testvalue'");
	final List<Category> bpgs = obQuery.list();
	assertEquals(1, bpgs.size());
	final Category bpg = bpgs.get(0);
	final OBContext obContext = OBContext.getOBContext();
	assertEquals(obContext.getUser().getId(), bpg.getCreatedBy().getId());
	assertEquals(obContext.getUser().getId(), bpg.getUpdatedBy().getId());
	// update and create have occured less than one second ago
	// note that if the delete fails for other reasons that you will have a
	// currency in the database which has for sure a created/updated time
	// longer in the past, You need to manually delete the currency record
	if (false) {
	    assertTrue(
		    "Created time not updated",
		    (System.currentTimeMillis() - bpg.getCreated().getTime()) < 2000);
	    assertTrue(
		    "Updated time not updated",
		    (System.currentTimeMillis() - bpg.getUpdated().getTime()) < 2000);
	}

	// first delete the related accounts
	final List<Object> parameters = new ArrayList<Object>();
	parameters.add(bpgs.get(0));
	final OBQuery<CategoryAccounts> q2 = OBDal.getInstance().createQuery(
		CategoryAccounts.class,
		" " + CategoryAccounts.PROPERTY_CATEGORY + "=?", parameters);
	final List<CategoryAccounts> bpgas = q2.list();
	for (CategoryAccounts bga : bpgas) {
	    OBDal.getInstance().remove(bga);
	}
	OBDal.getInstance().remove(bpgs.get(0));
	setErrorOccured(false);
    }

    // check if the BPGroup was removed
    public void testCheckBPGroupRemoved() {
	setErrorOccured(true);
	setUserContext("1000001");
	final OBQuery<Category> obQuery = OBDal.getInstance().createQuery(
		Category.class,
		Category.PROPERTY_NAME + "='testname' or "
			+ Category.PROPERTY_VALUE + "='testvalue'");
	final List<Category> bpgs = obQuery.list();
	assertEquals(0, bpgs.size());
	setErrorOccured(false);
    }

    // test querying for a specific currency and then updating it
    // should fail for a user
    public void testUpdateCurrencyByUser() {
	setErrorOccured(true);
	setUserContext("1000019");
	final OBCriteria<Currency> obc = OBDal.getInstance().createCriteria(
		Currency.class);
	obc.add(Expression.eq(Currency.PROPERTY_ISOCODE, "USD"));
	final List<Currency> cs = obc.list();
	assertEquals(1, cs.size());
	final Currency c = cs.get(0);
	c.setDescription(c.getDescription() + " a test");
	try {
	    OBDal.getInstance().save(c);
	    fail("No security check");
	} catch (OBSecurityException e) {
	    // successfull check
	}
	setErrorOccured(false);
    }

    // tests a paged read of transactions and print of the identifier.
    // the identifier of a transaction has been implemented such that
    // it reads all the references (which are non-null) and uses their
    // identifier to create the identifier of the transaction.
    // test sorting on product.name
    public void testTransaction25PageRead() {
	setErrorOccured(true);
	setUserContext("1000001");
	final OBQuery<MaterialTransaction> cq = OBDal.getInstance()
		.createQuery(MaterialTransaction.class,
			" order by product.name");
	final int count = cq.count();
	final int pageSize = 25;
	int pageCount = 1 + (count / pageSize);
	if (pageCount > 25) {
	    pageCount = 25;
	}
	for (int i = 0; i < pageCount; i++) {
	    final OBQuery<MaterialTransaction> obq = OBDal.getInstance()
		    .createQuery(
			    MaterialTransaction.class,
			    " order by " + MaterialTransaction.PROPERTY_PRODUCT
				    + "." + Product.PROPERTY_NAME);
	    final Query qry = obq.createQuery();
	    qry.setMaxResults(pageSize);
	    qry.setFirstResult(i * pageSize);

	    System.err.println("PAGE>>> " + (1 + i));
	    for (Object o : qry.list()) {
		System.err.println(((MaterialTransaction) o).getIdentifier());
	    }
	}
	setErrorOccured(false);
    }
}