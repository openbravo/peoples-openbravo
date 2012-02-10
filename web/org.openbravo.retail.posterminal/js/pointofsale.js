(function (OBPOS) {
  
  var Sales = {};
  
  
  Sales.Catalog = function (tbodyCat, tbodyProd) {
    this.tbodyCat = tbodyCat;
    this.tbodyProd = tbodyProd;
    this.selectedCategory = null;
    this.observers = [];
    
    this.qCategories = new OBPOS.Query('from OBPOS_CategoriesImage where $readableCriteria and parentPOSCategory.id = :pOSCategory order by name');  
    this.qRootCategories = new OBPOS.Query('from OBPOS_CategoriesImage where $readableCriteria and parentPOSCategory.id is null order by name');  
    
    this.qProducts = new OBPOS.Query('from OBPOS_ProductView ' +
        'where $readableCriteria and priceListVersion.id = :priceListVersion and pOSCategory.id = :pOSCategory and isCatalog = true ' +
        'order by pOSLine, name');    
  };
  
  Sales.Catalog.prototype.reloadCategories = function () {
    var me = this;
    this.qRootCategories.exec({
    }, function (data) {
      if (data.exception) {
        alert (data.exception.message);
      } else {
        var table = me.tbodyCat.empty();
        me.selectedCategory = null;
        for (var i in data){
          var tr = $('<tr/>').attr("id", "catrow-" + data[i].id);
          tr.append($('<td/>').append(getThumbnail(data[i].bindaryData)));
          tr.append($('<td/>').text(data[i]._identifier));
          tr.click((function (id, name) { return function(){
            me.reloadProducts(id, name);
          };}(data[i].id, data[i]._identifier)));
          table.append(tr);
        }
        
        // reload first product
        if (data.length > 0) {
          me.reloadProducts(data[0].id, data[0]._identifier);
        } else {
          me.tbodyProd.empty();
        }
      }      
    });
  }
  
  Sales.Catalog.prototype.reloadProducts = function (posCategory, posCategoryName) { // 
    
    var me = this;
    this.qProducts.exec({
        'priceListVersion':Sales.config.pricelistversion.id,
        'pOSCategory':posCategory
      }, function (data) {
        if (data.exception) {
          alert (data.exception.message);
        } else {
          
          // disable previous category
          if (me.selectedCategory) {
            $('#catrow-' + me.selectedCategory).css('background-color', '').css('color', '');
          }
          me.selectedCategory = posCategory;
          // enable current category
          $('#catrow-' + me.selectedCategory).css('background-color', '#049cdb').css('color', '#fff');
          
          $('#category').text(posCategoryName);
          var table = $('#productbody').empty();
          for (var i in data){
            var tr = $('<tr/>');
            tr.append($('<td/>').append(getThumbnail(data[i].binaryData)));
            tr.append($('<td/>').text(data[i]._identifier));
            tr.append($('<td/>').text(OBPOS.Format.formatNumber(data[i].netListPrice, {decimals: 2, decimal : ',', group:'.', currency: '# €'})));
            tr.click((function (p) { return function(){
              for (var i = 0, max = me.observers.length; i < max; i++) {
                me.observers[i].addProduct(p);
              } 
            };}(data[i])));
            table.append(tr);            
            table.append(tr);
          }
        }
      }
    );
  }   
  
  Sales.Catalog.prototype.addObserver = function (o) {
    this.observers.push(o);
  }
  
  function getThumbnail(base64, contentType) {
    var url = (base64)
      ? 'data:' + 
        (contentType ? contentType : 'image/png') +
        ';base64,' + base64
      : 'img/box.png';
    return $('<div/>')
    .css('height', '48')
    .css('width', '48')
    .css('background', 'url(' + url + ') center center no-repeat')
    .css('background-size', 'contain');
//    return $('<img/>')
//          .attr('src', 'data:' + 
//          (contentType ? contentType : 'image/png') +
//          ';base64,' + base64)
//          .attr('height' , '48')
//          .attr('width', '48');
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


  // Order list
  Sales.OrderTable = function(tbody) {
    this.observers = [];
    this.tbody = tbody;
    
    this.render = function (l) {
      var tr = $('<tr/>');
      tr.append($('<td/>').css('width', '40%').text(l.productname));
      tr.append($('<td/>').css('width', '20%').css('text-align', 'right').text(l.qty));
      tr.append($('<td/>').css('width', '20%').css('text-align', 'right').text(OBPOS.Format.formatNumber(l.price, {decimals: 2, decimal : ',', group:'.', currency: '# €'})));
      tr.append($('<td/>').css('width', '20%').css('text-align', 'right').text(OBPOS.Format.formatNumber(l.price * l.qty, {decimals: 2, decimal : ',', group:'.', currency: '# €'})));
      return tr;
    };
  }
  
  Sales.OrderTable.prototype.clear = function () {
    this.lines = [];
    this.tbody.empty();    
    this.notify();
  }
  
  Sales.OrderTable.prototype.addProduct = function (p) {
    var l = {
        productname: p._identifier,
        qty: 1,
        price: p.netListPrice
    };
    this.addLine(l);
  }
  
  Sales.OrderTable.prototype.addLine = function (l) {
    this.lines.push(l);
    this.tbody.append(this.render(l));    
    this.notify();
  }
  
  Sales.OrderTable.prototype.addObserver = function (o) {
    this.observers.push(o);
  }
  
  Sales.OrderTable.prototype.notify = function () {
    for (var i = 0, max = this.observers.length; i < max; i++) {
      this.observers[i].notify(this.lines);
    }
  }  
  
  // Total
  Sales.OrderTotal = function(elem) {
    this.elem = elem;
    this.render = function (lines) {
      var t = 0;
      for (var i = 0, max = lines.length; i < max; i++) {
        t += lines[i].price * lines[i].qty;
      }
      this.elem.text(OBPOS.Format.formatNumber(t, {decimals: 2, decimal : ',', group:'.', currency: '# €'}));
    }
  }
  
  Sales.OrderTotal.prototype.notify = function (lines) {
    this.render(lines);
  }
  
  // window definition
  
  
  var SalesWindow = {};
  
  SalesWindow.catalog = new Sales.Catalog($('#categorybody'), $('#productbody'));  
  SalesWindow.OrderTable = new Sales.OrderTable($('#ordertable'));  
  SalesWindow.OrderSummary = new Sales.OrderTotal($('#totalnet'));
  
  SalesWindow.catalog.addObserver(SalesWindow.OrderTable);
  SalesWindow.OrderTable.addObserver(SalesWindow.OrderSummary);
  SalesWindow.OrderTable.clear();
  
  $('#btnnew').click(function() {
    SalesWindow.OrderTable.clear();
    SalesWindow.catalog.reloadCategories();
  });
  
  $(document).ready(function () {
    
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
    SalesWindow.catalog.reloadCategories();
  }
  
  
  // Public Sales
  OBPOS.Sales = Sales;
  
}(window.OBPOS));