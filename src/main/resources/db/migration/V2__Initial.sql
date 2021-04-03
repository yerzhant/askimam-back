create table chats(
    id                      bigint          not null primary key auto_increment,
    asked_by                int             not null,
    answered_by             int,
    type                    varchar(100)    not null,
    is_visible_to_public    bit             not null,
    updated_at              datetime        not null,
    inquirer_fcm_token      varchar(255)    not null,
    is_viewed_by_imam       bit             not null,
    is_viewed_by_inquirer   bit             not null,
    created_at              datetime        not null,
    imam_fcm_token          varchar(255),
    subject                 varchar(4000),

    key        ix_chats_asked_by                (asked_by),
    key        ix_chats_answered_by             (answered_by),
    constraint fk_chats_asked_by    foreign key (asked_by)      references users(id),
    constraint fk_chats_answered_by foreign key (answered_by)   references users(id)
);

create table messages(
    id                      bigint          not null primary key auto_increment,
    chat_id                 bigint          not null,
    author_id               int             not null,
    created_at              datetime        not null,
    type                    varchar(100)    not null,
    author_type             varchar(100)    not null,
    text                    text            not null,
    updated_at              datetime,
    duration                varchar(100),   -- TODO: add duration to Message entity + it may need to be converted to int
    audio                   varchar(1024),

    key        ix_messages_chat_id                  (chat_id),
    key        ix_messages_author_id                (author_id),
    constraint fk_messages_chat_id      foreign key (chat_id)   references chats(id),
    constraint fk_messages_author_id    foreign key (author_id) references users(id)
);

create table favorites(
    id          bigint          not null primary key auto_increment,
    user_id     int             not null,
    chat_id     bigint          not null,
    added_at    datetime        not null,

    unique key uk_favorites_user_id_chat_id         (user_id, chat_id),
    key        ix_favorites_chat_id                 (chat_id),
    constraint fk_favorites_user_id     foreign key (user_id)   references users(id),
    constraint fk_favorites_chat_id     foreign key (chat_id)   references chats(id) on delete cascade
);
