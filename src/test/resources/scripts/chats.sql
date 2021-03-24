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
    100,
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
    created_at,
    type,
    text,
    audio,
    updated_at
) values (
    1,
    100,
    2,
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
    created_at,
    type,
    text,
    audio,
    updated_at
) values (
    2,
    100,
    1,
    now() + 1,
    'Text',
    'A new reply',
    null,
    now() + 1
);
insert into messages(
    id,
    chat_id,
    author_id,
    created_at,
    type,
    text,
    audio,
    updated_at
) values (
    3,
    100,
    1,
    now() + .5,
    'Audio',
    'Аудио',
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
    200,
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
    created_at,
    type,
    text,
    audio,
    updated_at
) values (
    4,
    200,
    2,
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
    300,
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
    created_at,
    type,
    text,
    audio,
    updated_at
) values (
    5,
    300,
    2,
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
    400,
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
    created_at,
    type,
    text,
    audio,
    updated_at
) values (
    6,
    400,
    2,
    now(),
    'Text',
    'A message',
    null,
    null
);
