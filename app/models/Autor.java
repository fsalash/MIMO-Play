package models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import io.ebean.ExpressionList;
import io.ebean.Finder;
import io.ebean.Model;
import play.data.validation.Constraints.Required;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.List;

@Entity
public class Autor extends Model {

    @Id
    private Long id;

    @JsonBackReference
    @OneToMany(cascade=CascadeType.ALL, mappedBy="autor")
    private List<Recipe> recetasAutor;

    @Required
    private String nombre;

    @Required
    private String apellidos;

    private String nacionalidad;


    public static final Finder<Long, Autor> find = new Finder<>(Autor.class);


    public static Autor findAuthorByNameAndSurname (String nombre, String apellidos){

        ExpressionList<Autor> query = find.query().where().eq("nombre",nombre).eq("apellidos",apellidos);
        Autor autor = query.findOne();

        return autor;
    }

    public static Autor findAuthorByIDReceta (Long id){

        ExpressionList<Autor> query = find.query().where().eq("id",id);
        Autor autor = query.findOne();

        return autor;
    }

    public static List<Autor> findAllAuthors (){

        List<Autor> autoresBBDD = find.query().findList();

        return autoresBBDD;
    }

    public static Autor findAuthorById (Long idAutor){

        ExpressionList<Autor> query = find.query().where().eq("id",idAutor);

        return query.findOne();
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<Recipe> getRecetasAutor() {
        return recetasAutor;
    }

    public void setRecetasAutor(List<Recipe> recetasAutor) {
        this.recetasAutor = recetasAutor;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getNacionalidad() {
        return nacionalidad;
    }

    public void setNacionalidad(String nacionalidad) {
        this.nacionalidad = nacionalidad;
    }
}
