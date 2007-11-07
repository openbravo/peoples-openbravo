/*
 * RuleProcessor.java
 *
 * Created on 6 de noviembre de 2007, 15:48
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.apache.ddlutils.platform.postgresql;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author adrian
 */
public class RuleProcessor {
    
    private ArrayList<ViewField> _viewfields = new ArrayList<ViewField>();
    private String _viewtable = null;
    
    /** Creates a new instance of RuleProcessor */
    public RuleProcessor(String sql) throws RuleProcessorException {
        String field = "\\s*((.+?)(\\s+?[Aa][Ss]\\s+?(.+?))??)\\s*?((,)|(\\s[Ff][Rr][Oo][Mm]))";
        Pattern p = Pattern.compile("^\\s*[Ss][Ee][Ll][Ee][Cc][Tt]\\s" + field); //  
        Matcher m = p.matcher(sql);
        if (m.find()) {

            addField(m.group(2), m.group(4));
            String sseparator = m.group(5);
            int offset = m.end();
            
            p = Pattern.compile(field);
            m = p.matcher(sql);

            while (",".equals(sseparator)) {
                if (m.find(offset)) {
                    addField(m.group(2), m.group(4));
                    sseparator = m.group(5);
                    offset = m.end();  

                } else {
                    throw new RuleProcessorException();
                }
            }
            // From reached
            
            p = Pattern.compile("\\s+?(.+?)\\s+?[Ww][Hh][Ee][Rr][Ee]\\s");
            m = p.matcher(sql);
            if (m.find(offset)) {
                addTable(m.group(1));
            } else {
                throw new RuleProcessorException();
            }

        } else {
            throw new RuleProcessorException();
        }
    }
    
    private void addField(String field, String fieldas) {
        _viewfields.add(new ViewField(field, fieldas));
    }
    
    private void addTable(String table) {
        _viewtable = table;
    }
    
    public String getViewTable() {
        return _viewtable;
    }
    
    public ArrayList<ViewField> getViewFields() {
        return _viewfields;
    }
    
    public static class ViewField {
        
        private String _field;
        private String _fieldas;
        
        public ViewField(String field, String fieldas) {
            _field = field;
            _fieldas = fieldas;
        }
        
        public String getField() {
            return _field;
        }
        
        public String getFieldas() {
            return _fieldas == null ? _field : _fieldas;
        }
    }
//    
//    public static void main(String[] args) {
//        
//        try {
//            RuleProcessor p = new RuleProcessor(" SELECT   PEPE AS MYPEPE, juanillo  , estupendo FROM COCOLOCO WHERE NO SE SABE");
//            p = new RuleProcessor(" SELECT PEPE  as cosa FROM COCOLOCO WHERE NO SE SABE");
//            p = new RuleProcessor(" SELECT PEPE FROM COCOLOCO WHERE NO SE SABE");
//            p = new RuleProcessor(" SELECT PEPEFROM COCOLOCO WHERE NO SE SABE");
//        } catch (RuleProcessorException e) {
//            System.out.println(e);
//        }
//
//    }    
}
