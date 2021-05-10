create table imam_ratings(
    imam_id     int                         not null primary key,
    rating      int                         not null check(rating >= 0),

    constraint  fk_imam_ratings_imam_id     foreign key (imam_id) references users(id) on delete cascade
);
