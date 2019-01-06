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
import views.xml.recetas;

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
 * .- Listar recetas
 * .- Dar de alta una receta
 * .- Modificar receta
 * .- Borrar receta
 *
 */

public class RecipeController extends Controller {

    @Inject
    FormFactory formFactory;

    @Inject
    private SyncCacheApi cache;

    private static List<Recipe> listaRecetas = new ArrayList<Recipe>();

    static final String XML = "XML";
    static final String JSON = "JSON";

    //por defecto JSON por si no se indicaran cabeceras en la llamada. El contenido del body solo lo aceptamos en json y la respuesta puede ser xml o json
    static String flagResponse = JSON;
    static String flagBody = JSON;

    static String flagLang = "ES"; //por defecto en castellano

    int flagErrorValidacion = 0; // 0 - todo ok, 1- error de tipo en parametro

    static Messages messages;

    /**
     * Metodo que consulta en bbdd las recetas creadas y devuelve informacion en json o xml segun Accept del header de la invocacion (json por defecto)
     * @return : listado de recetas
     */
    public Result retrieveRecipes() {

        messages = Http.Context.current().messages();

        //Revisar cabeceras de cliente para ver que formato usa en el body y que acepta como respuesta
        chequeaCabeceraRequest(request());


        //buscamos todas las recetas en bbdd y sus relaciones con los ingredientes (todo en un paso)

        List<Recipe> listaEnCache = cache.get("listaRecetas");

        if( listaEnCache != null){

            listaRecetas =  listaEnCache;
        }
        else{

            listaRecetas = Recipe.findAllRecipes();
            cache.set("listaRecetas",listaRecetas);
        }


        buscaInfoRecetas(listaRecetas);

        System.out.println("Tamaño del recetario--> " + listaRecetas.size() + " recetas guardadas");


        if(flagResponse.equals(XML)){

            System.out.println("type xml");
            //fakeReceta(); //codigo de prueba que genera recetas
            return ok(recetas.render(listaRecetas));
        }
        else{

            if(flagResponse.equals(JSON)){

                System.out.println("type json");
                //fakeReceta(); //codigo de prueba que genera recetas
                JsonNode jsonNodeListaRecetas = Json.toJson(listaRecetas);
                return ok(jsonNodeListaRecetas);

            }
        }

        return Results.noContent();
    }

    /**
     * Metodo para crear recetas
     * @return JSON/XML con la receta creada
     */
    public Result createRecipe (){

        messages = Http.Context.current().messages();

        chequeaCabeceraRequest(request());

        Form<Recipe> form = formFactory.form(Recipe.class).bindFromRequest();

        try{

            Recipe receta  = new Recipe();
            receta = form.get(); //"bindo" el formulario de entrada a un objeto receta



            /*Para poder dar de alta una nueva receta se debe cumplir que:

                1.- Existan los ingredientes a usar (si no existen se crean nuevos)
                2.- Exista una dificultad definida (si no existe se crea nueva)
                3.- Exista un autor de la receta (si no existe se crea nuevo)

                Por esto lo primera es usar finders para ver si en bbdd tenemos valores ya guardados
            */

            validaDatosEntrada(receta);


            switch (flagErrorValidacion){

                case -1:
                    JsonNode nodoRespuesta1 = Json.toJson(messages.at("case-1FlagValidacion") + receta.getId());
                    return badRequest(nodoRespuesta1);
                case 2:
                    JsonNode nodoRespuesta2 = Json.toJson(messages.at("case2FlagValidacion") + receta.getIngredientes() + receta.getId());
                    return badRequest(nodoRespuesta2);
                case 3:
                    JsonNode nodoRespuesta3 = Json.toJson(messages.at("case3FlagValidacion") + receta.getId());
                    return badRequest(nodoRespuesta3);
                case 4:
                    JsonNode nodoRespuesta4 = Json.toJson(messages.at("case4FlagValidacion") + receta.getId() + receta.getPosicion());
                    return badRequest(nodoRespuesta4);
                case 5 : //
                    JsonNode nodoRespuesta5 = Json.toJson(messages.at("case5FlagValidacion") + receta.getPosicion());
                    return badRequest(nodoRespuesta5);
                case 7 : //
                    JsonNode nodoRespuesta7 = Json.toJson(messages.at("case7FlagValidacion") + receta.getId() + receta.getPosicion() + receta.getIngredientes() );
                    return badRequest(nodoRespuesta7);
                case 8 : //
                    JsonNode nodoRespuesta8 = Json.toJson(messages.at("case8FlagValidacion") + receta.getPosicion() + receta.getPosicion());
                    return badRequest(nodoRespuesta8);


            }



            Recipe recipeByName = Recipe.findRecipeByName(receta.getNombre());

            if(recipeByName==null){

                //validacion manual de que los ingredientes asociados a la receta vengan con cantidad.
                //El valor en el modelo está marcado como transient porque lo quiero almancenar en la relacion n-m y no en el objeto ingrediente

                if(!validaCantidadesIngredientes(receta.getIngredientes())){

                    return ok(views.html.ingredienteErrCantidad.render(messages));
                }


                //1.-
                int flag = procesaPosicion(receta);

                if(flag == -1 ){
                    return ok(views.html.posicionRepe.render(messages));
                }

                //2.-
                procesaAutor(receta);

                //3.-
                procesaIngredientes(receta);

                receta.save(); //almaceno la nueva receta porque si estamos aqui "todo" ha ido bien

                cache.remove("listaRecetas"); //ya no vale la cache para siguientes consultas asi que anulamos
                cache.remove("listaAutores"); //ya no vale la cache para siguientes consultas asi que anulamos
                cache.remove("listaIngredientes"); //ya no vale la cache para siguientes consultas asi que anulamos

                switch (flagResponse){ //revisamos como acepta la respuesta el cliente

                    case XML:
                        return ok(views.xml.receta.render(receta)); //devolvemos info de receta en xml

                    case JSON:
                        JsonNode jsonNode = request().body().asJson();
                        System.out.println("doc jsonNode body: " + jsonNode.toString());
                        System.out.println(jsonNode);
                        return ok(jsonNode);//devolvemos info de receta en json

                    default:
                        return Results.badRequest();//en otro caso devolvemos error en request

                }

            }

            else{
                return ok(views.html.recetaRepe.render(messages)); //ya existe una receta guardada en bbdd con el mismo nombre
            }



        }catch (IllegalStateException ex){
            System.out.println("error de bindado del formulario de entrada, es posible que campos required no estén siendo informados");
            ex.printStackTrace();
            return badRequest(views.html.recetasErr.render(messages));
        }
    }


    /**
     * Borrado de receta por id
     * @param id
     * @return Json/xml con el resultado de la operacion
     */
    public Result deleteRecipe(Long id){

        messages = Http.Context.current().messages();

        chequeaCabeceraRequest(request());


        Recipe recipeById = Recipe.findRecipeById(id);
        if(recipeById!=null){
            System.out.println("Borrado correcto: " + recipeById.getId() + " // " + recipeById.getNombre());
            borraRelacionIngredientesReceta(id);
            recipeById.delete();
            cache.remove("listaRecetas"); //ya no vale la cache para siguientes consultas asi que anulamos
            return ok(views.html.recetaBorrada.render(recipeById,messages));
        }
        else{
            System.out.println("no se ha encontrado el id de receta: " + id);
            return ok(views.html.recetaNoEncontrada.render(id,messages));
        }

    }


    /**
     * Actualizacion del nombre de receta por id
     * @param id, nuevo nombre de la receta
     * @return Json/xml con el resultado de la operacion
     */
    public Result updateRecipe (Long id, String newRecipeName){

        messages = Http.Context.current().messages();

        chequeaCabeceraRequest(request());


        Recipe recipeById = Recipe.findRecipeById(id);

        if(recipeById!=null){

            recipeById.setNombre(newRecipeName);
            recipeById.update();
            cache.remove("listaRecetas"); //ya no vale la cache para siguientes consultas asi que anulamos
            System.out.println("Update correcto:  " + recipeById.getNombre());
            return ok(views.html.recetaActualizada.render(recipeById,messages));

        }
        else{

            System.out.println("Imposible actualizar - NO se ha encontrado la receta con id: " + id);
            return ok(views.html.recetaNoEncontrada.render(id,messages));
        }


    }
    //**********************************************************
    //////UTILIDADES PARA EL MANEJO y PROCESAMIENTO DE LA ENTRADA//////////////
    //**********************************************************

    /**
     * Metodo de validacion generico para controlar si todos los ingredientes para la receta en creacion vienen con la cantidad indicada
     *
     * @param: lista ingredientes
     * return : true si todo bien, false si algun ingrediente para la receta no trae cantidad
     */
    private boolean validaCantidadesIngredientes(List<Ingredients> listaIngredientes){


        for(Ingredients ingrediente: listaIngredientes ){

            if(ingrediente.getCantidad() > 0 && ingrediente.getCantidad() > 1000 )

                return false;

        }

        return true;
    }

    /**
     * Metodo de validacion generico para controlar longitudes excesivas de campos de autor, valor de posicion (no admitimos mas de 1000 "paginas/posiciones" en nuestro recetario), nombre de ingrediente muy largo...
     *
     * @param: receta de entrada
     *  No devuelve nada porque trabajamos con una variable global
     */

    private void validaDatosEntrada(Recipe receta){

        flagErrorValidacion = 0;


        //validacion del autor
       if(receta.getAutor()!=null && receta.getAutor().getNombre() != null && receta.getAutor().getApellidos()!=null && (receta.getAutor().getNombre().length()>30 || receta.getAutor().getApellidos().length()> 30)) {
            flagErrorValidacion = flagErrorValidacion -1 ;
        }


        // validacion ingredientes
        List<Ingredients> ingredientes = receta.getIngredientes();
        for(Ingredients ingredient : ingredientes){
            if (ingredient.getNombre().length()>25) {
                flagErrorValidacion = flagErrorValidacion + 3;
                break; //con encontrar uno me vale para salir
            }
        }


        ///posicion
        if (receta.getPosicion() !=null && receta.getPosicion().getId() != null && receta.getPosicion().getId().intValue()>1000){
            flagErrorValidacion = flagErrorValidacion + 5;
        }


    }

    /**
     * Metodo que borra las relaciones n-m guardadas entre idRecete e ingredientes
     * Al borrar una receta si no eliminamos la relacion entre idReceta e idIngrediente es posible que al crear otra receta el id se "aproveche" y tengamos lios :-)
     * @param: Recibe el id de la receta en cuestion
     */
    private void borraRelacionIngredientesReceta(Long idReceta){

        /*
        List<RecipeIngredients> ingredientsByIdRecipe = RecipeIngredients.findIngredientsByIdRecipe(new Long(idReceta));


        for(RecipeIngredients ingRec:ingredientsByIdRecipe){

            System.out.println("borrando relacion receta: " + idReceta + " con ingrediente: " + ingRec.getIdIngrediente());
            ingRec.delete();//borro relacion por si luego el id de la receta eliminada se reutiliza
        }
        */

    }




    /**
     * Metodo que se encarga de buscar los ingredientes para cada receta con la relacion n-m que exista asi como su cantidad,
     * buscar el autor de la receta para cada receta con la relacion 1-n que exista y
     * se encarga de buscar la dificultad para cada receta con la relacion 1-1 que exista
     *
     * @param: Recibe una lista de recetas
     *
     */
    private void buscaInfoRecetas(List<Recipe> listaRecetas){


        for(int i=0;i<listaRecetas.size();i++){

            Recipe receta =  listaRecetas.get(i);

            List<Ingredients> ingredientsByIdRecipe = receta.getIngredientes();

            Posicion posicion = new Posicion();
            Autor autor = new Autor();

            //ingredientes
            //ingredientsByIdRecipe = RecipeIngredients.findIngredientsByIdRecipe(recetaBBDD.getId());
            List<Ingredients> ingredientes = procesaRelacionIngredientesReceta(ingredientsByIdRecipe);
            receta.setIngredientes(ingredientes);


            //Posicion
            posicion = Posicion.findPosById(receta.getPosicion().getId());
            receta.setPosicion(posicion);


            //autor
            autor = Autor.findAuthorById(receta.getAutor().getId());
            receta.setAutor(autor);

        }

    }

    /**
     *  Metodo que procesa los ingredientes de la invocacion y los asocia a la receta validando previamente en bbdd para no repetir ingredientes
     * @param ingredientsByIdRecipe
     * @return Lista de relacion de ingredientes usados en una receta
     */
    private List<Ingredients> procesaRelacionIngredientesReceta ( List<Ingredients> ingredientsByIdRecipe ){

        List<Ingredients> listaIngredientesADevolver = new ArrayList<>();

        for (int j=0;j<ingredientsByIdRecipe.size();j++){

            Ingredients recIng = ingredientsByIdRecipe.get(j);

            Ingredients ingredientById = Ingredients.findIngredientById(recIng.getId());

            Ingredients ingrediente = new Ingredients();

            //obtengo nombre e id de la tabla "maestra" de ingredientes (relacion n-m)
            ingrediente.setNombre(ingredientById.getNombre());
            ingrediente.setId(recIng.getId());
            ingrediente.setCantidad(recIng.getCantidad());

            listaIngredientesADevolver.add(ingrediente);
        }

        return listaIngredientesADevolver;
    }


    /**
     * Metodo que procesa la entrada por POST para dar de alta los ingredientes de una receta (el id del ingrediente guardado es el que se usara para la relacion entre receta-ingrediente)
     * @param receta
     */
    private void procesaIngredientes(Recipe receta) {

        //recorremos los posibles ingredientes que hubieran pasado en la llamada
        List<Ingredients> ingredientes = receta.getIngredientes();

        List<Ingredients> auxIngredientes = new ArrayList<Ingredients>();

        for(int i=0;i<ingredientes.size();i++){

            Ingredients ingrediente = ingredientes.get(i);

                //guardo ingrediente con cantidad que se relacionara con una receta
                ingrediente.save();
                auxIngredientes.add(ingrediente);

            }


        receta.setIngredientes(auxIngredientes);

    }


    /**
     * Metodo que procesa la entrada por POST para dar de alta el autor de una receta validando si ya existia en bbdd previamente
     * @param receta
     */
    private void procesaAutor(Recipe receta){

        //Una receta tiene un autor y un autor varias receatas 1-n
        Autor autorAlmacenado = Autor.findAuthorByNameAndSurname(receta.getAutor().getNombre(), receta.getAutor().getApellidos());

        if(autorAlmacenado !=null){

           //guardamos relacion 1-n de autores con receta (una receta solo es de un autor, y un autor tiene n recetas
           receta.setAutor(autorAlmacenado);

        }
        else{ //se debe crear el nuevo autor para la receta

            Autor autorEnPost = receta.getAutor();
            autorEnPost.save();
            receta.setAutor(autorEnPost);
        }



 }

    /**
     * Metodo que procesa la entrada por POST para dar de alta la posicion de una receta validando si ya existe una receta en una posicion del recetario
     * (es un "poco" forzada esta relacion porque se podria hacer mas facil usando un campo en la tabla recipe, pero para completar el ejercicio creo una relacion OneToOne)
     * @param receta
     */
    private int procesaPosicion(Recipe receta){

        Posicion posicion = receta.getPosicion();


        Posicion difInBBDD = Posicion.findPosById(posicion.getId());

        if (difInBBDD!=null){

            //ya existe una receta en esa posicion. Violamos la relacion 1 a 1 y por tanto retornamos
            return -1;
        }
        else{

            posicion.setId(posicion.getId());
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
        //System.out.println("construyendo recetas FAKE");

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
            autor.setId(new Long(n));

            receta.setId(autor.getId());
            receta.setAutor(autor);

            listaFakeRecetas.add(receta);

        }

    return listaFakeRecetas;
    }
}
