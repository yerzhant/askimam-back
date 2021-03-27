insert into users(
    id,
    username,
    first_name,
    last_name,
    status,
    password_hash
) values (
    1,
    'the-imam',
    'The',
    'Imam',
    1,
    '$2y$12$9ZZQylVUQDpTROtRg.vmq.6NuH.LBcGC4sTt7G8NXIOic68SeAWYK' -- pwd
);

insert into users(
    id,
    username,
    first_name,
    last_name,
    status,
    password_hash
) values (
    2,
    'jon-dow',
    'Jon',
    'Dow',
    1,
    '$2y$12$YVshB8Gdf/S5tNB6kzSa9u0iebDtgDAv4tc47mtMYKW1dxTGfXvui' -- pwd
);


insert into auth_assignment values ('ask-imam', '1');
