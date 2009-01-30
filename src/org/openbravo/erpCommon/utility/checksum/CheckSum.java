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
 * The Initial Developer of the Original Code is Openbravo SL 
 * All portions are Copyright (C) 2009 Openbravo SL 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.utility.checksum;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.util.Properties;
import java.util.zip.CRC32;

public class CheckSum {
    private String obDir;
    private Properties properties;

    public CheckSum(String dir) {
        obDir = dir.replace("\\", "/");
        if (!obDir.endsWith("/"))
            obDir += "/";
        properties = new Properties();
        File propertiesFile = new File(obDir + "/config/checksums");
        if (propertiesFile.exists()) {
            try {
                properties.load(new FileInputStream(propertiesFile));
            } catch (Exception e) {
                // do nothing, just do not read properties
            }
        }
    }

    private void getCheckSum(CRC32 crc, File f) throws Exception {
        if (f.isDirectory()) {
            File[] list = f.listFiles(new FilenameFilter() {
                public boolean accept(File file, String s) {
                    return !s.equals(".svn");
                }
            });
            for (File element : list)
                getCheckSum(crc, element);
        } else {

            FileInputStream is = new FileInputStream(f);
            byte[] bytes = new byte[1024];
            int len = 0;

            // Checksum file directly
            while ((len = is.read(bytes)) >= 0) {
                crc.update(bytes, 0, len);
            }
            is.close();
        }
    }

    private String getCheckSum(String[] files) throws Exception {
        CRC32 crc = new CRC32();
        for (String fileName : files) {
            File file = new File(fileName);
            if (file.exists())
                getCheckSum(crc, file);
        }
        return Long.toHexString(crc.getValue());
    }

    private String[] getFiles(String type) {
        if (type.equals("db.structure")) {
            String rt[] = { obDir + "src-db/database/model" };
            return rt;
        } else if (type.equals("db.sourcedata")) {
            String rt[] = { obDir + "src-db/database/sourcedata" };
            return rt;
        } else if (type.equals("wad")) {
            String rt[] = { obDir + "src-wad/lib/openbravo-wad.jar" };
            return rt;
        } else {
            String rt[] = new String[0];
            return rt;
        }
    }

    private void saveCheckSum() throws Exception {
        FileOutputStream file = new FileOutputStream(new File(obDir
                + "/config/checksums"));
        properties.store(file, "Checksums for build tasks comparation");
    }

    private void calculateCheckSum(String type) {
        try {
            String[] files = getFiles(type);
            String checkSum = getCheckSum(files);
            properties.setProperty(type, checkSum);
            saveCheckSum();
        } catch (Exception e) {
            e.printStackTrace();
            // do nothing else
        }
    }

    public void calculateCheckSumDBStructure() {
        calculateCheckSum("db.structure");
    }

    public void calculateCheckSumDBSourceData() {
        calculateCheckSum("db.sourcedata");
    }

    public void calculateCheckSumWad() {
        calculateCheckSum("wad");
    }

    public String getCheckSumDBSTructure() {
        return properties.getProperty("db.structure", "0");
    }

    public String getCheckSumDBSourceData() {
        return properties.getProperty("db.sourcedata", "0");
    }

    public String getCheckSumWad() {
        return properties.getProperty("wad", "0");
    }

}
