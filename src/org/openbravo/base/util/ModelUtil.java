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
package org.openbravo.base.util;

import java.lang.reflect.Field;

import org.hibernate.proxy.HibernateProxy;
import org.openbravo.base.exception.OBException;

/**
 * Contains utility methods
 * 
 * @author mtaal
 */

public class ModelUtil {
  
  // returns the static member containing the entityname
  // handles hibernate proxies
  // TODO: create a cache!
  // TODO: this can be done nicer with an annotation but then
  // jdk1.5 is a prerequisite
  public static String getEntityName(Object o) {
    if (o instanceof HibernateProxy)
      return getEntityName(((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass());
    return getEntityName(o.getClass());
  }
  
  // Note: in case the class is retrieved from a object before calling this
  // method
  // then use the above method getEntityName(Object o).
  public static String getEntityName(Class<?> clz) {
    try {
      final Field fld = clz.getField("ENTITYNAME");
      return (String) fld.get(null);
    } catch (Exception e) {
      throw new OBException(e);
    }
  }
}