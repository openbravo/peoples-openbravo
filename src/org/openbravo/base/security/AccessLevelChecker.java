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

package org.openbravo.base.security;

import org.openbravo.base.exception.OBSecurityException;

/**
 * Enforces that certain tables in the system only contain records with the
 * correct client and organisation. The accesslevel of the table is used for
 * this.
 * 
 * System tables may only contain objects with Client id '0' and organisation id
 * '0' (=* organisation)
 * 
 * System/Client tables may contain objects from any client but only
 * organisations with id '0'
 * 
 * Organisation may not contain objects with client '0' or organisation '0'
 * (client != '0' and org != '0')
 * 
 * Client/Organisation may not contain objects with client '0', any organisation
 * is allowed
 * 
 * All this allows all client/organisations.
 * 
 * @author mtaal
 */

public class AccessLevelChecker {
  
  public static final AccessLevelChecker ALL = new AccessLevelChecker();
  
  public static final AccessLevelChecker SYSTEM = new AccessLevelChecker() {
    @Override
    public void checkAccessLevel(String entity, String clientId, String orgId) {
      failOnNonZeroClient(entity, clientId);
      failOnNonZeroOrg(entity, orgId);
    }
  };
  
  public static final AccessLevelChecker SYSTEM_CLIENT = new AccessLevelChecker() {
    @Override
    public void checkAccessLevel(String entity, String clientId, String orgId) {
      failOnNonZeroOrg(entity, orgId);
    }
  };
  
  public static final AccessLevelChecker ORGANISATION = new AccessLevelChecker() {
    @Override
    public void checkAccessLevel(String entity, String clientId, String orgId) {
      failOnZeroClient(entity, clientId);
      failOnZeroOrg(entity, orgId);
    }
  };
  
  public static final AccessLevelChecker CLIENT_ORGANISATION = new AccessLevelChecker() {
    @Override
    public void checkAccessLevel(String entity, String clientId, String orgId) {
      failOnZeroClient(entity, clientId);
    }
  };
  
  // default allways all
  public void checkAccessLevel(String entity, String clientId, String orgId) {
  }
  
  protected void failOnZeroClient(String entity, String clientId) {
    if (clientId.equals("0")) {
      throw new OBSecurityException("Entity " + entity + " may not have instances with client 0");
    }
  }
  
  protected void failOnNonZeroClient(String entity, String clientId) {
    if (!clientId.equals("0")) {
      throw new OBSecurityException("Entity " + entity + " may only have instances with client 0");
    }
  }
  
  protected void failOnZeroOrg(String entity, String orgId) {
    if (orgId.equals("0")) {
      throw new OBSecurityException("Entity " + entity + " may not have instances with organisation *");
    }
  }
  
  protected void failOnNonZeroOrg(String entity, String orgId) {
    if (!orgId.equals("0")) {
      throw new OBSecurityException("Entity " + entity + " may only have instances with organisation *");
    }
  }
}