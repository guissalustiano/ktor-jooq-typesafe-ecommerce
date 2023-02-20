create table category(
    id uuid primary key not null default gen_random_uuid(),
    created_at timestamp with time zone not null default now(),
    updated_at timestamp with time zone not null default now(),

    parent_id uuid references category(id),

    name text not null,
    slug text not null unique,
    description text not null default ''
);

create table product(
    id uuid primary key not null default gen_random_uuid(),
    created_at timestamp with time zone not null default now(),
    updated_at timestamp with time zone not null default now(),

    category_id uuid references category(id) not null,

    name text not null,
    slug text not null unique,
    description text not null default ''
);

CREATE TYPE clothe_size AS ENUM ('P', 'M', 'G', 'GG');

create table product_variant(
    id uuid primary key not null default gen_random_uuid(),
    created_at timestamp with time zone not null default now(),
    updated_at timestamp with time zone not null default now(),

    product_id uuid references product(id) not null,

    -- Color RGB
    color_name text,
    color_red smallint,
    color_green smallint,
    color_blue smallint,

    -- Textures
    color_url text,

    -- Size (for T-shirts)
    size clothe_size
);

create table product_image(
    id uuid primary key not null default gen_random_uuid(),
    created_at timestamp with time zone not null default now(),
    updated_at timestamp with time zone not null default now(),

    product_id uuid references product(id) not null,
    product_variant_id uuid references product_variant(id),

    url text not null,
    alt text not null default ''
);