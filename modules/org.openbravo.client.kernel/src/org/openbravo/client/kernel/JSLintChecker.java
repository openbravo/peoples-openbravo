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
 * All portions are Copyright (C) 2010 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.client.kernel;

import java.io.IOException;
import java.util.List;

import org.openbravo.base.provider.OBProvider;

import com.googlecode.jslint4java.Issue;
import com.googlecode.jslint4java.JSLint;
import com.googlecode.jslint4java.JSLintBuilder;
import com.googlecode.jslint4java.JSLintResult;
import com.googlecode.jslint4java.Option;

/**
 * In its base form calls {@link Component#generate()}, more advanced features will postprocess the
 * result from the component.
 * 
 * @author mtaal
 */
public class JSLintChecker {

  private static JSLintChecker instance;

  public static synchronized JSLintChecker getInstance() {
    if (instance == null) {
      instance = OBProvider.getInstance().get(JSLintChecker.class);
    }
    return instance;
  }

  public static synchronized void setInstance(JSLintChecker instance) {
    JSLintChecker.instance = instance;
  }

  public String check(String componentIdentifier, String javascript) {
    try {
      JSLint jsLint = new JSLintBuilder().fromDefault();
      jsLint.addOption(Option.BROWSER);
      jsLint.addOption(Option.EQEQEQ);
      jsLint.addOption(Option.EVIL);
      jsLint.addOption(Option.LAXBREAK);

      final JSLintResult lintResult = jsLint.lint("0", javascript);
      final List<Issue> issues = lintResult.getIssues();
      if (issues.isEmpty()) {
        return null;
      }
      final StringBuilder sb = new StringBuilder();
      if (issues.size() > 0) {
        sb.append(">>>>>>> Issues found in javascript <<<<<<<<<\n");
        sb.append(javascript);
        sb.append(">>>>>>> Issues <<<<<<<<<\n");
      }
      for (Issue issue : issues) {
        sb.append(componentIdentifier + ":" + issue.getLine() + ":" + issue.getCharacter() + ": "
            + issue.getReason() + "\n");
      }
      return sb.toString();
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

}
