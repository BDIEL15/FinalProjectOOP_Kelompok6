
package com.marsreboot.runtime;

import com.marsreboot.content.PuzzleCatalog;
import com.marsreboot.robot.RobotSimulation;
import com.marsreboot.robot.EvaluationStrategy;
import com.marsreboot.robot.HarvestEvaluation;
import com.marsreboot.robot.MatureHarvestEvaluation;
import com.marsreboot.robot.IrrigationEvaluation;
import java.util.*;

/** Candidate content; concept-use verification is separate from world success. */
public enum PuzzleDefinition {
    ONE(1, 2), TWO(2, 4), THREE(3, 4), FOUR(4, 4);
    private final int number, columns;

    PuzzleDefinition(int number, int columns) {
        this.number = number;
        this.columns = columns;
    }

    public int number() { return number; }
    public int columns() { return columns; }

    public int targetCount() {
        return this == THREE || this == FOUR ? 2 : columns;
    }

    public int actionLimit() {
        return contentDefinition().getScenarios().get(0).getActionLimit();
    }

    /** Fewest total robot actions (all scenarios) of the reference solution; see PuzzleProgressTest. */
    public int referenceActions() {
        return switch (this) {
            case ONE -> 3;
            case TWO -> 7;
            case THREE -> 5;
            case FOUR -> 10;
        };
    }

    public int totalActionLimit() {
        return contentDefinition().getScenarios()
            .stream()
            .mapToInt(scenario -> scenario.getActionLimit())
            .sum();
    }

    /** Efficiency rating for a passing run: 3 = reference-optimal, 2 = within halfway to the limit, 1 = passes. Display only. */
    public int efficiencyStars(int totalActions) {
        if (totalActions <= referenceActions()) return 3;
        return totalActions <= (referenceActions() + totalActionLimit()) / 2 ? 2 : 1;
    }

    public String scenarioId() {
        return scenarioId(0);
    }

    public String scenarioId(int index) {
        List<com.marsreboot.content.ScenarioFixture> scenarios =
            contentDefinition().getScenarios();

        if (index < 0 || index >= scenarios.size()) {
            throw new IllegalArgumentException("Scenario index");
        }

        return scenarios.get(index).getScenarioId();
    }

    public int scenarioCount() {
        return contentDefinition().getScenarios().size();
    }

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

    /** Field-log story notes (3 lines) shown in the [J] log for this puzzle. */
    public List<String> logNotes() {
        return switch (this) {
            case ONE -> List.of("Lokasi: Bio-Dome sektor timur. Dua tanaman matang masih bisa diselamatkan.",
                "Rover menunggu perintah. Klik rover untuk membuka editor kode.",
                "Batas: " + actionLimit() + " aksi. Perintah pertama: harvest() dan move().");

            case TWO -> List.of("Panen pertama berhasil, tetapi ada empat tanaman berjajar di bed.",
                "Menulis perintah yang sama empat kali melelahkan. Ulangi dengan for.",
                "Batas: " + actionLimit() + " aksi. Loop adalah latihan; penggunaannya belum dinilai otomatis.");

            case THREE -> List.of("Sebagian tanaman masih muda. Memanennya sekarang merusak kebun.",
                "Sensor can_harvest() membaca petak aktif tanpa memakai aksi.",
                "Batas: " + actionLimit() + " aksi. Mencoba panen tanaman muda membuat percobaan gagal.");

            case FOUR -> List.of("Panel air menunjukkan jatah terbatas: 2 unit per skenario.",
                "Hanya sebagian petak yang kering, dan posisinya berbeda di tiap skenario.",
                "Batas: " + actionLimit() + " aksi dan 2 air per skenario. Satu kode harus lulus keduanya.");
        };
    }

    public static final int HINT_TIERS = 3;

    /** Progressive help, tier 1..3: concept, relevant API, partial skeleton. Never the full reference solution. */
    public String hint(int tier) {
        if (tier < 1 || tier > HINT_TIERS) throw new IllegalArgumentException("Hint tier");

        return switch (this) {
            case ONE -> switch (tier) {
                case 1 -> "Panen tanaman di petak rover, lalu pindah ke petak berikutnya.";
                case 2 -> "harvest() memanen petak aktif; move(\"east\") menggeser rover satu petak.";
                default -> "harvest()\nmove(\"east\")\n# panen lagi di petak kedua";
            };

            case TWO -> switch (tier) {
                case 1 -> "Empat tanaman berjajar: pekerjaan yang sama diulang empat kali.";
                case 2 -> "for x in range(4): mengulang blok di bawahnya. Jangan bergerak setelah petak terakhir.";
                default -> "for x in range(4):\n    harvest()\n    # move east hanya jika x < 3";
            };

            case THREE -> switch (tier) {
                case 1 -> "Tanaman muda tidak boleh dipanen. Periksa dulu sebelum memanen.";
                case 2 -> "can_harvest() bernilai True hanya pada tanaman matang dan tidak memakai aksi.";
                default -> "for x in range(4):\n    if can_harvest():\n        # panen di sini\n    # lalu gerak ke petak berikutnya";
            };

            case FOUR -> switch (tier) {
                case 1 -> "Air hanya 2 unit per skenario. Siram hanya petak yang kering.";
                case 2 -> "needs_water() membaca petak aktif. water() pada petak lembap membuang air.";
                default -> "for x in range(4):\n    if needs_water():\n        # siram di sini\n    # lalu gerak ke petak berikutnya";
            };
        };
    }

    private com.marsreboot.content.PuzzleDefinition contentDefinition() {
        return switch (this) {
            case ONE -> PuzzleCatalog.createPuzzle1();
            case TWO -> PuzzleCatalog.createPuzzle2();
            case THREE -> PuzzleCatalog.createPuzzle3();
            case FOUR -> PuzzleCatalog.createPuzzle4();
        };
    }

    public RobotSimulation.Fixture fixture() { return fixture(0); }

    public RobotSimulation.Fixture fixture(int index) {
        scenarioId(index); // Validate before creating content.

        com.marsreboot.content.ScenarioFixture scenario =
            contentDefinition().getScenarios().get(index);

        Map<RobotSimulation.Position, RobotSimulation.Crop> crops =
            new HashMap<>();

        Set<RobotSimulation.Position> blocked = new HashSet<>();
        Set<RobotSimulation.Position> targets = new HashSet<>();
        Set<RobotSimulation.Position> dry = new HashSet<>();

        for (com.marsreboot.content.TileFixture tile : scenario.getTiles()) {
            RobotSimulation.Position position =
                new RobotSimulation.Position(tile.getX(), tile.getY());

            if (!tile.isPassable()) {
                blocked.add(position);
                continue;
            }

            switch (tile.getCropState()) {
                case EMPTY -> {
                    // No crop on this tile.
                }
                case YOUNG -> {
                    crops.put(position, RobotSimulation.Crop.YOUNG);
                }
                case RIPE -> {
                    crops.put(position, RobotSimulation.Crop.RIPE);
                    targets.add(position);
                }
            }

            if (tile.needsWater()) {
                dry.add(position);
            }
        }

        return new RobotSimulation.Fixture(
            scenario.getWidth(),
            scenario.getHeight(),
            new RobotSimulation.Position(
                scenario.getRobotStartX(),
                scenario.getRobotStartY()
            ),
            crops,
            blocked,
            targets,
            scenario.getActionLimit(),
            scenario.getApiCallLimit(),
            dry,
            scenario.getInitialWaterSupply()
        );
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
