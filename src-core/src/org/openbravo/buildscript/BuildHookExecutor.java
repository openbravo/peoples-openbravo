/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at https://www.openbravo.com/legal/license.html
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License.
 * The Original Code is Openbravo ERP.
 * The Initial Developer of the Original Code is Openbravo SLU 
 * All portions are Copyright (C) 2020 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.buildscript;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tools.ant.BuildException;
import org.openbravo.base.AntExecutor;

/**
 * Allows to execute post build scripts defined in external modules as ant tasks.
 *
 * <p>
 * It iterates over all modules looking for a {@code build.xml} file in their root directory. If
 * present, it checks whether a {@code postBuild} target is in that build file, if so it gets
 * executed.
 *
 * <p>
 * Build hooks are executed after {@code postsrc} target in the build flows, this is once all the
 * code is compiled and JavaScript files are deployed to {@code WebContent} directory.
 */
public class BuildHookExecutor {
  private static final Logger log = LogManager.getLogger();

  private Path modulesPath;

  private BuildHookExecutor(Path modulesPath) {
    this.modulesPath = modulesPath;
  }

  /** Entry point for BuildHookExecutor which is executed from ant build */
  public static void main(String[] args) throws Exception {
    String modulesPathArg = args[0];
    Path modulesPath = Paths.get(modulesPathArg);

    log.debug("Looking for hooks in {}", modulesPath);

    BuildHookExecutor executor = new BuildHookExecutor(modulesPath);
    executor.executeHooks();
  }

  private void executeHooks() throws Exception {
    List<Path> buildFiles = getBuildFilesWithHooks();

    if (buildFiles.isEmpty()) {
      return;
    }

    AntExecutor baseBuildXml = getAntExecutor(modulesPath.getParent().resolve("build.xml"));

    for (Path buildXml : buildFiles) {
      log.info("Executing build hook from module {}", buildXml.getParent().getFileName());
      AntExecutor executor = getAntExecutor(buildXml);
      executor.inheritPropertiesFrom(baseBuildXml);
      executor.logOutput();
      executor.runTask("postBuild");

      // prints an empty line in stdout after each script execution
      System.out.println();
    }
  }

  private List<Path> getBuildFilesWithHooks() throws IOException {
    try (var files = Files.walk(modulesPath, 2)) {
      return files.filter(Files::isRegularFile)
          .filter(f -> "build.xml".equals(f.getFileName().toString()))
          .filter(this::hasHook)
          .collect(Collectors.toList());
    }
  }

  private boolean hasHook(Path buildXml) {
    try {
      return getAntExecutor(buildXml).hasTarget("postBuild");
    } catch (BuildException ignore) {
      // don't fail, just ignore this file
      log.warn("Error checking build file {}, it will be ignored", buildXml);
      log.debug("Error", ignore);
      return false;
    }
  }

  private AntExecutor getAntExecutor(Path buildXml) {
    try {
      return new AntExecutor(buildXml.toAbsolutePath().toString(),
          buildXml.getParent().toAbsolutePath().toString());
    } catch (Exception e) {
      throw new BuildException("Couldn't read build file " + buildXml, e);
    }
  }
}
