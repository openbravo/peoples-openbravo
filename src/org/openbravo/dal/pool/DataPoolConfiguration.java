/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License.
 * The Original Code is Openbravo ERP.
 * The Initial Developer of the Original Code is Openbravo SLU
 * All portions are Copyright (C) 2022 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.dal.pool;

import java.util.Map;

/**
 * Interface used to define the database pool configurations for a specific type of data
 */
public interface DataPoolConfiguration {

  /**
   * Provides a map with all the configured values for an specific type of data
   *
   * @return configured values
   */
  public Map<String, String> getDataPoolSelection();

  /**
   * Provides the preference name of the default database pool for an specific type of data
   *
   * @return preference name
   */
  public String getPreferenceName();

  /**
   * Provides the name of the specific type of data implemented for this class
   *
   * @return data type name
   */
  public String getDataType();

}
