/*
 ************************************************************************************
 * Copyright (C) 2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/* global module __dirname */

require('../../../web/org.openbravo.retail.posterminal/app/external-device/printing/PrintTemplate');
const fs = require('fs');
const path = require('path');

/**
 * Gets a print template mock with the content of the provided file
 *
 * @param {string} path - The path to a file with the template content
 * @return {OB.App.Class.PrintTemplate} - The print template mock
 */
function getPrintTemplateMock(path) {
  const printTemplate = new OB.App.Class.PrintTemplate('testTemplate', path);
  printTemplate.getData = jest.fn().mockResolvedValue(getFileContent(path));
  return printTemplate;
}

function getFileContent(fileName) {
  return fs.readFileSync(path.resolve(__dirname, './', fileName), 'utf-8');
}

module.exports = getPrintTemplateMock;
