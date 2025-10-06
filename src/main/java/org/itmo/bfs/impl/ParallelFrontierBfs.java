package org.itmo.bfs.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicIntegerArray;
import org.itmo.Graph;
import org.itmo.bfs.Bfs;

public class ParallelFrontierBfs implements Bfs {

    private final int threadNumber;

    public ParallelFrontierBfs(int threadNumber) {
        this.threadNumber = threadNumber;
    }

    @Override
    public void bfs(Graph graph, int startVertex) {
        var adjList = graph.getAdjList();
        var visited = new AtomicIntegerArray(graph.getSize());
        visited.set(startVertex, 1);

        Collection<Integer> frontier = new ArrayList<>();
        frontier.add(startVertex);

        try (var es = Executors.newFixedThreadPool(threadNumber)) {
            while (!frontier.isEmpty()) {
                // queue is bad for large parallelism factor
                Queue<Integer> nextFrontier = new ConcurrentLinkedQueue<>();

                var butchSize = Math.max(1, frontier.size() / threadNumber);

                List<Future<?>> futures = new ArrayList<>();
                for (int i = 0; i < frontier.size(); i += butchSize) {
                    var startIndex = i;
                    var lastIndex = Math.min(i + butchSize, frontier.size());

                    var future = es.submit(() ->
                            {
                                for (int vertex = startIndex; vertex < lastIndex; vertex++) {
                                    for (int neighbor : adjList[vertex]) {
                                        if (visited.compareAndSet(neighbor, 0, 1)) {
                                            nextFrontier.add(neighbor);
                                        }
                                    }
                                }
                            }
                    );

                    futures.add(future);
                }

                for (Future<?> f : futures) {
                    f.get();
                }

                frontier = nextFrontier;
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
