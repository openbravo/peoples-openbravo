/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License.
 * The Original Code is Openbravo ERP.
 * The Initial Developer of the Original Code is Openbravo SLU
 * All portions are Copyright (C) 2020 Openbravo SLU
 * All Rights Reserved.
 ************************************************************************
 */

const path = require('path');
const fs = require('fs');
const xmlParser = require('fast-xml-parser');

const reactDir = 'web-jspack';
const package_json_template = 'package-template.json';
const package_json = 'package.json';
const source_data_path = 'src-db/database/sourcedata';
const ad_module = 'AD_MODULE.xml';
const ad_module_dependency = 'AD_MODULE_DEPENDENCY.xml';

const buildModuleMap = modulesDir => {
  const map = { 0: '../../' };

  fs.readdirSync(modulesDir)
    .filter(m =>
      fs.existsSync(path.resolve(modulesDir, m, source_data_path, ad_module))
    )
    .forEach(m => {
      const modulePath = path.resolve(
        modulesDir,
        m,
        source_data_path,
        ad_module
      );
      const moduleXml = fs.readFileSync(modulePath, 'utf8');
      const module = xmlParser.parse(moduleXml);
      const moduleInfo = module.data.AD_MODULE;
      map[moduleInfo.AD_MODULE_ID] = moduleInfo.JAVAPACKAGE;
    });
  return map;
};

const getPackageJson = mod => {
  const modulePackageJsonPath = path.resolve(
    modulesDir,
    mod,
    reactDir,
    mod,
    package_json
  );
  if (fs.existsSync(modulePackageJsonPath)) {
    return JSON.parse(fs.readFileSync(modulePackageJsonPath));
  } else {
    const modulePackageJsonTemplatePath = path.resolve(
      modulesDir,
      mod,
      reactDir,
      mod,
      package_json_template
    );
    if (fs.existsSync(modulePackageJsonTemplatePath)) {
      JSON.parse(fs.readFileSync(modulePackageJsonTemplatePath));
    } else {
      return null;
    }
  }
};

const checkIfAlreadyDefined = (
  originalDependenciesInfo,
  directDependency,
  version,
  dependenciesInfo
) => {
  const originalDependencyVersion =
    originalDependenciesInfo.dependencies[directDependency];
  if (originalDependencyVersion) {
    if (originalDependencyVersion === version) {
      console.warn(
        `Package ${directDependency} defined in ${originalDependenciesInfo.name} but already defined in ${dependenciesInfo.name} with same version ${version}`
      );
    } else {
      throw Error(
        `Package ${directDependency} defined in ${originalDependenciesInfo.name} with version ${originalDependencyVersion} but also defined in ${dependenciesInfo.name} with version ${version}`
      );
    }
  }
};

const copyDeps = (
  dependencyJavaPackages,
  originalDependenciesInfo,
  copiedDependencies
) => {
  dependencyJavaPackages.forEach(dependency => {
    const moduleReactPath = path.resolve(
      modulesDir,
      dependency,
      reactDir,
      dependency
    );
    if (fs.existsSync(moduleReactPath)) {
      const packageJson = getPackageJson(dependency);
      if (packageJson != null) {
        const dependenciesInfo = {
          name: dependency,
          dependencies: {
            ...packageJson.dependencies,
            ...packageJson.devDependencies,
            ...packageJson.peerDependencies,
          },
        };

        for (const [directDependency, version] of Object.entries(
          dependenciesInfo.dependencies
        )) {
          checkIfAlreadyDefined(
            originalDependenciesInfo,
            directDependency,
            version,
            dependenciesInfo
          );
          copiedDependencies[directDependency] = version;
        }
      }
    }
    const newDependencyJavaPackages = getDependantModules(
      dependency,
      moduleMap
    );
    copyDeps(
      newDependencyJavaPackages,
      originalDependenciesInfo,
      copiedDependencies
    );
  });
};

function getDependantModules(module, moduleMap) {
  const adModuleDependencyPath = path.resolve(
    modulesDir,
    module,
    source_data_path,
    ad_module_dependency
  );
  if (!fs.existsSync(adModuleDependencyPath)) {
    return [];
  }
  const adModuleDependencyXml = fs.readFileSync(adModuleDependencyPath, 'utf8');
  const adModuleDependency = xmlParser.parse(adModuleDependencyXml);
  const dependencies = adModuleDependency.data.AD_MODULE_DEPENDENCY;
  const dependencyJavaPackages = [];
  if (Array.isArray(dependencies)) {
    dependencies.forEach(dep => {
      dependencyJavaPackages.push(moduleMap[dep.AD_DEPENDENT_MODULE_ID]);
    });
  } else {
    dependencyJavaPackages.push(moduleMap[dependencies.AD_DEPENDENT_MODULE_ID]);
  }
  return dependencyJavaPackages;
}

const copyDependencies = (module, template, moduleMap) => {
  template.peerDependencies = template.peerDependencies || {};

  const dependencyJavaPackages = getDependantModules(module, moduleMap);
  const originalDependenciesInfo = {
    name: module,
    dependencies: {
      ...template.dependencies,
      ...template.devDependencies,
      ...template.peerDependencies,
    },
  };
  const copiedDependencies = {};
  copyDeps(
    dependencyJavaPackages,
    originalDependenciesInfo,
    copiedDependencies,
    moduleMap
  );
  for (const [depName, version] of Object.entries(copiedDependencies)) {
    template.peerDependencies[depName] = version;
  }
};

const getModuleInfo = m => {
  const adModulePath = path.resolve(modulesDir, m, source_data_path, ad_module);
  const adModuleXml = fs.readFileSync(adModulePath, 'utf8');
  const adModule = xmlParser.parse(adModuleXml);
  const moduleInfo = adModule.data.AD_MODULE;
  return moduleInfo;
};

const getTemplate = moduleReactPath => {
  const template_path = path.resolve(moduleReactPath, package_json_template);
  const template = JSON.parse(fs.readFileSync(template_path));
  return template;
};

const generatePackageJson = (m, moduleMap) => {
  const moduleReactPath = path.resolve(modulesDir, m, reactDir, m);
  const template = getTemplate(moduleReactPath);
  const moduleInfo = getModuleInfo(m);
  template.name = moduleInfo.JAVAPACKAGE;
  template.version = moduleInfo.VERSION;
  template.description = moduleInfo.DESCRIPTION;
  copyDependencies(m, template, moduleMap);
  const package_json_path = path.resolve(moduleReactPath, package_json);
  fs.writeFileSync(package_json_path, JSON.stringify(template, null, 2));
};

const modulesDir = path.resolve('modules');
const moduleMap = buildModuleMap(modulesDir);
fs.readdirSync(modulesDir)
  .filter(m =>
    fs.existsSync(
      path.resolve(modulesDir, m, reactDir, m, package_json_template)
    )
  )
  .forEach(m => {
    generatePackageJson(m, moduleMap);
  });
