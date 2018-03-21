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
 * All portions are Copyright (C) 2017-2018 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.test.reporting;

import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assume.assumeThat;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;

import org.hibernate.Query;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.client.application.report.ReportingUtils;
import org.openbravo.dal.service.OBDal;
import org.openbravo.test.base.OBBaseTest;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperReport;

/**
 * Compiles all jrxml templates present in the sources directory ensuring they can be compiled with
 * the current combination of jdk + ejc.
 * 
 * @author alostale
 *
 */
@RunWith(Parameterized.class)
public class AllJrxmlCompilation extends OBBaseTest {

  @Parameter(0)
  public Path report;
  @Parameter(1)
  public Path fileName;

  @Override
  protected boolean shouldMockServletContext() {
    return true;
  }

  @Test
  public void jrxmlShouldCompile() throws JRException {
    String reportPath = report.toString();
    // compile the report for the first time
    ReportingUtils.compileReport(reportPath);
    assumeThat("Has modules in development", hasModulesInDevelopment(), not(true));
    // launch the compilation again: result will be retrieved from cache
    JasperReport jasperReport = ReportingUtils.compileReport(reportPath);
    assertNotNull(jasperReport);
  }

  private Boolean hasModulesInDevelopment() {
    final Query indevelMods = OBDal.getInstance().getSession()
        .createQuery("select 1 from ADModule m where m.inDevelopment=true");
    indevelMods.setMaxResults(1);
    return indevelMods.list().size() > 0;
  }

  @Parameters(name = "{1}")
  public static Collection<Object[]> parameters() throws IOException {
    final Collection<Object[]> allJasperFiles = new ArrayList<>();
    allJasperFiles.addAll(getJrxmlTemplates("src"));
    allJasperFiles.addAll(getJrxmlTemplates("modules"));
    return allJasperFiles;
  }

  private static Collection<Object[]> getJrxmlTemplates(String dir) throws IOException {
    final Collection<Object[]> allJasperFiles = new ArrayList<>();

    final PathMatcher matcher = FileSystems.getDefault().getPathMatcher("regex:.*\\.jrxml");
    Path basePath = Paths.get(OBPropertiesProvider.getInstance().getOpenbravoProperties()
        .getProperty("source.path"), dir);
    Files.walkFileTree(basePath, new SimpleFileVisitor<Path>() {
      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        if (matcher.matches(file)) {
          allJasperFiles.add(new Object[] { file, file.getFileName() });
        }
        return FileVisitResult.CONTINUE;
      }
    });
    return allJasperFiles;
  }
}
