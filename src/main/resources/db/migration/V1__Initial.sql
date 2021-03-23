create table if not exists users(id int);

create table chat(
    id          bigint          not null primary key auto_increment
);

create table favorite(
    id          bigint          not null primary key auto_increment,
    user_id     bigint          not null references users(id),
    chat_id     bigint          not null references chat(id),
    added_at    datetime        not null,
    unique index(user_id, chat_id)
);