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
 * All portions are Copyright (C) 2010 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.base.weld;

import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;

/**
 * Provides weld utilities.
 * 
 * @author mtaal
 */
@ApplicationScoped
public class WeldUtils {

  @SuppressWarnings("serial")
  public static final AnnotationLiteral<Any> ANY_LITERAL = new AnnotationLiteral<Any>() {
  };

  @Inject
  private BeanManager beanManager;

  /**
   * Return an instance which has the requested class. Ignores inheritance, so if a class A extends
   * a class B, if B is requested then an instance of B is returned and not an instance of A.
   * 
   * @param <T>
   *          the expected class
   * @param type
   *          the type to search, this type and all its subtypes are searched
   * @return an instance of the requested type
   */
  @SuppressWarnings("unchecked")
  public <T> T getInstance(Class<T> type) {
    final Set<Bean<?>> beans = beanManager.getBeans(type, ANY_LITERAL);
    for (Bean<?> bean : beans) {
      if (bean.getBeanClass() == type) {
        return (T) beanManager.getReference(bean, type, beanManager.createCreationalContext(bean));
      }
    }
    throw new IllegalArgumentException("No bean found for type " + type);
  }
}
