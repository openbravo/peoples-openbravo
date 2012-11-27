/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal;

import java.io.File;
import java.util.List;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.servlet.ServletContext;

import org.apache.commons.codec.digest.DigestUtils;
import org.openbravo.base.exception.OBException;
import org.openbravo.client.kernel.BaseComponent;
import org.openbravo.client.kernel.KernelConstants;
import org.openbravo.client.kernel.KernelUtils;
import org.openbravo.client.kernel.StaticResourceComponent;
import org.openbravo.model.ad.module.Module;
import org.openbravo.utils.FileUtility;

/**
 * This class replaces call to org.openbravo.client.kernel.StaticResourceComponent because the way
 * POS loads resources requires of $LAB
 * 
 * @author alostale
 * 
 */

public class OBPOSStaticResorcesComponent extends BaseComponent {
  private static final String GEN_TARGET_LOCATION = "web/js/gen";

  @Inject
  @Any
  private Instance<StaticResourceComponent> rc;

  public String generate() {
    String filePath = "";
    String tempFilePath;
    File finalFile;

    final ServletContext context = (ServletContext) getParameters().get(
        KernelConstants.SERVLET_CONTEXT);
    filePath = GEN_TARGET_LOCATION + "/" + getStaticResourceFileName() + ".js";
    if (!isDevelopment()) {
      finalFile = new File(context.getRealPath(filePath));
      if (finalFile.exists()) {
        return "$LAB.script('" + getContextUrl() + filePath + "');";
      }
    }
    StaticResourceComponent sr = rc.get();
    sr.setParameters(getParameters());
    tempFilePath = sr.getStaticResourceFileName();
    if (isDevelopment()) {
      filePath = GEN_TARGET_LOCATION + "/" + tempFilePath + ".js";
    } else {
      finalFile = new File(context.getRealPath(filePath));

      File tempFile = new File(
          context.getRealPath(GEN_TARGET_LOCATION + "/" + tempFilePath + ".js"));
      try {
        finalFile.createNewFile();
        FileUtility.copyFile(tempFile, finalFile);
      } catch (Exception e) {
        throw new OBException("There was a problem when generating the static resources file", e);
      }
    }

    return "$LAB.script('" + getContextUrl() + filePath + "');";
  }

  @Override
  public Object getData() {
    // TODO Auto-generated method stub
    return null;
  }

  public static String getStaticResourceFileName() {
    StringBuffer versionString = new StringBuffer();
    final List<Module> modules = KernelUtils.getInstance().getModulesOrderedByDependency();
    for (Module module : modules) {
      versionString.append(module.getVersion());
    }
    return DigestUtils.md5Hex(versionString.toString());
  }

  public static boolean isDevelopment() {
    for (Module module : KernelUtils.getInstance().getModulesOrderedByDependency()) {
      if (module.isInDevelopment()) {
        return true;
      }
    }
    return false;

  }

  @Override
  public boolean bypassAuthentication() {
    return true;
  }
}
