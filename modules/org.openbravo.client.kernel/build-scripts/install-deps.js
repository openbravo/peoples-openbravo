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
/* eslint-disable no-console */

/**
 * node script that installs the npm dependencies defined in the package.json of the Openbravo root and
 * on openbravo modules that include a package.json file.
 *
 * it runs npm ci to ensure that a clean installation is done, enforcing the use of the dependency versions
 * included in the package-lock files
 *
 * dependencies are not hoisted, the dependencies defined in each module will be installed in a node_modules
 * folder in the module folder where the package.json file is included
 *
 * symlinks are created to ensure that a given module has access to the dependencies defined in modules that it
 * depends on
 */

const NpmDependencyReader = require('./NpmDependencyReader');
const execSync = require('child_process').execSync;
const path = require('path');
const fs = require('fs');

const WEB_JSPACK = 'web-jspack';
const PACKAGE_JSON = 'package.json';
const GLOBAL_MODULES = 'node_modules_global';

const npmDependencyReader = new NpmDependencyReader();

const modulesDir = path.resolve('modules');
const globalModulesPath = path.resolve(GLOBAL_MODULES);

// prepares folder where links to openbravo node modules will be linked
execSync(`rm -rf ${globalModulesPath}`, { stdio: 'inherit' });
execSync(`mkdir -p ${globalModulesPath}`, { stdio: 'inherit' });

// install modules in openbravo root rolder
// ignore scripts to avoid a infinite loop caused by this script already being executed as part of a npm script
execSync('npm ci --ignore-scripts', { stdio: 'inherit' });

getModules()
  .filter(module => moduleContainsPackageJson(module))
  .forEach(module => {
    const packageJsonPaths = [
      path.resolve(modulesDir, module),
      path.resolve(modulesDir, module, WEB_JSPACK, module)
    ];
    packageJsonPaths.forEach(packageJsonPath => {
      if (fs.existsSync(path.resolve(packageJsonPath, PACKAGE_JSON))) {
        execSync('mkdir -p node_modules', {
          stdio: 'inherit',
          cwd: packageJsonPath
        });
        console.log(`Installing node modules in ${packageJsonPath}`);
        console.log(`npm ci...`);
        execSync('npm ci', { stdio: 'inherit', cwd: packageJsonPath });
        console.log(
          `Running npm link to make ${packageJsonPath} available to other modules`
        );
        const fromPath = `${globalModulesPath}`;
        const toPath = `${packageJsonPath}`;
        const relativePath = path.relative(fromPath, toPath);
        console.log(
          `Executing ln -fs ${relativePath} ${module} in ${globalModulesPath}`
        );
        execSync(`ln -fs ${relativePath} ${module}`, {
          stdio: 'inherit',
          cwd: `${globalModulesPath}`
        });
        console.log(
          `Creating links for ${module} dependencies in ${packageJsonPath}`
        );
        linkInheritedDependencies(module, packageJsonPath);
      }
    });
  });

function getModules() {
  return fs.readdirSync(modulesDir);
}

function moduleContainsPackageJson(m) {
  const modulesDir = path.resolve('modules');
  const paths = [
    path.resolve(modulesDir, m, PACKAGE_JSON),
    path.resolve(modulesDir, m, WEB_JSPACK, m, PACKAGE_JSON)
  ];
  return paths.some(path => fs.existsSync(path));
}

function getScopeAndName(npmDependency) {
  let scope;
  let packageName;
  if (npmDependency.startsWith('@')) {
    const slashIndex = npmDependency.indexOf('/');
    scope = npmDependency.substring(0, slashIndex);
    packageName = npmDependency.substring(slashIndex + 1);
  } else {
    scope = '';
    packageName = npmDependency;
  }
  return { scope, packageName };
}

function linkInheritedDependencies(module, packageJsonPath) {
  const inheritedDependencies = npmDependencyReader.getInheritedNpmDependencies(
    module
  );
  Object.keys(inheritedDependencies)
    .filter(obDependency => obDependency !== '../../')
    .forEach(obDependency => {
      const npmDependencies = inheritedDependencies[obDependency];
      execSync(`ln -fs ${globalModulesPath}/${obDependency} ${obDependency}`, {
        stdio: 'inherit',
        cwd: `${packageJsonPath}/node_modules`
      });
      Object.keys(npmDependencies).forEach(depFullName => {
        const { scope, packageName } = getScopeAndName(depFullName);
        if (scope.length > 0) {
          execSync(`mkdir -p ${scope}`, {
            stdio: 'inherit',
            cwd: `${packageJsonPath}/node_modules`
          });
        }
        const fromPath = `${packageJsonPath}/node_modules/${scope}`;
        const toPath = `${globalModulesPath}/${obDependency}/node_modules/${scope}/${packageName}`;
        const relativePath = path.relative(fromPath, toPath);
        execSync(`ln -fs ${relativePath} ${packageName}`, {
          stdio: 'inherit',
          cwd: `${packageJsonPath}/node_modules/${scope}`
        });
      });
    });
}
