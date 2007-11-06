CREATE TRUSTED PROCEDURAL LANGUAGE 'plpgsql'
  HANDLER plpgsql_call_handler
  VALIDATOR plpgsql_validator
/-- END

CREATE OR REPLACE FUNCTION dateFormat
(
)
RETURNS VARCHAR AS ' 
BEGIN
RETURN ''DD-MM-YYYY'';
EXCEPTION 
  WHEN OTHERS THEN 
    RETURN NULL;
END;
' LANGUAGE 'plpgsql'
/-- END

CREATE OR REPLACE FUNCTION to_number
(
text
)
RETURNS NUMERIC AS '
BEGIN
RETURN to_number($1, ''S99999999999999D999999'');
EXCEPTION 
  WHEN OTHERS THEN 
    RETURN NULL;
END;
' LANGUAGE 'plpgsql'
/-- END

CREATE OR REPLACE FUNCTION to_date
(
text
)
RETURNS timestamp AS '
BEGIN
RETURN to_timestamp($1, dateFormat());
END;
' LANGUAGE 'plpgsql'
/-- END

CREATE OR REPLACE FUNCTION to_date
(
 timestamptz
)
  RETURNS timestamp AS '
BEGIN
RETURN to_timestamp(to_char($1, dateFormat()), dateFormat());
END;
' LANGUAGE 'plpgsql'
/-- END

CREATE OR REPLACE FUNCTION to_timestamp
(
 timestamptz
)
  RETURNS timestamp AS '
BEGIN
RETURN to_timestamp(to_char($1, dateFormat()), dateFormat());
END;
' LANGUAGE 'plpgsql'
/-- END

CREATE OR REPLACE FUNCTION to_char
(
integer
)
RETURNS  VARCHAR AS '
BEGIN
RETURN to_char($1, ''999999999999D'');
END;
' LANGUAGE 'plpgsql'
/-- END

CREATE OR REPLACE FUNCTION to_char
(
timestamp
)
RETURNS  VARCHAR AS '
BEGIN
RETURN to_char($1, dateFormat());
END;
' LANGUAGE 'plpgsql'
/-- END

CREATE OR REPLACE FUNCTION to_char
(
VARCHAR
)
RETURNS  VARCHAR AS '
BEGIN
RETURN $1;
END;
' LANGUAGE 'plpgsql'
/-- END

CREATE OR REPLACE FUNCTION to_char
(
NUMERIC
)
RETURNS  VARCHAR AS '
BEGIN
RETURN ''''||$1;
END;
' LANGUAGE 'plpgsql'
/-- END

CREATE OR REPLACE FUNCTION to_char
(
text
)
RETURNS  VARCHAR AS '
BEGIN
RETURN $1;
END;
' LANGUAGE 'plpgsql'
/-- END

CREATE OR REPLACE FUNCTION round
(
numeric,
numeric
)
RETURNS  NUMERIC AS '
BEGIN
RETURN round($1,CAST($2 AS INTEGER));
END;
' LANGUAGE 'plpgsql'
/-- END

CREATE OR REPLACE FUNCTION rpad
(
varchar,
numeric,
varchar
)
RETURNS VARCHAR AS '
BEGIN
return to_char(rpad($1::text,CAST($2 AS INTEGER), $3::text));
END;
' LANGUAGE 'plpgsql'
/-- END

CREATE OR REPLACE FUNCTION substr
(
varchar,
numeric,
numeric
)
RETURNS VARCHAR AS '
BEGIN
return substr($1,CAST((CASE $2 WHEN 0 THEN 1 ELSE $2 END) AS INTEGER), CAST($3 AS INTEGER));
END;
' LANGUAGE 'plpgsql'
/-- END

create or replace function to_interval (
       integer,
       varchar
)
returns interval 
as '	
declare    
       interval__number	     alias for $1;
       interval__units	     alias for $2;
begin

	-- We should probably do unit checking at some point
	return ('''''''' || interval__number || '' '' || interval__units || '''''''')::interval;

END;
' language 'plpgsql'
/-- END

create or replace function add_months (
       timestamptz, 
       integer
)
returns timestamptz as '
begin
	return $1 + to_interval($2,to_char(''months''));

END;
' language 'plpgsql'
/-- END


CREATE OR REPLACE FUNCTION add_months
(
date,
numeric
)
RETURNS timestamptz AS '
BEGIN
	return $1 + to_interval($2::INTEGER,to_char(''months''));
END;
' LANGUAGE 'plpgsql'
/-- END

CREATE OR REPLACE FUNCTION add_months
(
timestamp,
integer
)
RETURNS timestamptz AS '
BEGIN
	return $1 + to_interval($2,to_char(''months''));
END;
' LANGUAGE 'plpgsql'
/-- END

CREATE OR REPLACE FUNCTION add_months
(
timestamp,
numeric
)
RETURNS timestamptz AS '
BEGIN
	return $1 + to_interval($2::INTEGER,to_char(''months''));
END;
' LANGUAGE 'plpgsql'
/-- END

CREATE OR REPLACE FUNCTION add_days
(
timestamp with time zone,
INTEGER
)
RETURNS DATE AS '
BEGIN
RETURN cast($1 AS date) + $2 ;
END;
' LANGUAGE 'plpgsql'
/-- END

CREATE OR REPLACE FUNCTION add_days
(
date,
NUMERIC
)
RETURNS DATE AS '
BEGIN
RETURN $1 + cast($2 AS INTEGER) ;
END;
' LANGUAGE 'plpgsql'
/-- END

CREATE OR REPLACE FUNCTION add_days(timestamptz, numeric)
  RETURNS date 
AS '
BEGIN
RETURN cast($1 AS date) + cast($2 AS integer) ;
END;
'  LANGUAGE 'plpgsql'
/-- END

--DROP OPERATOR + (timestamptz, numeric) CASCADE/-- END
CREATE OPERATOR +(
  PROCEDURE = "add_days",
  LEFTARG = timestamptz,
  RIGHTARG = numeric,
  COMMUTATOR = +)
/-- END

--DROP OPERATOR + (timestamptz, integer)/-- END
CREATE OPERATOR + (
   LEFTARG = timestamp with time zone,
   RIGHTARG = integer,
   PROCEDURE = add_days,
   COMMUTATOR = +
)
/-- END

--DROP OPERATOR + (date, numeric)/-- END
CREATE OPERATOR + (
   LEFTARG = date,
   RIGHTARG = numeric,
   PROCEDURE = add_days,
   COMMUTATOR = +
)
/-- END

CREATE OR REPLACE FUNCTION substract_days(timestamptz, numeric)
  RETURNS date 
AS '
BEGIN
RETURN cast($1 AS date) - cast($2 AS int4) ;
END;
' LANGUAGE 'plpgsql'
/-- END

CREATE OR REPLACE FUNCTION substract_days
(
timestamp with time zone,
INTEGER
)
RETURNS DATE AS '
BEGIN
RETURN cast($1 AS date) - $2 ;
END;
' LANGUAGE 'plpgsql'
/-- END

CREATE OR REPLACE FUNCTION substract_days
(
date,
NUMERIC
)
RETURNS DATE AS '
BEGIN
RETURN $1 - cast($2 AS INTEGER) ;
END;
' LANGUAGE 'plpgsql'
/-- END

--DROP OPERATOR - (timestamptz, integer)/-- END
CREATE OPERATOR - (
   LEFTARG = timestamp with time zone,
   RIGHTARG = integer,
   PROCEDURE = substract_days
)
/-- END

--DROP OPERATOR - (date, numeric)/-- END
CREATE OPERATOR - (
   LEFTARG = date,
   RIGHTARG = numeric,
   PROCEDURE = substract_days
)
/-- END


CREATE OR REPLACE FUNCTION negation(boolean)
  RETURNS boolean AS
'
BEGIN
RETURN NOT $1 ;
END;
' LANGUAGE 'plpgsql'
/-- END

--DROP OPERATOR ! (NONE, boolean)/-- END
CREATE OPERATOR !(
  PROCEDURE = "negation",
  RIGHTARG = boolean)
/-- END


CREATE OR REPLACE FUNCTION trunc
(
date,
varchar
)
RETURNS DATE AS '
DECLARE 
  p_transformation VARCHAR;
BEGIN
IF UPPER($2) = ''MM'' THEN
  p_transformation := ''month'';
ELSIF UPPER($2) = ''DD'' THEN
  p_transformation := ''day'';
ELSIF UPPER($2) = ''Q'' THEN
  p_transformation := ''quarter'';
ELSE 
  p_transformation := $2;
END IF;
RETURN date_trunc( p_transformation , $1) ;
END;
' LANGUAGE 'plpgsql'
/-- END

CREATE OR REPLACE FUNCTION trunc
(
timestamp with time zone,
varchar
)
RETURNS DATE AS '
DECLARE 
  p_transformation VARCHAR;
BEGIN
IF UPPER($2) = ''MM'' THEN
  p_transformation := ''month'';
ELSIF UPPER($2) = ''DD'' THEN
  p_transformation := ''day'';
ELSE 
  p_transformation := $2;
END IF;
RETURN date_trunc( p_transformation , $1) ;
END;
' LANGUAGE 'plpgsql'
/-- END

CREATE OR REPLACE FUNCTION trunc
(
timestamp
)
RETURNS DATE AS '
BEGIN
RETURN to_timestamp(to_char($1, dateFormat()), dateFormat());
END;
' LANGUAGE 'plpgsql'
/-- END

CREATE OR REPLACE FUNCTION trunc
(
timestamp with time zone
)
RETURNS DATE AS '
BEGIN
RETURN to_timestamp(to_char($1, dateFormat()), dateFormat());
END;
' LANGUAGE 'plpgsql'
/-- END

CREATE OR REPLACE FUNCTION instr(varchar, varchar)
  RETURNS int4 AS 
'DECLARE
    pos integer;
BEGIN
    pos:= instr($1, $2, 1);
    RETURN pos;
END;
' LANGUAGE 'plpgsql'
/-- END

CREATE OR REPLACE FUNCTION instr(string varchar, string_to_search varchar, beg_index int4)
  RETURNS int4 AS 
'DECLARE
    pos integer NOT NULL DEFAULT 0;
    temp_str varchar;
    beg integer;
    length integer;
    ss_length integer;
BEGIN
    IF ((string IS NULL) OR (string_to_search IS NULL) OR (beg_index IS NULL)) THEN RETURN 0; END IF;
    IF beg_index > 0 THEN
      temp_str := substring(string FROM beg_index);
      pos := position(string_to_search IN temp_str);
      IF pos = 0 THEN
        RETURN 0;
      ELSE
        RETURN pos + beg_index - 1;
      END IF;
    ELSE
      ss_length := char_length(string_to_search);
      length := char_length(string);
      beg := length + beg_index - ss_length + 2;
      WHILE beg > 0 LOOP
        temp_str := substring(string FROM beg FOR ss_length);
        pos := position(string_to_search IN temp_str);
        IF pos > 0 THEN
          RETURN beg;
        END IF;
        beg := beg - 1;
      END LOOP;
      RETURN 0;
    END IF;
END;
' LANGUAGE 'plpgsql'
/-- END

CREATE OR REPLACE FUNCTION instr(string varchar, string_to_search varchar, beg_index int4, occur_index int4)
  RETURNS int4 AS 
'DECLARE
pos integer NOT NULL DEFAULT 0;
occur_number integer NOT NULL DEFAULT 0;
temp_str varchar;
beg integer;
i integer;
length integer;
ss_length integer; BEGIN
    IF ((string IS NULL) OR (string_to_search IS NULL) OR (beg_index IS NULL) OR (occur_index IS NULL)) THEN RETURN 0; END IF;
IF beg_index > 0 THEN
    beg := beg_index;
    temp_str := substring(string FROM beg_index);

    FOR i IN 1..occur_index LOOP
        pos := position(string_to_search IN temp_str);
         IF i = 1 THEN
            beg := beg + pos - 1;
        ELSE
            beg := beg + pos;
        END IF;
         temp_str := substring(string FROM beg + 1);
    END LOOP;          
    IF pos = 0 THEN
        RETURN 0;
    ELSE
        RETURN beg;
    END IF;
ELSE
    ss_length := char_length(string_to_search);
    length := char_length(string);
    beg := length + beg_index - ss_length + 2;
     WHILE beg > 0 LOOP
        temp_str := substring(string FROM beg FOR ss_length);
        pos := position(string_to_search IN temp_str);
         IF pos > 0 THEN
            occur_number := occur_number + 1;
             IF occur_number = occur_index THEN
                RETURN beg;
            END IF;
        END IF;
         beg := beg - 1;
    END LOOP;
     RETURN 0;
END IF; 
END;
' LANGUAGE 'plpgsql'
/-- END

create or replace function last_day(date) returns date as 'select
cast(date_trunc(''month'', $1) + ''1 month''::interval as date) - 1'
language sql
/-- END

create or replace function last_day(timestamptz) returns date as 'select
cast(date_trunc(''month'', cast($1 AS date)) + ''1 month''::interval as date) - 1'
language sql
/-- END


CREATE OR REPLACE FUNCTION DUMP(varchar)
  RETURNS varchar AS 
'DECLARE
BEGIN
    RETURN $1;
END;
'
  LANGUAGE 'plpgsql'
/-- END

CREATE OR REPLACE FUNCTION DUMP(NUMERIC)
  RETURNS NUMERIC AS 
'DECLARE
BEGIN
    RETURN $1;
END;
'
  LANGUAGE 'plpgsql'
/-- END

CREATE OR REPLACE FUNCTION substract_days
(
timestamp with time zone,
timestamp with time zone
)
RETURNS float AS '
BEGIN
RETURN extract(epoch from ($1 - $2)) / 86400.0::float;
END;
' LANGUAGE 'plpgsql'
/-- END

CREATE OR REPLACE FUNCTION substract_days
(
timestamp,
timestamp
)
RETURNS float AS '
BEGIN
RETURN extract(epoch from ($1 - $2)) / 86400.0::float;
END;
' LANGUAGE 'plpgsql'
/-- END

--DROP OPERATOR - (timestamp, timestamp)/-- END
--CREATE OPERATOR - (
--   LEFTARG = timestamp,
--   RIGHTARG = timestamp,
--   PROCEDURE = substract_days
--)
/-- END

--DROP OPERATOR - (timestamptz,timestamptz);
--CREATE OPERATOR - (
--  LEFTARG = timestamptz,
--   RIGHTARG = timestamptz,
--   PROCEDURE = substract_days
--)
/-- END

--DROP OPERATOR - (timestamptz, numeric)/-- END
CREATE OPERATOR -(
  PROCEDURE = substract_days,
  LEFTARG = timestamptz,
  RIGHTARG = numeric)
/-- END

CREATE OR REPLACE VIEW DUAL AS SELECT 'X' AS dummy
/-- END


CREATE OR REPLACE VIEW USER_TABLES
(TABLE_NAME, BLOCKS, DURATION, LAST_ANALYZED)
AS 
SELECT UPPER(TABLENAME), NULL, NULL, NULL 
FROM PG_TABLES 
WHERE SCHEMANAME = CURRENT_SCHEMA()
/-- END

--DROP VIEW USER_CONSTRAINTS/-- END

CREATE OR REPLACE VIEW user_constraints AS 
 SELECT upper(pg_class.relname) AS table_name, 
        upper(pg_constraint.conname) AS constraint_name, 
        CASE upper(pg_constraint.contype)
            WHEN 'F' THEN 'R'
            ELSE upper(pg_constraint.contype)
        END AS constraint_type, upper(pg_constraint.confdeltype) AS delete_rule, 
        array_to_string(ARRAY( SELECT '"'||pg_attribute.attname||'"' as attr_name
                               FROM pg_attribute
                               WHERE pg_attribute.attrelid = pg_constraint.conrelid  AND pg_attribute.attnum = ANY(pg_constraint.conkey)
                               ORDER BY position( pg_attribute.attnum in array_to_string(pg_constraint.conkey,'-'))),
                         ',') AS column_names,
        upper(fk_table.relname) AS fk_table,
        array_to_string(ARRAY( SELECT '"'||pg_attribute.attname||'"' as attr_name
                               FROM pg_attribute
                               WHERE pg_attribute.attrelid = pg_constraint.confrelid AND pg_attribute.attnum = ANY(pg_constraint.confkey)
                               ORDER BY position( pg_attribute.attnum in array_to_string(pg_constraint.confkey,'-'))),
                         ',') AS fk_column_names, pg_constraint.confmatchtype AS fk_matchtype,
        CASE upper(pg_constraint.contype)
            WHEN 'P' THEN upper(pg_constraint.conname)
            WHEN 'U' THEN upper(pg_constraint.conname)
            ELSE ''
        END AS index_name, 
        pg_constraint.consrc AS search_condition
 FROM pg_constraint
 INNER JOIN pg_class ON  pg_class.oid = pg_constraint.conrelid 
 LEFT JOIN pg_class fk_table ON fk_table.oid = pg_constraint.confrelid
/-- END

--DROP VIEW USER_INDEXES/-- END

CREATE OR REPLACE VIEW USER_INDEXES
(TABLE_NAME, INDEX_NAME, TABLESPACE_NAME, UNIQUENESS, INDEX_TYPE, TABLE_TYPE)
AS 
SELECT UPPER(PG_CLASS1.RELNAME), UPPER(PG_CLASS.RELNAME), UPPER(PG_NAMESPACE.NSPNAME), CASE PG_INDEX.indisunique WHEN true THEN 'UNIQUE' ELSE 'NONUNIQUE' END, 
TO_CHAR('NORMAL'), TO_CHAR('TABLE') 
FROM PG_INDEX, PG_CLASS, PG_CLASS PG_CLASS1, PG_NAMESPACE
WHERE PG_INDEX.indexrelid = PG_CLASS.OID
AND PG_INDEX.indrelid = PG_CLASS1.OID
AND PG_CLASS.RELNAMESPACE = PG_NAMESPACE.OID
AND PG_CLASS1.RELNAMESPACE = PG_NAMESPACE.OID
AND PG_NAMESPACE.NSPNAME = CURRENT_SCHEMA()
/-- END

--DROP VIEW USER_IND_COLUMNS/-- END

CREATE OR REPLACE VIEW USER_IND_COLUMNS
(TABLE_NAME, INDEX_NAME, COLUMN_NAME, TABLESPACE_NAME, COLUMN_POSITION)
AS 
SELECT UPPER(PG_CLASS1.RELNAME), UPPER(PG_CLASS.RELNAME), UPPER(PG_ATTRIBUTE.ATTNAME), UPPER(PG_NAMESPACE.NSPNAME), PG_ATTRIBUTE.ATTNUM
FROM PG_INDEX, PG_CLASS, PG_CLASS PG_CLASS1, PG_NAMESPACE, PG_ATTRIBUTE
WHERE PG_INDEX.INDEXRELID = PG_CLASS.OID
AND PG_INDEX.INDRELID = PG_CLASS1.OID
AND PG_ATTRIBUTE.ATTRELID = PG_INDEX.INDRELID 
AND PG_ATTRIBUTE.ATTNUM IN (PG_INDEX.INDKEY[0], PG_INDEX.INDKEY[1], PG_INDEX.INDKEY[2] ,PG_INDEX.INDKEY[3], PG_INDEX.INDKEY[4] ,PG_INDEX.INDKEY[5],PG_INDEX.INDKEY[6], PG_INDEX.INDKEY[7], PG_INDEX.INDKEY[8] ,PG_INDEX.INDKEY[9], PG_INDEX.INDKEY[10] ,PG_INDEX.INDKEY[11])
AND PG_CLASS.RELNAMESPACE = PG_NAMESPACE.OID
AND PG_CLASS1.RELNAMESPACE = PG_NAMESPACE.OID
AND PG_NAMESPACE.NSPNAME = CURRENT_SCHEMA()
/-- END


CREATE OR REPLACE VIEW USER_OBJECTS
(TABLE_NAME, OBJECT_NAME, OBJECT_ID, OBJECT_TYPE, STATUS)
AS 
SELECT UPPER(PG_CLASS.RELNAME), UPPER(PG_TRIGGER.TGNAME), PG_TRIGGER.TGFOID, TO_CHAR('TRIGGER'), CASE PG_TRIGGER.TGENABLED WHEN TRUE THEN TO_CHAR('VALID') ELSE TO_CHAR('INVALID') END
FROM PG_TRIGGER, PG_NAMESPACE, PG_CLASS 
WHERE PG_TRIGGER.TGRELID = PG_CLASS.OID
AND PG_TRIGGER.TGISCONSTRAINT = FALSE
AND PG_CLASS.RELNAMESPACE = PG_NAMESPACE.OID
AND PG_NAMESPACE.NSPNAME = CURRENT_SCHEMA()
/-- END


CREATE OR REPLACE VIEW USER_TAB_COLUMNS
(TABLE_NAME, COLUMN_NAME, DATA_TYPE, 
DATA_LENGTH, DATA_PRECISION, DATA_SCALE, DATA_DEFAULT, NULLABLE, COLUMN_ID)
AS 
SELECT UPPER(PG_CLASS.RELNAME), UPPER(PG_ATTRIBUTE.ATTNAME),UPPER(PG_TYPE.TYPNAME), 
CASE PG_ATTRIBUTE.ATTLEN WHEN -1 THEN PG_ATTRIBUTE.ATTTYPMOD-4 ELSE PG_ATTRIBUTE.ATTLEN END,10, 0, 
CASE PG_ATTRIBUTE.ATTHASDEF WHEN TRUE THEN (SELECT PG_ATTRDEF.ADSRC FROM
PG_ATTRDEF WHERE PG_ATTRDEF.ADRELID = PG_CLASS.OID AND PG_ATTRDEF.ADNUM = 
PG_ATTRIBUTE.ATTNUM) ELSE NULL END, PG_ATTRIBUTE.ATTNOTNULL, PG_ATTRIBUTE.ATTNUM 
FROM PG_CLASS, PG_NAMESPACE, PG_ATTRIBUTE, PG_TYPE
WHERE PG_ATTRIBUTE.ATTRELID = PG_CLASS.OID
AND PG_ATTRIBUTE.ATTTYPID = PG_TYPE.OID 
AND PG_CLASS.RELNAMESPACE = PG_NAMESPACE.OID
AND PG_NAMESPACE.NSPNAME = CURRENT_SCHEMA()
AND PG_ATTRIBUTE.ATTNUM>0
/-- END

--DROP VIEW USER_TRIGGERS/-- END

CREATE OR REPLACE VIEW USER_TRIGGERS
(TABLE_NAME, TABLESPACE_NAME, TRIGGER_NAME)
AS 
SELECT UPPER(PG_CLASS.RELNAME), UPPER(PG_NAMESPACE.NSPNAME), PG_TRIGGER.TGNAME
FROM PG_TRIGGER, PG_CLASS, PG_NAMESPACE
WHERE PG_TRIGGER.tgrelid = PG_CLASS.OID
AND PG_CLASS.RELNAMESPACE = PG_NAMESPACE.OID
AND PG_NAMESPACE.NSPNAME = CURRENT_SCHEMA()
/-- END

CREATE OR REPLACE FUNCTION C_DateDayInMonth
(
 i_Day  IN NUMERIC,
 i_Date  IN  TIMESTAMPTZ
)
RETURNS TIMESTAMP AS ' DECLARE
BEGIN 
 RETURN C_DateDayInMonth($1, TO_DATE($2));
END;
   ' LANGUAGE 'plpgsql'
/-- END


