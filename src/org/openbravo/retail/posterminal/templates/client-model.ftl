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

  OB.Data.Registry.registerModel(${data.modelName});
}());