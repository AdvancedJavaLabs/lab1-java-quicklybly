package org.itmo.bfs.impl;

import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicIntegerArray;
import org.itmo.Graph;
import org.itmo.bfs.Bfs;

public class ForkJoinBfs implements Bfs {

    @Override
    public void bfs(Graph graph, int startVertex) {
        var visited = new AtomicIntegerArray(graph.getSize());
        visited.set(startVertex, 1);

        // we can use faster collection, bz we don't remove elements and queue is sequential (linearizable) on CAS
        Collection<Integer> frontier = new ConcurrentLinkedQueue<>();
        frontier.add(startVertex);

        while (!frontier.isEmpty()) {
            Queue<Integer> nextFrontier = new ConcurrentLinkedQueue<>();

            // actually this need bulk optimization for unbalanced graphs
            frontier.parallelStream()
                    .flatMap(v -> graph.getAdjList()[v].stream())
                    .filter(neighbor -> visited.compareAndSet(neighbor, 0, 1))
                    .forEach(nextFrontier::add);

            frontier = nextFrontier;
        }
    }
}
