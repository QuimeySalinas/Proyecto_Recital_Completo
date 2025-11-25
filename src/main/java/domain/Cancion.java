
package domain;
import java.util.*;

public class Cancion {
    private String nombre;
    private Map<String,Integer> roles = new HashMap<>();
    private List<AsignacionRol> asignaciones = new ArrayList<>();

    public Cancion(){}

    public Map<String,Integer> rolesFaltantes(){
        Map<String,Integer> faltan = new HashMap<>(roles);
        for(AsignacionRol a: asignaciones){
            faltan.put(a.getRol(), faltan.get(a.getRol())-1);
        }
        faltan.entrySet().removeIf(e -> e.getValue()<=0);
        return faltan;
    }

    public void asignar(AsignacionRol ar){ asignaciones.add(ar); }

    public void setNombre(String nombre){ this.nombre = nombre; }
    public String getNombre(){ return nombre; }

    public void setRoles(Map<String,Integer> roles){ this.roles = roles; }
    public Map<String,Integer> getRoles(){ return roles; }

    public List<AsignacionRol> getAsignaciones(){ return asignaciones; }
    // getters & setters
}
