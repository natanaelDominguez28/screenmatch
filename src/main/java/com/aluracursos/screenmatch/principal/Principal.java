package com.aluracursos.screenmatch.principal;

import ch.qos.logback.core.encoder.JsonEscapeUtil;
import com.aluracursos.screenmatch.model.*;
import com.aluracursos.screenmatch.repository.SerieRepository;
import com.aluracursos.screenmatch.service.ConsumoAPI;
import com.aluracursos.screenmatch.service.ConvierteDatos;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class Principal {

    private Scanner teclado = new Scanner(System.in);
    private ConsumoAPI consumoAPI = new ConsumoAPI();
    private final String URL_BASE = "https://www.omdbapi.com/?t=";
    private final String API_KEY = "&apikey=1b93e0a3";
    private ConvierteDatos conversor = new ConvierteDatos();
    private List<DatosSerie> datosSeries = new ArrayList<>();
    private SerieRepository repositorio;
    private List<Serie> series;
    private Optional<Serie> serieBuscada;

    public Principal(SerieRepository repository) {
        this.repositorio = repository;
    }

    public void muestraElMenu() {
        var opcion = -1;
        while (opcion != 0) {
            var menu = """
                    1 - Buscar series
                    2 - Buscar episodios
                    3 - Mostrar series buscadas
                    4 - Buscar series por titulo
                    5 - Top 5 mejores series
                    6 - Buscar series por categoria
                    7 - Filtrar series
                    8 - Buscar episodios por titulo
                    9 - Top 5 episodios por serie
                    
                    0 - Salir
                    """;
            System.out.println(menu);
            opcion = teclado.nextInt();
            teclado.nextLine();

            switch (opcion) {
                case 1:
                    buscarSerieWeb();
                    break;
                case 2:
                    buscarEpisodioPorSerie();
                    break;
                case 3:
                    mostrarSeriesBuscadas();
                    break;
                case 4:
                    buscarSeriePorTitulo();
                    break;
                case 5:
                    buscarTop5Series();
                    break;
                case 6:
                    buscarSeriePorCategoria();
                    break;
                case 7:
                    filtrarSeriesPorTemporadaYEvaluacion();
                    break;
                case 8:
                    buscarEpisodiosPorTitulo();
                    break;
                case 9:
                    buscarTop5episodios();
                    break;
                case 0:
                    System.out.println("Cerrando la aplicación...");
                    break;
                default:
                    System.out.println("Opción inválida");
            }
        }
    }



    private DatosSerie getDatosSerie() {
        System.out.println("Por favor escribe el nombre de la serie que deseas buscar:");
        //busco los datos generales de las series
        var nombreSerie = teclado.nextLine();
        var json = consumoAPI.obtenerDatos(URL_BASE + nombreSerie.replace(" ","+") + API_KEY);
        System.out.println(json);
        DatosSerie datos = conversor.obtenerDatos(json, DatosSerie.class);
        return datos;
    }

    private void buscarEpisodioPorSerie() {
//        DatosSerie datosSerie = getDatosSerie();
        mostrarSeriesBuscadas();
        System.out.println("Escribe el nombre de la serie de la cual quiere ver los episodios:");
        var nombreSerie = teclado.nextLine();

        Optional<Serie> serie = series.stream()
                .filter(s -> s.getTitulo().toLowerCase().contains(nombreSerie.toLowerCase()))
                .findFirst();

        if (serie.isPresent()) {
            var serieEncontrada = serie.get();

            //Episodios por temporada
            List<DatosTemporadas> temporadas = new ArrayList<>();

            for (int i = 1; i <= serieEncontrada.getTotalDeTemporadas(); i++) {
                var json = consumoAPI.obtenerDatos(URL_BASE + serieEncontrada.getTitulo().replace(" ", "+") + "&Season=" + i + API_KEY);
                DatosTemporadas datosTemporada = conversor.obtenerDatos(json, DatosTemporadas.class);
                temporadas.add(datosTemporada);
            }
            temporadas.forEach(System.out::println);
            List<Episodio> episodios = temporadas.stream()
                    .flatMap(d -> d.episodios().stream()
                                        .map(e -> new Episodio(d.numero(), e)))
                                        .collect(Collectors.toList());

            serieEncontrada.setEpisodios(episodios);
            repositorio.save(serieEncontrada);
        }


    }

    private void buscarSerieWeb() {
        DatosSerie datos = getDatosSerie();
        Serie serie = new Serie(datos);
        repositorio.save(serie);
        //datosSeries.add(datos);
        System.out.println(datos);
    }

    private void mostrarSeriesBuscadas() {
//        List<Serie> series = new ArrayList<>();
//        series = datosSeries.stream()
//                .map(d -> new Serie(d))
//                .collect(Collectors.toList());
        series = repositorio.findAll();
        series.stream()
                .sorted(Comparator.comparing(Serie::getGenero))
                .forEach(System.out::println);
    }

    private void buscarSeriePorTitulo(){
        System.out.println("Escribe el titulo de la serie que deseas buscar: ");
        var nombreSerie = teclado.nextLine();
        serieBuscada = repositorio.findByTituloContainsIgnoreCase(nombreSerie);

        if (serieBuscada.isPresent()) {
            System.out.println("La serie buscada es: "+serieBuscada.get());
        } else {
            System.out.println("Serie no encontrada");
        }
    }

    private void buscarTop5Series() {
        List<Serie> topSeries = repositorio.findTop5ByOrderByEvaluacionDesc();
        topSeries.forEach(s ->
                System.out.println("Serie: "+s.getTitulo()+" Evaluacion: "+s.getEvaluacion()));
    }

    private void buscarSeriePorCategoria() {
        System.out.println("Escriba el genero/categoria de la serie que desea buscar:");
        var genero = teclado.nextLine();
        var categoria = Categoria.fromEspanol(genero);
        List<Serie> seriesPorCategoria = repositorio.findByGenero(categoria);
        System.out.println("Las series de la categoria "+genero);
        seriesPorCategoria.forEach(System.out::println);

    }

    private void filtrarSeriesPorTemporadaYEvaluacion() {
        System.out.println("¿Filtrar séries con cuántas temporadas? ");
        var totalTemporadas = teclado.nextInt();
        teclado.nextLine();
        System.out.println("¿Con evaluación a partir de cuál valor? ");
        var evaluacion = teclado.nextDouble();
        teclado.nextLine();
        List<Serie> filtroSeries = repositorio.seriesPorTemporadaYevaluacion(totalTemporadas, evaluacion);
        System.out.println("*** Series filtradas ***");
        filtroSeries.forEach(s ->
                System.out.println(s.getTitulo() + "  - evaluacion: " + s.getEvaluacion()));
    }

    private void buscarEpisodiosPorTitulo(){
        System.out.println("Escribe el nombre del episodio que deseas buscar: ");
        var nombreEpisodio = teclado.nextLine();
        List<Episodio> episodiosEncontrados = repositorio.episodiosPorNombre(nombreEpisodio);
        episodiosEncontrados.forEach(e -> System.out.printf("Serie: %s Temporada %s Episodio %s Evaluacion %s\n",
                e.getSerie().getTitulo(), e.getTemporada(), e.getNumeroEpisodio(), e.getEvaluacion()));
    }

    private void buscarTop5episodios(){
        buscarSeriePorTitulo();
        if (serieBuscada.isPresent()) {
            Serie serie = serieBuscada.get();
            List<Episodio> topEpisodios = repositorio.top5episodios(serie);
            topEpisodios.forEach(e -> System.out.printf("Serie: %s - Temporada %s - Numero %s - Episodio %s - Evaluacion %s\n",
                    e.getSerie().getTitulo(), e.getTemporada(), e.getNumeroEpisodio(), e.getTitulo(), e.getEvaluacion()));
        }
    }

}



        //muestro los datos de todas las temporadas almacenadas
        //temporadas.forEach(System.out::println);

        //Mostrar solo el título de los episodios de las temporadas
        /*for (int i = 0; i < datos.totalDeTemporadas(); i++) {
            List<DatosEpisodio> episodiosTemporada = temporadas.get(i).episodios();
            for (int j = 0; j < episodiosTemporada.size(); j++) {
                System.out.println(episodiosTemporada.get(j).titulo());
            }
        }*/
        //expresiones lambda
        //recorre la lista de temporadas y dentro de ella la lista de episodios y muestra el título de ellos
        //temporadas.forEach(t -> t.episodios().forEach(e -> System.out.println(e.titulo())));

        //Convertir todas las informaciones a una lista del tipo DatosEpisodio
//        List<DatosEpisodio> datosEpisodios = temporadas.stream()
//                .flatMap(t -> t.episodios().stream())
//                .collect(Collectors.toList());//permite agregar nuevos elementos a la lista

                //.toList(); convierte una lista inmutable, no permite agregar nuevos elementos

        //top 5 episodios
//        System.out.println("Top 5 episodios");
//        datosEpisodios.stream()
//                .filter(e -> !e.evaluacion().equalsIgnoreCase("N/A"))//solo va a traer los que no tengan "N/A" como evaluación
//                .peek(e -> System.out.println( "Primer Filtro (N/A)"+e))
//                .sorted(Comparator.comparing(DatosEpisodio::evaluacion).reversed())//ordena del último al primero
//                .peek(e -> System.out.println( "Segundo Filtro ordenación (M>m)"+e))
//                .map(e -> e.titulo().toUpperCase())
//                .peek(e -> System.out.println( "Tercer Filtro Mayúscula (m>M)"+e))
//                .limit(5)//especifica que solo quiere 5 elementos
//                .forEach(System.out::println);//muestra los elementos

//        //convirtiendo los datos a una lista del tipo Episodio
//        List<Episodio> episodios = temporadas.stream()
//                .flatMap(t -> t.episodios().stream()
//                        .map(d -> new Episodio(t.numero(),d)))
//                .collect(Collectors.toList());
//
//        episodios.forEach(System.out::println);

        //Busqueda de episodios a partir de x año
//        System.out.println("Por favor indica el año a partir del cual deseas ver los episodios");
//        var fecha = teclado.nextInt();
//        teclado.nextLine();

        //LocalDate fechaBusqueda = LocalDate.of(fecha, 1, 1);

        //DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy"); //formatea un tipo de fecha para que se vea en el patrón día/mes/año

        //me va a devolver todos los episodios desde una fecha ingresada por el usuario
//        episodios.stream()
//                .filter(e -> e.getFechaDeLanzamiento() != null && e.getFechaDeLanzamiento().isAfter(fechaBusqueda))
//                .forEach(e -> System.out.println(
//                        "Temporada " + e.getTemporada() +
//                                " Episodio " + e.getTitulo() +
//                                " Fecha de Lanzamiento " + e.getFechaDeLanzamiento().format(dtf)
//                ));

        //Busco episodios por pedazo del título
//        System.out.println("Por favor escriba el titulo del episodio que desea ver: ");
//        var pedazoTitulo = teclado.nextLine();
//        Optional<Episodio> episodioBuscado = episodios.stream()
//                .filter(e -> e.getTitulo().toUpperCase().contains(pedazoTitulo.toUpperCase()))
//                .findFirst();//encuentra el primer elemento que coincida con el elemento buscado
//        //si se encuentra el episodio, muestro sus datos
//        if(episodioBuscado.isPresent()){
//            System.out.println(" Episodio encontrado");
//            System.out.println("Los datos son: "+episodioBuscado.get());
//        } else {
//            System.out.println("Episodio no encontrado");
//        }

//        //Mapas
//        Map<Integer,Double> evaluacionesPorTemporadas = episodios.stream()
//                .filter(e -> e.getEvaluacion() > 0.0)
//                .collect(Collectors.groupingBy(Episodio::getTemporada,
//                        Collectors.averagingDouble(Episodio::getEvaluacion)));
//
//        System.out.println(evaluacionesPorTemporadas);
//
//        //Recolección de datos estadísticos
//        DoubleSummaryStatistics est = episodios.stream()
//                .filter(e -> e.getEvaluacion() > 0.0)
//                .collect(Collectors.summarizingDouble(Episodio::getEvaluacion));
//        System.out.println("Media de las evaluaciones: " + est.getAverage());
//        System.out.println("Episodio mejor evaluado: " + est.getMax());
//        System.out.println("Episodio peor evaluado: " + est.getMin());
//}
