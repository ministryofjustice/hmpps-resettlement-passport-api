alter table prisoner_support_need add column latest_update_id integer references prisoner_support_need_update (id);
