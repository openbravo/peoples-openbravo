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

import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.BaseOBObjectDef;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;

/**
 * Base business object, the root of the inheritance tree. The class model here
 * combines an inheritance structure with interface definitions. The inheritance
 * structure is used to enable some re-use of code. The interfaces are used to
 * tag a certain implementation with the functionality it provides. The outside
 * world should use the interfaces to determine if an object supports specific
 * functionality.
 * 
 * @author mtaal
 */

public abstract class BaseOBObject implements BaseOBObjectDef, Identifiable, DynamicEnabled {
  
  private Entity model = null;
  
  // is used to force an insert of this object. This is usefull if the id of the
  // object should be preserved when it is imported
  private boolean newOBObject = false;
  
  public Object getId() {
    return get("id");
  }
  
  public void setId(Object id) {
    set("id", id);
  }
  
  public abstract String getEntityName();
  
  public String getIdentifier() {
    return IdentifierProvider.getInstance().getIdentifier(this);
  }
  
  public Object get(String featureName) {
    if (featureName.equals("entityName")) {
      return getEntityName();
    }
    throw new OBException("Feature name " + featureName + " not defined for entity " + getEntityName());
  }
  
  public void set(String featureName, Object value) {
    throw new OBException("Feature name " + featureName + " not defined for entity " + getEntityName());
  }
  
  public Entity getModel() {
    if (model == null) {
      model = ModelProvider.getInstance().getEntity(getEntityName());
    }
    return model;
  }
  
  public void validate() {
    getModel().validate(this);
  }
  
  @Override
  // toString method which returns the identifier and the values of all
  // primitive type columns
  public String toString() {
    final Entity e = getModel();
    final StringBuilder sb = new StringBuilder();
    // and also display all primitive type values
    for (Property p : e.getProperties()) {
      Object value = get(p.getName());
      if (value != null) {
        if (sb.length() == 0) {
          sb.append("(");
        } else {
          sb.append(", ");
        }
        if (value instanceof BaseOBObject) {
          value = ((BaseOBObject) value).getId();
        }
        sb.append(p.getName() + ": " + value);
      }
    }
    if (sb.length() > 0) {
      sb.append(")");
    }
    return getIdentifier() + " " + sb.toString();
  }
  
  public boolean isNewOBObject() {
    return newOBObject;
  }
  
  public void setNewOBObject(boolean newOBObject) {
    this.newOBObject = newOBObject;
  }
}