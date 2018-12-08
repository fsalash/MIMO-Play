package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;

import java.util.Optional;



/**
 * Controlador general para el API de ingrediente
 *
 * Expone metodos para:
 *
 * .- Listar ingredientes
 * .- Dar de alta un ingrediente
 * .- Modificar ingrediente
 * .- Borrar ingrediente
 *
 */

public class IngredientController extends Controller {



    static String flagResponse = "";
    static String flagBody = "";

    static final String XML = "XML";
    static final String JSON = "JSON";

    public Result createIngredient(String jsonIngrediente) {

        //Revisar cabeceras de cliente para ver que acepta

        System.out.println("funcionando createIngredient en IngredientController");



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


    public static void chequeaCabeceraRequest(Http.Request req){


        System.out.println("funcionando createIngredient");





    }


}
