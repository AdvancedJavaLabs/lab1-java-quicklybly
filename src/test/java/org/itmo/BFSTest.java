package org.itmo;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
import org.itmo.bfs.impl.ParallelBfs;
import org.itmo.bfs.impl.SimpleBfs;
import org.junit.jupiter.api.Test;

public class BFSTest {

    @Test
    public void bfsTest() throws IOException {
        int[] sizes = new int[]{10, 100, 1000, 10_000, 10_000, 50_000, 100_000, 1_000_000, 2_000_000};
        int[] connections = new int[]{50, 500, 5000, 50_000, 100_000, 1_000_000, 1_000_000, 10_000_000, 10_000_000};
        Random r = new Random(42);
        try (FileWriter fw = new FileWriter("tmp/results.txt")) {
            for (int i = 0; i < sizes.length; i++) {
                System.out.println("--------------------------");
                System.out.println("Generating graph of size " + sizes[i] + " ...wait");
                Graph g = new RandomGraphGenerator().generateGraph(r, sizes[i], connections[i]);
                System.out.println("Generation completed!\nStarting bfs");
                long serialTime = executeSerialBfsAndGetTime(g);
                long parallelTime = executeParallelBfsAndGetTime(g);
                fw.append("Times for " + sizes[i] + " vertices and " + connections[i] + " connections: ");
                fw.append("\nSerial: " + serialTime);
                fw.append("\nParallel: " + parallelTime);
                fw.append("\n--------\n");
            }
            fw.flush();
        }
    }

    private long executeSerialBfsAndGetTime(Graph g) {
        var bfs = new SimpleBfs();
        long startTime = System.currentTimeMillis();
        bfs.bfs(g, 0);
        long endTime = System.currentTimeMillis();
        return endTime - startTime;
    }

    private long executeParallelBfsAndGetTime(Graph g) {
        var bfs = new ParallelBfs();
        long startTime = System.currentTimeMillis();
        bfs.bfs(g, 0);
        long endTime = System.currentTimeMillis();
        return endTime - startTime;
    }
}
