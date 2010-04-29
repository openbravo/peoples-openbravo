package org.openbravo.erpCommon.ad_process.buildStructure;

import java.util.ArrayList;
import java.util.List;

import org.openbravo.data.FieldProvider;

public class Build {
  private List<BuildMainStep> mainSteps;

  public Build() {
    mainSteps = new ArrayList<BuildMainStep>();
  }

  public List<BuildMainStep> getMainSteps() {
    return mainSteps;
  }

  public void addMainStep(BuildMainStep bms) {
    mainSteps.add(bms);
  }

  public BuildTranslation generateBuildTranslation(String language) {
    BuildTranslation trl = new BuildTranslation();
    trl.setLanguage(language);
    for (BuildMainStep mStep : mainSteps) {
      trl.addMainStepTranslation(mStep.generateBuildMainStepTranslation());
    }
    return trl;
  }

  public FieldProvider[] getFieldProvidersForBuild() {
    ArrayList<FieldProvider> fieldProviderList = new ArrayList<FieldProvider>();
    for (BuildMainStep mainStep : mainSteps) {
      fieldProviderList.add(new BuildStepWrapper(mainStep).getFieldProvider());
      for (BuildStep step : mainStep.getStepList()) {
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
