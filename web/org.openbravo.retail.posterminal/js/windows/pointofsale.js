/*global B, $, _ */

(function () {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.PointOfSale = OB.COMP.CustomView.extend({
    createView: function () {

      return (
        {kind: B.KindJQuery('section'), content: [

          {kind: OB.Model.Order},
          {kind: OB.Collection.OrderList},

          {kind: OB.DATA.Container, content: [
            {kind: OB.COMP.HWManager, attr: {
              'templateline': 'res/printline.xml',
              'templatereceipt': 'res/printreceipt.xml',
              'templateinvoice': 'res/printinvoice.xml',
              'templatereturn': 'res/printreturn.xml',
              'templatereturninvoice': 'res/printreturninvoice.xml'
            }}
          ]},

          {kind: OB.DATA.OrderDiscount},
          {kind: OB.DATA.OrderTaxes},
          {kind: OB.DATA.OrderSave},

          {kind: OB.COMP.ModalBPs},
          {kind: OB.COMP.ModalProcessReceipts},
          {kind: OB.UI.ModalReceipts},
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
                    {kind: OB.COMP.ToolbarMenu.extend({icon: 'btn-icon btn-icon-menu'/*, label: OB.I18N.getLabel('OBPOS_LblMenu')*/}), content: [
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
                    {kind: OB.UI.ButtonTabBrowse}
                  ]}
                ]},
                {kind: B.KindJQuery('li'), attr: {'class': 'span2'}, content: [
                  {kind: B.KindJQuery('div'), attr: {'data-toggle': 'tab', 'style': 'margin: 0px 5px 0px 5px;'}, content: [
                    {kind: OB.UI.ButtonTabSearch}
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
                            {kind: OB.UI.OrderDetails},
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
                {kind: OB.UI.TabBrowse },
                {kind: OB.UI.TabSearch },
                {kind: OB.COMP.TabEditLine },
                {kind: OB.COMP.TabPayment }
              ]},
              {kind: OB.COMP.KeyboardOrder}
            ]}
          ]}

        ], init: function () {
          var ctx = this.context,
              modelterminal = OB.POS.modelterminal,
              me = this, processPaidOrders;

          processPaidOrders = function() {
            // Processes the paid, unprocessed orders
            var orderlist = me.context.modelorderlist,
              criteria = {
                hasbeenpaid:'Y'
              };
            if (OB.POS.modelterminal.get('connectedToERP')) {
              OB.Dal.find(OB.Model.Order, criteria, function (ordersPaidNotProcessed) { //OB.Dal.find success
                var successCallback, errorCallback;
                if (!ordersPaidNotProcessed) {
                  return;
                }
                successCallback = function() {
                  $('.alert:contains("' + OB.I18N.getLabel('OBPOS_ProcessPendingOrders') +'")').alert('close');
                   OB.UTIL.showSuccess(OB.I18N.getLabel('OBPOS_MsgSuccessProcessOrder'));
                 };
                 errorCallback = function() {
                   $('.alert:contains("' + OB.I18N.getLabel('OBPOS_ProcessPendingOrders') +'")').alert('close');
                   OB.UTIL.showError(OB.I18N.getLabel('OBPOS_MsgErrorProcessOrder'));
                 };
                OB.UTIL.showAlert(OB.I18N.getLabel('OBPOS_ProcessPendingOrders'), OB.I18N.getLabel('OBUIAPP_Info'));
                OB.UTIL.processOrders(me.context, ordersPaidNotProcessed, successCallback, errorCallback);
              });
            }
          };
          this.context.on('domready', function () {
            var loadUnpaidOrders;
            modelterminal.saveDocumentSequenceInDB();

            processPaidOrders();

            loadUnpaidOrders = function() {
              // Shows a modal window with the orders pending to be paid
              var orderlist = me.context.modelorderlist,
                  criteria={
                    'hasbeenpaid' : 'N'
                  };
              OB.Dal.find(OB.Model.Order, criteria, function (ordersNotPaid) { //OB.Dal.find success
                var currentOrder = {}, loadOrderStr;
                if (!ordersNotPaid || ordersNotPaid.length === 0) {
                  // If there are no pending orders,
                  //  add an initial empty order
                  orderlist.addNewOrder();
                } else {
                  // The order object is stored in the json property of the row fetched from the database
                  orderlist.reset(ordersNotPaid.models);
                  // At this point it is sure that there exists at least one order
                  currentOrder = ordersNotPaid.models[0];
                  orderlist.load(currentOrder);
                  loadOrderStr = OB.I18N.getLabel('OBPOS_Order') + currentOrder.get('documentNo') + OB.I18N.getLabel('OBPOS_Loaded');
                  OB.UTIL.showAlert(loadOrderStr, OB.I18N.getLabel('OBUIAPP_Info'));
                }
              }, function () { //OB.Dal.find error
                // If there is an error fetching the pending orders,
                // add an initial empty order
                orderlist.addNewOrder();
              });
            };
            loadUnpaidOrders();

          }, this);
          modelterminal.on('online', function () {
            // When the connection is restored, process the pending orders
            processPaidOrders();
          });
          modelterminal.on('offline', function () {
            OB.UTIL.showWarning(OB.I18N.getLabel('OBPOS_OfflineModeWarning'));
          });
        }}
      );
    }
  });

  // Register window
  OB.POS.windows['retail.pointofsale'] = OB.COMP.PointOfSale;

  // Register required models/data
  OB.DATA['retail.pointofsale'] = [OB.Model.TaxRate, OB.Model.Product, OB.Model.ProductPrice, OB.Model.ProductCategory, OB.Model.BusinessPartner, OB.Model.Order, OB.Model.DocumentSequence];
}());
