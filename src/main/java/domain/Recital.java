
package domain;
import java.util.*;

public class Recital {
    private List<Cancion> canciones = new ArrayList<>();
    private List<Artista> base = new ArrayList<>();
    private List<Artista> contratados = new ArrayList<>();

    public Set<String> bandasBase(){
        Set<String> b = new HashSet<>();
        for(Artista a: base){
            b.addAll(a.getBandas());
        }
        return b;
    }

    // getters & setters
    public List<Cancion> getCanciones(){ return canciones; }
    public void setCanciones(List<Cancion> c){ this.canciones = c; }

    public List<Artista> getBase(){ return base; }
    public void setBase(List<Artista> b){ this.base = b; }

    public List<Artista> getContratados(){ return contratados; }
    public void setContratados(List<Artista> c){ this.contratados = c; }

    public void agregarContratado(Artista a){ this.contratados.add(a); }

    /**
     * Calcula los roles faltantes para una canción concreta, teniendo en cuenta
     * las asignaciones ya hechas en la canción y la disponibilidad de los artistas de la base.
     * Se asume que cada artista de la base puede tocar una única parte por canción
     * (es decir, no se agotan entre canciones).
     */
    public Map<String,Integer> rolesFaltantesParaCancion(Cancion c){
        Map<String,Integer> faltan = new HashMap<>(c.rolesFaltantes());

        // Para cada artista de la base intentamos cubrir un rol que pueda tocar
        for(Artista a: this.base){
            String elegido = null;
            int maxNecesidad = 0;
            // Elegimos el rol con mayor necesidad que el artista pueda cubrir
            for(String rol: new HashSet<>(faltan.keySet())){
                Integer need = faltan.get(rol);
                if(need!=null && need>0 && a.puedeTocar(rol)){
                    if(need>maxNecesidad){ maxNecesidad = need; elegido = rol; }
                }
            }
            if(elegido!=null){
                faltan.put(elegido, faltan.get(elegido)-1);
                if(faltan.get(elegido)<=0) faltan.remove(elegido);
            }
        }
        return faltan;
    }

    /**
     * Calcula los roles faltantes para todas las canciones del recital,
     * sumando las necesidades de cada canción después de incluir a los artistas base
     * en cada una de ellas (los artistas base pueden usarse por canción de forma independiente).
     */
    public Map<String,Integer> rolesFaltantesTotales(){
        Map<String,Integer> totales = new HashMap<>();
        for(Cancion c: this.canciones){
            Map<String,Integer> faltan = rolesFaltantesParaCancion(c);
            for(Map.Entry<String,Integer> e: faltan.entrySet()){
                totales.put(e.getKey(), totales.getOrDefault(e.getKey(), 0) + e.getValue());
            }
        }
        return totales;
    }
}
