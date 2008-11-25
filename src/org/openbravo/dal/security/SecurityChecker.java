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

package org.openbravo.dal.security;

import org.openbravo.base.exception.OBSecurityException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.provider.OBSingleton;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.base.structure.ClientEnabled;
import org.openbravo.base.structure.OrganizationEnabled;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.core.SessionHandler;

/**
 * Centralizes checking of security for data objects.
 * 
 * @author mtaal
 */

public class SecurityChecker implements OBSingleton {

    private static SecurityChecker instance;

    public static SecurityChecker getInstance() {
        if (instance == null) {
            instance = OBProvider.getInstance().get(SecurityChecker.class);
        }
        return instance;
    }

    public void checkDeleteAllowed(Object o) {
        if (!OBContext.getOBContext().isInAdministratorMode()
                && o instanceof BaseOBObject) {
            final BaseOBObject bob = (BaseOBObject) o;
            final Entity entity = ModelProvider.getInstance().getEntity(
                    bob.getEntityName());
            if (!entity.isDeletable()) {
                throw new OBSecurityException("Entity " + entity.getName()
                        + " is not deletable");
            }
        }
        checkWriteAccess(o);
    }

    // NOTE: this method needs to be kept insync with the checkWritable method
    public boolean isWritable(Object o) {

        // check that the client id and organisation id are resp. in the list of
        // user_client and user_org
        // TODO: throw specific and translated exception, for more info:
        // Utility.translateError(this, vars, vars.getLanguage(),
        // Utility.messageBD(this, "NoWriteAccess", vars.getLanguage()))

        final OBContext obContext = OBContext.getOBContext();

        String clientId = "";
        if (o instanceof ClientEnabled
                && ((ClientEnabled) o).getClient() != null) {
            clientId = (String) DalUtil.getId(((ClientEnabled) o).getClient());
        }
        String orgId = "";
        if (o instanceof OrganizationEnabled
                && ((OrganizationEnabled) o).getOrganization() != null) {
            orgId = (String) DalUtil.getId(((OrganizationEnabled) o)
                    .getOrganization());
        }

        final Entity entity = ((BaseOBObject) o).getEntity();
        if (!obContext.isInAdministratorMode() && clientId.length() > 0) {
            if (o instanceof ClientEnabled) {
                if (!obContext.getCurrentClient().getId().equals(clientId)) {
                    return false;
                }
            }

            // todo can be improved by only checking if the client or
            // organisation
            // actually changed...
            if (!obContext.getEntityAccessChecker().isWritable(entity)) {
                return false;
            }

            if (o instanceof OrganizationEnabled && orgId.length() > 0) {
                if (!obContext.getWritableOrganisations().contains(orgId)) {
                    return false;
                }
            }
        }

        // accesslevel check must also be done for administrators
        try {
            entity.checkAccessLevel(clientId, orgId);
        } catch (final OBSecurityException e) {
            return false;
        }
        return true;
    }

    // NOTE: this method needs to be kept insync with the isWritable method
    public void checkWriteAccess(Object o) {

        // check that the client id and organisation id are resp. in the list of
        // user_client and user_org
        // TODO: throw specific and translated exception, for more info:
        // Utility.translateError(this, vars, vars.getLanguage(),
        // Utility.messageBD(this, "NoWriteAccess", vars.getLanguage()))
        final OBContext obContext = OBContext.getOBContext();

        String clientId = "";
        if (o instanceof ClientEnabled
                && ((ClientEnabled) o).getClient() != null) {
            clientId = (String) DalUtil.getId(((ClientEnabled) o).getClient());
        }
        String orgId = "";
        if (o instanceof OrganizationEnabled
                && ((OrganizationEnabled) o).getOrganization() != null) {
            orgId = (String) DalUtil.getId(((OrganizationEnabled) o)
                    .getOrganization());
        }

        final Entity entity = ((BaseOBObject) o).getEntity();
        if (!obContext.isInAdministratorMode() && clientId.length() > 0) {
            if (o instanceof ClientEnabled) {
                if (!obContext.getCurrentClient().getId().equals(clientId)) {
                    // TODO: maybe move rollback to exception throwing
                    SessionHandler.getInstance().setDoRollback(true);
                    throw new OBSecurityException("Client (" + clientId
                            + ") of object (" + o
                            + ") is not present  in ClientList "
                            + obContext.getCurrentClient().getId());
                }
            }

            // todo can be improved by only checking if the client or
            // organisation
            // actually changed...
            obContext.getEntityAccessChecker().checkWritable(entity);

            if (o instanceof OrganizationEnabled && orgId.length() > 0) {
                // todo as only the id is required this can be made much more
                // efficient
                // by
                // not loading the hibernate proxy
                if (!obContext.getWritableOrganisations().contains(orgId)) {
                    // TODO: maybe move rollback to exception throwing
                    SessionHandler.getInstance().setDoRollback(true);
                    throw new OBSecurityException("Organisation " + orgId
                            + " of object (" + o
                            + ") is not present  in OrganisationList "
                            + obContext.getWritableOrganisations());
                }
            }
        }

        // accesslevel check must also be done for administrators
        entity.checkAccessLevel(clientId, orgId);
    }
}