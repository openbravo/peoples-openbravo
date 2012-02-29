(function (OBPOS) {

  // window definition
  
  var SalesWindow = {}; /// vars
   
  //// Model
  
  var modelterminal = new OBPOS.Model.Terminal();
  
  var modelcategories = new OBPOS.Model.CategoryCol();
  var modelproducts = new OBPOS.Model.ProductCol();
  
  modelterminal.on('ready', function() {
    // Load of master data
    modelproducts.ds.load({'priceListVersion': modelterminal.get('pricelistversion').id });
    // modelproducts.load();
    
    modelcategories.load();
  });
  
  
  
  SalesWindow.modelorder = new OBPOS.Model.Order();
  SalesWindow.stackorder = new OBPOS.Model.Stack();
  SalesWindow.stackorder.setModel(SalesWindow.modelorder.get('lines'));
  
  //// Views
  
  SalesWindow.Terminal = new OBPOS.Sales.Terminal($("#terminal"), $("#status"));
  SalesWindow.Terminal.setModel(modelterminal);

  SalesWindow.Catalog = new OBPOS.Sales.Catalog($('#catalog'));
  SalesWindow.Catalog.setModel(modelcategories, modelproducts, SalesWindow.modelorder, SalesWindow.stackorder);
  
  SalesWindow.OrderTable = new OBPOS.Sales.OrderView($('#ordercontainer'));
  SalesWindow.OrderTable.setModel(SalesWindow.modelorder, SalesWindow.stackorder);
  
  SalesWindow.EditLine = new OBPOS.Sales.EditLine($('#edition'));
  SalesWindow.EditLine.setModel(SalesWindow.modelorder, SalesWindow.stackorder);
  
  SalesWindow.Payment = new OBPOS.Sales.Payment($('#payment'));
  SalesWindow.Payment.setModel(SalesWindow.modelorder);
  
  SalesWindow.HWView = new OBPOS.Sales.HWManager(new OBPOS.HWServer('http://192.168.0.8:8090/printer'));
  SalesWindow.HWView.setModel(SalesWindow.modelorder, SalesWindow.stackorder);
  
  //// Events
/*
  SalesWindow.Terminal.addReadyListener(function (t) {   
    // executed when all terminal data is loaded
    SalesWindow.modelorder.reset();
    SalesWindow.HWView.hw.print('res/welcome.xml');
  });
 */   
  
  
  SalesWindow.modelorder.on('reset', function() {
    $('#cataloglink').tab('show');
  });
   
  SalesWindow.stackorder.on('gotoedit', function () {
    $('#editionlink').tab('show');
  });
  

  $('#btnnew').click(function () {
    SalesWindow.modelorder.reset();
  });
  
  $('#btnprint').click(function () {
    SalesWindow.HWView.printOrder();
  });
  

  $(document).ready(function () {
    modelterminal.load();
   
    SalesWindow.HWView.hw.print('res/welcome.xml');
    SalesWindow.modelorder.reset();
    // SalesWindow.Terminal.init();
  });


}(window.OBPOS));