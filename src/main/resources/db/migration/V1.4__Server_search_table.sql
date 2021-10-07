create table server_search
(
    server_number      int         not null primary key,
    search_data        text,
    created_date       timestamptz not null,
    last_modified_date timestamptz not null,
    version            int         not null
);
