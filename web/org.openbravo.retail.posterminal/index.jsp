<%@ page import="java.util.Properties" %>
<%@ page import="org.openbravo.base.HttpBaseServlet" %>
<%@ page import="org.openbravo.dal.core.OBContext"%>
<%@ page import="org.openbravo.base.util.OBClassLoader" %>
<%@ page import="org.openbravo.authentication.AuthenticationManager" %>
<%@ page import="org.openbravo.client.kernel.KernelUtils" %>
<%@ page import="org.openbravo.dal.core.OBContext" %>
<%@ page import="org.openbravo.model.ad.module.Module" %>
<%@ page import="org.apache.log4j.Logger" %>
<%
Logger log = Logger.getLogger(org.openbravo.authentication.AuthenticationManager.class);

String terminal = request.getParameter("terminal");
String window = request.getParameter("window");

if (terminal != null) {
  terminal = "terminal=" + terminal;
}
if (window != null) {
  window = "window=" + window;
}

if (terminal != null && window != null) {
  terminal = "?" + terminal;
  window = "&" + window;
} else if (terminal != null && window == null) {
  terminal = "?" + terminal;
  window = "";
} else if (terminal == null && window != null) {
  terminal = "";
  window = "?" + window;
} else {
  terminal = "";
  window = "";
}

request.setAttribute("loginURL", "/web/org.openbravo.retail.posterminal/login.jsp" + terminal + window);

AuthenticationManager authManager = AuthenticationManager.getAuthenticationManager(this);

String userId = authManager.authenticate(request, response);

if(userId == null){
  return;
}
%>
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <title>Openbravo Point of Sale</title>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, user-scalable=no, initial-scale=1.0, maximum-scale=1.0">
  <meta name="description" content="Openbravo Point of Sale window">
  <meta name="author" content="Openbravo, S.L.U.">

  <!--  Apple application capable attributes -->
  <meta name="apple-mobile-web-app-capable" content="yes" />
  <meta name="apple-mobile-web-app-status-bar-style" content="black" />
  <meta name="apple-touch-fullscreen" content="yes" />
  <link rel="apple-touch-startup-image" href="img/openbravopos.png"/>

  <!-- Application icons -->
  <link rel="apple-touch-icon" href="img/openbravopos57x57.png" />
  <link rel="apple-touch-icon" sizes="72x72" href="img/openbravopos72x72.png" />
  <link rel="apple-touch-icon" sizes="114x114" href="img/openbravopos114x114.png" />
  <link rel="shortcut icon" type="image/x-icon" href="../../web/images/favicon.ico" />

  <link rel="stylesheet/less" href="js/libs/bootstrap/less/bootstrap.less">
  <link rel="stylesheet/less" href="js/libs/bootstrap/less/responsive.less">
  <script src="js/libs/less/less-1.3.0.min.js"></script>

  <link rel="stylesheet" type="text/css" href="css/standard.css" />
  <link rel="stylesheet" type="text/css" href="css/login.css" />
</head>

<body style="background-color: darkgray; background: url(img/BACKGROUND-PNG24.png) top left">
  <div id="container" class="container">
    <div id="topsection" class="section">
      <div class="row" style="height: 50px; vertical-align: middle; display: table-cell;">
        <div class="span12" style="color: white; font-size: 16px;">
          <div style="display: inline-block; vertical-align: middle; margin: 3px 0px 0px 0px;">
            <div id="online" style="display: inline-block; margin-left: 15px;"><span style="display: inline-block;
  width: 20px; color: transparent; background-image: url('./img/login-connected.png'); background-repeat: no-repeat; background-position: 2px 3px;">.</span><span>Online</span></div>
            <div id="terminal" style="display: inline-block; margin-left: 50px;"></div>
            <div class="dropdown" style="display: inline-block; margin-left: 50px;" >
              <a id="yourcompany" class="btn-dropdown" href="#" class="dropdown-toggle" data-toggle="dropdown"></a>
              <div class="dropdown-menu" style="color: black; width: 350px;">
                <div style="height: 60px; background-repeat: no-repeat; background-position: center center; background-image: url('../../utility/ShowImageLogo?logo=yourcompanymenu');"></div>
                <div id="yourcompanyproperties" style="display: block; padding: 10px; float: left; background-color: #FFF899; line-height: 23px;"></div>
                <div style="clear: both;"></div>
              </div>
            </div>
            <div class="dropdown" style="display: inline-block; margin-left: 50px;" >
              <a id="loggeduser" class="btn-dropdown" href="#" class="dropdown-toggle" data-toggle="dropdown"></a>
              <div id="loggeduserproperties" class="dropdown-menu" style="color: black; padding: 0px; width: 350px;">
              </div>
            </div>
          </div>
          <div style="display: inline-block; float: right;">
            <div style="display: inline-block; float: left; margin: 4px 10px 0px 0px;">Openbravo Web POS</div>
            <div style="width: 30px; height: 30px; float: right; margin: 0px 12px 0px 0px;">
              <div class="top-right-logo">
            </div>
          </div>
          <div id="dialogsContainer"></div>
        </div>

      </div>
    </div>
    <div>
      <div id="containerLoading">
        <div class="POSLoadingCenteredBox">
          <div class="POSLoadingPromptLabel" id="">Loading...</div>
          <div class="POSLoadingProgressBar">
            <div class="POSLoadingProgressBarImg"></div>
          </div>
        </div>
      </div>
      <div id="containerWindow" style="display: none;">
        <!-- Here it goes the POS window... -->
      </div>
    </div>
  </div>

  <script src="../org.openbravo.client.kernel/js/BigDecimal-all-1.0.1.min.js"></script>
  <script src="../../org.openbravo.client.kernel/OBCLKER_Kernel/Application"></script>
  <script src="../../org.openbravo.client.kernel/OBCLKER_Kernel/Labels"></script>

  <script src="js/libs/jquery-1.7.2.js"></script>
  <script src="js/libs/underscore-1.3.3.js"></script>
  <script src="js/libs/backbone-0.9.2.js"></script>
  <script src="js/libs/bootstrap/js/bootstrap-tab.js"></script>
  <script src="js/libs/bootstrap/js/bootstrap-dropdown.js"></script>
  <script src="js/libs/bootstrap/js/bootstrap-modal.js"></script>
  <script src="js/libs/bootstrap/js/bootstrap-alert.js"></script>
  <script src="js/libs/bootstrap/js/bootstrap-button.js"></script>

  <!-- Models -->
  <script src="../../org.openbravo.client.kernel/OBPOS_Main/ClientModel?entity=FinancialMgmtTaxRate&modelName=TaxRate&source=org.openbravo.retail.posterminal.master.TaxRate"></script>
  <script src="../../org.openbravo.client.kernel/OBPOS_Main/ClientModel?entity=PricingProductPrice&modelName=ProductPrice&source=org.openbravo.retail.posterminal.master.ProductPrice"></script>
  <script src="js/model/product-category.js"></script>
  <script src="js/model/product.js"></script>
  <script src="js/model/businesspartner.js"></script>
  <!-- Web POS Application -->
  <script src="../../org.openbravo.client.kernel/OBCLKER_Kernel/StaticResources?_appName=WebPOS"></script>

  <script>
    // Hack focus captured by location bar in android browser.
    (function () {
      var locationwarning = true;
      var focuskeeper = $('<input id="focuskeeper" style="position:fixed; top:-1000px; left:-1000px;" type="text"/>');
      $("body").append(focuskeeper);
      $("body").focusin(function() {
        locationwarning = false;
      });
      $("body").focusout(function() {
        locationwarning = true;
      });

      window.fixFocus = function () {
        if (locationwarning) {
          focuskeeper.focus();
        }
        var t = document.activeElement.tagName;
        var id = document.activeElement.id;
        return (id === 'focuskeeper' || (t !=='INPUT' && t !=='SELECT' && t !=='TEXTAREA')); // process key
      }
    }());
  </script>
</body>
</html>
