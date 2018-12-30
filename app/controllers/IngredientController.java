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
 * Controlador general para el API de recetas
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
     * Metodo que consulta en bbdd los ingredientes creadas y devuelve informacion en json o xml segun Accept del header de la invocacion (json por defecto)
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
                        return ok(views.xml._ingrediente.render(ingrediente)); //devolvemos info del ingrediente en xml

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
        List<RecipeIngredients> relacionesRecetaIngredientes = RecipeIngredients.findIngredientsByIdIngredient(id);

        if(ingredientById!=null){

            if(relacionesRecetaIngredientes == null) {
                ingredientById.delete();
                cache.remove("listaIngredientes"); //ya no vale la cache para siguientes consultas asi que anulamos
                return ok(views.html.ingredienteBorrado.render(ingredientById,messages));
            }
            else {
                System.out.println("No se puede borrar el ingrediente porque está siendo usado en una receta.");
                return forbidden(Json.toJson(messages.at("ingredienteEnUso")));

            }
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
     * Metodo que borra las relaciones n-m guardadas entre idRecete e ingredientes
     * Al borrar una receta si no eliminamos la relacion entre idReceta e idIngrediente es posible que al crear otra receta el id se "aproveche" y tengamos lios :-)
     * @param: Recibe el id de la receta en cuestion
     */
    private void borraRelacionIngredientesReceta(Long idReceta){

        List<RecipeIngredients> ingredientsByIdRecipe = RecipeIngredients.findIngredientsByIdRecipe(new Long(idReceta));


        for(RecipeIngredients ingRec:ingredientsByIdRecipe){

            System.out.println("borrando relacion receta: " + idReceta + " con ingrediente: " + ingRec.getIdIngrediente());
            ingRec.delete();//borro relacion por si luego el id de la receta eliminada se reutiliza
        }

    }




    /**
     * Metodo que se encarga de buscar los ingredientes para cada receta con la relacion n-m que exista asi como su cantidad,
     * buscar el autor de la receta para cada receta con la relacion 1-n que exista y
     * se encarga de buscar la dificultad para cada receta con la relacion 1-1 que exista
     *
     * @param: Recibe una lista de recetas
     * @return Lista de recetas de bbdd
     */
    private List<Recipe>  buscaInfoRecetas(List<Recipe> listaRecetas){


        List<Recipe> listaAux = new ArrayList<Recipe>();


        for(int i=0;i<listaRecetas.size();i++){

            List<RecipeIngredients> ingredientsByIdRecipe = new ArrayList<RecipeIngredients>();
            Posicion posicion = new Posicion();
            Autor autor = new Autor();

            Recipe recetaBBDD = listaRecetas.get(i);
            Recipe receta = new Recipe();

           receta.setIdReceta(recetaBBDD.getIdReceta());
           receta.setNombre(recetaBBDD.getNombre());


            //ingredientes
            ingredientsByIdRecipe = RecipeIngredients.findIngredientsByIdRecipe(recetaBBDD.getIdReceta());
            List<Ingredients> ingredientes = procesaRelacionIngredientesReceta(ingredientsByIdRecipe);
            receta.setIngredientes(ingredientes);


            //Posicion
            posicion = Posicion.findPosById(recetaBBDD.getPosicion().getIdPosicion());
            receta.setPosicion(posicion);


            //autor
            autor = Autor.findAuthorByIDReceta(recetaBBDD.getAutor().getId());
            receta.setAutor(autor);


            listaAux.add(receta);
        }

        return listaAux;

    }

    /**
     *  Metodo que procesa los ingredientes de la invocacion y los asocia a la receta validando previamente en bbdd para no repetir ingredientes
     * @param ingredientsByIdRecipe
     * @return Lista de relacion de ingredientes usados en una receta
     */
    private List<Ingredients> procesaRelacionIngredientesReceta ( List<RecipeIngredients> ingredientsByIdRecipe ){

        List<Ingredients> listaIngredientesADevolver = new ArrayList<>();

        for (int j=0;j<ingredientsByIdRecipe.size();j++){

            RecipeIngredients recIng = ingredientsByIdRecipe.get(j);

            Ingredients ingredientById = Ingredients.findIngredientById(recIng.getIdIngrediente());

            Ingredients ingrediente = new Ingredients();

            //obtengo nombre e id de la tabla "maestra" de ingredientes (relacion n-m)
            ingrediente.setNombre(ingredientById.getNombre());

            //obtengo cantidad de la tabla "auxiliar" n-m que almacena la cantidad de gramos de cada ingrediente para cada receta
            ingrediente.setCantidad(recIng.getCantidad());

            ingrediente.setIdIngrediente(recIng.getIdIngrediente());
            listaIngredientesADevolver.add(ingrediente);
        }

        return listaIngredientesADevolver;
    }


    /**
     * Metodo que procesa la entrada por POST para dar de alta los ingredientes de una receta validando si ya existian (el id del ingrediente guardado es el que se usara para la relacion entre receta-ingrediente-cantidad)
     * @param receta
     */
    private void procesaIngredientes(Recipe receta) {

        //recorremos los posibles ingredientes que hubieran pasado en la llamada
        List<Ingredients> ingredientes = receta.getIngredientes();


        for(int i=0;i<ingredientes.size();i++){

            Ingredients ingrediente = ingredientes.get(i);


            Ingredients ingredienteGuardado = Ingredients.findIngredientByName(ingrediente.getNombre());//busco si el ingrediente de esta receta ya existe de anteriores
            RecipeIngredients recipeIngredients = new RecipeIngredients();//esto me sirve para guardar la relacion n-m de recetas con ingredientes

            if(ingredienteGuardado!=null){

                //existe el ingrediente en bbdd entonces lo asocio a la receta
                recipeIngredients.setIdIngrediente(ingredienteGuardado.getIdIngrediente());


            }
            else{
                //el ingrediente no existia en bbdd de ejecuciones anteriores y lo almaceno como ingrediente nuevo

                ingrediente.save();
                recipeIngredients.setIdIngrediente(ingrediente.getIdIngrediente());

            }


            recipeIngredients.setCantidad(ingrediente.getCantidad());//esto siempre lo cojo de la request porque para cada receta la cantidad podria ser diferente

            recipeIngredients.setIdReceta(receta.getIdReceta()); //guardo relacion con receta antes de persistir relacion receta-cliente (N-M)

            recipeIngredients.save();//guardo relacion n-m recetas con ingredientes

        }

    }


    /**
     * Metodo que procesa la entrada por POST para dar de alta el autor de una receta validando si ya existia en bbdd previamente
     * @param receta
     */
    private void procesaAutor(Recipe receta){

        Autor autor= receta.getAutor(); //Una receta tiene un autor y un autor varias receatas 1-n


        Autor autorAlmacenado = Autor.findAuthorByNameAndSurname(autor.getNombre(), autor.getApellidos());

        if(autorAlmacenado !=null){

           receta.setAutor(autorAlmacenado);//guardamos relacion 1-n de autores con receta (una receta solo es de un autor, y un autor tiene n recetas
        }
        else{

            autor.save();
            receta.setAutor(autor); //idem guardamos la relacion entre autor nuevo y la receta creada
        }



 }

    /**
     * Metodo que procesa la entrada por POST para dar de alta la posicion de una receta validando si ya existe una receta en una posicion del recetario
     * (es un "poco" forzada esta relacion porque se podria hacer mas facil usando un campo en la tabla recipe, pero para completar el ejercicio creo una relacion OneToOne)
     * @param receta
     */
    private int procesaPosicion(Recipe receta){

        Posicion posicion = receta.getPosicion();


        Posicion difInBBDD = Posicion.findPosById(posicion.getIdPosicion());

        if (difInBBDD!=null){

            //ya existe una receta en esa posicion. Violamos la relacion 1 a 1 y por tanto retornamos
            return -1;
        }
        else{

            posicion.setIdPosicion(posicion.getIdPosicion());
            posicion.save();
            receta.setPosicion(posicion);//guardamos relacion uno a uno entre posicion de nueva creacion y receta

        }


        return 0;

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
            posicion.setIdPosicion(new Long(n));
            receta.setComplejidad("DIFICIL");

            receta.setPosicion(posicion);
            receta.setNombre("recetaFake-" + i);



            for (int k = 0; k < receta.getIngredientes().size(); k++) {

                RecipeIngredients recIng = new RecipeIngredients();

                Ingredients ingrediente = receta.getIngredientes().get(k);

                recIng.setIdIngrediente(ingrediente.getIdIngrediente());
                recIng.setIdReceta(receta.getIdReceta());
                recIng.setCantidad(i);
                // recIng.save();

            }

            Autor autor = new Autor();
            autor.setApellidos("fakeSurname");
            autor.setNombre("fakeName");

            receta.setAutor(autor);

            listaFakeRecetas.add(receta);

        }

    return listaFakeRecetas;
    }
}
