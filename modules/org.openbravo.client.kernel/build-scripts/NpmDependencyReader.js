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
 * All portions are Copyright (C) 2021 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

const path = require('path');
const fs = require('fs');

const WEB_JSPACK = 'web-jspack';
const PACKAGE_JSON = 'package.json';
const PACKAGE_PART_JSON = 'package-part.json';
const SOURCE_DATA_PATH = 'src-db/database/sourcedata';
const AD_MODULE = 'AD_MODULE.xml';
const AD_MODULE_DEPENDENCY = 'AD_MODULE_DEPENDENCY.xml';

/**
 * Class that exposes the getInheritedNpmDependencies function that, given the java package of a module,
 * will return an object describing the list of npm dependencies defined in modules from with the given
 * module depends
 *
 * For instance, if:
 * - module org.openbravo.module1 has a dependency to org.openbravo.module2
 * - module org.openbravo.module2 has a dependency to org.openbravo.module3
 * - module org.openbravo.module2 defines an npm dependency to package1 with semver ^1.2.3
 * - module org.openbravo.module3 defines an npm dependency to package1 with semver 4.5.6
 * a call to getInheritedNpmDependencies('org.openbravo.module1') will return:
 *   {
 *     'org.openbravo.module2': { package1: '^1.2.3' },
 *     'org.openbravo.module3': { package2: '4.5.6'}
 *   }
 */
class NpmDependencyReader {
  constructor() {
    this.modulesDir = path.resolve('modules');
    this.moduleMap = this.buildModuleMap();
  }

  getInheritedNpmDependencies(module) {
    const obDependencies = this.getOpenbravoDependencies(module);
    let allInheritedDependencies = {};
    obDependencies.forEach(({ modulePath }) => {
      if (this.hasReactPath(modulePath)) {
        const { dependencies, devDependencies } = this.getNpmDependencies(
          modulePath
        );
        allInheritedDependencies[modulePath] = {
          ...this.getNameAndVersion(dependencies),
          ...this.getNameAndVersion(devDependencies)
        };
      }
      const inheritedDependencies = this.getInheritedNpmDependencies(
        modulePath
      );
      allInheritedDependencies = {
        ...allInheritedDependencies,
        ...inheritedDependencies
      };
    });
    return allInheritedDependencies;
  }

  getOpenbravoDependencies(module) {
    const adModuleDependencyPath = this.getAdModuleDependencyPath(module);
    if (!fs.existsSync(adModuleDependencyPath)) {
      return [];
    }
    const adModuleDependencyXml = fs.readFileSync(
      adModuleDependencyPath,
      'utf8'
    );

    const regex = /<AD_DEPENDENT_MODULE_ID><!\[CDATA\[(.*)\]\]><\/AD_DEPENDENT_MODULE_ID>/g;
    const adDependencies = [];
    let match = regex.exec(adModuleDependencyXml);

    while (match) {
      adDependencies.push(match[1]);
      match = regex.exec(adModuleDependencyXml);
    }

    const dependencies = [];
    adDependencies.forEach(dep => {
      dependencies.push({
        modulePath: this.moduleMap[dep].modulePath
      });
    });
    return dependencies;
  }

  getNpmDependencies(module) {
    const packageFilePaths = this.getPackageFilePaths(module);
    const packageFilePath = packageFilePaths.find(path => fs.existsSync(path));
    if (packageFilePath) {
      const jsonContent = JSON.parse(fs.readFileSync(packageFilePath));
      return {
        dependencies: { ...jsonContent.dependencies },
        devDependencies: { ...jsonContent.devDependencies }
      };
    }
    return {
      dependencies: {},
      devDependencies: {}
    };
  }

  buildModuleMap() {
    // initializes map with path to root module
    const map = {
      0: {
        modulePath: '../../'
      }
    };
    fs.readdirSync(this.modulesDir)
      .filter(m => fs.existsSync(this.getSourceDataPath(m)))
      .forEach(m => {
        const moduleInfo = this.getModuleInfo(m);
        map[moduleInfo.id] = {
          modulePath: moduleInfo.javaPackage
        };
      });
    return map;
  }

  getModuleInfo(module) {
    const regExProperties = {
      id: /<AD_MODULE_ID><!\[CDATA\[(.*)\]\]><\/AD_MODULE_ID>/,
      javaPackage: /<JAVAPACKAGE><!\[CDATA\[(.*)\]\]><\/JAVAPACKAGE>/
    };
    const adModulePath = this.getAdModulePath(module);
    const adModuleXml = fs.readFileSync(adModulePath, 'utf8');
    const moduleInfo = {};
    Object.keys(regExProperties).forEach(prop => {
      const match = adModuleXml.match(regExProperties[prop]);
      if (match) {
        [, moduleInfo[prop]] = adModuleXml.match(regExProperties[prop]);
      }
    });
    return moduleInfo;
  }

  hasReactPath(module) {
    const moduleReactPath = this.getWebJspackPath(module);
    return fs.existsSync(moduleReactPath);
  }

  getNameAndVersion(dependencies) {
    return Object.entries(dependencies).reduce((map, [depName, version]) => {
      map[depName] = version;
      return map;
    }, {});
  }

  getWebJspackPath(module) {
    return path.resolve(this.modulesDir, module, WEB_JSPACK, module);
  }

  getAdModulePath(module) {
    return path.resolve(this.modulesDir, module, SOURCE_DATA_PATH, AD_MODULE);
  }

  getSourceDataPath(module) {
    return path.resolve(this.modulesDir, module, SOURCE_DATA_PATH, AD_MODULE);
  }

  getAdModuleDependencyPath(module) {
    return path.resolve(
      this.modulesDir,
      module,
      SOURCE_DATA_PATH,
      AD_MODULE_DEPENDENCY
    );
  }

  getPackageFilePaths(module) {
    return [
      path.resolve(
        this.modulesDir,
        module,
        WEB_JSPACK,
        module,
        PACKAGE_PART_JSON
      ),
      path.resolve(this.modulesDir, module, WEB_JSPACK, module, PACKAGE_JSON),
      path.resolve(this.modulesDir, module, PACKAGE_PART_JSON),
      path.resolve(this.modulesDir, module, PACKAGE_JSON)
    ];
  }
}
// eslint-disable-next-line no-undef
module.exports = NpmDependencyReader;
