alter table bank_application alter column application_submitted_date type date using (
    if (extract(HOUR from timestamp(application_submitted_date)) in (22, 23)) then
      date(application_submitted_date) + 1;
    else
      date(application_submitted_date);
    end if;
    );

