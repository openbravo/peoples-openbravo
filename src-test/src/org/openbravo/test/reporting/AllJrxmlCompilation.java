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
package org.openbravo.test.reporting;

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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.openbravo.base.session.OBPropertiesProvider;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

/**
 * Compiles all jrxml templates present in the sources directory ensuring they can be compiled with
 * the current combination of jdk + ejc.
 * 
 * @author alostale
 *
 */
@RunWith(Parameterized.class)
public class AllJrxmlCompilation {

  @Parameter(0)
  public Path report;
  @Parameter(1)
  public Path fileName;

  @Test
  public void jrxmlShouldCompile() throws JRException {
    JasperDesign jasperDesign = JRXmlLoader.load(report.toFile());
    JasperCompileManager.compileReport(jasperDesign);
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
    Path basePath = Paths.get(
        OBPropertiesProvider.getInstance().getOpenbravoProperties().getProperty("source.path"),
        dir);
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
