
CREATE OR REPLACE FUNCTION ad_script_disable_triggers(p_seqNoStart numeric)
  RETURNS numeric AS
$BODY$ DECLARE
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
 v_seqNo      NUMERIC := p_seqNoStart;
 Cur_Constraints RECORD;
BEGIN
 FOR Cur_Constraints IN  (SELECT TABLE_NAME, CONSTRAINT_NAME
                          FROM USER_CONSTRAINTS C1
                          WHERE CONSTRAINT_TYPE IN ('P','U','R')
                          --AND DELETE_RULE NOT LIKE 'C'
                          ORDER BY (CASE CONSTRAINT_TYPE WHEN 'R' THEN 1 WHEN 'U' THEN 2 WHEN 'P' THEN 3 END), TABLE_NAME, CONSTRAINT_NAME) LOOP
   v_seqNo := v_seqNo + 1;
   --INSERT INTO AD_SCRIPT_SQL VALUES (v_seqNo, 'ALTER TABLE '||Cur_Constraints.TABLE_NAME||' DISABLE CONSTRAINT '||Cur_Constraints.CONSTRAINT_NAME);
  INSERT INTO AD_SCRIPT_SQL VALUES (v_seqNo, 'ALTER TABLE '||Cur_Constraints.TABLE_NAME||' DROP CONSTRAINT '||Cur_Constraints.CONSTRAINT_NAME);
 END LOOP;
 RETURN v_seqNo;
END;   $BODY$
  LANGUAGE 'plpgsql' VOLATILE
/-- END


CREATE OR REPLACE FUNCTION ad_script_enable_triggers(p_seqNoStart numeric)
  RETURNS numeric AS
$BODY$ DECLARE
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
 v_seqNo      NUMERIC := p_seqNoStart;
 Cur_Constraints RECORD;
BEGIN
 /*FOR Cur_ConstraintsEnable IN (SELECT TABLE_NAME, CONSTRAINT_NAME
                FROM USER_CONSTRAINTS C1
          WHERE CONSTRAINT_TYPE IN ('P','U','R')
          AND DELETE_RULE NOT LIKE 'CASCADE'
          ORDER BY (CASE CONSTRAINT_TYPE WHEN 'R' THEN 3 WHEN 'U' THEN 2 WHEN 'P' THEN 1 END), TABLE_NAME, CONSTRAINT_NAME) LOOP
   v_seqNo := v_seqNo + 1;
   INSERT INTO AD_SCRIPT_SQL VALUES (v_seqNo+100000, 'ALTER TABLE '||Cur_ConstraintsEnable.TABLE_NAME||' ENABLE CONSTRAINT '||Cur_ConstraintsEnable.CONSTRAINT_NAME);
 END LOOP;*/

-- Make sure all foreign keys are satisfied
  FOR Cur_Constraints IN  (SELECT TABLE_NAME, CONSTRAINT_NAME, CONSTRAINT_TYPE, DELETE_RULE, COLUMN_NAMES, FK_TABLE, FK_COLUMN_NAMES, FK_MATCHTYPE
                           FROM USER_CONSTRAINTS C1
                           WHERE CONSTRAINT_TYPE = 'R'
                           --AND DELETE_RULE = 'C'
            ) LOOP
    v_seqNo := v_seqNo + 1;
    INSERT INTO AD_SCRIPT_SQL VALUES (v_seqNo+100000, 'DELETE FROM '||Cur_Constraints.TABLE_NAME||' WHERE '||Cur_Constraints.COLUMN_NAMES|| 
                    ' IS NOT NULL AND ' ||Cur_Constraints.COLUMN_NAMES|| 
                    ' NOT IN (SELECT ' ||Cur_Constraints.FK_COLUMN_NAMES || ' FROM ' || Cur_Constraints.FK_TABLE || ')'
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
  RETURN v_seqNo;
END;   $BODY$
  LANGUAGE 'plpgsql' VOLATILE
/-- END

CREATE OR REPLACE FUNCTION ad_script_drop_recreate_indexes(p_seqNoStart numeric)
  RETURNS numeric AS
$BODY$ DECLARE
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

CREATE OR REPLACE FUNCTION ad_script_execute()
  RETURNS varchar AS
$BODY$ DECLARE
 v_ResultStr     VARCHAR(2000) := '';
 Cur_Script RECORD;
BEGIN
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
 return v_ResultStr;
END;   $BODY$
  LANGUAGE 'plpgsql' VOLATILE
/-- END


-- Function: ad_delete_client(p_pinstance_id numeric, p_ad_client_id numeric)
-- DROP FUNCTION ad_delete_client(p_pinstance_id numeric, p_ad_client_id numeric)/-- END

CREATE OR REPLACE FUNCTION ad_delete_client(p_pinstance_id numeric, p_ad_client_id numeric)
  RETURNS void AS
$BODY$ DECLARE
 -- Logistice
 v_ResultStr     VARCHAR(2000) := '';
 v_Result      NUMERIC := 1; -- 0=failure

 -- Parameter
 --TYPE RECORD IS REFCURSOR;
  Cur_Parameter RECORD; 
 
 -- Parameter Variables
 v_AD_Client_ID NUMERIC(10);
 p_NewClientValue VARCHAR(80);
 --
 Cur_Triggers RECORD;
 Cur_Constraints RECORD;
 Cur_ConstraintsEnable RECORD;
 Cur_Tables RECORD;
    --
 v_seqNo         NUMERIC := 0;
 v_NextNo  NUMERIC(10) := 1000000;
 v_count   NUMERIC := 0;
 v_ID_Column  VARCHAR(30);
 v_minID   NUMERIC(10);
 v_maxID   NUMERIC(10);
 v_nextID  NUMERIC(10);
 v_strTemp  VARCHAR(4000) := '';
 v_strSql  VARCHAR(4000) := '';
 v_tableActual   VARCHAR(30);

BEGIN
 IF (p_PInstance_ID IS NOT NULL) THEN
     --  Update AD_PInstance
  RAISE NOTICE '%','Updating PInstance - Processing ' || p_PInstance_ID;
  v_ResultStr := 'PInstanceNotFound';
     PERFORM Ad_Update_PInstance(p_PInstance_ID, NULL, 'Y', NULL, NULL);
  -- Get Parameters
  v_ResultStr := 'ReadingParameters';
  FOR Cur_Parameter IN (SELECT p.ParameterName, p.P_String, p.P_Number
                        FROM AD_PInstance i left join AD_PInstance_Para p on i.AD_PInstance_ID=p.AD_PInstance_ID
                        WHERE i.AD_PInstance_ID=p_PInstance_ID
                        ORDER BY p.SeqNo) LOOP
          IF (Cur_Parameter.ParameterName = 'AD_Client_ID') THEN
              v_AD_Client_ID := Cur_Parameter.P_Number;
              RAISE NOTICE '%','  AD_Client_ID=' || v_AD_Client_ID;
          ELSE
              RAISE NOTICE '%','*** Unknown Parameter=' || Cur_Parameter.ParameterName;
          END IF;
  END LOOP; -- Get Parameter
 ELSE
     v_AD_Client_ID := p_AD_Client_ID;
     --v_Message := '@ADPinstanceIsNull@';
  --GOTO END_PROCESS;
 END IF;
BEGIN --BODY
 DELETE FROM AD_SCRIPT_SQL;
 v_ResultStr := 'Disabling  triggers';
 v_seqNo = ad_script_disable_triggers(v_seqNo);

 v_ResultStr := 'Disabling constraints';
 v_seqNo = ad_script_disable_constraints(v_seqNo);

 v_NextNo:= AD_Sequence_Next('AD_Client', 1000000);

 FOR Cur_Tables IN (SELECT AD_Table_ID, UPPER(TableName) AS NAME
                    FROM AD_Table
                    WHERE IsView = 'N'
                    AND UPPER(TableName) <> 'AD_CLIENT'
                     AND EXISTS
                       (SELECT 1 FROM AD_Column
                       WHERE AD_Table_ID = AD_Table.AD_Table_ID
                       AND UPPER(ColumnName) = 'AD_CLIENT_ID')
                       ORDER BY TableName) LOOP
   v_seqNo := v_seqNo + 1;
   INSERT INTO AD_SCRIPT_SQL VALUES (v_seqNo, 'DELETE FROM '||Cur_Tables.NAME||' WHERE AD_Client_ID = '||v_AD_Client_ID);
 END LOOP;

 v_seqNo := v_seqNo + 1;
 INSERT INTO AD_SCRIPT_SQL VALUES (v_seqNo, 'DELETE FROM AD_Client WHERE AD_Client_ID = '||v_AD_Client_ID);
-- Deal with users that have defaults set to the deleted client
 v_seqNo := v_seqNo + 1;
 INSERT INTO AD_SCRIPT_SQL VALUES (v_seqNo, 'UPDATE AD_USER SET DEFAULT_AD_CLIENT_ID=null where DEFAULT_AD_CLIENT_ID NOT IN (SELECT AD_Client_ID from AD_Client)');
 v_seqNo := v_seqNo + 1;
 INSERT INTO AD_SCRIPT_SQL VALUES (v_seqNo, 'UPDATE AD_USER SET DEFAULT_AD_ORG_ID=null where DEFAULT_AD_ORG_ID NOT IN (SELECT AD_Org_ID from AD_Org)');
 v_seqNo := v_seqNo + 1;
 INSERT INTO AD_SCRIPT_SQL VALUES (v_seqNo, 'UPDATE AD_USER SET DEFAULT_AD_ROLE_ID=null where DEFAULT_AD_ROLE_ID NOT IN (SELECT AD_Role_ID from AD_Role)');
 v_seqNo := v_seqNo + 1;
 INSERT INTO AD_SCRIPT_SQL VALUES (v_seqNo, 'UPDATE AD_USER SET DEFAULT_M_WAREHOUSE_ID=null where DEFAULT_M_WAREHOUSE_ID NOT IN (SELECT M_Warehouse_ID from M_Warehouse)');
 -- make sure Openbravo user can logon
 v_seqNo := v_seqNo + 1;
 INSERT INTO AD_SCRIPT_SQL VALUES (v_seqNo, 'UPDATE AD_USER SET DEFAULT_AD_CLIENT_ID=0, DEFAULT_AD_ORG_ID=0, DEFAULT_AD_ROLE_ID=0 where AD_USER_ID=100 AND DEFAULT_AD_CLIENT_ID is null and DEFAULT_AD_ORG_ID is null and DEFAULT_AD_ROLE_ID is null');


 v_ResultStr := 'Enabling  constraints';
 v_seqNo = ad_script_enable_constraints(v_seqNo);

 v_ResultStr := 'Enabling  triggers';
 v_seqNo = ad_script_enable_triggers(v_seqNo);

 -- Update sequence values
 v_ResultStr := 'Updating sequences';
 PERFORM Ad_Update_Sequence_Generate();

 v_ResultStr := ad_script_execute();

----<<END_PROCESS>>
 IF (p_PInstance_ID IS NOT NULL) THEN
  --  Update AD_PInstance
  RAISE NOTICE '%','Updating PInstance - Finished ' || v_ResultStr;
     --PERFORM Ad_Update_PInstance(p_PInstance_ID, NULL, 'N', v_Result, v_ResultStr);
    RAISE NOTICE '%','AD_UPDATE_PINSTANCE';
    UPDATE AD_PINSTANCE
        SET Updated=TO_DATE(now()),
        IsProcessing='N',
        Result=v_Result, -- 1=success
        ErrorMsg=v_ResultStr
    WHERE AD_PInstance_ID=p_PInstance_ID;

 ELSE
  RAISE NOTICE '%','Finished ' || v_ResultStr;
 END IF;
 
    RETURN;

END; --BODY
EXCEPTION
    WHEN  OTHERS THEN
  v_ResultStr := v_ResultStr || ': ' || SQLERRM;
  RAISE NOTICE '%',v_ResultStr;
  
  IF (p_PInstance_ID IS NOT NULL) THEN
      PERFORM Ad_Update_PInstance(p_PInstance_ID, NULL, 'N', 0, v_ResultStr);
  END IF;
    RETURN;

END;   $BODY$
  LANGUAGE 'plpgsql' VOLATILE
/-- END



-- Function: ad_copy_client(p_pinstance_id numeric)
-- DROP FUNCTION ad_copy_client(p_pinstance_id numeric)/-- END

CREATE OR REPLACE FUNCTION ad_copy_client(p_pinstance_id numeric)
  RETURNS void AS
$BODY$ DECLARE
  --  Logistice
  v_ResultStr VARCHAR(2000):='';
  v_Result NUMERIC:=1; -- 0=failure
  --  Parameter
  --TYPE RECORD IS REFCURSOR;
    Cur_Parameter RECORD;
    --  Parameter Variables
    p_AD_Client_ID NUMERIC(10) ;
    p_NewClientValue VARCHAR(80) ;
    --
    Cur_Triggers RECORD;
    Cur_Triggers1 RECORD;
    Cur_Constraints RECORD;
    Cur_ConstraintsEnable RECORD;
    Cur_UniqueIndex RECORD;
    Cur_IndexColumns RECORD;
    Cur_Tables RECORD;
    Cur_RoleTables RECORD;
    Cur_Columns RECORD;
    Cur_Columns1 RECORD;
    Cur_TranslateData RECORD;
    Cur_TranslateDataSelfDep RECORD;
    Cur_Script RECORD;

    CUR_v_rc_ISOPEN BOOLEAN :=false;

    --
    v_seqNo NUMERIC:=0;
    v_NextNo NUMERIC(10):=1000000;
    v_count NUMERIC:=0;
    v_ID_Column VARCHAR(30) ;
    v_minID NUMERIC(10) ;
    v_maxID NUMERIC(10) ;
    v_nextID NUMERIC(10) ;
    v_strTemp VARCHAR(4000):='';
    v_strSql VARCHAR(4000):='';
    v_tableActual VARCHAR(30) ;
    v_translatedId NUMERIC:=0;
    v_oldId NUMERIC:=0;
    v_dynamic_select VARCHAR(4000):='';
 TYPE_Ref REFCURSOR;
    v_rc TYPE_REF%TYPE;
    v_Tree_ID NUMERIC;
    v_offset NUMERIC;
    v_offset2 NUMERIC;
  BEGIN
    IF(p_PInstance_ID IS NOT NULL) THEN
      --  Update AD_PInstance
      RAISE NOTICE '%','Updating PInstance - Processing ' || p_PInstance_ID;
      v_ResultStr:='PInstanceNotFound';
      PERFORM Ad_Update_PInstance(p_PInstance_ID, NULL, 'Y', NULL, NULL) ;
      --  Get Parameters
      v_ResultStr:='ReadingParameters';
      RAISE NOTICE '%',v_ResultStr;
      FOR Cur_Parameter IN
        (SELECT p.ParameterName, p.P_String, p.P_Number
        FROM AD_PInstance i
        LEFT JOIN AD_PInstance_Para p
          ON i.AD_PInstance_ID=p.AD_PInstance_ID
        WHERE i.AD_PInstance_ID=p_PInstance_ID
        ORDER BY p.SeqNo
        )
      LOOP
        IF(Cur_Parameter.ParameterName='AD_Client_ID') THEN
          p_AD_Client_ID:=Cur_Parameter.P_Number;
          RAISE NOTICE '%','  AD_Client_ID=' || p_AD_Client_ID;
        ELSIF(Cur_Parameter.ParameterName='ClientValue') THEN
          p_NewClientValue:=Cur_Parameter.P_String;
          RAISE NOTICE '%','  ClientValue=' || p_NewClientValue;
        ELSE
          RAISE NOTICE '%','*** Unknown Parameter=' || Cur_Parameter.ParameterName;
        END IF;
      END LOOP; --  Get Parameter
    ELSE
      p_AD_Client_ID:=1000001;
      p_NewClientValue:='Test2';
      --v_Message := '@ADPinstanceIsNull@';
      --GOTO END_PROCESS;
    END IF;
  BEGIN --BODY
    -- Update sequence values
    v_ResultStr:='Updating sequences';
    RAISE NOTICE '%',v_ResultStr;
    PERFORM Ad_Update_Sequence() ;
    v_ResultStr:='Deleting old data';
    RAISE NOTICE '%',v_ResultStr;
    DELETE FROM AD_SCRIPT_SQL;
    DELETE FROM AD_ID_TRANSLATION;
    PERFORM Ad_Dependencies_Create() ;

    v_ResultStr:='Disabling triggers';
    RAISE NOTICE '%',v_ResultStr;
    v_seqNo:= ad_script_disable_triggers(v_seqNo);

    v_ResultStr:='Disabling constraints';
    RAISE NOTICE '%',v_ResultStr;
    v_seqNo:= ad_script_disable_constraints(v_seqNo);

    v_ResultStr:='Dropping and recreating unique indexes';
    RAISE NOTICE '%',v_ResultStr;
    v_seqNo:= ad_script_drop_recreate_indexes(v_seqNo);

    v_ResultStr:='Insert client';
    RAISE NOTICE '%',v_ResultStr;
 v_NextNo:= AD_Sequence_Next('AD_Client', 1000000);
    --AD_Client
    v_seqNo:=v_seqNo + 1;
    INSERT
    INTO AD_SCRIPT_SQL VALUES
      (v_seqNo, 'INSERT INTO AD_Client (AD_CLIENT_ID, AD_ORG_ID, ISACTIVE, CREATED, CREATEDBY, UPDATED, UPDATEDBY, '||
      'VALUE, NAME, DESCRIPTION, SMTPHOST, REQUESTEMAIL, REQUESTUSER, REQUESTUSERPW, REQUESTFOLDER, '||
      'AD_LANGUAGE, WEBDIR, ISMULTILINGUALDOCUMENT, ISSMTPAUTHORIZATION, DOCUMENTDIR, '||
      'WEBPARAM1, WEBPARAM2, WEBPARAM3, WEBPARAM4, WEBORDEREMAIL, WEBINFO, WEBPARAM6, WEBPARAM5) '||
      'SELECT '||v_NextNo||', 0, ISACTIVE, TO_DATE(now()), 0, TO_DATE(now()), 0, '||''''||p_NewClientValue||''', '''||p_NewClientValue||''', DESCRIPTION, SMTPHOST, REQUESTEMAIL, '||
      'REQUESTUSER, REQUESTUSERPW, REQUESTFOLDER, AD_LANGUAGE, WEBDIR, ISMULTILINGUALDOCUMENT, '|| 'ISSMTPAUTHORIZATION, DOCUMENTDIR, WEBPARAM1, WEBPARAM2, WEBPARAM3, WEBPARAM4, WEBORDEREMAIL, '||
      'WEBINFO, WEBPARAM6, WEBPARAM5 '|| 'FROM AD_Client '|| 'WHERE AD_Client_ID = '||p_AD_Client_ID) ;
      v_ResultStr:='Insert role';
    RAISE NOTICE '%',v_ResultStr;
    --AD_Role
    SELECT MIN(AD_ROLE_ID)
    INTO v_minID
    FROM AD_ROLE
    WHERE ClientList=to_char(p_AD_Client_ID);
    SELECT CurrentNext
    INTO v_nextID
    FROM AD_Sequence
    WHERE UPPER(Name)='AD_ROLE' AND IsActive='Y' AND IsTableID='Y' AND IsAutoSequence='Y';
    INSERT
    INTO AD_ID_TRANSLATION
      (
        AD_TABLE, AD_COLUMN, AD_CLIENT_OLD, AD_CLIENT_NEW, "OFFSET"
      )
      VALUES
      ('AD_ROLE', 'AD_ROLE_ID', p_AD_Client_ID, v_NextNo, v_nextID-v_minID) ;
    v_seqNo:=v_seqNo + 1;
    INSERT
    INTO AD_SCRIPT_SQL VALUES
      (v_seqNo, 'INSERT INTO AD_Role (AD_ROLE_ID, AD_CLIENT_ID, AD_ORG_ID, ISACTIVE, CREATED, CREATEDBY, UPDATED, UPDATEDBY, '||
      'NAME, DESCRIPTION, USERLEVEL, CLIENTLIST, ORGLIST, C_CURRENCY_ID, AMTAPPROVAL, AD_TREE_MENU_ID, ISMANUAL) '||
      'SELECT AD_ROLE_ID + '||TO_CHAR(v_nextID-v_minID) ||', '||v_NextNo||', 0, ISACTIVE, TO_DATE(now()), 0, TO_DATE(now()), 0, '||
      'NAME, DESCRIPTION, USERLEVEL, '''||v_NextNo||''', ORGLIST, C_CURRENCY_ID, AMTAPPROVAL, AD_TREE_MENU_ID, ISMANUAL '||
      'FROM AD_Role '|| 'WHERE ClientList = '''||p_AD_Client_ID||'''') ;
    v_ResultStr:='Insert into role tables';
    RAISE NOTICE '%',v_ResultStr;
    FOR Cur_RoleTables IN
      (SELECT AD_Table_ID, UPPER(TableName) AS NAME
    FROM AD_Table
    WHERE IsView='N' AND(UPPER(TableName) LIKE 'AD_%ACCESS' OR UPPER(TableName) IN('AD_USER_ROLES'))
    ORDER BY TableName)
    LOOP
      v_seqNo:=v_seqNo + 1;
      v_strSql:='INSERT INTO '||Cur_RoleTables.NAME||' (AD_CLIENT_ID';
      v_strTemp:='';
      FOR Cur_Columns IN
        (SELECT UPPER(COLUMNNAME) AS NAME
      FROM AD_Column
      WHERE AD_Table_ID=Cur_RoleTables.AD_Table_ID AND UPPER(ColumnName)<>'AD_CLIENT_ID' AND(UPPER(ColumnName)<>'' OR '' IS NULL)
      ORDER BY UPPER(COLUMNNAME))
      LOOP
        v_strTemp:=v_strTemp ||','|| Cur_Columns.NAME;
      END LOOP;
      v_strSql:=v_strSql || v_strTemp ||') SELECT ' || v_NextNo|| v_strTemp || ' FROM ' || Cur_RoleTables.NAME;
      v_strSql:=v_strSql || ' WHERE AD_ROLE_ID IN (SELECT AD_ROLE_ID FROM AD_ROLE WHERE ClientList = '''||p_AD_Client_ID||''')';
      INSERT INTO AD_SCRIPT_SQL VALUES(v_seqNo, v_strSql) ;
    END LOOP;
    v_ResultStr:='Insert into tables';
    RAISE NOTICE '%',v_ResultStr;
    FOR Cur_Tables IN
      (SELECT AD_Table_ID, UPPER(TableName) AS NAME
    FROM AD_Table
    WHERE IsView='N'
      AND(UPPER(TableName) NOT LIKE 'AD_%'
      OR UPPER(TableName) LIKE 'AD_TREE%'
      OR UPPER(TableName) IN('AD_CLIENTINFO', 'AD_ORG', 'AD_ORGINFO', 'AD_IMPFORMAT', 'AD_IMPFORMATROW', 'AD_SEQUENCE', 'AD_USER', 'AD_PROCESS_SCHEDULING'))
      AND UPPER(TableName) != 'FACT_ACCT'
   AND EXISTS
      (SELECT 1
      FROM AD_Column
      WHERE AD_Table_ID=AD_Table.AD_Table_ID AND UPPER(ColumnName)='AD_CLIENT_ID'
      )
    ORDER BY TableName)
    LOOP
      EXECUTE 'SELECT COUNT(*) FROM '||Cur_Tables.NAME||' WHERE AD_Client_ID = '||p_AD_Client_ID INTO v_count;
      IF(v_count>0) THEN
        SELECT MAX(UPPER(ColumnName))
        INTO v_ID_Column
        FROM AD_Column
        WHERE AD_Table_ID=Cur_Tables.AD_Table_ID AND AD_Reference_ID=13 AND UPPER(ColumnName)=Cur_Tables.NAME||'_ID';
        IF(v_ID_Column IS NOT NULL) THEN
          EXECUTE 'SELECT MIN('||v_ID_Column||') FROM '||Cur_Tables.NAME||' WHERE AD_Client_ID = '||p_AD_Client_ID INTO v_minID;
          SELECT CurrentNext
          INTO v_nextID
          FROM AD_Sequence
          WHERE UPPER(Name)=Cur_Tables.NAME AND IsActive='Y' AND IsTableID='Y' AND IsAutoSequence='Y';
          INSERT
          INTO AD_ID_TRANSLATION
            (
              AD_TABLE, AD_COLUMN, AD_CLIENT_OLD, AD_CLIENT_NEW, "OFFSET"
            )
            VALUES
            (Cur_Tables.NAME, v_ID_Column, p_AD_Client_ID, v_NextNo, v_nextID-v_minID) ;
        END IF;
        v_strTemp:='';
        FOR Cur_Columns IN
          (SELECT UPPER(COLUMNNAME) AS NAME
        FROM AD_Column
        WHERE AD_Table_ID=Cur_Tables.AD_Table_ID AND UPPER(ColumnName)<>'AD_CLIENT_ID' AND(UPPER(ColumnName)<>v_ID_Column OR v_ID_Column IS NULL)
        ORDER BY UPPER(COLUMNNAME))
        LOOP
          v_strTemp:=v_strTemp ||','|| Cur_Columns.NAME;
        END LOOP;
        v_strSql:='INSERT INTO '||Cur_Tables.NAME||' (AD_CLIENT_ID';
        IF(v_ID_Column IS NOT NULL) THEN
          v_strSql:=v_strSql || ',' || v_ID_Column;
        END IF;
        v_strSql:=v_strSql || v_strTemp || ') SELECT ' || v_NextNo;
        IF(v_ID_Column IS NOT NULL) THEN
          v_strSql:=v_strSql || ',' || v_ID_Column || ' + '|| TO_CHAR(v_nextID-v_minID) ;
        END IF;
        v_strSql:=v_strSql || v_strTemp || ' FROM '|| Cur_Tables.NAME || ' WHERE AD_CLIENT_ID = ' || p_AD_Client_ID;
        v_seqNo:=v_seqNo + 1;
        INSERT INTO AD_SCRIPT_SQL VALUES(v_seqNo, v_strSql) ;
      END IF;
    END LOOP;
    v_ResultStr:='Updating tables';
    RAISE NOTICE '%',v_ResultStr;
    v_strSql:='';
    v_tableActual:='xx';
    v_count:=0;
    FOR Cur_TranslateData IN
      (SELECT AD_DEPENDENCIES.TABLENAME, AD_DEPENDENCIES.COLUMNNAME, AD_DEPENDENCIES.DEPENDS_ON_TABLENAME, AD_DEPENDENCIES.DEPENDS_ON_COLUMNNAME, AD_ID_TRANSLATION."OFFSET"
    FROM AD_DEPENDENCIES, AD_ID_TRANSLATION
    WHERE AD_DEPENDENCIES.DEPENDS_ON_TABLENAME=AD_ID_TRANSLATION.AD_TABLE AND AD_DEPENDENCIES.DEPENDS_ON_TABLENAME<>AD_DEPENDENCIES.TABLENAME AND AD_DEPENDENCIES.DEPENDS_ON_COLUMNNAME=AD_ID_TRANSLATION.AD_COLUMN
    ORDER BY AD_DEPENDENCIES.TABLENAME, AD_DEPENDENCIES.COLUMNNAME)
    LOOP
      IF(Cur_TranslateData.TABLENAME<>v_tableActual OR v_count>20) THEN
        v_count:=0;
        v_tableActual:=Cur_TranslateData.TABLENAME;
        IF(v_strSql IS NOT NULL) THEN
          v_strSql:=v_strSql || ' WHERE AD_CLIENT_ID = '|| v_NextNo;
          v_seqNo:=v_seqNo + 1;
          INSERT INTO AD_SCRIPT_SQL VALUES(v_seqNo, v_strSql) ;
        END IF;
        v_strSql:='UPDATE '||v_tableActual||' A SET AD_CLIENT_ID = '|| v_NextNo || ' ';
      END IF;
      v_strSql:=v_strSql || ', '||Cur_TranslateData.COLUMNNAME||' = AD_Translate_ID('||Cur_TranslateData.COLUMNNAME|| ',
        '||Cur_TranslateData."OFFSET"||','''||Cur_TranslateData.DEPENDS_ON_TABLENAME||''','''||Cur_TranslateData.DEPENDS_ON_COLUMNNAME||''',
        '||p_AD_Client_ID||')';
      v_count:=v_count + 1;
    END LOOP;
    v_strSql:=v_strSql || ' WHERE AD_CLIENT_ID = '|| v_NextNo;
    v_seqNo:=v_seqNo + 1;
    INSERT INTO AD_SCRIPT_SQL VALUES(v_seqNo, v_strSql) ;
    v_ResultStr:='Updating tables1';
    RAISE NOTICE '%',v_ResultStr;
    v_strSql:='';
    v_tableActual:='xx';
    v_count:=0;
    FOR Cur_TranslateDataSelfDep IN
      (SELECT AD_DEPENDENCIES.TABLENAME, AD_DEPENDENCIES.COLUMNNAME, AD_DEPENDENCIES.DEPENDS_ON_TABLENAME, AD_DEPENDENCIES.DEPENDS_ON_COLUMNNAME, AD_ID_TRANSLATION."OFFSET"
    FROM AD_DEPENDENCIES, AD_ID_TRANSLATION
    WHERE AD_DEPENDENCIES.DEPENDS_ON_TABLENAME=AD_ID_TRANSLATION.AD_TABLE AND AD_DEPENDENCIES.DEPENDS_ON_TABLENAME=AD_DEPENDENCIES.TABLENAME AND AD_DEPENDENCIES.DEPENDS_ON_COLUMNNAME=AD_ID_TRANSLATION.AD_COLUMN
    ORDER BY AD_DEPENDENCIES.TABLENAME, AD_DEPENDENCIES.COLUMNNAME)
    LOOP
      IF(Cur_TranslateDataSelfDep.TABLENAME<>v_tableActual OR v_count>20) THEN
        v_count:=0;
        v_tableActual:=Cur_TranslateDataSelfDep.TABLENAME;
        IF(v_strSql IS NOT NULL) THEN
          v_strSql:=v_strSql || ' WHERE AD_CLIENT_ID = '|| v_NextNo;
          v_seqNo:=v_seqNo + 1;
          INSERT INTO AD_SCRIPT_SQL VALUES(v_seqNo, v_strSql) ;
        END IF;
      END IF;
      v_dynamic_select:='SELECT DISTINCT ' || Cur_TranslateDataSelfDep.COLUMNNAME || ' FROM ' || Cur_TranslateDataSelfDep.TABLENAME || ' WHERE AD_CLIENT_ID = ' || p_AD_Client_ID || ' AND '||Cur_TranslateDataSelfDep.COLUMNNAME||' IS NOT NULL';
  --RAISE NOTICE '%','v_dynamic_select '||v_dynamic_select;
 IF (CUR_v_rc_ISOPEN=true) THEN
  CUR_v_rc_ISOPEN:=false;
  CLOSE v_rc;
 END IF;
 OPEN  v_rc  FOR EXECUTE  v_dynamic_select;
 CUR_v_rc_ISOPEN:=true;
      LOOP
        FETCH v_rc INTO v_oldId;
        EXIT WHEN NOT FOUND;
        v_strSql:='UPDATE '||v_tableActual||' A SET AD_CLIENT_ID = '|| v_NextNo || ' ';
        v_translatedId:=AD_Translate_ID(v_oldId, Cur_TranslateDataSelfDep."OFFSET", Cur_TranslateDataSelfDep.DEPENDS_ON_TABLENAME, Cur_TranslateDataSelfDep.DEPENDS_ON_COLUMNNAME, p_AD_Client_ID) ;
        v_strSql:=v_strSql || ', '||Cur_TranslateDataSelfDep.COLUMNNAME||' = ' || v_translatedId ;
        v_strSql:=v_strSql || ' WHERE AD_CLIENT_ID = '|| v_NextNo || ' AND '||Cur_TranslateDataSelfDep.COLUMNNAME||' = ' || v_oldId;
        v_count:=v_count + 1;
        v_seqNo:=v_seqNo + 1;
        INSERT INTO AD_SCRIPT_SQL VALUES(v_seqNo, v_strSql) ;
        v_strSql:='';
      END LOOP;
    END LOOP;
    v_ResultStr:='Correcciones finales';
    DECLARE
      i RECORD;
    BEGIN
    --Accounting is not copied so any doc should be posted
    for i in (select 'update '||t.tablename||' set posted=''N'' where ad_client_id='||v_NextNo as vsql
                from ad_column c, ad_table t
               where upper(columnname)='POSTED'
                and c.ad_table_id = t.ad_table_id) loop
      v_seqNo:=v_seqNo + 1;
      insert into AD_SCRIPT_SQL VALUES
        (v_seqNo, i.vsql);
    end loop;
    END;
    
    RAISE NOTICE '%',v_ResultStr;
    --Pendiente: actualizar los record_id de la fact_acct...!!!!
    --ACTUALIZACION DE NAMES PARA LAS TABLAS DE LOGIN
    v_seqNo:=v_seqNo + 1;
    INSERT
    INTO AD_SCRIPT_SQL VALUES
      (v_seqNo, 'UPDATE AD_ROLE SET NAME = '''||p_NewClientValue||'_''||NAME WHERE AD_CLIENT_ID = ' ||v_NextNo) ;
    v_seqNo:=v_seqNo + 1;
    INSERT
    INTO AD_SCRIPT_SQL VALUES
      (v_seqNo, 'UPDATE AD_USER SET USERNAME = (CASE WHEN USERNAME IS NULL THEN NULL ELSE '''||p_NewClientValue||'_''||USERNAME END) WHERE AD_CLIENT_ID = ' ||v_NextNo) ;
    v_seqNo:=v_seqNo + 1;
    INSERT
    INTO AD_SCRIPT_SQL VALUES
      (v_seqNo, 'UPDATE AD_ORG SET NAME = '''||p_NewClientValue||'_''||NAME WHERE AD_CLIENT_ID = ' ||v_NextNo) ;
    v_seqNo:=v_seqNo + 1;
    INSERT
    INTO AD_SCRIPT_SQL VALUES
      (v_seqNo, 'UPDATE M_WAREHOUSE SET NAME = '''||p_NewClientValue||'_''||NAME WHERE AD_CLIENT_ID = ' ||v_NextNo) ;
    --AD_ORGINFO; AD_ORG_ID = 0
    v_seqNo:=v_seqNo + 1;
    INSERT
    INTO AD_SCRIPT_SQL VALUES
      (v_seqNo, 'DELETE FROM AD_ORGINFO WHERE AD_ORG_ID = 0 AND AD_CLIENT_ID = ' ||v_NextNo) ;
    --AD_TreeNodes...
    --AD_TreeNodePR
    SELECT max("OFFSET")
    INTO v_offset
    FROM AD_ID_TRANSLATION
    WHERE UPPER(ad_table)='M_PRODUCT' AND AD_CLIENT_OLD=p_AD_Client_ID AND AD_CLIENT_NEW=v_NextNo;
    if(v_offset is not null) then
      v_seqNo:=v_seqNo + 1;
      INSERT
      INTO AD_SCRIPT_SQL VALUES
        (v_seqNo, 'UPDATE AD_TREENODEPR SET '|| 'NODE_ID = AD_Translate_ID(Node_ID,'||v_offset||',''M_PRODUCT'',''M_PRODUCT_ID'',
        '||p_AD_Client_ID||')'|| ', PARENT_ID = AD_Translate_ID(Parent_ID,'||v_offset||',''M_PRODUCT'',''M_PRODUCT_ID'',
        '||p_AD_Client_ID||') '|| 'WHERE AD_CLIENT_ID = ' ||v_NextNo) ;
    end if;
    --AD_TreeNodeBP
    SELECT max("OFFSET")
    INTO v_offset
    FROM AD_ID_TRANSLATION
    WHERE UPPER(ad_table)='C_BPARTNER' AND AD_CLIENT_OLD=p_AD_Client_ID AND AD_CLIENT_NEW=v_NextNo;
    if(v_offset is not null) then
      v_seqNo:=v_seqNo + 1;
      INSERT
      INTO AD_SCRIPT_SQL VALUES
        (v_seqNo, 'UPDATE AD_TREENODEBP SET '|| 'NODE_ID = AD_Translate_ID(Node_ID,'||v_offset||',''C_BPARTNER'',''C_BPARTNER_ID'',
        '||p_AD_Client_ID||')'|| ', PARENT_ID = AD_Translate_ID(Parent_ID,'||v_offset||',''C_BPARTNER'',''C_BPARTNER_ID'',
        '||p_AD_Client_ID||') '|| 'WHERE AD_CLIENT_ID = ' ||v_NextNo) ;
    end if;
    --AD_TreeNode - Organization Tree
    SELECT "OFFSET"
    INTO v_offset2
    FROM AD_ID_TRANSLATION
    WHERE UPPER(ad_table)='AD_TREE' AND AD_CLIENT_OLD=p_AD_Client_ID AND AD_CLIENT_NEW=v_NextNo;
    SELECT max("OFFSET")
    INTO v_offset
    FROM AD_ID_TRANSLATION
    WHERE UPPER(ad_table)='AD_ORG' AND AD_CLIENT_OLD=p_AD_Client_ID AND AD_CLIENT_NEW=v_NextNo;
    if(v_offset is not null) then
      SELECT AD_TREE_ORG_ID
      INTO v_tree_ID
      FROM ad_clientinfo
      WHERE ad_client_id=p_AD_Client_ID;
      v_seqNo:=v_seqNo + 1;
      INSERT
      INTO AD_SCRIPT_SQL VALUES
        (v_seqNo, 'UPDATE AD_TREENODE SET '|| 'NODE_ID = AD_Translate_ID(Node_ID,'||v_offset||',''AD_ORG'',''AD_ORG_ID'',
        '||p_AD_Client_ID||')'|| ', PARENT_ID = AD_Translate_ID(Parent_ID,'||v_offset||',''AD_ORG'',''AD_ORG_ID'',
        '||p_AD_Client_ID||') '|| 'WHERE AD_CLIENT_ID = ' ||v_NextNo|| ' AND AD_TREE_ID = AD_Translate_ID('||v_tree_ID||','||v_offset2||',
        ''AD_TREE'',''AD_TREE_ID'','||p_AD_Client_ID||')') ;

    end if;
    --AD_TreeNode - Project Tree
    SELECT max("OFFSET")
    INTO v_offset
    FROM AD_ID_TRANSLATION
    WHERE UPPER(ad_table)='C_PROJECT' AND AD_CLIENT_OLD=p_AD_Client_ID AND AD_CLIENT_NEW=v_NextNo;
    if(v_offset is not null) then
      SELECT AD_TREE_PROJECT_ID
      INTO v_tree_ID
      FROM ad_clientinfo
      WHERE ad_client_id=p_AD_Client_ID;
      v_seqNo:=v_seqNo + 1;
      INSERT
      INTO AD_SCRIPT_SQL VALUES
        (v_seqNo, 'UPDATE AD_TREENODE SET '|| 'NODE_ID = AD_Translate_ID(Node_ID,'||v_offset||',''C_PROJECT'',''C_PROJECT_ID'',
        '||p_AD_Client_ID||')'|| ', PARENT_ID = AD_Translate_ID(Parent_ID,'||v_offset||',''C_PROJECT'',''C_PROJECT_ID'',
        '||p_AD_Client_ID||') '|| 'WHERE AD_CLIENT_ID = ' ||v_NextNo|| ' AND AD_TREE_ID = AD_Translate_ID('||v_tree_ID||',
        '||v_offset2||',''AD_TREE'',''AD_TREE_ID'','||p_AD_Client_ID||')') ;

    end if;
    --AD_TreeNode - Sales Region
    SELECT max("OFFSET")
    INTO v_offset
    FROM AD_ID_TRANSLATION
    WHERE UPPER(ad_table)='C_SALESREGION' AND AD_CLIENT_OLD=p_AD_Client_ID AND AD_CLIENT_NEW=v_NextNo;
    if(v_offset is not null) then
      SELECT AD_TREE_SALESREGION_ID
      INTO v_tree_ID
      FROM ad_clientinfo
      WHERE ad_client_id=p_AD_Client_ID;
      v_seqNo:=v_seqNo + 1;
      INSERT
      INTO AD_SCRIPT_SQL VALUES
        (v_seqNo, 'UPDATE AD_TREENODE SET '|| 'NODE_ID = AD_Translate_ID(Node_ID,'||v_offset||',''C_SALESREGION'',''C_SALESREGION_ID'',
        '||p_AD_Client_ID||')'|| ', PARENT_ID = AD_Translate_ID(Parent_ID,'||v_offset||',''C_SALESREGION'',''C_SALESREGION_ID'',
        '||p_AD_Client_ID||') '|| 'WHERE AD_CLIENT_ID = ' ||v_NextNo|| ' AND AD_TREE_ID = AD_Translate_ID('||v_tree_ID||','||v_offset2||',
        ''AD_TREE'',''AD_TREE_ID'','||p_AD_Client_ID||')') ;

    end if;
    --AD_TreeNode - Element value (accounting tree)
    SELECT max("OFFSET")
    INTO v_offset
    FROM AD_ID_TRANSLATION
    WHERE UPPER(ad_table)='C_ELEMENTVALUE' AND AD_CLIENT_OLD=p_AD_Client_ID AND AD_CLIENT_NEW=v_NextNo;
    if(v_offset is not null) then
      SELECT AD_TREE_ID
      INTO v_tree_ID
      FROM AD_TREE
      WHERE ad_client_id=p_AD_Client_ID
      AND TREETYPE = 'EV';
      v_seqNo:=v_seqNo + 1;
      INSERT
      INTO AD_SCRIPT_SQL VALUES
        (v_seqNo, 'UPDATE AD_TREENODE SET '|| 'NODE_ID = AD_Translate_ID(Node_ID,'||v_offset||',''C_ELEMENTVALUE'',''C_ELEMENTVALUE_ID'',
        '||p_AD_Client_ID||')'|| ', PARENT_ID = AD_Translate_ID(Parent_ID,'||v_offset||',''C_ELEMENTVALUE'',''C_ELEMENTVALUE_ID'',
        '||p_AD_Client_ID||') '|| 'WHERE AD_CLIENT_ID = ' ||v_NextNo|| ' AND AD_TREE_ID = AD_Translate_ID('||v_tree_ID||','||v_offset2||',
        ''AD_TREE'',''AD_TREE_ID'','||p_AD_Client_ID||')') ;
    end if;
    --AD_Role
    v_seqNo:=v_seqNo + 1;
    INSERT
    INTO AD_SCRIPT_SQL VALUES
      (v_seqNo, 'UPDATE AD_Role SET ClientList = TO_CHAR(AD_Client_ID), OrgList = AD_OrgList(AD_Role_ID) '|| 'WHERE AD_Client_ID = '||v_NextNo) ;    v_ResultStr:='Enabling  constraints';
    RAISE NOTICE '%',v_ResultStr;
    v_seqNo:= ad_script_enable_constraints(v_seqNo);

    v_ResultStr:='Enabling triggers';
    RAISE NOTICE '%',v_ResultStr;
    v_seqNo:= ad_script_enable_triggers(v_seqNo);

    -- Update sequence values
    v_ResultStr:='Updating sequences';
    RAISE NOTICE '%', v_ResultStr;
    PERFORM Ad_Update_Sequence_Generate() ;

 v_ResultStr := ad_script_execute();
 v_Result := 1;

  ----<<END_PROCESS>>
 IF (p_PInstance_ID IS NOT NULL) THEN
  --  Update AD_PInstance
  RAISE NOTICE '%','Updating PInstance - Finished ' || v_ResultStr;
     PERFORM Ad_Update_PInstance(p_PInstance_ID, NULL, 'N', v_Result, v_ResultStr);
 ELSE
  RAISE NOTICE '%','Finished ' || v_ResultStr;
 END IF;
 
    RETURN;

END; --BODY
EXCEPTION
    WHEN  OTHERS THEN
  v_ResultStr := v_ResultStr || ': ' || SQLERRM;
  RAISE NOTICE '%',v_ResultStr;
  
  IF (p_PInstance_ID IS NOT NULL) THEN
      PERFORM Ad_Update_PInstance(p_PInstance_ID, NULL, 'N', 0, v_ResultStr);
  END IF;
    RETURN;

END;   $BODY$
  LANGUAGE 'plpgsql' VOLATILE
/-- END



