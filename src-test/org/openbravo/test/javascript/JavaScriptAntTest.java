/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.0  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License.
 * The Original Code is Openbravo ERP.
 * The Initial Developer of the Original Code is Openbravo SL
 * All portions are Copyright (C) 2009 Openbravo SL
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.test.javascript;

import java.io.File;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * @author iperdomo
 * 
 */
public class JavaScriptAntTest extends Task {
  private JavaScriptAPIChecker jsAPIChecker = null;
  private String apiDetailsPath = null;
  private String jsPath = null;

  public String getApiDetailsPath() {
    return apiDetailsPath;
  }

  public void setApiDetailsPath(String apiDetailsPath) {
    this.apiDetailsPath = apiDetailsPath;
  }

  public String getJsPath() {
    return jsPath;
  }

  public void setJsPath(String jsPath) {
    this.jsPath = jsPath;
  }

  @Override
  public void execute() throws BuildException {
    System.out.println("JS API details folder: " + this.apiDetailsPath);
    System.out.println("JS folder:" + this.jsPath);
    jsAPIChecker = new JavaScriptAPIChecker();
    jsAPIChecker.setDetailsFolder(new File(this.apiDetailsPath));
    jsAPIChecker.setJSFolder(new File(this.jsPath));
    jsAPIChecker.process();
    if (!jsAPIChecker.getAPIMap().isEmpty()) {
      throw new BuildException("API Map must be empty: " + jsAPIChecker.getAPIMap());
    }
  }
}
