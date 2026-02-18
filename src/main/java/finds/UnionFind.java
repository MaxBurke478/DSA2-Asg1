package finds;

public class UnionFind {
    private final int[] parent;
    private final byte[] rank;

    public UnionFind(int size) {
        parent = new int[size];
        rank = new byte[size];

        for(int i = 0; i < size; i++) {
            parent[i] = i;
            rank[i] = 0;
        }
    }

    public int find(int p) {
        int root = p;
        while(parent[root] != root) {
            root = parent[root];
        }
        while(parent[p] != p){
            int next = parent[p];
            parent[p] = root;
            p = next;
        }
        return root;
    }

    public void union(int p, int q) {
        int rootP = find(p);
        int rootQ = find(q);
        if(rootP == rootQ) {
            return;
        }
        if(rank[rootP] < rank[rootQ]) {
            parent[rootP] = rootQ;
        }else if(rank[rootP] > rank[rootQ]) {
            parent[rootQ] = rootP;
        }else{
            parent[rootQ] = rootP;
            rank[rootP]++;
        }
    }
}
