package com.marsreboot.runtime;

import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;

/** Actual bundled interpreter, public runtime boundary; no window required. */
public final class PythonIntegrationTest {
    private static int assertions;
    private static PythonRuntime runtime;
    public static void main(String[] args) throws Exception {
        runtime = new PythonRuntime(Path.of(System.getProperty("mars.pythonDir"), "python.exe"));
        var result = runtime.execute("harvest()\nmove('east')\nharvest()\nprint(get_pos())", 1,
            new AtomicBoolean(), () -> { });
        if (result.status() != RunCoordinator.Status.SUCCESS) throw new AssertionError(result);
        if (result.snapshot().actionCount() != 3) throw new AssertionError("reference costs three actions");
        if (!result.output().contains("(1, 0)")) throw new AssertionError("position comes from Java");
        check(result.trace().size()==3,"three action snapshots");
        check(result.reusable(),"process cleaned up");
        String configured=System.getProperty("mars.pythonDir");
        try {
            System.clearProperty("mars.pythonDir");
            check(PythonRuntime.bundled().execute("",2,new AtomicBoolean(),()->{}).status()==RunCoordinator.Status.INCOMPLETE,"runtime resolves beside JAR or from repo classes without PATH");
        } finally { System.setProperty("mars.pythonDir",configured); }
        check(run("").status()==RunCoordinator.Status.INCOMPLETE,"empty source incomplete");
        check(run("harvest()").snapshot().remainingTargets().size()==1,"partial target remains");
        check(run("assert can_harvest()\nassert get_pos() == (0, 0)\nprint('sensor')").snapshot().actionCount()==0,"sensors cost no actions");
        var syntax=run("if :");
        check(syntax.status()==RunCoordinator.Status.ERROR && syntax.message().contains("Baris 1"),"syntax line shown");
        check(run("1/0").message().contains("ZeroDivisionError"),"runtime error shown");
        check(run("harvest()\nmove('east')\nharvest()\n1/0").status()==RunCoordinator.Status.ERROR,"error after targets never succeeds");
        check(run("move('up')").status()==RunCoordinator.Status.ERROR,"bad direction rejected");
        check(run("move(3)").snapshot().actionCount()==0,"bad argument does not mutate world");
        check(run("harvest()\nmove('east')\nharvest()\nfor i in range(3): move('east')").status()==RunCoordinator.Status.SUCCESS,"exact action budget succeeds");
        check(run("for i in range(7): move('west')").status()==RunCoordinator.Status.LIMIT_EXCEEDED,"over action cap fails");
        check(run("while True: get_pos()").status()==RunCoordinator.Status.LIMIT_EXCEEDED,"sensor loop bounded");
        var flood=run("for i in range(100000): print('é' * 100)\nharvest()\nmove('east')\nharvest()");
        check(flood.status()==RunCoordinator.Status.SUCCESS,"output cap does not invalidate a valid program");
        check(flood.output().getBytes(java.nio.charset.StandardCharsets.UTF_8).length < 8300,"output bytes bounded");
        check(flood.output().indexOf("[Output dipotong") == flood.output().lastIndexOf("[Output dipotong"),"truncation notice once");
        check(run("for i in range(65): print(i)").output().contains("Output dipotong"),"line cap enforced");
        check(run("print('é' * 4096, end='')").output().getBytes(java.nio.charset.StandardCharsets.UTF_8).length==8192,"exact UTF-8 output byte cap");
        check(run("#".repeat(16384)).status()==RunCoordinator.Status.INCOMPLETE,"exact source byte cap");
        check(run("é".repeat(8193)).status()==RunCoordinator.Status.ERROR,"oversize source rejected");
        check(run("import sys\nsys.__stdout__.write('{}\\n')\nsys.__stdout__.flush()").status()==RunCoordinator.Status.ERROR,"forged frame rejected");
        check(run("import sys\nsys.__stdout__.write('x' * 16384 + '\\n')\nsys.__stdout__.flush()").status()==RunCoordinator.Status.ERROR,"oversized frame rejected");
        check(run("import os\nos._exit(0)").status()==RunCoordinator.Status.ERROR,"exit without finished rejected");
        check(new PythonRuntime(Path.of("missing-python.exe")).execute("",1,new AtomicBoolean(),()->{}).status()==RunCoordinator.Status.ERROR,"missing runtime recoverable");
        check(new PythonRuntime(Path.of(System.getProperty("mars.pythonDir"),"python.exe"),1,5000).execute("",1,new AtomicBoolean(),()->{}).status()==RunCoordinator.Status.TIMEOUT,"startup deadline enforced");
        check(run("import atexit, os\natexit.register(lambda: os._exit(1))\nharvest()\nmove('east')\nharvest()").status()==RunCoordinator.Status.ERROR,"nonzero exit after finished rejected");
        check(run("import os\nos.write(2,b'x' * 100000)\nharvest()\nmove('east')\nharvest()").status()==RunCoordinator.Status.SUCCESS,"stderr drained beyond cap without deadlock");
        long began=System.nanoTime();
        check(run("while True: pass").status()==RunCoordinator.Status.TIMEOUT,"CPU loop watchdog");
        check((System.nanoTime()-began)/1e9 < 9,"watchdog cleanup bounded");
        var cancelled=new AtomicBoolean();
        Thread stopper=new Thread(()->{try {Thread.sleep(200);}catch(InterruptedException e){Thread.currentThread().interrupt();}cancelled.set(true);});
        stopper.start(); began=System.nanoTime();
        check(runtime.execute("while True: pass",12,cancelled,()->{}).status()==RunCoordinator.Status.STOPPED,"Stop without API requests");
        check((System.nanoTime()-began)/1e9 < 3,"Stop cleanup prompt"); stopper.join();
        check(run("assert get_pos() == (0,0)\nharvest()\nmove('east')\nharvest()").status()==RunCoordinator.Status.SUCCESS,"Run after timeout/Stop uses clean fixture and namespace");
        System.out.println("PASS: " + assertions + " bundled Python integration assertions; executable=" + System.getProperty("mars.pythonDir"));
    }
    private static RunCoordinator.Execution run(String source) {return runtime.execute(source,42,new AtomicBoolean(),()->{});}
    private static void check(boolean value,String message) {if(!value)throw new AssertionError(message);assertions++;}
}
