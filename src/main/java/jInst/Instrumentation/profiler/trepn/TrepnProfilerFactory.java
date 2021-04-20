package jInst.Instrumentation.profiler.trepn;

import jInst.Instrumentation.profiler.MethodOrientedProfiler;
import jInst.Instrumentation.profiler.ProfilerAbstractFactory;
import jInst.Instrumentation.profiler.TestOrientedProfiler;

/**
 * Created by rrua on 17/06/17.
 */
public class TrepnProfilerFactory  implements ProfilerAbstractFactory {

    @Override
    public TestOrientedProfiler createTestOrientedProfiler() {
        return new TrepnTestOrientedProfiler();
    }

    @Override
    public MethodOrientedProfiler createMethodOrientedProfiler() {
        return new TrepnMethodOrientedProfiler();
    }
}
