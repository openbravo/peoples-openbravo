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
 * All portions are Copyright (C) 2010 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

isc.ClassFactory.defineClass('OBGrid', isc.ListGrid);

isc.OBGrid.addProperties({
  exportData: function(requestProperties, additionalProperties) {
    // var criteria = this.getCriteria();
    var dsURL = this.dataSource.dataURL;
    var data = {
        _dataSource: this.dataSource.ID,
        _operationType: 'fetch',
        exportFormat: 'csv',
        exportToFile: (requestProperties
                       && requestProperties.params
                       && requestProperties.params.exportToFile)
    };
    isc.addProperties(data, additionalProperties);
    
    OB.Utilities.postThroughHiddenFrame(dsURL, data);
  }
});

isc.ClassFactory.defineClass('OBGridHeaderImgButton', isc.ImgButton);