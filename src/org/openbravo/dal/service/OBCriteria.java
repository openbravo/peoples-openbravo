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
 * This object is an implementation of the Hibernate Criteria interface. It adds
 * transparent client and organization filtering to the Hibernate Criteria.
 * Internally the OBCriteria keeps a Hibernate Criteria object as a delegate.
 * Most calls are delegated to the Hibernate Criteria object after first setting
 * the additional filters.
 * <p/>
 * This class also offers a convenience method to set orderby, the entities
 * refered to from the order by are automatically joined in the query.
 * 
 * @see OBContext#getReadableClients()
 * @see OBContext#getReadableOrganizations()
 * 
 * @author mtaal
 */

public class OBCriteria<E extends BaseOBObject> implements Criteria {
    private static final Logger log = Logger.getLogger(OBCriteria.class);

    private Criteria criteria;
    private Entity entity;

    private boolean filterOnReadableClients = true;
    private boolean filterOnReadableOrganization = true;
    private boolean filterOnActive = true;
    private List<OrderBy> orderBys = new ArrayList<OrderBy>();

    // package visible
    OBCriteria() {
    }

    /**
     * @see Criteria#list()
     */
    @SuppressWarnings("unchecked")
    public List<E> list() throws HibernateException {
        initialize();
        return criteria.list();
    }

    /**
     * A convenience method which is not present in the standard Hibernate
     * Criteria object. The count of objects is returned.
     * 
     * @return the count of the objects using the filter set in this Criteria
     */
    public int count() {
        initialize();
        final Criteria c = getCriteria();
        c.setProjection(Projections.rowCount());
        log.debug("Counting using criteria " + c.toString());
        return ((Number) c.uniqueResult()).intValue();
    }

    /**
     * @see Criteria#scroll()
     */
    public ScrollableResults scroll() throws HibernateException {
        initialize();
        return criteria.scroll();
    }

    /**
     * @see Criteria#scroll(ScrollMode)
     */
    public ScrollableResults scroll(ScrollMode scrollMode)
            throws HibernateException {
        initialize();
        return criteria.scroll(scrollMode);
    }

    /**
     * @see Criteria#uniqueResult()
     */
    public Object uniqueResult() throws HibernateException {
        initialize();
        return criteria.uniqueResult();
    }

    void initialize() {
        final OBContext obContext = OBContext.getOBContext();
        final Criteria c = getCriteria();
        final Entity e = getEntity();

        OBContext.getOBContext().getEntityAccessChecker().checkReadable(e);

        if (isFilterOnReadableOrganization() && e.isOrganizationEnabled()) {
            getCriteria().add(
                    Restrictions.in(PROPERTY_ORGANIZATION + ".id", obContext
                            .getReadableOrganizations()));
        }

        if (isFilterOnReadableClients() && getEntity().isClientEnabled()) {
            c.add(Restrictions.in(PROPERTY_CLIENT + ".id", obContext
                    .getReadableClients()));
        }

        if (isFilterOnActive() && e.isActiveEnabled()) {
            c.add(Restrictions.eq(PROPERTY_ISACTIVE, true));
        }

        // add the order by and create a join if necessary
        for (final OrderBy ob : orderBys) {
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

    /**
     * Convenience method not present in the standard Hibernate Criteria object.
     * 
     * @param orderOn
     *            the property on which to order, can also be a property of an
     *            associated entity (etc.)
     * @param ascending
     *            if true then order ascending, false order descending
     */
    public void addOrderBy(String orderOn, boolean ascending) {
        orderBys.add(new OrderBy(orderOn, ascending));
    }

    /**
     * @return the internal Hibernate Criteria object used
     */
    public Criteria getCriteria() {
        return criteria;
    }

    void setCriteria(Criteria criteria) {
        this.criteria = criteria;
    }

    /**
     * @return the Entity for which is queried
     * @see Entity
     */
    public Entity getEntity() {
        return entity;
    }

    void setEntity(Entity entity) {
        this.entity = entity;
    }

    /**
     * @return true then when querying (for example call list()) a filter on
     *         readable organizations is added to the query, if false then this
     *         is not done
     * @see OBContext#getReadableOrganizations()
     */
    public boolean isFilterOnReadableOrganization() {
        return filterOnReadableOrganization;
    }

    /**
     * Makes it possible to control if a filter on readable organizations should
     * be added to the Criteria automatically. The default is true.
     * 
     * @param filterOnReadableOrganization
     *            if true then when querying (for example call list()) a filter
     *            on readable organizations is added to the query, if false then
     *            this is not done
     * @see OBContext#getReadableOrganizations()
     */
    public void setFilterOnReadableOrganization(
            boolean filterOnReadableOrganization) {
        this.filterOnReadableOrganization = filterOnReadableOrganization;
    }

    /**
     * Filter the results on the active property. Default is true. If set then
     * only objects with isActive true are returned by the Criteria object.
     * 
     * @return true if objects are filtered on isActive='Y', false otherwise
     */
    public boolean isFilterOnActive() {
        return filterOnActive;
    }

    /**
     * Filter the results on the active property. Default is true. If set then
     * only objects with isActive true are returned by the Criteria object.
     * 
     * @param filterOnActive
     *            if true then only objects with isActive='Y' are returned,
     *            false otherwise
     */
    public void setFilterOnActive(boolean filterOnActive) {
        this.filterOnActive = filterOnActive;
    }

    /**
     * Filter the results on readable clients (@see
     * OBContext#getReadableClients()). The default is true.
     * 
     * @return if true then only objects from readable clients are returned, if
     *         false then objects from all clients are returned
     */
    public boolean isFilterOnReadableClients() {
        return filterOnReadableClients;
    }

    /**
     * Filter the results on readable clients (@see
     * OBContext#getReadableClients()). The default is true.
     * 
     * @param filterOnReadableClients
     *            if true then only objects from readable clients are returned,
     *            if false then objects from all clients are returned
     */
    public void setFilterOnReadableClients(boolean filterOnReadableClients) {
        this.filterOnReadableClients = filterOnReadableClients;
    }

    // OrderBy to support multiple orderby clauses
    static class OrderBy {
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

    // +++++++++++++++ Methods coming from the Criteria interface +++++++++++++

    /**
     * @see Criteria#add(Criterion)
     */
    public Criteria add(Criterion criterion) {
        return criteria.add(criterion);
    }

    /**
     * @see Criteria#addOrder(Order)
     */
    public Criteria addOrder(Order order) {
        return criteria.addOrder(order);
    }

    /**
     * @see Criteria#createAlias(String, String, int)
     */
    public Criteria createAlias(String associationPath, String alias,
            int joinType) throws HibernateException {
        return criteria.createAlias(associationPath, alias, joinType);
    }

    /**
     * @see Criteria#createAlias(String, String)
     */
    public Criteria createAlias(String associationPath, String alias)
            throws HibernateException {
        return criteria.createAlias(associationPath, alias);
    }

    /**
     * @see Criteria#createCriteria(String, int)
     */
    public Criteria createCriteria(String associationPath, int joinType)
            throws HibernateException {
        return criteria.createCriteria(associationPath, joinType);
    }

    /**
     * @see Criteria#createCriteria(String, String, int)
     */
    public Criteria createCriteria(String associationPath, String alias,
            int joinType) throws HibernateException {
        return criteria.createCriteria(associationPath, alias, joinType);
    }

    /**
     * @see Criteria#createCriteria(String, String)
     */
    public Criteria createCriteria(String associationPath, String alias)
            throws HibernateException {
        return criteria.createCriteria(associationPath, alias);
    }

    /**
     * @see Criteria#createCriteria(String)
     */
    public Criteria createCriteria(String associationPath)
            throws HibernateException {
        return criteria.createCriteria(associationPath);
    }

    /**
     * @see Criteria#getAlias()
     */
    public String getAlias() {
        return criteria.getAlias();
    }

    /**
     * @see Criteria#setCacheable(boolean)
     */
    public Criteria setCacheable(boolean cacheable) {
        return criteria.setCacheable(cacheable);
    }

    /**
     * @see Criteria#setCacheMode(CacheMode)
     */
    public Criteria setCacheMode(CacheMode cacheMode) {
        return criteria.setCacheMode(cacheMode);
    }

    /**
     * @see Criteria#setCacheRegion(String)
     */
    public Criteria setCacheRegion(String cacheRegion) {
        return criteria.setCacheRegion(cacheRegion);
    }

    /**
     * @see Criteria#setComment(String)
     */
    public Criteria setComment(String comment) {
        return criteria.setComment(comment);
    }

    /**
     * @see Criteria#setFetchMode(String, FetchMode)
     */
    public Criteria setFetchMode(String associationPath, FetchMode mode)
            throws HibernateException {
        return criteria.setFetchMode(associationPath, mode);
    }

    /**
     * @see Criteria#setFetchSize(int)
     */
    public Criteria setFetchSize(int fetchSize) {
        return criteria.setFetchSize(fetchSize);
    }

    /**
     * @see Criteria#setFirstResult(int)
     */
    public Criteria setFirstResult(int firstResult) {
        return criteria.setFirstResult(firstResult);
    }

    /**
     * @see Criteria#setFlushMode(FlushMode)
     */
    public Criteria setFlushMode(FlushMode flushMode) {
        return criteria.setFlushMode(flushMode);
    }

    /**
     * @see Criteria#setLockMode(LockMode)
     */
    public Criteria setLockMode(LockMode lockMode) {
        return criteria.setLockMode(lockMode);
    }

    /**
     * @see Criteria#setLockMode(String, LockMode)
     */
    public Criteria setLockMode(String alias, LockMode lockMode) {
        return criteria.setLockMode(alias, lockMode);
    }

    /**
     * @see Criteria#setMaxResults(int)
     */
    public Criteria setMaxResults(int maxResults) {
        return criteria.setMaxResults(maxResults);
    }

    /**
     * @see Criteria#setProjection(Projection)
     */
    public Criteria setProjection(Projection projection) {
        return criteria.setProjection(projection);
    }

    /**
     * @see Criteria#setResultTransformer(ResultTransformer)
     */
    public Criteria setResultTransformer(ResultTransformer resultTransformer) {
        return criteria.setResultTransformer(resultTransformer);
    }

    /**
     * @see Criteria#setTimeout(int)
     */
    public Criteria setTimeout(int timeout) {
        return criteria.setTimeout(timeout);
    }
}