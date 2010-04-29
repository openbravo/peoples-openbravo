package org.openbravo.erpCommon.ad_process.buildStructure;

import java.util.ArrayList;
import java.util.List;

public class BuildTranslation {
  private String language;
  private List<BuildMainStepTranslation> mainStepTranslations;

  public BuildTranslation() {
    mainStepTranslations = new ArrayList<BuildMainStepTranslation>();
  }

  public String getLanguage() {
    return language;
  }

  public void setLanguage(String language) {
    this.language = language;
  }

  public List<BuildMainStepTranslation> getMainStepTranslations() {
    return mainStepTranslations;
  }

  public void setMainStepTranslations(List<BuildMainStepTranslation> mainStepTranslations) {
    this.mainStepTranslations = mainStepTranslations;
  }

  public void addMainStepTranslation(BuildMainStepTranslation mStepT) {
    mainStepTranslations.add(mStepT);
  }

  public String getTranslatedName(String code) {
    for (BuildMainStepTranslation mainStep : mainStepTranslations) {
      if (mainStep.getCode().equalsIgnoreCase(code)) {
        return mainStep.getTranslatedName();
      }
      for (BuildStepTranslation step : mainStep.getStepTranslations()) {
        if (step.getCode().equalsIgnoreCase(code)) {
          return step.getTranslatedName();
        }
      }
    }
    return "";
  }

}
