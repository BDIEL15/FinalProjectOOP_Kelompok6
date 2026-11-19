package com.marsreboot.runtime;

import com.marsreboot.robot.RobotSimulation;
import com.marsreboot.robot.EvaluationStrategy;
import com.marsreboot.robot.HarvestEvaluation;
import com.marsreboot.robot.MatureHarvestEvaluation;
import com.marsreboot.robot.IrrigationEvaluation;
import java.util.*;

/** Candidate content; concept-use verification is separate from world success. */
public enum PuzzleDefinition {
    ONE(1, 2, 6), TWO(2, 4, 12), THREE(3, 4, 12), FOUR(4, 4, 12);

    private final int number, columns, actionLimit;
    PuzzleDefinition(int number, int columns, int actionLimit) {
        this.number = number; this.columns = columns; this.actionLimit = actionLimit;
    }
    public int number() { return number; }
    public int columns() { return columns; }
    public int targetCount() { return this == THREE || this == FOUR ? 2 : columns; }
    public int actionLimit() { return actionLimit; }
    public String scenarioId() { return scenarioId(0); }
    public String scenarioId(int index) {
        if (index < 0 || index >= scenarioCount()) throw new IllegalArgumentException("Scenario index");
        return "p" + number + (index == 0 ? "-a" : "-b");
    }
    public int scenarioCount() { return this == FOUR ? 2 : 1; }
    public boolean watering() { return this == FOUR; }
    public boolean rejectsYoungHarvest() { return this == THREE; }
    public EvaluationStrategy evaluationStrategy() {
        return switch (this) {
            case ONE, TWO -> new HarvestEvaluation();
            case THREE -> new MatureHarvestEvaluation(new HarvestEvaluation());
            case FOUR -> new IrrigationEvaluation();
        };
    }
    public String instruction() {
        if (this == FOUR) return "Siram semua petak kering di dua skenario dengan kode yang sama. Air 2 unit/skenario; gunakan needs_water().";
        return this == THREE ? "Panen matang di x=0,2. Jangan mencoba panen muda di x=1,3; gunakan can_harvest()."
            : this == TWO ? "Panen empat tanaman. Latihan for; penggunaan loop belum dinilai otomatis."
            : "Panen dua tanaman matang.";
    }
    public RobotSimulation.Fixture fixture() { return fixture(0); }
    public RobotSimulation.Fixture fixture(int index) {
        scenarioId(index); // Validate before creating content.
        if (this == FOUR) {
            Set<RobotSimulation.Position> dry = Set.of(new RobotSimulation.Position(index,0),new RobotSimulation.Position(index+2,0));
            return new RobotSimulation.Fixture(4,1,new RobotSimulation.Position(0,0),Map.of(),Set.of(),Set.of(),12,1024,dry,2);
        }
        Map<RobotSimulation.Position, RobotSimulation.Crop> crops = new HashMap<>();
        Set<RobotSimulation.Position> targets = new HashSet<>();
        for (int x = 0; x < columns; x++) {
            var position = new RobotSimulation.Position(x, 0);
            boolean young = this == THREE && x % 2 == 1;
            crops.put(position, young ? RobotSimulation.Crop.YOUNG : RobotSimulation.Crop.RIPE);
            if (!young) targets.add(position);
        }
        return new RobotSimulation.Fixture(columns, 1, new RobotSimulation.Position(0, 0), crops,
            Set.of(), targets, actionLimit, 1024);
    }
    public String starter() {
        if (this == ONE) return reference();
        if (this == FOUR) return "# Dua skenario, satu kode. Periksa petak kering sebelum water().\nfor x in range(4):\n    # Gunakan needs_water() di sini.\n    if x < 3:\n        move(\"east\")\n";
        if (this == THREE) return "# Panen hanya yang matang; can_harvest() membaca petak aktif.\nfor x in range(4):\n    # Periksa sensor sebelum harvest().\n    if x < 3:\n        move(\"east\")\n";
        return "# Panen empat tanaman. Coba gunakan for.\nfor x in range(4):\n    harvest()\n    # Bergerak east jika masih ada petak berikutnya.\n";
    }
    public String reference() {
        if (this == FOUR) return "for x in range(4):\n    if needs_water():\n        water()\n    if x < 3:\n        move(\"east\")";
        if (this == ONE) return "harvest()\nmove(\"east\")\nharvest()";
        if (this == THREE) return "for x in range(4):\n    if can_harvest():\n        harvest()\n    if x < 3:\n        move(\"east\")";
        return "for x in range(4):\n    harvest()\n    if x < 3:\n        move(\"east\")";
    }
}
