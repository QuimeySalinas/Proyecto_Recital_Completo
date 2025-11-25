
package domain;
import java.util.*;

public class Artista {
    private String nombre;
    private Set<String> roles = new HashSet<>();
    private Set<String> bandas = new HashSet<>();
    private double costoBase;
    private int maxCanciones;
    private boolean base;
    private int asignaciones = 0;

    public Artista() {}

    public boolean puedeTocar(String rol){ return roles.contains(rol); }
    public void agregarRol(String r){ roles.add(r); }
    public void agregarBanda(String b){ bandas.add(b); }

    public double costoPara(Set<String> bandasBase){
        for(String b: bandas){
            if(bandasBase.contains(b)) return costoBase * 0.5;
        }
        return costoBase;
    }

    public Collection<? extends String> getBandas() {
        return bandas;
    }

    // getters & setters
    public void setNombre(String nombre){ this.nombre = nombre; }
    public String getNombre(){ return nombre; }

    public void setCostoBase(double costo){ this.costoBase = costo; }
    public double getCostoBase(){ return costoBase; }

    public void setMaxCanciones(int max){ this.maxCanciones = max; }
    public int getMaxCanciones(){ return maxCanciones; }

    public void setBase(boolean b){ this.base = b; }
    public boolean isBase(){ return base; }

    public int getAsignaciones(){ return asignaciones; }
    public void setAsignaciones(int a){ this.asignaciones = a; }
    
    public Set<String> getRoles() {
        return roles;
    }

    /**
     * Entrena al artista para un nuevo rol.
     * - No permite entrenar si el artista es parte de la base (`base == true`).
     * - No permite entrenar si el artista ya tiene asignaciones en canciones.
     * - Al entrenar, añade el rol y aumenta `costoBase` en un 50% (multiplica por 1.5).
     */
    public void entrenar(String nuevoRol){
        if(this.base) throw new IllegalStateException("No se puede entrenar a un artista de la base");
        if(this.asignaciones>0) throw new IllegalStateException("No se puede entrenar a un artista ya asignado a una canción");
        if(nuevoRol==null || nuevoRol.isEmpty()) return;
        if(!roles.contains(nuevoRol)){
            roles.add(nuevoRol);
            this.costoBase = this.costoBase * 1.5;
        }
    }
}
