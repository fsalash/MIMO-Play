package models;

import io.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

@Entity
public class Dificultad extends Model {

    @Id
    @OneToOne(mappedBy="dificultad")
    private Long idDificultad;
    private String descripcion;

    public Long getIdDificultad() {
        return idDificultad;
    }

    public void setIdDificultad(Long idDificultad) {
        this.idDificultad = idDificultad;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
