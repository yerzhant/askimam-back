SET NAMES 'utf8';

create table if not exists users(
    id              int,
    first_name      varchar(255),
    last_name       varchar(255)
);

create table if not exists auth_assignment(
    item_name       varchar(64),
    user_id         varchar(64)
);

create table chats(
    id                      bigint          not null primary key auto_increment,
    asked_by                bigint          not null references users(id),
    answered_by             bigint                   references users(id),
    type                    varchar(100)    not null,
    is_visible_to_public    bit             not null,
    updated_at              datetime        not null,
    inquirer_fcm_token      varchar(255)    not null,
    is_viewed_by_imam       bit             not null,
    is_viewed_by_inquirer   bit             not null,
    created_at              datetime        not null,
    imam_fcm_token          varchar(255),
    subject                 varchar(255)
);

create table messages(
    id                      bigint          not null primary key auto_increment,
    chat_id                 bigint          not null references chats(id),
    author_id               bigint          not null references users(id),
    created_at              datetime        not null,
    type                    varchar(100)    not null,
    author_type             varchar(100)    not null,
    text                    text            not null,
    audio                   varchar(1024),
    updated_at              datetime
);

create table favorites(
    id          bigint          not null primary key auto_increment,
    user_id     bigint          not null references users(id),
    chat_id     bigint          not null references chats(id) on delete cascade,
    added_at    datetime        not null,
    unique index(user_id, chat_id)
);