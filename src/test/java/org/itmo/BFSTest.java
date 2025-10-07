package org.itmo;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import org.itmo.bfs.impl.ForkJoinBfs;
import org.itmo.bfs.impl.ParallelBfs;
import org.itmo.bfs.impl.ParallelFrontierBfs;
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
                long forkJoinTime = executeForkJoinBfs(g);
                long frontierTime = executeParallelFrontierBfsAndGetTime(g);
                long parallelTime = executeParallelBfsAndGetTime(g);
                fw.append("Times for " + sizes[i] + " vertices and " + connections[i] + " connections: ");
                fw.append("\nSerial: " + serialTime);
                fw.append("\nForkJoin: " + forkJoinTime);
                fw.append("\nFrontier: " + frontierTime);
                fw.append("\nParallel: " + parallelTime);
                fw.append("\n--------\n");
            }
            fw.flush();
        }
    }

    @Test
    public void bfsThreadsTest() throws IOException {
        int graphSize = 2_000_000;
        int connections = 10_000_000;
        int[] threadCounts = new int[]{1, 2, 4, 8, 12, 16, 24};
        Random r = new Random(42);

        System.out.println("Generating graph of size " + graphSize + " with " + connections + " connections... wait");
        Graph g = new RandomGraphGenerator().generateGraph(r, graphSize, connections);
        System.out.println("Graph generation completed!");

        try (FileWriter fw = new FileWriter("tmp/threads_results.txt")) {
            for (int threads : threadCounts) {
                System.out.println("--------------------------");
                System.out.println("Running BFS with " + threads + " threads");
                var bfs = new ParallelFrontierBfs(threads);
                long startTime = System.nanoTime();
                bfs.bfs(g, 0);
                long endTime = System.nanoTime();
                long result = (endTime - startTime) / 1_000_000;

                fw.append("Times for " + graphSize + " vertices, " + connections + " connections and " + threads + " threads:\n");
                fw.append("Frontier: " + result + "\n");
                fw.append("--------\n");
                fw.flush();
            }
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
        var runtime = Runtime.getRuntime().availableProcessors() * 2;
        var bfs = new ParallelBfs(runtime);
        long startTime = System.nanoTime();
        bfs.bfs(g, 0);
        long endTime = System.nanoTime();
        return (endTime - startTime) / 1_000_000;
    }

    private long executeParallelFrontierBfsAndGetTime(Graph g) {
        var runtime = Runtime.getRuntime().availableProcessors() * 2;
        var bfs = new ParallelFrontierBfs(runtime);
        long startTime = System.nanoTime();
        bfs.bfs(g, 0);
        long endTime = System.nanoTime();
        return (endTime - startTime) / 1_000_000;
    }

    private long executeForkJoinBfs(Graph g) {
        var bfs = new ForkJoinBfs();
        long startTime = System.nanoTime();
        try (var pool = ForkJoinPool.commonPool()) {
            pool.submit(() -> bfs.bfs(g, 0)).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
        long endTime = System.nanoTime();
        return (endTime - startTime) / 1_000_000;
    }
}
