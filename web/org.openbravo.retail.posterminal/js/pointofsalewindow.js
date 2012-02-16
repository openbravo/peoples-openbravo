(function (OBPOS) {

  // window definition
  var SalesWindow = {};
  
  SalesWindow.Terminal = new OBPOS.Sales.Terminal($("#terminal"), $("#status")); // Global, used by other objects...
  SalesWindow.Catalog = new OBPOS.Sales.Catalog($('#catalog'));
  SalesWindow.OrderTable = new OBPOS.Sales.OrderTable($('#ordertable'));
  SalesWindow.OrderTable.clear();
  SalesWindow.OrderSummary = new OBPOS.Sales.OrderTotal($('#totalcontainer'));
  SalesWindow.EditLine = new OBPOS.Sales.EditLine($('#edition'));

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
  SalesWindow.OrderTable.addSelectListener(function (l, line) {
    SalesWindow.EditLine.editLine(l, line);
  });
  SalesWindow.EditLine.addClickListener(function (key) {
    keyPressed(key);
  });
   
  // $('#edition').load('editline.html');

  $('#btnnew').click(function () {
    newOrder();
  });
  


  function newOrder() {
    $('#cataloglink').tab('show');
    SalesWindow.OrderTable.clear();
    SalesWindow.Catalog.reloadCategories();
    SalesWindow.EditLine.cleanLine();
  }
  
  $(window).keypress(function(e) {
    keyPressed(e.which);
  });  
  
  function keyPressed(key) {
    if (key === '-') {
      SalesWindow.OrderTable.removeUnit();
    } else if (key === '+') {
      SalesWindow.OrderTable.addUnit();
    } else if (key === 'x') {
      SalesWindow.OrderTable.removeLine();
    }
  }

  $(document).ready(function () {

    SalesWindow.Terminal.init();

  });


}(window.OBPOS));