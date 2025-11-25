package services;
import domain.*;
import org.jpl7.*;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.jpl7.Atom;
import org.jpl7.Query;
import org.jpl7.Term;
import org.jpl7.Variable;

public class CalculoEntrenamientos {

    public void calcular(Recital r) throws Exception {

        generarArchivoProlog(r);

        JPL.init(); 
        Query q1 = new Query(
                "consult",
                new Term[] { new Atom("CalculoEntrenamientos.pl") }
        );
        
        System.out.println("consult: " + (q1.hasSolution() ? "OK" : "ERROR"));
        //System.out.println("Test: " + Query.hasSolution("rol_necesario(X)"));

        Variable N = new Variable("N");
        Query q2 = new Query("cuantos_entrenamientos", new Term[]{ N });
        //Query q2 = new Query("cuantos_entrenamientos(N)");


        Map<String, Term> sol = q2.oneSolution();

        System.out.println("Entrenamientos mínimos necesarios: " + sol.get("N"));

    }

    public static void generarArchivoProlog(Recital recital) throws IOException {
        try (FileWriter fw = new FileWriter("CalculoEntrenamientos.pl")) {
            fw.write(
                "member(X, [X|_]).\n" +
                "member(X, [_|T]) :- member(X, T).\n\n" +
                "subtract([], _, []).\n" +
                "subtract([H|T], R, Result) :- member(H, R), subtract(T, R, Result).\n" +
                "subtract([H|T], R, [H|Result]) :- \\+ member(H, R), subtract(T, R, Result).\n\n"
            );

            for (Cancion can : recital.getCanciones()) {
                for (Map.Entry<String, java.lang.Integer> e : can.getRoles().entrySet()) {
                    String rol = e.getKey().replace(" ", "_");
                    int cantidad = e.getValue(); 
                    for (int i = 0; i < cantidad; i++) {
                        fw.write("rol_necesario(" + rol + ").\n");
                    }
                }

            }
            fw.write("\n");

            // artista_base(nombre, rol)
            for (Artista a : recital.getBase()) {
                for (String rol : a.getRoles()) {
                    fw.write("artista_base(" + a.getNombre().replace(" ", "_").toLowerCase() + "," + rol.replace(" ", "_").toLowerCase() + ").\n");
                }
            }
            fw.write("\n");

            // artista_contratado(nombre, rol)
            for (Cancion can : recital.getCanciones()) {
                for (AsignacionRol ar : can.getAsignaciones()) {
                    if (!ar.getArtista().isBase()) {
                        fw.write("artista_contratado(" + ar.getArtista().getNombre().replace(" ", "_").toLowerCase() + "," +
                                 ar.getRol().replace(" ", "_").toLowerCase() + ").\n");
                    }
                }
            }
            fw.write("\n");

            // Regla para calcular entrenamientos faltantes
            fw.write(
                "cuantos_entrenamientos(N) :-\n" +
                "    findall(R, rol_necesario(R), Req),\n" +
                "    findall(X, (artista_base(_, X); artista_contratado(_, X)), Roles),\n" +
                "    subtract(Req, Roles, Faltantes),\n" +
                "    length(Faltantes, N).\n"
            );
        }
    }


}
