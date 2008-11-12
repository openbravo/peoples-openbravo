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

import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.plm.Product;
import org.openbravo.test.base.BaseTest;

/**
 * Does some simple performance tests.
 * 
 * @author mtaal
 */

public class DalPerformanceProductTest extends BaseTest {
    // tests a paged read of products and print of the identifier.
    public void testProduct25PageRead() {
	setErrorOccured(true);
	setUserContext("1000019");
	final OBCriteria<Product> countObc = OBDal.getInstance()
		.createCriteria(Product.class);
	final int count = countObc.count();
	System.err.println("Number of products " + count);
	final int pageSize = 25;
	final int pageCount = 1 + (count / pageSize);
	long time = System.currentTimeMillis();
	long avg = 0;
	for (int i = 0; i < pageCount; i++) {
	    final OBCriteria<Product> obc = OBDal.getInstance().createCriteria(
		    Product.class);
	    obc.setFilterOnReadableOrganisation(false);
	    obc.addOrderBy(Product.PROPERTY_NAME, true);
	    obc.setMaxResults(pageSize);
	    obc.setFirstResult(i * pageSize);

	    System.err.println("PAGE>>> " + (1 + i));
	    for (final Product t : obc.list()) {
		System.err.println(t.getIdentifier());
	    }
	    if (avg == 0) {
		avg = System.currentTimeMillis() - time;
	    } else {
		avg = (avg + System.currentTimeMillis() - time) / 2;
	    }
	    time = System.currentTimeMillis();
	    OBDal.getInstance().commitAndClose();
	}

	System.err.println("Read " + pageCount + " pages with average " + avg
		+ " milliSeconds per page");
	setErrorOccured(false);
    }

    // tests a paged read of products and print of the identifier.
    public void testProduct25PageReadGetExtra() {
	setErrorOccured(true);
	setUserContext("1000019");
	final OBCriteria<Product> countObc = OBDal.getInstance()
		.createCriteria(Product.class);
	final int count = countObc.count();
	System.err.println("Number of products " + count);
	final int pageSize = 25;
	final int pageCount = 1 + (count / pageSize);
	long time = System.currentTimeMillis();
	long avg = 0;
	for (int i = 0; i < pageCount; i++) {
	    final OBCriteria<Product> obc = OBDal.getInstance().createCriteria(
		    Product.class);
	    obc.setFilterOnReadableOrganisation(false);
	    obc.addOrderBy(Product.PROPERTY_NAME, true);
	    obc.setMaxResults(pageSize);
	    obc.setFirstResult(i * pageSize);

	    System.err.println("PAGE>>> " + (1 + i));
	    for (final Product t : obc.list()) {
		System.err.println(t.toString()
			+ " Product Category "
			+ (t.getProductCategory() != null ? t
				.getProductCategory().getIdentifier() : "NULL")
			+ " Tax Category "
			+ (t.getTaxCategory() != null ? t.getTaxCategory()
				.getIdentifier() : "NULL"));
		System.err.println(t.toString());
	    }
	    if (avg == 0) {
		avg = System.currentTimeMillis() - time;
	    } else {
		avg = (avg + System.currentTimeMillis() - time) / 2;
	    }
	    time = System.currentTimeMillis();
	    OBDal.getInstance().commitAndClose();
	}

	System.err.println("Read " + pageCount + " pages with average " + avg
		+ " milliSeconds per page (read extra info)");
	setErrorOccured(false);
    }

    // tests reading all products
    public void testReadProducts() {
	setErrorOccured(true);
	setUserContext("1000019");
	final OBCriteria<Product> obc = OBDal.getInstance().createCriteria(
		Product.class);
	obc.setFilterOnReadableOrganisation(false);
	obc.addOrderBy(Product.PROPERTY_NAME, true);

	final long time = System.currentTimeMillis();
	for (final Product t : obc.list()) {
	    final String rs = t.toString()
		    + " Product Category "
		    + (t.getProductCategory() != null ? t.getProductCategory()
			    .getIdentifier() : "NULL")
		    + " Tax Category "
		    + (t.getTaxCategory() != null ? t.getTaxCategory()
			    .getIdentifier() : "NULL");
	    System.err.println(rs);
	}

	System.err.println("Read 75000 products in "
		+ (System.currentTimeMillis() - time)
		+ " milliSeconds (reading extra info)");
	setErrorOccured(false);
    }

    // tests a paged read of products and print of the identifier.
    public void testUpdateAllProducts() {
	setErrorOccured(true);
	setUserContext("1000019");
	final OBCriteria<Product> countObc = OBDal.getInstance()
		.createCriteria(Product.class);
	final int count = countObc.count();
	System.err.println("Number of products " + count);
	final long time = System.currentTimeMillis();
	final OBCriteria<Product> obc = OBDal.getInstance().createCriteria(
		Product.class);
	obc.setFilterOnReadableOrganisation(false);
	obc.addOrderBy(Product.PROPERTY_NAME, true);

	OBContext.getOBContext().setInAdministratorMode(true);
	for (final Product t : obc.list()) {
	    t.setName(t.getName() + "t");
	    OBDal.getInstance().save(t);
	}
	OBDal.getInstance().flush();
	OBDal.getInstance().commitAndClose();
	System.err.println("Updated " + count + " products in "
		+ (System.currentTimeMillis() - time) + " milliseconds ");
	setErrorOccured(false);
    }

    public void testUpdateAllProductsByPage() {
	setErrorOccured(true);
	setUserContext("1000019");
	OBContext.getOBContext().setInAdministratorMode(true);
	final OBCriteria<Product> countObc = OBDal.getInstance()
		.createCriteria(Product.class);
	final int count = countObc.count();
	System.err.println("Number of products " + count);
	final int pageSize = 25;
	final int pageCount = 1 + (count / pageSize);
	long time = System.currentTimeMillis();
	long avg = 0;
	for (int i = 0; i < pageCount; i++) {
	    final OBCriteria<Product> obc = OBDal.getInstance().createCriteria(
		    Product.class);
	    obc.setFilterOnReadableOrganisation(false);
	    obc.addOrderBy(Product.PROPERTY_NAME, true);
	    obc.setMaxResults(pageSize);
	    obc.setFirstResult(i * pageSize);

	    // System.err.println("PAGE>>> " + (1 + i));
	    for (final Product t : obc.list()) {
		t.setName(t.getName() + "t");
		OBDal.getInstance().save(t);
	    }
	    if (avg == 0) {
		avg = System.currentTimeMillis() - time;
	    } else {
		avg = (avg + System.currentTimeMillis() - time) / 2;
	    }
	    time = System.currentTimeMillis();
	    OBDal.getInstance().commitAndClose();
	}

	System.err.println("Updated " + pageCount
		+ " pages of products with average " + avg
		+ " milliSeconds per page and 25 products per page");
	setErrorOccured(false);
    }
}