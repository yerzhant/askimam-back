insert into users(id, username, first_name, last_name, status, password_hash)
       values (10, 'the-imam-10', 'The', 'Imam', 1, 'x');
insert into users(id, username, first_name, last_name, status, password_hash)
       values (20, 'the-imam-20', 'The', 'Imam', 1, 'x');

insert into auth_assignment values ('ask-imam', '10');
insert into auth_assignment values ('ask-imam', '20');

insert into fcm_tokens values ('fcm-1', 10);
insert into fcm_tokens values ('fcm-2', 20);
insert into fcm_tokens values ('fcm-3', 20);
