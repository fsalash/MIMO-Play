package models;

import io.ebean.ExpressionList;
import io.ebean.Finder;
import io.ebean.Model;
import play.data.validation.Constraints.Required;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;

@Entity
public class Posicion extends Model {

    @Id
    @OneToOne(mappedBy="posicion")
    private Long idPosicion;//pagina del recetario. En una pagina una receta y solo una. Un poco forzada pero queria representar una relacion uno a uno sin usar un campo extra en un tabla :-)


    private String descPosicion;

    public static final Finder<Long, Posicion> find = new Finder<>(Posicion.class);

    public static Posicion findDificultByIdPos (Long idPos){

        ExpressionList<Posicion> query = find.query().where().eq("idPosicion",idPos);
        Posicion posicion = query.findOne();

        return posicion;
    }

    public Long getIdPosicion() {

        return idPosicion;
    }

    public void setIdPosicion(Long idPos) {

        this.idPosicion = idPos;
    }

    public String getDescPosicion() {
        return descPosicion;
    }

    public void setDescPosicion(String descPosicion) {
        this.descPosicion = descPosicion;
    }
}
