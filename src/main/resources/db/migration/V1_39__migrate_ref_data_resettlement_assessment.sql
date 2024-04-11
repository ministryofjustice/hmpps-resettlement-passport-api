alter table resettlement_assessment add pathway varchar(100) not null default 'PENDING';

update resettlement_assessment set pathway = (
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

alter table resettlement_assessment drop column pathway_id;

alter table resettlement_assessment add assessment_status varchar(100) not null default 'PENDING';

update resettlement_assessment set assessment_status = (
    case
        when assessment_status_id = 1 then 'NOT_STARTED'
        when assessment_status_id = 2 then 'IN_PROGRESS'
        when assessment_status_id = 3 then 'COMPLETE'
        when assessment_status_id = 4 then 'SUBMITTED'
    end
);

alter table resettlement_assessment drop column assessment_status_id;

alter table resettlement_assessment add status_changed_to varchar(100);

update resettlement_assessment set status_changed_to = (
    case
        when status_changed_to_status_id = 1 then 'NOT_STARTED'
        when status_changed_to_status_id = 2 then 'IN_PROGRESS'
        when status_changed_to_status_id = 3 then 'SUPPORT_NOT_REQUIRED'
        when status_changed_to_status_id = 4 then 'SUPPORT_DECLINED'
        when status_changed_to_status_id = 5 then 'DONE'
        when status_changed_to_status_id = 6 then 'SUPPORT_REQUIRED'
    end
);

alter table resettlement_assessment drop column status_changed_to_status_id;
