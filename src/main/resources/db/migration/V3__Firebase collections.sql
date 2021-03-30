create table fb_topics(
    id                   varchar(255)        not null primary key,
    uid                  varchar(255)        not null,
    is_linked_to_user    bit                 not null default false,
    is_public            bit                 not null,
    name                 varchar(1000)       not null,
    created_on           datetime            not null,
    modified_on          datetime            not null,
    viewed_on            datetime            not null,
    fcm_token            varchar(255)        not null,
    chat_id              bigint,
    imam_uid             varchar(255),
    is_answered          bit,
    imam_fcm_token       varchar(255),
    imam_viewed_on       datetime,

    index ix_fb_topics_uid (uid)
);

create table fb_messages(
    id                  varchar(255)            not null primary key,
    uid                 varchar(255)            not null,
    is_linked_to_user   bit                     not null,
    topic_id            varchar(255)            not null,
    created_on          datetime                not null,
    sender              char                    not null,
    text                text                    not null,
    message_id          bigint,
    sender_name         varchar(255),
    audio_url           varchar(1000),
    edited_on           datetime,

    key ix_fb_messages_uid (uid)
);

create table fb_favorites(
    id                  varchar(255)            not null primary key,
    uid                 varchar(255)            not null,
    is_linked_to_user   bit                     not null,
    topic_id            varchar(255)            not null,
    created_on          datetime                not null,
    topic_name          varchar(1000)           not null,
    favorite_id         bigint,

    key ix_fb_favorites_uid (uid)
);

create table fb_imams(
    id                  varchar(255)            not null primary key,
    user_id             int                     not null,
    name                varchar(255)            not null
);
