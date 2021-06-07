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
/* eslint-disable no-undef */

/**
 * Script that helps to write the license documentation regarding npm dependencies. It looks for npm modules in the
 * base folder of the given module (or in the Openbravo root if no module is specified) and in the web-jspack/<packageName> folder.
 * 
 * It uses the license-checker package to obtain the list of licenses used by the installed dependencies. 
 * 
 * It executes npm ci --ignore-scripts to work with an updated list of dependencies and prevent inconsistencies
 
 * Usage:
 *
 * npm run license-summary
 * --module=<javaPackage> the Openbravo module to be considered. If this parameter is not included, the script will check the licenses of the Openbravo root dependencies
 * --toFile=<path> the path to a file where the output should be written. 
 * 
 * Output: A header for each license type and below it the list of packages that use it
 * 
 */
const checker = require('license-checker');
const execSync = require('child_process').execSync;
const path = require('path');
const fs = require('fs');
const os = require('os');

const WEB_JSPACK = 'web-jspack';
const PACKAGE_JSON = 'package.json';
const DEFAULT_LICENSE_FILENAME = `${getLegalFolder()}/npmLicenses.txt`;

const fileName = process.env.npm_config_toFile || DEFAULT_LICENSE_FILENAME;

const packageJsonPath = getPackageJsonPath();
if (!packageJsonPath) {
  console.log(`No package json found in ${packageJsonPaths}`);
  process.exit(0);
} else {
  console.log(`Processing ${packageJsonPath}`);
}

// Run npm ci to ensure node_modules is updated
execSync('npm ci --ignore-scripts', { stdio: 'inherit', cwd: packageJsonPath });

checker.init(
  {
    start: packageJsonPath
  },
  function(err, packages) {
    if (err) {
      console.error(err);
      process.exit(0);
    } else {
      const groupedLicenses = groupedByLicense(packages);
      printSummary(groupedLicenses);
      console.log(`License summary written to ${fileName}`);
    }
  }
);

function getLegalFolder() {
  const targetModule = process.env.npm_config_module;
  return targetModule ? path.resolve(`modules/${targetModule}/legal`) : 'legal';
}

function getPackageJsonPath() {
  const targetModule = process.env.npm_config_module;
  const targetPath = targetModule
    ? path.resolve(`modules/${targetModule}`)
    : '.';
  if (!fs.existsSync(targetPath)) {
    console.log(`module not found: ${targetModule}`);
    process.exit(1);
  }
  const packageJsonPaths = [targetPath];
  if (targetModule) {
    packageJsonPaths.push(path.resolve(targetPath, WEB_JSPACK, targetModule));
  }
  return packageJsonPaths.find(aPath =>
    fs.existsSync(path.resolve(aPath, PACKAGE_JSON))
  );
}

function groupedByLicense(packages) {
  const groupedByLicense = {};
  Object.keys(packages).forEach(packageName => {
    if (packages[packageName].licenses) {
      const license = packages[packageName].licenses;
      groupedByLicense[license] = [
        ...(groupedByLicense[license] || []),
        packageName
      ];
    } else {
      console.log(`${packages[packageName]} does not have a license!`);
    }
  });
  return groupedByLicense;
}

function printSummary(licenses) {
  fs.writeFileSync(fileName, '\n');
  Object.keys(licenses)
    .sort()
    .forEach(license => {
      const packages = licenses[license];
      fs.appendFileSync(fileName, `Available under ${license} license: \n`);
      fs.appendFileSync(
        fileName,
        `-----------------------------------------------------------\n`
      );
      packages.sort().forEach(package => {
        fs.appendFileSync(fileName, `  ${package}\n`);
      });
      fs.appendFileSync(fileName, '\n');
    });
}
