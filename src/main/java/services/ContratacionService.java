
package services;
import domain.*;
import java.util.*;

public class ContratacionService {

    public void contratarParaCancion(Recital recital, Cancion c){
        // Empezamos con los roles faltantes de la canción (sin considerar contrataciones nuevas aún)
        Map<String,Integer> faltan = c.rolesFaltantes();
        Set<String> bandasBase = recital.bandasBase();

        for(Map.Entry<String,Integer> e: new ArrayList<>(faltan.entrySet())){
            String rol = e.getKey();
            int cant = e.getValue();

            for(int i=0;i<cant;i++){
                Artista mejor = null;
                double mejorCosto = Double.MAX_VALUE;

                for(Artista a: recital.getContratados()){
                    // Verificar que el artista puede tocar el rol y que tiene capacidad
                    boolean puede = a.puedeTocar(rol);
                    boolean tieneCapacidad = (a.getMaxCanciones() <= 0) || (a.getAsignaciones() < a.getMaxCanciones());
                    if(!puede || !tieneCapacidad) continue;

                    double costo = a.costoPara(bandasBase);
                    if(costo < mejorCosto){
                        mejorCosto = costo; mejor = a;
                    }
                }

                if(mejor==null) throw new RuntimeException("No hay artista disponible para rol "+rol);

                // Asignar y marcar la contratación para esa canción
                c.asignar(new AsignacionRol(mejor,rol));
                mejor.setAsignaciones(mejor.getAsignaciones() + 1);
            }
        }
    }

    /**
     * Contrata artistas para todas las canciones restantes del recital optimizando
     * el coste total. Se respetan las capacidades `maxCanciones` de cada artista
     * (número máximo de canciones que puede interpretar por recital) y se aplica
     * el descuento definido en `Artista.costoPara` usando las bandas base.
     * Las canciones que ya no tienen roles faltantes se omiten.
     */
    public void contratarParaRecital(Recital recital){
        List<Cancion> canciones = new ArrayList<>();
        List<Map<String,Integer>> faltantesPorCancion = new ArrayList<>();
        for(Cancion c: recital.getCanciones()){
            Map<String,Integer> falt = rolesFaltantesFromCancionRecital(recital, c);
            if(falt.isEmpty()) continue; // exenta
            canciones.add(c); faltantesPorCancion.add(falt);
        }

        if(canciones.isEmpty()) return;

        List<Artista> artistas = recital.getContratados();
        Set<String> bandasBase = recital.bandasBase();

        // Build flow network
        // Nodes: source(0), artists(1..A), artistSong nodes, songRole nodes, sink
        int A = artistas.size();
        int S = canciones.size();

        // Assign indices
        int idx = 1; // next available index
        int[] artistIdx = new int[A];
        for(int i=0;i<A;i++) artistIdx[i]=idx++;

        // map artist-song node index: artistSongIdx[i][j]
        int[][] artistSongIdx = new int[A][S];
        for(int i=0;i<A;i++){
            for(int j=0;j<S;j++) artistSongIdx[i][j]=idx++;
        }

        // song-role nodes mapping: for each song, for each role string we create a node
        List<Map<String,Integer>> songRoleIdx = new ArrayList<>();
        for(int j=0;j<S;j++){
            Map<String,Integer> map = new HashMap<>();
            for(String r: faltantesPorCancion.get(j).keySet()) map.put(r, idx++);
            songRoleIdx.add(map);
        }

        int sink = idx++;

        MinCostMaxFlow mcmf = new MinCostMaxFlow(idx);

        // source index 0
        // source -> artist with capacity = maxCanciones (if <=0 treat as S)
        for(int i=0;i<A;i++){
            int cap = artistas.get(i).getMaxCanciones();
            if(cap<=0) cap = S; // unlimited -> at most number of songs
            mcmf.addEdge(0, artistIdx[i], cap, 0);
            // artist -> artistSong (per song) cap 1 if artist can play any needed role in that song
            for(int j=0;j<S;j++){
                boolean canAny=false;
                for(String rol: faltantesPorCancion.get(j).keySet()){
                    if(artistas.get(i).puedeTocar(rol)){ canAny=true; break; }
                }
                if(canAny) mcmf.addEdge(artistIdx[i], artistSongIdx[i][j], 1, 0);
            }
        }

        // artistSong -> songRole edges cost = costo del artista para las bandasBase
        for(int i=0;i<A;i++){
            double costoArt = artistas.get(i).costoPara(bandasBase);
            int costInt = (int)Math.round(costoArt*100); // cents
            for(int j=0;j<S;j++){
                for(String rol: faltantesPorCancion.get(j).keySet()){
                    if(artistas.get(i).puedeTocar(rol)){
                        int songRoleNode = songRoleIdx.get(j).get(rol);
                        mcmf.addEdge(artistSongIdx[i][j], songRoleNode, 1, costInt);
                    }
                }
            }
        }

        // songRole -> sink with capacity = required count
        for(int j=0;j<S;j++){
            for(Map.Entry<String,Integer> e: faltantesPorCancion.get(j).entrySet()){
                int node = songRoleIdx.get(j).get(e.getKey());
                mcmf.addEdge(node, sink, e.getValue(), 0);
            }
        }

        // run flow
        int totalDemand=0; for(Map<String,Integer> m: faltantesPorCancion) for(int v: m.values()) totalDemand+=v;
        MinCostMaxFlow.Result res = mcmf.minCostMaxFlow(0, sink, totalDemand);
        
        //if(res.flow < totalDemand) throw new RuntimeException("No es posible contratar todos los roles necesarios");

        // Build assignments from used edges artistSong -> songRole
        for(int i=0;i<A;i++){
            for(int j=0;j<S;j++){
                int asIdx = artistSongIdx[i][j];
                for(MinCostMaxFlow.Edge e: mcmf.adj[asIdx]){
                    if(e.to==0) continue;
                    // edges from artistSong to songRole have capacity 1 and cost> =0
                    if(e.cap==0 && e.cost>=0){ // used (residual cap 0)
                        // find role string for node e.to
                        for(Map.Entry<String,Integer> entry: songRoleIdx.get(j).entrySet()){
                            if(entry.getValue()==e.to){
                                String rol = entry.getKey();
                                Cancion can = canciones.get(j);
                                Artista art = artistas.get(i);
                                can.asignar(new AsignacionRol(art, rol));
                                art.setAsignaciones(art.getAsignaciones()+1);
                            }
                        }
                    }
                }
            }
        }
        if(res.flow < totalDemand) throw new RuntimeException("No es posible contratar todos los roles necesarios");
    }

    // Helper: compute roles faltantes para una cancion teniendo en cuenta asignaciones actuales
    private Map<String,Integer> rolesFaltantesFromCancionRecital(Recital recital, Cancion c){
        // Use Cancion.rolesFaltantes() which considers current asignaciones
        // Then account for base coverage per song as in Recital.rolesFaltantesParaCancion
        Map<String,Integer> faltan = new HashMap<>(c.rolesFaltantes());
        for(Artista a: recital.getBase()){
            String elegido = null; int maxNeed=0;
            for(String rol: new HashSet<>(faltan.keySet())){
                Integer need = faltan.get(rol);
                if(need!=null && need>0 && a.puedeTocar(rol)){
                    if(need>maxNeed){ maxNeed=need; elegido=rol; }
                }
            }
            if(elegido!=null){
                faltan.put(elegido, faltan.get(elegido)-1);
                if(faltan.get(elegido)<=0) faltan.remove(elegido);
            }
        }
        return faltan;
    }

    // Minimal integer-cost min-cost max-flow implementation
    static class MinCostMaxFlow {
        static class Edge { int to, rev; int cap; int cost; Edge(int to,int rev,int cap,int cost){this.to=to;this.rev=rev;this.cap=cap;this.cost=cost;} }
        final int N;
        List<Edge>[] adj;
        @SuppressWarnings("unchecked")
        public MinCostMaxFlow(int n){ N=n; adj = new ArrayList[N]; for(int i=0;i<N;i++) adj[i]=new ArrayList<>(); }
        public void addEdge(int u,int v,int cap,int cost){ Edge a=new Edge(v, adj[v].size(), cap, cost); Edge b=new Edge(u, adj[u].size(), 0, -cost); adj[u].add(a); adj[v].add(b); }
        static class Result{ int flow; long cost; Result(int f,long c){flow=f; cost=c;} }

        public Result minCostMaxFlow(int s,int t,int maxf){ int flow=0; long cost=0; int[] dist = new int[N]; int[] pv = new int[N]; int[] pe = new int[N];
            while(flow<maxf){ Arrays.fill(dist, Integer.MAX_VALUE); dist[s]=0; boolean[] inq=new boolean[N]; int[] q=new int[N*10]; int qh=0,qt=0; q[qt++]=s; inq[s]=true;
                while(qh<qt){ int u=q[qh++]; inq[u]=false; for(int i=0;i<adj[u].size();++i){ Edge e=adj[u].get(i); if(e.cap>0 && dist[e.to]>dist[u]+e.cost){ dist[e.to]=dist[u]+e.cost; pv[e.to]=u; pe[e.to]=i; if(!inq[e.to]){ inq[e.to]=true; q[qt++]=e.to; } } } }
                if(dist[t]==Integer.MAX_VALUE) break; int add = maxf-flow; int v=t; while(v!=s){ Edge e = adj[pv[v]].get(pe[v]); add = Math.min(add, e.cap); v=pv[v]; }
                v=t; while(v!=s){ Edge e = adj[pv[v]].get(pe[v]); e.cap -= add; adj[v].get(e.rev).cap += add; v=pv[v]; }
                flow += add; cost += (long)add * dist[t];
            }
            return new Result(flow,cost);
        }
    }

}
