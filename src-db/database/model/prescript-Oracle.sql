 -- create temporary tables
 CREATE GLOBAL TEMPORARY TABLE C_TEMP_SELECTION
 (
   C_TEMP_SELECTION_ID  VARCHAR2(32 BYTE)             NOT NULL,
   CONSTRAINT C_TEMP_SELECTION_key PRIMARY KEY (C_TEMP_SELECTION_ID)
  )
  ON COMMIT DELETE ROWS
/-- END 

 CREATE GLOBAL TEMPORARY TABLE C_TEMP_SELECTION2
  (
  QUERY_ID        VARCHAR2(32 BYTE)             NOT NULL,
  C_TEMP_SELECTION_ID  VARCHAR2(32 BYTE)             NOT NULL,
   CONSTRAINT C_TEMP_SELECTION2_key PRIMARY KEY (QUERY_ID, C_TEMP_SELECTION_ID)	
   )
   ON COMMIT PRESERVE ROWS
/-- END 

CREATE OR REPLACE FUNCTION C_CREATE_TEMPORARY_TABLES RETURN VARCHAR2
AS
/*************************************************************************
* The contents of this file are subject to the Openbravo  Public  License
* Version  1.0  (the  "License"),  being   the  Mozilla   Public  License
* Version 1.1  with a permitted attribution clause; you may not  use this
* file except in compliance with the License. You  may  obtain  a copy of
* the License at http://www.openbravo.com/legal/license.html
* Software distributed under the License  is  distributed  on  an "AS IS"
* basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
* License for the specific  language  governing  rights  and  limitations
* under the License.
* The Original Code is Openbravo ERP.
* The Initial Developer of the Original Code is Openbravo SL
* All portions are Copyright (C) 2001-2006 Openbravo SL
* All Rights Reserved.
* Contributor(s):  ______________________________________.
************************************************************************/
BEGIN
RETURN null;
END C_CREATE_TEMPORARY_TABLES;
/-- END 

create or replace FUNCTION GET_UUID RETURN VARCHAR2
AS
/*************************************************************************
* The contents of this file are subject to the Openbravo  Public  License
* Version  1.0  (the  "License"),  being   the  Mozilla   Public  License
* Version 1.1  with a permitted attribution clause; you may not  use this
* file except in compliance with the License. You  may  obtain  a copy of
* the License at http://www.openbravo.com/legal/license.html
* Software distributed under the License  is  distributed  on  an "AS IS"
* basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
* License for the specific  language  governing  rights  and  limitations
* under the License.
* The Original Code is Openbravo ERP.
* The Initial Developer of the Original Code is Openbravo SL
* All portions are Copyright (C) 2008 Openbravo SL
* All Rights Reserved.
* Contributor(s):  ______________________________________.
************************************************************************/
BEGIN
 return rawtohex(sys_guid());
END GET_UUID;
/-- END
