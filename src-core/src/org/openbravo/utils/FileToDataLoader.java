/*
 ************************************************************************************
 * Copyright (C) 2001-2006 Openbravo S.L.
 * Licensed under the Apache Software License version 2.0
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to  in writing,  software  distributed
 * under the License is distributed  on  an  "AS IS"  BASIS,  WITHOUT  WARRANTIES  OR
 * CONDITIONS OF ANY KIND, either  express  or  implied.  See  the  License  for  the
 * specific language governing permissions and limitations under the License.
 ************************************************************************************
 */
package org.openbravo.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.openbravo.data.FieldProvider;
import org.openbravo.data.SetFieldProvider;
import org.openbravo.database.StandAloneConnection;

public class FileToDataLoader extends StandAloneConnection {

  static Logger log4j = Logger.getLogger(FileToDataLoader.class);

  public static FieldProvider[] getFileIntoData(File path, String FileName, SetFieldProvider data) {
    if (log4j.isDebugEnabled())
      log4j.debug("processing replace file: " + FileName);
    Vector<FieldProvider> vector = new Vector<FieldProvider>();
    FieldProvider[] newData = null;
    try {
      File file = new File(path, FileName);
      if (!file.exists()) {
        log4j.error("Unknown file: " + path + "\\" + FileName);
        return null;
      }
      BufferedReader fileBuffer = new BufferedReader(new FileReader(file));

      String nextLine = fileBuffer.readLine();
      while (nextLine != null) {
        FieldProvider fieldProvider = data.setFieldProvider(nextLine);
        if (fieldProvider != null)
          vector.addElement(fieldProvider);
        nextLine = fileBuffer.readLine();
      }
      fileBuffer.close();
      newData = new FieldProvider[vector.size()];
      vector.copyInto(newData);
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
    return newData;
  }
}
