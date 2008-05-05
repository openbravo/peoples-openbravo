
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
 INSERT INTO AD_SCRIPT_SQL VALUES (v_seqNo, 'update pg_class set reltriggers = 0 WHERE PG_CLASS.RELNAMESPACE IN (SELECT PG_NAMESPACE.OID FROM PG_NAMESPACE WHERE PG_NAMESPACE.NSPNAME = CURRENT_SCHEMA());');
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
* All portions are Copyright (C) 2001-2008 Openbravo SL
* All Rights Reserved.
* Contributor(s):  ______________________________________.
************************************************************************/
 v_seqNo      NUMERIC := p_seqNoStart;
BEGIN
  INSERT INTO AD_SCRIPT_SQL VALUES (v_seqNo + 100000, 'update pg_class set reltriggers = (SELECT count(*) from pg_trigger where pg_class.oid=tgrelid) WHERE PG_CLASS.RELNAMESPACE IN (SELECT PG_NAMESPACE.OID FROM PG_NAMESPACE WHERE PG_NAMESPACE.NSPNAME = CURRENT_SCHEMA());');
  RETURN v_seqNo+100001;
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





