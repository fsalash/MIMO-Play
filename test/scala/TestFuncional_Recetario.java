package scala;

import akka.util.ByteString;
import controllers.RecipeController;
import models.*;
import org.junit.Before;
import org.junit.Test;
import play.Application;
import play.mvc.Http;
import play.mvc.Result;
import play.test.Helpers;
import play.test.WithApplication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.inMemoryDatabase;

public class TestFuncional_Recetario extends WithApplication {

    @Override
    protected Application provideApplication() {
        return Helpers.fakeApplication(Helpers.inMemoryDatabase());
    }



    @Test
    public void recuperaAutorPorNombre(){

        Autor autor = new Autor();
        autor.setNombre("dummyName");
        autor.setApellidos("dummySurname");

        autor.save();


        System.out.println("autor guardado--> " + autor.getNombre()+","+ autor.getApellidos());

        Autor authorByNameAndSurname = autor.findAuthorByNameAndSurname("dummyName", "dummySurname");

        assertNotNull(authorByNameAndSurname);

        autor.delete();//veo que los almacena en la misma bbdd usada en ejecucion asi que borro elemento dummy
    }


    @Test
    public void recuperaIngredientePorNombre(){

        Ingredients ingrediente = new Ingredients();
        ingrediente.setNombre("dummyIngredient");
        ingrediente.save();


        System.out.println("ingrediente guardado--> " + ingrediente.getNombre()+","+ ingrediente.getIdIngrediente());

        Ingredients ingredientById = ingrediente.findIngredientById(ingrediente.getIdIngrediente());

        assertNotNull(ingredientById);

        ingredientById.delete();//veo que los almacena en la misma bbdd usada en ejecucion asi que borro elemento dummy
    }

    @Test //pruba funcional completa de modelo, vista y controller procesando el map y recibiendo body como Form para reutilizar
    public void recuperaCreaYrecuperaAutoresFake(){


        for (int i =0;i<5;i++){

            //https://stackoverflow.com/questions/10890381/test-multipartformdata-in-play-2-0-fakerequest/28130543
            Map<String,String> data = new HashMap<String, String>();

            data.put("nombre", "authorName-"+i);
            data.put("apellidos", "authorSurname-"+i);

            Http.RequestBuilder req = (Http.RequestBuilder) Helpers.fakeRequest().
                method("POST")
                    .uri("/author")
                    .header("Content-Type", "application/json")
                    .bodyForm(data);

            Result r = Helpers.route(app, req);
            assertEquals(200, r.status());
            System.out.println("autor fake test created + " + i);

        }

        Http.RequestBuilder req = Helpers.fakeRequest()
                .method("GET")
                .uri("/authors")
                .header("Accept", "application/xml");

        Result r = Helpers.route(app, req);
        assertEquals(200, r.status());
        System.out.println("autores--> " + r.body());


    }


}
