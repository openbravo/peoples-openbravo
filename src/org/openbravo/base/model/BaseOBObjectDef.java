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
package org.openbravo.base.model;

/**
 * Defines the BaseOBObject interface, this interface has been introduced to
 * prevent cyclic references between generated code and model code which is used
 * by the generator. This interface only contains the minimally required methods
 * to prevent this cycle.
 * 
 * @author mtaal
 */

public interface BaseOBObjectDef {
  
  public Object get(String featureName);
  
  public void set(String featureName, Object value);
}