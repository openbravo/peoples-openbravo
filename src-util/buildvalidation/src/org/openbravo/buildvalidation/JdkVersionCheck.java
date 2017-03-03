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
 * All portions are Copyright (C) 2016-2017 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.buildvalidation;

import java.util.ArrayList;
import java.util.List;

import org.openbravo.base.ExecutionLimits;
import org.openbravo.modulescript.OpenbravoVersion;

public class JdkVersionCheck extends BuildValidation {
  private static final int JAVA7_MAJOR_VERSION = 51;

  @Override
  public List<String> execute() {
    ArrayList<String> errors = new ArrayList<String>();
    try {
      checkJdkVersion(errors);
      return errors;
    } catch (Exception e) {
      return handleError(e);
    }
  }

  private void checkJdkVersion(ArrayList<String> errors) {
    int majorJavaVersion = new Double(getJavaMajorVersion()).intValue();
    if (majorJavaVersion < JAVA7_MAJOR_VERSION) {
      errors.add("Openbravo requires Java 7 (1.7) or higher to work.");
      errors.add("Current Java version (" + getJavaSpecificationVersion() + ") is not supported.");
    }
  }

  private String getJavaMajorVersion() {
    return System.getProperty("java.class.version"); // Java class format version number
  }

  private String getJavaSpecificationVersion() {
    return System.getProperty("java.specification.version"); // Java Runtime Environment
                                                             // specification version
  }
}