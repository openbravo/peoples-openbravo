/*
 * 
 * Copyright (C) 2001-2008 Openbravo S.L. Licensed under the Apache Software
 * License version 2.0 You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package org.openbravo.dal.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.base.util.Check;

/**
 * This class is responsible for generating the hibernate mapping for the tables
 * and entities within OpenBravo.
 * 
 * TODO: make use column.fieldlength and valuemax and valuemin.
 * 
 * @author mtaal
 */

public class DalMappingGenerator {
  private static final Logger log = Logger.getLogger(DalMappingGenerator.class);
  
  private final static String TEMPLATE_FILE = "template.hbm.xml";
  private final static String MAIN_TEMPLATE_FILE = "template_main.hbm.xml";
  // private final static char TAB = '\t';
  private final static String TAB2 = "\t\t";
  private final static String TAB3 = "\t\t\t";
  private final static char NL = '\n';
  
  private static DalMappingGenerator instance = new DalMappingGenerator();
  
  public static DalMappingGenerator getInstance() {
    return instance;
  }
  
  public static void setInstance(DalMappingGenerator dalMappingGenerator) {
    instance = dalMappingGenerator;
  }
  
  private String templateContents;
  
  public String generateMapping() {
    final ModelProvider mp = ModelProvider.getInstance();
    final StringBuilder sb = new StringBuilder();
    for (Entity e : mp.getModel()) {
      final String entityMapping = generateMapping(e);
      sb.append(entityMapping);
    }
    final String mainTemplate = readFile(MAIN_TEMPLATE_FILE);
    final String result = mainTemplate.replace("content", sb.toString());
    
    // System.err.println(result);
    
    if (log.isDebugEnabled()) {
      log.debug(result);
    }
    
    if (false) {
      try {
        final File f = new File("/home/mtaal/mytmp/hibernate.hbm.xml");
        if (f.exists()) {
          f.delete();
        }
        f.createNewFile();
        final FileWriter fw = new FileWriter(f);
        fw.write(result);
        fw.close();
      } catch (Exception e) {
        throw new OBException(e);
      }
    }
    return result;
  }
  
  public String generateMapping(Entity entity) {
    String hbm = getClassTemplateContents();
    hbm = hbm.replaceAll("mappingName", entity.getName());
    hbm = hbm.replaceAll("tableName", entity.getTableName());
    hbm = hbm.replaceAll("className", entity.getClassName());
    hbm = hbm.replaceAll("ismutable", entity.isMutable() + "");
    
    // create the content by first getting the id
    final StringBuffer content = new StringBuffer();
    if (entity.hasCompositeId()) {
      content.append(generateCompositeID(entity));
    } else {
      content.append(generateStandardID(entity));
    }
    content.append(NL);
    
    // now handle the standard columns
    for (Property p : entity.getProperties()) {
      if (p.isId() && p.isPrimitive()) { // handled separately
        continue;
      }
      
      if (p.isPartOfCompositeId()) {
        continue;
      }
      
      if (p.isPrimitive()) {
        content.append(generatePrimitiveMapping(p));
      } else {
        content.append(generateReferenceMapping(p));
      }
    }
    
    hbm = hbm.replace("content", content.toString());
    return hbm;
  }
  
  private String generatePrimitiveMapping(Property p) {
    if (p.getPrimitiveType() == Object.class) {
      return "";
    }
    final StringBuffer sb = new StringBuffer();
    sb.append(TAB2 + "<property name=\"" + p.getName() + "\"");
    
    String type = p.getPrimitiveType().getName();
    if (p.isBoolean()) {
      type = OBYesNoType.class.getName(); // "yes_no";
    }
    sb.append(" type=\"" + type + "\"");
    
    sb.append(" column=\"" + p.getColumnName() + "\"");
    
    if (p.isMandatory()) {
      sb.append(" not-null=\"true\"");
    }
    
    if (!p.isUpdatable()) {
      sb.append(" update=\"false\"");
    }
    
    sb.append("/>" + NL);
    return sb.toString();
  }
  
  private String generateReferenceMapping(Property p) {
    if (p.getTargetEntity() == null) {
      return "<!-- Unsupported reference type " + p.getName() + " of entity " + p.getEntity().getName() + "-->" + NL;
    }
    final StringBuffer sb = new StringBuffer();
    if (p.isOneToOne()) {
      final String name = p.getSimpleTypeName().substring(0, 1).toLowerCase() + p.getSimpleTypeName().substring(1);
      sb.append(TAB2 + "<one-to-one name=\"" + name + "\"");
      sb.append(" constrained=\"true\"");
    } else {
      sb.append(TAB2 + "<many-to-one name=\"" + p.getName() + "\" column=\"" + p.getColumnName() + "\"");
      if (p.isMandatory()) {
        sb.append(" not-null=\"true\"");
      }
    }
    sb.append(" entity-name=\"" + p.getTargetEntity().getName() + "\"");
    
    if (p.getReferencedProperty() != null && !p.getReferencedProperty().isId()) {
      sb.append(" property-ref=\"" + p.getReferencedProperty().getName() + "\"");
    }
    
    sb.append("/>" + NL);
    return sb.toString();
  }
  
  // assumes one primary key column
  private String generateStandardID(Entity entity) {
    Check.isTrue(entity.getIdProperties().size() == 1, "Method can only handle primary keys with one column");
    final Property p = entity.getIdProperties().get(0);
    final StringBuffer sb = new StringBuffer();
    sb.append(TAB2 + "<id name=\"" + p.getName() + "\" type=\"" + p.getPrimitiveType().getName() + "\" column=\"" + p.getColumnName() + "\">" + NL);
    if (p.getIdBasedOnProperty() != null) {
      sb.append(TAB3 + "<generator class=\"foreign\">" + NL);
      sb.append(TAB2 + TAB2 + "<param name=\"property\">" + p.getIdBasedOnProperty().getName() + "</param>" + NL);
      sb.append(TAB3 + "</generator>" + NL);
    } else if (p.isUuid()) {
      sb.append(TAB3 + "<generator class=\"uuid\"/>" + NL);
    }
    sb.append(TAB2 + "</id>" + NL);
    return sb.toString();
  }
  
  private String generateCompositeID(Entity e) {
    Check.isTrue(e.hasCompositeId(), "Method can only handle primary keys with more than one column");
    final StringBuffer sb = new StringBuffer();
    sb.append(TAB2 + "<composite-id name=\"id\" class=\"" + e.getClassName() + "$Id\">" + NL);
    final Property compId = e.getIdProperties().get(0);
    Check.isTrue(compId.isCompositeId(), "Property " + compId + " is expected to be a composite Id");
    for (Property p : compId.getIdParts()) {
      if (p.isPrimitive()) {
        String type = p.getPrimitiveType().getName();
        if (boolean.class.isAssignableFrom(p.getPrimitiveType().getClass()) || Boolean.class == p.getPrimitiveType()) {
          type = "yes_no";
        }
        sb.append(TAB3 + "<key-property name=\"" + p.getName() + "\" column=\"" + p.getColumnName() + "\" type=\"" + type + "\"/>" + NL);
      } else {
        sb.append(TAB3 + "<key-many-to-one name=\"" + p.getName() + "\" column=\"" + p.getColumnName() + "\"");
        sb.append(" entity-name=\"" + p.getTargetEntity().getName() + "\"");
        sb.append("/>" + NL);
      }
    }
    sb.append(TAB2 + "</composite-id>" + NL);
    return sb.toString();
  }
  
  private String getClassTemplateContents() {
    if (templateContents == null) {
      templateContents = readFile(TEMPLATE_FILE);
    }
    return templateContents;
  }
  
  private String readFile(String fileName) {
    try {
      final InputStreamReader fr = new InputStreamReader(getClass().getResourceAsStream(fileName));
      final BufferedReader br = new BufferedReader(fr);
      try {
        String line;
        final StringBuffer sb = new StringBuffer();
        while ((line = br.readLine()) != null) {
          sb.append(line + "\n");
        }
        return sb.toString();
      } finally {
        br.close();
        fr.close();
      }
    } catch (IOException e) {
      throw new OBException(e);
    }
  }
}
