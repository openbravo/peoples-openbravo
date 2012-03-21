define([], function () {
  
  // Mockup for OB.I18N
  
  OB = window.OB || {};
  OB.I18N = window.OB.I18N || {};
  
  OB.I18N.labels = {
    'OBPOS_DeleteLine': 'Deleted line %0 x %1',
    'OBPOS_AddLine': 'Added line %0 x %1',
    'OBPOS_AddUnits': 'Added %0 x %1',
    'OBPOS_RemoveUnits': 'Removed %0 x %1',
    'OBPOS_SetUnits': 'Set %0 x %1'    
  };
  
  
  OB.I18N.getLabel = function(key, params, object, property) {
    if (!OB.I18N.labels[key]) {
      return 'UNDEFINED ' + key;      
    }
    var label = OB.I18N.labels[key], i;
    if (params && params.length && params.length > 0) {
        for (i = 0; i < params.length; i++) {
            label = label.replace("%" + i, params[i]);
        }
    }
    return label;
  };

});