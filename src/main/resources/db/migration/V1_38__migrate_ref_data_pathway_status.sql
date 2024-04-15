alter table pathway_status add pathway varchar(100) not null default 'PENDING';

update pathway_status set pathway = (
    case
        when pathway_id = 1 then 'ACCOMMODATION'
        when pathway_id = 2 then 'ATTITUDES_THINKING_AND_BEHAVIOUR'
        when pathway_id = 3 then 'CHILDREN_FAMILIES_AND_COMMUNITY'
        when pathway_id = 4 then 'DRUGS_AND_ALCOHOL'
        when pathway_id = 5 then 'EDUCATION_SKILLS_AND_WORK'
        when pathway_id = 6 then 'FINANCE_AND_ID'
        when pathway_id = 7 then 'HEALTH'
    end
);

alter table pathway_status alter column pathway drop default;

alter table pathway_status drop column pathway_id;

alter table pathway_status add status varchar(100) not null default 'PENDING';

update pathway_status set status = (
    case
        when status_id = 1 then 'NOT_STARTED'
        when status_id = 2 then 'IN_PROGRESS'
        when status_id = 3 then 'SUPPORT_NOT_REQUIRED'
        when status_id = 4 then 'SUPPORT_DECLINED'
        when status_id = 5 then 'DONE'
        when status_id = 6 then 'SUPPORT_REQUIRED'
    end
);

alter table pathway_status alter column status drop default;

alter table pathway_status drop column status_id;
