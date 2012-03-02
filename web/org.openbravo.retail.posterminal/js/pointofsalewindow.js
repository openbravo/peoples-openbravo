


define([
        'model/terminal', 'model/order', 
        'components/hwmanager', 
        'components/catalog', 'components/editline', 'components/order', 'components/payment'], function () {
  

  return function (modelterminal, hwserver) {
    
    //// Model
    var modelcategories = new OB.MODEL.CategoryCol();
    var modelproducts = new OB.MODEL.ProductCol();
    
    var modelorder = new OB.MODEL.Order();
    
    //// Views
    var ordereditor = new OB.COMP.OrderView($('#ordercontainer'));
    ordereditor.setModel(modelorder);  
  
    var catalog = new OB.COMP.Catalog($('#catalog'));
    catalog.setModel(modelcategories, modelproducts, modelorder, ordereditor.orderview.stack);
    
  
    
    var lineeditor = new OB.COMP.EditLine($('#edition'));
    lineeditor.setModel(modelproducts, modelorder, ordereditor.orderview.stack);
    
    var payment = new OB.COMP.Payment($('#payment'));
    payment.setModel(modelorder);
    
    var hwview = new OB.COMP.HWManager(hwserver);
    hwview.setModel(modelorder, ordereditor.orderview.stack);
    
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
     
    modelterminal.on('ready', function() {
      // Load of master data
      modelproducts.ds.load({'priceListVersion': modelterminal.get('pricelistversion').id });
      // modelproducts.load();
      
      modelcategories.load();
      
      modelorder.reset();
    });
    

  };
});