package com.marsreboot.runtime;

import com.marsreboot.robot.RobotSimulation;
import com.marsreboot.runtime.RunCoordinator.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.BooleanSupplier;

/** Public controller and wire behavior, without Python or OpenGL. */
public final class RuntimeTest {
    private static int assertions;
    public static void main(String[] args) throws Exception {
        protocolRejectsBadFrames();
        replayCommitsOnlyAtEnd();
        cancelledWorkerCannotPublish();
        unreapedProcessBlocksNextRun();
        System.out.println("PASS: " + assertions + " controller/protocol assertions.");
    }
    private static Execution reference() {
        var robot = new RobotSimulation(RobotSimulation.Fixture.puzzleOne());
        List<RobotSimulation.Snapshot> trace = new ArrayList<>();
        robot.harvest(); trace.add(robot.snapshot()); robot.move("east"); trace.add(robot.snapshot());
        robot.harvest(); trace.add(robot.snapshot());
        return new Execution(Status.SUCCESS,robot.finish(),trace,"done","Semua target selesai.");
    }
    private static void protocolRejectsBadFrames() throws Exception {
        String good = new RuntimeProtocol(7).encode("ready",RuntimeProtocol.object());
        check(new RuntimeProtocol(7).decode(good).getString("type").equals("ready"),"valid identity");
        for (String changed : List.of(good.replace("\"generation\":7","\"generation\":8"),
                good.replace("\"protocolVersion\":1","\"protocolVersion\":2"),
                good.replace("\"sequence\":1","\"sequence\":3"), good.replace("p1-a","p2-a"),
                good.replace("run-7","run-6"), good.replace("\"sequence\":1","\"sequence\":true"), "{}", "{bad")) {
            throwsError(() -> new RuntimeProtocol(7).decode(changed),"invalid wire rejected");
        }
        var protocol = new RuntimeProtocol(7); protocol.decode(good);
        throwsError(() -> protocol.decode(good),"duplicate sequence rejected");
        check(RuntimeProtocol.readLine(stream("é\n"),4).equals("é"),"UTF-8 frame decoded");
        check(RuntimeProtocol.readLine(stream(""),4)==null,"clean EOF");
        throwsError(() -> RuntimeProtocol.readLine(stream("abc"),4),"partial frame rejected");
        throwsError(() -> RuntimeProtocol.readLine(stream("abcd\n"),4),"byte cap before parsing");
        throwsError(() -> RuntimeProtocol.readLine(new ByteArrayInputStream(new byte[]{(byte)255,10}),4),"malformed UTF-8 rejected");
    }
    private static void replayCommitsOnlyAtEnd() throws Exception {
        try (var coordinator = new RunCoordinator((s,id,c,r) -> { r.run(); return reference(); })) {
            check(coordinator.run("harvest()"),"run accepted");
            await(() -> { coordinator.update(0,false); return coordinator.view().status()==Status.REPLAYING; });
            check(!coordinator.view().completed(),"no progress before replay");
            check(!coordinator.run(""),"cannot overlap replay");
            coordinator.update(0.35f,false);
            check(coordinator.view().snapshot().harvestCount()==1,"first harvest displayed");
            coordinator.update(0.35f,true);
            check(coordinator.view().snapshot().robot().x()==1,"move displayed with reduced motion");
            check(!coordinator.view().completed(),"not yet complete after two actions");
            coordinator.update(0.35f,true);
            check(coordinator.view().status()==Status.SUCCESS && coordinator.view().completed(),"success only after final step");
            coordinator.reset();
            check(coordinator.view().completed(),"reset preserves completion");
            check(coordinator.view().snapshot().remainingTargets().size()==2,"reset restores targets");
            check(!coordinator.run("é".repeat(8193)),"source capped in UTF-8 bytes");
            check(coordinator.run("#".repeat(16384)),"exact source limit allowed");
            await(() -> {coordinator.update(0,false);return coordinator.view().status()==Status.REPLAYING;});
            coordinator.update(0.35f,false); var last=coordinator.view().snapshot(); coordinator.stop();
            coordinator.update(1,false);
            check(coordinator.view().status()==Status.STOPPED && coordinator.view().snapshot().equals(last),"stop freezes visible replay");
        }
    }
    private static void cancelledWorkerCannotPublish() throws Exception {
        CountDownLatch entered=new CountDownLatch(1), release=new CountDownLatch(1);
        var coordinator=new RunCoordinator((s,id,c,r) -> {
            entered.countDown(); try { release.await(); } catch(InterruptedException e) { throw new RuntimeException(e); }
            r.run(); return reference(); // Deliberately ignores cancellation, like a late result.
        });
        try {
            coordinator.run("old"); check(entered.await(2,TimeUnit.SECONDS),"worker entered");
            coordinator.stop(); check(!coordinator.run("new"),"cleanup prevents overlapping process");
            coordinator.reset(); long epoch=coordinator.view().generation(); release.countDown();
            await(() -> { coordinator.update(0,false); return coordinator.view().canRun(); });
            check(coordinator.view().status()==Status.READY && !coordinator.view().completed(),"late success ignored");
            check(coordinator.view().generation()==epoch && coordinator.view().snapshot().actionCount()==0,"late frames cannot change reset");
        } finally { release.countDown();coordinator.close(); }
        check(!coordinator.view().canRun(),"closed coordinator rejects runs");
    }
    private static void unreapedProcessBlocksNextRun() throws Exception {
        try (var coordinator=new RunCoordinator((s,id,c,r) -> new Execution(Status.ERROR,reference().snapshot(),List.of(),"","process alive",false))) {
            coordinator.run(""); await(() -> { coordinator.update(0,false); return coordinator.view().status()==Status.ERROR; });
            coordinator.reset(); check(!coordinator.view().canRun() && !coordinator.run(""),"reset cannot bypass failed process cleanup");
        }
    }
    static void await(BooleanSupplier condition) throws Exception {
        long deadline=System.nanoTime()+TimeUnit.SECONDS.toNanos(4);
        while(!condition.getAsBoolean()) {if(System.nanoTime()>deadline)throw new AssertionError("wait timed out");Thread.sleep(5);}
    }
    private static InputStream stream(String text) {return new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));}
    @FunctionalInterface private interface Checked { void run() throws Exception; }
    private static void throwsError(Checked action,String message) {try {action.run();throw new AssertionError(message);}catch(Exception expected){assertions++;}}
    private static void check(boolean value,String message) {if(!value)throw new AssertionError(message);assertions++;}
}
