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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.openbravo.base.model.ad.User;

/**
 * Dynamic OB Object which supports full dynamic mapping without java members.
 * 
 * @author mtaal
 */

public class DynamicOBObject extends BaseOBObject implements Traceable, Identifiable {
  
  private Map<String, Object> data = new HashMap<String, Object>();
  
  @Override
  public void set(String propertyName, Object value) {
    data.put(propertyName, value);
  }
  
  @Override
  public Object get(String propertyName) {
    return data.get(propertyName);
  }
  
  public boolean isNew() {
    return getId() == null;
  }
  
  public boolean isActive() {
    return (Boolean) get("active");
  }
  
  public void setActive(boolean active) {
    set("active", active);
  }
  
  @Override
  public String getId() {
    return (String) get("id");
  }
  
  public void setId(String id) {
    set("id", id);
  }
  
  public User getUpdatedby() {
    return (User) get("updatedby");
  }
  
  public void setUpdatedby(User updatedby) {
    set("updatedby", updatedby);
  }
  
  public Date getUpdated() {
    return (Date) get("updated");
  }
  
  public void setUpdated(Date updated) {
    set("updated", updated);
  }
  
  public User getCreatedby() {
    return (User) get("createdby");
  }
  
  public void setCreatedby(User createdby) {
    set("createdby", createdby);
  }
  
  public Date getCreated() {
    return (Date) get("created");
  }
  
  public void setCreated(Date created) {
    set("created", created);
  }
  
  @Override
  public String getEntityName() {
    return (String) get("entityName");
  }
  
  public void setEntityName(String entityName) {
    set("entityName", entityName);
  }
  
  public Map<String, Object> getData() {
    return data;
  }
  
  @Override
  public String getIdentifier() {
    return IdentifierProvider.getInstance().getIdentifier(this);
  }
}