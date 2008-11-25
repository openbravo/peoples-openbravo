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

package org.openbravo.dal.service;

import static org.openbravo.model.ad.system.Client.PROPERTY_ORGANIZATION;
import static org.openbravo.model.common.enterprise.Organization.PROPERTY_CLIENT;
import static org.openbravo.model.common.enterprise.Organization.PROPERTY_ISACTIVE;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.openbravo.base.model.Entity;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.base.util.Check;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.core.SessionHandler;

/**
 * Dal entry point which allows free-format where clauses to do queries.
 * 
 * @author mtaal
 */

public class OBQuery<E extends BaseOBObject> {
    private static final Logger log = Logger.getLogger(OBQuery.class);

    private String whereAndOrderBy;
    private Entity entity;
    private List<Object> parameters;
    private boolean filterOnAccessibleOrganisation = true;
    private boolean filterOnAccessibleClients = true;
    private boolean filterOnActive = true;

    // package visible
    OBQuery() {
    }

    @SuppressWarnings("unchecked")
    public List<E> list() throws HibernateException {
        return createQuery().list();
    }

    public int count() {
        final Query qry = getSession().createQuery(
                "select count(*) " + stripOrderBy(createQueryString()));
        setParameters(qry);
        return ((Number) qry.uniqueResult()).intValue();
    }

    private String stripOrderBy(String qryStr) {
        if (qryStr.toLowerCase().indexOf("order by") != -1) {
            return qryStr
                    .substring(0, qryStr.toLowerCase().indexOf("order by"));
        }
        return qryStr;
    }

    public Query createQuery() {
        final Query qry = getSession().createQuery(createQueryString());
        setParameters(qry);
        return qry;
    }

    String createQueryString() {
        final OBContext obContext = OBContext.getOBContext();
        final Entity e = getEntity();

        // split the orderby and where
        final String qryStr = getWhereAndOrderBy();
        final String orderByClause;
        String whereClause;
        final int orderByIndex = qryStr.toLowerCase().indexOf("order by");
        if (orderByIndex != -1) {
            whereClause = qryStr.substring(0, orderByIndex);
            orderByClause = qryStr.substring(orderByIndex);
        } else {
            whereClause = qryStr;
            orderByClause = "";
        }

        // strip the where, is added later
        if (whereClause.trim().toLowerCase().startsWith("where")) {
            final int whereIndex = whereClause.toLowerCase().indexOf("where");
            if (whereIndex != -1) {
                whereClause = whereClause.substring(1 + whereIndex
                        + "where".length());
            }
        }

        // the query can start with an alias to support joins
        // 
        String alias = null;
        // this is a space on purpose
        String prefix = " ";
        if (whereClause.toLowerCase().trim().startsWith("as")) {
            // strip the as
            final String strippedWhereClause = whereClause.toLowerCase().trim()
                    .substring(2).trim();
            // get the next space
            final int index = strippedWhereClause.indexOf(" ");
            alias = strippedWhereClause.substring(0, index);
            prefix = alias + ".";
        }

        // is used because the clauses which are added should all be and-ed
        // special cases which need to be handled:
        // left join a left join b where a.id is not null or b.id is not null
        // id='0' and exists (from ADModelObject as mo where mo.id=id)
        // id='0'
        boolean addWhereClause = true;
        if (whereClause.trim().length() > 0) {
            if (!whereClause.toLowerCase().contains("where")) {
                // simple case: id='0's
                whereClause = " where (" + whereClause + ")";
                addWhereClause = false;
            } else {
                // check if the where is before
                final int fromIndex = whereClause.toLowerCase().indexOf("from");
                int whereIndex = -1;
                if (fromIndex == -1) {
                    // already there and no from
                    // now find the place where to put the brackets
                    // case: left join a left join b where a.id is not null or
                    // b.id is not null

                    whereIndex = whereClause.toLowerCase().indexOf("where");
                    Check.isTrue(whereIndex != -1,
                            "Where not found in string: " + whereClause);
                } else {
                    // example: id='0' and exists (from ADModelObject as mo
                    // where mo.id=id)
                    // example: left join x where id='0' and x.id=id and exists
                    // (from ADModelObject as mo where mo.id=id)

                    // check if the whereClause is before the first from
                    whereIndex = whereClause.toLowerCase().substring(0,
                            fromIndex).indexOf("where");
                }

                if (whereIndex != -1) {
                    // example: left join x where id='0' and x.id=id and exists
                    // (from ADModelObject as mo where mo.id=id)
                    addWhereClause = false;
                    // now put the ( at the correct place
                    final int endOfWhere = whereIndex + "where".length();
                    whereClause = whereClause.substring(0, endOfWhere) + " ("
                            + whereClause.substring(endOfWhere) + ")";
                } else { // no whereclause before the from
                    // example: id='0' and exists (from ADModelObject as mo
                    // where mo.id=id)
                    whereClause = " where (" + whereClause + ")";
                    addWhereClause = false;
                }
            }
        }
        OBContext.getOBContext().getEntityAccessChecker().checkReadable(e);

        if (isFilterOnAccessibleOrganisation() && e.isOrganisationEnabled()) {
            whereClause = (addWhereClause ? " where " : "")
                    + addAnd(whereClause) + prefix + PROPERTY_ORGANIZATION
                    + ".id "
                    + createInClause(obContext.getReadableOrganisations());
            if (addWhereClause) {
                addWhereClause = false;
            }
        }

        if (isFilterOnAccessibleClients() && getEntity().isClientEnabled()) {
            whereClause = (addWhereClause ? " where " : "")
                    + addAnd(whereClause) + prefix + PROPERTY_CLIENT + ".id "
                    + createInClause(obContext.getReadableClients());
            if (addWhereClause) {
                addWhereClause = false;
            }
        }

        if (isFilterOnActive() && e.isActiveEnabled()) {
            whereClause = (addWhereClause ? " where " : "")
                    + addAnd(whereClause) + prefix + PROPERTY_ISACTIVE
                    + "='Y' ";
            if (addWhereClause) {
                addWhereClause = false;
            }
        }

        // now determine the join
        // final StringBuilder join = new StringBuilder();
        // if (orderByClause.length() > 0) {
        // // strip the order by
        // final int orderBy = orderByClause.toLowerCase().indexOf("order by");
        // final String clauses = orderByClause.substring(1 + orderBy
        // + "order by".length());
        // for (String part : clauses.split(",")) {
        // part = part.trim();
        // // now just get the dotted part, only support one for now
        // int firstIndexOf = part.indexOf(".");
        // if (firstIndexOf != -1) {
        // // get the second one
        // int secondIndexOf = part.indexOf(".", firstIndexOf + 1);
        // if (secondIndexOf != -1) {
        // join.append(" left join e."
        // + part.substring(1 + firstIndexOf,
        // secondIndexOf));
        // }
        // }
        // }
        // join.append(" ");
        // }

        final String result;
        if (alias != null) {
            result = "select " + alias + " from " + getEntity().getName() + " "
                    + whereClause + orderByClause;
        } else {
            result = "from " + getEntity().getName() + " " + whereClause
                    + orderByClause;
        }
        log.debug("Created query string " + result);
        return result;
    }

    private String addAnd(String whereClause) {
        if (whereClause.trim().length() > 0) {
            return whereClause + " and ";
        }
        return whereClause;
    }

    private String createInClause(String[] values) {
        if (values.length == 0) {
            return " in ('') ";
        }
        final StringBuilder sb = new StringBuilder();
        for (final String v : values) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append("'" + v + "'");
        }
        return " in (" + sb.toString() + ")";
    }

    public Entity getEntity() {
        return entity;
    }

    void setEntity(Entity entity) {
        this.entity = entity;
    }

    private void setParameters(Query qry) {
        int pos = 0;
        for (final Object param : getParameters()) {
            if (param instanceof BaseOBObject) {
                qry.setEntity(pos++, param);
            } else {
                qry.setParameter(pos++, param);
            }
        }
    }

    public boolean isFilterOnAccessibleOrganisation() {
        return filterOnAccessibleOrganisation;
    }

    public void setFilterOnAccessibleOrganisation(
            boolean filterOnAccessibleOrganisation) {
        this.filterOnAccessibleOrganisation = filterOnAccessibleOrganisation;
    }

    public boolean isFilterOnActive() {
        return filterOnActive;
    }

    public void setFilterOnActive(boolean filterOnActive) {
        this.filterOnActive = filterOnActive;
    }

    public String getWhereAndOrderBy() {
        return whereAndOrderBy;
    }

    public void setWhereAndOrderBy(String queryString) {
        if (queryString == null) {
            this.whereAndOrderBy = "";
        } else {
            this.whereAndOrderBy = queryString;
        }
    }

    private Session getSession() {
        return SessionHandler.getInstance().getSession();
    }

    public List<Object> getParameters() {
        return parameters;
    }

    public void setParameters(List<Object> parameters) {
        if (parameters == null) {
            this.parameters = new ArrayList<Object>();
        } else {
            this.parameters = parameters;
        }
    }

    public boolean isFilterOnAccessibleClients() {
        return filterOnAccessibleClients;
    }

    public void setFilterOnAccessibleClients(boolean filterOnAccessibleClients) {
        this.filterOnAccessibleClients = filterOnAccessibleClients;
    }
}