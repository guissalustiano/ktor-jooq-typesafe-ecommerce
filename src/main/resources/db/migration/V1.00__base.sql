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

CREATE TYPE clothe_size AS ENUM ('P', 'M', 'G', 'GG');

create table product_variant(
    id bigserial primary key,
    created_at timestamp with time zone not null default now(),
    updated_at timestamp with time zone not null default now(),

    product_id bigint references product(id) not null,

    -- Color RGB
    color_name text,
    color_red smallint,
    color_green smallint,
    color_blue smallint,

    -- Textures
    color_url text,

    -- Size (for T-shirts)
    size clothe_size
)