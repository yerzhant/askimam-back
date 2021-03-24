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
    created_at,
    type,
    text,
    audio,
    updated_at
) values (
    1,
    1,
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
    1,
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
    1,
    1,
    now() + .5,
    'Audio',
    'Аудио',
    'audio.mp3',
    null
);

--insert into chats values(2);