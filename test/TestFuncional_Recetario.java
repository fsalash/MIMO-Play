import models.Autor;
import org.junit.Before;
import org.junit.Test;
import play.Application;
import play.test.Helpers;
import play.test.WithApplication;

import static org.junit.Assert.assertNotNull;
import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.inMemoryDatabase;

public class TestFuncional_Recetario extends WithApplication {


    @Override
    protected Application provideApplication() {
        return Helpers.fakeApplication(Helpers.inMemoryDatabase("testFunc-MIMO"));
    }

    @Before
    public void preparaAutorTest(){

        Autor autor = new Autor();
        autor.setNombre("dummyName");
        autor.setApellidos("dummySurname");
        autor.save();
    }

    @Test
    public void recuperaAutoPorNombre(){

        Autor autor = new Autor();
        Autor authorByNameAndSurname = autor.findAuthorByNameAndSurname("dummyName", "dummySurname");

        assertNotNull(authorByNameAndSurname);

    }
}
