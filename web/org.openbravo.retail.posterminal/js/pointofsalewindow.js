


define([], function () {
  
  //// Model
  
  var modelterminal = new OB.MODEL.Terminal();
  
  var modelcategories = new OB.MODEL.CategoryCol();
  var modelproducts = new OB.MODEL.ProductCol();
  
  modelterminal.on('ready', function() {
    // Load of master data
    modelproducts.ds.load({'priceListVersion': modelterminal.get('pricelistversion').id });
    // modelproducts.load();
    
    modelcategories.load();
  });
  
  var modelorder = new OB.MODEL.Order();
  
  //// Views
  
  var terminal = new OBPOS.Sales.Terminal($("#terminal"), $("#status"));
  terminal.setModel(modelterminal);
  
  var ordereditor = new OBPOS.Sales.OrderView($('#ordercontainer'));
  ordereditor.setModel(modelorder);  

  var catalog = new OBPOS.Sales.Catalog($('#catalog'));
  catalog.setModel(modelcategories, modelproducts, modelorder, ordereditor.orderview.stack);
  

  
  var lineeditor = new OBPOS.Sales.EditLine($('#edition'));
  lineeditor.setModel(modelproducts, modelorder, ordereditor.orderview.stack);
  
  var payment = new OBPOS.Sales.Payment($('#payment'));
  payment.setModel(modelorder);
  
  var hwview = new OBPOS.Sales.HWManager(new OB.DS.HWServer('http://192.168.0.8:8090/printer'));
  hwview.setModel(modelorder, ordereditor.orderview.stack);
  
  //// Events
/*
  SalesWindow.Terminal.addReadyListener(function (t) {   
    // executed when all terminal data is loaded
    SalesWindow.modelorder.reset();
    SalesWindow.HWView.hw.print('res/welcome.xml');
  });
 */   
  
  
  modelorder.on('reset', function() {
    $('#cataloglink').tab('show');
    
  });
   
  ordereditor.orderview.stack.on('click', function () {
    $('#editionlink').tab('show');
  });
  

  $('#btnnew').click(function () {
    modelorder.reset();
  });
  
  $('#btnprint').click(function () {
    hwview.printOrder();
  });
  

  $(document).ready(function () {
    modelterminal.load();
   
    hwview.hw.print('res/welcome.xml');
    modelorder.reset();

  });


});