package domain;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;

public class ArtistaTest {

    @Test
    public void costoParaDevuelveMitadSiComparteBandaBase() {
        Artista a = new Artista();
        a.setCostoBase(100.0);
        a.agregarBanda("BandA");

        Set<String> bandasBase = new HashSet<>();
        bandasBase.add("BandA");

        double costo = a.costoPara(bandasBase);
        System.out.println("Costo con banda base compartida: " + costo);
        assertEquals(50.0, costo, 0.001);
    }

    @Test
    public void costoParaDevuelveCompletoSiNoComparteBanda() {
        Artista a = new Artista();
        a.setCostoBase(120.0);
        a.agregarBanda("BandX");

        Set<String> bandasBase = new HashSet<>();
        bandasBase.add("BandY");

        double costo = a.costoPara(bandasBase);
        System.out.println("Costo sin banda base compartida: " + costo);
        assertEquals(120.0, costo, 0.001);
    }
}
