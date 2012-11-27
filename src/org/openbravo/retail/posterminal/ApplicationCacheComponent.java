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
 * All portions are Copyright (C) 2012 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.retail.posterminal;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import javax.enterprise.context.RequestScoped;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.openbravo.client.kernel.BaseTemplateComponent;
import org.openbravo.client.kernel.KernelUtils;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.client.kernel.Template;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.module.Module;

/**
 * 
 * @author iperdomo
 */

@RequestScoped
public class ApplicationCacheComponent extends BaseTemplateComponent {

  private static final String TEMPLATE_ID = "7A911850D921448EA5AC2E6F4D5FDE2D";
  private static final String PATH_PREFIX = "web/";
  private static final Logger log = Logger.getLogger(ApplicationCacheComponent.class);

  private String version = null;

  @Override
  protected Template getComponentTemplate() {
    return OBDal.getInstance().get(Template.class, TEMPLATE_ID);
  }

  public String getVersion() {

    if (version != null) {
      return version;
    }

    StringBuffer versionString = new StringBuffer();
    final List<Module> modules = KernelUtils.getInstance().getModulesOrderedByDependency();
    for (Module module : modules) {
      versionString.append(module.getVersion());
    }
    version = DigestUtils.md5Hex(versionString.toString());

    return version;
  }

  public String getNetwork() {
    return "*";
  }

  private List<String> transformPath(List<String> stringFileList) {
    final List<String> resources = new ArrayList<String>();
    final String relativePath = PATH_PREFIX + getModulePackageName();

    for (final String f : stringFileList) {
      final int pos = f.indexOf(relativePath);
      resources.add("../../" + f.substring(pos));
    }
    return resources;
  }

  @Override
  public String getContentType() {
    return "text/cache-manifest";
  }

  @Override
  public String getETag() {
    return UUID.randomUUID().toString();
  }

  @Override
  public boolean isJavaScriptComponent() {
    return false;
  }

  public List<String> getImageFileList() {
    final String[] extensions = { "png", "gif" };
    return transformPath(getFileList(extensions));
  }

  public List<String> getcssFileList() {
    final String[] extensions = { "css", "less" };
    return transformPath(getFileList(extensions));
  }

  private List<String> getFileList(String[] extensions) {

    final String relativePath = PATH_PREFIX + getModulePackageName();

    List<String> fileList = new ArrayList<String>();

    final File directory = new File(RequestContext.getServletContext().getRealPath(relativePath));

    final Iterator<File> it = FileUtils.iterateFiles(directory, extensions, true);

    while (it.hasNext()) {
      final File f = (File) it.next();
      fileList.add(f.getPath());
    }
    return fileList;
  }

  public String getFileName() {
    return OBPOSStaticResorcesComponent.getStaticResourceFileName();
  }

  public boolean getNotInDevelopment() {
    return !OBPOSStaticResorcesComponent.isDevelopment();
  }

  @Override
  public boolean bypassAuthentication() {
    return true;
  }

}
