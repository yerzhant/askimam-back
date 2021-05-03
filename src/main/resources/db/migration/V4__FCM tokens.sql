create table fcm_tokens(
    value       varchar(768)            not null primary key,
    user_id     int                     not null,

    key        ix_fcm_tokens_user_id                (user_id),
    constraint fk_fcm_tokens_user_id    foreign key (user_id)   references users(id)
);
