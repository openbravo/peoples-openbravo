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

package org.openbravo.dal.core;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.proxy.HibernateProxy;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Property;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.base.util.ArgumentException;
import org.openbravo.base.util.Check;

/**
 * Utility class used by the dal layer
 * 
 * @author mtaal
 */

public class DalUtil {

    // Copies a BaseOBObject and all its children, note will
    // nullify the id of the copied object.
    public static List<BaseOBObject> copyAll(List<BaseOBObject> source) {
        return copyAll(source, true);
    }

    public static List<BaseOBObject> copyAll(List<BaseOBObject> source,
            boolean resetId) {
        final List<BaseOBObject> result = new ArrayList<BaseOBObject>();
        for (final BaseOBObject bob : source) {
            result.add(copy(bob, true, resetId));
        }
        return result;
    }

    public static BaseOBObject copy(BaseOBObject source) {
        return copy(source, true);
    }

    public static BaseOBObject copy(BaseOBObject source, boolean copyOneToMany) {
        return copy(source, copyOneToMany, true);
    }

    public static BaseOBObject copy(BaseOBObject source, boolean copyOneToMany,
            boolean resetId) {
        final BaseOBObject target = (BaseOBObject) OBProvider.getInstance()
                .get(source.getEntityName());
        for (final Property p : source.getEntity().getProperties()) {
            final Object value = source.getValue(p.getName());
            if (p.isOneToMany()) {
                if (copyOneToMany) {
                    final List<BaseOBObject> targetChildren = new ArrayList<BaseOBObject>();
                    target.setValue(p.getName(), targetChildren);
                    @SuppressWarnings("unchecked")
                    final List<BaseOBObject> sourceChildren = (List<BaseOBObject>) value;
                    for (final BaseOBObject sourceChild : sourceChildren) {
                        targetChildren.add(copy(sourceChild, copyOneToMany,
                                resetId));
                    }
                }
            } else {
                target.setValue(p.getName(), value);
            }
        }
        if (resetId) {
            target.setId(null);
        }
        return target;
    }

    // returns the referenced value, handles primary key as
    // well as non-primary key properties. The referencingProperty
    // is the property from the owner object.
    public static Serializable getReferencedPropertyValue(
            Property referencingProperty, Object o) {
        Check.isTrue(referencingProperty.getReferencedProperty() != null,
                "This property " + referencingProperty
                        + " does not have a referenced Property");
        final Property referencedProperty = referencingProperty
                .getReferencedProperty();
        if (referencedProperty.isId()) {
            if (o instanceof HibernateProxy)
                return ((HibernateProxy) o).getHibernateLazyInitializer()
                        .getIdentifier();
            if (o instanceof BaseOBObject)
                return (Serializable) ((BaseOBObject) o).getId();
        } else if (o instanceof BaseOBObject) {
            return (Serializable) ((BaseOBObject) o).get(referencedProperty
                    .getName());
        }

        throw new ArgumentException(
                "Argument is not a BaseOBObject and not a HibernateProxy");
    }

    // returns the id, takes care of not resolving proxies
    public static Serializable getId(Object o) {
        if (o instanceof HibernateProxy)
            return ((HibernateProxy) o).getHibernateLazyInitializer()
                    .getIdentifier();
        if (o instanceof BaseOBObject)
            return (Serializable) ((BaseOBObject) o).getId();
        throw new ArgumentException(
                "Argument is not a BaseOBObject and not a HibernateProxy "
                        + (o != null ? o.getClass().getName() : "NULL"));
    }

    // returns the static member containing the entityname
    // handles hibernate proxies
    // TODO: create a cache!
    // TODO: this can be done nicer with an annotation but then
    // jdk1.5 is a prerequisite
    public static String getEntityName(Object o) {
        if (o instanceof HibernateProxy)
            return getEntityName(((HibernateProxy) o)
                    .getHibernateLazyInitializer().getPersistentClass());
        return getEntityName(o.getClass());
    }

    // Note: in case the class is retrieved from a object before calling this
    // method
    // then use the above method getEntityName(Object o).
    public static String getEntityName(Class<?> clz) {
        try {
            final Field fld = clz.getField("ENTITY_NAME");
            return (String) fld.get(null);
        } catch (final Exception e) {
            throw new OBException(e);
        }
    }
}