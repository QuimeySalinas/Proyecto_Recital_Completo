package domain;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;

public class RecitalTest {

    @Test
    public void rolesFaltantesParaCancionUsaBase() {
        Recital r = new Recital();

        // base tiene un guitarrista
        Artista base = new Artista();
        base.setNombre("BaseG");
        base.agregarRol("Guitarra");
        r.setBase(Arrays.asList(base));

        Cancion c = new Cancion();
        Map<String,Integer> roles = new HashMap<>();
        roles.put("Guitarra", 1);
        roles.put("Bateria", 1);
        c.setRoles(roles);

        Map<String,Integer> faltan = r.rolesFaltantesParaCancion(c);
        System.out.println("Faltantes para la cancion (RecitalTest.rolesFaltantesParaCancion): " + faltan);
        // la guitarra debe quedar cubierta por el artista base
        assertFalse(faltan.containsKey("Guitarra"));
        assertEquals(Integer.valueOf(1), faltan.get("Bateria"));
    }

    @Test
    public void rolesFaltantesTotalesSumaPorCancion() {
        Recital r = new Recital();

        // base no cubre nada en este test
        r.setBase(Collections.emptyList());

        Cancion c1 = new Cancion();
        Map<String,Integer> roles1 = new HashMap<>();
        roles1.put("Guitarra", 2);
        c1.setRoles(roles1);

        Cancion c2 = new Cancion();
        Map<String,Integer> roles2 = new HashMap<>();
        roles2.put("Guitarra", 1);
        roles2.put("Voz", 1);
        c2.setRoles(roles2);

        r.setCanciones(Arrays.asList(c1, c2));

        Map<String,Integer> tot = r.rolesFaltantesTotales();
        System.out.println("Faltantes totales por recital: " + tot);
        assertEquals(Integer.valueOf(3), tot.get("Guitarra"));
        assertEquals(Integer.valueOf(1), tot.get("Voz"));
    }
}
