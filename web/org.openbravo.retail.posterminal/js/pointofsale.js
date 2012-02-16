(function (OBPOS) {

  var Sales = {};

  Sales.terminal = null;
  Sales.bplocation = null;
  Sales.location = null;
  Sales.pricelist = null;
  Sales.pricelistversion = null;

  function initDataSources() {

    // DataSources...
    Sales.DSProduct = new OBPOS.DataSource(
    new OBPOS.Query('from OBPOS_ProductView where $readableCriteria and priceListVersion.id = :priceListVersion and isCatalog = true'), {
      'priceListVersion': Sales.pricelistversion.id
    });

    Sales.DSCategories = new OBPOS.DataSource(
    new OBPOS.Query('from OBPOS_CategoryView where $readableCriteria and isCatalog = true order by pOSLine, name'));
  }


  function isScrolledIntoView(container, elem) {

    var docViewTop = container.scrollTop();
    var docViewBottom = docViewTop + container.height();

    var elemTop = elem.offset().top;
    var elemBottom = elemTop + elem.height();

    return ((elemBottom >= docViewTop) && (elemTop <= docViewBottom) && (elemBottom <= docViewBottom) && (elemTop >= docViewTop));
  }

  function makeElemVisible(container, elem) {

    var docViewTop = container.offset().top;
    var docViewBottom = docViewTop + container.height();

    var elemTop = elem.offset().top;
    var elemBottom = elemTop + elem.height();

    var currentScroll = container.scrollTop();

    if (elemTop < docViewTop) {
      container.scrollTop(currentScroll - docViewTop + elemTop);
    } else if (elemBottom > docViewBottom) {
      container.scrollTop(currentScroll + elemBottom - docViewBottom);
    }
  }

  // public
  Sales.getThumbnail = function (base64, width, height, contentType) {
    var url = (base64) ? 'data:' + (contentType ? contentType : 'image/png') + ';base64,' + base64 : 'img/box.png';
    return $('<div/>').css('margin', 'auto').css('height', height ? height : '48').css('width', width ? width : '48').css('background', 'url(' + url + ') center center no-repeat').css('background-size', 'contain');
  };

  Sales.Terminal = function (elemt, elems) {
    this.elemt = elemt;
    this.elems = elems;
    this.readyListeners = [];
  }

  Sales.Terminal.prototype.addReadyListener = function (l) {
    this.readyListeners.push(l);
  }

  Sales.Terminal.prototype.fireReadyEvent = function (o) {
    for (var i = 0, max = this.readyListeners.length; i < max; i++) {
      this.readyListeners[i](this);
    }
  }
  Sales.Terminal.prototype.init = function () {

    var me = this;
    var t = OBPOS.getParameterByName("terminal") || "POS-1";

    new OBPOS.Query('from OBPOS_Applications where $readableCriteria and searchKey = :terminal').exec({
      terminal: t
    }, function (data) {
      if (data.exception) {
        location = '../org.openbravo.client.application.mobile/login.jsp';
      } else if (data[0]) {
        Sales.terminal = data[0];
        me.printStatus();
      } else {
        alert("Terminal does not exists: " + t);
      }
    });
  }

  Sales.Terminal.prototype.printStatus = function () {

    var me = this;

    if (!Sales.bplocation) {
      Sales.bplocation = {};
      new OBPOS.Query('from BusinessPartnerLocation where id = (select min(id) from BusinessPartnerLocation where businessPartner.id = :bp and $readableCriteria)').exec({
        bp: Sales.terminal.businessPartner
      }, function (data) {
        if (data[0]) {
          Sales.bplocation = data[0];
          me.printStatus();
        }
      });
    }

    if (!Sales.location) {
      Sales.location = {};
      new OBPOS.Query('from Location where id = (select min(locationAddress) from OrganizationInformation where organization.id = :org and $readableCriteria)').exec({
        org: Sales.terminal.organization
      }, function (data) {
        if (data[0]) {
          Sales.location = data[0];
          me.printStatus();
        }
      });
    }

    if (!Sales.pricelist) {
      Sales.pricelist = {};
      new OBPOS.Query('from PricingPriceList where id =:pricelist and $readableCriteria').exec({
        pricelist: Sales.terminal.priceList
      }, function (data) {
        if (data[0]) {
          Sales.pricelist = data[0];
          me.printStatus();
        }
      });
    }

    if (!Sales.pricelistversion) {
      Sales.pricelistversion = {};
      new OBPOS.Query('select plv.id AS id from PricingPriceListVersion AS plv where plv.$readableCriteria and plv.priceList.id =:pricelist and plv.validFromDate = (select max(pplv.validFromDate) from PricingPriceListVersion as pplv where pplv.priceList.id = :pricelist)').exec({
        pricelist: Sales.terminal.priceList
      }, function (data) {
        if (data[0]) {
          Sales.pricelistversion = data[0];
          me.printStatus();

          initDataSources();

          me.fireReadyEvent();
        }
      });
    }

    var line1 = OBPOS.Sales.terminal['client._identifier'] + " | " + OBPOS.Sales.terminal['organization._identifier'];
    if (Sales.pricelist) {
      line1 += Sales.pricelist._identifier + " | " + Sales.pricelist['currency._identifier'];
    }

    var line2 = "";
    if (Sales.location) {
      line2 += Sales.location._identifier;
    }

    this.elemt.text(OBPOS.Sales.terminal._identifier);
    this.elems.html(line1 + "<br/>" + line2);
  }


  // Order list
  Sales.OrderTable = function (tbody) {
    this.changeListeners = [];
    this.selectListeners = [];
    this.tbody = tbody;

    this.render = function (tr, l) {
      tr.append($('<td/>').css('width', '40%').text(l.productname));
      tr.append($('<td/>').css('width', '20%').css('text-align', 'right').text(l.qty));
      tr.append($('<td/>').css('width', '20%').css('text-align', 'right').text(OBPOS.Format.formatNumber(l.price, {
        decimals: 2,
        decimal: '.',
        group: ',',
        currency: '$ #'
      })));
      tr.append($('<td/>').css('width', '20%').css('text-align', 'right').text(OBPOS.Format.formatNumber(l.price * l.qty, {
        decimals: 2,
        decimal: '.',
        group: ',',
        currency: '$ #'
      })));
    };
  }

  Sales.OrderTable.prototype.createRow = function (l) {
    var me = this;
    var tr = $('<tr/>');
    this.render(tr, l);
    tr.click(function () {
      me.setSelected((me.tbody.children().index(tr)));
    });
    return tr;
  }

  Sales.OrderTable.prototype.clear = function () {
    this.lines = [];
    this.lineselected = -1;

    this.tbody.empty();
    this.fireChangeEvent();
    this.fireSelectEvent(-1, null);
  }

  Sales.OrderTable.prototype.setSelected = function (n) {
    var children = this.tbody.children();
    if (this.lineselected > -1) {
      children.eq(this.lineselected).css('background-color', '').css('color', '');
    }
    this.lineselected = n;
    if (this.lines[this.lineselected]) {
      var elemselected = children.eq(this.lineselected);
      elemselected.css('background-color', '#049cdb').css('color', '#fff');
      makeElemVisible($('#orderscroll'), elemselected);
      this.fireSelectEvent(this.lineselected, this.lines[this.lineselected]);
    } else {
      this.lineselected = -1;
      this.fireSelectEvent(-1, null);
    }
  }


  Sales.OrderTable.prototype.addProduct = function (p) {
    var l = {
      productid: p.id,
      productname: p._identifier,
      qty: 1,
      price: p.netListPrice
    };
    this.addLine(l);
  }

  Sales.OrderTable.prototype.addUnit = function () {
    if (this.lineselected > -1) {
      this.lines[this.lineselected].qty += 1;
      var tr = this.tbody.children().eq(this.lineselected).empty();
      this.render(tr, this.lines[this.lineselected]);
      this.fireChangeEvent();
      this.fireSelectEvent(this.lineselected, this.lines[this.lineselected]);
    }
  }

  Sales.OrderTable.prototype.removeUnit = function () {
    if (this.lineselected > -1) {
      this.lines[this.lineselected].qty -= 1;
      if (this.lines[this.lineselected].qty <= 0) {
        this.removeLine();
      } else {
        var tr = this.tbody.children().eq(this.lineselected).empty();
        this.render(tr, this.lines[this.lineselected]);
        this.fireChangeEvent();
        this.fireSelectEvent(this.lineselected, this.lines[this.lineselected]);
      }
    }
  }

  Sales.OrderTable.prototype.removeLine = function () {
    var l = arguments[0] ? arguments[0] : this.lineselected;
    if (l > -1) {
      this.lines.splice(l, 1);
      this.tbody.children().eq(l).remove();
      this.fireChangeEvent();

      if (l >= this.lines.length) {
        this.setSelected(this.lines.length - 1);
      } else {
        this.setSelected(l);
      }
    }
  }

  Sales.OrderTable.prototype.addLine = function (l) {
    this.lines.push(l);
    this.tbody.append(this.createRow(l));
    this.setSelected(this.lines.length - 1);
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

  Sales.OrderTable.prototype.addSelectListener = function (l) {
    this.selectListeners.push(l);
  }

  Sales.OrderTable.prototype.fireSelectEvent = function (l, line) {
    for (var i = 0, max = this.selectListeners.length; i < max; i++) {
      this.selectListeners[i](l, line);
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
      $('#totalnet').text(OBPOS.Format.formatNumber(t, {
        decimals: 2,
        decimal: '.',
        group: ',',
        currency: '$ #'
      }));
      $('#totalgross').text(OBPOS.Format.formatNumber(t, {
        decimals: 2,
        decimal: '.',
        group: ',',
        currency: '$ #'
      }));
    }
  }

  Sales.OrderTotal.prototype.calculate = function (lines) {
    this.render(lines);
  }

  // EditLine Object
  Sales.EditLine = function (container) {
    var me = this;
    this.container = container;
    this.clickListeners = [];

    container.load('editline.html', function () {
      $('#btnplus').click(function () {
        me.fireClickEvent('+');
      });
      $('#btnminus').click(function () {
        me.fireClickEvent('-');
      });
      $('#btnremove').click(function () {
        me.fireClickEvent('x');
      });
    });
  }

  Sales.EditLine.prototype.cleanLine = function () {
    this.editLine(-1, null)
  }
  
  Sales.EditLine.prototype.editLine = function (l, line) {

    if (l >= 0) {
      OBPOS.Sales.DSProduct.find({
        id: line.productid
      }, function (data) {
        if (data) {
          $('#editlineimage').empty().append(Sales.getThumbnail(data.binaryData, 128, 164));
          $('#editlinename').text(data._identifier);
          $('#editlineqty').text(line.qty);
          $('#editlineprice').text(OBPOS.Format.formatNumber(line.price, {
            decimals: 2,
            decimal: '.',
            group: ',',
            currency: '$ #'
          }));
          $('#editlinenet').text(OBPOS.Format.formatNumber(line.qty * line.price, {
            decimals: 2,
            decimal: '.',
            group: ',',
            currency: '$ #'
          }));
        }
      });
    } else {
      $('#editlineimage').empty();
      $('#editlinename').empty();
      $('#editlineqty').empty();
      $('#editlineprice').empty();
      $('#editlinenet').empty();
    }
  };

  Sales.EditLine.prototype.addClickListener = function (l) {
    this.clickListeners.push(l);
  };

  Sales.EditLine.prototype.fireClickEvent = function (key) {
    for (var i = 0, max = this.clickListeners.length; i < max; i++) {
      this.clickListeners[i](key);
    }
  };

  // Public Sales
  OBPOS.Sales = Sales;

}(window.OBPOS));