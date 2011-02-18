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
 * The Initial Developer of the Original Code is Openbravo SLU 
 * All portions are Copyright (C) 2009 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.base.seam.persistence;

import java.io.Serializable;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.dal.service.OBDal;

/**
 * A generic class which can be used to retrieve {@link BaseOBObject} instances.
 * 
 * @author mtaal
 */
@Name("obDataProvider")
@AutoCreate
@Install(precedence = Install.FRAMEWORK)
@Scope(ScopeType.CONVERSATION)
public class OBDataProvider implements Serializable {

  private static final long serialVersionUID = 1L;

  @In
  private EntityManager entityManager;

  /**
   * Query for {@link BaseOBObject} instances defined by the passed entityName parameter. The method
   * supports paging and sorting on one property.
   * 
   * The method takes into account readable organizations and readable clients (see
   * {@link OBDal#getReadableOrganizationsInClause()} and {@link OBDal#getReadableClientsInClause()}
   * ).
   * 
   * @param entityName
   *          the name of the entities to query
   * @param sortField
   *          the property to sort on, maybe null in which case no sorting is done.
   * @param asc
   *          the sort direction, only relevant if sortField != null
   * @param startRow
   *          the first row to return, is ignored if set to -1
   * @param rowCount
   *          the number of rows to return, is ignored if startRow == -1
   * @return a list of business objects, can be empty
   */
  public List<?> getBusinessObjects(String entityName, String sortField, boolean asc, int startRow,
      int rowCount) {
    final StringBuilder qryStr = new StringBuilder("select e from " + entityName + " e ");

    final Entity entity = ModelProvider.getInstance().getEntity(entityName);
    if (entity.hasProperty("client")) {
      qryStr.append(" where e.client.id " + OBDal.getInstance().getReadableClientsInClause());
    }
    if (entity.hasProperty("organization")) {
      if (!entity.hasProperty("client")) {
        qryStr.append(" where ");
      } else {
        qryStr.append(" and ");
      }
      qryStr.append("e.organization.id " + OBDal.getInstance().getReadableOrganizationsInClause());
    }

    if (sortField != null) {
      qryStr.append(" order by " + sortField);
      if (asc) {
        qryStr.append(" asc ");
      }
    }

    // System.err.println(qryStr);

    final Query qry = entityManager.createQuery(qryStr.toString());
    if (startRow >= -1) {
      qry.setFirstResult(startRow);
      qry.setMaxResults(rowCount);
    }
    return qry.getResultList();
  }

  /**
   * Count the number of instances in the database for a certain entity. takes into account readable
   * organizations and readable clients (see {@link OBDal#getReadableOrganizationsInClause()} and
   * {@link OBDal#getReadableClientsInClause()}).
   * 
   * @param entityName
   *          the name of the entity to count.
   * @return the number of entities in the database
   */
  public long countBusinessObjects(String entityName) {
    final StringBuilder qryStr = new StringBuilder("select count(*) from " + entityName + " e ");

    final Entity entity = ModelProvider.getInstance().getEntity(entityName);
    if (entity.hasProperty("client")) {
      qryStr.append(" where e.client.id " + OBDal.getInstance().getReadableClientsInClause());
    }
    if (entity.hasProperty("organization")) {
      if (!entity.hasProperty("client")) {
        qryStr.append(" where ");
      } else {
        qryStr.append(" and ");
      }
      qryStr.append("e.organization.id " + OBDal.getInstance().getReadableOrganizationsInClause());
    }

    final Query qry = entityManager.createQuery(qryStr.toString());
    return (Long) qry.getSingleResult();
  }

  /**
   * Queries for a specific instance defined by the entityname and id.
   * 
   * @param entityName
   *          the name of the entity to query for,
   * @param id
   *          its id
   * @return the instance or null if not found.
   */
  public BaseOBObject get(String entityName, String id) {
    final Query qry = entityManager.createQuery("select e from " + entityName + " e where e.id=?");
    qry.setParameter(1, id);
    qry.setMaxResults(1);
    final List<?> result = qry.getResultList();
    if (result.size() == 0) {
      return null;
    }
    return (BaseOBObject) result.get(0);
  }
}