create table server
(
    id                 int         not null primary key,
    name               text,
    product            text        not null,
    dc                 text        not null,
    ip_v4              inet,
    ip_v6              inet,
    zabbix_host_id     int,
    zabbix_host        text,
    zabbix_ip          inet,
    search_keywords    text,
    tags               text[],
    created_date       timestamptz not null,
    last_modified_date timestamptz not null,
    version            int         not null
);
