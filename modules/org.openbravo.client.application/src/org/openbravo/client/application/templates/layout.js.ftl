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
 * All portions are Copyright (C) 2010-2017 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
*/

// should be moved to client.kernel component
// placed here to prevent dependencies of client.kernel on Preferences
OB.Application.startPage = '${data.startPage}';

OB.Application.imageWidth = '${data.companyImageLogoWidth}';
OB.Application.imageHeight = '${data.companyImageLogoHeight}';

    

OB.Application.navigationBarComponents = [<#list data.navigationBarComponents as nbc>
                                          ${nbc.jscode}<#if nbc_has_next>,</#if>
                                          </#list>];
OB.Application.professionalLink = '${data.addProfessionalLink?string}';

${data.notesDataSource}


