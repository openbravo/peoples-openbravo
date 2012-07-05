/*global B, $, _ */

(function () {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.PointOfSale = OB.COMP.CustomView.extend({
    createView: function () {

      return (
        {kind: B.KindJQuery('section'), content: [

          {kind: OB.MODEL.Order},
          {kind: OB.MODEL.OrderList},

          {kind: OB.DATA.Container, content: [
            {kind: OB.COMP.HWManager, attr: { 'templateline': 'res/printline.xml', 'templatereceipt': 'res/printreceipt.xml'}}
          ]},

          {kind: OB.DATA.OrderDiscount},
          {kind: OB.DATA.OrderTaxes},
          {kind: OB.DATA.OrderSave},

          {kind: OB.COMP.ModalBPs},
          {kind: OB.COMP.ModalReceipts},
          {kind: B.KindJQuery('div'), attr: {'class': 'row', 'style': 'margin-bottom: 5px'}, content: [
            {kind: B.KindJQuery('div'), attr: {'class': 'span4'}, content: [
              {kind: B.KindJQuery('ul'), attr: {'class': 'unstyled nav-pos row-fluid'}, content: [
                {kind: B.KindJQuery('li'), attr: {'class': 'span3'}, content: [
                  {kind: B.KindJQuery('div'), attr: {'style': 'margin: 0px 5px 0px 5px;'}, content: [
                    {kind: OB.COMP.ButtonNew}
                  ]}
                ]},
                {kind: B.KindJQuery('li'), attr: {'class': 'span3'}, content: [
                  {kind: B.KindJQuery('div'), attr: {'style': 'margin: 0px 5px 0px 5px;'}, content: [
                    {kind: OB.COMP.ModalDeleteReceipt},
                    {kind: OB.COMP.ButtonDelete}
                  ]}
                ]},
                {kind: B.KindJQuery('li'), attr: {'class': 'span3'}, content: [
                  {kind: B.KindJQuery('div'), attr: {'style': 'margin: 0px 5px 0px 5px;'}, content: [
                    {kind: OB.COMP.ButtonPrint}
                  ]}
                ]},
                {kind: B.KindJQuery('li'), attr: {'class': 'span3'}, content: [
                  {kind: B.KindJQuery('div'), attr: {'style': 'margin: 0px 5px 0px 5px;'}, content: [
                    {kind: OB.COMP.ToolbarMenuButton.extend({icon: 'btn-icon btn-icon-menu'/*, label: OB.I18N.getLabel('OBPOS_LblMenu')*/}), content: [
                      {kind: OB.COMP.MenuReturn},
                      {kind: OB.COMP.MenuInvoice},
                      {kind: OB.COMP.MenuSeparator},
                      {kind: OB.COMP.MenuItem.extend({href: '../..', target: '_blank', onclick: 'return true;', label:OB.I18N.getLabel('OBPOS_LblOpenbravoWorkspace')})},
                      {kind: OB.COMP.MenuItem.extend({href: OB.POS.hrefWindow('retail.cashmanagement'), label: OB.I18N.getLabel('OBPOS_LblCashManagement')})},
                      {kind: OB.COMP.MenuItem.extend({href: OB.POS.hrefWindow('retail.cashup'), label: OB.I18N.getLabel('OBPOS_LblCloseCash')})}
                    ]}
                  ]}
                ]}
              ]}
            ]},
            {kind: B.KindJQuery('div'), attr: {'class': 'span8'}, content: [
              {kind: B.KindJQuery('ul'), attr: {'class': 'unstyled nav-pos row-fluid'}, content: [
                {kind: B.KindJQuery('li'), attr: {'class': 'span3'}, content: [
                  {kind: B.KindJQuery('div'), attr: {'data-toggle': 'tab', 'style': 'margin: 0px 5px 0px 5px;'}, content: [
                    {kind: OB.COMP.ButtonTabPayment}
                  ]}
                ]},
                {kind: B.KindJQuery('li'), attr: {'class': 'span3'}, content: [
                  {kind: B.KindJQuery('div'), attr: {'data-toggle': 'tab', 'style': 'margin: 0px 5px 0px 5px;'}, content: [
                    {kind: OB.COMP.ButtonTabScan}
                  ]}
                ]},
                {kind: B.KindJQuery('li'), attr: {'class': 'span2'}, content: [
                  {kind: B.KindJQuery('div'), attr: {'data-toggle': 'tab', 'style': 'margin: 0px 5px 0px 5px;'}, content: [
                    {kind: OB.COMP.ButtonTabBrowse}
                  ]}
                ]},
                {kind: B.KindJQuery('li'), attr: {'class': 'span2'}, content: [
                  {kind: B.KindJQuery('div'), attr: {'data-toggle': 'tab', 'style': 'margin: 0px 5px 0px 5px;'}, content: [
                    {kind: OB.COMP.ButtonTabSearch}
                  ]}
                ]},
                {kind: B.KindJQuery('li'), attr: {'class': 'span2'}, content: [
                  {kind: B.KindJQuery('div'), attr: {'data-toggle': 'tab', 'style': 'margin: 0px 5px 0px 5px;'}, content: [
                    {kind: OB.COMP.ButtonTabEditLine }
                  ]}
                ]}
              ]}
            ]}
          ]},

          {kind: B.KindJQuery('div'), attr: {'class': 'row'}, content: [

            {kind: B.KindJQuery('div'), attr: {'class': 'span6'}, content: [

              {kind: B.KindJQuery('div'), attr: {'style': 'overflow:auto; margin: 5px'}, content: [
                {kind: B.KindJQuery('div'), attr: {'style': 'position: relative;background-color: #ffffff; color: black;'}, content: [
                  {kind: OB.COMP.ReceiptsCounter},
                  {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px;'}, content: [
                    {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
                      {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
                        {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 10px 0px; border-bottom: 1px solid #cccccc;'}, content: [
                            {kind: OB.COMP.OrderDetails},
                            {kind: OB.COMP.BusinessPartner},
                            {kind: B.KindJQuery('div'), attr: {'style': 'clear:both;'}}
                        ]}
                      ]}
                    ]},
                    {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid', 'style': 'max-height: 503px; overflow: auto;'}, content: [
                      {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
                        {kind: OB.COMP.OrderView}
                      ]}
                    ]}
                  ]}
                ]}
              ]}
            ]},

            {kind: B.KindJQuery('div'), attr: {'class': 'span6'}, content: [
              {kind: B.KindJQuery('div'), attr: {'class': 'tab-content'}, content: [
                {kind: OB.COMP.TabScan },
                {kind: OB.COMP.TabBrowse },
                {kind: OB.COMP.TabSearch },
                {kind: OB.COMP.TabEditLine },
                {kind: OB.COMP.TabPayment }
              ]},
              {kind: OB.COMP.KeyboardOrder}
            ]}
          ]}

        ], init: function () {
          this.context.on('domready', function () {
            var orderlist = this.context.modelorderlist,
            criteria={
                'hasbeenpaid' : 'N'
              };
            OB.Dal.find(OB.MODEL.Order, criteria, function (fetchedOrderList) { //OB.Dal.find success
              var currentOrder = {};
              if (!fetchedOrderList || fetchedOrderList.length === 0) {
                // If there are no pending orders, 
                //  add an initial empty order
                orderlist.addNewOrder();
              } else {
                var transformedOrderList = [], transformedOrder;
                // The order object is stored in the json property of the row fetched from the database
                _.each(fetchedOrderList.models, function(model) {
                  transformedOrder = JSON.parse(model.get('json'));
                  // Get the id from the model, in case it was
                  // not stored in the json attribute
                  transformedOrder.id = model.get('id');
                  transformedOrderList.push(transformedOrder);
                });
                orderlist.reset(transformedOrderList);
                // At this point it is sure that there exists at least one order
                currentOrder = new OB.MODEL.Order(transformedOrderList[0]);
                orderlist.load(currentOrder);
                // Only show the pending receipts modal window if there are at
                // least two orders pending
                if (fetchedOrderList.length > 1) {
                  $('#modalreceipts').modal('show');
                }
              }
            }, function () { //OB.Dal.find error
              // If there is an error fetching the pending orders, 
              // add an initial empty order
              orderlist.addNewOrder();
            });
          }, this);
        }}
      );
    }
  });

  // Register window
  OB.POS.windows['retail.pointofsale'] = OB.COMP.PointOfSale;

  // Register required models/data
  OB.DATA['retail.pointofsale'] = [OB.Model.TaxRate, OB.Model.Product, OB.Model.ProductPrice, OB.Model.ProductCategory, OB.Model.BusinessPartner, OB.MODEL.Order];
}());
