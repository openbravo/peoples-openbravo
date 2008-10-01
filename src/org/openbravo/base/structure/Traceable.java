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

import org.openbravo.base.model.ad.User;

/**
 * An interface modeling traced open bravo objects.
 * 
 * @author mtaal
 */

public interface Traceable {
  public User getCreatedby();
  
  public void setCreatedby(User user);
  
  public Date getCreated();
  
  public void setCreated(Date date);
  
  public User getUpdatedby();
  
  public void setUpdatedby(User user);
  
  public Date getUpdated();
  
  public void setUpdated(Date date);
}