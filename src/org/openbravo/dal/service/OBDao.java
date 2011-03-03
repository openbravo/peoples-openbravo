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

import java.util.Collection;
import java.util.List;

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
    List<T> criteria = getAllInstances(clazz, setClientFilter, setOrganizationFilter, constraints);
    if (criteria.isEmpty()) {
      return null;
    }
    return criteria.get(0);
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

    public Constraint(String property, Object value) {
      this.property = property;
      this.value = value;
      this.operator = Operator.EQUAL_OPERATOR;
    }

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

  public static enum Operator {
    EQUAL_OPERATOR, NOT_EQUAL_OPERATOR, LESS_EQUAL_OPERATOR, GREATER_EQUAL_OPERATOR, LESS_OPERATOR, GREATER_OPERATOR, IN_OPERATOR
  }
}
