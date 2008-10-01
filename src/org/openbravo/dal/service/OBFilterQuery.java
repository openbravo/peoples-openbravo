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

package org.openbravo.dal.service;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.base.structure.ClientEnabled;
import org.openbravo.base.structure.OrganisationEnabled;
import org.openbravo.base.util.Check;
import org.openbravo.dal.core.OBContext;

/**
 * Uses a filter object to create filtered queries, supports paging and count.
 * 
 * @author mtaal
 */

public class OBFilterQuery {
  private static final Logger log = Logger.getLogger(OBFilterQuery.class);
  
  private OBFilter filter = new OBFilter();
  private String countClause = "count(*)";
  private Session session = null;
  
  public void setSession(Session session) {
    this.session = session;
  }
  
  public Session getSession() {
    Check.isNotNull(session, "The session has not been set, set it by calling setSession()");
    return session;
  }
  
  public OBFilter getFilter() {
    return filter;
  }
  
  public void setFilter(OBFilter filter) {
    this.filter = filter;
  }
  
  // public void setSessionFilters() {
  // if (getFilter().isFilterActive()) {
  // // TODO externalize
  // getSession().enableFilter(OBFilter.FILTER_ACTIVE).setParameter("active",
  // "Y");
  // }
  // if (getFilter().isFilterClient()) {
  // final OBContext obContext = OBContext.getOBContext();
  // String clientList = obContext.getUserClientList();
  // // strip the first and last comma from the clientlist
  // if (clientList.length() > 0) {
  // clientList = clientList.substring(1, clientList.length() - 1);
  // }
  //getSession().enableFilter(OBFilter.FILTER_CLIENT).setParameter("clientList",
  // clientList);
  // }
  // if (getFilter().isFilterOrganisation()) {
  // final OBContext obContext = OBContext.getOBContext();
  // String organisationList = obContext.getUserOrganisationList();
  // // strip the first and last comma from the organisationlist
  // if (organisationList.length() > 0) {
  // organisationList = organisationList.substring(1, organisationList.length()
  // - 1);
  // }
  // getSession().enableFilter(OBFilter.FILTER_ORGANISATION).setParameter(
  // "organisationList", organisationList);
  // }
  // }
  //  
  // public void removeSessionFilters() {
  // getSession().disableFilter(OBFilter.FILTER_ACTIVE);
  // getSession().disableFilter(OBFilter.FILTER_CLIENT);
  // getSession().disableFilter(OBFilter.FILTER_ORGANISATION);
  // }
  
  // public Query createQuery(String queryString) {
  // // add order by
  // final StringBuilder sb = new StringBuilder(queryString);
  // final List<OBFilter.OrderBy> orderBys = getFilter().getOrderBys();
  // if (queryString.toLowerCase().indexOf("order by") == -1 &&
  // !orderBys.isEmpty()) {
  // sb.append(" order by ");
  // int i = 0;
  // for (OBFilter.OrderBy orderBy : orderBys) {
  // if (i > 0) {
  // sb.append(", ");
  // }
  // sb.append(orderBy.getOrderOn());
  // if (orderBy.isAscending()) {
  // sb.append(" asc");
  // } else {
  // sb.append(" desc");
  // }
  // }
  // }
  //    
  // final Query q = getSession().createQuery(sb.toString());
  //    
  // if (getFilter().getFirstResult() > -1) {
  // q.setFirstResult(getFilter().getFirstResult());
  // }
  // if (getFilter().getMaxResults() > -1) {
  // q.setMaxResults(getFilter().getMaxResults());
  // }
  //    
  // return q;
  // }
  
  @SuppressWarnings("unchecked")
  public <T extends Object> List<T> list(Class<T> clz) {
    try {
      final Criteria c = createCriteria(clz);
      final OBFilter localFilter = getFilter();
      
      // TODO: check for strange values and combinations of firstResult and
      // Maxresults
      if (localFilter.getFirstResult() > -1) {
        c.setFirstResult(localFilter.getFirstResult());
      }
      if (localFilter.getMaxResults() > -1) {
        c.setMaxResults(localFilter.getMaxResults());
      }
      
      localFilter.setJoinAndOrderBy(c);
      log.debug("Quering using criteria " + c.toString());
      return c.list();
    } catch (Throwable t) {
      t.printStackTrace(System.err);
      throw new OBException(t);
    }
  }
  
  @SuppressWarnings("unchecked")
  public List<BaseOBObject> list(String entityName) {
    try {
      final Criteria c = createCriteria(entityName);
      final OBFilter localFilter = getFilter();
      
      // TODO: check for strange values and combinations of firstResult and
      // Maxresults
      if (localFilter.getFirstResult() > -1) {
        c.setFirstResult(localFilter.getFirstResult());
      }
      if (localFilter.getMaxResults() > -1) {
        c.setMaxResults(localFilter.getMaxResults());
      }
      
      localFilter.setJoinAndOrderBy(c);
      log.debug("Quering using criteria " + c.toString());
      return c.list();
    } catch (Throwable t) {
      t.printStackTrace(System.err);
      throw new OBException(t);
    }
  }
  
  public int count(Class<?> clz) {
    try {
      final Criteria c = createCriteria(clz);
      c.setProjection(Projections.rowCount());
      log.debug("Counting using criteria " + c.toString());
      return ((Number) c.uniqueResult()).intValue();
    } catch (Throwable t) {
      t.printStackTrace(System.err);
      throw new OBException(t);
    }
  }
  
  public int count(String entityName) {
    try {
      final Criteria c = createCriteria(entityName);
      c.setProjection(Projections.rowCount());
      log.debug("Counting using criteria " + c.toString());
      return ((Number) c.uniqueResult()).intValue();
    } catch (Throwable t) {
      t.printStackTrace(System.err);
      throw new OBException(t);
    }
  }
  
  public String getCountClause() {
    return countClause;
  }
  
  public void setCountClause(String countClause) {
    this.countClause = countClause;
  }
  
  // Computes the standard criterion object using the filter information
  protected Criteria createCriteria(String entityName) {
    final Criteria c = session.createCriteria(entityName);
    addClientOrganisationFilter(c, getEntity(entityName));
    return c;
  }
  
  protected Criteria createCriteria(Class<?> clz) {
    final Criteria c = session.createCriteria(getEntity(clz).getName());
    if (ClientEnabled.class.isAssignableFrom(clz) || OrganisationEnabled.class.isAssignableFrom(clz)) {
      addClientOrganisationFilter(c, getEntity(clz));
    }
    return c;
  }
  
  private Entity getEntity(String entityName) {
    return ModelProvider.getInstance().getEntity(entityName);
  }
  
  private Entity getEntity(Class<?> clz) {
    return ModelProvider.getInstance().getEntity(clz);
  }
  
  protected void addClientOrganisationFilter(Criteria c, Entity e) {
    final OBContext obContext = OBContext.getOBContext();
    final OBFilter localFilter = getFilter();
    // TODO externalise -1
    
    // System.err.println(obContext.getAccessibleOrganisations());
    // System.err.println(obContext.getUserOrganisationList());
    // System.err.println(obContext.getUserClientList());
    
    if (localFilter.getOrganisation() != -1 && e.isOrganisationEnabled()) {
      // TODO add warning if localFilter.isFilterOnUserOrganisation()==true
      c.add(Restrictions.eq("org.id", localFilter.getOrganisation()));
    } else if (localFilter.isFilterOnAccessibleOrganisation() && e.isOrganisationEnabled()) {
      c.add(Restrictions.in("org.id", obContext.getReadableOrganisations()));
    }
    
    if (localFilter.isFilterOnUserClient() && e.isClientEnabled()) {
      c.add(Restrictions.in("client.id", obContext.getReadableClients()));
    }
    
    if (localFilter.getExtraRestrictions() != null) {
      c.add(localFilter.getExtraRestrictions());
    }
    
    if (localFilter.isFilterOnActive() && e.isActiveEnabled()) {
      c.add(Restrictions.eq("active", true));
    }
  }
  
  // comma delimited string array to int array
  private String[] toStringArray(String ids) {
    // strip the first and last comma from the organisationlist/clientlist
    if (ids.length() > 0 && ids.startsWith(",")) {
      ids = ids.substring(1);
    }
    if (ids.length() > 0 && ids.endsWith(",")) {
      ids = ids.substring(0, ids.length() - 1);
    }
    
    return ids.split(",");
    // final String[] result = new String[strIds.length];
    // int i = 0;
    // for (String str : strIds) {
    // result[i++] = new Long(str);
    // }
    // return result;
  }
}