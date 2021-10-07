-- https://github.com/lukas-krecan/ShedLock#jdbctemplate
create table shedlock
(
    name       varchar(64)  not null primary key,
    lock_until timestamp    not null,
    locked_at  timestamp    not null,
    locked_by  varchar(255) not nulL
);
