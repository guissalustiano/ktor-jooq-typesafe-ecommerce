create table category(
    id bigserial primary key,
    created_at timestamp with time zone not null default now(),
    updated_at timestamp with time zone not null default now(),

    parent_id bigint references category(id),

    name text not null,
    slug text not null unique,
    description text not null default ''
);

create table product(
    id bigserial primary key,
    created_at timestamp with time zone not null default now(),
    updated_at timestamp with time zone not null default now(),

    category_id bigint references category(id) not null,

    name text not null,
    slug text not null unique,
    description text not null default ''
);