package scala;

import akka.util.ByteString;
import controllers.RecipeController;
import models.*;
import org.junit.Before;
import org.junit.Test;
import play.Application;
import play.libs.Json;
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

    @Test //prueba funcional completa de modelo, vista y controller procesando el map y recibiendo body como Form para reutilizar
    public void creaYrecuperaAutoresFake(){


        for (int i =0;i<5;i++){

            //https://stackoverflow.com/questions/10890381/test-multipartformdata-in-play-2-0-fakerequest/28130543
            Map<String,String> data = new HashMap<String, String>();

            data.put("nombre", "authorFakeName-"+i);
            data.put("apellidos", "authorFakeSurname-"+i);

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


    @Test //prueba funcional completa de modelo, vista y controller procesando el map y recibiendo body como Form para reutilizar
    public void creaYrecuperaIngredientesFake(){


        for (int i =0;i<5;i++){

            //https://stackoverflow.com/questions/10890381/test-multipartformdata-in-play-2-0-fakerequest/28130543
            Map<String,String> data = new HashMap<String, String>();

            data.put("nombre", "ingredientFakeName-"+i);


            Http.RequestBuilder req = (Http.RequestBuilder) Helpers.fakeRequest().
                    method("POST")
                    .uri("/ingredient")
                    .header("Content-Type", "application/json")
                    .bodyForm(data);

            Result r = Helpers.route(app, req);
            assertEquals(200, r.status());
            System.out.println("ingredient fake test created + " + i);

        }

        Http.RequestBuilder req = Helpers.fakeRequest()
                .method("GET")
                .uri("/ingredients")
                .header("Accept", "application/xml");

        Result r = Helpers.route(app, req);
        assertEquals(200, r.status());
        System.out.println("ingredients--> " + r.body());


    }

    @Test //prueba funcional completa de modelo, vista y controller procesando el map y recibiendo body como Form para reutilizar
    public void creaYrecuperaRecetasFake(){


        for (int i =0;i<5;i++){

            //https://www.playframework.com/documentation/2.6.x/ScalaTestingWithScalaTest
            String data = "{\"nombre\":\"recipe-" + i +
                    " \",\"autor\":{\"nombre\":\"authorFakeName-"+i+
                    "\",\"apellidos\": \"salas\"}, " +
                    "\"ingredientes\": [{\"nombre\":\"fakeIngredient-"+i+
                    "\",\"cantidad\":\"3"+i+"\"}] ,\"complejidad\":\"alta\"," +
                    " \"posicion\":{\"idPosicion\":\"90"+i+"\"} }";

            Http.RequestBuilder req = (Http.RequestBuilder) Helpers.fakeRequest().
                    method("POST")
                    .uri("/recipe")
                    .header("Content-Type", "application/json")
                    .bodyJson(Json.parse(data));

            Result r = Helpers.route(app, req);
            assertEquals(200, r.status());
            System.out.println("recipe fake test created + " + i);

        }


        Http.RequestBuilder req = Helpers.fakeRequest()
                .method("GET")
                .uri("/recipes")
                .header("Accept", "application/xml");

        Result r = Helpers.route(app, req);
        assertEquals(200, r.status());
        System.out.println("recipes--> " + r.body());


    }


    @Test //prueba funcional completa de modelo, vista y controller procesando el map y recibiendo body como Form para reutilizar
    public void validaRequiredEnRecetaFake() {

            //https://www.playframework.com/documentation/2.6.x/ScalaTestingWithScalaTest
            String data = "{\"nombreES\":\"recipe-X" +//deberia validar que no hay nombre de receta (he puesto nombreES)
                    " \",\"autor\":{\"nombre\":\"authorFakeName-X" +
                    "\",\"apellidos\": \"salas\"}, " +
                    "\"ingredientes\": [{\"nombre\":\"fakeIngredient-" +
                    "\",\"cantidad\":\"30" +  "\"}] ,\"complejidad\":\"alta\"," +
                    " \"posicion\":{\"idPosicion\":\"90" + "\"} }";

            Http.RequestBuilder req = (Http.RequestBuilder) Helpers.fakeRequest().
                    method("POST")
                    .uri("/recipe")
                    .header("Content-Type", "application/json")
                    .bodyJson(Json.parse(data));

            Result r = Helpers.route(app, req);
            assertEquals(400, r.status());
            System.out.println("recipe fake test");

    }

}
