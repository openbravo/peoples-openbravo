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

import static org.openbravo.model.ad.system.Client.PROPERTY_ORGANIZATION;
import static org.openbravo.model.common.enterprise.Organization.PROPERTY_CLIENT;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.criterion.Expression;
import org.openbravo.base.model.AccessLevel;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.base.model.UniqueConstraint;
import org.openbravo.base.provider.OBNotSingleton;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.base.util.Check;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.security.OrganisationStructureProvider;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.datamodel.Table;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.ad.utility.ReferenceDataStore;
import org.openbravo.model.common.enterprise.Organization;

/**
 * The entity resolver will get an identifier and entityname and check if this
 * instance exists in the database using the mapping table (ref_data_loaded).
 * 
 * @author mtaal
 */

public class EntityResolver implements OBNotSingleton {

    public enum ResolvingMode {
        ALLOW_NOT_EXIST, MUSTEXIST
    }

    public static EntityResolver getInstance() {
        return OBProvider.getInstance().get(EntityResolver.class);
    }

    // keeps track of the mapping from id's to objects
    private Map<Object, BaseOBObject> data = new HashMap<Object, BaseOBObject>();
    private Map<BaseOBObject, String> objectOriginalIdMapping = new HashMap<BaseOBObject, String>();
    private Client clientZero;
    private Organization organizationZero;
    private String[] zeroOrgTree = new String[] { "0" };
    private Client client;
    private Organization organization;
    private String[] orgNaturalTree;
    private String[] orgIdTree;
    private ResolvingMode resolvingMode = ResolvingMode.ALLOW_NOT_EXIST;

    private OrganisationStructureProvider organisationStructureProvider;

    private boolean optionCreateReferencedIfNotFound = true;

    public void clear() {
        data.clear();
        objectOriginalIdMapping.clear();
    }

    // searches for a previous entity with the same id or an id retrieved from
    // the ad_ref_data_loaded table. The resolving takes into account different
    // access levels and
    public BaseOBObject resolve(String entityName, String id, boolean referenced) {

        Check.isNotNull(client, "Client should not be null");
        Check.isNotNull(organization, "Org should not be null");

        final Entity entity = ModelProvider.getInstance().getEntity(entityName);

        BaseOBObject result = null;
        // note id can be null if someone did not care to add it in a manual
        // xml file
        if (id != null) {
            result = data.get(entityName + id);
            if (result != null) {
                return result;
            }

            result = searchInstance(entity, id);
        }

        if (result != null) {
            // found, cache it for future use
            data.put(entityName + id, result);
        } else {
            if (referenced && !isOptionCreateReferencedIfNotFound()) {
                throw new EntityNotFoundException("Entity " + entityName
                        + " with id " + id + " not found");
            }
            if (resolvingMode == ResolvingMode.MUSTEXIST) {
                throw new EntityNotFoundException("Entity " + entityName
                        + " with id " + id + " not found");
            }

            // not found create a new one
            result = (BaseOBObject) OBProvider.getInstance().get(entityName);

            if (id != null) {
                // keep the relation so that ad_ref_data_loaded can be filled
                // later
                objectOriginalIdMapping.put(result, id);

                // check if we can keep the id for this one
                if (!OBDal.getInstance().exists(entityName, id)) {
                    result.setId(id);
                }
                // force new
                result.setNewOBObject(true);

                // keep it here so it can be found later
                data.put(entityName + id, result);
            }

            // TODO: add warning if the entity is created in a different
            // client/organisation than the inputted ones
            // Set the client and organization on the most detailed level
            // looking at the accesslevel of the entity
            Client setClient;
            Organization setOrg;
            if (entity.getAccessLevel() == AccessLevel.SYSTEM) {
                setClient = clientZero;
                setOrg = organizationZero;
            } else if (entity.getAccessLevel() == AccessLevel.SYSTEM_CLIENT) {
                setClient = client;
                setOrg = organizationZero;
            } else if (entity.getAccessLevel() == AccessLevel.CLIENT) {
                setClient = client;
                setOrg = organizationZero;
            } else if (entity.getAccessLevel() == AccessLevel.CLIENT_ORGANIZATION) {
                setClient = client;
                setOrg = organization;
            } else if (entity.getAccessLevel() == AccessLevel.ORGANIZATION) {
                // TODO: is this correct? That it is the same as the previous
                // one?
                setClient = client;
                setOrg = organization;
            } else if (entity.getAccessLevel() == AccessLevel.ALL) {
                setClient = client;
                setOrg = organization;
            } else {
                throw new EntityXMLException("Access level "
                        + entity.getAccessLevel() + " not supported");
            }
            if (entity.isClientEnabled()) {
                result.setValue(PROPERTY_CLIENT, setClient);
            }
            if (entity.isOrganisationEnabled()) {
                result.setValue(PROPERTY_ORGANIZATION, setOrg);
            }
        }
        return result;
    }

    // search on the basis of the access level of the entity
    public BaseOBObject searchInstance(Entity entity, String id) {
        final AccessLevel al = entity.getAccessLevel();
        BaseOBObject result = null;
        if (al == AccessLevel.SYSTEM) {
            result = searchSystem(id, entity);
        } else if (al == AccessLevel.SYSTEM_CLIENT) {
            // search client and system
            result = searchClient(id, entity);
            if (result == null) {
                result = searchSystem(id, entity);
            }
        } else if (al == AccessLevel.CLIENT) {
            result = searchClient(id, entity);
        } else if (al == AccessLevel.CLIENT_ORGANIZATION) {
            // search 2 levels
            result = searchClientOrganization(id, entity);
            if (result == null) {
                result = searchClient(id, entity);
            }
        } else if (al == AccessLevel.ALL) {
            // search all three levels from the bottom
            result = searchClientOrganization(id, entity);
            if (result == null) {
                result = searchClient(id, entity);
            }
            if (result == null) {
                result = searchSystem(id, entity);
            }
        }
        return result;
    }

    public String getOriginalId(BaseOBObject bob) {
        return objectOriginalIdMapping.get(bob);
    }

    protected BaseOBObject searchSystem(String id, Entity entity) {
        // this works because it assumes that only the
        return doSearch(id, entity, "0", "0");
    }

    protected BaseOBObject searchClient(String id, Entity entity) {
        return search(id, entity, "0");
    }

    protected BaseOBObject searchClientOrganization(String id, Entity entity) {
        return search(id, entity, organization.getId());
    }

    protected BaseOBObject search(String id, Entity entity, String orgId) {
        // first check if the object was already imported in this level
        // so check if there is a new id available
        final List<String> newIds = getId(id, entity, orgId);
        if (newIds.size() > 0) {
            for (final String newId : newIds) {
                final BaseOBObject result = doSearch(newId, entity, client
                        .getId(), orgId);
                if (result != null) {
                    return result;
                }
            }
        }
        return doSearch(id, entity, client.getId(), orgId);
    }

    protected BaseOBObject doSearch(String id, Entity entity, String clientId,
            String orgId) {
        final String[] searchOrgIds = getOrgIds(orgId);
        final OBCriteria<?> obc = OBDal.getInstance().createCriteria(
                entity.getName());
        obc.setFilterOnActive(false);
        obc.setFilterOnReadableClients(false);
        obc.setFilterOnReadableOrganisation(false);
        if (entity.isClientEnabled()) {
            obc.add(Expression.eq(PROPERTY_CLIENT + "."
                    + Organization.PROPERTY_ID, clientId));
        }
        if (entity.isOrganisationEnabled()) {
            // Note the query is for other types than client but the client
            // property names
            // are good standard ones to use
            obc.add(Expression.in(PROPERTY_ORGANIZATION + "."
                    + Client.PROPERTY_ID, searchOrgIds));
        }
        // same for here
        obc.add(Expression.eq(Organization.PROPERTY_ID, id));
        final List<?> res = obc.list();
        Check.isTrue(res.size() <= 1, "More than one result when searching in "
                + entity.getName() + " with id " + id);
        if (res.size() == 1) {
            return (BaseOBObject) res.get(0);
        }
        return null;
    }

    // get the new id which was created in previous imports
    // note that there is a rare case that when an instance is removed
    // and then re-imported that it occurs multiple times.
    private List<String> getId(String id, Entity entity, String orgId) {
        final String[] searchOrgIds = getOrgIds(orgId);
        try {
            OBContext.getOBContext().setInAdministratorMode(true);
            final OBCriteria<ReferenceDataStore> rdlCriteria = OBDal
                    .getInstance().createCriteria(ReferenceDataStore.class);
            rdlCriteria.setFilterOnActive(false);
            rdlCriteria.setFilterOnReadableOrganisation(false);
            rdlCriteria.setFilterOnReadableClients(false);
            rdlCriteria.add(Expression.eq(ReferenceDataStore.PROPERTY_GENERIC,
                    id));
            rdlCriteria.add(Expression.eq(ReferenceDataStore.PROPERTY_CLIENT
                    + "." + Client.PROPERTY_ID, client.getId()));
            rdlCriteria.add(Expression.in(
                    ReferenceDataStore.PROPERTY_ORGANIZATION + "."
                            + Organization.PROPERTY_ID, searchOrgIds));
            rdlCriteria.add(Expression.eq(ReferenceDataStore.PROPERTY_TABLE
                    + "." + Table.PROPERTY_ID, entity.getTableId()));
            final List<ReferenceDataStore> rdls = rdlCriteria.list();

            final List<String> result = new ArrayList<String>();
            for (final ReferenceDataStore rdl : rdls) {
                result.add(rdl.getSpecific());
            }
            return result;
        } finally {
            OBContext.getOBContext().restorePreviousAdminMode();
        }
    }

    // determines which org ids to look, if 0 then only look zero
    // in other cases look only in the passed orgId if this is not
    // a referenced one, otherwise use the naturaltree
    private String[] getOrgIds(String orgId) {
        final String[] searchOrgIds;
        if (true) {
            if (orgId.equals("0")) {
                searchOrgIds = zeroOrgTree;
            } else {
                searchOrgIds = orgNaturalTree;
            }
        } else {
            searchOrgIds = orgIdTree;
        }
        return searchOrgIds;
    }

    protected void setClientOrganisationZero() {
        if (clientZero != null) {
            return;
        }
        clientZero = OBDal.getInstance().get(Client.class, "0");
        organizationZero = OBDal.getInstance().get(Organization.class, "0");
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        setClientOrganisationZero();
        organisationStructureProvider = OBProvider.getInstance().get(
                OrganisationStructureProvider.class);
        organisationStructureProvider.setClientId(client.getId());
        this.client = client;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        orgIdTree = new String[] { organization.getId() };
        final Set<String> orgs = organisationStructureProvider
                .getNaturalTree(organization.getId());
        orgNaturalTree = orgs.toArray(new String[orgs.size()]);
        this.organization = organization;
    }

    public boolean isOptionCreateReferencedIfNotFound() {
        return optionCreateReferencedIfNotFound;
    }

    public void setOptionCreateReferencedIfNotFound(
            boolean optionCreateReferencedIfNotFound) {
        this.optionCreateReferencedIfNotFound = optionCreateReferencedIfNotFound;
    }

    public ResolvingMode getResolvingMode() {
        return resolvingMode;
    }

    public void setResolvingMode(ResolvingMode resolvingMode) {
        this.resolvingMode = resolvingMode;
    }

    // queries the database for another object which has the same values
    // for properties which are part of a uniqueconstraint
    // if found a check is done if the object is part of the current
    // installed
    public BaseOBObject findUniqueConstrainedObject(BaseOBObject obObject) {
        // an existing object should not be able to violate his/her
        // own constraints
        if (!obObject.isNewOBObject()) {
            return null;
        }

        final Entity entity = obObject.getEntity();
        final Object id = obObject.getId();
        for (final UniqueConstraint uc : entity.getUniqueConstraints()) {
            final OBCriteria<BaseOBObject> criteria = OBDal.getInstance()
                    .createCriteria(entity.getName());
            if (id != null) {
                criteria.add(Expression.ne("id", id));
            }

            boolean ignoreUniqueConstraint = false;
            for (final Property p : uc.getProperties()) {
                final Object value = obObject.getValue(p.getName());

                // a special check, the property refers to an
                // object which is also new, presumably this object
                // is also added in the import
                // in this case the
                // uniqueconstraint can never fail
                // so move on to the next
                if (value instanceof BaseOBObject
                        && ((BaseOBObject) value).isNewOBObject()) {
                    ignoreUniqueConstraint = true;
                    break;
                }

                criteria.add(Expression.eq(p.getName(), value));
            }
            if (ignoreUniqueConstraint) {
                continue;
            }

            criteria.setFilterOnActive(false);
            criteria.setFilterOnReadableOrganisation(false);
            criteria.setFilterOnReadableClients(false);
            criteria.setMaxResults(1);

            final List<BaseOBObject> queryResult = criteria.list();
            if (queryResult.size() > 0) {

                // check if the found unique match is a valid
                // object to use
                // TODO: this can be made faster by
                // adding client/organisation filtering above in
                // the criteria
                final BaseOBObject searchResult = searchInstance(entity,
                        (String) queryResult.get(0).getId());
                if (searchResult == null) {
                    // not valid return null
                    return null;
                }
                return queryResult.get(0);
            }
        }

        return null;
    }

}