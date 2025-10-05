package org.itmo;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import org.itmo.bfs.impl.ForkJoinBfs;
import org.itmo.bfs.impl.ParallelBfs;
import org.itmo.bfs.impl.SimpleBfs;
import org.junit.jupiter.api.Test;

public class BFSTest {

    @Test
    public void kek() {
        int size = 1_000_000;
        int connections = 10_000_000;

        Random r = new Random(42);

        var g = new RandomGraphGenerator().generateGraph(r, size, connections);

        long time1 = executeSerialBfsAndGetTime(g);
        long time2 = executeForkJoinBfs(g);

        System.out.println("Serial " + time1);
        System.out.println("ForkJoin " + time2);
    }

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
        long startTime = System.nanoTime();
        bfs.bfs(g, 0);
        long endTime = System.nanoTime();
        return (endTime - startTime) / 1_000_000;
    }

    private long executeParallelBfsAndGetTime(Graph g) {
        var bfs = new ParallelBfs(1);
        long startTime = System.nanoTime();
        bfs.bfs(g, 0);
        long endTime = System.nanoTime();
        return (endTime - startTime) / 1_000_000;
    }

    private long executeForkJoinBfs(Graph g) {
        var bfs = new ForkJoinBfs();
        long startTime = System.nanoTime();
        try {
            ForkJoinPool.commonPool().submit(() -> bfs.bfs(g, 0)).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
        long endTime = System.nanoTime();
        return (endTime - startTime) / 1_000_000;
    }
}
