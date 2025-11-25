
package ui;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import domain.*;
import services.*;


import java.io.File;
import java.nio.file.Paths;
import java.util.*;


public class MenuPrincipal {
    public static void main(String[] args) throws Exception{
        System.out.println("Demo: cargar ejemplos y contratar artistas para el recital\n");

        ObjectMapper m = new ObjectMapper();
        // rutas a los ejemplos
        File artistasFile = Paths.get("src","main","resources","datosOrigen","artistas.json").toFile();
        File recitalFile = Paths.get("src","main","resources","datosOrigen","recital.json").toFile();
        File discograficaFile = Paths.get("src","main","resources","datosOrigen","artistas-discografica.json").toFile();

        List<Map<String,Object>> artistasRaw = m.readValue(artistasFile, new TypeReference<List<Map<String,Object>>>(){});
        List<Map<String,Object>> cancionesRaw = m.readValue(recitalFile, new TypeReference<List<Map<String,Object>>>(){});
        List<String> baseNames = m.readValue(discograficaFile, new TypeReference<List<String>>(){});

        List<Artista> artistas = new ArrayList<>();
        for(Map<String,Object> a : artistasRaw){
            Artista art = new Artista();
            art.setNombre((String)a.get("nombre"));
            Object rolesObj = a.get("roles");
            if(rolesObj instanceof List){
                for(Object r: (List)rolesObj) art.agregarRol(((String)r));
            }
            Object bandasObj = a.get("bandas");
            if(bandasObj instanceof List){
                for(Object b: (List)bandasObj) art.agregarBanda(((String)b));
            }
            Number costo = (Number)a.get("costo");
            if(costo!=null) art.setCostoBase(costo.doubleValue());
            Number max = (Number)a.get("maxCanciones");
            if(max!=null) art.setMaxCanciones(max.intValue());
            artistas.add(art);
        }

        Recital recital = new Recital();
        List<Cancion> canciones = new ArrayList<>();
        for(Map<String,Object> c : cancionesRaw){
            Cancion can = new Cancion();
            can.setNombre((String)c.get("titulo"));
            Map<String,Integer> rolesMap = new HashMap<>();
            Object rr = c.get("rolesRequeridos");
            if(rr instanceof List){
                for(Object r: (List)rr){
                    String rs = (String)r;
                    rolesMap.put(rs, rolesMap.getOrDefault(rs,0)+1);
                }
            }
            can.setRoles(rolesMap);
            canciones.add(can);
        }
        recital.setCanciones(canciones);

        // base: artistas incluidos por nombre
        List<Artista> base = new ArrayList<>();
        List<Artista> contratables = new ArrayList<>();
        Set<String> baseSet = new HashSet<>(baseNames);
        for(Artista a: artistas){
            if(baseSet.contains(a.getNombre())){ a.setBase(true); base.add(a); }
            else contratables.add(a);
        }
        recital.setBase(base);
        recital.setContratados(contratables);

        System.out.println("Artistas cargados: " + artistas.size() + " (base=" + base.size() + ", contratables=" + contratables.size() + ")\n");
        
        // MOSTRAR MENU PRINCIPAL
        Scanner mp = new Scanner(System.in);
        boolean seguir = true;
        ContratacionService cs = new ContratacionService();
        CalculoEntrenamientos ce = new CalculoEntrenamientos();
        ObjectMapper mapper = new ObjectMapper();

        while (seguir) {
            System.out.println("\n\n\n\n------- MENU PRINCIPAL -------");
            System.out.println("1 - Realizar contrataciones");
            System.out.println("2 - Entrenar artistas");
            System.out.println("3 - Listar roles faltantes por cancion");
            System.out.println("4 - Listar roles faltantes por todas las canciones");
            System.out.println("5 - Contratar artistas para cancion");
            System.out.println("6 - Listar artistas contratados");
            System.out.println("7 - Listar estado de las canciones");
            System.out.println("8 - Entrenamientos minimos a realizar para cubrir todas las canciones");
            //BONUS Importar recital previo
            System.out.println("9 - Importar recital previo");
            System.out.println("10 - Eliminar artista de recital");
            System.out.println("11 - Salir");
            System.out.print("Seleccione una opcion: ");

            String opcion = mp.nextLine();
            
            switch (opcion) {
                case "1":
                    // Contratacion
                    
                    try{
                        cs.contratarParaRecital(recital);
                    }catch(RuntimeException re){
                        System.out.println(re.getMessage());

                        Scanner sc = new Scanner(System.in);

                        // Obtener todos los roles faltantes por canción
                        List<String> rolesPendientes = new ArrayList<>();
                        List<Cancion> cancionesPendientes = new ArrayList<>();

                        for (Cancion can : recital.getCanciones()) {
                            Map<String, Integer> faltan = recital.rolesFaltantesParaCancion(can);
                            if (!faltan.isEmpty()) {
                                for (Map.Entry<String, Integer> e : faltan.entrySet()) {
                                    for (int i = 0; i < e.getValue(); i++) {
                                        rolesPendientes.add(e.getKey());
                                        cancionesPendientes.add(can);
                                    }
                                }
                            }
                        }

                        if (rolesPendientes.isEmpty()) {
                            System.out.println("\nNo hay roles faltantes, error inesperado.");
                            return;
                        }
                        System.out.println("Desea entrenar artistas para cubrirlos? (s/n)");
                        String resp = sc.nextLine();

                        if (resp.equalsIgnoreCase("s")) {
                            for (int i = 0; i < rolesPendientes.size(); i++) {
                                String rolPendiente = rolesPendientes.get(i);
                                Cancion can = cancionesPendientes.get(i);

                                System.out.println("ROL FALTANTE: " + rolPendiente + " (Cancion: " + can.getNombre() + ")");
                                System.out.println("Desea entrenar un artista para este rol? (s = si, otra tecla = saltar)");
                                String opt = sc.nextLine();

                                if (!opt.equalsIgnoreCase("s")) {
                                    System.out.println("Se salta este rol.");
                                    continue;
                                }

                                // Listar artistas disponibles para entrenar
                                List<Artista> disp = new ArrayList<>();
                                for (Artista a : recital.getContratados()) {
                                    if (a.getAsignaciones() == 0) {
                                        disp.add(a);
                                    }
                                }

                                System.out.println("\nArtistas disponibles para entrenar:");
                                for (int j = 0; j < disp.size(); j++) {
                                    Artista a = disp.get(j);
                                    System.out.println(j + " - " + a.getNombre() + " (roles actuales: " + a.getRoles() + ")");
                                }

                                System.out.print("\nSeleccione el numero del artista a entrenar: ");
                                int idArt = Integer.parseInt(sc.nextLine());
                                Artista elegido = disp.get(idArt);

                                try {
                                    elegido.entrenar(rolPendiente);
                                    System.out.println("Entrenamiento exitoso: ahora " + elegido.getNombre() + " puede interpretar el rol " + rolPendiente);
                                } catch(Exception ex) {
                                    System.out.println("No se pudo entrenar al artista: " + ex.getMessage());
                                }
                            }

                            // Intentamos contratar nuevamente después de entrenar
                            try{
                                cs.contratarParaRecital(recital);
                                System.out.println("\nReasignacion completada luego del entrenamiento.");
                            }catch(RuntimeException re2){
                                System.out.println("\nNo se pudieron cubrir todos los roles.");
                            }
                        }
                    }
                    break;
                case "2":
                    System.out.println("\n Entrenamiento de artistas");
                    entrenarArtistas(recital);
                    break;
                    
                case "3":
                    Cancion elegida = seleccionarCancion(mp,recital.getCanciones());
                    if(elegida == null){
                        System.out.println("No se selecciono ninguna cancion valida.");
                        break;
                    }
                    Map<String, Integer> faltantes = recital.rolesFaltantesParaCancion(elegida);
                    System.out.println("\n--- Roles faltantes para la cancion: " + elegida.getNombre() + " ---");

                    if (faltantes.isEmpty()) {
                        System.out.println("La cancion esta COMPLETA, no faltan roles.");
                    } else {
                        for (Map.Entry<String, Integer> entry : faltantes.entrySet()) {
                            System.out.println("- " + entry.getKey() + " x" + entry.getValue());
                        }
                    }
                    break;
                    
                case "4":
                    // Mostrar roles faltantes por canción antes de contratar
                    System.out.println("Roles faltantes por canción (considerando base):");
                    for(Cancion can: recital.getCanciones()){
                        System.out.println(" - " + can.getNombre() + ": " + recital.rolesFaltantesParaCancion(can));
                    }
                    break;
                    
                case "5":
                    Cancion c = seleccionarCancion(mp, recital.getCanciones());
                    if (c == null) {
                        System.out.println("No se selecciono ninguna cancion valida.");
                        break;
                    }

                    try {
                        cs.contratarParaCancion(recital, c);
                        System.out.println("Contratacion realizada con exito!");
                    } catch (Exception e) {
                        System.out.println("Error: " + e.getMessage());
                    }
                    mp.nextLine();
                    break;
                
                case "6":
                    // Listar artistas contratados y su informacion
                    Set<Artista> contratadosFinal = new HashSet<>();
                    for (Cancion can : recital.getCanciones()) {
                        for (AsignacionRol ar : can.getAsignaciones()) {
                            if (!ar.getArtista().isBase()) {
                                contratadosFinal.add(ar.getArtista());
                            }
                        }
                    }

                    if (contratadosFinal.isEmpty()) {
                        System.out.println("\nNo hay artistas contratados todavia.");
                    } else {
                        System.out.println("\nArtistas contratados final:");
                        for (Artista a : contratadosFinal) {
                            double costo = a.costoPara(recital.bandasBase());
                            System.out.println("Nombre: " + a.getNombre());
                            if (a.getAsignaciones() == 1)
                                System.out.println("Asignado a " + a.getAsignaciones() + " cancion");
                            else
                                System.out.println("Asignado a " + a.getAsignaciones() + " canciones");
                            System.out.println("Costo por cancion: " + costo);
                            System.out.println();
                        }
                    }
                    break;
                
                case "7":
                    // Listar estado de las canciones
                    System.out.println("\n\nEstado de las canciones del recital:");

                    for (Cancion can : recital.getCanciones()) {
                        Map<String, Integer> faltan = recital.rolesFaltantesParaCancion(can);
                        System.out.println("Cancion: " + can.getNombre());

                        // Calcular coste de la cancion: sumar el costo por artista usado en la cancion (sin duplicar)
                        Set<Artista> artistasEnCancion = new HashSet<>();
                        for (AsignacionRol ar : can.getAsignaciones()) {
                            artistasEnCancion.add(ar.getArtista());
                        }

                        double costoCancion = 0.0;
                        Set<String> bandasBase = recital.bandasBase();
                        for (Artista a : artistasEnCancion) {
                            if (a.isBase()) {
                                continue;
                            }
                            costoCancion += a.costoPara(bandasBase);
                        }

                        if (faltan.isEmpty()) {
                            System.out.println("COMPLETA - todos los roles estan asignados");
                        } else {
                            System.out.println("INCOMPLETA - faltan roles:");
                            for (Map.Entry<String, Integer> e : faltan.entrySet()) {
                                System.out.println("- " + e.getKey() + " x" + e.getValue());
                            }
                        }
                        System.out.println("Coste estimado de la cancion: " + costoCancion);
                        System.out.println();
                    }
                    break;
                
                case "8":
                    //INTEGRACION CON PROLOG
                    ce.calcular(recital);
                    break;
                case "9":
                    try {
                          recital = mapper.readValue(new File("recital-out.json"), Recital.class);
                          System.out.println("Recital cargado con exito!");
                      } catch (Exception e) {
                          System.out.println("Error al cargar el archivo: " + e.getMessage());
                      }
                    break;
                
                case "10":
                    //Lista de artistas asignados
                    Set<Artista> artistasAsignados = new HashSet<>();
                    for (Cancion can : recital.getCanciones()) {
                        for (AsignacionRol ar : can.getAsignaciones()) {
                            artistasAsignados.add(ar.getArtista());
                        }
                    }
                    if (artistasAsignados.isEmpty()) {
                        System.out.println("No hay artistas asignados en el recital.");
                        break;
                    }
                    List<Artista> listaParaSeleccion = new ArrayList<>(artistasAsignados);

                    Artista artistaAEliminar = seleccionarArtista(mp, listaParaSeleccion);
                    if (artistaAEliminar == null) {
                        System.out.println("No se selecciono un artista valido.");
                        break;
                    }

                    System.out.println("\nEliminando todas las asignaciones de: " + artistaAEliminar.getNombre());

                    //Eliminamos TODAS las asignaciones del artista en cada cancion
                    for (Cancion can : recital.getCanciones()) {
                        int antes = can.getAsignaciones().size();
                        // removemos todas las asignaciones cuyo artista coincida
                        can.getAsignaciones().removeIf(ar -> ar.getArtista().equals(artistaAEliminar));
                        int despues = can.getAsignaciones().size();
                        int eliminadas = antes - despues;
                        if (eliminadas > 0) {
                            artistaAEliminar.setAsignaciones(
                                artistaAEliminar.getAsignaciones() - eliminadas
                            );
                        }
                    }

                    System.out.println("Artista eliminado correctamente del recital.");
                    break;
                case "11":
                    seguir = false;
                    break;

                default:
                    System.out.println("\nOpcion invalida. Intente nuevamente.");
                    break;
            }
        }

        /* Mostrar asignaciones por canción y coste estimado
        double totalCost = 0.0;
        Set<Artista> usados = new HashSet<>();
        System.out.println("\nAsignaciones finales:");
        for(Cancion can: recital.getCanciones()){
            System.out.println(" - " + can.getNombre() + ":");
            for(AsignacionRol ar: can.getAsignaciones()){
                System.out.println("    * rol=" + ar.getRol() + ", artista=" + ar.getArtista().getNombre());
                totalCost += ar.getArtista().costoPara(recital.bandasBase());
                usados.add(ar.getArtista());
            }
        }
        System.out.println("\nCoste total estimado: " + totalCost);
        System.out.println("Artistas usados: " + usados.size());
        */
        
        //Bonus - Realizar exportacion del estado del recital
        mapper.writerWithDefaultPrettyPrinter().writeValue(
            new File("recital-out.json"),
            recital
        );
        System.out.println("Archivo recital-out.json exportado!");

    }
    
    public static void entrenarArtistas(Recital recital) {
        Scanner sc = new Scanner(System.in);
        List<Artista> disponibles = new ArrayList<>();
        disponibles.addAll(recital.getContratados());

        if (disponibles.isEmpty()) {
            System.out.println("No hay artistas disponibles para entrenar.");
            return;
        }

        // Mostrar artistas
        System.out.println("\nArtistas disponibles:");
        for (int i = 0; i < disponibles.size(); i++) {
            Artista a = disponibles.get(i);
            System.out.println(
                i + " - " + a.getNombre() + 
                " | roles actuales: " + a.getRoles()
            );
        }

        System.out.print("\nSeleccione el número del artista a entrenar: ");
        String opcion = sc.nextLine();

        int idArt;
        try {
            idArt = Integer.parseInt(opcion);
        } catch (Exception e) {
            System.out.println("Opción inválida.");
            return;
        }

        if (idArt < 0 || idArt >= disponibles.size()) {
            System.out.println("Número fuera de rango.");
            return;
        }

        Artista elegido = disponibles.get(idArt);

        // Pedir el rol
        System.out.print("Ingrese el nombre del rol a entrenar: ");
        String nuevoRol = sc.nextLine().trim();

        if (nuevoRol.isEmpty()) {
            System.out.println("No ingresó un rol válido.");
            return;
        }

        try {
            elegido.entrenar(nuevoRol);
            System.out.println(
                "\nEl artista " + elegido.getNombre() + 
                " ahora puede interpretar el rol: " + nuevoRol
            );
        } catch (Exception ex) {
            System.out.println("\nNo se pudo entrenar al artista: " + ex.getMessage());
        }
    }
    public static Cancion seleccionarCancion(Scanner sc, List<Cancion> canciones) {
        System.out.println("\nSeleccione una cancion");
        if (canciones.isEmpty()) {
            System.out.println("No hay canciones cargadas.");
            return null;
        }
        for (int i = 0; i < canciones.size(); i++) {
            System.out.println((i + 1) + ". " + canciones.get(i).getNombre());
        }
        System.out.print("Ingrese una opcion: ");
        int opcion = sc.nextInt();
        sc.nextLine(); 
        if (opcion < 1 || opcion > canciones.size()) {
            System.out.println("Opcion invalida.");
            return null;
        }
        return canciones.get(opcion - 1);
    }
    public static Artista seleccionarArtista(Scanner sc, List<Artista> artistas) {
        System.out.println("\n\nSeleccione un artista");
        if (artistas.isEmpty()) {
            System.out.println("No hay artistas cargados.");
            return null;
        }
        // Listado de artistas
        for (int i = 0; i < artistas.size(); i++) {
            System.out.println((i + 1) + ". " + artistas.get(i).getNombre());
        }
        System.out.print("Ingrese una opción: ");
        int opcion = sc.nextInt();
        sc.nextLine(); 
        if (opcion < 1 || opcion > artistas.size()) {
            System.out.println("Opción inválida.");
            return null;
        }
        return artistas.get(opcion - 1);
    }
    
}
