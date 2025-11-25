package services;

import com.fasterxml.jackson.databind.ObjectMapper;
import domain.*;

import java.io.File;
import java.util.*;

public class ExportadorRecital {

    public static void exportar(Recital recital) {

        try {
            Map<String, Object> root = new HashMap<>();

            double costoTotal = 0.0;
            Set<Artista> artistasUsados = new HashSet<>();

            for (Cancion can : recital.getCanciones()) {
                for (AsignacionRol ar : can.getAsignaciones()) {
                    artistasUsados.add(ar.getArtista());
                    costoTotal += ar.getArtista().costoPara(recital.bandasBase());
                }
            }

            long cancionesCompletas = recital.getCanciones().stream()
                    .filter(c -> recital.rolesFaltantesParaCancion(c).isEmpty())
                    .count();

            root.put("costoTotalRecital", costoTotal);
            root.put("artistasUsados", artistasUsados.size());
            root.put("cancionesCompletas", cancionesCompletas);
            root.put("cancionesIncompletas", recital.getCanciones().size() - cancionesCompletas);

            List<Map<String, Object>> cancionesOut = new ArrayList<>();

            for (Cancion can : recital.getCanciones()) {

                Map<String, Object> cjson = new LinkedHashMap<>();
                Map<String, Integer> faltan = recital.rolesFaltantesParaCancion(can);

                // costo
                double costoCancion = 0.0;
                Set<Artista> artistasEnCancion = new HashSet<>();
                for (AsignacionRol ar : can.getAsignaciones()) {
                    artistasEnCancion.add(ar.getArtista());
                }
                for (Artista a : artistasEnCancion) {
                    if (!a.isBase())
                        costoCancion += a.costoPara(recital.bandasBase());
                }

                cjson.put("titulo", can.getNombre());
                cjson.put("estado", faltan.isEmpty() ? "COMPLETA" : "INCOMPLETA");
                cjson.put("costo", costoCancion);
                cjson.put("rolesFaltantes", faltan);

                // asignaciones
                List<Map<String, Object>> asignacionesOut = new ArrayList<>();
                for (AsignacionRol ar : can.getAsignaciones()) {
                    Map<String, Object> ajson = new HashMap<>();
                    ajson.put("rol", ar.getRol());
                    ajson.put("artista", ar.getArtista().getNombre());
                    asignacionesOut.add(ajson);
                }

                cjson.put("asignaciones", asignacionesOut);
                cancionesOut.add(cjson);
            }

            root.put("canciones", cancionesOut);


            List<Map<String, Object>> artistasOut = new ArrayList<>();
            for (Artista a : artistasUsados) {
                if (!a.isBase()) {
                    Map<String, Object> aj = new LinkedHashMap<>();
                    aj.put("nombre", a.getNombre());
                    aj.put("roles", a.getRoles());
                    aj.put("asignadoACanciones", a.getAsignaciones());
                    aj.put("costoPorCancion", a.costoPara(recital.bandasBase()));
                    artistasOut.add(aj);
                }
            }

            root.put("artistasContratados", artistasOut);

            ObjectMapper mapper = new ObjectMapper();
            mapper.writerWithDefaultPrettyPrinter().writeValue(
                    new File("recital-out.json"), root
            );

            System.out.println("\nArchivo recital-out.json generado exitosamente.");

        } 
        catch (Exception ex) {
        }
    }
}
