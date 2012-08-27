CACHE MANIFEST

# Version: ${data.version}

NETWORK:
${data.network}

CACHE:
# Libs
../../web/org.openbravo.client.kernel/js/BigDecimal-all-1.0.1.min.js
../../web/org.openbravo.retail.posterminal/js/libs/less/less-1.3.0.min.js  
../../web/org.openbravo.client.kernel/js/LAB.min.js  
../../web/org.openbravo.retail.posterminal/js/libs/jquery-1.7.2.js
../../web/org.openbravo.retail.posterminal/js/libs/underscore-1.3.3.js
../../web/org.openbravo.retail.posterminal/js/libs/backbone-0.9.2.js
../../web/org.openbravo.retail.posterminal/js/libs/bootstrap/js/bootstrap-tab.js
../../web/org.openbravo.retail.posterminal/js/libs/bootstrap/js/bootstrap-dropdown.js
../../web/org.openbravo.retail.posterminal/js/libs/bootstrap/js/bootstrap-modal.js
../../web/org.openbravo.retail.posterminal/js/libs/bootstrap/js/bootstrap-alert.js
../../web/org.openbravo.retail.posterminal/js/libs/bootstrap/js/bootstrap-button.js
../../web/org.openbravo.retail.posterminal/js/libs/mbp-helper.js
../../web/org.openbravo.retail.posterminal/js/libs/enyo.js

# Boot code 
../../web/org.openbravo.client.application/js/utilities/ob-utilities-date.js
../../web/org.openbravo.retail.posterminal/js/datasource.js
../../web/org.openbravo.retail.posterminal/js/utilities.js
../../web/org.openbravo.retail.posterminal/js/utilitiesui.js
../../web/org.openbravo.retail.posterminal/js/i18n.js
../../web/org.openbravo.retail.posterminal/js/components/clock.js
../../web/org.openbravo.retail.posterminal/js/components/commonbuttons.js
../../web/org.openbravo.retail.posterminal/js/model/terminal.js
../../web/org.openbravo.retail.posterminal/js/components/terminal.js
../../web/org.openbravo.retail.posterminal/js/login/model/login-model.js
../../web/org.openbravo.retail.posterminal/js/login/view/login.js
../../web/org.openbravo.retail.posterminal/js/data/dal.js
../../web/org.openbravo.retail.posterminal/js/model/product-category.js  
../../web/org.openbravo.retail.posterminal/js/model/product.js  
../../web/org.openbravo.retail.posterminal/js/model/businesspartner.js  
../../web/org.openbravo.retail.posterminal/js/model/document-sequence.js  
../../web/org.openbravo.retail.posterminal/js/model/user.js  


# Images
<#list data.imageFileList as imageFile>
${imageFile}
</#list>

# CSS
<#list data.cssFileList as cssFile>
${cssFile}
</#list>

# Labels
../../org.openbravo.client.kernel/OBPOS_Main/Labels

# Generated file
../../org.openbravo.client.kernel/OBPOS_Main/StaticResources?_appName=WebPOS
#../../web/js/gen/${data.fileName}.js