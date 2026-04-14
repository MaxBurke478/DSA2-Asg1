package org.example;

import finds.UnionFind;
import model.BlueBox;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 3)
@Measurement(iterations = 5)
@Fork(1)
public class LeafBenchmark {

    private UnionFind uf;

    @Setup
    public void setup() {
        uf = new UnionFind(1000000);
    }

    @Benchmark
    public void benchmarkUnionFind() {
        for (int i = 0; i < 999999; i++) {
            uf.union(i, i + 1);
        }
    }

    @Benchmark
    public int benchmarkFind() {
        return uf.find(999999);
    }

    @Benchmark
    public BlueBox benchmarkBlueBoxExpansion() {
        BlueBox bb = new BlueBox(0, 0);
        for (int i = 0; i < 10000; i++) {
            bb.expandForPixels(i, i);
        }
        return bb;
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(LeafBenchmark.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }
}
