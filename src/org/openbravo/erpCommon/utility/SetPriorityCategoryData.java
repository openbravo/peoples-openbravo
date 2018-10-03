package org.openbravo.erpCommon.utility;

import java.util.Vector;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.openbravo.data.FieldProvider;

class SetPriorityCategoryData implements FieldProvider {

  static Logger log4j = LogManager.getLogger();
  public String category;
  public String priority;
  public String rownum;

  public String getField(String fieldName) {
    if (fieldName.equals("category")) {
      return ((category == null) ? "" : category);
    } else if (fieldName.equals("priority")) {
      return ((priority == null) ? "" : priority);
    } else if (fieldName.equals("rownum")) {
      return ((rownum == null) ? "" : rownum);
    } else {
      if (log4j.isDebugEnabled())
        log4j.debug("Field does not exist: " + fieldName);
      return null;
    }
  }

  public static SetPriorityCategoryData[] getCategories() {
    Vector<SetPriorityCategoryData> vector = new Vector<SetPriorityCategoryData>(0);
    LoggerContext lm = (LoggerContext) LogManager.getContext(false);
    log4j.debug("All appenders {}", lm.getConfiguration().getAppenders());

    for (org.apache.logging.log4j.core.Logger logger : lm.getLoggers()) {
      SetPriorityCategoryData setPriorityCategoryData = new SetPriorityCategoryData();
      setPriorityCategoryData.category = logger.getName();
      setPriorityCategoryData.priority = logger.getLevel().toString();

      log4j.trace("{} -> {} - {}", logger.getName(), logger.getLevel(), logger.getAppenders());
      if (vector.isEmpty()) {
        vector.addElement(setPriorityCategoryData);
      } else {
        int index = 0;
        while (index < vector.size()) {
          SetPriorityCategoryData cd = vector.get(index);
          if (setPriorityCategoryData.category.compareTo(cd.category) < 0) {
            vector.add(index, setPriorityCategoryData);
            break;
          }
          index++;
        }
        if (index == vector.size())
          vector.addElement(setPriorityCategoryData);
      }
    }

    SetPriorityCategoryData categoryData[] = new SetPriorityCategoryData[vector.size()];
    vector.copyInto(categoryData);
    for (int i = 0; i < categoryData.length; i++) {
      categoryData[i].rownum = "" + (i + 1);
    }
    return (categoryData);
  }

}
