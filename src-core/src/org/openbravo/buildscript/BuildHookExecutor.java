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

public class BuildHookExecutor {
  private static final Logger log = LogManager.getLogger();

  private Path modulesPath;

  public BuildHookExecutor(Path modulesPath) {
    this.modulesPath = modulesPath;
  }

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
          modulesPath.getParent().toAbsolutePath().toString());
    } catch (Exception e) {
      throw new BuildException("Couldn't read build file " + buildXml, e);
    }
  }
}
