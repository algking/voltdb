create table t (
  a bigint not null,
  b bigint not null,
  c bigint not null,
  d bigint not null,
  e bigint not null
);

create index idx_1_hash on t (a, b, c, d);
create index idx_2_TREE on t (e, a, b, c, d);

create index cover2_TREE on t (a, b);
create index cover3_TREE on t (a, c, b);

create table l
(
    id bigint NOT NULL,
    lname varchar(32) NOT NULL,
    a tinyint NOT NULL,
    b tinyint NOT NULL,
    CONSTRAINT PK_LOG PRIMARY KEY ( lname, id )
);

create index idx_c on l (lname, a, b, id);
create index idx_b on l (lname, b, id);
create index idx_a on l (lname, a, id);

CREATE TABLE a
(
    id BIGINT NOT NULL,
    deleted TINYINT NOT NULL,
    updated_date BIGINT NOT NULL,
    CONSTRAINT id PRIMARY KEY (id)
);

CREATE INDEX deleted_since_idx ON a (deleted, updated_date, id);

