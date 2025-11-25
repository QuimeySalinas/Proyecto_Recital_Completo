package domain;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;

public class CancionTest {

    @Test
    public void rolesFaltantesCuentaCorrectamente() {
        Cancion c = new Cancion();
        Map<String,Integer> roles = new HashMap<>();
        roles.put("Guitarra", 2);
        roles.put("Voz", 1);
        c.setRoles(roles);

        // una asignacion de Guitarra ya hecha
        Artista a = new Artista();
        AsignacionRol ar = new AsignacionRol(a, "Guitarra");
        c.asignar(ar);

        Map<String,Integer> faltan = c.rolesFaltantes();
        System.out.println("Faltantes para la cancion (CancionTest): " + faltan);
        assertEquals(2, faltan.size());
        assertTrue(faltan.containsKey("Guitarra"));
        assertEquals(Integer.valueOf(1), faltan.get("Guitarra"));
        assertEquals(Integer.valueOf(1), faltan.get("Voz"));
    }
}
