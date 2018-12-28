import controllers.RecipeController;
import models.Recipe;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TestUnitario_Validacion {


    List<Recipe> testRecetas;

    @Before
    public void preparaListaRecetas() {
        testRecetas = RecipeController.fakeRecetas();
    }

    @Test
    public void validacionFakeMethodListaNoNula() {

        testRecetas = RecipeController.fakeRecetas();

        assertNotNull(testRecetas);
    }

    @Test
    public void validacionFakeMethodListaConDiezItems() {

        testRecetas = RecipeController.fakeRecetas();

        assertTrue(testRecetas.size() == 10);
    }


    @Test
    public void validaAutor(){

        Recipe receta = null;

        if(testRecetas.size()>0)
            receta = testRecetas.get(0);

        assertNotNull(receta.getAutor());
    }

    @Test
    public void validaLongitudesCamposObligatoriosAutor(){

        Recipe receta = null;

        if(testRecetas.size()>0)
            receta = testRecetas.get(0);

        assertTrue(receta.getAutor().getNombre().length()<30 && receta.getAutor().getApellidos().length()<30);
    }

    @Test
    public void validaLongitudesCamposObligatoriosIngrediente(){

        Recipe receta = null;

        if(testRecetas.size()>0){
            receta = testRecetas.get(0);}

        assertTrue(receta.getIngredientes().get(0).getNombre().length()<25 &&
                receta.getIngredientes().get(0).getCantidad() >= 0);
    }

    @After
    public void limpiaListaRecetas(){
        testRecetas = null;
    }
}
