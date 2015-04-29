-- Function: uuid_generate_v1()

-- DROP FUNCTION uuid_generate_v1();

CREATE OR REPLACE FUNCTION uuid_generate_v1()
  RETURNS uuid AS
'$libdir/uuid-ossp', 'uuid_generate_v1'
  LANGUAGE c VOLATILE STRICT
  COST 1;
ALTER FUNCTION uuid_generate_v1()
  OWNER TO postgres;

-- Function: get_uuid()

-- DROP FUNCTION get_uuid();

CREATE OR REPLACE FUNCTION get_uuid()
  RETURNS character varying AS
$BODY$ DECLARE
/*************************************************************************
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
* All portions are Copyright (C) 2008-2009 Openbravo SLU
* All Rights Reserved.
* Contributor(s):  ______________________________________.
************************************************************************/
var VARCHAR:=substr(uuid_generate_v1()::character varying, 10);
prefix VARCHAR;
BEGIN
 WHILE var=substr(uuid_generate_v1()::character varying, 10) LOOP
END LOOP; 

  var = uuid_generate_v1()::character varying;
  prefix = substr(var, 0, 9);
  return replace(upper(substr(var, 10) || prefix),'-','');
END;   $BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION get_uuid()
  OWNER TO tad;



