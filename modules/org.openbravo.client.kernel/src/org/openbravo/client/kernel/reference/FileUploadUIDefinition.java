package org.openbravo.client.kernel.reference;

import org.openbravo.model.ad.ui.Field;


/**
 *
 */
public class FileUploadUIDefinition extends UIDefinition {
    @Override
    public String getParentType() {
        return "image";
    }

    @Override
    public String getFormEditorType() {
        return "OBFileItem";
    }

    @Override
    public String getTypeProperties() {
        return "shortDisplayFormatter: function(value, field, component, record) {" + "return \"\";"
                + "},";
    }

    @Override
    public String getGridFieldProperties(Field field) {
        return super.getGridFieldProperties(field) + ", canGroupBy: false";
    }

//    @Override
//    public String getFieldProperties(Field field) {
//        String fieldProperties = super.getFieldProperties(field);
//        try {
//
//            NumberFormat f = Utility.getFormat(RequestContext.get().getVariablesSecureApp(),
//                    "amountInform");
//            BigDecimal maxsize = field.getColumn().getObfblFilemaxsize();
//            String maxsizeformat = maxsize == null ? null : f.format(maxsize);
//
//            JSONObject obj;
//            if (fieldProperties.equals("")) {
//                obj = new JSONObject();
//            } else {
//                obj = new JSONObject(fieldProperties);
//            }
//
//            obj.put("fileExtensions", field.getColumn().getObfblFileextensions());
//            obj.put("fileMaxSize", maxsize);
//            obj.put("fileMaxSizeFormat", maxsizeformat);
//            obj.put("fileMaxSizeUnit", field.getColumn().getObfblFilemaxsizeunit());
//            return obj.toString();
//        } catch (Exception e) { // ignore
//            log.error("There was an error when calculating the properties of an File BLOB field", e);
//            return fieldProperties;
//        }
//    }
}
