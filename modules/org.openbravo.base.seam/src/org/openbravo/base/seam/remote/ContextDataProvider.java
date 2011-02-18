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

package org.openbravo.base.seam.remote;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.remoting.WebRemote;
import org.jboss.seam.core.Expressions;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.base.util.Check;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;

/**
 * Is the main entry point for getting to data in the backend and returning it to the front end. It
 * can handle both EL expressions as well as queries for data.
 * 
 * TODO:
 * 
 * 1) How to handle exceptions......, we should return something if something fails...
 * 
 * 2) How to do formatting taking into account differences between edit and display format...
 * 
 * 3) Provide convenience methods to fill a listbox both using an enumerate as well as a foreign
 * key.
 * 
 * @author mtaal
 */
@Name("contextDataProvider")
@Scope(ScopeType.APPLICATION)
@Install(precedence = Install.FRAMEWORK)
public class ContextDataProvider {

  @In
  private DataToJsonConverter dataToJsonConverter;

  /**
   * Resolve the name of a context variable against the context. The method calls the
   * {@link #resolveExpression(String)} method with the value enclosed by #{...}.
   * 
   * @param value
   *          contains the name of the context variable which should be resolved.
   * @return a json string containing the result.
   * @see DataToJsonConverter
   */
  @WebRemote
  public String resolveValue(String value) {
    return resolveExpression("#{" + value + "}");
  }

  /**
   * Resolve an EL expression against the context.
   * 
   * @param elExpression
   *          a valid EL expression between #{ and }.
   * @return a json string containing the resolved data
   * @see DataToJsonConverter
   */
  @WebRemote
  public String resolveExpression(String elExpression) {
    Check.isTrue(elExpression.trim().startsWith("#{") && elExpression.trim().endsWith("}"),
        "Only an EL expression between #{..} is supported!");

    Object value = Expressions.instance().createValueExpression(elExpression).getValue();
    return dataToJsonConverter.convertToJsonString(value, DataResolvingMode.FULL);
  }

  /**
   * Queries the database with additional paging parameters, returns the retrieved data as json
   * arrays.
   * 
   * @param entityName
   *          the name of the entity to query for
   * @param whereClause
   *          the whereClause (hql)
   * @param startFrom
   *          the record number to start from a value of 0 or lower means that all records are
   *          returned.
   * @param pageSize
   *          the number of records to return.
   * @return the retrieved data as a json string.
   * @see DataToJsonConverter
   */
  @WebRemote
  public String query(String entityName, String whereClause, int startFrom, int pageSize,
      DataResolvingMode dataResolvingMode) {
    final OBQuery<BaseOBObject> obc = OBDal.getInstance().createQuery(entityName, whereClause);
    if (startFrom > 0) {
      obc.setFirstResult(startFrom);
      obc.setMaxResult(pageSize);
    }

    return dataToJsonConverter.convertToJsonString(obc.list(), dataResolvingMode);
  }

  /**
   * Retrieves one object from the backend and returns it as a json string.
   * 
   * @param entityName
   *          the entity name denoting the entity to retrieve
   * @param id
   *          the id of the entity to retrieve
   * @return a json string representing the requested object
   */
  @WebRemote
  public String get(String entityName, String id) {
    final BaseOBObject bob = OBDal.getInstance().get(entityName, id);
    return dataToJsonConverter.convertToJsonString(bob, DataResolvingMode.FULL);
  }
}
