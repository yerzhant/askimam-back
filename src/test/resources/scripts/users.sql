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
    '$2y$12$4C3av3VYh/8CW7ITlH8Yeeza12Q9QR5QdWV04S4HcS896w0l0yBq.' -- passwd
);


insert into auth_assignment values ('ask-imam', '1');
