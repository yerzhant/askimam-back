create table fb_topics(
    id                   varchar(255)        not null primary key,
    uid                  varchar(255)        not null,
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
    imam_viewed_on       datetime
);

create table fb_messages(
    id                  varchar(255)            not null primary key,
    uid                 varchar(255)            not null,
    topic_id            varchar(255)            not null,
    created_on          datetime                not null,
    sender              char                    not null,
    text                text                    not null,
    message_id          bigint,
    sender_name         varchar(255),
    edited_on           datetime,
    duration            varchar(100),
    audio_url           varchar(1000)
);

create table fb_favorites(
    id                  varchar(255)            not null primary key,
    uid                 varchar(255)            not null,
    topic_id            varchar(255)            not null,
    created_on          datetime                not null,
    topic_name          varchar(1000)           not null,
    favorite_id         bigint
);

create table fb_imams(
    id                  varchar(255)            not null primary key,
    user_id             int                     not null,
    name                varchar(255)            not null,

    unique key uk_fb_imams_name (name)
);

create table fb_profiles(
    id                  varchar(255)            not null primary key,
    login               varchar(1000)           not null,
    timezone            int                     not null,
    user_id             int
);
