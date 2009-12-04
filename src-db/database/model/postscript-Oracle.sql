CREATE OR REPLACE function NOW
RETURN DATE
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
* All portions are Copyright (C) 2001-2009 Openbravo SL
* All Rights Reserved.
* Contributor(s):  ______________________________________.
************************************************************************/
BEGIN
  RETURN SYSDATE;
END NOW;
/-- END NOW

CREATE OR REPLACE FUNCTION hex_to_int (hexn VARCHAR)
  RETURN number
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
* All portions are Copyright (C) 2001-2008 Openbravo SL
* All Rights Reserved.
* Contributor(s):  ______________________________________.
************************************************************************/
BEGIN
    return to_number(hexn,'xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx');
END hex_to_int;
/-- END hex_to_int

CREATE OR REPLACE FUNCTION ad_script_disable_triggers (p_seqNoStart NUMBER)
  RETURN NUMBER
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
* All portions are Copyright (C) 2001-2008 Openbravo SL
* All Rights Reserved.
* Contributor(s):  ______________________________________.
************************************************************************/
 v_seqNo      NUMBER := p_seqNoStart;
 TYPE RECORD IS REF CURSOR;
 Cur_Triggers RECORD;
BEGIN
     FOR Cur_Triggers IN 
      (SELECT OBJECT_NAME AS NAME 
      FROM USER_OBJECTS 
      WHERE OBJECT_TYPE='TRIGGER' 
      ORDER BY OBJECT_NAME 
      ) 
    LOOP 
      v_seqNo:=v_seqNo + 1; 
      INSERT 
      INTO AD_SCRIPT_SQL VALUES 
        (v_seqNo, 'ALTER TRIGGER '||Cur_Triggers.NAME||' DISABLE') ; 
    END LOOP; 

 RETURN v_seqNo;
END ad_script_disable_triggers;
/-- END

CREATE OR REPLACE FUNCTION ad_script_disable_constraints(p_seqNoStart NUMBER)
  RETURN NUMBER
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
* All portions are Copyright (C) 2001-2008 Openbravo SL
* All Rights Reserved.
* Contributor(s):  ______________________________________.
************************************************************************/
 v_seqNo      NUMBER := p_seqNoStart;
 TYPE RECORD IS REF CURSOR;
 Cur_Constraints RECORD;
BEGIN
    FOR Cur_Constraints IN 
      (SELECT TABLE_NAME, CONSTRAINT_NAME 
    FROM USER_CONSTRAINTS C1 
    WHERE CONSTRAINT_TYPE IN('P', 'U', 'R') AND COALESCE(DELETE_RULE,'.') NOT LIKE 'CASCADE' 
    ORDER BY(CASE CONSTRAINT_TYPE WHEN 'R' THEN 1 WHEN 'U' THEN 2 WHEN 'P' THEN 3 
      END 
      ), TABLE_NAME, CONSTRAINT_NAME) 
    LOOP 
      v_seqNo:=v_seqNo + 1; 
      INSERT 
      INTO AD_SCRIPT_SQL VALUES 
        (v_seqNo, 'ALTER TABLE '||Cur_Constraints.TABLE_NAME||' DISABLE CONSTRAINT '||Cur_Constraints.CONSTRAINT_NAME) ; 
    END LOOP; 

 RETURN v_seqNo;
END ad_script_disable_constraints;
/-- END


CREATE OR REPLACE FUNCTION ad_script_enable_triggers(p_seqNoStart NUMBER)
  RETURN NUMBER 
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
* All portions are Copyright (C) 2001-2008 Openbravo SL
* All Rights Reserved.
* Contributor(s):  ______________________________________.
************************************************************************/
 v_seqNo      NUMBER := p_seqNoStart;
 TYPE RECORD IS REF CURSOR;
 Cur_Triggers RECORD;
BEGIN
    FOR Cur_Triggers IN 
      (SELECT OBJECT_NAME AS NAME 
    FROM USER_OBJECTS 
    WHERE OBJECT_TYPE='TRIGGER' 
    ORDER BY OBJECT_NAME) 
    LOOP 
      v_seqNo:=v_seqNo + 1; 
      INSERT 
      INTO AD_SCRIPT_SQL VALUES 
        (v_seqNo, 'ALTER TRIGGER '||Cur_Triggers.NAME||' ENABLE') ; 
    END LOOP; 

 RETURN v_seqNo;
END ad_script_enable_triggers;
/-- END

CREATE OR REPLACE FUNCTION ad_script_enable_constraints(p_seqNoStart NUMBER)
  RETURN NUMBER
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
* All portions are Copyright (C) 2001-2008 Openbravo SL
* All Rights Reserved.
* Contributor(s):  ______________________________________.
************************************************************************/
 v_seqNo      NUMBER := p_seqNoStart;
 TYPE RECORD IS REF CURSOR;
 Cur_Constraints RECORD;
BEGIN
    FOR Cur_ConstraintsEnable IN 
      (SELECT TABLE_NAME, CONSTRAINT_NAME 
    FROM USER_CONSTRAINTS C1 
    WHERE CONSTRAINT_TYPE IN('P', 'U', 'R') AND COALESCE(DELETE_RULE,'.') NOT LIKE 'CASCADE' 
    ORDER BY(CASE CONSTRAINT_TYPE WHEN 'R' THEN 3 WHEN 'U' THEN 2 WHEN 'P' THEN 1 
      END 
      ), TABLE_NAME, CONSTRAINT_NAME) 
    LOOP 
      v_seqNo:=v_seqNo + 1; 
      INSERT 
      INTO AD_SCRIPT_SQL VALUES 
        (v_seqNo, 'ALTER TABLE '||Cur_ConstraintsEnable.TABLE_NAME||' ENABLE CONSTRAINT '||Cur_ConstraintsEnable.CONSTRAINT_NAME) ; 
    END LOOP; 

  RETURN v_seqNo;
END ad_script_enable_constraints;
/-- END


CREATE OR REPLACE FUNCTION ad_script_execute (param_Message VARCHAR2)
  RETURN VARCHAR2
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
* All portions are Copyright (C) 2001-2008 Openbravo SL
* All Rights Reserved.
* Contributor(s):  ______________________________________.
************************************************************************/
 v_Message       VARCHAR2(4000) := '';
 v_ResultStr     VARCHAR2(4000) := '';
 TYPE RECORD IS REF CURSOR;
 Cur_Script RECORD;
BEGIN
    v_Message := param_Message;
    FOR Cur_Script IN 
      (SELECT STRSQL, SEQNO FROM AD_SCRIPT_SQL ORDER BY SEQNO)
    LOOP 
    BEGIN 
      EXECUTE IMMEDIATE(Cur_Script.STRSQL) ; 
    EXCEPTION 
    WHEN OTHERS THEN 
      
      if (coalesce(length(v_Message),0)!=0) then
        v_Message:=substr(v_Message||'<br><br>',1,2000);
      end if;
      v_Message := substr(v_Message||'@SQLScriptError@ '||Cur_Script.SeqNo||'. @Executing@'||Cur_Script.strSQL||'<br>'||SQLERRM,1,2000);
    END;
  END LOOP;

 IF( LENGTH(v_Message) > 0 ) THEN
    DBMS_OUTPUT.PUT_LINE('Script errors: ' || v_Message);
 END IF;
 return substr(coalesce(v_ResultStr,'') || coalesce(v_Message,''), 1, 2000);
END ad_script_execute;
/-- END

CREATE OR REPLACE FUNCTION ad_script_drop_recreate_index (p_seqNoStart NUMBER)
  RETURN NUMBER
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
* All portions are Copyright (C) 2001-2008 Openbravo SL
* All Rights Reserved.
* Contributor(s):  ______________________________________.
************************************************************************/
 v_seqNo         NUMBER; 
 v_strSql        VARCHAR2(4000) := '';
 v_strTemp       VARCHAR2(4000) := '';
 v_Message       VARCHAR2(4000) := '';
 v_ResultStr     VARCHAR2(4000) := '';
 TYPE RECORD IS REF CURSOR;
 Cur_UniqueIndex  RECORD;
 Cur_IndexColumns RECORD;
BEGIN
    v_seqNo := p_seqNoStart;
    FOR Cur_UniqueIndex IN (SELECT i.INDEX_NAME, i.TABLE_NAME, i.TABLESPACE_NAME, CONSTRAINT_TYPE
                 FROM USER_INDEXES I left join USER_CONSTRAINTS C1 on c1.INDEX_NAME=I.INDEX_NAME
                 WHERE UNIQUENESS='UNIQUE' AND INDEX_TYPE='NORMAL' AND TABLE_TYPE='TABLE'
               --AND CONSTRAINT_TYPE != 'U'
               ORDER BY INDEX_NAME)

    LOOP
      v_seqNo:=v_seqNo + 1;
      INSERT
      INTO AD_SCRIPT_SQL VALUES (v_seqNo, 'DROP INDEX '||Cur_UniqueIndex.INDEX_NAME) ;

   IF Cur_UniqueIndex.CONSTRAINT_TYPE != 'P' THEN
    v_strSql:='CREATE INDEX '||Cur_UniqueIndex.INDEX_NAME||' ON '||Cur_UniqueIndex.TABLE_NAME||'(';
       v_strTemp:='';
       FOR Cur_IndexColumns IN
         (SELECT COLUMN_NAME
       FROM USER_IND_COLUMNS
       WHERE INDEX_NAME=Cur_UniqueIndex.INDEX_NAME
       ORDER BY COLUMN_POSITION)
       LOOP
         v_strTemp:=v_strTemp ||','|| Cur_IndexColumns.COLUMN_NAME;
       END LOOP;
       v_strSql:=v_strSql || SUBSTR(v_strTemp, 2, 2000) || ') TABLESPACE '||Cur_UniqueIndex.TABLESPACE_NAME;
       INSERT INTO AD_SCRIPT_SQL VALUES(v_seqNo+100000, v_strSql) ;
   END IF;
 END LOOP;
 return v_seqNo;
END ad_script_drop_recreate_index;
/-- END

CREATE OR REPLACE PROCEDURE DBA_RECOMPILE(p_PInstance_ID IN VARCHAR2) 
AS
/*************************************************************************
  * The contents of this file are subject to the Compiere Public
  * License 1.1 ("License"); You may not use this file except in
  * compliance with the License. You may obtain a copy of the License in
  * the legal folder of your Openbravo installation.

  * Software distributed under the License is distributed on an
  * "AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
  * implied. See the License for the specific language governing rights

  * and limitations under the License.
  * The Original Code is  Compiere  ERP &  Business Solution
  * The Initial Developer of the Original Code is Jorg Janke and ComPiere, Inc.

  * Portions created by Jorg Janke are Copyright (C) 1999-2001 Jorg Janke,
  * parts created by ComPiere are Copyright (C) ComPiere, Inc.;
  * All Rights Reserved.
  * Contributor(s): Openbravo SL

  * Contributions are Copyright (C) 1999-2005 Openbravo, S.L
  *
  * Specifically, this derivative work is based upon the following Compiere
  * file and version.
  *************************************************************************
  * $Id: DBA_Recompile.sql,v 1.7 2003/03/14 06:11:21 jjanke Exp $
  ***
  * Title:  Recompile all User_Objects
  * Description:
  ************************************************************************/
  -- Logistice
  v_Message VARCHAR2(2000):=' ';
  v_Result NUMBER:=1; --  0=failure
  --
  v_Buffer VARCHAR2(2000):='';
  v_Line VARCHAR(100) ;
  v_PrintInfo CHAR(1):='N'; -- Diagnostic
  --
  CURSOR Cur_Invalids IS
    SELECT object_id,
      object_name,
      object_type
    FROM user_objects
    WHERE status<>'VALID'
      AND object_type IN('VIEW', 'PACKAGE', 'PACKAGE BODY', 'FUNCTION', 'PROCEDURE', 'TRIGGER', 'JAVA CLASS')
    ORDER BY object_type,
      object_name;
    CURSOR Cur_Valids (p_id NUMBER) IS
      SELECT 'FOUND'  FROM user_objects  WHERE status='VALID'  AND object_id=p_id;
      --  failed compile
    TYPE invalid_tab IS TABLE OF Cur_Invalids%ROWTYPE INDEX BY BINARY_INTEGER;
    invalid_tab_rec invalid_tab;
    count_compiled PLS_INTEGER;
    valid_text VARCHAR2(5) ;
    exec_cursor PLS_INTEGER:=DBMS_SQL.OPEN_CURSOR;
    sql_statement VARCHAR2(200) ;
    count_object PLS_INTEGER:=0;
  BEGIN
    LOOP
      count_compiled:=0;
      FOR ci IN Cur_Invalids
      LOOP
        --  not unsuccessfuly compiled yet
        IF NOT invalid_tab_rec.EXISTS(ci.object_id) THEN
          IF(ci.object_type='JAVA CLASS') THEN
            sql_statement:='ALTER JAVA CLASS "' || ci.object_name || '" RESOLVE';
          ELSIF(ci.object_type='PACKAGE BODY') THEN
            sql_statement:='ALTER PACKAGE ' || ci.object_name || ' COMPILE BODY';
          ELSE
            sql_statement:='ALTER ' || ci.object_type || ' ' || ci.object_name || ' COMPILE';
          END IF;
          --  compile
        BEGIN
          count_object:=count_object + 1;
          DBMS_SQL.PARSE(exec_cursor, sql_statement, DBMS_SQL.NATIVE) ;
        EXCEPTION
        WHEN OTHERS THEN
          NULL;
        END;
        --
        OPEN Cur_Valids(ci.object_ID) ;
        FETCH Cur_Valids INTO valid_text;
        IF Cur_Valids%ROWCOUNT>0 THEN
          IF(v_PrintInfo='Y') THEN
            DBMS_OUTPUT.PUT_LINE('OK: ' || ci.object_type || ' ' || ci.object_name) ;
          END IF;
          count_compiled:=count_compiled + 1;
          CLOSE Cur_Valids;
          EXIT;
        ELSE
          IF(LENGTH(v_Message)<1950) THEN
            v_Message:=v_Message || ci.object_name || ' ';
          END IF;
          IF(v_PrintInfo='Y') THEN
            DBMS_OUTPUT.PUT_LINE('Error: ' || ci.object_type || ' ' || ci.object_name) ;
          END IF;
          --
          invalid_tab_rec(ci.object_id) .object_name:=ci.object_name;
          invalid_tab_rec(ci.object_id) .object_type:=ci.object_type;
          CLOSE Cur_Valids;
        END IF;
      END IF; -- not unsuccessfuly compiled yet
    END LOOP; -- Cur_Invalids
    --  any other to be compiled
    IF count_compiled=0 THEN
      EXIT;
    END IF;
  END LOOP; -- outer loop
  DBMS_SQL.CLOSE_CURSOR(exec_cursor) ;
  --
  -- Print Message
  IF(LENGTH(v_Message)=1) THEN
    v_Message:='All valid';
    DBMS_OUTPUT.PUT_LINE(v_Message) ;
  ELSIF(LENGTH(v_Message)>80) THEN
    v_Buffer:=v_Message;
    DBMS_OUTPUT.PUT_LINE('>') ;
    WHILE(LENGTH(v_Buffer)>0)
    LOOP
      v_Line:=SUBSTR(v_Buffer, 1, 80) ;
      DBMS_OUTPUT.PUT_LINE(v_Line) ;
      v_Buffer:=SUBSTR(v_Buffer, 81) ;
    END LOOP;
    DBMS_OUTPUT.PUT_LINE('<') ;
    v_Result:=0;
    DBMS_OUTPUT.PUT_LINE('ERROR') ;
  ELSE
    DBMS_OUTPUT.PUT_LINE('>' || v_Message || '<') ;
    v_Result:=0;
    DBMS_OUTPUT.PUT_LINE('ERROR') ;
  END IF;
  --<<FINISH_PROCESS>>
  IF(p_PInstance_ID IS NOT NULL) THEN
    --  Update AD_PInstance
    AD_UPDATE_PINSTANCE(p_PInstance_ID, NULL, 'N', v_Result, v_Message) ;
  END IF;
  RETURN;
EXCEPTION
WHEN OTHERS THEN
  DBMS_OUTPUT.PUT_LINE(SQLERRM) ;
  IF DBMS_SQL.IS_OPEN(exec_cursor) THEN
    DBMS_SQL.CLOSE_CURSOR(exec_cursor) ;
  END IF;
  IF Cur_Valids%ISOPEN THEN
    CLOSE Cur_Valids;
  END IF;
END DBA_Recompile;
/-- END

CREATE OR REPLACE PROCEDURE DBA_AFTERIMPORT
AS
/*************************************************************************
  * The contents of this file are subject to the Compiere Public
  * License 1.1 ("License"); You may not use this file except in
  * compliance with the License. You may obtain a copy of the License in
  * the legal folder of your Openbravo installation.
  * Software distributed under the License is distributed on an
  * "AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
  * implied. See the License for the specific language governing rights
  * and limitations under the License.
  * The Original Code is  Compiere  ERP &  Business Solution
  * The Initial Developer of the Original Code is Jorg Janke and ComPiere, Inc.
  * Portions created by Jorg Janke are Copyright (C) 1999-2001 Jorg Janke,
  * parts created by ComPiere are Copyright (C) ComPiere, Inc.;
  * All Rights Reserved.
  * Contributor(s): Openbravo SL
  * Contributions are Copyright (C) 2001-2008 Openbravo, S.L.
  *
  * Specifically, this derivative work is based upon the following Compiere
  * file and version.
  *************************************************************************
  * $Id: DBA_AfterImport.sql,v 1.5 2002/10/21 04:49:46 jjanke Exp $
  * $Source: /cvsroot/compiere/db/database/Procedures/DBA_AfterImport.sql,v $
  ***
  * Title:  Run after Import
  * Description:
  * - Recompile
  * - Compute Statistics
  *****************************************************************************/
  -- Statistics
  TYPE RECORD IS REF CURSOR;
    Cur_Stat RECORD;
    --
    v_Cmd VARCHAR2(256):='';
    v_NoC NUMBER:=0;
    --
  BEGIN
    -- Recompile
    DBA_Recompile(NULL) ;
    -- Statistics
    FOR Cur_Stat IN
      (SELECT Table_Name,
        Blocks
      FROM USER_TABLES
      WHERE DURATION IS NULL -- No temporary tables
        AND(LAST_ANALYZED IS NULL
        OR LAST_ANALYZED<SysDate-7)
      )
    LOOP
      v_Cmd:='ANALYZE TABLE ' || Cur_Stat.Table_Name || ' COMPUTE STATISTICS';
      v_NoC:=v_NoC + 1;
      EXECUTE IMMEDIATE v_Cmd;
    END LOOP;
    DBMS_OUTPUT.PUT_LINE('Statistics computed: ' || v_NoC) ;
    --
END DBA_AfterImport;
/-- END

CREATE OR REPLACE FUNCTION AD_GET_DOC_LE_BU(p_header_table IN VARCHAR2, p_document_id IN VARCHAR2, p_header_column_id IN VARCHAR2, p_type IN VARCHAR2)
 RETURN VARCHAR
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
* All portions are Copyright (C) 2009 Openbravo SL
* All Rights Reserved.
* Contributor(s):  ______________________________________.
************************************************************************/
   v_org_header_id ad_org.ad_org_id%TYPE;
   v_isbusinessunit ad_orgtype.isbusinessunit%TYPE;
   v_islegalentity ad_orgtype.islegalentity%TYPE;
   
 BEGIN
 
   -- Gets the organization and the organization type of the document's header
   EXECUTE IMMEDIATE 
     'SELECT ad_org.ad_org_id, ad_orgtype.isbusinessunit, ad_orgtype.islegalentity 
     FROM '||p_header_table||', ad_org, ad_orgtype
     WHERE '||p_header_table||'.'||p_header_column_id||' = '''||p_document_id||'''
     AND ad_org.ad_orgtype_id = ad_orgtype.ad_orgtype_id
     AND '||p_header_table||'.ad_org_id=ad_org.ad_org_id ' 
     INTO v_org_header_id, v_isbusinessunit, v_islegalentity; 
 
   -- Gets recursively the organization parent until finding a Business Unit or a Legal Entity
   IF (p_type IS NULL) THEN
	   WHILE (v_isbusinessunit='N' AND v_islegalentity='N') LOOP
	     SELECT hh.parent_id, ad_orgtype.isbusinessunit, ad_orgtype.islegalentity
	     INTO v_org_header_id, v_isbusinessunit, v_islegalentity
	     FROM ad_org, ad_orgtype, ad_treenode pp, ad_treenode hh
	     WHERE pp.node_id = hh.parent_id
	     AND hh.ad_tree_id = pp.ad_tree_id
	     AND pp.node_id=ad_org.ad_org_id
	     AND hh.node_id=v_org_header_id
	     AND ad_org.ad_orgtype_id=ad_orgtype.ad_orgtype_id
	     AND ad_org.isready='Y'
	     AND  EXISTS (SELECT 1 FROM ad_tree WHERE ad_tree.treetype='OO' AND hh.ad_tree_id=ad_tree.ad_tree_id AND hh.ad_client_id=ad_tree.ad_client_id);     
	   END LOOP;
   -- Gets recursively the organization parent until finding a Legal Entity
   ELSIF (p_type='LE') THEN
	   WHILE (v_islegalentity='N') LOOP
         SELECT hh.parent_id, ad_orgtype.islegalentity
         INTO v_org_header_id, v_islegalentity
         FROM ad_org, ad_orgtype, ad_treenode pp, ad_treenode hh
         WHERE pp.node_id = hh.parent_id
         AND hh.ad_tree_id = pp.ad_tree_id
         AND pp.node_id=ad_org.ad_org_id
         AND hh.node_id=v_org_header_id
         AND ad_org.ad_orgtype_id=ad_orgtype.ad_orgtype_id
         AND ad_org.isready='Y'
         AND  EXISTS (SELECT 1 FROM ad_tree WHERE ad_tree.treetype='OO' AND hh.ad_tree_id=ad_tree.ad_tree_id AND hh.ad_client_id=ad_tree.ad_client_id);     
       END LOOP;
   -- Gets recursively the organization parent until finding a Business Unit
   ELSIF (p_type='BU') THEN
       WHILE (v_isbusinessunit='N' AND v_org_header_id<>'0') LOOP
         SELECT hh.parent_id, ad_orgtype.isbusinessunit
         INTO v_org_header_id, v_isbusinessunit
         FROM ad_org, ad_orgtype, ad_treenode pp, ad_treenode hh
         WHERE pp.node_id = hh.parent_id
         AND hh.ad_tree_id = pp.ad_tree_id
         AND pp.node_id=ad_org.ad_org_id
         AND hh.node_id=v_org_header_id
         AND ad_org.ad_orgtype_id=ad_orgtype.ad_orgtype_id
         AND ad_org.isready='Y'
         AND  EXISTS (SELECT 1 FROM ad_tree WHERE ad_tree.treetype='OO' AND hh.ad_tree_id=ad_tree.ad_tree_id AND hh.ad_client_id=ad_tree.ad_client_id);     
       END LOOP;
       RETURN NULL;
   END IF;
   
   RETURN v_org_header_id;
   
END AD_GET_DOC_LE_BU;
/-- END

CREATE OR REPLACE FUNCTION AD_ORG_CHK_DOCUMENTS(p_header_table IN VARCHAR2, p_lines_table IN VARCHAR2, p_document_id IN VARCHAR2, p_header_column_id IN VARCHAR2, p_lines_column_id IN VARCHAR2) 
 RETURN NUMBER
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
* All portions are Copyright (C) 2008-2009 Openbravo SL
* All Rights Reserved.
* Contributor(s):  ______________________________________.
************************************************************************/
   v_org_header_id ad_org.ad_org_id%TYPE;
   v_isbusinessunit ad_orgtype.isbusinessunit%TYPE;
   v_islegalentity ad_orgtype.islegalentity%TYPE;
   v_is_included NUMBER:=0;
  
   TYPE RECORD IS REF CURSOR;
   cur_doc_lines RECORD;
 
   v_line_org VARCHAR2(32);
   v_org_line_id VARCHAR2(32);
 BEGIN  
	 
   -- Gets the Business Unit or Legal Entity of the document
   SELECT AD_GET_DOC_LE_BU(p_header_table, p_document_id, p_header_column_id, NULL)
   INTO v_org_header_id
   FROM DUAL;
 
   -- Check the lines belong to the same BU or LE as the header
   OPEN cur_doc_lines FOR
   'SELECT DISTINCT('||p_lines_table||'.ad_org_id) AS v_line_org
    FROM '||p_header_table||', '||p_lines_table||'
    WHERE '||p_header_table||'.'||p_header_column_id||' = '||p_lines_table||'.'||p_lines_column_id||'
    AND '||p_lines_table||'.ad_org_id<>'||''''||v_org_header_id||'''
    AND '||p_lines_table||'.'||p_lines_column_id||'='''||p_document_id||'''';    
    LOOP
      FETCH cur_doc_lines INTO v_line_org;
      EXIT WHEN cur_doc_lines%NOTFOUND;
 
      SELECT ad_orgtype.isbusinessunit, ad_orgtype.islegalentity
      INTO v_isbusinessunit, v_islegalentity
      FROM AD_Org, AD_OrgType
      WHERE AD_Org.AD_OrgType_ID=AD_OrgType.AD_OrgType_ID
      AND AD_Org.AD_Org_ID=v_line_org;
      
      v_org_line_id:=v_line_org;
      -- Gets recursively the organization parent until finding a Business Unit or a Legal Entity
      WHILE (v_isbusinessunit='N' AND v_islegalentity='N') LOOP
        SELECT hh.parent_id, ad_orgtype.isbusinessunit, ad_orgtype.islegalentity
        INTO v_org_line_id, v_isbusinessunit, v_islegalentity
        FROM ad_org, ad_orgtype, ad_treenode pp, ad_treenode hh
        WHERE pp.node_id = hh.parent_id
        AND hh.ad_tree_id = pp.ad_tree_id
        AND pp.node_id=ad_org.ad_org_id
        AND hh.node_id=v_org_line_id
        AND ad_org.ad_orgtype_id=ad_orgtype.ad_orgtype_id
        AND ad_org.isready='Y'
        AND  EXISTS (SELECT 1 FROM ad_tree WHERE ad_tree.treetype='OO' AND hh.ad_tree_id=ad_tree.ad_tree_id AND hh.ad_client_id=ad_tree.ad_client_id);     
      END LOOP;
      
     IF (v_org_line_id<>v_org_header_id) THEN
       v_is_included:=-1;
     END IF;
     EXIT WHEN v_is_included=-1;
 
    END LOOP; 
   CLOSE cur_doc_lines;
 
  RETURN v_is_included;
 
END AD_ORG_CHK_DOCUMENTS;
/-- END

CREATE OR REPLACE FUNCTION AD_ORG_CHK_DOC_PAYMENTS(p_header_table IN VARCHAR2, p_lines_table IN VARCHAR2, p_document_id IN VARCHAR2, p_header_column_id IN VARCHAR2, p_lines_column_id IN VARCHAR2, p_lines_column_payment_id IN VARCHAR2) 
 RETURN NUMBER
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
* All portions are Copyright (C) 2008-2009 Openbravo SL
* All Rights Reserved.
* Contributor(s):  ______________________________________.
************************************************************************/
   v_org_header_id ad_org.ad_org_id%TYPE;
   v_isbusinessunit ad_orgtype.isbusinessunit%TYPE;
   v_islegalentity ad_orgtype.islegalentity%TYPE;
   v_is_included NUMBER:=0;
 
 
   TYPE RECORD IS REF CURSOR;
   cur_doc_lines_payment RECORD;
 
   v_line_org_payment VARCHAR2(32);
   v_org_payment_line_id VARCHAR2(32);
 BEGIN
 
   -- Gets the Business Unit or Legal Entity of the document
   SELECT AD_GET_DOC_LE_BU(p_header_table, p_document_id, p_header_column_id, NULL)
   INTO v_org_header_id
   FROM DUAL;
 
   -- Check the payments of the lines belong to the same BU or LE as the document header
   OPEN cur_doc_lines_payment FOR
   'SELECT DISTINCT(C_DEBT_PAYMENT.ad_org_id) AS v_line_org_payment
    FROM '||p_header_table||', '||p_lines_table||', C_DEBT_PAYMENT
    WHERE '||p_header_table||'.'||p_header_column_id||' = '||p_lines_table||'.'||p_lines_column_id||'
    AND C_DEBT_PAYMENT.C_DEBT_PAYMENT_ID='||p_lines_table||'.'||p_lines_column_payment_id||'
    AND '||p_lines_table||'.ad_org_id<>'||''''||v_org_header_id||'''
    AND '||p_lines_table||'.'||p_lines_column_id||'='''||p_document_id||'''';
 
 
   LOOP
    FETCH cur_doc_lines_payment INTO v_line_org_payment;
    EXIT WHEN cur_doc_lines_payment%NOTFOUND;
 

    SELECT ad_orgtype.isbusinessunit, ad_orgtype.islegalentity
    INTO v_isbusinessunit, v_islegalentity
    FROM AD_Org, AD_OrgType
    WHERE AD_Org.AD_OrgType_ID=AD_OrgType.AD_OrgType_ID
    AND AD_Org.AD_Org_ID=v_line_org_payment;

    v_org_payment_line_id:=v_line_org_payment;
    -- Gets recursively the organization parent until finding a Business Unit or a Legal Entity
    WHILE (v_isbusinessunit='N' AND v_islegalentity='N') LOOP
      SELECT hh.parent_id, ad_orgtype.isbusinessunit, ad_orgtype.islegalentity
      INTO v_org_payment_line_id, v_isbusinessunit, v_islegalentity
      FROM ad_org, ad_orgtype, ad_treenode pp, ad_treenode hh
      WHERE pp.node_id = hh.parent_id
      AND hh.ad_tree_id = pp.ad_tree_id
      AND pp.node_id=ad_org.ad_org_id
      AND hh.node_id=v_org_payment_line_id
      AND ad_org.ad_orgtype_id=ad_orgtype.ad_orgtype_id
      AND ad_org.isready='Y'
      AND  EXISTS (SELECT 1 FROM ad_tree WHERE ad_tree.treetype='OO' AND hh.ad_tree_id=ad_tree.ad_tree_id AND hh.ad_client_id=ad_tree.ad_client_id);     
    END LOOP;

    IF (v_org_payment_line_id<>v_org_header_id) THEN
      v_is_included:=-1;
    END IF;
    EXIT WHEN v_is_included=-1;

   END LOOP; 
   CLOSE cur_doc_lines_payment;
 
   RETURN v_is_included;
 
END AD_ORG_CHK_DOC_PAYMENTS;
/-- END

CREATE OR REPLACE VIEW AD_INTEGER AS 
SELECT to_number(a.value) AS value
   FROM ( SELECT ( SELECT count(*) AS count
                   FROM ad_element
                  WHERE ad_element.ad_element_id <= e.ad_element_id) AS value
           FROM ad_element e) a
  WHERE a.value <= 1024
/-- END

create or replace FUNCTION AD_GET_RDBMS RETURN VARCHAR2
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
* All portions are Copyright (C) 2009 Openbravo SL
* All Rights Reserved.
* Contributor(s):  ______________________________________.
************************************************************************/
BEGIN
 return 'ORACLE';
END AD_GET_RDBMS;
/-- END
 

--Regenerate mappings and classnames for tabs in modules (issue #11431)
update ad_tab set name = 'M'||name where ad_module_id != '0'
/-- END
 
update ad_tab set name = substr(name,2) where ad_module_id != '0'
/-- END

CALL DBA_RECOMPILE(NULL)
/-- END

