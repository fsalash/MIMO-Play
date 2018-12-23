package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models.Ingredients;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;

import javax.inject.Inject;
import java.util.Optional;

/**
 * Controlador general para el API de dificultades
 *
 * Expone metodos para:
 *
 * .- Listar dificultades
 * .- Dar de alta una dificultad
 * .- Modificar dificultad
 * .- Borrar dificultad
 *
 */

public class DificultadController extends Controller {



    static String flagResponse = "";
    static String flagBody = "";

    static final String XML = "XML";
    static final String JSON = "JSON";

      @Inject
      FormFactory formFactory;

      public Result createIngredient(String jsonIngrediente) {

        //Revisar cabeceras de cliente para ver que acepta
        chequeaCabeceraRequest(request());


        Form<Ingredients> form = formFactory.form(Ingredients.class).bindFromRequest();

        Ingredients ingrediente = form.get();

        System.out.println("ingrediente--> " + ingrediente.toString());
               
        System.out.println("ingrediente--> " + ingrediente.getNombre());

        switch (flagBody){

            case XML:
                //solo aceptamos json para recibir
                return  Results.notAcceptable();
            case JSON:
                JsonNode jsonNode = request().body().asJson();
                 System.out.println("doc jsonNode body: " + jsonNode.toString());
                 ingrediente.save();
                System.out.println(jsonNode);
                break;

                default:
                    return Results.badRequest();

        }

        if(flagResponse.equals(XML)){

            System.out.println("type xml");

            return ok();
        }
        else{

            if(flagResponse.equals(JSON)){

                System.out.println("type json");


                return ok();

            }
        }

        return Results.noContent();
    }


    public static void chequeaCabeceraRequest(Http.Request req) {


        System.out.println("funcionando IngredientController en RECIPE");

        if (req != null) {


            Optional<String> sContent = req.getHeaders().get("Content-Type");
            Optional<String> sAccept = req.getHeaders().get("Accept");


            if (sContent.isPresent()) {
                if (sContent.get().equals("application/xml")) {
                    System.out.println("body en XML");
                    flagBody = XML;
                } else {

                    if (sContent.get().equals("application/json")) {
                            System.out.println("body en JSON");
                        flagBody = JSON;

                    }
                }
            }

            if (sAccept.isPresent()) {
                if (sAccept.get().equals("application/xml")) {
                    if (sAccept.get().equals("application/xml")) {
                            System.out.println("acepta respuestas en XML");
                        flagResponse = XML;
                    } else {

                        if (sAccept.get().equals("application/json")) {
                              System.out.println("acepta respuestas en JSON");
                            flagResponse = JSON;

                        }
                    }
                }
            }

        }

    }


}
