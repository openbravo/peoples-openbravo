(function (OBPOS) {
  
  var Sales = {};
  
  var qCategories = new OBPOS.Query('from OBPOS_CategoriesImage where $readableCriteria and parentPOSCategory.id = :pOSCategory order by name');  
  var qRootCategories = new OBPOS.Query('from OBPOS_CategoriesImage where $readableCriteria and parentPOSCategory.id is null order by name');  
  
  var qProducts = new OBPOS.Query('from OBPOS_ProductView ' +
      'where $readableCriteria and priceListVersion.id = :priceListVersion and pOSCategory.id = :pOSCategory and isCatalog = true ' +
      'order by pOSLine, name');
  
  function reloadCategories() {
    qRootCategories.exec({
    }, function (data) {
      if (data.exception) {
        alert (data.exception.message);
      } else {
        var table = $('#categorybody').empty();
        for (var i in data){
          var tr = $('<tr/>');
          tr.append($('<td/>').append(getThumbnail(data[i].bindaryData)));
          tr.append($('<td/>').text(data[i]._identifier));
          tr.click((function (id) { return function(){
            reloadProducts(id);
          };}(data[i].id)));
          table.append(tr);
        }
      }      
    });
  }
  
  function reloadProducts(posCategory) { // 
    
    qProducts.exec({
        'priceListVersion':'8A64B71A2B0B2946012B0BD97329018B',
        'pOSCategory':posCategory
      }, function (data) {
        if (data.exception) {
          alert (data.exception.message);
        } else {
          var table = $('#productbody').empty();
          for (var i in data){
            var tr = $('<tr/>');
            tr.append($('<td/>').append(getThumbnail(data[i].binaryData)));
            tr.append($('<td/>').text(data[i]._identifier));
            tr.append($('<td/>').text(data[i]['pOSCategory._identifier']));
            tr.append($('<td/>').text(OBPOS.Format.formatNumber(data[i].netListPrice, {decimals: 2, decimal : ',', group:'.', currency: '# €'})));
            table.append(tr);
          }
        }
      }
    );
  }
  
  function getThumbnail(base64, contentType) {
    return $('<div/>')
    .css('height', '48')
    .css('width', '48')
    .css('background', 'url(data:' + 
        (contentType ? contentType : 'image/png') +
        ';base64,' + base64 + ') center center no-repeat')
    .css('background-size', 'contain');
//    return $('<img/>')
//          .attr('src', 'data:' + 
//          (contentType ? contentType : 'image/png') +
//          ';base64,' + base64)
//          .attr('height' , '48')
//          .attr('width', '48');
  }
  
  $("#btncategories").click(reloadCategories);
  $("#btnproducts").click(function() { reloadProducts('456FE871DA3C46A4B76D1EE9E905048A');});
    

  
  OBPOS.Sales = Sales;
  
}(window.OBPOS));