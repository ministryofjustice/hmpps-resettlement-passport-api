alter table document_location rename column document_key to original_document_key;
alter table document_location add column html_document_key uuid
