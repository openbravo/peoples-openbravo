//jslint

/*global Backbone */

<#function dataType property>
  <#if property.primitive && property.numericType>
    <#return "NUMERIC">
  </#if>
  <#return "TEXT">
</#function>

(function () {

  var ${data.modelName} = Backbone.Model.extend({
    modelName: '${data.modelName}',
    tableName: '${data.tableName}',
    entityName: '${data.entityName}',
    source: '<#if data.source??>${data.source}</#if>',
    properties: [
<#list data.properties as property>
  <#if property.columnName??>
     '${property.name}',
  </#if>
</#list>
     '_identifier',
     '_idx'
    ],
    propertyMap: {
<#list data.properties as property>
  <#if property.columnName??>
     '${property.name}': '${property.columnName?lower_case}',
  </#if>
</#list>
     '_identifier': '_identifier',
     '_idx': '_idx'
    },
    createStatement: '${data.createStatement?js_string}',
    dropStatement: '${data.dropStatement?js_string}',
    insertStatement: '${data.insertStatement?js_string}'
  });

  var ${data.modelName}List = Backbone.Collection.extend({
    model: ${data.modelName}
  });

  window.OB = window.OB || {};
  window.OB.Model = window.OB.Model || {};
  window.OB.Collection = window.OB.Collection || {};

  window.OB.Model.${data.modelName} = ${data.modelName};
  window.OB.Collection.${data.modelName}List = ${data.modelName}List;
}());