/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use. this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License.
 * The Original Code is Openbravo ERP.
 * The Initial Developer of the Original Code is Openbravo SLU
 * All portions are Copyright (C) 2021 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

//== OBProcessFileUpload ==
//This class is used to upload files to a process definition
isc.ClassFactory.defineClass('OBProcessFileUpload', isc.FileItem);

isc.OBProcessFileUpload.addProperties({
  multiple: false, // Allows only one file per parameter
  setDisabled: function(disabled) {
    // this.setCanEdit(!disabled);
  },
  fileSizeIsAboveMax: function(fileItem) {
    const maxFileSize = OB.PropertyStore.get(
      'OBUIAPP_ProcessFileUploadMaxSize'
    );

    return maxFileSize && fileItem && fileItem.size / 1000000 > maxFileSize;
  },
  validators: [
    {
      type: 'custom',
      condition: function(item) {
        const fileItem = item.form
          .getItem(item.name)
          .editForm.getItem(0)
          .getElement().files[0];

        if (item.fileSizeIsAboveMax(fileItem)) {
          item.view.messageBar.setMessage(
            isc.OBMessageBar.TYPE_ERROR,
            null,
            OB.I18N.getLabel('OBUIAPP_ProcessFileMaxSizeExceeded', [
              fileItem.name,
              OB.PropertyStore.get('OBUIAPP_ProcessFileUploadMaxSize')
            ])
          );
          return false;
        }

        return true;
      }
    }
  ]
});
