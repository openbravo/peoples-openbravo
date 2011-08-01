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
 * All portions are Copyright (C) 2011 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s): ___________
 ************************************************************************
 */

// = Personalization Utilities =
// Contains utility methods for window personalization, for example
// to create the datastructure used by the formbuilder from a form
// or apply the datastructure to a form.
OB.Personalization = {
  STATUSBAR_GROUPNAME : '_statusBar',
  MAIN_GROUPNAME : '_main'
};

// ** {{{OB.Personalization.getDataStructureFromForm}}} **
// Creates the data structure used by the form builder and stored
// as personalized form information in the UI personaliz
OB.Personalization.getDataStructureFromForm = function(form) {
  // just return the personalization data which was used on the 
  // form, we can not reconstruct it completely from the form fields
  // as we don't store extra personalization data in the form fields
  // them selve
  if (form.view && form.view.personalizationData) {
    return form.view.personalizationData;
  }
  
  var fld, j, record, i, k, addedToStatusBar;
  dataFields = [];

  dataFields.push({
    isFolder : true,
    title : OB.I18N.getLabel('OBUIAPP_Personalization_StatusBar_Group'),
    name : OB.Personalization.STATUSBAR_GROUPNAME,
    isSection : true,
    canSelect: false,
    _canEdit : false
  });

//  dataFields.push({
//    isFolder : true,
//    title : OB.I18N.getLabel('OBUIAPP_Personalization_Main_Group'),
//    name : OB.Personalization.MAIN_GROUPNAME,
//    isSection : true,
//    _canEdit : false
//  });

  for (i = 0; i < form.getFields().length; i++) {
    fld = form.getFields()[i];

    if (fld.personalizable === false) {
      continue;
    }

    if (isc.isA.SectionItem(fld)) {
      record = {
        isFolder : true,
        _canEdit : false,
        canDrag : false,
        isSection : true,
        childNames : fld.itemIds,
        title : fld.title,
        canSelect: false,
        name : fld.name
      };
    } else {
      record = {
        title : fld.title,
        name : fld.name,
        hiddenInForm : fld.hiddenInForm,
        startRow : fld.startRow,
        colSpan : fld.colSpan,
        rowSpan : fld.rowSpan
      };
    }
    if (fld.displayed === false) {
      record.displayed = false;
    } else {
      record.displayed = true;
    }
    dataFields.push(record);
  }

  // now resolve the parent names
  for (i = 0; i < dataFields.length; i++) {
    record = dataFields[i];
    // can only have one level of parents
    if (record.childNames) {
      continue;
    }
    for (j = 0; j < dataFields.length; j++) {
      if (dataFields[j].childNames) {
        for (k = 0; k < dataFields[j].childNames.length; k++) {
          if (dataFields[j].childNames[k] === record.name) {
            record.parentName = dataFields[j].name;
            break;
          }
        }
      }
      if (record.parentName) {
        break;
      }
    }
  }

  // add to the status bar fields
  addedToStatusBar = false;
  for (j = 0; j < form.statusBarFields.length; j++) {
    record = dataFields.find('name', form.statusBarFields[j]);
    fld = form.getFields().find('name', form.statusBarFields[j]);
    if (record && !record.parentName) {
      record.parentName = OB.Personalization.STATUSBAR_GROUPNAME;
      // these items can not be moved from the statusbar
      record.isStaticStatusBarField = true;
      addedToStatusBar = true;
    }
  }
  
  // remove the status bar group, if there is nothing there
  if (!addedToStatusBar) {
    record = dataFields.find('name', OB.Personalization.STATUSBAR_GROUPNAME);
    dataFields.remove(record);
  }

  for (j = 0; j < dataFields.length; j++) {
    // do not consider the not-displayed ones which are not
    // part of the statusbar
    if (!dataFields[j].parentName && dataFields[j].displayed === false) {
      dataFields.removeAt(j);
    }
  }

  if (form.firstFocusedField) {
    record = dataFields.find('name', form.firstFocusedField);
    if (record) {
      record.firstFocus = true;
    }
  }

  return { form: {
      fields : dataFields
    }
  };
};

// ** {{{OB.Personalization.personalizeWindow}}} **
// Applies the data structure which contains the personalization settings to
// a complete window (an instance of ob-standard-window).
OB.Personalization.personalizeWindow = function(data, window) {
  var tabId, personalizationData, form, view, i, viewsToReset = [], done;

  // no personalization, nothing to do
  if (!data) {
    return;
  }

  for (i = 0; i < window.views.length; i++) {
    if (window.views[i].personalizationData) {
      viewsToReset.push({tabId: window.views[i].tabId});
    }
    delete window.views[i].personalizationData;
  }
  
  for (tabId in data) {
    if (data.hasOwnProperty(tabId)) {
      personalizationData = data[tabId];
      view = window.getView(tabId);
      
      done = viewsToReset.find('tabId', tabId);
      if (done) {
        viewsToReset.remove(done);
      }

      // note, the personalization for a tab maybe null
      // view can be null if a personalization setting
      // is not in sync anymore with the window
      if (personalizationData && view) {
        view.personalizationData = personalizationData;
        OB.Personalization.personalizeForm(personalizationData, view.viewForm);
      }
    }
  }
  
  // set all removed personalizations, reset those 
  for (i = 0; i < viewsToReset.length; i++) {
    view = window.getView(viewsToReset[i].tabId);
    view.viewForm.setFields(isc.shallowClone(view.viewForm._originalFields));
    view.viewForm.markForRedraw();
  }
};

// ** {{{OB.Personalization.personalizeForm}}} **
// Applies the data structure which contains the personalization settings to a
// form.
OB.Personalization.personalizeForm = function(data, form) {
  var persId, i, j, fld, fldDef, childFld, newField, newFields = [], record, allChildFieldsHidden, statusBarFields = [];
  
  // work further with the fields themselves
  if (data.form) {
    data = data.form;
  }
  if (data.fields) {
    data = data.fields;
  }
  
  for (i = 0; i < data.length; i++) {
    record = data[i];

    // original name is used when a field is visible in the status bar
    // and also on the form
    fld = form.getField(record.originalName || record.name);
    // use the original.fields as we are then sure
    // that we do not get ready build form items
    // but just the original simple objects
    // with properties
    fldDef = form._originalFields.find('name', record.originalName || record.name);
    if (!fld || !fldDef) {
      // main group for example
      continue;
    }

    // for the demo form get rid of all non-personalizable stuff
    if (form.isDemoForm && !fldDef.personalizable) {
      continue;
    }
    
    // set the first focused field
    if (record.firstFocus) {
      form.firstFocusedField = record.name;
    }

    // work with a clone
    newField = isc.shallowClone(fldDef);

    if (record.isSection) {
      newField.itemIds = [];
      // find the child items and set them
      allChildFieldsHidden = true;
      for (j = 0; j < data.length; j++) {
        if (data[j].parentName && data[j].parentName === newField.name) {
          newField.itemIds.push(data[j].name);
          allChildFieldsHidden = allChildFieldsHidden && data[j].hiddenInForm;
        }
      }
      // if all fields are hidden then don't show the section item either
      if (allChildFieldsHidden) {
        newField.hiddenInForm = true;
        newField.visible = false;
        newField.alwaysTakeSpace = false;
      } else {
        newField.alwaysTakeSpace = true;
        delete newField.hiddenInForm;
        delete newField.visible;
      }
    } else if (record.isDynamicStatusBarField || record.isStaticStatusBarField) {
      if (!record.hiddenInForm) {
        statusBarFields.push(record.originalName || record.name);
      }
    } else {
      // only copy the things we want to copy
      newField.startRow = record.startRow;
      newField.colSpan = record.colSpan;
      newField.rowSpan = record.rowSpan;

      if (record.hiddenInForm) {
        newField.hiddenInForm = true;
        newField.visible = false;
        newField.alwaysTakeSpace = false;
      } else {
        newField.alwaysTakeSpace = true;
        delete newField.hiddenInForm;
        delete newField.visible;
      }
    }

    // the dynamic status bar field already exist on the form
    if (!record.isDynamicStatusBarField) {
      newFields.push(newField);
    }
  }

  // now add the ones which we did not manage through the
  // formbuilder
  if (!form.isDemoForm) {
    for (i = 0; i < form.getFields().length; i++) {
      record = data.find('name', form.getFields()[i].name);
      // use the original.fields as we are then sure
      // that we do not get ready build form items
      // but just the original simple objects
      // with properties
      fldDef = form._originalFields.find('name', form.getFields()[i].name);
      if (!record && fldDef) {
        // clone the fieldDef
        newFields.push(isc.shallowClone(fldDef));
      }
    }    
  }

  // set the fields
  form.statusBarFields = statusBarFields;
  form.setFields(newFields);

  // and show me the stuff!
  form.markForRedraw();
  if (form.statusBar) {
    // the demo form has a direct reference to the statusbar
    form.statusBar.setContentLabel(null, null, form.getStatusBarFields());
  } else if (form.view && form.view.statusBar) {
    // when opened directly from a form
    form.view.statusBar.setContentLabel(null, null, form.getStatusBarFields());
  }
};
