/*
 * OracleModelReader.java
 *
 * Created on 13 de noviembre de 2007, 9:59
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.apache.ddlutils.platform.oracle;

import java.sql.DatabaseMetaData;
import java.sql.Types;
import org.apache.ddlutils.Platform;
import org.apache.ddlutils.platform.ModelLoaderBase;
import org.apache.ddlutils.util.ExtTypes;

/**
 *
 * @author adrian
 */
public class OracleModelLoader extends ModelLoaderBase {
    
    /** Creates a new instance of BasicModelLoader */
    public OracleModelLoader(Platform p) {
        super(p);
    }  
    
    protected String readName() {
        return "Oracle server";
    }
    
    protected boolean translateRequired(String required) {
        return "N".equals(required);
    }
    
    protected String translateDefault(String value, int type) {
        
        if (value == null) {
            return null;
        } else {
            String sreturn = value.trim();

            switch (type) {
                case Types.CHAR:
                case Types.VARCHAR:
                case ExtTypes.NCHAR:
                case ExtTypes.NVARCHAR:
                case Types.LONGVARCHAR:
                    if (sreturn.length() >= 2 && sreturn.startsWith("'") && sreturn.endsWith("'")) {
                        sreturn =  sreturn.substring(1, sreturn.length() - 1);
                        int i = 0;
                        StringBuffer sunescaped = new StringBuffer();
                        while (i < sreturn.length()) {
                            char c = sreturn.charAt(i);
                            if (c == '\'') {
                                i++;
                                if (i < sreturn.length()) {
                                    sunescaped.append(c);
                                    i++;                                    
                                }                                
                            } else {
                                sunescaped.append(c);
                                i++;
                            }
                        }
                        return sunescaped.toString();
                    } else {
                        return sreturn;
                    }
                default: return sreturn;
            }
        }
    }
    
    protected int translateColumnType(String nativeType) {
        
        if (nativeType == null) {
            return Types.NULL;
        } else if ("CHAR".equalsIgnoreCase(nativeType)) {
            return Types.CHAR;
        } else if ("VARCHAR2".equalsIgnoreCase(nativeType)) {
            return Types.VARCHAR;
        } else if ("NCHAR".equalsIgnoreCase(nativeType)) {
            return ExtTypes.NCHAR;
        } else if ("NVARCHAR2".equalsIgnoreCase(nativeType)) {
            return ExtTypes.NVARCHAR;
        } else if ("NUMBER".equalsIgnoreCase(nativeType)) {
            return Types.DECIMAL;
        } else if ("DATE".equalsIgnoreCase(nativeType)) {
            return Types.TIMESTAMP;
        } else if ("CLOB".equalsIgnoreCase(nativeType)) {
            return Types.CLOB;
        } else if ("BLOB".equalsIgnoreCase(nativeType)) {
            return Types.BLOB;
        } else {
            return Types.VARCHAR;
        }
    }
    
    protected int translateParamType(String nativeType) {
        
        if (nativeType == null) {
            return Types.NULL;
        } else if ("CHAR".equalsIgnoreCase(nativeType)) {
            return Types.CHAR;
        } else if ("VARCHAR2".equalsIgnoreCase(nativeType)) {
            return Types.VARCHAR;
        } else if ("NCHAR".equalsIgnoreCase(nativeType)) {
            return ExtTypes.NCHAR;
        } else if ("NVARCHAR2".equalsIgnoreCase(nativeType)) {
            return ExtTypes.NVARCHAR;
        } else if ("NUMBER".equalsIgnoreCase(nativeType)) {
            return Types.NUMERIC;
        } else if ("DATE".equalsIgnoreCase(nativeType)) {
            return Types.TIMESTAMP;
        } else if ("CLOB".equalsIgnoreCase(nativeType)) {
            return Types.CLOB;
        } else if ("BLOB".equalsIgnoreCase(nativeType)) {
            return Types.BLOB;
        } else {
            return Types.VARCHAR;
        }
    }    
    
    protected int translateFKEvent(String fkevent) {
        if ("CASCADE".equalsIgnoreCase(fkevent)) {
            return DatabaseMetaData.importedKeyCascade;
        } else if ("SET NULL".equalsIgnoreCase(fkevent)) {
            return DatabaseMetaData.importedKeySetNull;
        } else if ("RESTRICT".equalsIgnoreCase(fkevent)) {
            return DatabaseMetaData.importedKeyRestrict;
        } else {
            return DatabaseMetaData.importedKeyNoAction;
        }
    }      
}
