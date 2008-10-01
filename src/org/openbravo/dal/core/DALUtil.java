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

import java.io.Serializable;

import org.hibernate.proxy.HibernateProxy;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.base.util.ArgumentException;

/**
 * Util class
 * 
 * @author mtaal
 */

public class DALUtil {
  
  // returns the id, takes care of not resolving proxies
  public static Serializable getId(Object o) {
    if (o instanceof HibernateProxy)
      return ((HibernateProxy) o).getHibernateLazyInitializer().getIdentifier();
    if (o instanceof BaseOBObject)
      return (Serializable) ((BaseOBObject) o).getId();
    throw new ArgumentException("Argument is not a BaseOBObject and not a HibernateProxy");
  }
  
}