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

import java.util.List;

import org.hibernate.criterion.Expression;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.xml.EntityXMLConverter;
import org.openbravo.model.ad.module.Module;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.ad.system.Language;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.geography.Country;
import org.openbravo.model.common.geography.CountryTrl;
import org.openbravo.service.db.DataImportService;
import org.openbravo.service.db.ImportResult;

/**
 * Test import of data
 * 
 * @author mtaal
 */

public class UniqueConstraintImportTest extends XMLBaseTest {

    // country trl table can be empty, create some test values
    public void testACreateCountryTrl() {
        setErrorOccured(true);
        setUserContext("0");
        final Country country = getCountry("Norway");
        final OBCriteria<CountryTrl> obc = OBDal.getInstance().createCriteria(
                CountryTrl.class);
        obc.add(Expression.eq("country", country));
        final List<CountryTrl> countryTrls = obc.list();
        if (countryTrls.size() > 0) {
            return;
        }

        final OBCriteria<Language> languageCriteria = OBDal.getInstance()
                .createCriteria(Language.class);
        final List<Language> languages = languageCriteria.list();
        int created = 0;
        for (final Language l : languages) {
            final CountryTrl countryTrl = OBProvider.getInstance().get(
                    CountryTrl.class);
            countryTrl.setCountry(country);
            countryTrl.setLanguage(l);
            countryTrl.setDescription(country.getDescription());
            countryTrl.setName(country.getName());
            countryTrl.setRegionName(country.getRegionName());
            countryTrl.setDisplaySequence(country.getDisplaySequence());
            OBDal.getInstance().save(countryTrl);
            created++;
        }
        System.err.println("Created " + created + " countrytrl objects");
        setErrorOccured(false);
    }

    // this test, reads countrytrl from the db and imports them again
    // after changing the id. This should result in updates of existing
    // countrytrl because they are found using the unique constraint of country
    // and language
    public void testCountryTrlImport() {
        setErrorOccured(true);
        setUserContext("100");

        // read countrytrl
        String xml = exportClass(CountryTrl.class, "country",
                getCountry("Norway"));

        // 
        xml = xml.replaceAll("<CountryTrl id=\"..", "<CountryTrl id=\"1k");

        final ImportResult ir = DataImportService.getInstance()
                .importDataFromXML(
                        OBDal.getInstance().get(Client.class, "1000001"),
                        OBDal.getInstance().get(Organization.class, "1000001"),
                        xml, OBDal.getInstance().get(Module.class, "0"));

        System.err.println("WARNING>>>>");
        System.err.println(ir.getWarningMessages());
        assertTrue(ir.getWarningMessages() != null
                && ir.getWarningMessages().trim().length() != 0);

        for (final BaseOBObject bob : ir.getUpdatedObjects()) {
            assertEquals(CountryTrl.class.getName(), bob.getClass().getName());
        }

        setErrorOccured(false);
    }

    private Country getCountry(String name) {
        final OBCriteria<Country> obc = OBDal.getInstance().createCriteria(
                Country.class);
        obc.add(Expression.eq("name", name));
        return obc.list().get(0);
    }

    private <T extends BaseOBObject> String exportClass(Class<T> clz,
            String field, Object value) {
        final OBCriteria<?> obc = OBDal.getInstance().createCriteria(clz);
        if (field != null) {
            obc.add(Expression.eq(field, value));
        }

        final EntityXMLConverter exc = EntityXMLConverter.newInstance();
        exc.setOptionIncludeChildren(true);
        exc.setOptionIncludeReferenced(true);
        exc.setAddSystemAttributes(false);

        @SuppressWarnings("unchecked")
        final List<BaseOBObject> list = (List<BaseOBObject>) obc.list();
        final String xml = exc.toXML(list);
        System.err.println(xml);
        return xml;
    }
}