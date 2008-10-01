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

package org.openbravo.base.structure;

import org.apache.log4j.Logger;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;

/**
 * Factory for all Openbravo objects.
 * 
 * TODO: support external configuration of the factory so that it is possible to
 * replace the standard business objects of Openbravo with a custom
 * implementation.
 * 
 * TODO: it can make sense to make changes in subclasses of the generated
 * classes (in src-gen). However to support this factory has to be configurable
 * so that it instantiates the correct sub-type.
 * 
 * @author mtaal
 */
public class OBFactory {
  private static final long serialVersionUID = 1L;
  private static final Logger log = Logger.getLogger(OBFactory.class);
  
  private static OBFactory instance = new OBFactory();
  
  public static OBFactory getInstance() {
    return instance;
  }
  
  public static void setInstance(OBFactory instance) {
    OBFactory.instance = instance;
  }
  
  public Object create(String entityName) {
    final Entity e = ModelProvider.getInstance().getEntity(entityName);
    try {
      return e.getMappingClass().newInstance();
    } catch (Exception x) {
      throw new OBException(x);
    }
  }
  
  @SuppressWarnings("unchecked")
  public <T extends Object> T create(Class<T> clz) {
    final Entity e = ModelProvider.getInstance().getEntity(clz);
    try {
      return (T) e.getMappingClass().newInstance();
    } catch (Exception x) {
      throw new OBException(x);
    }
  }
}
