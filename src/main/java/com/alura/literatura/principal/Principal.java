package com.alura.literatura.principal;

import com.alura.literatura.model.*;
import com.alura.literatura.repository.AutorRepository;
import com.alura.literatura.repository.LibroRepository;
import com.alura.literatura.service.ConsumoAPI;
import com.alura.literatura.service.ConvierteDatos;

import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;


public class Principal {
    private Scanner teclado = new Scanner(System.in);
    private ConsumoAPI consumoApi = new ConsumoAPI();
    private final String URL_BASE = "https://gutendex.com/books/?search=";
    private ConvierteDatos conversor = new ConvierteDatos();
    private LibroRepository repositoryLibro;
    private AutorRepository repositoryAutor;
    private List<Autor> autores;
    private List<Libro> libros;
    private String blue = "\033[34m";
    private String reset = "\u001B[0m";
    private String red = "\033[31m";

    public Principal(LibroRepository repositoryLibro, AutorRepository repositoryAutor) {
        this.repositoryLibro = repositoryLibro;
        this.repositoryAutor = repositoryAutor;
    }

    public void muestraElMenu() {
        var opcion = -1;
        System.out.println("********** Menu Principal ********** ");
        while (opcion != 0) {
            var menu = blue + """
                                       
                    1 - Buscar libros por título
                    2 - Mostrar libros registrados
                    3 - Mostrar autores registrados
                    4 - Autores vivos en determinado año
                    5 - Buscar libros por idioma
                    6 - Top 10 libros más descargados
                    7 - Libro más descargado y menos descargado 
                                        
                    0 - Salir                   
                    """ + reset;
            System.out.println(menu);
            System.out.println(""" 
                    ********************************************
                    Ingrese el nùmero de la opcion a realizar """);
            while (!teclado.hasNextInt()) {
                System.out.println("¡Opcion no Valida, ingrese una de las opciones que estan disponibles en el menú!");
                teclado.nextLine();
            }
            opcion = teclado.nextInt();
            teclado.nextLine();
            switch (opcion) {
                case 1:
                    buscarLibro();
                    break;
                case 2:
                    mostrarLibros();
                    break;
                case 3:
                    mostrarAutores();
                    break;
                case 4:
                    autoresVivosPorAnio();
                    break;
                case 5:
                    buscarLibroPorIdioma();
                    break;
                case 6:
                    top10LibrosMasDescargados();
                    break;
                case 7:
                    rankingLibro();
                    break;
                case 0:
                    System.out.println("Saliendo de la aplicación");
                    break;
                default:
                    System.out.printf("Opción inválida\n");
            }
        }
    }

    private DatosBusqueda getBusqueda() {
        System.out.println("Escribe el nombre del libro: ");
        var nombreLibro = teclado.nextLine();
        var json = consumoApi.obtenerDatos(URL_BASE + nombreLibro.replace(" ", "%20"));
        DatosBusqueda datos = conversor.obtenerDatos(json, DatosBusqueda.class);
        return datos;

    }

    private void buscarLibro() {
        DatosBusqueda datosBusqueda = getBusqueda();
        if (datosBusqueda != null && !datosBusqueda.resultado().isEmpty()) {
            DatosLibros primerLibro = datosBusqueda.resultado().get(0);


            Libro libro = new Libro(primerLibro);
            System.out.println("****** Detalle Libro ******");
            System.out.println(libro);
            System.out.println("***************************");

            Optional<Libro> libroExiste = repositoryLibro.findByTitulo(libro.getTitulo());
            if (libroExiste.isPresent()) {
                System.out.println("\n¡Libro ya esta registrado!\n");
            } else {

                if (!primerLibro.autor().isEmpty()) {
                    DatosAutor autor = primerLibro.autor().get(0);
                    Autor autor1 = new Autor(autor);
                    Optional<Autor> autorOptional = repositoryAutor.findByNombre(autor1.getNombre());

                    if (autorOptional.isPresent()) {
                        Autor autorExiste = autorOptional.get();
                        libro.setAutor(autorExiste);
                        repositoryLibro.save(libro);
                    } else {
                        Autor autorNuevo = repositoryAutor.save(autor1);
                        libro.setAutor(autorNuevo);
                        repositoryLibro.save(libro);
                    }

                    Integer numeroDescargas = libro.getNumero_descargas() != null ? libro.getNumero_descargas() : 0;
                    System.out.println("*******  Libro ******* ");
                    System.out.printf("Titulo: %s%nAutor: %s%nIdioma: %s%nNumero de Descargas: %s%n",
                            libro.getTitulo(), autor1.getNombre(), libro.getLenguaje(), libro.getNumero_descargas());
                    System.out.println("***********************\n");
                } else {
                    System.out.println("Autor Desconocido");
                }
            }
        } else {
            System.out.println("¡libro no encontrado!");
        }
    }

    private void mostrarLibros() {
        libros = repositoryLibro.findAll();
        libros.stream()
                .forEach(System.out::println);
    }

    private void mostrarAutores() {
        autores = repositoryAutor.findAll();
        autores.stream()
                .forEach(System.out::println);
    }

    private void autoresVivosPorAnio() {
        System.out.println("Ingresa el año del que desea buscar autores: ");
        var anio = teclado.nextInt();
        autores = repositoryAutor.listaAutoresVivosPorAnio(anio);
        autores.stream()
                .forEach(System.out::println);
    }

    private List<Libro> datosBusquedaLenguaje(String idioma) {
        var dato = Idioma.fromString(idioma);
        System.out.println("Idioma buscado: " + dato);

        List<Libro> libroPorIdioma = repositoryLibro.findByLenguaje(dato);
        return libroPorIdioma;
    }

    private void buscarLibroPorIdioma() {
        System.out.println("********** Menu Idioma ********** ");

        var opcion = -1;
        while (opcion != 0) {
            var opciones = red + """
                    1. en - Ingles
                    2. es - Español
                    3. fr - Francés
                    4. pt - Portugués
                                        
                    0. Regresar al menu principal
                                        
                    """ + reset;
            System.out.println(opciones);
            System.out.println("Selecciona el idioma que deseas buscar:");

            while (!teclado.hasNextInt()) {
                System.out.println("¡Opcion inválida, ingrese una de las opciones que están disponibles en el menú");
                teclado.nextLine();
            }
            opcion = teclado.nextInt();
            teclado.nextLine();
            switch (opcion) {
                case 1:
                    List<Libro> librosEnIngles = datosBusquedaLenguaje("[en]");
                    librosEnIngles.forEach(System.out::println);
                    break;
                case 2:
                    List<Libro> librosEnEspanol = datosBusquedaLenguaje("[es]");
                    librosEnEspanol.forEach(System.out::println);
                    break;
                case 3:
                    List<Libro> librosEnFrances = datosBusquedaLenguaje("[fr]");
                    librosEnFrances.forEach(System.out::println);
                    break;
                case 4:
                    List<Libro> librosEnPortugues = datosBusquedaLenguaje("[pt]");
                    librosEnPortugues.forEach(System.out::println);
                    break;
                case 0:
                    return;
                default:
                    System.out.println("Ningún idioma seleccionado");
            }
        }
    }

    private void top10LibrosMasDescargados() {
        List<Libro> topLibros = repositoryLibro.top10LibrosMasDescargados();
        topLibros.forEach(System.out::println);
    }

    private void rankingLibro() {
        libros = repositoryLibro.findAll();
        IntSummaryStatistics est = libros.stream()
                .filter(l -> l.getNumero_descargas() > 0)
                .collect(Collectors.summarizingInt(Libro::getNumero_descargas));

        Libro libroMasDescargado = libros.stream()
                .filter(l -> l.getNumero_descargas() == est.getMax())
                .findFirst()
                .orElse(null);

        Libro libroMenosDescargado = libros.stream()
                .filter(l -> l.getNumero_descargas() == est.getMin())
                .findFirst()
                .orElse(null);
        System.out.println("********************************************************");
        System.out.printf("%nLibro más descargado: %s%nNúmero de descargas: " +
                        "%d%n%nLibro menos descargado: %s%nNúmero de descargas: " +
                        "%d%n%n", libroMasDescargado.getTitulo(), est.getMax(),
                libroMenosDescargado.getTitulo(), est.getMin());
        System.out.println("********************************************************");
    }
}
