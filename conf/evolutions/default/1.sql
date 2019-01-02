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
  id_ingrediente                bigint auto_increment not null,
  nombre                        varchar(255),
  constraint pk_ingredients primary key (id_ingrediente)
);

create table posicion (
  id_posicion                   bigint auto_increment not null,
  complejidad                   varchar(255),
  constraint pk_posicion primary key (id_posicion)
);

create table recipe (
  id_receta                     bigint auto_increment not null,
  nombre                        varchar(255),
  posicion_id_posicion          bigint,
  explicacion                   varchar(255),
  id_autor                      bigint,
  complejidad                   varchar(255),
  constraint uq_recipe_posicion_id_posicion unique (posicion_id_posicion),
  constraint pk_recipe primary key (id_receta)
);

create table recipe_ingredients (
  id                            bigint auto_increment not null,
  id_receta                     bigint,
  id_ingrediente                bigint,
  cantidad                      integer,
  constraint pk_recipe_ingredients primary key (id)
);

alter table recipe add constraint fk_recipe_posicion_id_posicion foreign key (posicion_id_posicion) references posicion (id_posicion) on delete restrict on update restrict;


# --- !Downs

alter table recipe drop constraint if exists fk_recipe_posicion_id_posicion;

drop table if exists autor;

drop table if exists ingredients;

drop table if exists posicion;

drop table if exists recipe;

drop table if exists recipe_ingredients;

