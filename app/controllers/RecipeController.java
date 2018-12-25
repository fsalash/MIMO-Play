package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models.*;
import play.data.Form;
import play.data.FormFactory;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;

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

    private static List<Recipe> listaRecetas = new ArrayList<Recipe>();

    static final String XML = "XML";
    static final String JSON = "JSON";

    //por defecto JSON por si no se indicaran cabeceras en la llamada. El contenido del body solo lo aceptamos en json y la respuesta puede ser xml o json
    static String flagResponse = JSON;
    static String flagBody = JSON;

    boolean flagErrorValidacion = false;

    /**
     * Metodo que consulta en bbdd las recetas creadas y devuelve informacion en json o xml segun Accept del header de la invocacion (json por defecto)
     * @return : listado de recetas
     */
    public Result retrieveRecipes() {

        //Revisar cabeceras de cliente para ver que formato usa en el body y que acepta como respuesta
        chequeaCabeceraRequest(request());


        //buscamos todas las recetas en bbdd y sus relaciones con los ingredientes (todo en un paso)
        listaRecetas = Recipe.findAllRecipes();
        List<Recipe> listaRecetasCompleta  = buscaInfoRecetas(listaRecetas);

        System.out.println("Tamaño del recetario--> " + listaRecetasCompleta.size() + " recetas guardadas");


        if(flagResponse.equals(XML)){

            System.out.println("type xml");
            //fakeReceta(); //codigo de prueba que genera recetas
            return ok(views.xml.recetas.render(listaRecetasCompleta));
        }
        else{

            if(flagResponse.equals(JSON)){

                System.out.println("type json");
                //fakeReceta(); //codigo de prueba que genera recetas
                JsonNode jsonNodeListaRecetas = Json.toJson(listaRecetasCompleta);
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

        System.out.println("funcionando createRecipe en RECIPE");

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


            Recipe recipeByName = Recipe.findRecipeByName(receta.getNombre());

            if(recipeByName==null){

                //1.-
                int flag = procesaPosicion(receta);

                //2.-
                procesaIngredientes(receta);

                //3.-
                procesaAutor(receta);

                if (flag<0 || flagErrorValidacion){
                    return ok(views.html.posicionRepe.render()); //salimos porque la posicion donde se quiere guardar la receta está ocupada o porque hay un error de validacion en autor o posicion (controlado por el bind pero por si acaso)
                }

                receta.save(); //almaceno la nueva receta porque todo ha ido bien

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
                return ok(views.html.recetaRepe.render()); //ya existe una receta guardada en bbdd con el mismo nombre
            }



        }catch (IllegalStateException ex){
            System.out.println("error de bindado del formulario de entrada, es posible que campos required no estén siendo informados");
            ex.printStackTrace();
            return badRequest(views.html.recetasErr.render());
        }
    }


    /**
     * Borrado de receta por id
     * @param id
     * @return Json/xml con el resultado de la operacion
     */
    public Result deleteRecipe(Integer id){

        Recipe recipeById = Recipe.findRecipeById(id);
        if(recipeById!=null){
            System.out.println("Borrado correcto: " + recipeById.getIdReceta() + " // " + recipeById.getNombre());
            borraRelacionIngredientesReceta(id);
            recipeById.delete();
            return ok(views.html.recetaBorrada.render(recipeById));
        }
        else{
            System.out.println("no se ha encontrado el id de receta: " + id);
            return ok(views.html.recetaNoEncontrada.render(id));
        }

    }


    /**
     * Actualizacion del nombre de receta por id
     * @param id, nuevo nombre de la receta
     * @return Json/xml con el resultado de la operacion
     */
    public Result updateRecipe (Integer id, String newRecipeName){

        Recipe recipeById = Recipe.findRecipeById(id);

        if(recipeById!=null){

            recipeById.setNombre(newRecipeName);
            recipeById.update();
            System.out.println("Update correcto:  " + recipeById.getNombre());
            return ok(views.html.recetaActualizada.render(recipeById));

        }
        else{

            System.out.println("Imposible actualizar - NO se ha encontrado la receta con id: " + id);
            return ok(views.html.recetaNoEncontrada.render(id));
        }


    }
    //**********************************************************
    //////UTILIDADES PARA EL MANEJO y PROCESAMIENTO DE LA ENTRADA//////////////
    //**********************************************************


    /**
     *
     *
     *
     */
    private void borraRelacionIngredientesReceta(Integer idReceta){

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
           receta.setId(recetaBBDD.getId());
           receta.setNombre(recetaBBDD.getNombre());


            //ingredientes
            ingredientsByIdRecipe = RecipeIngredients.findIngredientsByIdRecipe(recetaBBDD.getIdReceta());
            List<Ingredients> ingredientes = procesaRelacionIngredientesReceta(ingredientsByIdRecipe);
            receta.setIngredientes(ingredientes);


            //Posicion
            posicion = Posicion.findDificultByIdPos(recetaBBDD.getPosicion().getIdPosicion());
            receta.setPosicion(posicion);


            //autor
            autor = Autor.findAuthorByIDReceta(recetaBBDD.getAutor().getId());
            receta.setAutor(autor);


            listaAux.add(receta);
        }

        return listaAux;

    }

    /**
     *
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
     * Metodo que procesa la entrada por POST para dar de alta los ingredientes de una receta (si ya existia se guarda relacion)
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
     * Metodo que procesa la entrada por POST para dar de alta el autor de una receta (si ya existia se guarda relacion)
     * @param receta
     */
    private void procesaAutor(Recipe receta){

        Autor autor= receta.getAutor(); //Una receta tiene un autor y un autor varias receatas 1-n


        if(autor!=null && autor.getNombre() != null && autor.getApellidos()!=null){

            Autor autorAlmacenado = Autor.findAuthorByNameAndSurname(autor.getNombre(), autor.getApellidos());

            if(autorAlmacenado !=null){

               receta.setAutor(autorAlmacenado);//guardamos relacion 1-n de autores con receta (una receta solo es de un autor, y un autor tiene n recetas
            }
            else{

                autor.save();
                receta.setAutor(autor); //idem guardamos la relacion entre autor nuevo y la receta creada
            }

        }
        else{

            //nunca deberiamos llegar aqui porque el bindado automatico habría lanzado excepcion al principio pero marcamos flag por si ocurriera algo inesperado
            flagErrorValidacion = true;
        }

 }

    /**
     * Metodo que procesa la entrada por POST para dar de alta la dificultad de una receta (si ya existia se guarda relacion)
     * @param receta
     */
    private int procesaPosicion(Recipe receta){

        Posicion posicion = receta.getPosicion();


        if (posicion !=null && posicion.getIdPosicion() != null){

            Posicion difInBBDD = Posicion.findDificultByIdPos(posicion.getIdPosicion());

            if (difInBBDD!=null){

                //ya existe una receta en esa posicion. Violamos la relacion 1 a 1 y por tanto retornamos
                return -1;
            }
            else{

                posicion.setIdPosicion(posicion.getIdPosicion());
                posicion.save();
                receta.setPosicion(posicion);//guardamos relacion uno a uno entre posicion de nueva creacion y receta

            }
        }

        else{

            //nunca deberiamos llegar aqui porque el bindado automatico habría lanzado excepcion al principio pero marcamos flag por si ocurriera algo inesperado
            flagErrorValidacion = true;
        }

        return 0;

    }


    /**
     * Chequea el content-type y el formato de respuesta que soporta el cliente que invoca
     * @param req
     */
    public static void chequeaCabeceraRequest(Http.Request req){


        System.out.println("chequeo cabecera  en RECIPECONTROLLER");

        if(req!=null){


            Optional<String> sContent = req.getHeaders().get("Content-Type");
            Optional<String> sAccept = req.getHeaders().get("Accept");


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


            System.out.println("content-type: " +flagBody);
            System.out.println("accept: " +flagResponse);

         }

    }


    /**
     * FAKE. Metodo para ir probando la construccion. Crea un recetario fake v0 (sin autor)
     */
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

            Posicion posicion = new Posicion();
            Random rand = new Random();

            int n = rand.nextInt(5000) + 1;
            posicion.setIdPosicion(new Long(n));
            receta.setComplejidad("DIFICIL");

            posicion.save();

            receta.setPosicion(posicion);
            receta.setNombre("recetaFake-"+i);


            receta.save();




            for(int k=0;k<receta.getIngredientes().size();k++){

                RecipeIngredients recIng = new RecipeIngredients();

                Ingredients ingrediente = receta.getIngredientes().get(k);

                System.out.println("creando relacion receta: " + i + " con ingrediente : " + k);

                recIng.setIdIngrediente(ingrediente.getIdIngrediente());
                recIng.setIdReceta(receta.getIdReceta());
                recIng.setCantidad(i);
                recIng.save();

            }

            listaRecetas.add(receta);

        }


    }
}
