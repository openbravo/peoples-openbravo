CACHE MANIFEST

NETWORK:
${data.network}

<#if data.notInDevelopment>
# Version: ${data.version}
CACHE:
# Libs 
../../web/org.openbravo.client.kernel/js/BigDecimal-all-1.0.1.min.js
../../web/org.openbravo.client.kernel/js/LAB.min.js
../../web/org.openbravo.retail.posterminal/js/libs/jquery-1.7.2.js
../../web/org.openbravo.retail.posterminal/js/libs/underscore-1.3.3.js
../../web/org.openbravo.retail.posterminal/js/libs/backbone-0.9.2.js
../../web/org.openbravo.retail.posterminal/js/libs/core-min.js
../../web/org.openbravo.retail.posterminal/js/libs/sha1-min.js
../../web/org.openbravo.retail.posterminal/js/libs/enyo.js
../../web/org.openbravo.retail.posterminal/js/libs/onyx/onyx.js

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
../../web/org.openbravo.retail.posterminal/js/model/session.js  
../../web/org.openbravo.retail.posterminal/js/arithmetic.js  
../../web/org.openbravo.retail.posterminal/js/model/order.js
../../org.openbravo.client.kernel/OBPOS_Main/ClientModel?entity=FinancialMgmtTaxRate&modelName=TaxRate&source=org.openbravo.retail.posterminal.master.TaxRate
../../org.openbravo.client.kernel/OBPOS_Main/ClientModel?entity=PricingProductPrice&modelName=ProductPrice&source=org.openbravo.retail.posterminal.master.ProductPrice

../../org.openbravo.client.kernel/OBPOS_Main/ClientModel?entity=PricingAdjustment&modelName=Discount&source=org.openbravo.retail.posterminal.master.Discount
../../org.openbravo.client.kernel/OBPOS_Main/ClientModel?entity=PricingAdjustmentBusinessPartner&modelName=DiscountFilterBusinessPartner&source=org.openbravo.retail.posterminal.master.DiscountFilterBusinessPartner
../../org.openbravo.client.kernel/OBPOS_Main/ClientModel?entity=PricingAdjustmentBusinessPartnerGroup&modelName=DiscountFilterBusinessPartnerGroup&source=org.openbravo.retail.posterminal.master.DiscountFilterBusinessPartnerGroup
../../org.openbravo.client.kernel/OBPOS_Main/ClientModel?entity=PricingAdjustmentProduct&modelName=DiscountFilterProduct&source=org.openbravo.retail.posterminal.master.DiscountFilterProduct
../../org.openbravo.client.kernel/OBPOS_Main/ClientModel?entity=PricingAdjustmentProductCategory&modelName=DiscountFilterProductCategory&source=org.openbravo.retail.posterminal.master.DiscountFilterProductCategory

../../web/org.openbravo.retail.posterminal/res/printline.xml
../../web/org.openbravo.retail.posterminal/res/printreceipt.xml
#eee
# Images
<#list data.imageFileList as imageFile>
${imageFile}
</#list>

# CSS
<#list data.cssFileList as cssFile>
${cssFile}
</#list>

# Generated file
../../org.openbravo.client.kernel/OBPOS_Main/StaticResources?_appName=WebPOS
../../web/js/gen/${data.fileName}.js

</#if>