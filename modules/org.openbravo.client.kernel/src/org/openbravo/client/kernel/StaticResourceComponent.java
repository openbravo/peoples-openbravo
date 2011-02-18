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

import java.util.ArrayList;
import java.util.List;

import org.openbravo.model.ad.module.Module;

/**
 * The component representing the component in the
 * 
 * @author mtaal
 */
public class StaticResourceComponent extends BaseComponent {

  /**
   * @return returns this instance
   * @see org.openbravo.client.kernel.BaseComponent#getData()
   */
  public Object getData() {
    return this;
  }

  @Override
  public String generate() {
    final StringBuilder sb = new StringBuilder();
    for (String resource : getStaticResources()) {
      if (isDirectScriptResource(resource)) {
        sb.append(replaceContextUrlParameter(resource) + "\n");
      } else {
        String globalResource = resource;
        if (globalResource.startsWith("/") && getContextUrl().length() > 0) {
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
        final String versionParameters = KernelUtils.getInstance().getVersionParameters(module);

        if (provider.getModule().getId().equals(module.getId())) {
          for (String resource : provider.getGlobalResources()) {
            String localResource = resource;
            if (!isDirectScriptResource(resource)) {
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
}
