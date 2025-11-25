
package domain;

public class AsignacionRol {
    private Artista artista;
    private String rol;
    
    public AsignacionRol() { }
    public AsignacionRol(Artista a,String r){
        this.artista=a; this.rol=r;
    }
    public Artista getArtista(){ return artista; }
    public String getRol(){ return rol; }
}
