package com.aluracursos.screenmatch.principal;

import java.util.Arrays;
import java.util.List;

public class EjemploStreams {
    public void muestraEjemplo(){
        List<String> nombres = Arrays.asList("Brenda","Luis","Maria Fernanda","Eric","Genesys");
        //ordena una lista y muestra su contenido
        nombres.stream()
                .sorted()//ordena los elementos de la lista
                .limit(4)//limita para que muestre solo los primeros 4
                .filter(n -> n.startsWith("L"))//busca el nombre que comience con la letra L
                .map(n -> n.toUpperCase())//lo convierte en mayúscula
                .forEach(System.out::println);
    }
}
