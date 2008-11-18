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

package org.openbravo.test.model;

import java.util.List;

import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.base.model.UniqueConstraint;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.geography.Country;
import org.openbravo.test.base.BaseTest;

/**
 * Tests unique constraints
 * 
 * @author mtaal
 */

public class UniqueConstraintTest extends BaseTest {

    public void testUniqueConstraintLoad() {
        final Entity entity = ModelProvider.getInstance().getEntityByTableName(
                "C_Country_Trl");
        assertEquals(1, entity.getUniqueConstraints().size());
        dumpUniqueConstraints();
    }

    public void testUniqueConstraintQuerying() {
        setUserContext("1000001");
        OBContext.getOBContext().setInAdministratorMode(true);
        final List<Country> countries = OBDal.getInstance().createCriteria(
                Country.class).list();
        assertTrue(countries.size() > 0);
        for (final Country c : countries) {
            // make copy to not interfere with hibernate's auto update mechanism
            final Country copy = (Country) DalUtil.copy(c);
            copy.setId("test");
            final List<BaseOBObject> queried = OBDal.getInstance()
                    .findUniqueConstrainedObjects(copy);
            assertEquals(1, queried.size());
            assertEquals(c.getId(), queried.get(0).getId());
        }
    }

    // dump uniqueconstraints
    private void dumpUniqueConstraints() {
        for (final Entity e : ModelProvider.getInstance().getModel()) {
            if (e.getUniqueConstraints().size() > 0) {
                for (final UniqueConstraint uc : e.getUniqueConstraints()) {
                    System.err.println(">>> Entity " + e);
                    System.err.println("UniqueConstraint " + uc.getName());
                    for (final Property p : uc.getProperties()) {
                        System.err.print(p.getName() + " ");
                    }
                }
                System.err.println("");
            }
        }
    }

}