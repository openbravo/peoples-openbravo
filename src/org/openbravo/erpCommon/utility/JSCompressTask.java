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
 * All portions are Copyright (C) 2020 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.utility;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.openbravo.client.kernel.JSCompressor;

/**
 * Ant task that compresses JavaScript using JSMin
 */
public class JSCompressTask extends Task {
  private List<FileSet> filesets = new ArrayList<>();
  private String outputDir;

  public void addFileset(FileSet fileset) {
    filesets.add(fileset);
  }

  public void setOutputDir(String outputDir) {
    this.outputDir = outputDir;
  }

  @Override
  public void execute() throws BuildException {
    verifyParameters();
    for (FileSet fileSet : filesets) {
      DirectoryScanner scanner = fileSet.getDirectoryScanner(getProject());
      File dir = scanner.getBasedir();
      String[] fileNames = scanner.getIncludedFiles();
      int totalCompressedFiles = 0;
      for (String fileName : fileNames) {
        try {
          if (fileName.endsWith(".js")) {
            compressJSFile(new File(dir, fileName), new File(outputDir, fileName));
            totalCompressedFiles++;
          }
        } catch (IOException ex) {
          log("Failed to compress file: " + fileName, ex, Project.MSG_ERR);
        }
      }
      log(String.format("Compressed %d files from %s directory to %s directory%n",
          totalCompressedFiles, fileSet.getDir().getPath(), outputDir));
    }
  }

  private void verifyParameters() {
    String errorMsg = "";

    if (outputDir == null || "".equals(outputDir)) {
      errorMsg += "Output directory is not specified\n";
    }

    if (!"".equals(errorMsg)) {
      throw new BuildException("Output directory is not specified");
    }
  }

  private void compressJSFile(File source, File dest) throws IOException {
    try (BufferedWriter out = new BufferedWriter(new FileWriter(dest))) {
      String fileContent = Files.readString(source.toPath());
      String compressedLine = JSCompressor.getInstance().compress(fileContent);
      out.write(compressedLine);
      out.flush();
    } catch (IOException ex) {
      throw new IOException("Failed to read/write JS file" + source, ex);
    }
  }
}
