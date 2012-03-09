


define(['builder',
        'model/terminal', 'model/order', 
        'components/hwmanager', 
        'components/catalog', 'components/editline', 'components/order', 'components/payment', 'components/keyboard'
        ], function (B) {
  

  return function (modelterminal, hwserver) {
    
    //// Model
    var modelcategories = new OB.MODEL.CategoryCol();
    var modelproducts = new OB.MODEL.ProductCol();
    
    var modelorder = new OB.MODEL.Order();
    
    //// Views
    var ordereditor = new OB.COMP.OrderView($('#ordercontainer'));
    ordereditor.setModel(modelorder);  
  
    
    B.set('modelproducts', modelproducts);
    B.set('modelorder', modelorder);
    B.set('orderviewstack', ordereditor.orderview.stack);
    
    var catalog = new OB.COMP.Catalog($('#catalog'));
    catalog.setModel(modelcategories, modelproducts, modelorder, ordereditor.orderview.stack);
    
    var lineeditor = B({kind: OB.COMP.EditLine });
    $('#edition').append(lineeditor.$);
    var payment = B({kind: OB.COMP.Payment });   
    $('#payment').append(payment.$);
    
    
    
    
    
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