package models;


import io.ebean.Model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Recipe extends Model {


    @Id
    @ManyToMany(cascade = CascadeType.ALL)
    private Long idReceta;


    private String nombre;

    @OneToOne(cascade = CascadeType.ALL)
    private Long dificultad;


    private List<Ingredients> ingredientes ;


    public Recipe(){

        ingredientes = new ArrayList<Ingredients>();


    }

    public Long getId() {
        return idReceta;
    }

    public void setId(Long id) {
        this.idReceta = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Long getDificultad() {
        return dificultad;
    }

    public void setDificultad(Long dificultad) {
        this.dificultad = dificultad;
    }

    public List<Ingredients> getIngredientes() {
        return ingredientes;
    }

    public void setIngredientes(List<Ingredients> ingredientes) {
        this.ingredientes = ingredientes;
    }
}
