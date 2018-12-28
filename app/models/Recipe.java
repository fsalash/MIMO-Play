package models;


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
    @ManyToMany(cascade = CascadeType.ALL)
    private Long idReceta;

    @Required
    private String nombre;

    @Required
    @OneToOne(cascade = CascadeType.ALL)
    private Posicion posicion;


    @Required
    private List<Ingredients> ingredientes ;

    private String explicacion;

    @Required
    @ManyToOne
    private Autor autor;

    @Required
    private String complejidad;



    public static final Finder<Long,Recipe> find = new Finder<>(Recipe.class);

    public static Recipe findRecipeById (Long id){

        ExpressionList<Recipe> query = find.query().where().eq("idReceta",id);
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

    public Long getIdReceta() {

        return idReceta;
    }

    public void setIdReceta(Long idReceta) {

        this.idReceta = idReceta;
    }

    public String getExplicacion() {

        return explicacion;
    }

    public void setExplicacion(String pasos) {

        this.explicacion = pasos;
    }

    public Autor getAutor() {

        return autor;
    }

    public void setAutor(Autor autor) {

        this.autor = autor;
    }

    public String getComplejidad() {
        return complejidad;
    }

    public void setComplejidad(String complejidad) {
        this.complejidad = complejidad;
    }
}
