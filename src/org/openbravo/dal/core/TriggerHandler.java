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

package org.openbravo.dal.core;

import org.apache.log4j.Logger;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.util.Check;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.core.ADSessionStatus;

/**
 * Supports disabling and again enabling of database triggers.
 * 
 * The user of this class should call disable() at the beginning of the
 * transaction and enable at the end, before committing.
 * 
 * @author martintaal
 */

public class TriggerHandler {
    private static final Logger log = Logger.getLogger(TriggerHandler.class);

    private static TriggerHandler instance;

    public static TriggerHandler getInstance() {
        if (instance == null) {
            instance = OBProvider.getInstance().get(TriggerHandler.class);
        }
        return instance;
    }

    private ThreadLocal<ADSessionStatus> sessionStatus = new ThreadLocal<ADSessionStatus>();

    /**
     * Creates a ADSessionStatus and stores it in the AD_SESSION_STATUS table.
     * This method will also call flush.
     */
    public void disable() {
        log.debug("Disabling triggers");
        Check.isNull(sessionStatus.get(),
                "There is already a ADSessionStatus present in this thread, "
                        + "call enable before calling disable again");
        try {
            OBContext.getOBContext().setInAdministratorMode(true);
            final ADSessionStatus localSessionStatus = OBProvider.getInstance()
                    .get(ADSessionStatus.class);
            localSessionStatus.setImporting(true);
            localSessionStatus.setClient(OBDal.getInstance().get(Client.class,
                    "0"));
            localSessionStatus.setOrganization(OBDal.getInstance().get(
                    Organization.class, "0"));
            OBDal.getInstance().save(localSessionStatus);
            OBDal.getInstance().flush();
            Check.isNotNull(localSessionStatus.getId(),
                    "The id is not set after insert");
            sessionStatus.set(localSessionStatus);
        } finally {
            OBContext.getOBContext().restorePreviousAdminMode();
        }
    }

    /** Returns true if the SessionStatus is present */
    public boolean isDisabled() {
        return sessionStatus.get() != null;
    }

    /** Removes the ADSessionStatus from the database. This enables triggers. */
    public void enable() {
        log.debug("Enabling triggers");
        Check.isNotNull(sessionStatus.get(),
                "SessionStatus not set, call disable "
                        + "before calling this method");
        try {
            OBContext.getOBContext().setInAdministratorMode(true);
            OBDal.getInstance().remove(sessionStatus.get());
            OBDal.getInstance().flush();
        } finally {
            sessionStatus.set(null);
            OBContext.getOBContext().restorePreviousAdminMode();
        }
    }
}