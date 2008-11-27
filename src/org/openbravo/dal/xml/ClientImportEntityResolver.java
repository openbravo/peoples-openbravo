/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.0  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html 
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License. 
 * The Original Code is Openbravo ERP. 
 * The Initial Developer of the Original Code is Openbravo SL 
 * All portions are Copyright (C) 2008 Openbravo SL 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.dal.xml;

import org.openbravo.base.model.AccessLevel;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.structure.BaseOBObject;

/**
 * This entity resolver is used in complete Client imports. With complete Client
 * imports all the data on Client/Organization level is present in the xml
 * String. This means that the EntityResolver only needs to search for existing
 * objects on System level. This class overrides some methods from the
 * {@link EntityResolver} to accomplish this. So, this entity resolver will only
 * search for existing objects at system level. New objects are assumed to have
 * the client/organization set through the xml.
 * <p/>
 * This resolver does not query the AD_REF_DATA_LOADED table.
 * 
 * @author mtaal
 */

public class ClientImportEntityResolver extends EntityResolver {

    public static ClientImportEntityResolver getInstance() {
        return OBProvider.getInstance().get(ClientImportEntityResolver.class);
    }

    // searches for a previous entity with the same id or an id retrieved from
    // the ad_ref_data_loaded table. The resolving takes into account different
    // access levels and
    @Override
    BaseOBObject resolve(String entityName, String id, boolean referenced) {

        final Entity entity = ModelProvider.getInstance().getEntity(entityName);

        BaseOBObject result = null;
        // note id can be null if someone did not care to add it in a manual
        // xml file
        if (id != null) {
            result = getData().get(entityName + id);
            if (result != null) {
                return result;
            }

            result = searchInstance(entity, id);
        }

        if (result != null) {
            // found, cache it for future use
            getData().put(entityName + id, result);
        } else {

            // not found create a new one
            result = (BaseOBObject) OBProvider.getInstance().get(entityName);

            if (id != null) {

                // force new
                result.setNewOBObject(true);

                // keep it here so it can be found later
                getData().put(entityName + id, result);
            }
        }
        return result;
    }

    // search on the basis of the access level of the entity
    @Override
    public BaseOBObject searchInstance(Entity entity, String id) {
        final AccessLevel al = entity.getAccessLevel();
        if (al == AccessLevel.SYSTEM) {
            return searchSystem(id, entity);
        }
        return null;
    }
}