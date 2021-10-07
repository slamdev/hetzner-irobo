create table update_history
(
    id            int primary key default unique_rowid(),
    update_type   text        not null,
    executed_date timestamptz not null
);

create index last_executed_update on update_history (update_type, executed_date desc);
