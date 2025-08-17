alter table auth
    add role VARCHAR(20) default 'USER' not null;