package com.aluracursos.screenmatch;

import com.aluracursos.screenmatch.model.DatosEpisodio;
import com.aluracursos.screenmatch.model.DatosSerie;
import com.aluracursos.screenmatch.model.DatosTemporadas;
import com.aluracursos.screenmatch.principal.EjemploStreams;
import com.aluracursos.screenmatch.principal.Principal;
import com.aluracursos.screenmatch.repository.SerieRepository;
import com.aluracursos.screenmatch.service.ConsumoAPI;
import com.aluracursos.screenmatch.service.ConvierteDatos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
public class ScreenmatchApplication implements CommandLineRunner {

	@Autowired //se utiliza para la inyección de dependencias
	private SerieRepository serieRepository;

	public static void main(String[] args) {
		SpringApplication.run(ScreenmatchApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		/*var consumoAPI = new ConsumoAPI();
		//devuelve los datos de una serie
		var json = consumoAPI.obtenerDatos("https://www.omdbapi.com/?t=game+of+thrones&apikey=1b93e0a3");
		//devuelve una imágen aleatoria de un café
		//var json = consumoAPI.obtenerDatos("https://coffee.alexflipnote.dev/random.json");
		System.out.println(json);

		ConvierteDatos conversor = new ConvierteDatos();
		//va a convertir los datos traídos por la api y asignarlos en las variables declaradas para esos valores
		var datos = conversor.obtenerDatos(json, DatosSerie.class);
		System.out.println(datos);
		//obtengo los datos del episodio 1 de la serie
		json= consumoAPI.obtenerDatos("https://www.omdbapi.com/?t=game+of+thrones&Season=1&episode=1&apikey=1b93e0a3");
		DatosEpisodio episodios = conversor.obtenerDatos(json, DatosEpisodio.class);
		System.out.println(episodios);*/
		Principal principal = new Principal(serieRepository);
		principal.muestraElMenu();
		/*EjemploStreams ejemploStreams = new EjemploStreams();
		ejemploStreams.muestraEjemplo();*/

	}
}
