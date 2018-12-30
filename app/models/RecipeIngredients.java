package models;

import io.ebean.ExpressionList;
import io.ebean.Finder;
import io.ebean.Model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import java.util.List;


@Entity
public class RecipeIngredients  extends Model {


    @Id
    private Long id;


    @ManyToMany(mappedBy = "idReceta")
    private Long idReceta;


    @ManyToMany(mappedBy = "idIngrediente")
    private Long idIngrediente;


    private Integer cantidad; //en gramos por ejemplo



    public static final Finder<Long,RecipeIngredients> find = new Finder<>(RecipeIngredients.class);

    public static List<RecipeIngredients> findIngredientsByIdRecipe (Long idReceta){

        ExpressionList<RecipeIngredients> query = find.query().where().eq("idReceta",idReceta);
        List<RecipeIngredients> ingredientes = query.findList();

        return ingredientes;
    }

    public static List<RecipeIngredients> findIngredientsByIdIngredient (Long idIngrediente){

        ExpressionList<RecipeIngredients> query = find.query().where().eq("idIngrediente",idIngrediente);
        List<RecipeIngredients> relacionesRecetaIngredientes = query.findList();

        return relacionesRecetaIngredientes;
    }

    public Long getId() {

        return id;
    }

    public void setId(Long id) {

        this.id = id;
    }

    public Long getIdReceta() {

        return idReceta;
    }

    public void setIdReceta(Long idReceta) {

        this.idReceta = idReceta;
    }

    public Long getIdIngrediente() {

        return idIngrediente;
    }

    public void setIdIngrediente(Long idIngrediente) {

        this.idIngrediente = idIngrediente;
    }

    public Integer getCantidad() {

        return cantidad;
    }

    public void setCantidad(Integer cantidad) {

        this.cantidad = cantidad;
    }
}
