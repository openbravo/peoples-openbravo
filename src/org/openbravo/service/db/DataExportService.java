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
 * All portions are Copyright (C) 2008 Openbravo SL 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.service.db;

import java.util.List;

import org.apache.log4j.Logger;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.provider.OBSingleton;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.dal.xml.EntityXMLConverter;
import org.openbravo.model.ad.utility.DataSet;
import org.openbravo.model.ad.utility.DataSetTable;

/**
 * Exports business objects using datasets, makes use of the dataSetService.
 * 
 * @author Martin Taal
 */
public class DataExportService implements OBSingleton {
    private static final Logger log = Logger.getLogger(DataExportService.class);

    private static DataExportService instance;

    public static DataExportService getInstance() {
        if (instance == null) {
            instance = OBProvider.getInstance().get(DataExportService.class);
        }
        return instance;
    }

    public static void setInstance(DataExportService instance) {
        DataExportService.instance = instance;
    }

    public String exportDataSetToXML(DataSet dataSet) {
        return exportDataSetToXML(dataSet, null);
    }

    // note returns null if nothing has been generated
    public String exportDataSetToXML(DataSet dataSet, String moduleId) {
        log.debug("Exporting dataset " + dataSet.getName());

        final EntityXMLConverter exc = EntityXMLConverter.newInstance();
        exc.setOptionIncludeReferenced(true);
        final List<DataSetTable> dts = dataSet.getDataSetTableList();
        boolean generatedXML = false;
        for (final DataSetTable dt : dts) {
            final Boolean isbo = dt.isBusinessObject();
            exc.setOptionIncludeChildren(isbo != null && isbo.booleanValue());
            final List<BaseOBObject> list = DataSetService.getInstance()
                    .getExportableObjects(dt, moduleId);
            if (list.size() > 0) {
                exc.process(list);
                generatedXML = true;
            }
        }
        if (!generatedXML) {
            return null;
        }
        return exc.getProcessResult();
    }
}
