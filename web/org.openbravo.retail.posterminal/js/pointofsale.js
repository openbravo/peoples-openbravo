(function (OBPOS) {
  
  var Sales = {};
  
  var qCategories = new OBPOS.Query('from OBPOS_CategoriesImage where $readableCriteria and parentPOSCategory.id = :pOSCategory order by name');  
  var qRootCategories = new OBPOS.Query('from OBPOS_CategoriesImage where $readableCriteria and parentPOSCategory.id is null order by name');  
  
  var qProducts = new OBPOS.Query('from OBPOS_ProductView ' +
      'where $readableCriteria and priceListVersion.id = :priceListVersion and pOSCategory.id = :pOSCategory and isCatalog = true ' +
      'order by pOSLine, name');
  
  function reloadCategories() {
    qRootCategories.exec({
    }, function (data) {
      if (data.exception) {
        alert (data.exception.message);
      } else {
        var table = $('#categorybody').empty();
        for (var i in data){
          var tr = $('<tr/>');
          tr.append($('<td/>').append(getThumbnail(data[i].bindaryData)));
          tr.append($('<td/>').text(data[i]._identifier));
          tr.click((function (id, name) { return function(){
            reloadProducts(id, name);
          };}(data[i].id, data[i]._identifier)));
          table.append(tr);
        }
        
        // reload first product
        if (data.length > 0) {
          reloadProducts(data[0].id, data[0]._identifier);
        } else {
          $('#productbody').empty();
        }
      }      
    });
  }
  
  function reloadProducts(posCategory, posCategoryName) { // 
    
    qProducts.exec({
        'priceListVersion':Sales.config.pricelistversion.id,
        'pOSCategory':posCategory
      }, function (data) {
        if (data.exception) {
          alert (data.exception.message);
        } else {
          $('#category').text(posCategoryName);
          var table = $('#productbody').empty();
          for (var i in data){
            var tr = $('<tr/>');
            tr.append($('<td/>').append(getThumbnail(data[i].binaryData)));
            tr.append($('<td/>').text(data[i]._identifier));
            tr.append($('<td/>').text(OBPOS.Format.formatNumber(data[i].netListPrice, {decimals: 2, decimal : ',', group:'.', currency: '# €'})));
            table.append(tr);
          }
        }
      }
    );
  }
  
  function getThumbnail(base64, contentType) {
    return $('<div/>')
    .css('height', '48')
    .css('width', '48')
    .css('background', 'url(data:' + 
        (contentType ? contentType : 'image/png') +
        ';base64,' + base64 + ') center center no-repeat')
    .css('background-size', 'contain');
//    return $('<img/>')
//          .attr('src', 'data:' + 
//          (contentType ? contentType : 'image/png') +
//          ';base64,' + base64)
//          .attr('height' , '48')
//          .attr('width', '48');
  }
  
  
  $(document).ready(function () {
    $("#btncategories").click(reloadCategories);
    $("#btnproducts").click(function() { reloadProducts('456FE871DA3C46A4B76D1EE9E905048A');});
    
    var t = OBPOS.getParameterByName("terminal") || "POS-1";
    
    new OBPOS.Query('from OBPOS_Applications where $readableCriteria and searchKey = :terminal')
    .exec({
      terminal: t
    }, function (data) {
      if (data.exception) {
        location = '../org.openbravo.client.application.mobile/login.jsp';
      } else if (data[0]) {        
        Sales.terminal = data[0];
        Sales.config = {};
        
        $("#terminal").text(Sales.terminal._identifier);
        
        printStatus();
      } else {
        alert("Terminal does not exists: " + t);
      }
    });
    
  });
  
  function readyData() {
    // executed when all config data is loaded
    reloadCategories();
  }
  
  function printStatus() {
    
    if (!Sales.config.bplocation) {
      new OBPOS.Query('from BusinessPartnerLocation where id = (select min(id) from BusinessPartnerLocation where businessPartner.id = :bp and $readableCriteria)')
      .exec({
        bp: Sales.terminal.businessPartner
      }, function (data){
        if (data[0]) {
          Sales.config.bplocation = data[0];
          printStatus();
        } else {
          Sales.config.bplocation = {};
        }          
      });
    }
    
    if (!Sales.config.location) {
      new OBPOS.Query('from Location where id = (select min(locationAddress) from OrganizationInformation where organization.id = :org and $readableCriteria)')
      .exec({
        org: Sales.terminal.organization
      }, function (data){
        if (data[0]) {
          Sales.config.location = data[0];
          printStatus();
        } else {
          Sales.config.location = {};
        }
      });
    }   

    if (!Sales.config.pricelist) {
      new OBPOS.Query('from PricingPriceList where id =:pricelist and $readableCriteria')
      .exec({
        pricelist: Sales.terminal.priceList
      }, function (data){
        if (data[0]) {        
          Sales.config.pricelist = data[0];
          printStatus();
        } else {
          Sales.config.pricelist = {};
        }          
      });      
    }
  
    if (!Sales.config.pricelistversion) {
      new OBPOS.Query('select plv.id AS id from PricingPriceListVersion AS plv where plv.$readableCriteria and plv.priceList.id =:pricelist and plv.validFromDate = (select max(pplv.validFromDate) from PricingPriceListVersion as pplv where pplv.priceList.id = :pricelist)')
      .exec({
        pricelist: Sales.terminal.priceList
      }, function (data){
        if (data[0]) {
          Sales.config.pricelistversion = data[0];
          printStatus();
          readyData();
        } else {
          Sales.config.pricelistversion = {};
        }        
      });      
    }
  
    var line1 = Sales.terminal['client._identifier'] + " | " + Sales.terminal['organization._identifier'];
    if (Sales.config.pricelist) {
      line1 += Sales.config.pricelist._identifier + " | " + Sales.config.pricelist['currency._identifier'];
    }    

    var line2 = "";
    if (Sales.config.location) {
      line2 += Sales.config.location._identifier;
    }
    $("#status").html(line1 + "<br/>" + line2);
  }

  
  OBPOS.Sales = Sales;
  
}(window.OBPOS));