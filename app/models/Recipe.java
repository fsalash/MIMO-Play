package models;


import com.fasterxml.jackson.annotation.JsonManagedReference;
import io.ebean.ExpressionList;
import io.ebean.Finder;
import io.ebean.Model;
import play.data.validation.Constraints.Required;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Recipe extends Model {


    @Id
    private Long id;

    @Required
    private String nombre;

    @Required
    private String explicacion;

    private String complejidad;

    //relacion 1 a N: Una receta tiene un autor y un autor varias recetas
    @Required
    @JsonManagedReference
    @ManyToOne
    private Autor autor;

    //relacion 1 a 1: Una receta tiene una Posicion y solo en una posicion hay una receta (podria ser una columna de receta pero asi creo relacion 1-1)
    @Required
    @JsonManagedReference
    @OneToOne(cascade = CascadeType.ALL)
    private Posicion posicion;

    //relacion N a M: Un ingrediente puede estar en varias recetas y una receta puede tener varios ingredientes
    @Required
    @JsonManagedReference
    @ManyToMany(cascade = CascadeType.ALL)
    private List<Ingredients> ingredientes;


    public static final Finder<Long,Recipe> find = new Finder<>(Recipe.class);

    public static Recipe findRecipeById (Long id){

        ExpressionList<Recipe> query = find.query().where().eq("id",id);
        Recipe receta = query.findOne();

        return receta;
    }

    public static Recipe findRecipeByName (String name){

        ExpressionList<Recipe> query = find.query().where().eq("nombre",name);
        Recipe receta = query.findOne();

        return receta;
    }

    public static List<Recipe> findAllRecipes (){

        List<Recipe> recetas = find.query().findList();

        return recetas;
    }

    public static List<Recipe> findRelationsByIdAuthor (Long id){

        ExpressionList<Recipe> query = find.query().where().eq("autor.id",id);

        return query.findList();
    }


    public Recipe(){

        ingredientes = new ArrayList<Ingredients>();

    }


    public String getNombre() {

        return nombre;
    }

    public void setNombre(String nombre) {

        this.nombre = nombre;
    }

    public Posicion getPosicion() {

        return posicion;
    }

    public void setPosicion(Posicion posicion) {

        this.posicion = posicion;

    }

    public List<Ingredients> getIngredientes() {

        return ingredientes;

    }

    public void setIngredientes(List<Ingredients> ingredientes) {

        this.ingredientes = ingredientes;

    }

    public Long getId() {

        return id;
    }

    public void setId(Long idReceta) {

        this.id = idReceta;
    }

    public String getExplicacion() {

        return explicacion;
    }

    public void setExplicacion(String pasos) {

        this.explicacion = pasos;
    }


    public String getComplejidad() {
        return complejidad;
    }

    public void setComplejidad(String complejidad) {
        this.complejidad = complejidad;
    }

    public Autor getAutor() {
        return autor;
    }

    public void setAutor(Autor autor) {
        this.autor = autor;
    }
}
