create table users(
    id              int,
    username        varchar(255),
    first_name      varchar(255),
    last_name       varchar(255),
    status          int,
    password_hash   varchar(255)
);

create table auth_assignment(
    item_name       varchar(64),
    user_id         varchar(64)
);
