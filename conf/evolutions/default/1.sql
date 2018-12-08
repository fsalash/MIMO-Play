# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table dificultad (
  id_dificultad                 bigint auto_increment not null,
  descripcion                   varchar(255),
  constraint pk_dificultad primary key (id_dificultad)
);

create table ingredients (
  id_ingrediente                bigint auto_increment not null,
  nombre                        varchar(255),
  constraint pk_ingredients primary key (id_ingrediente)
);

create table recipe (
  id_receta                     bigint auto_increment not null,
  nombre                        varchar(255),
  dificultad                    bigint,
  constraint pk_recipe primary key (id_receta)
);

create table recipe_ingredients (
  id                            bigint auto_increment not null,
  id_receta                     bigint,
  id_ingrediente                bigint,
  cantidad                      integer,
  constraint pk_recipe_ingredients primary key (id)
);


# --- !Downs

drop table if exists dificultad;

drop table if exists ingredients;

drop table if exists recipe;

drop table if exists recipe_ingredients;

