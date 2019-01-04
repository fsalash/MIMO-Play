package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models.*;
import play.cache.SyncCacheApi;
import play.data.Form;
import play.data.FormFactory;
import play.i18n.Messages;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;
import views.xml.ingredientes;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;


/**
 * Controlador general para el API de ingredientes
 *
 * Expone metodos para:
 *
 * .- Listar ingredientes
 * .- Dar de alta un ingrediente
 * .- Modificar nombre ingrediente
 * .- Borrar ingrediente
 *
 */

public class IngredientController extends Controller {

    @Inject
    FormFactory formFactory;

    @Inject
    private SyncCacheApi cache;

    private static List<Ingredients> listaIngredientes = new ArrayList<Ingredients>();

    static final String XML = "XML";
    static final String JSON = "JSON";

    //por defecto JSON por si no se indicaran cabeceras en la llamada. El contenido del body solo lo aceptamos en json y la respuesta puede ser xml o json
    static String flagResponse = JSON;
    static String flagBody = JSON;

    static String flagLang = "ES"; //por defecto en castellano

    int flagErrorValidacion = 0; // 0 - todo ok, 1- error de tipo en parametro

    static Messages messages;

    /**
     * Metodo que consulta en bbdd los ingredientes creados y devuelve informacion en json o xml segun Accept del header de la invocacion (json por defecto)
     * @return : listado de ingredientes almacenados
     */
    public Result retrieveIngredients() {

        messages = Http.Context.current().messages();

        //Revisar cabeceras de cliente para ver que formato usa en el body y que acepta como respuesta
        chequeaCabeceraRequest(request());


        //buscamos todos los ingredientes existentes en bbdd

        List<Ingredients> listaEnCache = cache.get("listaIngredientes");

        if( listaEnCache != null){

            listaIngredientes =  listaEnCache;
        }
        else{

            listaIngredientes = Ingredients.findAllIngredients();
            cache.set("listaIngredientes",listaIngredientes);
        }



        if(flagResponse.equals(XML)){

            System.out.println("type xml");
            return ok(ingredientes.render(listaIngredientes));
        }
        else{

            if(flagResponse.equals(JSON)){

                System.out.println("type json");
                JsonNode jsonNodeListaRecetas = Json.toJson(listaIngredientes);
                return ok(jsonNodeListaRecetas);

            }
        }

        return Results.noContent();
    }

    /**
     * Metodo para crear ingredientes
     * @return JSON/XML con el ingrediente creado
     */
    public Result createIngredient (){

        messages = Http.Context.current().messages();

        chequeaCabeceraRequest(request());

        Form<Ingredients> form = formFactory.form(Ingredients.class).bindFromRequest();

        try{

            Ingredients ingrediente  = new Ingredients();
            ingrediente = form.get(); //"bindo" el formulario de entrada a un objeto ingrediente



            /*Para poder dar de alta un nuevo ingrediente se debe cumplir que:

                1.- No exista un ingrediente con el mismo nombre en bbdd

               Usamos finder previo a la insercion para ver si ya existe el elemento
            */

            validaDatosEntrada(ingrediente);


            switch (flagErrorValidacion){

                case -1:
                    JsonNode nodoRespuesta1 = Json.toJson(messages.at("ingrediente-1FlagValidacion") + ingrediente.getNombre());
                    return badRequest(nodoRespuesta1);


            }



            Ingredients ingredientByName = Ingredients.findIngredientByName(ingrediente.getNombre());

            if(ingredientByName==null){

               //no existe el ingrediente asi que lo guardamos

                ingrediente.save();

                cache.remove("listaIngredientes"); //ya no vale la cache para siguientes consultas asi que anulamos


                switch (flagResponse){ //revisamos como acepta la respuesta el cliente

                    case XML:
                        return ok(views.xml.ingrediente.render(ingrediente)); //devolvemos info del ingrediente en xml

                    case JSON:
                        JsonNode jsonNode = Json.toJson(ingrediente);
                        System.out.println("doc jsonNode body: " + jsonNode.toString());
                        System.out.println(jsonNode);
                        return ok(jsonNode);//devolvemos info del ingrediente en json

                    default:
                        return Results.badRequest();//en otro caso devolvemos error en request

                }

            }

            else{
                return ok(views.html.ingredienteRepe.render(messages)); //ya existe un ingrediente guardado en bbdd con el mismo nombre
            }



        }catch (IllegalStateException ex){
            System.out.println("error de bindado del formulario de entrada, es posible que campos required no estén siendo informados");
            ex.printStackTrace();
            return badRequest(views.html.ingredienteErr.render(messages));
        }
    }


    /**
     * Borrado de ingrediente por id (ojo que habrá operaciones que por la relacion entre receta-ingrediente no se puedan realizar
     * @param id
     * @return Json/xml con el resultado de la operacion
     */
    public Result deleteIngredient(Long id){

        messages = Http.Context.current().messages();

        chequeaCabeceraRequest(request());

        Ingredients ingredientById = Ingredients.findIngredientById(id);

        if(ingredientById!=null){

                ingredientById.delete();
                cache.remove("listaIngredientes"); //ya no vale la cache para siguientes consultas asi que anulamos
                return ok(views.html.ingredienteBorrado.render(ingredientById,messages));


        }
        else{
            System.out.println("no se ha encontrado el id del ingrediente: " + id);
            return ok(views.html.ingredienteNoEncontrado.render(id,messages));
        }

    }


    /**
     * Actualizacion del nombre del ingrediente por id
     * @param id, nuevo nombre del ingrediente
     * @return Json/xml con el resultado de la operacion
     */
    public Result updateIngredient (Long id, String newIngredientName){

        messages = Http.Context.current().messages();

        chequeaCabeceraRequest(request());

        Ingredients ingredientById = Ingredients.findIngredientById(id);

        if(ingredientById!=null){

            ingredientById.setNombre(newIngredientName);
            ingredientById.update();
            cache.remove("listaIngredientes"); //ya no vale la cache para siguientes consultas asi que anulamos
            System.out.println("Update correcto:  " + ingredientById.getNombre());
            return ok(views.html.ingredienteActualizado.render(ingredientById,messages));

        }
        else{

            System.out.println("Imposible actualizar - NO se ha encontrado el ingrediente con id: " + id);
            return ok(views.html.ingredienteNoEncontrado.render(id,messages));
        }


    }
    //**********************************************************
    //////UTILIDADES PARA EL MANEJO y PROCESAMIENTO DE LA ENTRADA//////////////
    //**********************************************************


    /**
     * Metodo de validacion generico para controlar longitud del nombre del ingrediente a crear
     *
     * @param: ingrediente de entrada
     *  No devuelve nada porque trabajamos con una variable global
     */

    private void validaDatosEntrada(Ingredients ingrediente){

        flagErrorValidacion = 0;

        // validacion ingrediente

            if (ingrediente.getNombre().length()>25) {
                flagErrorValidacion = -1;
            }


    }

    /**
     * Chequea el content-type y el formato de respuesta que soporta el cliente que invoca. Me sirve para saber como me mandan la info y que
     * soporte el cliente como respuesta.
     * @param req
     */
    public static void chequeaCabeceraRequest(Http.Request req){


        System.out.println("chequeo cabecera  en RECIPECONTROLLER");

        if(req!=null){


            Optional<String> sContent = req.getHeaders().get("Content-Type");
            Optional<String> sAccept = req.getHeaders().get("Accept");
            Optional<String> sAcceptLang = req.getHeaders().get("Accept-Language");



            if(sContent.isPresent()){
                if(sContent.get().equals("application/xml")){

                    flagBody = XML;

                }
                else{

                    if(sContent.get().equals("application/json")){

                        flagBody = JSON;

                    }
                }
            }



            if(sAccept.isPresent()){
                    if(sAccept.get().equals("application/xml")){

                        flagResponse = XML;
                    }
                    else {

                        if (sAccept.get().equals("application/json")) {

                            flagResponse = JSON;

                        }
                    }
            }

            if(sAcceptLang.isPresent()){
                if(sAcceptLang.get().toUpperCase().equals("ES")){

                    flagLang = "ES";
                }
                else {

                    if (sAccept.get().toUpperCase().equals("EN")) {

                        flagLang = "EN";

                    }
                }
            }

            System.out.println("content-type: " +flagBody);
            System.out.println("accept: " +flagResponse);

         }

    }


    /**
     * FAKE. Metodo para ir probando la construccion. Crea un recetario fake v0 (sin autor). No valido para la valoracion del ejercicio :-)
     */
    public static List<Recipe> fakeRecetas() {

        List<Recipe> listaFakeRecetas = new ArrayList<Recipe>();
        System.out.println("construyendo recetas FAKE");

        for (int i = 0; i < 10; i++) {

            Recipe receta = new Recipe();
            List<Ingredients> listaIngredientes = new ArrayList<Ingredients>();

            for (int j = 0; j < 3; j++) {

                Ingredients ingrediente = new Ingredients();
                ingrediente.setNombre("ingrediente-" + i);

                listaIngredientes.add(ingrediente);


            }

            receta.setIngredientes(listaIngredientes);

            Posicion posicion = new Posicion();
            Random rand = new Random();

            int n = rand.nextInt(5000) + 1;
            posicion.setId(new Long(n));
            receta.setComplejidad("DIFICIL");

            receta.setPosicion(posicion);
            receta.setNombre("recetaFake-" + i);



            Autor autor = new Autor();
            autor.setApellidos("fakeSurname");
            autor.setNombre("fakeName");

            receta.setAutor(autor);

            listaFakeRecetas.add(receta);

        }

    return listaFakeRecetas;
    }
}
