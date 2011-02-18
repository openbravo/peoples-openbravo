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
 * All portions are Copyright (C) 2010 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.client.application;

import org.hibernate.criterion.Expression;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.util.OBClassLoader;
import org.openbravo.client.kernel.BaseComponent;
import org.openbravo.client.kernel.BaseTemplateComponent;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;

/**
 * Reads the view and generates it.
 * 
 * @author mtaal
 */
public class ViewComponent extends BaseComponent {

  @Override
  public String generate() {

    final String viewName = getParameter("name");
    if (viewName == null) {
      throw new IllegalArgumentException("Name parameter not found, it is mandatory");
    }

    try {
      OBContext.setAdminMode();

      OBCriteria<OBUIAPPViewImplementation> obc = OBDal.getInstance().createCriteria(
          OBUIAPPViewImplementation.class);
      obc.add(Expression.eq(OBUIAPPViewImplementation.PROPERTY_NAME, viewName));

      if (obc.list().size() > 0) {
        OBUIAPPViewImplementation viewImpDef = obc.list().get(0);

        final BaseTemplateComponent component;
        if (viewImpDef.getJavaClassName() != null) {
          try {
            component = (BaseTemplateComponent) OBClassLoader.getInstance().loadClass(
                viewImpDef.getJavaClassName()).newInstance();
          } catch (Exception e) {
            throw new OBException(e);
          }
        } else {
          component = new BaseTemplateComponent();
          if (viewImpDef.getTemplate() == null) {
            throw new IllegalStateException("No class and no template defined for view " + viewName);
          }
        }
        component.setId(viewImpDef.getId());
        component.setComponentTemplate(viewImpDef.getTemplate());
        component.setParameters(getParameters());

        final String jsCode = component.generate();
        return jsCode;
      } else {
        throw new IllegalArgumentException("No view found using name " + viewName);
      }
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  @Override
  public Object getData() {
    return this;
  }

}
