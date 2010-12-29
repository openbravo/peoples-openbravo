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
 * All portions are Copyright (C) 2010 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.client.kernel;

import java.io.File;
import java.util.List;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.servlet.ServletContext;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.openbravo.client.kernel.BaseComponentProvider.ComponentResource;
import org.openbravo.client.kernel.BaseComponentProvider.ComponentResource.ComponentResourceType;
import org.openbravo.model.ad.module.Module;

/**
 * Component which reads all stylesheets.
 * 
 * @author mtaal
 * @author iperdomo
 */
public class StyleSheetResourceComponent extends BaseComponent {
  private static final Logger log = Logger.getLogger(StyleSheetResourceComponent.class);

  @Inject
  @Any
  private Instance<ComponentProvider> componentProviders;

  /**
   * @return returns this instance
   * @see org.openbravo.client.kernel.BaseComponent#getData()
   */
  public Object getData() {
    return this;
  }

  @Override
  public String getContentType() {
    return KernelConstants.CSS_CONTENTTYPE;
  }

  public boolean isJavaScriptComponent() {
    return false;
  }

  @Override
  public String getETag() {
    final List<Module> modules = KernelUtils.getInstance().getModulesOrderedByDependency();
    final StringBuilder version = new StringBuilder();
    for (Module module : modules) {
      boolean hasStyleSheet = false;
      for (ComponentProvider provider : componentProviders) {
        final List<ComponentResource> resources = provider.getGlobalComponentResources();
        if (resources == null || resources.size() == 0) {
          continue;
        }

        if (provider.getModule().getId().equals(module.getId())) {
          for (ComponentResource resource : resources) {
            if (resource.getType() == ComponentResourceType.Stylesheet) {
              hasStyleSheet = true;
              break;
            }
          }
        }
      }
      if (hasStyleSheet) {
        version.append(KernelUtils.getInstance().getVersionParameters(module));
      }
    }
    // compute the md5 of the version string and return that
    return DigestUtils.md5Hex(version.toString());
  }

  @Override
  public String generate() {
    final List<Module> modules = KernelUtils.getInstance().getModulesOrderedByDependency();
    final ServletContext context = (ServletContext) getParameters().get(
        KernelConstants.SERVLET_CONTEXT);
    final StringBuffer sb = new StringBuffer();

    final boolean classicMode = !getParameters().containsKey(KernelConstants.MODE_PARAMETER)
        || !getParameters().get(KernelConstants.MODE_PARAMETER).equals(
            KernelConstants.MODE_PARAMETER_300);

    final String skinParam;
    if (classicMode) {
      skinParam = KernelConstants.SKIN_VERSION_CLASSIC;
    } else {
      skinParam = KernelConstants.SKIN_VERSION_300;
    }

    for (Module module : modules) {
      for (ComponentProvider provider : componentProviders) {
        final List<ComponentResource> resources = provider.getGlobalComponentResources();
        if (resources == null || resources.size() == 0) {
          continue;
        }

        if (provider.getModule().getId().equals(module.getId())) {
          for (ComponentResource resource : resources) {
            if (classicMode && !resource.isIncludeAlsoInClassicMode()) {
              continue;
            }
            if (!classicMode && !resource.isIncludeInNewUIMode()) {
              continue;
            }
            log.debug("Processing resource: " + resource);
            if (resource.getType() == ComponentResourceType.Stylesheet) {
              String resourcePath = resource.getPath();

              // Skin version handling
              if (resourcePath.contains(KernelConstants.SKIN_VERSION_PARAMETER)) {
                resourcePath = resourcePath.replaceAll(KernelConstants.SKIN_VERSION_PARAMETER,
                    skinParam);
              }

              try {
                final File file = new File(context.getRealPath(resourcePath));
                if (!file.exists() || !file.canRead()) {
                  log.error(file.getAbsolutePath() + " cannot be read");
                  continue;
                }
                String resourceContents = FileUtils.readFileToString(file);

                final int lastIndex = resourcePath.lastIndexOf("/");
                final String path = getContextUrl() + resourcePath.substring(0, lastIndex);

                // repair urls
                resourceContents = resourceContents.replace("url(./", "url(" + path + "/");
                resourceContents = resourceContents
                    .replace("url(images", "url(" + path + "/images");
                resourceContents = resourceContents.replace("url(\"images", "url(\"" + path
                    + "/images");
                resourceContents = resourceContents.replace("url('images", "url('" + path
                    + "/images");
                resourceContents = resourceContents.replace("url('./", "url('" + path + "/");
                resourceContents = resourceContents.replace("url(\"./", "url(\"" + path + "/");

                sb.append(resourceContents);
              } catch (Exception e) {
                log.error("Error reading file: " + resource, e);
              }
            }
          }
        }
      }
    }

    return sb.toString();
  }

  public String getId() {
    return KernelConstants.STYLE_SHEET_COMPONENT_ID;
  }
}
