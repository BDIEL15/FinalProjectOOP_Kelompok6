package com.marsreboot.robot;

import java.util.*;
import static com.marsreboot.robot.RobotSimulation.*;

public final class WaterSimulationTest {
    private static int checks;
    public static void main(String[] args) {
        Position a=new Position(0,0), b=new Position(2,0);
        var dry=new HashSet<>(Set.of(a,b));
        var fixture=new Fixture(4,1,a,Map.of(),Set.of(),Set.of(),12,1024,dry,2);
        dry.clear();
        var robot=new RobotSimulation(fixture);
        check(robot.needsWater() && robot.snapshot().actionCount()==0,"sensor does not spend action");
        var before=robot.snapshot();
        check(robot.water(),"water on dry succeeds");
        check(!robot.needsWater() && robot.snapshot().remainingTargets().equals(Set.of(b)),"dry becomes moist");
        check(robot.water(),"watering moist still spends water");
        check(robot.snapshot().waterRemaining()==0 && robot.snapshot().waterUsed()==2,"water ledger");
        check(!robot.water() && robot.snapshot().waterRemaining()==0 && robot.snapshot().waterUsed()==2,"no negative water");
        check(robot.snapshot().actionCount()==3,"failed action is charged");
        check(robot.finish().outcome()==Outcome.INCOMPLETE,"wasting water leaves dry target");
        check(before.dryCells().size()==2 && before.waterRemaining()==2,"snapshot does not change later");
        try {before.dryCells().clear();throw new AssertionError("mutable snapshot");}catch(UnsupportedOperationException expected){checks++;}
        robot.reset();
        check(robot.snapshot().actionCount()==0 && robot.snapshot().apiCallCount()==0 && robot.snapshot().waterRemaining()==2,"reset counters/water");
        check(robot.snapshot().dryCells().equals(Set.of(a,b)),"fixture copied defensively");
        robot.water();robot.move("east");robot.move("east");robot.water();robot.move("east");
        check(robot.finish().outcome()==Outcome.SUCCESS && robot.snapshot().actionCount()==5,"reference costs five actions");
        check(robot.snapshot().waterUsed()==2 && robot.snapshot().dryCells().isEmpty(),"reference uses two water");
        robot.reset();for(int i=0;i<12;i++)robot.water();
        int used=robot.snapshot().waterUsed();
        try{robot.water();throw new AssertionError("action cap bypassed");}catch(IllegalStateException expected){checks++;}
        check(robot.snapshot().outcome()==Outcome.LIMIT_EXCEEDED && robot.snapshot().waterUsed()==used,"over-limit does not spend more water");
        robot.reset();for(int i=0;i<1024;i++)robot.needsWater();
        try{robot.needsWater();throw new AssertionError("sensor cap bypassed");}catch(IllegalStateException expected){checks++;}
        check(robot.snapshot().apiCallCount()==1024 && robot.snapshot().waterRemaining()==2,"sensor cap has no water mutation");
        try{new Fixture(4,1,a,Map.of(),Set.of(),Set.of(),12,1024,Set.of(b),-1);throw new AssertionError("negative supply");}catch(IllegalArgumentException expected){checks++;}
        try{new Fixture(4,1,a,Map.of(),Set.of(),Set.of(),12,1024,Set.of(new Position(4,0)),2);throw new AssertionError("outside target");}catch(IllegalArgumentException expected){checks++;}
        var planted=new RobotSimulation(new Fixture(4,1,a,Map.of(a,Crop.RIPE),Set.of(),Set.of(),12,1024,Set.of(a),2));
        planted.harvest();
        check(planted.finish().outcome()==Outcome.INCOMPLETE && planted.snapshot().dryCells().contains(a),"harvesting cannot satisfy irrigation goal");
        System.out.println("PASS: "+checks+" water domain assertions (no Python/OpenGL).");
    }
    private static void check(boolean value,String message){if(!value)throw new AssertionError(message);checks++;}
}
