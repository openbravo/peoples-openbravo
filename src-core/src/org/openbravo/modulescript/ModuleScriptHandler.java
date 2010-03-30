package org.openbravo.modulescript;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

public class ModuleScriptHandler extends Task {

  private static Logger log4j = Logger.getLogger(ModuleScriptHandler.class);

  private File basedir;
  private String moduleJavaPackage;

  @Override
  public void execute() {
    List<String> classes = new ArrayList<String>();
    if (moduleJavaPackage != null) {
      // We will only be executing the ModuleScripts of a specific module
      File moduleDir = new File(basedir, "modules/" + moduleJavaPackage + "/build/classes");
      readClassFiles(classes, moduleDir);
    } else {
      File coreBuildFolder = new File(basedir, "src-util/modulescript/build/classes");
      readClassFiles(classes, coreBuildFolder);
      File moduleFolder = new File(basedir, "modules");
      File modFolders[] = moduleFolder.listFiles();
      for (File modFolder : modFolders) {
        if (modFolder.isDirectory()) {
          File validationFolder = new File(modFolder, "build/classes");
          if (validationFolder.exists()) {
            readClassFiles(classes, validationFolder);
          }
        }
      }
    }
    for (String s : classes) {
      try {
        Class<?> myClass = Class.forName(s);
        if (myClass.getGenericSuperclass().equals(
            Class.forName("org.openbravo.modulescript.ModuleScript"))) {
          Object instance = myClass.newInstance();
          callExecute(myClass, instance);
        }
      } catch (Exception e) {
        throw new BuildException("Execution of moduleScript " + s + "failed.");
      }
    }
  }

  @SuppressWarnings("unchecked")
  private ArrayList<String> callExecute(Class<?> myClass, Object instance)
      throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
    return (ArrayList<String>) myClass.getMethod("execute", new Class[0]).invoke(instance,
        new Object[0]);
  }

  private void readClassFiles(List<String> coreClasses, File file) {
    if (file.isDirectory()) {
      File[] files = file.listFiles();
      for (File f : files) {
        readClassFiles(coreClasses, f);
      }
    } else {
      String fileName = file.getAbsolutePath();
      System.out.println(fileName);
      fileName = fileName.split("build" + File.separatorChar + "classes" + File.separatorChar)[1];
      coreClasses.add(fileName.replace(".class", "").replace(File.separatorChar, '.'));
    }
  }

  public File getBasedir() {
    return basedir;
  }

  public void setBasedir(File basedir) {
    this.basedir = basedir;
  }

  public String getModuleJavaPackage() {
    return moduleJavaPackage;
  }

  public void setModuleJavaPackage(String moduleJavaPackage) {
    this.moduleJavaPackage = moduleJavaPackage;
  }

}
