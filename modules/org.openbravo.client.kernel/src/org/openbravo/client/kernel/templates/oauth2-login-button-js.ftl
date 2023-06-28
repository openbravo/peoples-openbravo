<#--
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
 * All portions are Copyright (C) 2023 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
*/
-->

<#list data.buttons as button>
<div id="${button.id}" class="oAuth2SignInButton oAuth2SignInButton-${button.id}" onclick="doExternalAuthentication({ authorizationServerURL: '${button.authorizationServerURL}', clientID: '${button.clientID}', state: '${button.state}', redirectURL: '${button.redirectURL}', scope: '${button.scope}' })">
  <span title="${button.name}"></span>
</div>
<#if button_index != data.buttons?size - 1>&nbsp;</#if>
</#list>

<script>
  function doExternalAuthentication({ authorizationServerURL, clientID, state, redirectURL, scope }) {
    window.location.href = authorizationServerURL + "?" + "response_type=code&client_id=" + clientID + "&state=" + state + "&redirect_uri=" + redirectURL + "&scope=" + scope; 
  }
</script>

<style type="text/css">
  .oAuth2SignInButton {
    display: inline-block;
    color: white;
    width: 24px;
    border-radius: 2px;
    white-space: nowrap;
    border: 1px solid #d9d9d9;
    transition: opacity 0.3s ease;
  }
  .oAuth2SignInButton:hover,
  .oAuth2SignInButton:active {
    border-color: #c0c0c0;
    box-shadow: 0 1px 0 rgba(0, 0, 0, 0.10);
    cursor: hand;
  }
  .oAuth2SignInButton:hover {
    opacity: 0.7;
  }
  .oAuth2SignInButton > span {
    display: inline-block;	
    height: 24px;
    width: 24px;
    margin-top: -1px;
    vertical-align: middle;
  }
  <#list data.buttons as button>
    <#if button.icon??>
  .oAuth2SignInButton-${button.id} > span {
    background: url(${button.icon});
    background-size: cover;
  }
    </#if>
  </#list>
</style>
