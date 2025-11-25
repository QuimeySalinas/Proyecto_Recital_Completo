package services;

import org.junit.Test;
import static org.junit.Assert.*;
import domain.*;
import java.util.*;

public class ContratacionServiceTest {

    @Test
    public void contratarParaCancionEligeArtistaMasBarato() {
        Recital r = new Recital();

        Artista a1 = new Artista();
        a1.setNombre("A1");
        a1.agregarRol("Guitarra");
        a1.setCostoBase(100.0);

        Artista a2 = new Artista();
        a2.setNombre("A2");
        a2.agregarRol("Guitarra");
        a2.setCostoBase(80.0);

        List<Artista> contratados = new ArrayList<>();
        contratados.add(a1); contratados.add(a2);
        r.setContratados(contratados);

        Cancion c = new Cancion();
        Map<String,Integer> roles = new HashMap<>();
        roles.put("Guitarra", 1);
        c.setRoles(roles);

        ContratacionService cs = new ContratacionService();
        cs.contratarParaCancion(r, c);

        List<AsignacionRol> asigns = c.getAsignaciones();
        System.out.println("Asignaciones para la cancion:");
        for(AsignacionRol ar: asigns){
            System.out.println(" - rol=" + ar.getRol() + ", artista=" + ar.getArtista().getNombre());
        }
        assertEquals(1, asigns.size());
        assertEquals("A2", asigns.get(0).getArtista().getNombre());
    }
}
