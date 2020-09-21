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

const reactDir = 'web-jspack';
const package_json_template = 'package.template.json';
const package_lock_json = 'package-lock.json';
const modulesDir = path.resolve('modules');

const getPackageLock = mod => {
  const modulePackageLockPath = path.resolve(
    modulesDir,
    mod,
    reactDir,
    mod,
    package_lock_json
  );
  if (fs.existsSync(modulePackageLockPath)) {
    return JSON.parse(fs.readFileSync(modulePackageLockPath));
  } else {
    return null;
  }
};

const cleanPackageLock = (dependencies, packageLock) => {
  for (const modKey of Object.keys(dependencies)) {
    const depPackageJson = getPackageLock(modKey);
    if (depPackageJson !== null) {
      for (const depKey in packageLock.dependencies) {
        if (depPackageJson.dependencies[depKey]) {
          delete packageLock.dependencies[depKey];
        } else {
        }
      }
      // const nextDependencies = packageJson.dependencies;
      // cleanPackageLock(nextDependencies, packageLock);
    }
  }
};

fs.readdirSync(modulesDir)
  .filter(
    m =>
      fs.existsSync(
        path.resolve(modulesDir, m, reactDir, m, package_json_template)
      ) &&
      fs.existsSync(path.resolve(modulesDir, m, reactDir, m, package_lock_json))
  )
  .forEach(m => {
    const moduleReactPath = path.resolve(modulesDir, m, reactDir, m);
    const templatePath = path.resolve(moduleReactPath, package_json_template);
    const template = JSON.parse(fs.readFileSync(templatePath));

    const packageLock = getPackageLock(m);

    const dependencies = template.dependencies;

    console.log('packageLock before: ' + JSON.stringify(packageLock));

    cleanPackageLock(dependencies, packageLock);

    console.log('packageLock after: ' + JSON.stringify(packageLock));

    const packageLockPath = path.resolve(moduleReactPath, package_lock_json);
    fs.writeFileSync(packageLockPath, JSON.stringify(packageLock, null, 2));
  });
