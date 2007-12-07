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
 * All portions are Copyright (C) 2001-2006 Openbravo SL 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
*/
package org.openbravo.translate;

import org.openbravo.database.CPStandAlone;
import java.io.*;
import org.apache.xerces.parsers.*;
import org.xml.sax.XMLReader;
import org.xml.sax.Attributes;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;
import java.util.Vector;
import java.util.StringTokenizer;
import org.xml.sax.InputSource;
import javax.servlet.*;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * Translate the HTML file of the folder especified
 *
 **/
public class Translate extends DefaultHandler implements LexicalHandler {
  public static final String VERSION = "V1.O00.1";
  protected static CPStandAlone pool;
  static XMLReader parser;
  static TranslateData[] toLanguage;
  static String fromLanguage;
  static String actualLanguage;
  static String fileTermination;
  static OutputStreamWriter out;
  static PrintWriter printWriterTxt;
  static boolean error;
  static boolean isHtml = false;
  static StringBuffer strBuffer;
  static String actualTag;
  static String actualFile;
  static String actualPrefix;
  static StringBuffer translationText;
  static int count = 0;
  public static final String[] tokens = {"-", ":"};

  static Logger log4j = Logger.getLogger(Translate.class);

  public Translate (String xmlPoolFile) throws ServletException {
    pool = new CPStandAlone (xmlPoolFile);
  }
  public Translate (String xmlPoolFile, String _fileTermination) throws ServletException {
    this(xmlPoolFile);
    fileTermination = _fileTermination;
    isHtml=fileTermination.toLowerCase().endsWith("html");
    if (isHtml) parser =  new org.cyberneko.html.parsers.SAXParser();
    else {
      try {
        parser = new SAXParser();
        parser.setFeature( "http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
      } catch (Exception e) {
        log4j.error("Exception: " + e);
      }
    }
    parser.setContentHandler(this);
    fromLanguage = TranslateData.baseLanguage(pool);
    toLanguage = TranslateData.systemLanguage(pool);
  }

  public static void main(String argv[]) throws Exception {
    PropertyConfigurator.configure("log4j.lcf");
    String dirIni;
    String dirFin;
    boolean boolFilter;
    DirFilter dirFilter = null;
    String relativePath = "";

    if ((argv.length == 2) && argv[0].equals("clean")) {
      log4j.debug("clean AD_TEXTINTERFACES");
      Translate translate=new Translate(argv[1]);
      translate.clean();
      return;
    }
        
        
    if (argv.length < 4) {
      log4j.error("Usage: java Translate XmlPool.xml fileTermination sourceDir destinyDir relativePath");
      return;
    }

    Translate translate = new Translate(argv[0], argv[1]);

    dirIni = argv[2];
    dirFin = argv[3];
    if (argv.length > 4) relativePath = argv[4];
    boolFilter = true; 
    dirFilter = new  DirFilter(fileTermination);
    log4j.info("directory source: " + dirIni);
    log4j.info("directory destiny: " + dirFin);
    log4j.info("file termination: " + fileTermination);

    File path = new File(dirIni, relativePath);
    if (!path.exists()) {
      System.out.println("Can´t find directory: " + dirIni);
      translate.destroy();
      return;
    }
    File fileFin = new File(dirFin);
    if (!fileFin.exists()) {
      System.out.println("Can´t find directory: " + dirFin);
      translate.destroy();
      return;
    }
    listDir(path, boolFilter, dirFilter, fileFin, relativePath);
    System.out.println("Translated files for " + fileTermination + ": " + count);
    translate.destroy();
  }

  private void clean() {
    try {
      TranslateData.clean(pool);
    } catch (Exception e) {
      log4j.error(e.toString());
    }
  }
  // lista los archivos y directorios correspondientes a un File
  public static void listDir(File file, boolean boolFilter, DirFilter dirFilter, File fileFin, String relativePath) {
    File[] list;
    if(boolFilter) list = file.listFiles(dirFilter);
    else list = file.listFiles();
    for(int i = 0; i < list.length; i++) {
      File fileItem = list[i];
      if (fileItem.isDirectory()) {
        // si es subdirectorio se lista recursivamente
        String prevRelativePath = new String(relativePath);
        relativePath += "/" + fileItem.getName();
        listDir(fileItem, boolFilter, dirFilter, fileFin, relativePath);
        relativePath = prevRelativePath;
      } else {
        try {
          if (log4j.isDebugEnabled()) log4j.debug(list[i] + " Parent: " + fileItem.getParent() + " getName() " + fileItem.getName() + " canonical: " + fileItem.getCanonicalPath());
          for (int h=0;h<toLanguage.length;h++) {
            actualLanguage = toLanguage[h].name;
            parseFile(list[i], fileFin, relativePath);
          }
        } catch (IOException e) {
          log4j.error("IOException: " + e);
        }
      }
    }
  }

  /* cambiar dirIni.toString() por file
     buscar la terminacion solo en el final
   */

  private static void parseFile(File fileParsing, File fileFin, String relativePath) {
    String strFileName = fileParsing.getName();
    if (log4j.isDebugEnabled()) log4j.debug("Parsing of " + strFileName);
    int pos = strFileName.indexOf(fileTermination);
    if (pos == -1) {
      log4j.error("File " + strFileName + " don't have termination " + fileTermination);
      return;
    }
    String strFileWithoutTermination = strFileName.substring(0, pos);
    if (log4j.isDebugEnabled()) log4j.debug("File without termination: " + strFileWithoutTermination);
    actualFile = relativePath + "/" + strFileName;
    try {
      if (log4j.isDebugEnabled()) log4j.debug("Relative path: " + actualLanguage + "/" + relativePath);
      File dirHTML = new File(fileFin, actualLanguage + "/" + relativePath);
      if (log4j.isDebugEnabled()) log4j.debug(" dirHTML: " + dirHTML);
      dirHTML.mkdirs();
      File fileHTML = new File(dirHTML, strFileWithoutTermination + fileTermination);
      if (log4j.isDebugEnabled()) log4j.debug(" fileHTML: " + fileHTML);
      if (log4j.isDebugEnabled()) log4j.debug(" time file parsed: " + fileParsing.lastModified() + " time file HTML new: " + fileHTML.lastModified());
      if (fileHTML.exists()) {
        /*java.util.Date lastModified = new java.util.Date(fileHTML.lastModified());
        java.util.Calendar modificationReference = java.util.Calendar.getInstance();
        modificationReference.add(java.util.Calendar.MINUTE, -10);
        if (lastModified.compareTo(modificationReference.getTime()) < 0) return;*/
        java.util.Date newFileModified = new java.util.Date(fileHTML.lastModified());
        java.util.Date oldFileModified = new java.util.Date(fileParsing.lastModified());
        //if (count == 0) System.out.println("******************** " + newFileModified.compareTo(oldFileModified) + " - " + newFileModified + " - " + oldFileModified);
        //if (newFileModified.compareTo(oldFileModified) > 0) return;
        if (/*!fileTermination.equalsIgnoreCase("jrxml") && */newFileModified.compareTo(oldFileModified) > 0) return;
        //if (fileHTML.lastModified()>= fileParsing.lastModified()) return;
      }
      count++;
      FileOutputStream resultsFile = new FileOutputStream(fileHTML);
      out  = new OutputStreamWriter(resultsFile, "UTF-8");

      strBuffer = new StringBuffer();
      log4j.info("F " + fileParsing);
      java.util.Date date = new java.util.Date();  // there is date in java.sql.*
      if (log4j.isDebugEnabled()) log4j.debug("Hour: " + date.getTime());
      error = false;
      try {
//          parser.parse(fileParsing.getAbsolutePath());
        parser.parse(new InputSource(new FileReader(fileParsing)));
      } catch (IOException e) {
        log4j.error("file: " + actualFile);
        e.printStackTrace();
      } catch (Exception e) {
        log4j.error("file: " + actualFile);
        e.printStackTrace();
      }
      out.write(strBuffer.toString());
      out.flush();
      resultsFile.close();
      if (error) {
//          fileHTML.delete();
      }
    } catch(IOException e) {
       e.printStackTrace();
       log4j.error("Problem at close of the file");
    }
  }

  public String parseAttributes(Attributes amap) {
    StringBuffer data = new StringBuffer();
    String type = "";
    String value = "";
    boolean hasvalue = false;
    for (int i = 0; i < amap.getLength(); i++) {
      String strAux = amap.getValue(i);
      if (amap.getQName(i).equalsIgnoreCase("type")) {
        type = strAux;
      } else if (amap.getQName(i).equalsIgnoreCase("value")) {
        hasvalue = true;
        value = strAux;
      } else if (amap.getQName(i).equalsIgnoreCase("onMouseOver")) {
        if (strAux.toLowerCase().startsWith("window.status='")) {
          int j = strAux.lastIndexOf("';");
          int aux = j;
          while ((j!=-1) && (aux=strAux.lastIndexOf("';", j-1))!=-1) {
            j=aux;
          }
          String strToken = translate(strAux.substring(15, j));
          strAux = strAux.substring(0, 15) + strToken + strAux.substring(j);
        }
      } else if (amap.getQName(i).equalsIgnoreCase("alt")) {
        strAux = translate(strAux);
      } else if (amap.getQName(i).equalsIgnoreCase("title")) {
        strAux = translate(strAux);
      }
      if (!amap.getQName(i).equalsIgnoreCase("value")) data.append(" ").append(amap.getQName(i)).append("=\"").append(strAux).append("\"");
    }
    if (value!=null && !value.equals("")) {
      if (type.equalsIgnoreCase("button")) value = translate(value);
      data.append(" value=\"").append(value).append("\"");
    } else if (hasvalue) {
      data.append(" value=\"").append(value).append("\"");
    }
    return data.toString();
  }

  public void startDocument() {
    if (!isHtml) strBuffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
  }

  public void startPrefixMapping(String prefix, String uri) {
    actualPrefix = " xmlns:" + prefix + "=\"" + uri + "\"";
  }

  public boolean isParseable(String tagname) {
    if (tagname.equalsIgnoreCase("script")) return false;
    else if (fileTermination.equalsIgnoreCase("jrxml")) {
      if (!tagname.equalsIgnoreCase("text") && !tagname.equalsIgnoreCase("textFieldExpression")) return false;
    }
    return true;
  }

  public void startElement(String uri, String name, String qName, Attributes amap) {//(String name, AttributeList amap) throws SAXException {
    if (log4j.isDebugEnabled()) log4j.debug("Configuration: startElement is called: element name=" + qName);
    if (actualTag!=null && isParseable(actualTag) && translationText != null) {
      strBuffer.append(translate(translationText.toString()));
    } else if (translationText != null) strBuffer.append(translationText.toString());
    translationText = null;
    if (strBuffer.toString().endsWith(">")) strBuffer.append("\n");
    strBuffer.append("<" + qName);
    strBuffer.append(parseAttributes(amap));
    if (actualPrefix!=null && !actualPrefix.equals("")) {
      strBuffer.append(actualPrefix);
      actualPrefix = "";
    }
    strBuffer.append(">");
    actualTag=name.trim().toUpperCase();
  }

  public void comment(char[] ch, int start, int length) {}
  public void endDTD() {}
  public void endEntity(String name) {}
  public void startDTD(String name, String publicId, String systemId) {}
  public void startEntity(String name) {}

  public void startCDATA() {
    strBuffer.append("<![CDATA[");
  }
  public void endCDATA() {
    strBuffer.append("]]>");
  }

  public void endElement(String uri, String name, String qName) {//(String name) throws SAXException {
    if (log4j.isDebugEnabled()) log4j.debug("Configuration: endElement is called: " + qName);
    if (isParseable(actualTag) && translationText != null) {
      if (fileTermination.equalsIgnoreCase("jrxml")) startCDATA();
      strBuffer.append(translate(translationText.toString()));
      if (log4j.isDebugEnabled()) log4j.debug("endElement - TranslationText: " + translationText.toString());
      if (fileTermination.equalsIgnoreCase("jrxml")) endCDATA();
    } else if (translationText != null) {
      if (fileTermination.equalsIgnoreCase("jrxml")) startCDATA();
      strBuffer.append(translationText.toString());
      if (fileTermination.equalsIgnoreCase("jrxml")) endCDATA();
    }
    translationText = null;
    strBuffer.append("</" + qName + ">");
    actualTag="";
  }

  public void characters(char[] ch, int start, int length) {//throws SAXException {
    String chars = new String(ch, start, length);
    if (log4j.isDebugEnabled()) log4j.debug("Configuration(characters) is called: " + chars);
    if (translationText==null) translationText = new StringBuffer();
    translationText.append(chars);
  }

  public String translate(String ini) {
    return translate(ini, false);
  }

  public String translate(String ini, boolean isPartial) {
    ini = replace(replace(ini.trim(), "\r", ""), "\n", " ");
    ini = ini.trim();
    ini = delSp(ini);
    if (!isPartial && actualTag.equalsIgnoreCase("textFieldExpression")) {
      StringBuffer text = new StringBuffer();
      int pos = ini.indexOf("\"");
      while (pos!=-1) {
        text.append(ini.substring(0, pos+1));
        ini = ini.substring(pos+1);
        pos = ini.indexOf("\"");
        if (pos!=-1) {
          text.append(translate(ini.substring(0, pos), true)).append("\"");
          ini = ini.substring(pos+1);
        } else break;
        pos = ini.indexOf("\"");
      }
      text.append(ini);
      return text.toString();
    }
    Vector<String> translated = new Vector<String>(0);
    boolean aux=true;
    translated.addElement("Y");
    String resultado = ini;
    if (!ini.equals("") && !ini.toLowerCase().startsWith("xx") && !isNumeric(ini)) {
      resultado = tokenize(ini, 0, translated);
      try {
        aux = translated.elementAt(0).equals("Y");
        if (!aux && TranslateData.existsExpresion(pool, ini, actualFile)==0) {
          TranslateData.insert(pool,  ini, actualFile);
          log4j.warn("Couldn't translate: " + ini + ".Result translated: " + resultado + ".Actual file: " + actualFile);
        }
      } catch (ServletException e) {
        e.printStackTrace();
      }
    }
    return resultado;
  }

  public boolean isNumeric(String ini) {
    boolean isNumericData = true;
    for (int i=0; i< ini.length(); i++) {
      if (Character.isLetter(ini.charAt(i))) {
        isNumericData = false;
        break;
      }
    }
    return isNumericData;
  }

  public String replace(String strInicial, String strReplaceWhat, String strReplaceWith) {
    int index = 0;
    int pos;
    StringBuffer strFinal = new StringBuffer("");
    do {
      pos = strInicial.indexOf(strReplaceWhat, index);
      if (pos != - 1) {
        strFinal.append(strInicial.substring(index, pos) + strReplaceWith);
        index = pos + strReplaceWhat.length();
      } else {
        strFinal.append(strInicial.substring(index));
      }
    } while (index < strInicial.length() && pos != -1);
    return strFinal.toString();
  }

  public String delSp(String strIni){
    boolean sp = false;
    String strFin = "";
    for (int i=0; i<strIni.length() ; i++ ){
      if (!sp || strIni.charAt(i)!=' ') strFin += strIni.charAt(i);
      sp = (strIni.charAt(i)==' ');
    }
    return strFin;
  }

  public String tokenize(String ini, int indice, Vector<String> isTranslated) {
    StringBuffer fin = new StringBuffer();
    try {
      boolean first = true;
      String translated = null;
      TranslateData[] dataTranslated = TranslateData.select(pool,ini.trim(), actualFile, actualLanguage);
      if (dataTranslated!=null && dataTranslated.length>0) {
        translated = dataTranslated[0].tr;
        //TranslateData.update(pool, dataTranslated[0].baseDictionaryEntryId, actualLanguage);
        TranslateData.update(pool, dataTranslated[0].adTextinterfacesId);
      }
      if (translated!=null && translated.length()>0) {
        fin.append(translated);
        return fin.toString();
      }
      StringTokenizer st = new StringTokenizer(ini, tokens[indice], false);
      while (st.hasMoreTokens()) {
        if (first) {
          first = false;
        } else {
          fin.append(tokens[indice]);
        }
        String token = st.nextToken();
        token = token.trim();
        if (log4j.isDebugEnabled()) log4j.debug("Token of " + ini + " : -" + token + "-");
        translated = null;
        dataTranslated = TranslateData.select(pool,token.trim(), actualFile, actualLanguage);
        if (dataTranslated!=null && dataTranslated.length>0) {
          translated = dataTranslated[0].tr;
          //TranslateData.update(pool, dataTranslated[0].baseDictionaryEntryId, actualLanguage);
          TranslateData.update(pool, dataTranslated[0].adTextinterfacesId); 
        }
        if ((translated==null || translated.equals("")) && indice<(tokens.length-1)) translated=tokenize(token, indice+1, isTranslated);
        if (translated==null || translated.equals("")) {
          fin.append(token);
          isTranslated.set(0, "N");
        } else fin.append(translated);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return fin.toString();
  }

  public void destroy() {
    pool.destroy();
  }
}
