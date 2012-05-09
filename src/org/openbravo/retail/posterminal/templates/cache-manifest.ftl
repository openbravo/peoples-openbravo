CACHE MANIFEST

# Version: ${data.version}

NETWORK:
${data.network}

CACHE:
../../web/org.openbravo.client.kernel/js/BigDecimal-all-1.0.1.min.js
<#list data.cache as element>
${element}
</#list>