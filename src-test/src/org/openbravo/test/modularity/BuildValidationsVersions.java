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
 * All portions are Copyright (C) 2016 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.test.modularity;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.openbravo.base.ExecutionLimits;
import org.openbravo.buildvalidation.BuildValidation;
import org.openbravo.modulescript.OpenbravoVersion;

/**
 * Test cases covering build validation executions when updating database regarding the original
 * version the module is being updated from.
 * 
 * @author inigo.sanchez
 * 
 */
@RunWith(Parameterized.class)
public class BuildValidationsVersions {

  /** current module version we are updating from */
  final static Map<String, OpenbravoVersion> modulesVersionMap;
  static {
    String currentCoreVersion = "3.0.10000";

    modulesVersionMap = new HashMap<String, OpenbravoVersion>();
    modulesVersionMap.put("0", new OpenbravoVersion(currentCoreVersion));
  }

  private String moduleId;
  private String fromVersion;
  private String toVersion;
  private boolean onInstall;
  private boolean shouldExecute;

  /**
   * @param moduleId
   *          UUID of the module to establish the dependency with
   * @param fromVersion
   *          defines the first module version of the execution dependency
   * @param toVersion
   *          defines the last module version of the execution dependency
   * @param onInstall
   *          flag to indicate if the buildValidation should be executed on install
   * @param shouldExecute
   *          flag to indicate the final result, i.e., if the buildValidation should be executed or
   *          not
   * @param description
   *          description for the test case
   */
  public BuildValidationsVersions(String moduleId, String fromVersion, String toVersion,
      boolean onInstall, boolean shouldExecute, String description) {
    this.moduleId = moduleId;
    this.fromVersion = fromVersion;
    this.toVersion = toVersion;
    this.onInstall = onInstall;
    this.shouldExecute = shouldExecute;
  }

  @Parameters(name = "{index}: ''{5}'' -- moduleId: {0} - version limits: [{1}-{2}] - on install: {3} - should execute: {4}")
  public static Collection<Object[]> data() {
    return Arrays
        .asList(new Object[][] {

            // boundaries smaller than currentVersion
            { "0", "3.0.8000", "3.0.9000", false, false,
                "Should not be executed. Update from 3.0.10000." },
            { "0", null, "3.0.9000", false, false, "Should not be executed. Update from 3.0.10000." },
            { "0", "3.0.8000", null, false, true, "Should be executed. Update from 3.0.10000." },

            // boundaries contain currentVersion
            { "0", "3.0.9000", "3.0.11000", false, true,
                "Should be executed. Update from 3.0.10000." },
            { "0", null, "3.0.11000", false, true, "Should be executed. Update from 3.0.10000." },
            { "0", "3.0.9000", null, false, true, "Should be executed. Update from 3.0.10000." },

            // boundaries bigger than currentVersion
            { "0", "3.0.11000", "3.0.12000", false, false,
                "Should not be executed. Update from 3.0.10000." },
            { "0", null, "3.0.12000", false, true, "Should be executed. Update from 3.0.10000." },
            { "0", "3.0.11000", null, false, false,
                "Should not be executed. Update from 3.0.10000." },

            // Same currentVersion than versions
            { "0", "3.0.10000", "3.0.11000", false, false,
                "Should not be executed. Update from 3.0.10000." },
            { "0", "3.0.9000", "3.0.10000", false, false,
                "Should not be executed. Update from 3.0.10000." },
            { "0", null, "3.0.10000", false, false,
                "Should not be executed. Update from 3.0.10000." },
            { "0", "3.0.10000", null, false, false,
                "Should not be executed. Update from 3.0.10000." },

            // Force onInstall cases
            { "NEW", null, null, true, true, "New module should be executed on install." },
            { "NEW", null, null, false, false, "New module should not be executed on install." },

            // Incorrect boundaries definition
            { "0", "3.0.9000", "3.0.8000", true, false,
                "Should not be executed. Incorrect definition" } });
  }

  /** Executes the buildValidation with current version boundaries */
  @Test
  public void buildValidationIsExecutedBasedOnVersionLimits() {
    FakeBuildValidation ms = new FakeBuildValidation();
    ms.preExecute(modulesVersionMap);
    assertThat("BuildValidation was executed", ms.wasExecuted, is(shouldExecute));
  }

  /** Fake buildValidation with version limits, it simply flags when it is executed */
  public class FakeBuildValidation extends BuildValidation {
    /** flag set when the script has been executed */
    boolean wasExecuted = false;

    @Override
    public List<String> execute() {
      wasExecuted = true;
      return null;
    }

    @Override
    public ExecutionLimits getBuildValidationLimits() {
      if (moduleId == null) {
        return null;
      }
      return new ExecutionLimits(moduleId, //
          fromVersion == null ? null : new OpenbravoVersion(fromVersion),//
          toVersion == null ? null : new OpenbravoVersion(toVersion));
    }

    @Override
    public boolean executeOnInstall() {
      return onInstall;
    }
  }
}
