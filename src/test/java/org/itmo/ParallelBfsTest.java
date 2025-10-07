package org.itmo;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;
import org.itmo.bfs.impl.ParallelBfs.BfsWorker;
import org.openjdk.jcstress.annotations.Actor;
import org.openjdk.jcstress.annotations.Arbiter;
import org.openjdk.jcstress.annotations.Expect;
import org.openjdk.jcstress.annotations.JCStressTest;
import org.openjdk.jcstress.annotations.Outcome;
import org.openjdk.jcstress.annotations.State;
import org.openjdk.jcstress.infra.results.I_Result;

@JCStressTest
@Outcome(id = "0", expect = Expect.ACCEPTABLE, desc = "Нет нарушений")
@State
public class ParallelBfsTest {

    private final Graph graph = new RandomGraphGenerator().generateGraph(new Random(42), 1000, 100000);

    private final AtomicInteger activeNodes = new AtomicInteger(1);
    private final AtomicIntegerArray distances = new AtomicIntegerArray(graph.getSize());
    private final ConcurrentLinkedQueue<Integer> queue = new ConcurrentLinkedQueue<>();

    {
        queue.add(0);
    }

    @Actor
    public void actor1() {
        Thread t = new Thread(new BfsWorker(graph, distances, queue, activeNodes));
        t.start();

        try {
            t.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Actor
    public void actor2() {
        Thread t = new Thread(new BfsWorker(graph, distances, queue, activeNodes));
        t.start();

        try {
            t.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Actor
    public void actor3() {
        Thread t = new Thread(new BfsWorker(graph, distances, queue, activeNodes));
        t.start();

        try {
            t.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Actor
    public void actor4() {
        Thread t = new Thread(new BfsWorker(graph, distances, queue, activeNodes));
        t.start();

        try {
            t.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Arbiter
    public void arbiter(I_Result r) {
        List<Integer> distancesSimple = new ArrayList<>();
        for (int i = 0; i < distances.length(); i++) {
            distancesSimple.add(0);
        }

        Queue<Integer> queue = new LinkedList<>();

        queue.add(0);

        while (!queue.isEmpty()) {
            int vertex = queue.poll();

            for (int neighbor : graph.getAdjList()[vertex]) {
                if (distancesSimple.get(neighbor).equals(0)) {
                    distancesSimple.set(neighbor, distancesSimple.get(vertex) - 1);
                    queue.add(neighbor);
                }
            }
        }

        int result = 0;
        for (int i = 0; i < graph.getSize(); i++) {
            if (!distancesSimple.get(i).equals(distances.get(i))) {
                result++;
            }
        }

        r.r1 = result;
    }
}
