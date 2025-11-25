package services;

import org.junit.Test;
import static org.junit.Assert.*;
import domain.*;
import java.util.*;

public class ContratacionServiceAvailabilityTest {

    @Test
    public void respetaMaxCancionesYEscogeSiguienteMasBarato() {
        Recital r = new Recital();

        Artista a1 = new Artista();
        a1.setNombre("A1");
        a1.agregarRol("Guitarra");
        a1.setCostoBase(100.0);
        a1.setMaxCanciones(1);
        a1.setAsignaciones(1); // ya alcanzó su límite

        Artista a2 = new Artista();
        a2.setNombre("A2");
        a2.agregarRol("Guitarra");
        a2.setCostoBase(120.0);
        a2.setMaxCanciones(2);
        a2.setAsignaciones(0);

        r.setContratados(Arrays.asList(a1, a2));

        Cancion c = new Cancion();
        Map<String,Integer> roles = new HashMap<>();
        roles.put("Guitarra", 1);
        c.setRoles(roles);

        ContratacionService cs = new ContratacionService();
        cs.contratarParaCancion(r, c);

        List<AsignacionRol> asigns = c.getAsignaciones();
        System.out.println("Asignaciones (availability test):");
        for(AsignacionRol ar: asigns){
            System.out.println(" - rol=" + ar.getRol() + ", artista=" + ar.getArtista().getNombre());
        }
        assertEquals(1, asigns.size());
        assertEquals("A2", asigns.get(0).getArtista().getNombre());
        assertEquals(1, a2.getAsignaciones());
    }
}
