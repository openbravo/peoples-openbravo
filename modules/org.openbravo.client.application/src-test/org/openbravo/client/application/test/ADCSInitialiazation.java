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
 * All portions are Copyright (C) 2017 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.client.application.test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.junit.Test;
import org.openbravo.base.weld.test.WeldBaseTest;
import org.openbravo.client.application.window.ApplicationDictionaryCachedStructures;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ADCSInitialiazation extends WeldBaseTest {
  private static final Logger log = LoggerFactory.getLogger(ADCSInitialiazation.class);

  @Inject
  ApplicationDictionaryCachedStructures adcs;

  List<Exception> exceptions = new ArrayList<>();

  @Test
  public void aDCSshouldBeCorrectlyInitialized() {
    assumeTrue("Cache can be used (no modules in development)", adcs.useCache());

    int maxThreads = 8;
    ExecutorService executor = Executors.newFixedThreadPool(maxThreads);
    for (int i = 0; i < maxThreads; i++) {
      executor.execute(new ADCSInitializator());
    }

    executor.shutdown();
    try {
      executor.awaitTermination(20L, TimeUnit.MINUTES);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    if (!exceptions.isEmpty()) {
      log.error("Executed with " + exceptions.size() + " exceptions");
      for (Exception e : exceptions) {
        log.error("-------------------");
        log.error("Exception", e);
        log.error("-------------------");
      }
    }
    assertThat("Exceptions while initializating ADCS", exceptions, is(empty()));
  }

  private class ADCSInitializator implements Runnable {
    @Override
    public void run() {
      setSystemAdministratorContext();
      try {
        adcs.eagerInitialization(false);
      } catch (Exception e) {
        synchronized (exceptions) {
          exceptions.add(e);
        }
        run();
      }
    }
  }
}
