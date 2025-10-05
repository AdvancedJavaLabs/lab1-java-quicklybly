package org.itmo.bfs.impl;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.stream.IntStream;
import org.itmo.Graph;
import org.itmo.bfs.Bfs;

public class ParallelBfs implements Bfs {

    private final int threadNumber;

    public ParallelBfs(int threadNumber) {
        this.threadNumber = threadNumber;
    }

    @Override
    public void bfs(Graph graph, int startVertex) {
        AtomicInteger activeNodes = new AtomicInteger(1);
        AtomicIntegerArray distances = new AtomicIntegerArray(graph.getSize());
        ConcurrentLinkedQueue<Integer> queue = new ConcurrentLinkedQueue<>();

        queue.add(startVertex);

        IntStream.range(0, threadNumber)
                .mapToObj(i ->
                        new Thread(new BfsWorker(graph, distances, queue, activeNodes))
                )
                .peek(Thread::start)
                .forEach(thread -> {
                    try {
                        thread.join();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    private record BfsWorker(
            Graph graph,
            AtomicIntegerArray distances,
            Queue<Integer> queue,
            AtomicInteger activeNodes
    ) implements Runnable {

        @Override
        public void run() {
            var adjList = graph.getAdjList();
            while (activeNodes.get() > 0) {
                var currentVertex = queue.poll();
                if (currentVertex == null) {
                    continue;
                }
                int d = distances.get(currentVertex);
                for (var neighbor : adjList[currentVertex]) {
                    if (updateDistanceIfLower(neighbor, d - 1)) {
                        queue.add(neighbor);
                        activeNodes.incrementAndGet();
                    }
                }
                activeNodes.decrementAndGet();
            }
        }

        private boolean updateDistanceIfLower(int node, int newDistance) {
            while (true) {
                int currentDistance = distances.get(node);

                if (newDistance >= currentDistance || currentDistance != 0) {
                    return false;
                }

                if (distances.compareAndSet(node, currentDistance, newDistance)) {
                    return true;
                }
            }
        }
    }
}
