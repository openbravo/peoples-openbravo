-- Creating auxiliar functions for language creation
CREATE OR REPLACE FUNCTION exist_language(varchar)
RETURNS bigint AS ' 
  SELECT count(*) from pg_language where lanname = $1;
' LANGUAGE 'sql'
/-- END

CREATE OR REPLACE FUNCTION insert_pg_language()
RETURNS integer AS ' 
  CREATE TRUSTED PROCEDURAL LANGUAGE ''plpgsql''
  HANDLER plpgsql_call_handler
  VALIDATOR plpgsql_validator;
  SELECT 1;
' LANGUAGE 'sql'
/-- END

CREATE OR REPLACE FUNCTION create_language(varchar)
RETURNS integer AS '
SELECT
CASE WHEN exist_language($1)=0
THEN insert_pg_language()
END;
SELECT 1;
' LANGUAGE 'sql'
/-- END

SELECT * FROM create_language('plpgsql')
/-- END

--CREATE TRUSTED PROCEDURAL LANGUAGE 'plpgsql'
--  HANDLER plpgsql_call_handler
--  VALIDATOR plpgsql_validator;
--/--END

CREATE OR REPLACE FUNCTION dba_getattnumpos(conkey _int4, attnum int4)
  RETURNS int4 AS
$BODY$
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
  DECLARE i integer;
begin
  for i in 1..array_upper(conkey,1)
  loop     
     IF (conkey[i] = attnum) THEN
	RETURN i;
     END IF;
  end loop;  
  return 0;
end;
$BODY$
  LANGUAGE 'plpgsql' VOLATILE
/-- END


CREATE OR REPLACE FUNCTION dba_getstandard_search_text(text)
  RETURNS text AS
$BODY$
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
  DECLARE  v_text TEXT;
  DECLARE  v_p1 NUMERIC;
  DECLARE  v_p2 NUMERIC;
  DECLARE  v_p3 NUMERIC;
  DECLARE  v_i NUMERIC;
begin
  v_text := replace($1, ' = ANY',' in');
  v_text := replace(v_text, 'ARRAY[', '');
  v_text := replace(v_text, ']' , '');
  v_text := replace(v_text, '::bpchar', '');
  v_text := replace(v_text, '::text', '');
  v_text := substring(v_text,2,length(v_text)-2);

    WHILE (v_text LIKE '%::%') LOOP
      v_p1 := INSTR(v_text, '::');
      v_p2 := 0;
      v_p3 := 0;
      IF (SUBSTR(v_text,v_p1-1,1) = ')') THEN
        v_i := 1;
        WHILE (v_p2=0) LOOP
          v_i := v_i + 1;
          IF (SUBSTR(v_text, v_p1 - v_i, 1) = '(') THEN
            v_p2 := v_p1 - v_i;
          END IF;
        END LOOP;
      END IF;
      v_i := 1;
      WHILE (v_p3=0) LOOP
        v_i := v_i + 1;
        IF ((SUBSTR(v_text, v_p1 + v_i, 1) IN (' ',')',CHR(10),CHR(13)) OR (v_p1 + v_i>= length(v_text))))  THEN
          v_p3 := v_p1 + v_i;
        END IF;
      END LOOP;
      --RAISE_APPLICATION_ERROR(-20001,'v_p3 = '||v_p3);
      IF (v_p2 = 0) THEN
        v_text := SUBSTR(v_text, 1, v_p1-1) || SUBSTR(v_text, v_p3, 4000);
      ELSE
        v_text := SUBSTR(v_text, 1, v_p2-1) || SUBSTR(v_text, v_p2 + 1, v_p1 - (v_p2 + 2)) || SUBSTR(v_text, v_p3, 4000);
      END IF;
    END LOOP;

    RETURN upper(v_text);
END;
$BODY$
  LANGUAGE 'plpgsql' VOLATILE
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
 
CREATE OR REPLACE FUNCTION to_number
(
 interval
)
RETURNS NUMERIC AS '
BEGIN
RETURN extract(epoch FROM ($1))/86400;
EXCEPTION 
  WHEN OTHERS THEN 
    RETURN NULL;
END;
' LANGUAGE 'plpgsql'
/-- END

CREATE OR REPLACE FUNCTION to_number(integer)
  RETURNS "numeric" AS
$BODY$
BEGIN
RETURN to_number($1, 'S99999999999999D999999');
EXCEPTION 
  WHEN OTHERS THEN 
    RETURN NULL;
END;
$BODY$
  LANGUAGE 'plpgsql' VOLATILE;
/-- END

CREATE OR REPLACE FUNCTION to_number(bigint)
  RETURNS "numeric" AS
$BODY$
BEGIN
RETURN cast($1 as numeric);
END;
$BODY$
  LANGUAGE 'plpgsql' VOLATILE;
/-- END


CREATE OR REPLACE FUNCTION to_number(numeric)
  RETURNS "numeric" AS
$BODY$
BEGIN
RETURN $1;
EXCEPTION 
  WHEN OTHERS THEN 
    RETURN NULL;
END;
$BODY$
  LANGUAGE 'plpgsql' VOLATILE;
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
  RETURN to_timestamp(to_char($1, dateFormat()||'' HH24:MI:SS''), dateFormat()||'' HH24:MI:SS'');
END;
' LANGUAGE 'plpgsql'
/-- END

CREATE OR REPLACE FUNCTION to_date
(
timestamp, varchar
)
RETURNS timestamp AS '
BEGIN
RETURN to_timestamp($1, $2);
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
bigint
)
RETURNS  VARCHAR AS '
BEGIN
RETURN cast($1 as VARCHAR);
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
       interval__number      alias for $1;
       interval__units       alias for $2;
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

CREATE OR REPLACE FUNCTION months_between (init_date timestamptz, end_date timestamptz) 
RETURNS integer 
AS '
DECLARE
  v_months INTEGER;
  v_months_total INTEGER;
  v_years INTEGER;
begin
  v_months=extract(month from (age(init_date,end_date)));
  v_years=extract(year from (age(init_date,end_date)));

  v_months_total=abs((v_years*12) + v_months);
  return v_months_total;
end
'  LANGUAGE 'plpgsql';
/-- END

CREATE OR REPLACE FUNCTION type_oid(varchar)
RETURNS oid AS ' 
  SELECT pg_type.oid from pg_type WHERE pg_type.typname = $1;
' LANGUAGE 'sql'
/-- END

-- Creating auxiliar functions for operator dropping
CREATE or REPLACE function drop_operator (operator_name IN varchar,param1 IN varchar,param2 IN varchar) returns varchar as '
DECLARE
  cnt int4;
BEGIN
  SELECT into cnt count(*) from pg_operator where upper(oprname) = upper(operator_name::name) and oprleft = type_oid(param1) and oprright = type_oid(param2);
  if cnt > 0 then
    execute ''DROP OPERATOR '' || operator_name || ''('' || param1 || '','' || param2 || '') CASCADE;'';
    return operator_name || '' DROPPED'';
  end if;
  return operator_name || '' does not exist'';
END;'
language 'plpgsql' 
/-- END

--DROP OPERATOR + (timestamptz, numeric) CASCADE;
SELECT * FROM drop_operator('+'::varchar,'timestamptz'::varchar,'numeric'::varchar)
/-- END

CREATE OPERATOR +(
  PROCEDURE = "add_days",
  LEFTARG = timestamptz,
  RIGHTARG = numeric,
  COMMUTATOR = +)
/-- END

--DROP OPERATOR + (timestamptz, integer);
--SELECT * FROM drop_operator('+'::varchar,'timestamptz'::varchar,'integer'::varchar);
--CREATE OPERATOR + (
--   LEFTARG = timestamptz,
--   RIGHTARG = integer,
--   PROCEDURE = add_days,
--   COMMUTATOR = +
--)
--/--END

--DROP OPERATOR + (date, numeric);
SELECT * FROM drop_operator('+'::varchar,'date'::varchar,'numeric'::varchar)
/-- END

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
--SELECT * FROM drop_operator('-'::varchar,'timestamptz'::varchar,'integer'::varchar);
--CREATE OPERATOR - (
--   LEFTARG = timestamp with time zone,
--   RIGHTARG = integer,
--   PROCEDURE = substract_days
--)
--/-- END

--DROP OPERATOR - (date, numeric);
SELECT * FROM drop_operator('-'::varchar,'date'::varchar,'numeric'::varchar)
/-- END

CREATE OPERATOR - (
   LEFTARG = date,
   RIGHTARG = numeric,
   PROCEDURE = substract_days
)
/-- END


CREATE OR REPLACE FUNCTION negation(boolean)
  RETURNS boolean AS '
BEGIN
RETURN NOT $1 ;
END;
' LANGUAGE 'plpgsql'
/-- END

--DROP OPERATOR ! (NONE, boolean);
--SELECT * FROM drop_operator('!'::varchar,'NONE'::varchar,'boolean'::varchar);
--CREATE OPERATOR !(
--  PROCEDURE = "negation",
--  RIGHTARG = boolean)
--/-- END

CREATE OR REPLACE FUNCTION equal(numeric, varchar)
  RETURNS boolean AS '
BEGIN
RETURN $1 = TO_NUMBER($2);
END;
' LANGUAGE 'plpgsql'
/-- END

--DROP OPERATOR = (numeric, varchar);
SELECT * FROM drop_operator('='::varchar,'numeric'::varchar,'varchar'::varchar);
/-- END

CREATE OPERATOR =(
  PROCEDURE = "equal",
  LEFTARG = numeric,
  RIGHTARG = varchar)
--/-- END

CREATE OR REPLACE FUNCTION lowerequalnumeric(numeric, varchar)
  RETURNS boolean AS '
BEGIN
RETURN $1 <= TO_NUMBER($2);
END;
' LANGUAGE 'plpgsql'
/-- END

--DROP OPERATOR <= (numeric, varchar);
SELECT * FROM drop_operator('<='::varchar,'numeric'::varchar,'varchar'::varchar);
/-- END

CREATE OPERATOR <=(
  PROCEDURE = "lowerequalnumeric",
  LEFTARG = numeric,
  RIGHTARG = varchar)
/-- END

CREATE OR REPLACE FUNCTION lowerequaltimestamp(timestamp, varchar)
  RETURNS boolean AS '
BEGIN
RETURN $1 <= TO_DATE($2);
END;
' LANGUAGE 'plpgsql'
/-- END

--DROP OPERATOR <= (timestamp, varchar);
SELECT * FROM drop_operator('<='::varchar,'timestamp'::varchar,'varchar'::varchar);
/-- END

CREATE OPERATOR <=(
  PROCEDURE = "lowerequaltimestamp",
  LEFTARG = timestamp,
  RIGHTARG = varchar)
/-- END

CREATE OR REPLACE FUNCTION greaterequal(timestamp, varchar)
  RETURNS boolean AS '
BEGIN
RETURN $1 >= TO_DATE($2);
END;
' LANGUAGE 'plpgsql'
/-- END

--DROP OPERATOR >= (timestamp, varchar);
SELECT * FROM drop_operator('>='::varchar,'timestamp'::varchar,'varchar'::varchar);
/-- END

CREATE OPERATOR >=(
  PROCEDURE = "greaterequal",
  LEFTARG = timestamp,
  RIGHTARG = varchar)
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
--/--END

--DROP OPERATOR - (timestamptz, numeric)
--/--END

SELECT * FROM drop_operator('-','timestamptz','numeric')
/-- END

CREATE OPERATOR -(
  PROCEDURE = substract_days,
  LEFTARG = timestamptz,
  RIGHTARG = numeric)
/-- END

-- Auxiliar function for compatibility between 8.2 and 8.3 Postgres version.
CREATE OR REPLACE FUNCTION is_Trigger_Enabled(tg_name "text")
  RETURNS boolean AS
$BODY$ 
DECLARE
  v_isEnabled boolean := false;
BEGIN
  SELECT tgenabled INTO v_isEnabled FROM pg_trigger WHERE UPPER(tgname) = UPPER(tg_name);
  RETURN v_isEnabled;
EXCEPTION
WHEN OTHERS THEN
  SELECT (UPPER(tgenabled)<>'D') INTO v_isEnabled FROM pg_trigger WHERE tgname = tg_name;
  RETURN v_isEnabled;
END;   $BODY$
  LANGUAGE 'plpgsql' VOLATILE;
/-- END

-- Creating auxiliar functions for view dropping
CREATE or REPLACE function drop_view (view_name IN varchar) returns varchar as '
DECLARE
  cnt int4;
BEGIN
  SELECT into cnt count(*) from pg_views where upper(viewname) = upper(view_name::name);
  if cnt > 0 then
    execute ''DROP VIEW '' || view_name || '';'';
    return view_name || '' DROPPED'';
  end if;
  return view_name || '' does not exist'';
END;'
language 'plpgsql' 
/-- END

SELECT * FROM drop_view('DUAL')
/-- END

CREATE OR REPLACE VIEW DUAL AS SELECT 'X'::text AS dummy
/-- END

SELECT * FROM drop_view('USER_TABLES')
/-- END

CREATE OR REPLACE VIEW USER_TABLES
(TABLE_NAME, BLOCKS, DURATION, LAST_ANALYZED)
AS 
SELECT UPPER(TABLENAME), NULL::numeric, NULL::varchar, NULL::date 
FROM PG_TABLES 
WHERE SCHEMANAME = CURRENT_SCHEMA()
/-- END

--DROP VIEW USER_CONSTRAINTS
--/--END
SELECT * FROM drop_view('USER_CONSTRAINTS')
/-- END

CREATE OR REPLACE VIEW user_constraints AS 
 SELECT upper(pg_class.relname::text) AS table_name, upper(pg_constraint.conname::text) AS constraint_name, 
        CASE upper(pg_constraint.contype::text)
            WHEN 'F'::text THEN 'R'::text
            ELSE upper(pg_constraint.contype::text)
        END AS constraint_type, upper(pg_constraint.confdeltype::text) AS delete_rule, array_to_string(ARRAY( SELECT ('"'::text || pg_attribute.attname::text) || '"'::text AS attr_name
           FROM pg_attribute
          WHERE pg_attribute.attrelid = pg_constraint.conrelid AND (pg_attribute.attnum = ANY (pg_constraint.conkey))
          ORDER BY "position"(array_to_string(pg_constraint.conkey, '-'::text), pg_attribute.attnum::text)), ','::text) AS column_names, upper(fk_table.relname::text) AS fk_table, array_to_string(ARRAY( SELECT ('"'::text || pg_attribute.attname::text) || '"'::text AS attr_name
           FROM pg_attribute
          WHERE pg_attribute.attrelid = pg_constraint.confrelid AND (pg_attribute.attnum = ANY (pg_constraint.confkey))
          ORDER BY "position"(array_to_string(pg_constraint.confkey, '-'::text), pg_attribute.attnum::text)), ','::text) AS fk_column_names, pg_constraint.confmatchtype AS fk_matchtype, 
        CASE upper(pg_constraint.contype::text)
            WHEN 'P'::text THEN upper(pg_constraint.conname::text)
            WHEN 'U'::text THEN upper(pg_constraint.conname::text)
            ELSE ''::text
        END AS index_name, dba_getstandard_search_text(pg_constraint.consrc) AS search_condition, 'ENABLED'::text AS STATUS
   FROM pg_constraint
   JOIN pg_class ON pg_class.oid = pg_constraint.conrelid
   LEFT JOIN pg_class fk_table ON fk_table.oid = pg_constraint.confrelid
/-- END

--DROP VIEW USER_INDEXES
--/--END

SELECT * FROM drop_view('USER_INDEXES')
/-- END

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

--DROP VIEW USER_IND_COLUMNS
--/--END

SELECT * FROM drop_view('USER_IND_COLUMNS')
/-- END

CREATE OR REPLACE VIEW user_ind_columns AS
SELECT upper(pg_class1.relname::text) AS table_name, upper(pg_class.relname::text) AS index_name,
       upper(pg_attribute.attname::text) AS column_name,
       upper(pg_namespace.nspname::text) AS tablespace_name,
       (dba_getattnumpos(pg_index.indkey, pg_attribute.attnum)+1) AS column_position
   FROM pg_index, pg_class, pg_class pg_class1, pg_namespace, pg_attribute
  WHERE pg_index.indexrelid = pg_class.oid
    AND pg_index.indrelid = pg_class1.oid
    AND pg_attribute.attrelid = pg_index.indrelid
    AND pg_attribute.attnum = ANY (indkey)
    AND pg_class.relnamespace = pg_namespace.oid
    AND pg_class1.relnamespace = pg_namespace.oid
    AND pg_namespace.nspname = current_schema()
  ORDER BY table_name, index_name, column_position
/-- END

SELECT * FROM drop_view('USER_OBJECTS')
/-- END

CREATE OR REPLACE VIEW USER_OBJECTS
(TABLE_NAME, OBJECT_NAME, OBJECT_ID, OBJECT_TYPE, STATUS)
AS 
SELECT UPPER(PG_CLASS.RELNAME), UPPER(PG_TRIGGER.TGNAME), PG_TRIGGER.TGFOID, TO_CHAR('TRIGGER'), CASE is_Trigger_Enabled(PG_TRIGGER.TGNAME) WHEN TRUE THEN TO_CHAR('VALID') ELSE TO_CHAR('INVALID') END
FROM PG_TRIGGER, PG_NAMESPACE, PG_CLASS 
WHERE PG_TRIGGER.TGRELID = PG_CLASS.OID
AND PG_TRIGGER.TGISCONSTRAINT = FALSE
AND PG_CLASS.RELNAMESPACE = PG_NAMESPACE.OID
AND PG_NAMESPACE.NSPNAME = CURRENT_SCHEMA()
/-- END



SELECT * FROM drop_view('USER_CONS_COLUMNS')
/-- END

CREATE OR REPLACE VIEW user_cons_columns AS 
 SELECT upper(pg_constraint.conname::text) AS constraint_name, upper(pg_class.relname::text) AS table_name, upper(pg_attribute.attname::text) AS column_name, dba_getattnumpos(pg_constraint.conkey::integer[], pg_attribute.attnum::integer) AS "position"
   FROM pg_constraint, pg_class, pg_attribute
  WHERE pg_constraint.conrelid = pg_class.oid AND pg_attribute.attrelid = pg_constraint.conrelid AND (pg_attribute.attnum = ANY (pg_constraint.conkey))
  ORDER BY pg_class.relname, pg_constraint.conname
/-- END


--DROP VIEW USER_TAB_COLUMNS
--/--END

SELECT * FROM drop_view('USER_TAB_COLUMNS')
/-- END

CREATE OR REPLACE VIEW user_tab_columns AS
 SELECT upper(pg_class.relname::text) AS table_name, upper(pg_attribute.attname::text) AS column_name, upper(pg_type.typname::text) AS data_type,
        CASE pg_type.typname
            WHEN 'varchar'::name THEN pg_attribute.atttypmod - 4
            WHEN 'bpchar'::name THEN pg_attribute.atttypmod - 4
            ELSE NULL::integer
        END AS char_col_decl_length,

        CASE pg_type.typname
            WHEN 'bytea'::name THEN 4000
            WHEN 'text'::name THEN 4000
            WHEN 'oid'::name THEN 4000
            ELSE CASE PG_ATTRIBUTE.ATTLEN 
                     WHEN -1 THEN PG_ATTRIBUTE.ATTTYPMOD-4 
                     ELSE PG_ATTRIBUTE.ATTLEN 
                 END
        END AS data_length,

        CASE pg_type.typname
            WHEN 'bytea'::name THEN 4000
            WHEN 'text'::name THEN 4000
            WHEN 'oid'::name THEN 4000
            ELSE 
                CASE atttypmod
                    WHEN -1 THEN 0
                    ELSE 10 
                END
        END AS data_precision,
        0 AS data_scale,
        CASE pg_attribute.atthasdef
            WHEN true THEN ( SELECT pg_attrdef.adsrc
               FROM pg_attrdef
              WHERE pg_attrdef.adrelid = pg_class.oid AND pg_attrdef.adnum = pg_attribute.attnum)
            ELSE NULL::text
        END AS data_default, not pg_attribute.attnotnull AS nullable, pg_attribute.attnum AS column_id
   FROM pg_class, pg_namespace, pg_attribute, pg_type
  WHERE pg_attribute.attrelid = pg_class.oid AND pg_attribute.atttypid = pg_type.oid AND pg_class.relnamespace = pg_namespace.oid AND pg_namespace.nspname = current_schema() AND pg_attribute.attnum > 0
/-- END

SELECT * FROM drop_view('v$version')
/-- END

CREATE OR REPLACE VIEW v$version
AS 
 SELECT setting as banner
   FROM pg_settings
  WHERE name = 'server_version';
/-- END

--DROP VIEW USER_TRIGGERS
--/--END

SELECT * FROM drop_view('USER_TRIGGERS')
/-- END

CREATE OR REPLACE VIEW USER_TRIGGERS
(TABLE_NAME, TABLESPACE_NAME, TRIGGER_NAME, STATUS)
AS 
SELECT UPPER(PG_CLASS.RELNAME), UPPER(PG_NAMESPACE.NSPNAME), PG_TRIGGER.TGNAME, 'ENABLED'::text
FROM PG_TRIGGER, PG_CLASS, PG_NAMESPACE
WHERE PG_TRIGGER.tgrelid = PG_CLASS.OID
AND PG_CLASS.RELNAMESPACE = PG_NAMESPACE.OID
AND PG_NAMESPACE.NSPNAME = CURRENT_SCHEMA()
/-- END


-- DROP TEMPORARY FUNCTIONS
DROP FUNCTION exist_language(varchar)
/-- END

DROP FUNCTION insert_pg_language()
/-- END

DROP FUNCTION create_language(varchar)
/-- END

DROP FUNCTION type_oid(varchar)
/-- END

DROP FUNCTION drop_operator (varchar,varchar,varchar)
/-- END

DROP FUNCTION drop_view (varchar)
/-- END


