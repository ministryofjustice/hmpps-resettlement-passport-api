
Alter table document_location add column category varchar(100) not  null default 'LICENCE_CONDITIONS';
Alter table document_location alter column category drop default;
Alter table document_location add column original_document_file_name varchar(256) not  null default 'unknown';
Alter table document_location alter column original_document_file_name drop default;

