# Javascript Linter Scripts

In order to run these scripts, you need to have npm and nodejs installed.

## Enabling pretxncommit checking:
### Core
Add these lines in your hgrc file:
```
[hooks]
pretxncommit = <path-to-openbravo>/modules/org.openbravo.client.kernel/jslint/jscheck-hg
```

### Modules
Add these lines to your hgrc file in the .hg directory in your module:

```
[hooks]
pretxncommit = ../org.openbravo.client.kernel/jslint/jscheck-module-hg
```

## Running jslint directly

### Core
To run the linter to all js files in the project, run the following in Openbravo root folder:
```
 ./modules/org.openbravo.client.kernel/jslint/jscheck
```

### Modules
To run jslint directly for a module, go to the module directory and do:

```
 ../org.openbravo.client.kernel/jslint/jscheck-module
``` 

**NOTE:**
 it is possible that you have to set the executable flag on the jslint and jscheck scripts in org.openbravo.client.kernel/jslint.

