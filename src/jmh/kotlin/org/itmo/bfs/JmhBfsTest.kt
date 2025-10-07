package org.itmo.bfs

import org.itmo.Graph
import org.itmo.RandomGraphGenerator
import org.itmo.bfs.impl.ForkJoinBfs
import org.itmo.bfs.impl.ParallelBfs
import org.itmo.bfs.impl.ParallelFrontierBfs
import org.itmo.bfs.impl.SimpleBfs
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.BenchmarkMode
import org.openjdk.jmh.annotations.Fork
import org.openjdk.jmh.annotations.Measurement
import org.openjdk.jmh.annotations.Mode
import org.openjdk.jmh.annotations.OutputTimeUnit
import org.openjdk.jmh.annotations.Param
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.annotations.Warmup
import org.openjdk.jmh.infra.Blackhole
import java.util.concurrent.TimeUnit
import kotlin.math.min

@Fork(1)
@State(Scope.Benchmark)
@Warmup(iterations = 1, time = 5)
@Measurement(iterations = 4, time = 5)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
open class JmhBfsTest {

    @Param("10", "100", "1000", "10000", "50000", "100000", "1000000", "2000000")
    private var graphSize = 0

    private var connections = 0
    private lateinit var graph: Graph

    private val runtime = Runtime.getRuntime().availableProcessors() * 2

    private val simpleBfs = SimpleBfs()
    private val forkJoinBfs = ForkJoinBfs()
    private val parallelFrontierBfs = ParallelFrontierBfs(runtime)
    private val parallelBfs = ParallelBfs(runtime)

    @Setup
    fun setup() {
        connections = min(graphSize * 10, 10_000_000)

        println("Генерация графа размером $graphSize с $connections рёбрами...")

        graph = RandomGraphGenerator().generateGraph(java.util.Random(42), graphSize, connections)

        println("Генерация завершена.")
    }

    @Benchmark
    fun serialBfs(bh: Blackhole) {
        bh.consume(simpleBfs.bfs(graph, 0))
    }

    @Benchmark
    fun parallelBfs(bh: Blackhole) {
        bh.consume(parallelBfs.bfs(graph, 0))
    }

    @Benchmark
    fun parallelFrontierBfs(bh: Blackhole) {
        bh.consume(parallelFrontierBfs.bfs(graph, 0))
    }

    @Benchmark
    fun forkJoinBfs(bh: Blackhole) {
        bh.consume(forkJoinBfs.bfs(graph, 0))
    }
}
