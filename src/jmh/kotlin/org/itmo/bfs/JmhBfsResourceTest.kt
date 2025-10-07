package org.itmo.bfs

import org.itmo.Graph
import org.itmo.RandomGraphGenerator
import org.itmo.bfs.impl.ParallelBfs
import org.itmo.bfs.impl.ParallelFrontierBfs
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
open class JmhBfsResourceTest {

    @Param("1", "2", "4", "6", "12", "16", "24")
    private var runtime = 0

    private var connections = 0
    private lateinit var graph: Graph

    private lateinit var parallelFrontierBfs: Bfs
    private lateinit var parallelBfs: Bfs

    @Setup
    fun setup() {
        val graphSize = 2_000_000
        connections = min(graphSize * 10, 10_000_000)

        println("Генерация графа размером $graphSize с $connections рёбрами...")

        graph = RandomGraphGenerator().generateGraph(java.util.Random(42), graphSize, connections)

        println("Генерация завершена.")

        parallelBfs = ParallelBfs(runtime)
        parallelFrontierBfs = ParallelFrontierBfs(runtime)
    }

    @Benchmark
    fun parallelBfs(bh: Blackhole) {
        bh.consume(parallelBfs.bfs(graph, 0))
    }

    @Benchmark
    fun parallelFrontierBfs(bh: Blackhole) {
        bh.consume(parallelFrontierBfs.bfs(graph, 0))
    }
}
