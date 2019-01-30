# Javascript Formatter Scripts

In order to run these scripts, you need to have npm and nodejs installed.

## Enabling pre-commit checking:
### Core
Add these lines in your hgrc file:
```
[hooks]
pre-commit = <path-to-openbravo>/modules/org.openbravo.client.kernel/jsformatter/jscheck-format-hg
```

### Modules
Add these lines to your hgrc file in the .hg directory in your module:

```
[hooks]
pre-commit = ../org.openbravo.client.kernel/jsformatter/jscheck-format-module-hg
```

## Running jslint directly

### Core
To run the formatter to all js files in the project, run the following in Openbravo root folder:
```
 ./modules/org.openbravo.client.kernel/jsformatter/jscheck-format
```

### Modules
To run the formatter directly for a module, go to the module directory and do:

```
 ../org.openbravo.client.kernel/jslint/jsformatter/jscheck-format-module
```

**NOTE:**
 it is possible that you have to set the executable flag on the jslint and jscheck scripts in org.openbravo.client.kernel/jslint.
