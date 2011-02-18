/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.0  (the  "License"),  being   the  Mozilla   Public  License
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

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.model.ad.module.Module;

/**
 * The component representing the component in the
 * 
 * @author mtaal
 */
public class StaticResourceComponent extends BaseComponent {
  private static final Logger log = Logger.getLogger(StaticResourceComponent.class);

  // only do the jslint check once
  private static boolean staticJsLintCheckDone = false;

  private Boolean isAnyModuleInDevelopment;

  /**
   * @return returns this instance
   * @see org.openbravo.client.kernel.BaseComponent#getData()
   */
  public Object getData() {
    return this;
  }

  /*
   * Note: a tradeoff... this computation is fairly heavy as it reads the modules. So another choice
   * can be to always always return a unique string (System.currentTimeMillis) and let the static
   * resources be reloaded always. But then the StaticResources are always recomputed resulting in
   * an even heavier computation (as dependencies are taken into account).
   * 
   * @see org.openbravo.client.kernel.Component#getETag()
   */
  public String getETag() {
    final List<ComponentProvider> componentProviders = ComponentProviderRegistry.getInstance()
        .getComponentProviders();
    final OBQuery<Module> moduleQuery = OBDal.getInstance().createQuery(Module.class,
        " order by id");
    final StringBuilder result = new StringBuilder();
    for (Module module : moduleQuery.list()) {
      for (ComponentProvider provider : componentProviders) {
        if (provider.getGlobalResources() == null) {
          continue;
        }

        if (provider.getModule().getId().equals(module.getId())) {
          for (String resource : provider.getGlobalResources()) {
            if (!isDirectScriptResource(resource)) {
              final String versionParameters = provider.getVersionParameters(resource);
              result.append(versionParameters);
            } else if (module.isInDevelopment()) {
              // in case in development make unique for direct script resources also
              result.append("" + System.currentTimeMillis());
            }
          }
        }
      }
    }

    // compute the checksum which is used as the etag
    final String md5 = DigestUtils.md5Hex(result.toString());
    return md5;
  }

  @Override
  public String generate() {

    // determine if jslint checking should be done
    final boolean doJsLintCheck = !staticJsLintCheckDone && isThereAModuleInDevelopment();

    final StringBuilder sb = new StringBuilder();
    for (String resource : getStaticResources()) {
      if (isDirectScriptResource(resource)) {
        sb.append(replaceContextUrlParameter(resource) + "\n");
      } else {

        if (doJsLintCheck) {
          staticJsLintCheckDone = true;
          checkGlobalResource(resource);
        }

        String globalResource = resource;
        if (globalResource.toLowerCase().startsWith("http")) {
          // don't do any changes
        } else if (globalResource.startsWith("/") && getContextUrl().length() > 0) {
          globalResource = getContextUrl() + globalResource.substring(1);
        } else {
          globalResource = getContextUrl() + globalResource;
        }

        // note the document.write content must be divided up like this, if the document.write
        // contains a complete string like <script or </script> then the browser will execute them
        // directly and not the document.write, see here:
        // http://www.codehouse.com/javascript/articles/external/
        sb.append("document.write(\"<\" + \"script src='" + globalResource
            + "'><\" + \"/script>\");\n");
      }
    }
    return sb.toString();
  }

  private void checkGlobalResource(String resourcePath) {
    final String sourcePath = (String) OBPropertiesProvider.getInstance().getOpenbravoProperties()
        .get("source.path");
    if (sourcePath == null) {
      return;
    }
    // example:
    // web/org.openbravo.userinterface.selector/js/ob_selector_link_widget.js?_version=1.0.11&_language=192

    // strip the ? and the rest
    String resource = resourcePath;
    final int questionMarkIndex = resourcePath.indexOf("?");
    if (questionMarkIndex != -1) {
      resource = resourcePath.substring(0, questionMarkIndex);
    }

    // don't check the ones we don't understand....
    if (!resource.startsWith("web")) {
      return;
    }
    if (!resource.endsWith(".js")) {
      return;
    }
    // don't check the isomorphic sources
    if (resource.contains("/isomorphic/")) {
      return;
    }

    // split on the /
    final String[] segments = resource.split("/");

    if (segments.length <= 1) {
      return;
    }

    // the second segment should be the module name....
    final String moduleName = segments[1];
    final String directPath = sourcePath + "/modules/" + moduleName + "/" + resource;
    final File resourceFile = new File(directPath);
    if (!resourceFile.exists()) {
      return;
    }

    // got the file read it....
    try {
      final FileReader reader = new FileReader(resourceFile);
      StringBuilder source = new StringBuilder();
      int length = -1;
      char[] buffer = new char[5000];
      while ((length = reader.read(buffer)) != -1) {
        source.append(buffer, 0, length);
      }
      reader.close();
      final String checkResult = JSLintChecker.getInstance().check(resource, source.toString());
      if (checkResult != null) {
        log.error("JSLINT ERROR " + resource + ">>>\n" + checkResult);
      }
    } catch (IOException e) {
      throw new OBException(e);
    }
  }

  public String getId() {
    return KernelConstants.RESOURCE_COMPONENT_ID;
  }

  /**
   * @return all static resources needed by the application and placed in the top of the application
   *         page, in order based on module dependencies and using an unique version string to force
   *         client side reload or caching.
   */
  public List<String> getStaticResources() {
    final List<ComponentProvider> componentProviders = ComponentProviderRegistry.getInstance()
        .getComponentProviders();
    final List<Module> modules = KernelUtils.getInstance().getModulesOrderedByDependency();
    final List<String> result = new ArrayList<String>();
    for (Module module : modules) {
      for (ComponentProvider provider : componentProviders) {
        if (provider.getGlobalResources() == null) {
          continue;
        }

        if (provider.getModule().getId().equals(module.getId())) {
          for (String resource : provider.getGlobalResources()) {
            String localResource = resource;
            if (!isDirectScriptResource(resource)) {
              final String versionParameters = provider.getVersionParameters(localResource);
              if (resource.contains("?")) {
                localResource += "&" + versionParameters;
              } else {
                localResource += "?" + versionParameters;
              }
            }

            if (!result.contains(localResource)) {
              result.add(localResource);
            }
          }
        }
      }
    }
    return result;
  }

  public boolean isDirectScriptResource(String resource) {
    return resource.contains(KernelConstants.RESOURCE_STRING_TAG);
  }

  private String replaceContextUrlParameter(String resource) {
    final String contextUrl = getContextUrl();
    String repairedResource = resource;
    while (repairedResource.contains(KernelConstants.RESOURCE_CONTEXT_URL_PARAMETER)) {
      repairedResource = resource.replace(KernelConstants.RESOURCE_CONTEXT_URL_PARAMETER,
          contextUrl);
    }
    return repairedResource;
  }

  protected boolean isThereAModuleInDevelopment() {
    if (isAnyModuleInDevelopment != null) {
      return isAnyModuleInDevelopment;
    }
    final String whereClause = "inDevelopment=true and active=true";
    final OBQuery<Module> modules = OBDal.getInstance().createQuery(Module.class, whereClause);
    isAnyModuleInDevelopment = modules.count() > 0;
    return isAnyModuleInDevelopment;
  }

}
