# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table autor (
  id                            bigint auto_increment not null,
  nombre                        varchar(255),
  apellidos                     varchar(255),
  nacionalidad                  varchar(255),
  constraint pk_autor primary key (id)
);

create table ingredients (
  id                            bigint auto_increment not null,
  nombre                        varchar(255),
  cantidad                      bigint,
  constraint pk_ingredients primary key (id)
);

create table posicion (
  id                            bigint auto_increment not null,
  constraint pk_posicion primary key (id)
);

create table recipe (
  id                            bigint auto_increment not null,
  nombre                        varchar(255),
  explicacion                   varchar(255),
  complejidad                   varchar(255),
  autor_id                      bigint,
  posicion_id                   bigint,
  constraint uq_recipe_posicion_id unique (posicion_id),
  constraint pk_recipe primary key (id)
);

create table recipe_ingredients (
  recipe_id                     bigint not null,
  ingredients_id                bigint not null,
  constraint pk_recipe_ingredients primary key (recipe_id,ingredients_id)
);

create index ix_recipe_autor_id on recipe (autor_id);
alter table recipe add constraint fk_recipe_autor_id foreign key (autor_id) references autor (id) on delete restrict on update restrict;

alter table recipe add constraint fk_recipe_posicion_id foreign key (posicion_id) references posicion (id) on delete restrict on update restrict;

create index ix_recipe_ingredients_recipe on recipe_ingredients (recipe_id);
alter table recipe_ingredients add constraint fk_recipe_ingredients_recipe foreign key (recipe_id) references recipe (id) on delete restrict on update restrict;

create index ix_recipe_ingredients_ingredients on recipe_ingredients (ingredients_id);
alter table recipe_ingredients add constraint fk_recipe_ingredients_ingredients foreign key (ingredients_id) references ingredients (id) on delete restrict on update restrict;


# --- !Downs

alter table recipe drop constraint if exists fk_recipe_autor_id;
drop index if exists ix_recipe_autor_id;

alter table recipe drop constraint if exists fk_recipe_posicion_id;

alter table recipe_ingredients drop constraint if exists fk_recipe_ingredients_recipe;
drop index if exists ix_recipe_ingredients_recipe;

alter table recipe_ingredients drop constraint if exists fk_recipe_ingredients_ingredients;
drop index if exists ix_recipe_ingredients_ingredients;

drop table if exists autor;

drop table if exists ingredients;

drop table if exists posicion;

drop table if exists recipe;

drop table if exists recipe_ingredients;

