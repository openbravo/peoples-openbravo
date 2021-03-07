/*
 ************************************************************************************
 * Copyright (C) 2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

const NpmDependenciesValidator = require('../NpmDependenciesValidator');

describe('NpmDependenciesValidator', () => {
  let validator;

  beforeEach(() => {
    validator = new NpmDependenciesValidator();
    validator.getModules = jest.fn(() => {
      return ['module1', 'module2'];
    });

    validator.getPackageJsonPath = jest.fn(module => {
      return `${module}/package.json`;
    });
  });

  it('Does not return warnings or errors if dependency is not included in several modules', async () => {
    validator.readPackageJson = jest.fn(path => {
      if (path === 'module1/package.json') {
        return {
          dependencies: { lodash: '4.17.15' }
        };
      } else if (path === 'module2/package.json') {
        return {
          dependencies: { jest: '26.6.0' }
        };
      } else {
        return {};
      }
    });

    const { warnings, errors } = validator.validate();

    expect(warnings).toHaveLength(0);
    expect(errors).toHaveLength(0);
  });

  it('Returns warning message if dependency is already defined in other module with same version', async () => {
    validator.readPackageJson = jest.fn(() => {
      return {
        dependencies: { lodash: '4.17.15' }
      };
    });

    const { warnings, errors } = validator.validate();

    expect(warnings).toHaveLength(1);
    expect(warnings[0]).toBe(
      'Warning: Package lodash defined in module2/package.json but already defined in module1/package.json with same version 4.17.15'
    );
    expect(errors).toHaveLength(0);
  });

  it('Returns warning message even if one module includes a package as a dependency and another one in as a devDependency', async () => {
    validator.readPackageJson = jest.fn(path => {
      if (path === 'module1/package.json') {
        return {
          dependencies: { lodash: '4.17.15' }
        };
      } else if (path === 'module2/package.json') {
        return {
          devDependencies: { lodash: '4.17.15' }
        };
      } else {
        return {};
      }
    });

    const { warnings, errors } = validator.validate();

    expect(warnings).toHaveLength(1);
    expect(warnings[0]).toBe(
      'Warning: Package lodash defined in module2/package.json but already defined in module1/package.json with same version 4.17.15'
    );
    expect(errors).toHaveLength(0);
  });

  it('Returns error if dependency is already defined in other module with different version', async () => {
    validator.readPackageJson = jest.fn(path => {
      const version = path === 'module1/package.json' ? '4.17.15' : '4.17.16';
      return {
        dependencies: { lodash: version }
      };
    });

    const { warnings, errors } = validator.validate();

    expect(warnings).toHaveLength(0);
    expect(errors).toHaveLength(1);
    expect(errors[0]).toBe(
      'Error: Package lodash defined in module2/package.json with version 4.17.16 but also defined in module1/package.json with version 4.17.15'
    );
  });
});
