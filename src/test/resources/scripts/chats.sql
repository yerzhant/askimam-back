SET NAMES 'utf8';

-- *************** 1 *****************
insert into chats(
    id,
    asked_by,
    answered_by,
    type,
    is_visible_to_public,
    updated_at,
    inquirer_fcm_token,
    is_viewed_by_imam,
    is_viewed_by_inquirer,
    created_at,
    imam_fcm_token,
    subject
) values(
    1,
    2,
    1,
    'Public',
    true,
    now(),
    '456',
    true,
    true,
    now(),
    '123',
    'Subject'
);

insert into messages(
    id,
    chat_id,
    author_id,
    author_type,
    created_at,
    type,
    text,
    audio,
    updated_at
) values (
    1,
    1,
    2,
    'Inquirer',
    now(),
    'Text',
    'A message',
    null,
    null
);
insert into messages(
    id,
    chat_id,
    author_id,
    author_type,
    created_at,
    type,
    text,
    audio,
    updated_at
) values (
    2,
    1,
    1,
    'Imam',
    timestampadd(day, 2, now()),
    'Text',
    'A new reply',
    null,
    timestampadd(day, 3, now())
);
insert into messages(
    id,
    chat_id,
    author_id,
    author_type,
    created_at,
    type,
    text,
    audio,
    updated_at
) values (
    3,
    1,
    1,
    'Imam',
    timestampadd(day, 1, now()),
    'Audio',
    'Audio',
    'audio.mp3',
    null
);

-- *************** 2 *****************
insert into chats(
    id,
    asked_by,
    answered_by,
    type,
    is_visible_to_public,
    updated_at,
    inquirer_fcm_token,
    is_viewed_by_imam,
    is_viewed_by_inquirer,
    created_at,
    imam_fcm_token,
    subject
) values(
    2,
    2,
    null,
    'Public',
    false,
    now(),
    '456',
    true,
    true,
    now(),
    null,
    'Subject'
);

insert into messages(
    id,
    chat_id,
    author_id,
    author_type,
    created_at,
    type,
    text,
    audio,
    updated_at
) values (
    4,
    2,
    2,
    'Inquirer',
    now(),
    'Text',
    'A message',
    null,
    null
);

-- *************** 3 *****************
insert into chats(
    id,
    asked_by,
    answered_by,
    type,
    is_visible_to_public,
    updated_at,
    inquirer_fcm_token,
    is_viewed_by_imam,
    is_viewed_by_inquirer,
    created_at,
    imam_fcm_token,
    subject
) values(
    3,
    2,
    null,
    'Public',
    true,
    now(),
    '456',
    true,
    true,
    now(),
    null,
    'Subject'
);

insert into messages(
    id,
    chat_id,
    author_id,
    author_type,
    created_at,
    type,
    text,
    audio,
    updated_at
) values (
    5,
    3,
    2,
    'Inquirer',
    now(),
    'Text',
    'A message',
    null,
    null
);

-- *************** 4 *****************
insert into chats(
    id,
    asked_by,
    answered_by,
    type,
    is_visible_to_public,
    updated_at,
    inquirer_fcm_token,
    is_viewed_by_imam,
    is_viewed_by_inquirer,
    created_at,
    imam_fcm_token,
    subject
) values(
    4,
    2,
    null,
    'Private',
    true,
    now(),
    '456',
    true,
    true,
    now(),
    null,
    'Subject'
);

insert into messages(
    id,
    chat_id,
    author_id,
    author_type,
    created_at,
    type,
    text,
    audio,
    updated_at
) values (
    6,
    4,
    2,
    'Inquirer',
    now(),
    'Text',
    'A message',
    null,
    null
);
