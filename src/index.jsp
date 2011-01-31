<%@ page import="java.util.Properties" %>
<%@ page import="org.openbravo.base.HttpBaseServlet" %>
<%@ page import="org.openbravo.base.util.OBClassLoader" %>
<%@ page import="org.openbravo.base.session.OBPropertiesProvider" %>
<%@ page import="org.openbravo.authentication.AuthenticationManager" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%
/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html 
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License. 
 * The Original Code is Openbravo ERP. 
 * The Initial Developer of the Original Code is Openbravo SLU 
 * All portions are Copyright (C) 2011 Openbravo SLU
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
 
Properties obProperties = OBPropertiesProvider.getInstance().getOpenbravoProperties();
String authClass = obProperties.getProperty("authentication.class");

if(authClass == null || authClass.equals("")) {
  authClass = "org.openbravo.authentication.basic.DefaultAuthenticationManager";
}

AuthenticationManager authManager = (AuthenticationManager) OBClassLoader.getInstance().loadClass(authClass).newInstance();

HttpBaseServlet s = new HttpBaseServlet(); // required for ConnectionProvider
s.init(getServletConfig());
s.initialize(request, response);

authManager.init(s);

String userId = authManager.authenticate(request, response);
if(userId == null){
  return;
}
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
        "http://www.w3.org/TR/html4/loose.dtd">

<html>
<head>  
<meta http-equiv="Expires" content="Tue, 24 Apr 1979 00:00:01 GMT"/>
<meta http-equiv="Content-type" content="text/html;charset=utf-8"/>
<meta http-equiv="Pragma" content="no-cache" >
<meta name="author" content="Openbravo S.L.U.">
<meta name="keywords" content="openbravo">
<meta name="description" content="Openbravo S.L.U.">
<link rel="shortcut icon" href="./web/images/favicon.ico" />
<link rel="stylesheet" type="text/css" href="./org.openbravo.client.kernel/OBCLKER_Kernel/StyleSheetResources?_mode=3.00&&_skinVersion=3.00"/>
<title>Openbravo</title>
<script type="text/javascript" src="./web/org.openbravo.client.kernel/js/LAB.min.js"></script>
<!-- styles used during loading -->
<style type="text/css">
  html, body {
      height: 100%;
      width: 100%;
  }

  .OBCenteredBox {
      position: fixed;
      z-index: 1000000;
      top: 50%;
      left: 50%;
      margin: -25px 0 0 -150px;
      width: 300px;
      height: 50px;
  }

  .OBLoadingPromptLabel {
      font-family: 'Arial';
      font-size: 12px;
      color: #ccd0d4;
  }

  .OBLoadingPromptModalMask {
      left: 0;
      top: 0;
      width: 100%;
      height: 100%;
      background-color: #7f7f7f;
  }
</style>

</head>
<body>

<!-- shows the loading div -->
<div class="OBLoadingPromptModalMask" id="OBLoadingDiv">
    <div class="OBCenteredBox">
        <table>
            <tr>
                <td>
                    <span class="OBLoadingPromptLabel">LOADING...</span>
                </td>
                <td>
                    <img width="220" height="16" src="./web/org.openbravo.userinterface.smartclient/openbravo/skins/3.00/org.openbravo.client.application/images/system/windowLoading.gif"/>
                </td>
            </tr>
        </table>
    </div>
</div>
<!-- load the rest -->
<script type="text/javascript">
$LAB.setGlobalDefaults({AppendTo: 'body'})

var isomorphicDir='./web/org.openbravo.userinterface.smartclient/isomorphic/';

// starts the application is called as the last statement in the StaticResources part
function OBStartApplication() {
  OB.Layout.draw();
  $LAB.script(document.location.protocol + OB.Application.butlerUtilsUrl).wait();
  OB.Layout.ViewManager.createAddStartTab();
  // get rid of the loading stuff
  document.body.removeChild(document.getElementById('OBLoadingDiv'));
  OB.GlobalHiddenForm = document.forms.OBGlobalHiddenForm;
}
</script>
<script type="text/javascript" src="./web/org.openbravo.userinterface.smartclient/isomorphic/ISC_Combined.js"></script>
<script type="text/javascript" src="./web/org.openbravo.userinterface.smartclient/isomorphic/ISC_History.js"></script>
<script type="text/javascript" src="./org.openbravo.client.kernel/OBCLKER_Kernel/StaticResources?_mode=3.00&_skinVersion=3.00"></script>
<form name="OBGlobalHiddenForm" method="post" action="blank.html"></form>
</body>
</html>