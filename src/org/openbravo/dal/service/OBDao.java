/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html 
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License.
 * The Original Code is Openbravo ERP.
 * The Initial Developer of the Original Code is Openbravo SLU
 * All portions are Copyright (C) 2011 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.dal.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.Expression;
import org.openbravo.base.structure.BaseOBObject;

/**
 * Util class for DAL
 * 
 * @author gorkaion
 * 
 */
public class OBDao {

  /**
   * Generic OBCriteria.
   * 
   * @param clazz
   *          Class (entity).
   * @param setFilterClient
   *          If true then only objects from readable clients are returned, if false then objects
   *          from all clients are returned
   * @param setFilterOrg
   *          If true then when querying (for example call list()) a filter on readable
   *          organizations is added to the query, if false then this is not done
   * @param constraints
   *          Constraint. Property, value and operator.
   * @return An OBCriteria object with the constraints.
   */
  public static <T extends BaseOBObject> OBCriteria<T> getFilteredCriteria(Class<T> clazz,
      boolean setFilterClient, boolean setFilterOrg, Constraint... constraints) {
    OBCriteria<T> obc = OBDal.getInstance().createCriteria(clazz);
    obc.setFilterOnReadableClients(setFilterClient);
    obc.setFilterOnReadableOrganization(setFilterOrg);
    addContrainsToCriteria(obc, constraints);
    return obc;
  }

  /**
   * Implementation of {@link OBDao#getFilteredCriteria(Class, boolean, boolean, Constraint...)}
   * enabling the filter by readable clients and organizations.
   */
  public static <T extends BaseOBObject> OBCriteria<T> getFilteredCriteria(Class<T> clazz,
      Constraint... constraints) {
    return getFilteredCriteria(clazz, true, true, constraints);
  }

  /**
   * Returns a List object with the instances of the given OBObject Class filtered by the given
   * array of constraints. The default client and organization filters can be disabled using the
   * corresponding boolean parameters.
   * 
   * @param clazz
   *          Class (entity).
   * @param setFilterClient
   *          If true then only objects from readable clients are returned, if false then objects
   *          from all clients are returned
   * @param setFilterOrg
   *          If true then when querying (for example call list()) a filter on readable
   *          organizations is added to the query, if false then this is not done
   * @param constraints
   *          Constraint. Property, value and operator.
   * @return An List object with the objects of Class clazz filtered by the given constraints.
   */
  public static <T extends BaseOBObject> List<T> getAllInstances(Class<T> clazz,
      boolean setClientFilter, boolean setOrganizationFilter, Constraint... constraints) {
    return getFilteredCriteria(clazz, setClientFilter, setOrganizationFilter, constraints).list();
  }

  /**
   * Implementation of {@link OBDao#getAllInstances(Class, boolean, boolean, Constraint...)}
   * enabling the filter by readable clients and organizations.
   */
  public static <T extends BaseOBObject> List<T> getAllInstances(Class<T> clazz,
      Constraint... constraints) {
    return getFilteredCriteria(clazz, true, true, constraints).list();
  }

  /**
   * Returns the first object of the given OBObject Class filtered by the given array of
   * constraints. The default client and organization filters can be disabled using the
   * corresponding boolean parameters.
   * 
   * @param clazz
   *          Class (entity).
   * @param setFilterClient
   *          If true then only objects from readable clients are returned, if false then objects
   *          from all clients are returned
   * @param setFilterOrg
   *          If true then when querying (for example call list()) a filter on readable
   *          organizations is added to the query, if false then this is not done
   * @param constraints
   *          Constraint. Property, value and operator.
   * @return An List object with the objects of Class clazz filtered by the given constraints.
   */
  public static <T extends BaseOBObject> T getOneInstance(Class<T> clazz, boolean setClientFilter,
      boolean setOrganizationFilter, Constraint... constraints) {
    OBCriteria<T> criteria = getFilteredCriteria(clazz, setClientFilter, setOrganizationFilter,
        constraints);
    criteria.setMaxResults(1);
    if (criteria.list().isEmpty()) {
      return null;
    }
    return criteria.list().get(0);
  }

  /**
   * Implementation of {@link OBDao#getOneInstance(Class, boolean, boolean, Constraint...)} enabling
   * the filter by readable clients and organizations.
   */
  public static <T extends BaseOBObject> T getOneInstance(Class<T> clazz, Constraint... constraints) {
    return getOneInstance(clazz, true, true, constraints);
  }

  public static <T extends BaseOBObject> void addContrainsToCriteria(OBCriteria<T> obc,
      Constraint... constraints) {
    for (Constraint constraint : constraints) {
      if (constraint.getValue() == null && Operator.EQUAL_OPERATOR.equals(constraint.getOperator())) {
        obc.add(Expression.isNull(constraint.getProperty()));
      } else if (constraint.getValue() == null
          && Operator.NOT_EQUAL_OPERATOR.equals(constraint.getOperator())) {
        obc.add(Expression.isNotNull(constraint.getProperty()));
      } else if (Operator.EQUAL_OPERATOR.equals(constraint.getOperator())) {
        obc.add(Expression.eq(constraint.getProperty(), constraint.getValue()));
      } else if (Operator.NOT_EQUAL_OPERATOR.equals(constraint.getOperator())) {
        obc.add(Expression.ne(constraint.getProperty(), constraint.getValue()));
      } else if (Operator.LESS_OPERATOR.equals(constraint.getOperator())) {
        obc.add(Expression.lt(constraint.getProperty(), constraint.getValue()));
      } else if (Operator.GREATER_OPERATOR.equals(constraint.getOperator())) {
        obc.add(Expression.gt(constraint.getProperty(), constraint.getValue()));
      } else if (Operator.LESS_EQUAL_OPERATOR.equals(constraint.getOperator())) {
        obc.add(Expression.le(constraint.getProperty(), constraint.getValue()));
      } else if (Operator.GREATER_EQUAL_OPERATOR.equals(constraint.getOperator())) {
        obc.add(Expression.ge(constraint.getProperty(), constraint.getValue()));
      } else if (Operator.IN_OPERATOR.equals(constraint.getOperator())) {
        Object value = constraint.getValue();
        if (value instanceof Collection) {
          obc.add(Expression.in(constraint.getProperty(), (Collection<?>) value));
        } else if (value instanceof Object[]) {
          obc.add(Expression.in(constraint.getProperty(), (Object[]) constraint.getValue()));
        }
      } else {
        obc.add(Expression.eq(constraint.getProperty(), constraint.getValue()));
      }
    }

  }

  /**
   * Each instance of this class contains a Constraint to be added to the OBCriteria object used on
   * {@link OBDao#getFilteredCriteria(Class, boolean, boolean, Constraint...)}.
   * 
   * @author gorkaion
   * 
   */
  public static class Constraint {
    private String property;
    private Object value;
    // '==', '!=', '<=', '>=', '<', '>'
    private Operator operator;

    /**
     * Initializes a Constraint to be added to an OBCriteria. The Operator is defaulted to
     * {@link OBDao.Operator EQUAL_OPERATOR}.
     * 
     * <br>
     * A <i>null</i> value creates a "isNull" Expression for the given property
     * 
     * @param property
     *          String with the property to filter by the OBCriteria.
     * @param value
     *          Object with the filter the property is filtered by.
     */
    public Constraint(String property, Object value) {
      this.property = property;
      this.value = value;
      this.operator = Operator.EQUAL_OPERATOR;
    }

    /**
     * Initializes a Constraint to be added to an OBCriteria.
     * 
     * <br>
     * A <i>null</i> value and {@link OBDao.Operator EQUAL_OPERATOR} operator creates a "isNull"
     * expression for the given property.
     * 
     * <br>
     * A <i>null</i> value and {@link OBDao.Operator NOT_EQUAL_OPERATOR} operator creates a
     * "isNotNull" expression for the given property.
     * 
     * @param property
     *          String with the property to filter by the OBCriteria.
     * @param value
     *          Object with the filter the property is filtered by.
     * @param operator
     *          Operator that defines the expression type to be applied for this Constraint.
     */
    public Constraint(String property, Object value, Operator operator) {
      this.property = property;
      this.value = value;
      this.operator = operator;
    }

    public String getProperty() {
      return property;
    }

    public Object getValue() {
      return value;
    }

    public Operator getOperator() {
      return operator;
    }
  }

  /**
   * Valid operators to apply on a Constraint to filter an OBCriteria.
   * 
   * @author gorkaion
   * 
   */
  public static enum Operator {
    EQUAL_OPERATOR, NOT_EQUAL_OPERATOR, LESS_EQUAL_OPERATOR, GREATER_EQUAL_OPERATOR, LESS_OPERATOR, GREATER_OPERATOR, IN_OPERATOR
  }

  /**
   * Returns a List of BaseOBOBjects of the Property identified by the property from the
   * BaseOBObject obj. This method enables the activeFilter so inactive BaseOBObjects are not
   * included on the returned List.
   * 
   * @param obj
   *          BaseOBObject from which the values are requested
   * @param property
   *          the name of the Property for which the value is requested
   * @return
   */
  @SuppressWarnings("unchecked")
  public static <T extends BaseOBObject> List<T> getActiveOBObjectList(BaseOBObject obj,
      String property) {
    boolean isActiveFilterEnabled = OBDal.getInstance().isActiveFilterEnabled();
    if (!isActiveFilterEnabled) {
      OBDal.getInstance().enableActiveFilter();
    }
    try {
      return (List<T>) obj.get(property);
    } finally {
      if (!isActiveFilterEnabled) {
        OBDal.getInstance().disableActiveFilter();
      }
    }
  }

  /**
   * Parses the string of comma separated id's to return a List with the BaseOBObjects of the given
   * class. If there is an invalid id a null value is added to the List.
   * 
   * @param t
   *          class of the BaseOBObject the id's belong to
   * @param _IDs
   *          String containing the comma separated list of id's
   * @return a List object containing the parsed OBObjects
   */
  public static <T extends BaseOBObject> List<T> getOBObjectListFromString(Class<T> t, String _IDs) {
    String strBaseOBOBjectIDs = _IDs;
    final List<T> baseOBObjectList = new ArrayList<T>();
    if (strBaseOBOBjectIDs.startsWith("(")) {
      strBaseOBOBjectIDs = strBaseOBOBjectIDs.substring(1, strBaseOBOBjectIDs.length() - 1);
    }
    if (!strBaseOBOBjectIDs.equals("")) {
      strBaseOBOBjectIDs = StringUtils.remove(strBaseOBOBjectIDs, "'");
      StringTokenizer st = new StringTokenizer(strBaseOBOBjectIDs, ",", false);
      while (st.hasMoreTokens()) {
        String strBaseOBObjectID = st.nextToken().trim();
        baseOBObjectList.add((T) OBDal.getInstance().get(t, strBaseOBObjectID));
      }
    }
    return baseOBObjectList;
  }
}
