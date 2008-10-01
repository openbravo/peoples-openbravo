package org.openbravo.base.util;

import java.util.HashMap;
import java.util.List;

import org.openbravo.base.model.Column;

public class NamingUtil {
  private static HashMap<String, String> entityNames = null;
  private static HashMap<String, String> packageNames = null;
  private static HashMap<String, String> specialMappings = null;
  private static HashMap<String, String> reservedNames = null;
  
  private final static String prefix = "org.openbravo.base.model.";
  
  static {
    entityNames = new HashMap<String, String>();
    packageNames = new HashMap<String, String>();
    specialMappings = new HashMap<String, String>();
    
    entityNames.put("A", "");
    packageNames.put("A", "assetmanagement");
    
    entityNames.put("C", "Core");
    packageNames.put("C", "core");
    
    entityNames.put("AD", "AD");
    packageNames.put("AD", "ad"); // this could be applicationdictionary
    
    entityNames.put("M", "Material");
    packageNames.put("M", "materialmanagement");
    
    entityNames.put("GL", "GL");
    packageNames.put("GL", "generalledger");
    
    entityNames.put("FACT", "Accounting");
    packageNames.put("FACT", "accounting");
    
    entityNames.put("MA", "Manufacturing");
    packageNames.put("MA", "manufacturing");
    
    entityNames.put("MRP", "MRP");
    packageNames.put("MRP", "mrp");
    
    entityNames.put("S", "Service");
    packageNames.put("S", "service");
    
    entityNames.put("I", "Import");
    packageNames.put("I", "import");
    
    entityNames.put("AT", "AT");
    packageNames.put("AT", "automatictesting");
    
    // Special columns mappings
    specialMappings.put("AD_Column.Callout", "calloutName");
    specialMappings.put("AD_Language.AD_Language", "languageCode");
    specialMappings.put("AD_WF_Node.Workflow_ID", "workflowId");
    specialMappings.put("C_Location.City", "cityName");
    specialMappings.put("C_Year.Year", "yearName");
    specialMappings.put("C_Project.Projectphase", "phaseName");
    specialMappings.put("M_Substitute.Substitute_ID", "substituteId");
    specialMappings.put("C_AcctSchema_Element.Org_ID", "orgId");
    specialMappings.put("C_BP_BankAccount.A_Country", "aCountry");
    specialMappings.put("C_Greeting.Greeting", "greetingName");
    specialMappings.put("C_Greeting_Trl.Greeting", "greetingName");
    specialMappings.put("C_Withholding_Acct.Withholding_Acct", "_withholdingAcct"); // FIXME
    
    specialMappings.put("I_Tax.I_IsImported", "imported");
    specialMappings.put("I_Tax.I_ErrorMsg", "errorMsg");
    
    // 
    // modify
    // mapping
    // name
    specialMappings.put("C_CommissionAmt.CommissionAmt", "commisionAmount");
    specialMappings.put("C_CommissionLine.Org_ID", "orgId");
    specialMappings.put("M_AttributeSetInstance.Lot", "lotName");
    specialMappings.put("C_Discount.C_Discount_ID", "id");
    specialMappings.put("M_RequisitionLine.PriceList", "price");
    
    reservedNames = new HashMap<String, String>();
    reservedNames.put("default", "deflt");
    reservedNames.put("import", "imprt");
    
  }
  
  public static String getEntityName(String tableName) {
    int pos = tableName.indexOf("_");
    
    if (pos == -1)
      return tableName;
    
    String entityName = "";
    entityName = tableName.substring(pos + 1);
    entityName = entityName.replaceAll("_", "");
    return entityName;
    
  }
  
  public static String getMappingName(String tableName) {
    int pos = tableName.indexOf("_");
    
    if (pos == -1)
      return tableName;
    
    String tablePrefix = tableName.substring(0, pos);
    String entityName = "";
    
    if (entityNames.containsKey(tablePrefix.toUpperCase()))
      entityName = entityNames.get(tablePrefix.toUpperCase()) + tableName.substring(pos + 1);
    else
      entityName = "Custom" + tableName;
    ;
    entityName = entityName.replaceAll("_", "");
    
    return entityName;
  }
  
  public static String getPackageName(String tableName) {
    int pos = tableName.indexOf("_");
    
    if (pos == -1)
      return prefix + tableName.toLowerCase();
    
    String tablePrefix = tableName.substring(0, pos);
    String packageName = "";
    
    if (packageNames.containsKey(tablePrefix.toUpperCase()))
      packageName = packageNames.get(tablePrefix.toUpperCase());
    else
      packageName = "custom" + tableName.substring(0, pos).toLowerCase();
    
    if (reservedNames.get(packageName) != null) {
      packageName = reservedNames.get(packageName);
    }
    return prefix + packageName;
  }
  
  public static String getColumnMappingName(Column column) {
    final List<Column> columns = column.getTable().getColumns();
    final String columnName = column.getColumnName();
    final String tableName = column.getTable().getTableName();
    
    String mappingName = NamingUtil.calculateColumnMapping(columnName, tableName);
    
    // the is
    if (column.isBoolean() && mappingName.lastIndexOf("is") == 0) {
      String tmp = mappingName.substring(2);
      boolean duplicated = false;
      for (Column c : columns) {
        if (!c.getColumnName().equals(columnName)) {
          if (tmp.equals(NamingUtil.calculateColumnMapping(c.getColumnName(), tableName))) {
            duplicated = true;
            break;
          }
        }
      }
      if (!duplicated)
        mappingName = tmp;
      
    }
    
    if (reservedNames.get(mappingName) != null) {
      mappingName = reservedNames.get(mappingName);
    }
    
    return mappingName;
  }
  
  private static String calculateColumnMapping(String columnName, String tableName) {
    String mappingName = columnName.toLowerCase();
    String mappingPrefix = "";
    
    if (specialMappings.containsKey(tableName + "." + columnName))
      return specialMappings.get(tableName + "." + columnName);
    
    // Stripping prefix
    int pos = mappingName.indexOf("_");
    if (pos != -1) {
      mappingPrefix = mappingName.substring(0, pos);
      if (entityNames.containsKey(mappingPrefix.toUpperCase()) && !mappingPrefix.equalsIgnoreCase("I"))
        mappingName = mappingName.substring(pos + 1);
    }
    
    // Stripping suffix
    pos = mappingName.lastIndexOf("_");
    if (pos != -1) {
      if (mappingName.substring(pos).equalsIgnoreCase("_ID"))
        mappingName = mappingName.substring(0, pos);
    }
    
    // "CamelCasing"
    pos = mappingName.indexOf("_");
    while (pos != -1) {
      String leftPart = mappingName.substring(0, pos);
      String camelLetter = String.valueOf(mappingName.charAt(pos + 1)).toUpperCase();
      String rightPart = mappingName.substring(pos + 2);
      mappingName = leftPart + camelLetter + rightPart;
      pos = mappingName.indexOf("_");
    }
    return mappingName;
  }
}
