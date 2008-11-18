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

import static org.openbravo.model.ad.system.Client.PROPERTY_ORGANIZATION;
import static org.openbravo.model.common.enterprise.Organization.PROPERTY_CLIENT;

import java.util.Date;

import org.openbravo.base.model.Entity;
import org.openbravo.base.model.Property;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.common.enterprise.Organization;

/**
 * Dynamic OB Object which supports full dynamic mapping without java members.
 * 
 * @author mtaal
 */

public class DynamicOBObject extends BaseOBObject implements Traceable,
        ClientEnabled, OrganizationEnabled {

    private String entityName;

    public boolean isNew() {
        return getId() == null;
    }

    public boolean isActive() {
        return (Boolean) get(Organization.PROPERTY_ISACTIVE);
    }

    public void setActive(boolean active) {
        set(Organization.PROPERTY_ISACTIVE, active);
    }

    @Override
    public String getId() {
        return (String) get(Organization.PROPERTY_ID);
    }

    public void setId(String id) {
        set(Organization.PROPERTY_ID, id);
    }

    public User getUpdatedBy() {
        return (User) get(Organization.PROPERTY_UPDATEDBY);
    }

    public void setUpdatedBy(User updatedby) {
        set(Organization.PROPERTY_UPDATEDBY, updatedby);
    }

    public Date getUpdated() {
        return (Date) get(Organization.PROPERTY_UPDATED);
    }

    public void setUpdated(Date updated) {
        set(Organization.PROPERTY_UPDATED, updated);
    }

    public User getCreatedBy() {
        return (User) get(Organization.PROPERTY_CREATEDBY);
    }

    public void setCreatedBy(User createdby) {
        set(Organization.PROPERTY_CREATEDBY, createdby);
    }

    public Date getCreated() {
        return (Date) get(Organization.PROPERTY_CREATED);
    }

    public void setCreated(Date created) {
        set(Organization.PROPERTY_CREATED, created);
    }

    @Override
    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;

        // set the default values
        final Entity e = getEntity();
        for (Property p : e.getProperties()) {
            // only do primitive default values
            if (!p.isPrimitive()) {
                continue;
            }
            final Object defaultValue = p.getActualDefaultValue();
            if (defaultValue != null) {
                setValue(p.getName(), defaultValue);
            }
        }
    }

    @Override
    public String getIdentifier() {
        return IdentifierProvider.getInstance().getIdentifier(this);
    }

    public Client getClient() {
        return (Client) get(PROPERTY_CLIENT);
    }

    public void setClient(Client client) {
        set(PROPERTY_CLIENT, client);
    }

    public Organization getOrganization() {
        return (Organization) get(PROPERTY_ORGANIZATION);
    }

    public void setOrganization(Organization organisation) {
        set(PROPERTY_ORGANIZATION, organisation);
    }
}