package finds;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UnionFindTest {


    @Test
    void testRoots() {
        UnionFind uf = new UnionFind(5);
        assertEquals(0, uf.find(0));  // should be its own root
    }

    @Test
    void testUnionConnects() {
        UnionFind uf = new UnionFind(5);
        uf.union(0, 1);
        assertEquals(uf.find(0), uf.find(1));//should share same root
        uf.union(0, 1);
        uf.union(3, 4);
        assertNotEquals(uf.find(0), uf.find(3)); //shouldnt have same roots
    }
}