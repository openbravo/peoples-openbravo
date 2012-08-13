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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import javax.enterprise.context.RequestScoped;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.openbravo.client.kernel.BaseTemplateComponent;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.client.kernel.Template;
import org.openbravo.dal.service.OBDal;

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
  private List<String> fileList = null;

  @Override
  protected Template getComponentTemplate() {
    return OBDal.getInstance().get(Template.class, TEMPLATE_ID);
  }

  public String getVersion() {

    if (version != null) {
      return version;
    }

    final StringBuffer sb = new StringBuffer();

    for (final String file : getFileList()) {
      final String ext = file.substring(file.length() - 3);
      if (ext.contains("png")) {
        continue; // skip images
      }

      try {
        final String content = FileUtils.readFileToString(new File(file), "UTF-8");
        sb.append(content);
      } catch (IOException e) {
        log.error("Error reading file: " + e.getMessage(), e);
      }
    }

    if (sb.toString().equals("")) {
      sb.append(System.currentTimeMillis()); // fall-back version
    }

    version = DigestUtils.md5Hex(sb.toString());

    return version;
  }

  public String getNetwork() {
    return "*";
  }

  public List<String> getCache() {
    final List<String> resources = new ArrayList<String>();
    final String relativePath = PATH_PREFIX + getModulePackageName();

    for (final String f : getFileList()) {
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

  private List<String> getFileList() {

    if (fileList != null) {
      return fileList;
    }

    final String[] extensions = { "js", "css", "less", "png" };
    final String relativePath = PATH_PREFIX + getModulePackageName();

    fileList = new ArrayList<String>();

    final File directory = new File(RequestContext.getServletContext().getRealPath(relativePath));

    final Iterator<File> it = FileUtils.iterateFiles(directory, extensions, true);

    while (it.hasNext()) {
      final File f = (File) it.next();
      fileList.add(f.getPath());
    }
    return fileList;
  }

  public String getFileName() {
    return "";
  }

  @Override
  public boolean bypassAuthentication() {
    return true;
  }

}
