
CREATE OR REPLACE FUNCTION ad_script_disable_triggers(p_seqNoStart numeric)
  RETURNS numeric AS
$BODY$ DECLARE
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
 v_seqNo      NUMERIC := p_seqNoStart;
 Cur_Triggers RECORD;
BEGIN
 FOR Cur_Triggers IN (SELECT OBJECT_NAME AS NAME, TABLE_NAME
                      FROM USER_OBJECTS
                      WHERE OBJECT_TYPE = 'TRIGGER'
                      ORDER BY OBJECT_NAME) LOOP
    v_seqNo := v_seqNo + 1;
  --      INSERT INTO AD_SCRIPT_SQL VALUES (v_seqNo, 'ALTER TRIGGER '||Cur_Triggers.NAME||' DISABLE');
    INSERT INTO AD_SCRIPT_SQL VALUES (v_seqNo, 'ALTER TABLE  '||Cur_Triggers.TABLE_NAME||' DISABLE TRIGGER '||Cur_Triggers.NAME);
 END LOOP;
 RETURN v_seqNo;
END;   $BODY$
  LANGUAGE 'plpgsql' VOLATILE
/-- END

CREATE OR REPLACE FUNCTION ad_script_disable_constraints(p_seqNoStart numeric)
  RETURNS numeric AS
$BODY$ DECLARE
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
 v_seqNo      NUMERIC := p_seqNoStart;
 Cur_Constraints RECORD;
BEGIN
 RAISE NOTICE '%','ad_script_disable_constraints';
 FOR Cur_Constraints IN  (SELECT TABLE_NAME, CONSTRAINT_NAME
                          FROM USER_CONSTRAINTS C1
                          WHERE CONSTRAINT_TYPE IN ('P','U','R')
                          --AND DELETE_RULE NOT LIKE 'C'
                          ORDER BY (CASE CONSTRAINT_TYPE WHEN 'R' THEN 1 WHEN 'U' THEN 2 WHEN 'P' THEN 3 END), TABLE_NAME, CONSTRAINT_NAME) LOOP
   v_seqNo := v_seqNo + 1;
   --INSERT INTO AD_SCRIPT_SQL VALUES (v_seqNo, 'ALTER TABLE '||Cur_Constraints.TABLE_NAME||' DISABLE CONSTRAINT '||Cur_Constraints.CONSTRAINT_NAME);
  INSERT INTO AD_SCRIPT_SQL VALUES (v_seqNo, 'ALTER TABLE '||Cur_Constraints.TABLE_NAME||' DROP CONSTRAINT '||Cur_Constraints.CONSTRAINT_NAME);
 END LOOP;
 RETURN v_seqNo+1;
END;   $BODY$
  LANGUAGE 'plpgsql' VOLATILE
/-- END


CREATE OR REPLACE FUNCTION ad_script_enable_triggers(p_seqNoStart numeric)
  RETURNS numeric AS
$BODY$ DECLARE
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
 v_seqNo      NUMERIC := p_seqNoStart;
 Cur_Triggers RECORD;
BEGIN
 FOR Cur_Triggers IN (SELECT OBJECT_NAME AS NAME, TABLE_NAME
                      FROM USER_OBJECTS
                      WHERE OBJECT_TYPE = 'TRIGGER'
                      ORDER BY OBJECT_NAME) LOOP
      v_seqNo := v_seqNo + 1;
--    INSERT INTO AD_SCRIPT_SQL VALUES (v_seqNo, 'ALTER TRIGGER '||Cur_Triggers.NAME||' ENABLE');
    INSERT INTO AD_SCRIPT_SQL VALUES (v_seqNo+100000, 'ALTER TABLE  '||Cur_Triggers.TABLE_NAME||' ENABLE TRIGGER '||Cur_Triggers.NAME);
 END LOOP;
 RETURN v_seqNo;
END;   $BODY$
  LANGUAGE 'plpgsql' VOLATILE
/-- END

CREATE OR REPLACE FUNCTION ad_script_enable_constraints(p_seqNoStart numeric)
  RETURNS numeric AS
$BODY$ DECLARE
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
 v_seqNo      NUMERIC := p_seqNoStart;
 Cur_Constraints RECORD;
BEGIN
-- Make sure all foreign keys are satisfied
  FOR Cur_Constraints IN  (SELECT TABLE_NAME, CONSTRAINT_NAME, CONSTRAINT_TYPE, DELETE_RULE, COLUMN_NAMES, FK_TABLE, FK_COLUMN_NAMES, FK_MATCHTYPE
                           FROM USER_CONSTRAINTS C1
                           WHERE CONSTRAINT_TYPE = 'R'
                           --AND DELETE_RULE = 'C'
            ) LOOP
    v_seqNo := v_seqNo + 1;
    INSERT INTO AD_SCRIPT_SQL VALUES (v_seqNo+100000, 'DELETE FROM '||Cur_Constraints.TABLE_NAME||' WHERE '||Cur_Constraints.COLUMN_NAMES|| 
                    ' IS NOT NULL AND ' ||Cur_Constraints.COLUMN_NAMES|| 
                    ' IN (' ||
			' SELECT ' ||Cur_Constraints.COLUMN_NAMES || ' FROM ' || Cur_Constraints.TABLE_NAME || ' EXCEPT ' ||
			' SELECT ' ||Cur_Constraints.FK_COLUMN_NAMES || ' FROM ' || Cur_Constraints.FK_TABLE || ')'
                    );
  END LOOP;
 
 FOR Cur_Constraints IN  (SELECT TABLE_NAME, CONSTRAINT_NAME, CONSTRAINT_TYPE, DELETE_RULE, COLUMN_NAMES, FK_TABLE, FK_COLUMN_NAMES, FK_MATCHTYPE
                           FROM USER_CONSTRAINTS C1
                           WHERE CONSTRAINT_TYPE IN ('P','U','R')
                           --AND DELETE_RULE NOT LIKE 'C'
                           ORDER BY (CASE CONSTRAINT_TYPE WHEN 'R' THEN 3 WHEN 'U' THEN 2 WHEN 'P' THEN 1 END), TABLE_NAME, CONSTRAINT_NAME) LOOP
    v_seqNo := v_seqNo + 1;
   INSERT INTO AD_SCRIPT_SQL VALUES (v_seqNo+100000, 'ALTER TABLE '||Cur_Constraints.TABLE_NAME||' ADD CONSTRAINT '||Cur_Constraints.CONSTRAINT_NAME|| (CASE Cur_Constraints.CONSTRAINT_TYPE
     WHEN 'P' THEN '  PRIMARY KEY ('||Cur_Constraints.COLUMN_NAMES||')'
     WHEN 'U' THEN '  UNIQUE ('||Cur_Constraints.COLUMN_NAMES||')'
     WHEN 'R' THEN '  FOREIGN KEY ('||Cur_Constraints.COLUMN_NAMES||') REFERENCES '||Cur_Constraints.FK_TABLE||' ('||Cur_Constraints.FK_COLUMN_NAMES||') '||
                                (CASE Cur_Constraints.FK_MATCHTYPE WHEN 'f' THEN ' MATCH FULL' WHEN 'p' THEN ' MATCH PARTIAL' ELSE '' END) ||
                                (CASE Cur_Constraints.DELETE_RULE WHEN 'N' THEN ' ON DELETE SET NULL' WHEN 'D' THEN ' ON DELETE SET DEFAULT' WHEN 'C' THEN ' ON DELETE CASCADE' ELSE '' END)

     END));
  END LOOP;
  RETURN v_seqNo + 100001;
END;   $BODY$
  LANGUAGE 'plpgsql' VOLATILE
/-- END

CREATE OR REPLACE FUNCTION ad_script_drop_recreate_index(p_seqNoStart numeric)
  RETURNS numeric AS
$BODY$ DECLARE
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
 v_seqNo      NUMERIC := p_seqNoStart;
 v_strTemp VARCHAR(4000):='';
 v_strSql VARCHAR(4000):='';

 Cur_UniqueIndex RECORD;
 Cur_IndexColumns RECORD;
BEGIN
    FOR Cur_UniqueIndex IN (SELECT i.INDEX_NAME, i.TABLE_NAME, i.TABLESPACE_NAME, CONSTRAINT_TYPE
                 FROM USER_INDEXES I left join USER_CONSTRAINTS C1 on c1.INDEX_NAME=I.INDEX_NAME
                 WHERE UNIQUENESS='UNIQUE' AND INDEX_TYPE='NORMAL' AND TABLE_TYPE='TABLE'
               --AND CONSTRAINT_TYPE != 'U'
               ORDER BY INDEX_NAME)

    LOOP
      v_seqNo:=v_seqNo + 1;
      INSERT INTO AD_SCRIPT_SQL VALUES (v_seqNo, 'DROP INDEX '||Cur_UniqueIndex.INDEX_NAME) ;

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
           v_strSql:=v_strSql || SUBSTR(v_strTemp, 2, 4000) || ') ';
           INSERT INTO AD_SCRIPT_SQL VALUES(v_seqNo+150000, v_strSql) ;
       END IF;
    END LOOP;
 RETURN v_seqNo;
END;   $BODY$
  LANGUAGE 'plpgsql' VOLATILE
/-- END

CREATE OR REPLACE FUNCTION ad_script_execute(param_Message VARCHAR)
  RETURNS varchar AS
$BODY$ DECLARE
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
 v_Message       VARCHAR(4000) := '';
 v_ResultStr     VARCHAR(2000) := '';
 Cur_Script RECORD;
BEGIN
 v_Message := param_Message;
 FOR Cur_Script IN (SELECT STRSQL FROM AD_SCRIPT_SQL ORDER BY SEQNO) LOOP
  BEGIN
    RAISE NOTICE '%', Cur_Script.STRSQL;
    EXECUTE(Cur_Script.STRSQL);
  EXCEPTION
     WHEN OTHERS THEN
        IF (LENGTH(v_ResultStr || ': ' || SQLERRM || ' - ' ) < 1980) THEN
          v_ResultStr := v_ResultStr || ': ' || SQLERRM || ' - ';
        END IF;
       RAISE NOTICE '%',SQLERRM;
  END;
 END LOOP;
 IF( LENGTH(v_ResultStr) > 0 ) THEN
    RAISE NOTICE '%', 'Script errors: ' || v_ResultStr;
 END IF;
 return substr(coalesce(v_ResultStr,'') || coalesce(v_Message,''), 1, 2000);
END;   $BODY$
  LANGUAGE 'plpgsql' VOLATILE
/-- END

CREATE OR REPLACE FUNCTION AD_GET_DOC_LE_BU(p_header_table character varying, p_document_id character varying, p_header_column_id character varying, p_type character varying)
 RETURNS varchar AS
$BODY$ DECLARE
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
   EXECUTE 
     'SELECT ad_org.ad_org_id, ad_orgtype.isbusinessunit, ad_orgtype.islegalentity 
     FROM '||p_header_table||', ad_org, ad_orgtype
     WHERE '||p_header_table||'.'||p_header_column_id||'='||''''||p_document_id||''''||' 
     AND ad_org.ad_orgtype_id = ad_orgtype.ad_orgtype_id
     AND '||p_header_table||'.ad_org_id=ad_org.ad_org_id' 
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
	     AND  EXISTS (SELECT 1 FROM ad_tree WHERE ad_tree.treetype='OO' AND hh.ad_tree_id=ad_tree.ad_tree_id and hh.ad_client_id=ad_tree.ad_client_id);     
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
         AND  EXISTS (SELECT 1 FROM ad_tree WHERE ad_tree.treetype='OO' AND hh.ad_tree_id=ad_tree.ad_tree_id and hh.ad_client_id=ad_tree.ad_client_id);     
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
         AND  EXISTS (SELECT 1 FROM ad_tree WHERE ad_tree.treetype='OO' AND hh.ad_tree_id=ad_tree.ad_tree_id and hh.ad_client_id=ad_tree.ad_client_id);     
       END LOOP;
       RETURN NULL;
    END IF;
   
   RETURN v_org_header_id;
   
END;   $BODY$
  LANGUAGE 'plpgsql' VOLATILE
/-- END
	 
CREATE OR REPLACE FUNCTION AD_ORG_CHK_DOCUMENTS(p_header_table character varying, p_lines_table character varying, p_document_id character varying, p_header_column_id character varying, p_lines_column_id character varying)
   RETURNS numeric AS
$BODY$ DECLARE
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
   v_is_included NUMERIC:=0;
   v_dyn_cur VARCHAR(2000);
 
   TYPE_Ref REFCURSOR;
   cur_doc_lines TYPE_REF%TYPE;
 
   v_line_org VARCHAR(32);
   v_org_line_id VARCHAR(32);
 BEGIN
 
   -- Gets the Business Unit or Legal Entity of the document
   SELECT AD_GET_DOC_LE_BU(p_header_table, p_document_id, p_header_column_id, NULL)
   INTO v_org_header_id
   FROM DUAL;
 
   v_dyn_cur:='SELECT DISTINCT('||p_lines_table||'.ad_org_id) AS v_line_org 
	FROM '||p_header_table||', '||p_lines_table||'  
	WHERE '||p_header_table||'.'||p_header_column_id||' = '||p_lines_table||'.'||p_lines_column_id||' 
	AND '||p_lines_table||'.ad_org_id<>'||''''||v_org_header_id||'''
	AND '||p_lines_table||'.'||p_lines_column_id||'='||''''||p_document_id||'''';
 
   OPEN cur_doc_lines FOR EXECUTE v_dyn_cur;   
    LOOP
      FETCH cur_doc_lines INTO v_line_org;
      IF NOT FOUND THEN
        EXIT;
      END IF;

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
 
END;   $BODY$
  LANGUAGE 'plpgsql' VOLATILE
/-- END

CREATE OR REPLACE FUNCTION AD_ORG_CHK_DOC_PAYMENTS(p_header_table IN character varying, p_lines_table IN character varying, p_document_id IN character varying, p_header_column_id IN character varying, p_lines_column_id IN character varying, p_lines_column_payment_id IN character varying) 
 RETURNS numeric AS
 $BODY$ DECLARE
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
   v_is_included NUMERIC:=0;
   v_dyn_cur VARCHAR(2000);
 
   TYPE_Ref REFCURSOR;
   cur_doc_lines_payment TYPE_REF%TYPE;
 
   v_line_org_payment VARCHAR(32);
   v_org_payment_line_id VARCHAR(32);
 BEGIN
 
   -- Gets the Business Unit or Legal Entity of the document
   SELECT AD_GET_DOC_LE_BU(p_header_table, p_document_id, p_header_column_id, NULL)
   INTO v_org_header_id
   FROM DUAL;
 
   v_dyn_cur:='SELECT DISTINCT(C_DEBT_PAYMENT.ad_org_id) AS v_line_org_payment
    FROM '||p_header_table||', '||p_lines_table||', C_DEBT_PAYMENT
    WHERE '||p_header_table||'.'||p_header_column_id||' = '||p_lines_table||'.'||p_lines_column_id||'
    AND C_DEBT_PAYMENT.C_DEBT_PAYMENT_ID='||p_lines_table||'.'||p_lines_column_payment_id||'
	AND '||p_lines_table||'.ad_org_id<>'||''''||v_org_header_id||'''
    AND '||p_lines_table||'.'||p_lines_column_id||'='||''''||p_document_id||'''';

   -- Check the payments of the lines belong to the same BU or LE as the document header
   OPEN cur_doc_lines_payment FOR EXECUTE v_dyn_cur;
    LOOP
     FETCH cur_doc_lines_payment INTO v_line_org_payment;
     IF NOT FOUND THEN
       EXIT;
     END IF;


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
 
END;   $BODY$
  LANGUAGE 'plpgsql' VOLATILE
/-- END

CREATE OR REPLACE FUNCTION AD_GET_RDBMS()
  RETURNS varchar AS
$BODY$ DECLARE
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
  return 'POSTGRE';
END;   $BODY$
  LANGUAGE 'plpgsql' VOLATILE
/-- END

CREATE OR REPLACE VIEW AD_INTEGER AS
SELECT a.value::numeric AS value
   FROM generate_series(1, 1024) a(value);
/-- END

CREATE OR REPLACE FUNCTION uuid_generate_v4()
RETURNS uuid
AS '$libdir/uuid-ossp', 'uuid_generate_v4'
VOLATILE STRICT LANGUAGE C;
/-- END

alter table ad_tab disable trigger ad_tab_mod_trg;
/-- END

--Regenerate mappings and classnames for tabs in modules (issue #11431)
update ad_tab set name = 'M'||name where ad_module_id != '0';
/-- END
 
update ad_tab set name = substr(name,2) where ad_module_id != '0';
/-- END

alter table ad_tab enable trigger ad_tab_mod_trg;
/-- END

-- Inserts an alert recipient for available updates
-- See issue:  https://issues.openbravo.com/view.php?id=11743
CREATE OR REPLACE FUNCTION pg_temp.insert_recipient()
  RETURNS void AS
$BODY$ DECLARE
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
  INSERT INTO ad_alertrecipient(ad_client_id, ad_org_id, isactive, created, createdby,
                              updated, updatedby, ad_alertrecipient_id, ad_alertrule_id,
                              ad_role_id, sendemail)
       VALUES('0', '0', 'Y', now(), '100', now(), '100', '8CC1347628D148FABA1FC26622F4B070', '1005400000', '0', 'N');
EXCEPTION
WHEN OTHERS THEN
--do nothing
END;   $BODY$
  LANGUAGE 'plpgsql' VOLATILE;
SELECT pg_temp.insert_recipient();
/-- END

--Inserts role access for new register window
--See issue:  https://issues.openbravo.com/view.php?id=11349
CREATE OR REPLACE FUNCTION pg_temp.insert_register_form_access()
  RETURNS void AS
$BODY$ DECLARE
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
  INSERT INTO ad_form_access(ad_form_access_id, ad_form_id, ad_role_id,
                               ad_client_id, ad_org_id, isactive, created,
                               createdby, updated, updatedby, isreadwrite)
         VALUES('41263F39F7614270808A955844B07A7F', '3D8AB0C824ED4C70ADE086D9CFE5DA1A', '0', '0', '0', 'Y', now(), '0', now(), '0', 'Y');
EXCEPTION
WHEN OTHERS THEN
--do nothing
END;   $BODY$
  LANGUAGE 'plpgsql' VOLATILE;
SELECT pg_temp.insert_register_form_access();
/-- END
