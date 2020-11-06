package org.openbravo.buildscript;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.openbravo.base.AntExecutor;

public class BuildHookExecutor {
  private Path modulesPath;

  public BuildHookExecutor(Path modulesPath) {
    this.modulesPath = modulesPath;
  }

  public static void main(String[] args) throws Exception {
    String modulesPathArg = args[0];
    Path modulesPath = Paths.get(modulesPathArg);
    System.out.println(modulesPath);

    BuildHookExecutor executor = new BuildHookExecutor(modulesPath);
    executor.executeHooks();

  }

  private void executeHooks() throws Exception {

    List<Path> buildFiles = getBuildFilesWithHooks();
    for (Path buildXml : buildFiles) {
      System.out.println("excuting " + buildXml);
      new AntExecutor(buildXml.toAbsolutePath().toString(),
          modulesPath.getParent().toAbsolutePath().toString()).runTask("postBuild");

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
      return new AntExecutor(buildXml.toAbsolutePath().toString(),
          modulesPath.getParent().toAbsolutePath().toString()).hasTarget("postBuild");
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }
}
