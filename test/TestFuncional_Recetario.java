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

    @Before
    public void preparaDatosTest(){

        Autor autor = new Autor();
        autor.setNombre("dummyName");
        autor.setApellidos("dummySurname");

        autor.save();

        Ingredients ingrediente = new Ingredients();
        ingrediente.setNombre("ingredienteFake");
        ingrediente.setCantidad(100);

        ingrediente.save();

        List<Ingredients> ingredientes = new ArrayList<Ingredients>();
        ingredientes.add(ingrediente);

        Recipe receta = new Recipe();
        receta.setNombre("fakeRecipe");
        receta.setAutor(autor);

        receta.setIngredientes(ingredientes);

        Posicion pos = new Posicion();
        pos.setIdPosicion(new Long(122));
        pos.setDescPosicion("pagina 122");

        pos.save();

        receta.setPosicion(pos);
        receta.save();

        RecipeIngredients recIng = new RecipeIngredients();
        recIng.setIdIngrediente(ingrediente.getIdIngrediente());
        recIng.setIdReceta(receta.getIdReceta());

        recIng.save();

    }

    @Test
    public void recuperaAutoPorNombre(){

        Autor autor = new Autor();
        Autor authorByNameAndSurname = autor.findAuthorByNameAndSurname("dummyName", "dummySurname");

        assertNotNull(authorByNameAndSurname);

    }
}
