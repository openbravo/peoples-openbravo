-- ----------------------------------------------------------------------- 
-- TABLE PLAN_TABLE 
-- ----------------------------------------------------------------------- 
CREATE TABLE PLAN_TABLE
(
    STATEMENT_ID VARCHAR2(30),
    TIMESTAMP DATE,
    REMARKS VARCHAR2(80),
    OPERATION VARCHAR2(30),
    OPTIONS VARCHAR2(255),
    OBJECT_NODE VARCHAR2(128),
    OBJECT_OWNER VARCHAR2(30),
    OBJECT_NAME VARCHAR2(30),
    OBJECT_INSTANCE INTEGER,
    OBJECT_TYPE VARCHAR2(30),
    OPTIMIZER VARCHAR2(255),
    SEARCH_COLUMNS NUMBER,
    ID INTEGER,
    PARENT_ID INTEGER,
    POSITION INTEGER,
    COST INTEGER,
    CARDINALITY INTEGER,
    BYTES INTEGER,
    OTHER_TAG VARCHAR2(255),
    PARTITION_START VARCHAR2(255),
    PARTITION_STOP VARCHAR2(255),
    PARTITION_ID INTEGER,
    OTHER CLOB,
    DISTRIBUTION VARCHAR2(30),
    CPU_COST INTEGER,
    IO_COST INTEGER,
    TEMP_SPACE INTEGER,
    ACCESS_PREDICATES VARCHAR2(4000),
    FILTER_PREDICATES VARCHAR2(4000)
)
/-- END PLAN_TABLE 


CREATE OR REPLACE FUNCTION ad_script_disable_triggers (p_seqNoStart NUMBER)
  RETURN NUMBER
AS
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
 v_seqNo      NUMBER := p_seqNoStart;
 TYPE RECORD IS REF CURSOR;
 Cur_Constraints RECORD;
BEGIN
    FOR Cur_Constraints IN 
      (SELECT TABLE_NAME, CONSTRAINT_NAME 
    FROM USER_CONSTRAINTS C1 
    WHERE CONSTRAINT_TYPE IN('P', 'U', 'R') AND DELETE_RULE NOT LIKE 'CASCADE' 
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
 v_seqNo      NUMBER := p_seqNoStart;
 TYPE RECORD IS REF CURSOR;
 Cur_Constraints RECORD;
BEGIN
    FOR Cur_ConstraintsEnable IN 
      (SELECT TABLE_NAME, CONSTRAINT_NAME 
    FROM USER_CONSTRAINTS C1 
    WHERE CONSTRAINT_TYPE IN('P', 'U', 'R') AND DELETE_RULE NOT LIKE 'CASCADE' 
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


CALL DBA_RECOMPILE(NULL)
/-- END

