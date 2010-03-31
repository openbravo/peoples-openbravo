package org.openbravo.buildvalidation;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class BuildValidationHandler {
  private static final Logger log4j = Logger.getLogger(BuildValidationHandler.class);

  private static File basedir;
  private static String module;

  public static void main(String[] args) {
    basedir = new File(args[0]);
    module = args[1];
    PropertyConfigurator.configure("log4j.lcf");
    String errorMessage = "";
    List<String> classes = new ArrayList<String>();
    ArrayList<File> modFolders = new ArrayList<File>();
    if (module != null && !module.equals("%")) {
      String[] javapackages = module.split(",");
      for (String javapackage : javapackages) {
        File moduleFolder = new File(basedir, "modules/" + javapackage);
        modFolders.add(moduleFolder);
      }
      Collections.sort(modFolders);
    } else {
      File coreBuildFolder = new File(basedir, "src-util/buildvalidation/build/classes");
      readClassFiles(classes, coreBuildFolder);
      File moduleFolder = new File(basedir, "modules");
      for (File f : moduleFolder.listFiles()) {
        modFolders.add(f);
      }
      Collections.sort(modFolders);
    }
    for (File modFolder : modFolders) {
      if (modFolder.isDirectory()) {
        File validationFolder = new File(modFolder, "build/classes");
        if (validationFolder.exists()) {
          readClassFiles(classes, validationFolder);
        }
      }
    }
    for (String s : classes) {
      ArrayList<String> errors = new ArrayList<String>();
      try {
        Class<?> myClass = Class.forName(s);
        if (myClass.getGenericSuperclass().equals(
            Class.forName("org.openbravo.buildvalidation.BuildValidation"))) {
          Object instance = myClass.newInstance();
          log4j.info("Executing build validation: " + s);
          errors = callExecute(myClass, instance);
          for (String error : errors) {
            errorMessage += error + "\n";
          }
        }
      } catch (Exception e) {
        log4j.info("Error executing build-validation: " + s, e);
        log4j.error("The build validation " + s + " couldn't be properly executed" + e);
        System.exit(1);
      }
      if (errors.size() > 0) {
        log4j.error(errorMessage);
        log4j
            .error("The build validation failed. The system hasn't been modified. Fix the problems described in the validation messages (either by uninstalling the affected modules, or by fixing the problems the validation found), and then start the build again.");
        System.exit(1);
      }
    }
  }

  @SuppressWarnings("unchecked")
  private static ArrayList<String> callExecute(Class<?> myClass, Object instance)
      throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
    return (ArrayList<String>) myClass.getMethod("execute", new Class[0]).invoke(instance,
        new Object[0]);
  }

  private static void readClassFiles(List<String> coreClasses, File file) {
    ArrayList<String> newClasses = new ArrayList<String>();
    readClassFilesExt(newClasses, file);
    Collections.sort(newClasses);
    coreClasses.addAll(newClasses);
  }

  private static void readClassFilesExt(List<String> coreClasses, File file) {
    if (!file.exists()) {
      return;
    }
    if (file.isDirectory()) {
      File[] files = file.listFiles();
      for (File f : files) {
        readClassFiles(coreClasses, f);
      }
    } else {
      if (file.getAbsolutePath().endsWith(".class")) {
        String fileName = file.getAbsolutePath();
        fileName = fileName.split("build" + File.separatorChar + "classes" + File.separatorChar)[1];
        coreClasses.add(fileName.replace(".class", "").replace(File.separatorChar, '.'));
      }
    }
  }

  public File getBasedir() {
    return basedir;
  }

  public void setBasedir(File basedir) {
    this.basedir = basedir;
  }

  public String getModule() {
    return module;
  }

  public void setModule(String module) {
    this.module = module;
  }

}
