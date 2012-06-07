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

request.setAttribute("loginURL", "/web/org.openbravo.retail.posterminal/login.jsp");

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
  <link rel="shortcut icon" type="image/x-icon" href="favicon.ico"/>

  <link rel="stylesheet/less" href="js/libs/bootstrap/less/bootstrap.less">
  <link rel="stylesheet/less" href="js/libs/bootstrap/less/responsive.less">
  <script src="js/libs/less/less-1.3.0.min.js"></script>

  <link rel="shortcut icon" href="../../web/images/favicon.ico" />

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
            <span>Openbravo Point of Sale</span>&nbsp;
            <a data-toggle="modal" id="logoutlink" href="#logoutModal" class=""><i class="icon-off icon-white"></i></a>
            <div class="modal hide" id="logoutModal">
              <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal">×</button>
                <h3 style="color: black;">You may still have pending unsaved changes.<br />What do you want to do?</h3>
              </div>
              <div class="modal-body" style="text-align: center;">
                <br />
                <a href="#" class="btn btn-danger" id="logoutaction" style="height: 30px; width: 500px; font-size: 150%; padding-top: 10px;" data-dismiss="modal">Logout and lose unsaved changes</a>
                <br /><br /><br /><br />
                <a href="#" class="btn btn-primary" style="height: 30px; width: 500px; font-size: 150%; padding-top: 10px;" data-dismiss="modal">Lock terminal</a>
                <br /><br /><br /><br />
                <a href="#" class="btn" style="height: 30px; width: 500px; font-size: 150%; padding-top: 10px;" data-dismiss="modal">Cancel</a>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div>
      <div id="containerwindow">
        <!-- Here it goes the POS window... -->
      </div>
    </div>
  </div>

  <script src="../org.openbravo.client.kernel/js/BigDecimal-all-1.0.1.min.js"></script>
  <script src="../../org.openbravo.client.kernel/OBCLKER_Kernel/Application"></script>

  <script src="js/libs/jquery-1.7.2.js"></script>
  <script src="js/libs/underscore-1.3.3.js"></script>
  <script src="js/libs/backbone-0.9.2.js"></script>
  <script src="js/libs/bootstrap/js/bootstrap-tab.js"></script>
  <script src="js/libs/bootstrap/js/bootstrap-dropdown.js"></script>
  <script src="js/libs/bootstrap/js/bootstrap-modal.js"></script>
  <script src="js/libs/bootstrap/js/bootstrap-alert.js"></script>

  <script data-main="js/main" src="js/libs/require-1.0.7.min.js"></script>

  <script>
    $('#logoutaction').click(function (e) {
      e.preventDefault();
      OB.POS.logout();
    });

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
