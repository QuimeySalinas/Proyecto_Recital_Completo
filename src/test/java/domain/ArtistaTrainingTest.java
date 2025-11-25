package domain;

import org.junit.Test;
import static org.junit.Assert.*;

public class ArtistaTrainingTest {

    @Test
    public void entrenarAumentaCostoYAgregaRol() {
        Artista a = new Artista();
        a.setNombre("T1");
        a.setCostoBase(100.0);

        a.entrenar("Piano");
        assertTrue(a.puedeTocar("Piano"));
        assertEquals(150.0, a.getCostoBase(), 0.001);
        System.out.println("Costo tras entrenar Piano: " + a.getCostoBase());

        a.entrenar("Teclado");
        assertTrue(a.puedeTocar("Teclado"));
        assertEquals(225.0, a.getCostoBase(), 0.001);
        System.out.println("Costo tras entrenar Teclado: " + a.getCostoBase());
    }

    @Test(expected = IllegalStateException.class)
    public void noSePuedeEntrenarSiTieneAsignaciones() {
        Artista a = new Artista();
        a.setCostoBase(100.0);
        a.setAsignaciones(1);
        a.entrenar("Voz");
    }

    @Test(expected = IllegalStateException.class)
    public void noSePuedeEntrenarSiEsBase() {
        Artista a = new Artista();
        a.setCostoBase(100.0);
        a.setBase(true);
        a.entrenar("Voz");
    }
}
