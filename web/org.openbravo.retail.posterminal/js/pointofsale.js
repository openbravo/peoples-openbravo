(function (OBPOS) {

  var Sales = {};

  Sales.Catalog = function (tbodyCat, tbodyProd) {
    this.tbodyCat = tbodyCat;
    this.tbodyProd = tbodyProd;
    this.selectedCategory = null;
    this.selectListeners = [];

    this.qRootCategories = new OBPOS.Query('from OBPOS_CategoryView where $readableCriteria and isCatalog = true order by pOSLine, name');

    this.qProducts = new OBPOS.Query('from OBPOS_ProductView where $readableCriteria and priceListVersion.id = :priceListVersion and productCategory.id = :productCategory and isCatalog = true order by pOSLine, name');
  };

  Sales.Catalog.prototype.reloadCategories = function () {
    var me = this;
    this.qRootCategories.exec({}, function (data) {
      if (data.exception) {
        alert(data.exception.message);
      } else {
        var table = me.tbodyCat.empty();
        me.selectedCategory = null;
        for (var i in data) {
          var tr = $('<tr/>').attr("id", "catrow-" + data[i].id);
          tr.append($('<td/>').append(getThumbnail(data[i].bindaryData)));
          tr.append($('<td/>').text(data[i]._identifier));
          tr.click((function (id, name) {
            return function () {
              me.reloadProducts(id, name);
            };
          }(data[i].id, data[i]._identifier)));
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

  Sales.Catalog.prototype.reloadProducts = function (category, categoryName) { // 
    var me = this;
    this.qProducts.exec({
      'priceListVersion': SalesWindow.Terminal.pricelistversion.id,
      'productCategory': category
    }, function (data) {
      if (data.exception) {
        alert(data.exception.message);
      } else {

        // disable previous category
        if (me.selectedCategory) {
          $('#catrow-' + me.selectedCategory).css('background-color', '').css('color', '');
        }
        me.selectedCategory = category;
        // enable current category
        $('#catrow-' + me.selectedCategory).css('background-color', '#049cdb').css('color', '#fff');

        $('#category').text(categoryName);
        var table = $('#productbody').empty();
        for (var i in data) {
          var tr = $('<tr/>');
          tr.append($('<td/>').append(getThumbnail(data[i].binaryData)));
          tr.append($('<td/>').text(data[i]._identifier));
          tr.append($('<td/>').text(OBPOS.Format.formatNumber(data[i].netListPrice, {
            decimals: 2,
            decimal: ',',
            group: '.',
            currency: '# €'
          })));
          tr.click((function (p) {
            return function () {
              // Fire Select Listener event
              for (var i = 0, max = me.selectListeners.length; i < max; i++) {
                me.selectListeners[i](me, p);
              }
            };
          }(data[i])));
          table.append(tr);
          table.append(tr);
        }
      }
    });
  }

  Sales.Catalog.prototype.addSelectListener = function (l) {
    this.selectListeners.push(l);
  }

  function getThumbnail(base64, contentType) {
    var url = (base64) ? 'data:' + (contentType ? contentType : 'image/png') + ';base64,' + base64 : 'img/box.png';
    return $('<div/>').css('height', '48').css('width', '48').css('background', 'url(' + url + ') center center no-repeat').css('background-size', 'contain');
    //    return $('<img/>')
    //          .attr('src', 'data:' + 
    //          (contentType ? contentType : 'image/png') +
    //          ';base64,' + base64)
    //          .attr('height' , '48')
    //          .attr('width', '48');
  }


  Sales.Terminal = function (elemt, elems) {
    this.elemt = elemt;
    this.elems = elems;
    this.readyListeners = [];
    this.terminal = null;
    this.bplocation = null;
    this.location = null;
    this.pricelist = null;
    this.pricelistversion = null;
  }

  Sales.Terminal.prototype.addReadyListener = function (l) {
    this.readyListeners.push(l);
  }

  Sales.Terminal.prototype.fireReadyEvent = function (o) {
    for (var i = 0, max = this.readyListeners.length; i < max; i++) {
      this.readyListeners[i](this);
    }
  }
  Sales.Terminal.prototype.init = function (terminal) {
    this.terminal = terminal;
    this.printStatus();
  }

  Sales.Terminal.prototype.printStatus = function () {

    var me = this;

    if (!this.bplocation) {
      new OBPOS.Query('from BusinessPartnerLocation where id = (select min(id) from BusinessPartnerLocation where businessPartner.id = :bp and $readableCriteria)').exec({
        bp: this.terminal.businessPartner
      }, function (data) {
        if (data[0]) {
          me.bplocation = data[0];
          me.printStatus();
        } else {
          me.bplocation = {};
        }
      });
    }

    if (!this.location) {
      new OBPOS.Query('from Location where id = (select min(locationAddress) from OrganizationInformation where organization.id = :org and $readableCriteria)').exec({
        org: this.terminal.organization
      }, function (data) {
        if (data[0]) {
          me.location = data[0];
          me.printStatus();
        } else {
          me.location = {};
        }
      });
    }

    if (!this.pricelist) {
      new OBPOS.Query('from PricingPriceList where id =:pricelist and $readableCriteria').exec({
        pricelist: this.terminal.priceList
      }, function (data) {
        if (data[0]) {
          me.pricelist = data[0];
          me.printStatus();
        } else {
          me.pricelist = {};
        }
      });
    }

    if (!this.pricelistversion) {
      new OBPOS.Query('select plv.id AS id from PricingPriceListVersion AS plv where plv.$readableCriteria and plv.priceList.id =:pricelist and plv.validFromDate = (select max(pplv.validFromDate) from PricingPriceListVersion as pplv where pplv.priceList.id = :pricelist)').exec({
        pricelist: this.terminal.priceList
      }, function (data) {
        if (data[0]) {
          me.pricelistversion = data[0];
          me.printStatus();
          me.fireReadyEvent();
        } else {
          me.pricelistversion = {};
        }
      });
    }

    var line1 = this.terminal['client._identifier'] + " | " + this.terminal['organization._identifier'];
    if (this.pricelist) {
      line1 += this.pricelist._identifier + " | " + this.pricelist['currency._identifier'];
    }

    var line2 = "";
    if (this.location) {
      line2 += this.location._identifier;
    }

    this.elemt.text(this.terminal._identifier);
    this.elems.html(line1 + "<br/>" + line2);
  }


  // Order list
  Sales.OrderTable = function (tbody) {
    this.changeListeners = [];
    this.tbody = tbody;

    this.render = function (l) {
      var tr = $('<tr/>');
      tr.append($('<td/>').css('width', '40%').text(l.productname));
      tr.append($('<td/>').css('width', '20%').css('text-align', 'right').text(l.qty));
      tr.append($('<td/>').css('width', '20%').css('text-align', 'right').text(OBPOS.Format.formatNumber(l.price, {
        decimals: 2,
        decimal: ',',
        group: '.',
        currency: '# €'
      })));
      tr.append($('<td/>').css('width', '20%').css('text-align', 'right').text(OBPOS.Format.formatNumber(l.price * l.qty, {
        decimals: 2,
        decimal: ',',
        group: '.',
        currency: '# €'
      })));
      return tr;
    };
  }

  Sales.OrderTable.prototype.clear = function () {
    this.lines = [];
    this.tbody.empty();
    this.fireChangeEvent();
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
    this.fireChangeEvent();
  }

  Sales.OrderTable.prototype.addChangeListener = function (l) {
    this.changeListeners.push(l);
  }

  Sales.OrderTable.prototype.fireChangeEvent = function () {
    for (var i = 0, max = this.changeListeners.length; i < max; i++) {
      this.changeListeners[i](this.lines);
    }
  }

  // Total
  Sales.OrderTotal = function (elem) {
    this.elem = elem;
    this.render = function (lines) {
      var t = 0;
      for (var i = 0, max = lines.length; i < max; i++) {
        t += lines[i].price * lines[i].qty;
      }
      this.elem.text(OBPOS.Format.formatNumber(t, {
        decimals: 2,
        decimal: ',',
        group: '.',
        currency: '# €'
      }));
    }
  }

  Sales.OrderTotal.prototype.calculate = function (lines) {
    this.render(lines);
  }

  // window definition
  var SalesWindow = {};

  SalesWindow.Terminal = new Sales.Terminal($("#terminal"), $("#status")); // Global, used by other objects...
  SalesWindow.Catalog = new Sales.Catalog($('#categorybody'), $('#productbody'));
  SalesWindow.OrderTable = new Sales.OrderTable($('#ordertable'));
  SalesWindow.OrderSummary = new Sales.OrderTotal($('#totalnet'));

  SalesWindow.Terminal.addReadyListener(function (t) {
    // executed when all terminal data is loaded
    newOrder();
  });
  SalesWindow.Catalog.addSelectListener(function (source, product) {
    SalesWindow.OrderTable.addProduct(product);
  });
  SalesWindow.OrderTable.addChangeListener(function (lines) {
    SalesWindow.OrderSummary.calculate(lines);
  });
  SalesWindow.OrderTable.clear();

  $('#btnnew').click(function () {
    newOrder();
  });

  function newOrder() {
    SalesWindow.OrderTable.clear();
    SalesWindow.Catalog.reloadCategories();
  }

  $(document).ready(function () {

    var t = OBPOS.getParameterByName("terminal") || "POS-1";

    new OBPOS.Query('from OBPOS_Applications where $readableCriteria and searchKey = :terminal').exec({
      terminal: t
    }, function (data) {
      if (data.exception) {
        location = '../org.openbravo.client.application.mobile/login.jsp';
      } else if (data[0]) {
        SalesWindow.Terminal.init(data[0]);
      } else {
        alert("Terminal does not exists: " + t);
      }
    });

  });

  // Public Sales
  OBPOS.Sales = Sales;

}(window.OBPOS));