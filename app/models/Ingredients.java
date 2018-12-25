package models;


import io.ebean.ExpressionList;
import io.ebean.Finder;
import io.ebean.Model;
import play.data.validation.Constraints;
import play.data.validation.Constraints.Required;

import javax.persistence.*;

@Entity
public class Ingredients  extends Model {

    @Id
  //  private Long id;
    @ManyToMany(cascade = CascadeType.ALL)
    private Long idIngrediente;

    @Required
    private String nombre;

    //https://stackoverflow.com/questions/11236806/ebean-how-to-exclude-string-as-column
    //lo marco como transient para que pueda "bindarlo" de la request aunque no quiera almacenar este valor en este modelo
    //solucion buscada en internet y referenciada en el enlace indicado
    @Transient
    @Required
    private int cantidad;//en gramos


    public static final Finder<Long,Ingredients> find = new Finder<>(Ingredients.class);

    public static Ingredients findIngredientByName (String name){

        ExpressionList<Ingredients> query = find.query().where().eq("nombre",name);
        Ingredients ingrediente = query.findOne();

        return ingrediente;
    }

    public static Ingredients findIngredientById (Long id){

        ExpressionList<Ingredients> query = find.query().where().eq("idIngrediente",id);
        Ingredients ingrediente = query.findOne();

        return ingrediente;
    }


    public String getNombre() {

        return nombre;
    }

    public void setNombre(String nombre) {

        this.nombre = nombre;
    }

    public Long getIdIngrediente() {

        return idIngrediente;
    }

    public void setIdIngrediente(Long idIngrediente) {

        this.idIngrediente = idIngrediente;
    }

    public int getCantidad() {

        return cantidad;
    }

    public void setCantidad(int cantidad) {

        this.cantidad = cantidad;
    }
}
