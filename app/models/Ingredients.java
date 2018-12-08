package models;


import io.ebean.Model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;

@Entity
public class Ingredients  extends Model {

    @Id
    @ManyToMany(cascade = CascadeType.ALL)
    private Long idIngrediente;

    private String nombre;



    public Long getId() {
        return idIngrediente;
    }

    public void setId(Long id) {
        this.idIngrediente = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
