<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml" manifest="../../org.openbravo.client.kernel/OBPOS_Main/AppCacheManifest?_appName=WebPOS">
<head>
  <title>Openbravo POS</title>
  <meta charset="utf-8">
  <meta name="description" content="Openbravo Point of Sale window">
  <meta name="author" content="Openbravo, S.L.U.">

  <%@include file="../org.openbravo.mobile.core/assets/include/mobile.jsp" %>

  <link rel="shortcut icon" type="image/x-icon" href="../../web/images/favicon.ico" />
  <link rel="stylesheet" type="text/css" href="../../org.openbravo.client.kernel/OBCLKER_Kernel/StyleSheetResources?_appName=WebPOS"/>
</head>

<body class="enyo-body-fit webkitOverflowScrolling ob-body-standard">

  <script src="../../org.openbravo.client.kernel/OBMOBC_Main/Lib?_id=Enyo"></script>
  <script src="../../org.openbravo.client.kernel/OBMOBC_Main/Lib?_id=Deps"></script>
  <script src="../org.openbravo.client.kernel/js/LAB.min.js"></script>
  <script src="../../org.openbravo.client.kernel/OBMOBC_Main/StaticResources?_appName=WebPOS"></script>
  <script src="js/libs/jquery-1.7.2.js"></script>
  <script src="js/libs/core-min.js"></script>
  <script src="js/libs/sha1-min.js"></script>

  <script>
    (function () {
      window.addEventListener('load', function (e) {
        window.applicationCache.addEventListener('updateready', function (e) {
          if (window.applicationCache.status == window.applicationCache.UPDATEREADY) {
            OB.Dal.find(OB.Model.Order, {}, function (orders) {
              if (orders.models.length == 0) {
                //There are no pending orders, we can safely swap the cache
                window.applicationCache.swapCache();
                window.location.reload();
              } else {
                OB.ModelApp.view.$.containerWindow.getRoot().$.DatabaseDialog.show();
              }
            }, function () {
              window.console.error(arguments);
            });
          } else {
            // Manifest didn't change
          }
        }, false);
      }, false);
    }());
  </script>

  <script>
    if ((typeof OB !== "undefined") && (typeof OB.POS !== "undefined")) {
      OB.POS.terminal = new OB.UI.Terminal({
        terminal: OB.POS.modelterminal
      });
      OB.POS.terminal.write();
    } else {
      console.error('Cannot find OB namespace. Please, reload (F5). If this error raises again, check that the javascript files do not contain syntax errors.');
    }
  </script>
</body>
</html>
