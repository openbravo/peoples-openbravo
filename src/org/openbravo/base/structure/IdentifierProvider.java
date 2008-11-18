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
package org.openbravo.base.structure;

import java.util.List;

import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.provider.OBSingleton;

/**
 * Provides the identifier/title of an object using the application dictionary.
 * 
 * Note: the getIdentifier can also be generated in the java entity but the
 * current approach makes it possible to change the identifier definition at
 * runtime.
 * 
 * @author mtaal
 */

public class IdentifierProvider implements OBSingleton {

    private static IdentifierProvider instance;

    public static IdentifierProvider getInstance() {
        if (instance == null) {
            instance = OBProvider.getInstance().get(IdentifierProvider.class);
        }
        return instance;
    }

    public static void setInstance(IdentifierProvider instance) {
        IdentifierProvider.instance = instance;
    }

    // also use refered to identifiables to create the identifier
    public String getIdentifier(Object o) {
        return getIdentifier(o, true);
    }

    // identifyDeep determines if refered to objects are used
    // to identify the object
    public String getIdentifier(Object o, boolean identifyDeep) {
        // TODO: add support for null fields
        final StringBuilder sb = new StringBuilder();
        final DynamicEnabled dob = (DynamicEnabled) o;
        final String entityName = ((Identifiable) dob).getEntityName();
        final List<Property> identifiers = ModelProvider.getInstance()
                .getEntity(entityName).getIdentifierProperties();

        for (Property identifier : identifiers) {
            if (sb.length() > 0) {
                sb.append(" ");
            }
            final Object value = dob.get(identifier.getName());

            if (value instanceof Identifiable && identifyDeep) {
                sb.append(getIdentifier(value, false));
            } else if (value != null) {
                sb.append(value);
            }
        }
        if (identifiers.size() == 0) {
            return entityName + " (" + ((Identifiable) dob).getId() + ")";
        }
        return sb.toString();
    }
}