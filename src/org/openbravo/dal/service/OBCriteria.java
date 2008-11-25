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
import org.hibernate.CacheMode;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projection;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.ResultTransformer;
import org.openbravo.base.model.Entity;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.dal.core.OBContext;

/**
 * Uses a filter object to create filtered queries, supports paging and count.
 * 
 * @author mtaal
 */

public class OBCriteria<E extends BaseOBObject> implements Criteria {
    private static final Logger log = Logger.getLogger(OBCriteria.class);

    private Criteria criteria;
    private Entity entity;

    private boolean filterOnReadableClients = true;
    private boolean filterOnReadableOrganisation = true;
    private boolean filterOnActive = true;
    private List<OrderBy> orderBys = new ArrayList<OrderBy>();

    // package visible
    OBCriteria() {
    }

    @SuppressWarnings("unchecked")
    public List<E> list() throws HibernateException {
        initialize();
        return criteria.list();
    }

    public int count() {
        initialize();
        final Criteria c = getCriteria();
        c.setProjection(Projections.rowCount());
        log.debug("Counting using criteria " + c.toString());
        return ((Number) c.uniqueResult()).intValue();
    }

    public ScrollableResults scroll() throws HibernateException {
        initialize();
        return criteria.scroll();
    }

    public ScrollableResults scroll(ScrollMode scrollMode)
            throws HibernateException {
        initialize();
        return criteria.scroll(scrollMode);
    }

    public Object uniqueResult() throws HibernateException {
        initialize();
        return criteria.uniqueResult();
    }

    void initialize() {
        final OBContext obContext = OBContext.getOBContext();
        final Criteria c = getCriteria();
        final Entity e = getEntity();

        OBContext.getOBContext().getEntityAccessChecker().checkReadable(e);

        if (isFilterOnReadableOrganisation() && e.isOrganisationEnabled()) {
            getCriteria().add(
                    Restrictions.in(PROPERTY_ORGANIZATION + ".id", obContext
                            .getReadableOrganisations()));
        }

        if (isFilterOnReadableClients() && getEntity().isClientEnabled()) {
            c.add(Restrictions.in(PROPERTY_CLIENT + ".id", obContext
                    .getReadableClients()));
        }

        if (isFilterOnActive() && e.isActiveEnabled()) {
            c.add(Restrictions.eq(PROPERTY_ISACTIVE, true));
        }

        // add the order by and create a join if necessary
        for (final OrderBy ob : getOrderBys()) {
            final int j = 0;
            String orderOn = ob.getOrderOn();
            if (orderOn.indexOf(".") != -1) {
                final String orderJoin = orderOn.substring(0, orderOn
                        .lastIndexOf("."));
                final String alias = "order_ob_" + j;
                c.createCriteria(orderJoin, alias,
                        CriteriaSpecification.LEFT_JOIN);
                orderOn = alias + "."
                        + orderOn.substring(orderOn.lastIndexOf(".") + 1);
            }

            if (ob.isAscending()) {
                c.addOrder(Order.asc(orderOn));
            } else {
                c.addOrder(Order.desc(orderOn));
            }
        }
    }

    public void addOrderBy(String orderOn, boolean ascending) {
        orderBys.add(new OrderBy(orderOn, ascending));
    }

    public Criteria getCriteria() {
        return criteria;
    }

    void setCriteria(Criteria criteria) {
        this.criteria = criteria;
    }

    public Entity getEntity() {
        return entity;
    }

    void setEntity(Entity entity) {
        this.entity = entity;
    }

    public boolean isFilterOnReadableOrganisation() {
        return filterOnReadableOrganisation;
    }

    public void setFilterOnReadableOrganisation(
            boolean filterOnReadableOrganisation) {
        this.filterOnReadableOrganisation = filterOnReadableOrganisation;
    }

    public boolean isFilterOnActive() {
        return filterOnActive;
    }

    public void setFilterOnActive(boolean filterOnActive) {
        this.filterOnActive = filterOnActive;
    }

    public List<OrderBy> getOrderBys() {
        return orderBys;
    }

    public void setOrderBys(List<OrderBy> orderBys) {
        this.orderBys = orderBys;
    }

    // OrderBy to support multiple orderby clauses
    public static class OrderBy {
        private final String orderOn;
        private final boolean ascending;

        public OrderBy(String orderOn, boolean ascending) {
            this.orderOn = orderOn;
            this.ascending = ascending;
        }

        public String getOrderOn() {
            return orderOn;
        }

        public boolean isAscending() {
            return ascending;
        }

        @Override
        public String toString() {
            return getOrderOn() + (isAscending() ? " asc " : " desc ");
        }
    }

    public Criteria add(Criterion criterion) {
        return criteria.add(criterion);
    }

    public Criteria addOrder(Order order) {
        return criteria.addOrder(order);
    }

    public Criteria createAlias(String associationPath, String alias,
            int joinType) throws HibernateException {
        return criteria.createAlias(associationPath, alias, joinType);
    }

    public Criteria createAlias(String associationPath, String alias)
            throws HibernateException {
        return criteria.createAlias(associationPath, alias);
    }

    public Criteria createCriteria(String associationPath, int joinType)
            throws HibernateException {
        return criteria.createCriteria(associationPath, joinType);
    }

    public Criteria createCriteria(String associationPath, String alias,
            int joinType) throws HibernateException {
        return criteria.createCriteria(associationPath, alias, joinType);
    }

    public Criteria createCriteria(String associationPath, String alias)
            throws HibernateException {
        return criteria.createCriteria(associationPath, alias);
    }

    public Criteria createCriteria(String associationPath)
            throws HibernateException {
        return criteria.createCriteria(associationPath);
    }

    public String getAlias() {
        return criteria.getAlias();
    }

    public Criteria setCacheable(boolean cacheable) {
        return criteria.setCacheable(cacheable);
    }

    public Criteria setCacheMode(CacheMode cacheMode) {
        return criteria.setCacheMode(cacheMode);
    }

    public Criteria setCacheRegion(String cacheRegion) {
        return criteria.setCacheRegion(cacheRegion);
    }

    public Criteria setComment(String comment) {
        return criteria.setComment(comment);
    }

    public Criteria setFetchMode(String associationPath, FetchMode mode)
            throws HibernateException {
        return criteria.setFetchMode(associationPath, mode);
    }

    public Criteria setFetchSize(int fetchSize) {
        return criteria.setFetchSize(fetchSize);
    }

    public Criteria setFirstResult(int firstResult) {
        return criteria.setFirstResult(firstResult);
    }

    public Criteria setFlushMode(FlushMode flushMode) {
        return criteria.setFlushMode(flushMode);
    }

    public Criteria setLockMode(LockMode lockMode) {
        return criteria.setLockMode(lockMode);
    }

    public Criteria setLockMode(String alias, LockMode lockMode) {
        return criteria.setLockMode(alias, lockMode);
    }

    public Criteria setMaxResults(int maxResults) {
        return criteria.setMaxResults(maxResults);
    }

    public Criteria setProjection(Projection projection) {
        return criteria.setProjection(projection);
    }

    public Criteria setResultTransformer(ResultTransformer resultTransformer) {
        return criteria.setResultTransformer(resultTransformer);
    }

    public Criteria setTimeout(int timeout) {
        return criteria.setTimeout(timeout);
    }

    public boolean isFilterOnReadableClients() {
        return filterOnReadableClients;
    }

    public void setFilterOnReadableClients(boolean filterOnReadableClients) {
        this.filterOnReadableClients = filterOnReadableClients;
    }
}