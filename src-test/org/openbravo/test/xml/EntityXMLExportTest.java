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

package org.openbravo.test.xml;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.criterion.Expression;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.xml.EntityXMLConverter;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.financialmgmt.tax.TaxCategory;
import org.openbravo.model.financialmgmt.tax.TaxRate;

/**
 * Allows testing of hql
 * 
 * @author mtaal
 */

public class EntityXMLExportTest extends XMLBaseTest {
    public void testExportTax() {
	setErrorOccured(true);
	setUserContext("1000000");
	final OBCriteria<?> obc = OBDal.getInstance().createCriteria(
		TaxRate.class);

	final EntityXMLConverter exc = EntityXMLConverter.newInstance();
	exc.setOptionIncludeChildren(true);
	exc.setOptionIncludeReferenced(true);
	exc.setAddSystemAttributes(false);

	@SuppressWarnings("unchecked")
	final List<BaseOBObject> list = (List<BaseOBObject>) obc.list();
	final String xml = exc.toXML(list);
	System.err.println(xml);
    }

    public void testExportTaxCategory() {
	setErrorOccured(true);
	setUserContext("1000000");
	final OBCriteria<?> obc = OBDal.getInstance().createCriteria(
		TaxCategory.class);

	obc.setFilterOnActive(false);
	obc.setFilterOnReadableClients(false);
	obc.setFilterOnReadableOrganisation(false);

	final EntityXMLConverter exc = EntityXMLConverter.newInstance();
	exc.setOptionIncludeChildren(true);
	exc.setOptionIncludeReferenced(true);
	exc.setAddSystemAttributes(false);

	@SuppressWarnings("unchecked")
	final List<BaseOBObject> list = (List<BaseOBObject>) obc.list();
	final String xml = exc.toXML(list);
	System.err.println(xml);
    }

    public void testXMLExport() {
	setErrorOccured(true);
	setUserContext("1000000");
	final OBCriteria<BusinessPartner> obc = OBDal.getInstance()
		.createCriteria(BusinessPartner.class);
	// a bit complexer expression, just to test that
	obc.add(Expression.and(Expression.in("id", new String[] { "1000006",
		"1000007", "1000008", "1000009" }), Expression.and(Expression
		.ge("id", "1000006"), Expression.le("id", "1000009"))));
	obc.addOrderBy("name", true);
	final List<BusinessPartner> bps = obc.list();
	assertEquals(4, bps.size());
	final EntityXMLConverter exc = EntityXMLConverter.newInstance();
	exc.setOptionIncludeChildren(true);
	exc.setOptionIncludeReferenced(true);
	exc.setOptionEmbedChildren(true);
	exc.setAddSystemAttributes(false);
	final String xml = exc.toXML(new ArrayList<BaseOBObject>(bps));
	compare(xml, "bp_list_1.xml");
    }

    public void testXMLExportIncludeReference() {
	setErrorOccured(true);
	setUserContext("1000000");
	final OBCriteria<BusinessPartner> obc = OBDal.getInstance()
		.createCriteria(BusinessPartner.class);
	obc.addOrderBy("name", true);
	final List<BusinessPartner> bps = obc.list();
	// only export the first three, otherwise it is to big..
	bps
		.removeAll(new ArrayList<BusinessPartner>(bps.subList(3, bps
			.size())));
	final EntityXMLConverter exc = EntityXMLConverter.newInstance();
	exc.setOptionIncludeReferenced(true);
	exc.setAddSystemAttributes(false);
	final String xml = exc.toXML(new ArrayList<BaseOBObject>(bps));
	compare(xml, "bp_list_2.xml");
    }

    public void testXMLExportIncludeChildren() {
	setErrorOccured(true);
	setUserContext("1000000");
	final OBCriteria<BusinessPartner> obc = OBDal.getInstance()
		.createCriteria(BusinessPartner.class);
	obc.addOrderBy("name", true);
	final List<BusinessPartner> bps = obc.list();
	// only export the first three, otherwise it is to big..
	bps
		.removeAll(new ArrayList<BusinessPartner>(bps.subList(3, bps
			.size())));
	final EntityXMLConverter exc = EntityXMLConverter.newInstance();
	exc.setOptionIncludeChildren(true);
	exc.setAddSystemAttributes(false);
	final String xml = exc.toXML(new ArrayList<BaseOBObject>(bps));
	// System.err.println(xml);
	compare(xml, "bp_list_3.xml");
    }
}