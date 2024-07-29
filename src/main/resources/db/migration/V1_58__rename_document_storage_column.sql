Truncate table document_location;
Alter table document_location alter column original_document_key type uuid USING original_document_key::uuid;
Alter table document_location rename column html_document_key TO pdf_document_key


