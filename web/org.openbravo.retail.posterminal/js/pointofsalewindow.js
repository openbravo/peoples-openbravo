(function (OBPOS) {

  // window definition
  var SalesWindow = {};
  
  SalesWindow.Terminal = new OBPOS.Sales.Terminal($("#terminal"), $("#status")); // Global, used by other objects...
  SalesWindow.Catalog = new OBPOS.Sales.Catalog($('#catalog'));
  SalesWindow.OrderTable = new OBPOS.Sales.Order($('#ordercontainer'));
  SalesWindow.OrderTable.clear();
  SalesWindow.EditLine = new OBPOS.Sales.EditLine($('#edition'));
  SalesWindow.Payment = new OBPOS.Sales.Payment($('#payment'));

  SalesWindow.Terminal.addReadyListener(function (t) {   
    // executed when all terminal data is loaded
    newOrder();
  });
  
  SalesWindow.Catalog.addSelectListener(function (source, product) {
    SalesWindow.OrderTable.addProduct(product);
  });
  
  SalesWindow.OrderTable.addChangeListener(function (receipt) {
    SalesWindow.Payment.calculate(receipt);
  }); 
  
  SalesWindow.OrderTable.addSelectListener(function (l, line) {
    SalesWindow.EditLine.editLine(l, line);
    
    if ( l >= 0) {
      OBPOS.Sales.hw.print('res/printline.xml', {line: line });
    } else {
      OBPOS.Sales.hw.print('res/welcome.xml');
    }
    
  });
  SalesWindow.OrderTable.addClickListener(function (l, line) {
    $('#editionlink').tab('show');
  });
  SalesWindow.EditLine.addClickListener(function (key) {
    keyPressed(key);
  });
  
  SalesWindow.Payment.addCloseListener(function () {
    OBPOS.Sales.hw.print('res/printreceipt.xml', { order: SalesWindow.OrderTable.getReceipt()} );
    newOrder();
  });
   
  // $('#edition').load('editline.html');

  $('#btnnew').click(function () {
    newOrder();
  });
  
  $('#btnprint').click(function () {
    OBPOS.Sales.hw.print('res/printreceipt.xml', { order: SalesWindow.OrderTable.getReceipt()} );
  });

  function newOrder() {
    $('#cataloglink').tab('show');
    SalesWindow.OrderTable.clear();
    SalesWindow.Catalog.reloadCategories();
    SalesWindow.EditLine.cleanLine();
  }
  
  $(window).keypress(function(e) {
    keyPressed(String.fromCharCode(e.which));
  });  
  
  function keyPressed(key) {
    if (key === '-') {
      SalesWindow.OrderTable.removeUnit(SalesWindow.EditLine.getNumber());
    } else if (key === '+') {
      SalesWindow.OrderTable.addUnit(SalesWindow.EditLine.getNumber());
    } else if (key === '*') {
      SalesWindow.OrderTable.setUnit(SalesWindow.EditLine.getNumber());
    } else if (key === 'x') {
      SalesWindow.OrderTable.removeLine();
    } else if (key === String.fromCharCode(13)) {
      OBPOS.Sales.DSProduct.find({
        uPCEAN: SalesWindow.EditLine.getString()
      }, function (data) {
        if (data) {      
          SalesWindow.OrderTable.addProduct(data);
        } else {
          alert('UPC/EAN code not found');
        }
      });
    } else {
      SalesWindow.EditLine.typeKey(key)
    }
  }

  $(document).ready(function () {

    SalesWindow.Terminal.init();

  });


}(window.OBPOS));