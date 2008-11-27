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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.provider.OBSingleton;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.dal.xml.EntityXMLConverter;
import org.openbravo.model.ad.utility.DataSet;
import org.openbravo.model.ad.utility.DataSetTable;

/**
 * Exports business objects to XML on the basis of Datasets, DataSetTables and
 * DataSetColumns.
 * 
 * @see DataSetService
 * @see EntityXMLConverter
 * 
 * @author Martin Taal
 */
public class DataExportService implements OBSingleton {
    private static final Logger log = Logger.getLogger(DataExportService.class);

    private static DataExportService instance;

    /**
     * Returns the current singleton instance of the DataExportService.
     * 
     * @return the DataExportService instance
     */
    public static DataExportService getInstance() {
        if (instance == null) {
            instance = OBProvider.getInstance().get(DataExportService.class);
        }
        return instance;
    }

    /**
     * Makes it possible to set a specific DataExportService instance which will
     * be used by the rest of the Openbravo system.
     * 
     * @param instance
     *            the DataExportService instance used by the
     */
    public static void setInstance(DataExportService instance) {
        DataExportService.instance = instance;
    }

    /**
     * Export the data of a specific dataSet to XML. If the dataset is empty
     * then a null value is returned.
     * 
     * @param dataSet
     *            the dataset to export
     * @return the XML string containing the data of the dataset
     */
    public String exportDataSetToXML(DataSet dataSet) {
        return exportDataSetToXML(dataSet, null);
    }

    /**
     * Export the data of a specific dataSet to XML. If the dataset is empty
     * then a null value is returned.
     * 
     * @param dataSet
     *            the dataset to export
     * @param the
     *            moduleId is used as a parameter in where clauses of the
     *            DataSetTable and is used to set the module id in the
     *            AD_REF_DATA_LOADED table
     * @return the XML string containing the data of the dataset
     */
    public String exportDataSetToXML(DataSet dataSet, String moduleId) {
        return exportDataSetToXML(dataSet, moduleId, false,
                new HashMap<String, Object>());
    }

    /**
     * Exports data of a client. The main difference with the standard dataset
     * export is that also references to client and organizations are exported.
     * 
     * @param dataSet
     *            the DataSetTables of this dataSet will be exported
     * @param moduleId
     *            the moduleId is used in the where clause of dataset tables
     * @return the xml string, the resulting xml from the export, can be null if
     *         nothing is exported
     */
    public String exportClientToXML(DataSet dataSet, String moduleId,
            Map<String, Object> parameters) {
        return exportDataSetToXML(dataSet, moduleId, true, parameters);
    }

    // note returns null if nothing has been generated
    private String exportDataSetToXML(DataSet dataSet, String moduleId,
            boolean exportClientOrganizationReferences,
            Map<String, Object> parameters) {
        log.debug("Exporting dataset " + dataSet.getName());

        final EntityXMLConverter exc = EntityXMLConverter.newInstance();
        exc.setOptionIncludeReferenced(true);
        exc
                .setOptionExportClientOrganizationReferences(exportClientOrganizationReferences);
        final List<DataSetTable> dts = dataSet.getDataSetTableList();
        boolean generatedXML = false;
        for (final DataSetTable dt : dts) {
            final Boolean isbo = dt.isBusinessObject();
            exc.setOptionIncludeChildren(isbo != null && isbo.booleanValue());
            final List<BaseOBObject> list = DataSetService.getInstance()
                    .getExportableObjects(dt, moduleId, parameters);
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
