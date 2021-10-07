create table server
(
    server_number      int         not null primary key,
    server_ip_v4       inet,
    server_ip_v6       inet,
    server_name        text,
    product            text        not null,
    dc                 text        not null,
    traffic            text        not null,
    status             text        not null,
    cancelled          bool        not null,
    paid_until         date        not null,
    ips                inet[]      not null,
    subnets            jsonb,
    linked_storage_box int,
    zabbix_host_id     int,
    zabbix_host        text,
    zabbix_ip          inet,
    created_date       timestamptz not null,
    last_modified_date timestamptz not null,
    version            int         not null
);
