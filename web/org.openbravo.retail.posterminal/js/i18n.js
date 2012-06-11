/*global define */

define(['utilities'], function () {

  // Mockup for OB.I18N

  OB = window.OB || {};
  OB.I18N = window.OB.I18N || {};

  // Hardcoded US labels.
  OB.I18N.labels = {
    'OBPOS_CompanyClient': 'Client: ',
    'OBPOS_CompanyOrg': 'Organization: ',
    'OBPOS_CompanyPriceList': 'Price list: ',
    'OBPOS_CompanyCurrency': 'Currency: ',
    'OBPOS_CompanyLocation': 'Location: ',
    'OBPOS_User': 'User: ',
    'OBPOS_Role': 'Role: ',

    'OBPOS_WelcomeMessage': 'Welcome to Openbravo POS',
    'OBPOS_NoLineSelected': 'There is no line selected',
    'OBPOS_DeleteLine': 'Deleted line %0 x %1',
    'OBPOS_AddLine': 'Added line %0 x %1',
    'OBPOS_AddUnits': 'Added %0 x %1',
    'OBPOS_RemoveUnits': 'Removed %0 x %1',
    'OBPOS_SetUnits': 'Set %0 x %1',
    'OBPOS_SetPrice': 'Set price %0 to %1',
    'OBPOS_SetBP': 'Assigned customer %0',
    'OBPOS_ResetBP': 'Removed customer',
    'OBPOS_LblUndo': 'Undo',

    'OBPOS_DataMaxReached': 'It has been reached the maximum data to display.',
    'OBPOS_LblSuccess': 'SUCCESS',
    'OBPOS_LblError': 'ERROR!',
    'OBPOS_LblWarning': 'WARNING!',

    'OBPOS_MsgReceiptSaved': 'Receipt no: %0 has been closed successfully.',
    'OBPOS_MsgReceiptNotSaved': 'Receipt no: %0 cannot be closed.',
    'OBPOS_ButtonDelete': 'Delete',
    'OBPOS_LineDescription': 'Description',
    'OBPOS_LineQuantity': 'Quantity',
    'OBPOS_LinePrice': 'Price',
    'OBPOS_LineValue': 'LineValue',
    'OBPOS_LineDiscount': 'Discount',
    'OBPOS_LineTotal': 'Total',

    'OBPOS_KbQuantity': 'QUANTITY',
    'OBPOS_KbPrice': 'PRICE',
    'OBPOS_KbDiscount': 'DISCOUNT',
    'OBPOS_KbCode': 'UPC/EAN',
    'OBPOS_KbUPCEANCodeNotFound': 'UPC/EAN code not found: %0',

    'OBPOS_ReceiptNew': 'New Receipt',
    'OBPOS_ReceiptTaxes': 'Taxes',
    'OBPOS_ReceiptTotal': 'TOTAL',
    'OBPOS_ToBeReturned': 'To be Returned',

    'OBPOS_LblCategories': 'Categories',
    'OBPOS_LblNoCategory': '(No category)',

    'OBPOS_SearchNoResults': 'No results',
    'OBPOS_SearchAllCategories': '(All categories)',
    'OBPOS_SearchButtonSearch': ' Search',

    'OBPOS_PaymentsEmpty': 'No payments',
    'OBPOS_PaymentsRemaining': ' remaining to pay. ',
    'OBPOS_PaymentsChange': ' change. ',
    'OBPOS_PaymentsOverpayment': ' overpayment. ',
    'OBPOS_LblDone': 'Done',

    'OBPOS_WeightZero': 'No weight on the scale.',

    'OBPOS_MsgApplicationServerNotAvailable': 'Application server is not available.',
    'OBPOS_MsgHardwareServerNotAvailable': 'Printer and display are not available.',
    'OBPOS_MsgScaleServerNotAvailable': 'Scale is not available.',
    'OBPOS_MsgTemplateNotAvailable': 'Template is not available.',
    'OBPOS_MsgHardwareServerNotDefined': 'Hardware server URL is not defined.',
    'OBPOS_MsgPaymentAmountError': 'Payment amount not valid.\nContact your administrator.',
    'OBPOS_MsgPaymentTypeError': 'Payment type not valid.\nContact your administrator.',
    'OBPOS_MsgPaymentAmountZero': 'Total is zero. No payment needed',


    // Point of sale window
    'OBPOS_LogoutDialogText': 'You may have tickets pending which will be deleted when you log out.\nAre you sure you want to log out?',
    'OBPOS_LogoutDialogLogout': 'Log Out',
    'OBPOS_LogoutDialogLock': 'Lock Screen',
    'OBPOS_LogoutDialogCancel': 'Cancel',

    'OBPOS_MsgConfirmDelete': 'Do you really want to delete the current receipt?',
    'OBPOS_LblAssignCustomer': 'Assign a customer to this receipt',
    'OBPOS_LblAssignReceipt': 'Receipts',

    'OBPOS_LblNew': ' NEW',
    'OBPOS_LblDelete': ' DELETE',
    'OBPOS_LblPrint': ' PRINT',
    'OBPOS_LblMenu': ' MENU',
    'OBPOS_LblOpenbravoWorkspace': 'Openbravo Workspace',
    'OBPOS_LblReturn': 'Return this receipt',
    'OBPOS_LblCloseCash': 'Cash up',
    'OBPOS_LblPay': ' PAY',
    'OBPOS_LblBrowse': 'BROWSE',
    'OBPOS_LblSearch': 'SEARCH',
    'OBPOS_LblScan': 'SCAN',
    'OBPOS_LblEdit': 'EDIT',

    // Close cash window
    'OBPOS_LblCountCash': 'Count Cash',
    'OBPOS_LblPendingReceipts': 'Pending Receipts',

    'OBPOS_LblNextStep': 'Next ',
    'OBPOS_LblPrevStep': ' Previous',
    'OBPOS_LblCancel': 'Cancel',
    	
    	
    // Cash management window
    'OBPOS_LblDepositsDrops': 'Cash Deposits and Drops',
    'OBPOS_LblDepositsDropsMsg': 'Drop or deposit cash using the numerical pad and tap Done to complete',

    'OBPOS_LblDoneStep': 'Done',
    'OBPOS_LblStartCash': ' Starting cash',
    'OBPOS_LblTotalCash': 'Total cash tendered today',
    'OBPOS_LblTotalCash': 'Avaulable in cash',
    'OBPOS_LblDepositCash': 'Deposit Cash',
    'OBPOS_LblDropCash': 'Drop Cash',
    'OBPOS_LblDepositVoucher': 'Deposit Voucher',
    'OBPOS_LblDrop Voucher': 'Drop Voucher'
    	
  };


  OB.I18N.getLabel = function(key, params, object, property) {
    if (!OB.I18N.labels[key]) {
      return '** ' + key + ' **';
    }
    var label = OB.I18N.labels[key], i;
    if (params && params.length && params.length > 0) {
        for (i = 0; i < params.length; i++) {
            label = label.replace("%" + i, params[i]);
        }
    }
    return label;
  };

  OB.I18N.formatCurrency = function (num) {
    // Hardcoded to US Locale.
    return OB.I18N.formatGeneralNumber(num, {
      decimals: 2,
      decimal: '.',
      group: ',',
      currency: '$#'});
  };

  OB.I18N.formatRate = function (num) {
    // Hardcoded to US Locale.
    return OB.I18N.formatGeneralNumber(num * 100, {
      decimals: 2,
      decimal: '.',
      group: ',',
      currency: '#%'});
  };

  OB.I18N.formatGeneralNumber = function (num, options) {
    var n = num.toFixed(options.decimals);
    var x = n.split('.');
    var x1 = x[0];
    var x2 = x.length > 1 ? options.decimal + x[1] : '';
    var rgx = /(\d+)(\d{3})/;
    while (rgx.test(x1)) {
      x1 = x1.replace(rgx, '$1' + options.group + '$2');
    }
    if (options.currency) {
      return options.currency.replace("#", x1 + x2);
    } else {
      return x1 + x2;
    }
  };

  OB.I18N.formatDate = function (d) {
    var curr_date = d.getDate();
    var curr_month = d.getMonth();
    var curr_year = d.getFullYear();
    var curr_hour = d.getHours();
    var curr_min = d.getMinutes();
    var curr_sec = d.getSeconds();
    return OB.UTIL.padNumber(curr_date, 2) + '/' + OB.UTIL.padNumber(curr_month, 2) + '/' + curr_year;
  };

  OB.I18N.formatHour = function (d) {
    var curr_date = d.getDate();
    var curr_month = d.getMonth();
    var curr_year = d.getFullYear();
    var curr_hour = d.getHours();
    var curr_min = d.getMinutes();
    var curr_sec = d.getSeconds();
    return OB.UTIL.padNumber(curr_hour, 2) + ':' + OB.UTIL.padNumber(curr_min, 2);
  };



});