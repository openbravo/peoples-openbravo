(function (OBPOS) {

  // window definition
  var SalesWindow = {};

  SalesWindow.Terminal = new OBPOS.Sales.Terminal($("#terminal"), $("#status")); // Global, used by other objects...
  SalesWindow.Catalog = new OBPOS.Sales.Catalog($('#categorybody'), $('#productbody'));
  SalesWindow.OrderTable = new OBPOS.Sales.OrderTable($('#ordertable'));
  SalesWindow.OrderSummary = new OBPOS.Sales.OrderTotal($('#totalnet'));
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
  SalesWindow.EditLine.addClickListener(function (key) {
    keyPressed(key);
  });
  
  SalesWindow.OrderTable.clear();
  
  // $('#edition').load('editline.html');

  $('#btnnew').click(function () {
    newOrder();
  });
  


  function newOrder() {
    $('#cataloglink').tab('show');
    SalesWindow.OrderTable.clear();
    SalesWindow.Catalog.reloadCategories();
  }
  
  $(window).keypress(function(e) {
    console.log('-->' + String.fromCharCode(e.which));
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