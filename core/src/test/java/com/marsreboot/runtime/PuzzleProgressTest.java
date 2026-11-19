package com.marsreboot.runtime;
import java.nio.file.Path;
public final class PuzzleProgressTest {
 public static void main(String[] args) throws Exception {
  var runtime = new PythonRuntime(Path.of(System.getProperty("mars.pythonDir"),"python.exe"));
  var p3 = runtime.execute(PuzzleDefinition.THREE, PuzzleDefinition.THREE.reference(), 3, new java.util.concurrent.atomic.AtomicBoolean(),()->{});
  check(p3.status()==RunCoordinator.Status.SUCCESS && p3.snapshot().actionCount()==5 && p3.snapshot().harvestCount()==2,"P3 sensor reference: five actions, two ripe crops");
  check(p3.snapshot().apiCallCount()==9 && p3.snapshot().invalidHarvestCount()==0,"four sensors, five actions");
  check(p3.snapshot().crops().get(new com.marsreboot.robot.RobotSimulation.Position(1,0))==com.marsreboot.robot.RobotSimulation.Crop.YOUNG,"young remains young");
  var naive = runtime.execute(PuzzleDefinition.THREE,PuzzleDefinition.TWO.reference(),4,new java.util.concurrent.atomic.AtomicBoolean(),()->{});
  check(naive.status()==RunCoordinator.Status.INCOMPLETE && naive.snapshot().remainingTargets().isEmpty() && naive.snapshot().invalidHarvestCount()==2,"all ripe harvested but young attempts fail");
  check(naive.snapshot().outcome()==com.marsreboot.robot.RobotSimulation.Outcome.INCOMPLETE && naive.message().contains("muda"),"domain and runtime agree about violation");
  check(runtime.execute(PuzzleDefinition.THREE,PuzzleDefinition.THREE.reference()+"\n1/0",9,new java.util.concurrent.atomic.AtomicBoolean(),()->{}).status()==RunCoordinator.Status.ERROR,"error after P3 reference");
  var p2 = runtime.execute(PuzzleDefinition.TWO, PuzzleDefinition.TWO.reference(), 5, new java.util.concurrent.atomic.AtomicBoolean(),()->{});
  if(p2.status()!=RunCoordinator.Status.SUCCESS || p2.snapshot().harvestCount()!=4 || p2.snapshot().actionCount()!=7) throw new AssertionError(p2);
  check(runtime.execute(PuzzleDefinition.TWO,"harvest()",6,new java.util.concurrent.atomic.AtomicBoolean(),()->{}).status()==RunCoordinator.Status.INCOMPLETE,"partial P2");
  check(runtime.execute(PuzzleDefinition.TWO,PuzzleDefinition.TWO.reference()+"\n1/0",7,new java.util.concurrent.atomic.AtomicBoolean(),()->{}).status()==RunCoordinator.Status.ERROR,"error after all P2 targets");
  check(runtime.execute(PuzzleDefinition.TWO,"for i in range(13): move('west')",8,new java.util.concurrent.atomic.AtomicBoolean(),()->{}).status()==RunCoordinator.Status.LIMIT_EXCEEDED,"12 action cap");
  try(var coordinator=new RunCoordinator(runtime)) {
   check(!coordinator.selectPuzzle(PuzzleDefinition.THREE),"P3 initially locked");
   check(!coordinator.selectPuzzle(PuzzleDefinition.TWO),"P2 initially locked");
   coordinator.run(PuzzleDefinition.ONE.reference());
   check(!coordinator.selectPuzzle(PuzzleDefinition.TWO),"cannot change active run");
   finish(coordinator);
   check(coordinator.isCompleted(PuzzleDefinition.ONE),"P1 unlock after replay");
   check(coordinator.selectPuzzle(PuzzleDefinition.TWO),"P2 unlocked");
   check(coordinator.view().snapshot().remainingTargets().size()==4 && !coordinator.view().completed(),"P2 isolated fixture/progress");
   check(!coordinator.selectPuzzle(PuzzleDefinition.THREE),"P3 locked until P2 success");
   coordinator.run(PuzzleDefinition.TWO.reference());finish(coordinator);
   check(coordinator.isCompleted(PuzzleDefinition.TWO),"P2 completion");
   coordinator.reset();check(coordinator.view().completed() && coordinator.view().snapshot().actionCount()==0,"P2 reset");
   check(coordinator.selectPuzzle(PuzzleDefinition.THREE),"P3 unlocked");
   coordinator.run(PuzzleDefinition.TWO.reference());finish(coordinator);
   check(!coordinator.isCompleted(PuzzleDefinition.THREE),"young violation gives no progress");
   coordinator.reset();coordinator.run(PuzzleDefinition.THREE.reference());finish(coordinator);
   check(coordinator.isCompleted(PuzzleDefinition.THREE) && coordinator.view().snapshot().actionCount()==5,"P3 reference commits after replay");
   coordinator.selectPuzzle(PuzzleDefinition.ONE);check(coordinator.view().completed() && coordinator.view().snapshot().remainingTargets().size()==2,"P1 remains intact");
  }
  var p2Protocol=new RuntimeProtocol(1,"p2-a");
  try {p2Protocol.decode(new RuntimeProtocol(1).encode("ready",RuntimeProtocol.object()));throw new AssertionError("P1 frame accepted for P2");}catch(IllegalArgumentException expected){}
  System.out.println("PASS: P1/P2/P3 references and 23 progression/sensor/negative checks (real bundled Python).");
 }
 static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
 static void finish(RunCoordinator coordinator)throws Exception {
  long deadline=System.nanoTime()+java.util.concurrent.TimeUnit.SECONDS.toNanos(8);
  while(!coordinator.view().canRun()) {
   coordinator.update(.35f,true);Thread.sleep(10);
   if(System.nanoTime()>deadline)throw new AssertionError("wait: "+coordinator.view());
  }
 }
}
