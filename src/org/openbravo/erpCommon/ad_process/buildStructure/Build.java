package org.openbravo.erpCommon.ad_process.buildStructure;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.betwixt.io.BeanReader;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.data.FieldProvider;
import org.xml.sax.InputSource;

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

  public static Build getBuildFromXMLFile(String buildFilePath, String mappingFilePath)
      throws Exception {

    String source = OBPropertiesProvider.getInstance().getOpenbravoProperties().get("source.path")
        .toString();
    FileReader xmlReader = new FileReader(buildFilePath);

    BeanReader beanReader = new BeanReader();

    beanReader.getBindingConfiguration().setMapIDs(false);

    beanReader.getXMLIntrospector().register(
        new InputSource(new FileReader(new File(mappingFilePath))));

    beanReader.registerBeanClass("Build", Build.class);

    Build build = (Build) beanReader.parse(xmlReader);
    return build;
  }

  public BuildMainStep mainStepOfCode(String state) {

    for (BuildMainStep mstep : getMainSteps()) {
      if (mstep.getCode().equals(state))
        return mstep;
      for (BuildStep step : mstep.getStepList()) {
        if (step.getCode().equals(state)) {
          return mstep;
        }
      }
      if (state.equals(mstep.getSuccessCode()))
        return mstep;
      if (state.equals(mstep.getWarningCode()))
        return mstep;
      if (state.equals(mstep.getErrorCode()))
        return mstep;
    }
    return null;
  }
}
