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

package org.openbravo.dal.security;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.Query;
import org.openbravo.base.exception.OBSecurityException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.base.provider.OBNotSingleton;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.core.SessionHandler;
import org.openbravo.model.ad.access.TableAccess;
import org.openbravo.model.ad.access.WindowAccess;
import org.openbravo.model.ad.ui.Tab;
import org.openbravo.model.ad.ui.Window;

/**
 * Uses window/tab access information to determine which entities are writable
 * and readable by a user.
 * 
 * @author mtaal
 */

public class EntityAccessChecker implements OBNotSingleton {

    private String roleId;

    private Set<Entity> writableEntities = new HashSet<Entity>();
    private Set<Entity> readableEntities = new HashSet<Entity>();
    // the derived readable entities only contains the entities which are
    // derived
    // readable
    // the completely readable entities are present in the readableEntities
    private Set<Entity> derivedReadableEntities = new HashSet<Entity>();
    private Set<Entity> nonReadableEntities = new HashSet<Entity>();
    private boolean isInitialized = false;

    private OBContext obContext;

    public void initialize() {

        final ModelProvider mp = ModelProvider.getInstance();
        // Don't use dal because otherwise we can end up in infinite loops
        final String qryStr = "select wa from " + WindowAccess.class.getName()
                + " wa where role.id='" + getRoleId() + "'";
        final Query qry = SessionHandler.getInstance().createQuery(qryStr);
        @SuppressWarnings("unchecked")
        final List<WindowAccess> was = qry.list();
        for (final WindowAccess wa : was) {
            final Window w = wa.getWindow();
            final boolean writeAccess = wa.isReadWrite();
            // get the ttabs
            final String tfQryStr = "select t from " + Tab.class.getName()
                    + " t where window.id='" + w.getId() + "'";
            @SuppressWarnings("unchecked")
            final List<Tab> ts = SessionHandler.getInstance().createQuery(
                    tfQryStr).list();
            for (final Tab t : ts) {
                final String tableName = t.getTable().getTableName();
                final Entity e = mp.getEntityByTableName(tableName);
                if (e == null) { // happens for AD_Client_Info
                    continue;
                }
                if (writeAccess) {
                    writableEntities.add(e);
                    readableEntities.add(e);
                } else {
                    readableEntities.add(e);
                }
            }
        }

        // and take into account table access
        final String tafQryStr = "select ta from "
                + TableAccess.class.getName() + " ta where role.id='"
                + getRoleId() + "'";
        @SuppressWarnings("unchecked")
        final List<TableAccess> tas = SessionHandler.getInstance().createQuery(
                tafQryStr).list();
        for (final TableAccess ta : tas) {
            final String tableName = ta.getTable().getName();
            final Entity e = mp.getEntityByTableName(tableName);

            if (ta.isExclude()) {
                readableEntities.remove(e);
                writableEntities.remove(e);
                nonReadableEntities.add(e);
            } else if (ta.isReadOnly()) {
                writableEntities.remove(e);
            }
        }

        // and compute the derived readable
        for (final Entity e : readableEntities) {
            for (final Property p : e.getProperties()) {
                if (p.getTargetEntity() != null
                        && !readableEntities.contains(p.getTargetEntity())) {
                    derivedReadableEntities.add(p.getTargetEntity());
                    addDerivedReadableIdentifierProperties(p.getTargetEntity());
                }
            }
        }

        isInitialized = true;
    }

    public void dump() {
        System.err.println("");
        System.err.println(">>> Readabled entities: ");
        System.err.println("");
        dumpSorted(readableEntities);

        System.err.println("");
        System.err.println(">>> Derived Readabled entities: ");
        System.err.println("");
        dumpSorted(derivedReadableEntities);

        System.err.println("");
        System.err.println(">>> Writable entities: ");
        System.err.println("");
        dumpSorted(writableEntities);
        System.err.println("");
        System.err.println("");
    }

    private void dumpSorted(Set<Entity> set) {
        final List<String> names = new ArrayList<String>();
        for (final Entity e : set) {
            names.add(e.getName());
        }
        Collections.sort(names);
        for (final String n : names) {
            System.err.println(n);
        }
    }

    // a special case whereby an identifier property is again a reference to
    // another entity, then this other entity is also derived readable, etc.
    private void addDerivedReadableIdentifierProperties(Entity entity) {
        for (final Property p : entity.getProperties()) {
            if (p.isIdentifier() && p.getTargetEntity() != null
                    && !readableEntities.contains(p.getTargetEntity())) {
                derivedReadableEntities.add(p.getTargetEntity());
                addDerivedReadableIdentifierProperties(p.getTargetEntity());
            }
        }
    }

    public boolean isDerivedReadable(Entity e) {
        // prevent infinite looping
        if (!isInitialized) {
            return false;
        }

        // false is the allow read reply
        if (obContext.isInAdministratorMode()) {
            return false;
        }
        return derivedReadableEntities.contains(e);
    }

    public boolean isWritable(Entity entity) {
        // prevent infinite looping
        if (!isInitialized) {
            return true;
        }

        if (obContext.isInAdministratorMode()) {
            return true;
        }

        if (!writableEntities.contains(entity)) {
            return false;
        }
        return true;
    }

    public void checkWritable(Entity entity) {
        if (!isWritable(entity)) {
            throw new OBSecurityException("Entity " + entity
                    + " is not writable by this user");
        }
    }

    public void checkReadable(Entity entity) {
        // prevent infinite looping
        if (!isInitialized) {
            return;
        }

        if (obContext.isInAdministratorMode()) {
            return;
        }

        if (nonReadableEntities.contains(entity)) {
            throw new OBSecurityException("Entity " + entity
                    + " is not readable by this user");
        }

        if (derivedReadableEntities.contains(entity)) {
            return;
        }

        if (!readableEntities.contains(entity)) {
            throw new OBSecurityException("Entity " + entity
                    + " is not readable by this user");
        }
    }

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public OBContext getObContext() {
        return obContext;
    }

    public void setObContext(OBContext obContext) {
        this.obContext = obContext;
    }

}