package com.marsreboot.runtime;

import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;

/** Actual Python and Java simulation, no rendering. */
public final class WaterPuzzleTest {
    private static int checks;
    public static void main(String[] args) throws Exception {
        var runtime=new PythonRuntime(Path.of(System.getProperty("mars.pythonDir"),"python.exe"));
        var result=runtime.execute(PuzzleDefinition.FOUR,PuzzleDefinition.FOUR.reference(),100,new AtomicBoolean(),()->{});
        if(result.status()!=RunCoordinator.Status.SUCCESS || result.scenarios().size()!=2)throw new AssertionError(result);
        int actions=result.scenarios().stream().mapToInt(s->s.result().snapshot().actionCount()).sum();
        int water=result.scenarios().stream().mapToInt(s->s.result().snapshot().waterUsed()).sum();
        if(actions!=10 || water!=4)throw new AssertionError("Expected 5 actions/2 water per scenario");
        for(var scenario:result.scenarios()) {
            var state=scenario.result().snapshot();
            check(state.actionCount()==5 && state.waterUsed()==2 && state.waterRemaining()==0,"per-scenario fresh budget");
            check(state.apiCallCount()==9 && state.dryCells().isEmpty(),"sensor requests, dry targets");
        }
        String hardcode="water()\nmove('east')\nmove('east')\nwater()\nmove('east')";
        var hardcoded=runtime.execute(PuzzleDefinition.FOUR,hardcode,101,new AtomicBoolean(),()->{});
        check(hardcoded.status()==RunCoordinator.Status.INCOMPLETE && hardcoded.scenarios().size()==2,"hardcode fails second scenario");
        check(hardcoded.scenarios().get(0).result().status()==RunCoordinator.Status.SUCCESS,"hardcode passes first scenario only");
        check(hardcoded.message().contains("p4-b"),"failure identifies scenario B");
        var naive=runtime.execute(PuzzleDefinition.FOUR,"for x in range(4):\n    water()\n    if x < 3: move('east')",102,new AtomicBoolean(),()->{});
        check(naive.status()==RunCoordinator.Status.INCOMPLETE && naive.scenarios().size()==1,"wasteful solution fail-fast");
        var partial=runtime.execute(PuzzleDefinition.FOUR,"water()",103,new AtomicBoolean(),()->{});
        check(partial.status()==RunCoordinator.Status.INCOMPLETE && partial.scenarios().size()==1,"partial target fails");
        var error=runtime.execute(PuzzleDefinition.FOUR,PuzzleDefinition.FOUR.reference()+"\n1/0",104,new AtomicBoolean(),()->{});
        check(error.status()==RunCoordinator.Status.ERROR && error.scenarios().size()==1,"error after targets cannot pass");
        String fresh="try:\n    seen += 1\nexcept NameError:\n    seen = 1\nassert seen == 1\n"+PuzzleDefinition.FOUR.reference();
        check(runtime.execute(PuzzleDefinition.FOUR,fresh,105,new AtomicBoolean(),()->{}).status()==RunCoordinator.Status.SUCCESS,"namespace fresh per process");
        check(runtime.execute(PuzzleDefinition.ONE,"water()",106,new AtomicBoolean(),()->{}).status()==RunCoordinator.Status.ERROR,"water API unavailable on P1");
        var cancelled=new AtomicBoolean();var ready=new java.util.concurrent.atomic.AtomicInteger();
        var stopped=runtime.execute(PuzzleDefinition.FOUR,PuzzleDefinition.FOUR.reference(),107,cancelled,()->{if(ready.incrementAndGet()==2)cancelled.set(true);});
        check(stopped.status()==RunCoordinator.Status.STOPPED && stopped.scenarios().size()==2,"Stop during second process");
        var bounded=new PythonRuntime(Path.of(System.getProperty("mars.pythonDir"),"python.exe"),15000,1000);
        var timeout=bounded.execute(PuzzleDefinition.FOUR,"if not needs_water():\n    while True: pass\n"+PuzzleDefinition.FOUR.reference(),108,new AtomicBoolean(),()->{});
        check(timeout.status()==RunCoordinator.Status.TIMEOUT && timeout.scenarios().size()==2,"timeout second scenario after first passed");
        var flood=runtime.execute(PuzzleDefinition.FOUR,"for i in range(100): print('é' * 100)\n"+PuzzleDefinition.FOUR.reference(),109,new AtomicBoolean(),()->{});
        check(flood.status()==RunCoordinator.Status.SUCCESS && flood.output().getBytes(java.nio.charset.StandardCharsets.UTF_8).length<8300,"combined output remains bounded across scenarios");
        check(flood.scenarios().stream().allMatch(s->s.result().output().isEmpty()),"subresults do not retain duplicate output logs");
        var a=new RuntimeProtocol(5,"p4-a");var b=new RuntimeProtocol(5,"p4-b");
        try{b.decode(a.encode("ready",RuntimeProtocol.object()));throw new AssertionError("cross-scenario frame accepted");}catch(IllegalArgumentException expected){checks++;}
        try(var coordinator=new RunCoordinator(runtime)) {
            check(!coordinator.selectPuzzle(PuzzleDefinition.FOUR),"P4 initially locked");
            for(var p:java.util.List.of(PuzzleDefinition.ONE,PuzzleDefinition.TWO,PuzzleDefinition.THREE)) {
                check(coordinator.selectPuzzle(p),"prior puzzle selection");
                coordinator.run(p.reference());finish(coordinator);
                check(coordinator.view().completed(),"prior puzzle complete");
            }
            check(coordinator.selectPuzzle(PuzzleDefinition.FOUR),"P3 unlocks P4");
            coordinator.run(PuzzleDefinition.FOUR.reference());
            await(()->{coordinator.update(.35f,true);return coordinator.view().scenariosPassed()==1;});
            check(coordinator.view().scenarioNumber()==1 && coordinator.view().snapshot().robot().x()==3
                && coordinator.view().snapshot().dryCells().isEmpty(),"final frame of scenario A is presented before reset to B");
            await(()->{coordinator.update(.35f,true);return coordinator.view().scenarioNumber()==2;});
            check(!coordinator.view().completed() && coordinator.view().scenariosPassed()==1,"first pass cannot grant progress");
            check(coordinator.view().snapshot().waterRemaining()==2 && coordinator.view().totalActions()==5,"second replay starts from fresh water");
            coordinator.stop();check(!coordinator.view().completed() && coordinator.view().status()==RunCoordinator.Status.STOPPED,"Stop between replays blocks completion");
            coordinator.reset();check(coordinator.view().scenarioNumber()==1 && coordinator.view().scenariosPassed()==0,"reset all scenario progress");
            coordinator.run(hardcode);finish(coordinator);
            check(!coordinator.view().completed() && coordinator.view().scenariosPassed()==1,"second failure grants no progress");
            coordinator.run(PuzzleDefinition.FOUR.reference());finish(coordinator);
            check(coordinator.view().completed() && coordinator.view().scenariosPassed()==2,"all replays required for completion");
            check(coordinator.view().totalActions()==10 && coordinator.view().totalWaterUsed()==4,"aggregate metrics");
        }
        System.out.println("PASS: "+checks+" P4 multi-scenario/runtime/progression assertions; reference 10 actions / 4 water total.");
    }
    private static void check(boolean value,String message){if(!value)throw new AssertionError(message);checks++;}
    private static void finish(RunCoordinator coordinator)throws Exception {await(()->{coordinator.update(.35f,true);return coordinator.view().canRun();});}
    private static void await(java.util.function.BooleanSupplier predicate)throws Exception {
        long end=System.nanoTime()+java.util.concurrent.TimeUnit.SECONDS.toNanos(8);
        while(!predicate.getAsBoolean()){if(System.nanoTime()>end)throw new AssertionError("wait timed out");Thread.sleep(5);}
    }
}
