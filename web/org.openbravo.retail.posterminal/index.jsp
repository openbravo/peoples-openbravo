<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml" manifest="../../org.openbravo.mobile.core/OBPOS_Main/AppCacheManifest?_appName=WebPOS">
<head>
  <title>Openbravo POS</title>
  <meta charset="utf-8">
  <meta name="description" content="Openbravo Point of Sale window">
  <meta name="author" content="Openbravo, S.L.U.">

  <%@include file="../org.openbravo.mobile.core/assets/include/mobile.jsp" %>

  <link rel="shortcut icon" type="image/x-icon" href="../../web/images/favicon.ico" />
  <link rel="stylesheet" type="text/css" href="../../org.openbravo.mobile.core/OBCLKER_Kernel/StyleSheetResources?_appName=WebPOS"/>

  <script src="../../org.openbravo.mobile.core/OBMOBC_Main/Lib?_id=Enyo"></script>
  <script src="../../org.openbravo.mobile.core/OBMOBC_Main/Lib?_id=Deps"></script>
  <script src="../org.openbravo.client.kernel/js/LAB.min.js"></script>
  <script src="../../org.openbravo.mobile.core/OBMOBC_Main/StaticResources?_appName=WebPOS"></script>
  <script src="js/libs/jquery-1.7.2.js"></script>
  <script src="js/libs/core-min.js"></script>
  <script src="js/libs/sha1-min.js"></script>

  <script>
    (function () {
      // catch uncaught errors
      window.onerror = function (e, url, line) {
        var errorInfo;
        if (typeof (e) === 'string') {
          errorInfo = e + '. Line number: ' + line + '. File uuid: ' + url + '.';
          if (OB.UTIL.error) {
            OB.UTIL.error("index.jsp: " + errorInfo);
          } else {
            console.error("index.jsp: " + errorInfo);
          }
        }
      };
      // manage manifest
      window.addEventListener('load', function (e) {
        // manage manifest
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
</head>
<body class="ob-body-standard">
  <script>
    (function () {
      if ((typeof OB !== 'undefined') && (typeof OB.POS !== 'undefined')) {
        OB.POS.terminal = new OB.UI.Terminal({
          terminal: OB.MobileApp.model
        });
        // replace this body content with the final application
        OB.POS.terminal.renderInto(document.body);
      } else {
        console.error("The WebPOS cannot be loaded. Please, reload (F5). If this error raises again:\n\n- check that the server has finished its initialization\n- check that the javascript files do not contain syntax errors\n- check that the server calls have proper timeouts\n- check that the session is not being invalidated in the server\n");
        document.write("<p style='margin-left: 20px'>The WebPOS cannot be loaded</br>Please reload (F5)</br></br><i>If this error keeps showing, please contact the system administrator</i></p>".fontcolor("lightgray"));
      }
    }());
  </script>
</body>
</html>
