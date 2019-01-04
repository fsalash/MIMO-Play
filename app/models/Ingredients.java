package models;


import com.fasterxml.jackson.annotation.JsonBackReference;
import io.ebean.ExpressionList;
import io.ebean.Finder;
import io.ebean.Model;
import io.ebean.annotation.JsonIgnore;
import play.data.validation.Constraints.Required;

import javax.persistence.*;
import java.util.List;
import java.util.Set;

@Entity
public class Ingredients  extends Model {

    @Id
    private Long id;

    @JsonBackReference
    @ManyToMany(mappedBy = "ingredientes")
    private Set<Recipe> recetasIngrediente;

    @Required
    private String nombre;


    public static final Finder<Long,Ingredients> find = new Finder<>(Ingredients.class);

    public static Ingredients findIngredientByName (String name){

        ExpressionList<Ingredients> query = find.query().where().eq("nombre",name);
        Ingredients ingrediente = query.findOne();

        return ingrediente;
    }

    public static Ingredients findIngredientById (Long id){

        ExpressionList<Ingredients> query = find.query().where().eq("id",id);
        Ingredients ingrediente = query.findOne();

        return ingrediente;
    }


    public static List<Ingredients> findAllIngredients (){

        List<Ingredients> ingredientesBBDD = find.query().findList();

        return ingredientesBBDD;
    }

    public String getNombre() {

        return nombre;
    }

    public void setNombre(String nombre) {

        this.nombre = nombre;
    }

    public Long getId() {

        return id;
    }

    public void setId(Long idIngrediente) {

        this.id = idIngrediente;
    }


    public Set<Recipe> getRecetasIngrediente() {
        return recetasIngrediente;
    }

    public void setRecetasIngrediente(Set<Recipe> recetasIngrediente) {
        this.recetasIngrediente = recetasIngrediente;
    }
}
