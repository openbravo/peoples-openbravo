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

package org.openbravo.dal.core;

import java.lang.reflect.Method;
import java.util.Map;

import org.hibernate.HibernateException;
import org.hibernate.PropertyNotFoundException;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.property.PropertyAccessor;
import org.openbravo.base.structure.DynamicEnabled;

/**
 * The hibernate getter/setter for a dynamic property.
 * 
 * @author mtaal
 */
@SuppressWarnings("unchecked")
public class OBDynamicPropertyHandler implements PropertyAccessor {
  public Getter getGetter(Class theClass, String propertyName) throws PropertyNotFoundException {
    return new Getter(propertyName);
  }
  
  public Setter getSetter(Class theClass, String propertyName) throws PropertyNotFoundException {
    return new Setter(propertyName);
  }
  
  public static class Getter implements org.hibernate.property.Getter {
    private static final long serialVersionUID = 1L;
    
    private String propertyName;
    
    public Getter(String propertyName) {
      this.propertyName = propertyName;
    }
    
    public Method getMethod() {
      return null;
    }
    
    public String getMethodName() {
      return null;
    }
    
    public Object get(Object owner) throws HibernateException {
      return ((DynamicEnabled) owner).get(propertyName);
    }
    
    public Object getForInsert(Object owner, Map mergeMap, SessionImplementor session) throws HibernateException {
      return get(owner);
    }
    
    public Class getReturnType() {
      return null;
    }
  }
  
  public static class Setter implements org.hibernate.property.Setter {
    private static final long serialVersionUID = 1L;
    
    private String propertyName;
    
    public Setter(String propertyName) {
      this.propertyName = propertyName;
    }
    
    public Method getMethod() {
      return null;
    }
    
    public String getMethodName() {
      return null;
    }
    
    public void set(Object target, Object value, SessionFactoryImplementor factory) throws HibernateException {
      ((DynamicEnabled) target).set(propertyName, value);
    }
    
  }
}
