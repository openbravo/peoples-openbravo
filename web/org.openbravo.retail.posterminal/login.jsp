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

<body style="padding-top: 20px;background-color: darkgray; background: url(img/BACKGROUND-PNG24.png) top left">
<div id="container" class="container">
  <div id="topsection" class="section">
    <div class="row">
      <div class="span12" style="color: white;">
        <div id="online" style="display: inline-block; margin: 5px;"><span class="badge badge-success">Online</span></div>
        <div id="terminal" style="display: inline-block; margin: 5px;"></div>
        <div class="dropdown" style="display: inline-block; margin-left:30px;" >
          <a id="yourcompany" class="btn-dropdown" href="#" class="dropdown-toggle" data-toggle="dropdown"></a>
          <div class="dropdown-menu" style="color: black; padding:5px; width: 400px;">
            <img src="../../utility/ShowImageLogo?logo=yourcompanymenu" alt="Your company" style="display:block; float:left;width: 20%;"/>
            <div id="yourcompanyproperties" style="display:block; margin-left:10px; float:left;width: 70%"></div>
            <div style="clear:both;"></div>
          </div>
        </div>
        <div class="dropdown" style="display: inline-block; margin-left:30px;" >
          <a id="loggeduser" class="btn-dropdown" href="#" class="dropdown-toggle" data-toggle="dropdown"></a>
          <div id="loggeduserproperties" class="dropdown-menu" style="color: black; padding:5px; width: 400px;">
          </div>
        </div>
        <div style="display: inline-block; font-weight: bold; float:right">
          <span>Openbravo Point of Sale </span>
          <a id="logoutaction" href="#" class="" style="visibility:hidden;"><i class="icon-off icon-white"></i></a>
        </div>
      </div>
    </div>
  </div>
  <div>
    <div id="containerLoading">
      <div class="POSLoadingCenteredBox">
        <div class="POSLoadingPromptLabel" id="">LOADING...</div>
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

<script src="js/libs/jquery-1.7.2.js"></script>
<script src="js/libs/underscore-1.3.3.js"></script>
<script src="js/libs/backbone-0.9.2.js"></script>
<script src="js/libs/bootstrap/js/bootstrap-tab.js"></script>
<script src="js/libs/bootstrap/js/bootstrap-dropdown.js"></script>
<script src="js/libs/bootstrap/js/bootstrap-modal.js"></script>
<script src="js/libs/bootstrap/js/bootstrap-alert.js"></script>

<!-- Login application -->
<script src="js/builder.js"></script>
<script src="js/datasource.js"></script>
<script src="js/utilities.js"></script>
<script src="js/utilitiesui.js"></script>
<script src="js/i18n.js"></script>
<script src="js/components/clock.js"></script>
<script src="js/components/commonbuttons.js"></script>
<script src="js/model/terminal.js"></script>
<script src="js/windows/login.js"></script>
<script src="js/mainlogin.js"></script>

<script>
OB = window.OB || {};
OB.I18N = window.OB.I18N || {};
OB.I18N.getLabel = window.OB.I18N.getLabel || function(param) { return param; };

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
