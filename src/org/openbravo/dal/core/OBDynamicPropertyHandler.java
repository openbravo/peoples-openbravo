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
 * All portions are Copyright (C) 2008-2016 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.dal.core;

import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Map;

import org.hibernate.HibernateException;
import org.hibernate.PropertyNotFoundException;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.property.PropertyAccessor;
import org.openbravo.base.model.NamingUtil;
import org.openbravo.base.structure.BaseOBObject;

/**
 * The hibernate getter/setter for a dynamic property.
 * 
 * @author mtaal
 */
@SuppressWarnings("rawtypes")
public class OBDynamicPropertyHandler implements PropertyAccessor {
  public Getter getGetter(Class theClass, String propertyName) throws PropertyNotFoundException {
    return new Getter(theClass, propertyName);
  }

  public Setter getSetter(Class theClass, String propertyName) throws PropertyNotFoundException {
    return new Setter(theClass, propertyName);
  }

  public static class Getter implements org.hibernate.property.Getter {
    private static final long serialVersionUID = 1L;
    private static final String ID_GETTER = "getId";

    private String propertyName;
    private Class theClass;

    public Getter(Class theClass, String propertyName) {
      this.theClass = theClass;
      this.propertyName = NamingUtil.getStaticPropertyName(theClass, propertyName);
    }

    @SuppressWarnings("unchecked")
    public Method getMethod() {
      // special case for IDs, so when executing getId() in a proxy, it is not necessary to load the
      // whole object from DB
      if (BaseOBObject.ID.equals(propertyName)) {
        try {
          return theClass.getDeclaredMethod(ID_GETTER);
        } catch (NoSuchMethodException e) {
        } catch (SecurityException e) {
        }
      }

      return null;
    }

    public Member getMember() {
      return null;
    }

    public String getMethodName() {
      return null;
    }

    public Object get(Object owner) throws HibernateException {
      return ((BaseOBObject) owner).getValue(propertyName);
    }

    public Object getForInsert(Object owner, Map mergeMap, SessionImplementor session)
        throws HibernateException {
      return get(owner);
    }

    public Class getReturnType() {
      return null;
    }
  }

  public static class Setter implements org.hibernate.property.Setter {
    private static final long serialVersionUID = 1L;
    private static final String ID_SETTER = "setId";

    private String propertyName;
    private Class theClass;

    public Setter(Class theClass, String propertyName) {
      this.theClass = theClass;
      this.propertyName = NamingUtil.getStaticPropertyName(theClass, propertyName);
    }

    @SuppressWarnings("unchecked")
    public Method getMethod() {
      if (BaseOBObject.ID.equals(propertyName)) {
        try {
          return theClass.getDeclaredMethod(ID_SETTER, String.class);
        } catch (NoSuchMethodException e) {
        } catch (SecurityException e) {
        }
      }

      return null;
    }

    public String getMethodName() {
      return null;
    }

    public void set(Object target, Object value, SessionFactoryImplementor factory)
        throws HibernateException {
      ((BaseOBObject) target).setValue(propertyName, value);
    }

  }
}
