create table category(
    id uuid primary key not null default gen_random_uuid(),
    created_at timestamp with time zone not null default now(),
    updated_at timestamp with time zone not null default now(),

    parent_id uuid references category(id),

    name text not null,
    slug text not null unique, -- domain key
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

create type clothe_size as enum (
    'P',
    'M',
    'G',
    'GG'
);

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

create table "user"
(
    id uuid primary key not null default gen_random_uuid(),
    created_at timestamp with time zone not null default now(),
    updated_at timestamp with time zone not null default now(),

    email text not null unique
);

create table user_proprieties(
    id uuid primary key not null default gen_random_uuid(),
    created_at timestamp with time zone not null default now(),
    updated_at timestamp with time zone not null default now(),

    user_id uuid references "user"(id) not null,

    first_name text not null,
    last_name text not null,
    phone text not null
);

create type order_status as enum (
    'CREATED',
    'BUDGETED',
    'APPROVED',
    'PRODUCED',
    'DELIVERED',
    'BILLED',
    'FINISHED',
    'CANCELED'
);

create table "order"
(
    id uuid primary key not null default gen_random_uuid(),
    created_at timestamp with time zone not null default now(),
    updated_at timestamp with time zone not null default now(),

    customer_id uuid references "user"(id) not null,

    budgeted_at timestamp with time zone,
    approved_at timestamp with time zone,
    produced_at timestamp with time zone,
    delivered_at timestamp with time zone,
    billed_at timestamp with time zone,
    finished_at timestamp with time zone,
    canceled_at timestamp with time zone
);

create table order_item(
    id uuid primary key not null default gen_random_uuid(),
    created_at timestamp with time zone not null default now(),
    updated_at timestamp with time zone not null default now(),

    order_id uuid references "order"(id) not null,

    product_variant_id uuid references product_variant(id),

    quantity integer not null,
    price numeric(10,2) not null
);