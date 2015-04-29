--m_product indexes
CREATE INDEX m_product_value_upper ON m_product USING btree (upper(value::text) COLLATE pg_catalog."default" varchar_pattern_ops);
CREATE INDEX m_product_name_upper ON m_product USING btree (upper(name) COLLATE pg_catalog."default" varchar_pattern_ops);
CREATE INDEX m_product_upc_upper ON m_product USING btree (upper(upc::text) COLLATE pg_catalog."default" varchar_pattern_ops);

--c_bpartner indexes
CREATE INDEX c_bpartner_value_upper ON c_bpartner USING btree (upper(value) COLLATE pg_catalog."default" varchar_pattern_ops);
CREATE INDEX c_bpartner_name_upper ON c_bpartner USING btree (upper(name) COLLATE pg_catalog."default" varchar_pattern_ops);
CREATE INDEX c_bpartner_referenceno_upper ON c_bpartner USING btree (upper(referenceno) COLLATE pg_catalog."default" varchar_pattern_ops);

--c_order indexes
CREATE INDEX c_order_documentno_upper ON c_order USING btree (upper(documentno) COLLATE pg_catalog."default" varchar_pattern_ops);

--m_inout indexes
CREATE INDEX m_inout_documentno_upper ON m_inout USING btree (upper(documentno) COLLATE pg_catalog."default" varchar_pattern_ops);
