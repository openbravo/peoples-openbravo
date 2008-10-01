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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.openbravo.base.util.ArgumentException;

/**
 * Filter object to facilitate creation of queries which are filtered using
 * active, client, organisation etc. Also filter to support paging is provided.
 * 
 * @author mtaal
 */
// TODO: overall the filter tries to offer an interface without requiring a hb
// session,
// getting the session is done inside the OBDal (layer). This means that the
// OBFilter can't create a criteria object. On the other hand it can make
// sense to add the possibility to support joins which requires the
// criteria object. So to solve this the filter should probably have an
// internal structure holding criteria until they are really created.
// Note that the orderby computation should check for existing
// join clauses so that no double joins are done.
public class OBFilter {
  
  public static final String FILTER_ACTIVE = "filterActive";
  public static final String FILTER_CLIENT = "filterClient";
  public static final String FILTER_ORGANISATION = "filterOrganisation";
  
  private final boolean filterOnUserClient = true;
  private boolean filterOnAccessibleOrganisation = true;
  private boolean filterOnActive = true;
  private long organisation = -1;
  private int firstResult = -1; // to support pagination
  private int maxResults = -1;
  private List<OrderBy> orderBys = new ArrayList<OrderBy>();
  private Map<String, Join> joins = new HashMap<String, Join>();
  private Criterion extraRestrictions = null;;
  
  public boolean isFilterOnAccessibleOrganisation() {
    return filterOnAccessibleOrganisation;
  }
  
  public long getOrganisation() {
    return organisation;
  }
  
  public void setOrganisation(long organisation) {
    this.organisation = organisation;
  }
  
  public List<OrderBy> getOrderBys() {
    return orderBys;
  }
  
  public boolean isFilterOnUserClient() {
    return filterOnUserClient;
  }
  
  //  
  // public void setFilterOnUserClient(boolean filterClient) {
  // this.filterOnUserClient = filterClient;
  // }
  
  public boolean isFilterOnActive() {
    return filterOnActive;
  }
  
  public void setFilterOnActive(boolean filterActive) {
    this.filterOnActive = filterActive;
  }
  
  public void addOrderBy(String orderOn, boolean ascending) {
    orderBys.add(new OrderBy(orderOn, ascending));
  }
  
  public void addJoin(String joinOn, String alias, int joinType) {
    if (joins.get(joinOn) != null)
      throw new ArgumentException("Join criteria: " + joinOn + " is already present");
    joins.put(joinOn, new Join(joinOn, alias, joinType));
  }
  
  public int getFirstResult() {
    return firstResult;
  }
  
  public void setFirstResult(int firstResult) {
    this.firstResult = firstResult;
  }
  
  public int getMaxResults() {
    return maxResults;
  }
  
  public void setMaxResults(int maxResults) {
    this.maxResults = maxResults;
  }
  
  public void setJoinAndOrderBy(Criteria c) {
    for (OrderBy ob : getOrderBys()) {
      int j = 0;
      String orderOn = ob.getOrderOn();
      if (orderOn.indexOf(".") != -1) {
        final String orderJoin = orderOn.substring(0, orderOn.lastIndexOf("."));
        final String alias = "order_ob_" + j;
        if (joins.get(orderJoin) != null) {
          final Join join = joins.get(orderJoin);
          if (join.getAlias() != null) {
            orderOn = join.getAlias() + "." + orderOn.substring(orderOn.lastIndexOf(".") + 1);
          }
        } else {
          joins.put(orderJoin, new Join(orderJoin, alias, CriteriaSpecification.LEFT_JOIN));
          orderOn = alias + "." + orderOn.substring(orderOn.lastIndexOf(".") + 1);
        }
      }
      
      if (ob.isAscending()) {
        c.addOrder(Order.asc(orderOn));
      } else {
        c.addOrder(Order.desc(orderOn));
      }
    }
    
    for (Join join : joins.values()) {
      c.createCriteria(join.getJoinOn(), join.getAlias(), join.getJoinType());
    }
  }
  
  public Criterion getExtraRestrictions() {
    return extraRestrictions;
  }
  
  public void setExtraRestrictions(Criterion extraRestrictions) {
    this.extraRestrictions = extraRestrictions;
  }
  
  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append("Filter Information: \n");
    sb.append("filterOnUserClient: " + isFilterOnUserClient() + " \n");
    sb.append("filterOnAccessibleOrganisation: " + isFilterOnAccessibleOrganisation() + " \n");
    sb.append("filterOnActive: " + isFilterOnActive() + " \n");
    sb.append("organisation: " + getOrganisation() + " \n");
    sb.append("firstResult: " + getFirstResult() + " \n");
    sb.append("maxResults: " + getMaxResults() + " \n");
    sb.append("OrderBy: ");
    for (OrderBy ob : getOrderBys()) {
      sb.append(ob.toString());
    }
    sb.append("\n");
    sb.append("Joins : ");
    for (Join j : joins.values()) {
      sb.append(j.toString());
    }
    sb.append("\n");
    if (extraRestrictions != null) {
      sb.append("Extra Criteria: " + extraRestrictions);
    }
    return sb.toString();
  }
  
  // Join class, describes a join to make ordering possible on associated types
  private static class Join {
    private final String joinOn;
    private final String alias;
    private final int joinType;
    
    private Join(String joinOn, String alias, int joinType) {
      this.joinOn = joinOn;
      this.alias = alias;
      this.joinType = joinType;
    }
    
    public String getJoinOn() {
      return joinOn;
    }
    
    public String getAlias() {
      return alias;
    }
    
    public int getJoinType() {
      return joinType;
    }
    
    @Override
    public String toString() {
      return getJoinOn() + " " + getJoinType() + " ";
    }
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
  
  public void setFilterOnAccessibleOrganisation(boolean filterOnAccessibleOrganisation) {
    this.filterOnAccessibleOrganisation = filterOnAccessibleOrganisation;
  }
  
}