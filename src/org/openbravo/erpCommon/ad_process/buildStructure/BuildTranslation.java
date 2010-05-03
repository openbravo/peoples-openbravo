package org.openbravo.erpCommon.ad_process.buildStructure;

import java.util.ArrayList;
import java.util.List;

import org.openbravo.data.FieldProvider;

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

  public FieldProvider[] getFieldProvidersForBuild() {
    ArrayList<FieldProvider> fieldProviderList = new ArrayList<FieldProvider>();
    for (BuildMainStepTranslation mainStep : mainStepTranslations) {
      fieldProviderList.add(new BuildStepWrapper(mainStep).getFieldProvider());
      for (BuildStepTranslation step : mainStep.getStepTranslations()) {
        fieldProviderList.add(new BuildStepWrapper(step).getFieldProvider());
      }
    }

    FieldProvider[] fps = new FieldProvider[fieldProviderList.size()];
    int i = 0;
    for (FieldProvider fp : fieldProviderList)
      fps[i++] = fp;
    return fps;
  }
}
