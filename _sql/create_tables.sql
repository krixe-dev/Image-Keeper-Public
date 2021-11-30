DROP TABLE IF EXISTS user_data;

CREATE TABLE IF NOT EXISTS user_data
(
    id bigint NOT NULL,
    name character varying(100) COLLATE pg_catalog."default" NOT NULL,
    CONSTRAINT user_data_pkey PRIMARY KEY (id),
    CONSTRAINT uk_oj8aadf6iha7ixj3q7p36x4fu UNIQUE (name)
);


DROP TABLE IF EXISTS image_data;

CREATE TABLE IF NOT EXISTS image_data
(
    id bigint NOT NULL,
    creation_time timestamp without time zone NOT NULL,
    description character varying(255) COLLATE pg_catalog."default",
    image_uid character varying(36) COLLATE pg_catalog."default" NOT NULL,
    last_update_time timestamp without time zone NOT NULL,
    status character varying(255) COLLATE pg_catalog."default",
    original_file_name character varying(255) COLLATE pg_catalog."default",
    title character varying(100) COLLATE pg_catalog."default",
    user_id bigint NOT NULL,
    CONSTRAINT image_data_pkey PRIMARY KEY (id),
    CONSTRAINT uk_g6iutoj27663nm0j0hulm1tyu UNIQUE (image_uid),
    CONSTRAINT fkoy7o6xr5c4w3035wye0ll6ah5 FOREIGN KEY (user_id)
        REFERENCES user_data (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);