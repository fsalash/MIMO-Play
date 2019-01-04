package models;

import io.ebean.ExpressionList;
import io.ebean.Finder;
import io.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;

@Entity
public class Posicion extends Model {

    @Id
    private Long id;


    @OneToOne(mappedBy="posicion")
    private Recipe recetaEnPosicion;//pagina del recetario. En una pagina una receta y solo una. Un poco forzada pero queria representar una relacion uno a uno sin usar un campo extra en un tabla :-)


    private String complejidad;

    public static final Finder<Long, Posicion> find = new Finder<>(Posicion.class);

    public static Posicion findPosById(Long idPos){

        ExpressionList<Posicion> query = find.query().where().eq("id",idPos);
        Posicion posicion = query.findOne();

        return posicion;
    }

    public Long getId() {

        return id;
    }

    public void setId(Long idPos) {

        this.id = idPos;
    }

    public String getComplejidad() {
        return complejidad;
    }

    public void setComplejidad(String descPosicion) {
        this.complejidad = descPosicion;
    }

    public Recipe getRecetaEnPosicion() {
        return recetaEnPosicion;
    }

    public void setRecetaEnPosicion(Recipe recetaEnPosicion) {
        this.recetaEnPosicion = recetaEnPosicion;
    }
}
