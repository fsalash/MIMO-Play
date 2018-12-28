package scala;

import controllers.RecipeController;
import models.*;
import org.junit.Before;
import org.junit.Test;
import play.Application;
import play.test.Helpers;
import play.test.WithApplication;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.inMemoryDatabase;

public class TestFuncional_Recetario extends WithApplication {

    @Override
    protected Application provideApplication() {
        return Helpers.fakeApplication(Helpers.inMemoryDatabase());
    }



    @Test
    public void recuperaAutoPorNombre(){

        Autor autor = new Autor();
        autor.setNombre("dummyName");
        autor.setApellidos("dummySurname");

        autor.save();


        System.out.println("autor guardado--> " + autor.getNombre()+","+ autor.getApellidos());

        Autor authorByNameAndSurname = autor.findAuthorByNameAndSurname("dummyName", "dummySurname");

        assertNotNull(authorByNameAndSurname);

        autor.delete();//veo que los almacena en la misma bbdd usada en ejecucion asi que borro elemento dummy
    }
}
