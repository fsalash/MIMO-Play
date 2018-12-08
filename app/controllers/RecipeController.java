package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models.Ingredients;
import models.Recipe;
import models.RecipeIngredients;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


/**
 * Controlador general para el API de recetas
 *
 * Expone metodos para:
 *
 * .- Listar recetas
 * .- Dar de alta una receta
 * .- Modificar receta
 * .- Borrar receta
 *
 */

public class RecipeController extends Controller {


    private static List<Recipe> listaRecetas = new ArrayList<Recipe>();


    public Result retrieveRecipes() {

        //Revisar cabeceras de cliente para ver que acepta

        System.out.println("funcionando retrieveRecipes en RECIPE");

        Http.Headers headers =
                request().getHeaders();


        Optional<String> s = headers.get("Content-Type");
        System.out.println("s get--> " +s.get());

        System.out.println("content--> " + headers.get("Content-Type"));

        System.out.println(s.get().equals("application/xml"));
        System.out.println(s.get().equals("application/json"));

        if(s.get().equals("application/xml")){

            System.out.println("type xml");
            fakeReceta(); //codigo de prueba que genera recetas
            return ok(views.xml.recetas.render(listaRecetas));
        }
        else{

            if(s.get().equals("application/json")){

                System.out.println("type json");
                fakeReceta(); //codigo de prueba que genera recetas

                JsonNode jsonNodeListaRecetas = Json.toJson(listaRecetas);
                return ok(jsonNodeListaRecetas);

            }
        }

        return Results.noContent();
    }

    /**
     *
     * @param receta: Sera una json que represente a la receta a crear
     * @return
     */
    public Result createRecipe (String receta){

        System.out.println("funcionando createRecipe en RECIPE");
        return ok();
    }

    public static void fakeReceta(){

        System.out.println("construyendo recetas FAKE");

        for (int i=0;i<10;i++)
        {

            System.out.println("construyendo receta: " + i);
            Recipe receta = new Recipe();
            List<Ingredients> listaIngredientes = new ArrayList<Ingredients>();

            for(int j=0;j<3;j++){

                System.out.println("construyendo ingrediente : "+ j + " de receta : " + i);
                Ingredients ingrediente = new Ingredients();
                ingrediente.setNombre("ingrediente-"+i);

                listaIngredientes.add(ingrediente);
                System.out.println("ingrediente introducido en receta");
                ingrediente.save();
                System.out.println("ingrediente introducido en bbdd");


            }

            receta.setIngredientes(listaIngredientes);
            receta.setDificultad(i);
            receta.setNombre("recetaFake-"+i);


            receta.save();




            for(int k=0;k<receta.getIngredientes().size();k++){

                RecipeIngredients recIng = new RecipeIngredients();

                Ingredients ingrediente = receta.getIngredientes().get(k);

                System.out.println("creando relacion receta: " + i + " con ingrediente : " + k);

                recIng.setIdIngrediente(ingrediente.getId());
                recIng.setIdReceta(receta.getId());
                recIng.setCantidad(i);
                recIng.save();

            }

            listaRecetas.add(receta);

        }


    }
}
