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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.openbravo.base.util.Check;

/**
 * Maintains a list of template processors by template language.
 * 
 * @author mtaal
 */
public class TemplateProcessorRegistry {
  private static final Logger log = Logger.getLogger(TemplateProcessorRegistry.class);

  private static TemplateProcessorRegistry instance = new TemplateProcessorRegistry();

  public static synchronized TemplateProcessorRegistry getInstance() {
    if (instance == null) {
      instance = new TemplateProcessorRegistry();
    }
    return instance;
  }

  public static synchronized void setInstance(TemplateProcessorRegistry instance) {
    TemplateProcessorRegistry.instance = instance;
  }

  private Map<String, TemplateProcessor> registry = new ConcurrentHashMap<String, TemplateProcessor>();

  /**
   * @param name
   *          the name of the {@link ComponentProvider}
   * @return true if there is a template processor for the language, false otherwise.
   */
  public synchronized boolean isTemplateProcessorPresent(String language) {
    return registry.containsKey(language);
  }

  /**
   * Returns a template processor for the language. Template processors are registered using the
   * language they support.
   * 
   * @param name
   *          the name of the {@link ComponentProvider}
   * @return an instance of {@link ComponentProvider} or an exception if not found
   */
  public synchronized TemplateProcessor getTemplateProcessor(String language) {
    final TemplateProcessor templateProcessor = registry.get(language);
    Check.isNotNull(templateProcessor, "No template provider found using name " + language);
    return templateProcessor;
  }

  /**
   * Registers a new component provider. An error is logged if there is already a component provider
   * for that name (@link {@link ComponentProvider#getName()}.
   * 
   * @param componentProvider
   */
  public synchronized void registerTemplateProcessor(TemplateProcessor templateProcessor) {
    if (registry.get(templateProcessor.getTemplateLanguage()) != null) {
      log.error("There is already a component provider registered with the name "
          + templateProcessor.getTemplateLanguage() + " its class "
          + registry.get(templateProcessor.getClass().getName()));
    }
    registry.put(templateProcessor.getTemplateLanguage(), templateProcessor);
  }

  /**
   * Clears the cache of all registered template processors.
   */
  public synchronized void clearCache() {
    for (TemplateProcessor templateProcessor : registry.values()) {
      templateProcessor.clearCache();
    }
  }
}
