import pathlib
import sys
import unittest

ROOT = pathlib.Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / "src" / "python"))
from simulation import FarmSimulation, RunFinished
from runner import run_source, LimitedOutput

STARTER = (ROOT / "src" / "python" / "starter.py").read_text()


class SimulationTests(unittest.TestCase):
    def test_fixture(self):
        sim = FarmSimulation()
        self.assertEqual((sim.x, sim.y, sim.tick, sim.harvested), (0, 0, 0, 0))
        self.assertEqual(sim.ripe_at, [0 if (x + y) % 2 == 0 else 8 for y in range(6) for x in range(6)])
        self.assertEqual(sim.snapshot()["cells"].count("ripe"), 18)

    def test_directions_and_edges(self):
        sim = FarmSimulation()
        self.assertFalse(sim.move("west"))
        self.assertFalse(sim.move("south"))
        for direction, position in [("east", (1, 0)), ("north", (1, 1)), ("west", (0, 1)), ("south", (0, 0))]:
            self.assertTrue(sim.move(direction))
            self.assertEqual(sim.get_pos(), position)
        sim.x = sim.y = 5
        self.assertFalse(sim.move("north"))
        self.assertFalse(sim.move("east"))
        self.assertEqual(sim.tick, 8)

    def test_invalid_arguments_unchanged(self):
        for value in ["North", "up", "", None, 1, [], {}]:
            sim = FarmSimulation()
            with self.assertRaises((ValueError, TypeError)):
                sim.move(value)
            self.assertEqual(sim.tick, 0)
        for source in ['move()', 'move("north", "east")', 'wait(1)', 'harvest(1)', 'get_pos(1)', 'can_harvest(1)']:
            result = run_source(source)
            self.assertEqual(result["outcome"], "error")
            self.assertEqual(result["snapshot"]["tick"], 0)

    def test_harvest_success_and_empty_cost(self):
        sim = FarmSimulation()
        self.assertTrue(sim.harvest())
        self.assertFalse(sim.harvest())
        self.assertEqual((sim.tick, sim.harvested), (4, 1))
        self.assertEqual(sim.snapshot()["cells"][0], "empty")

    def test_harvest_uses_pre_cost_tick(self):
        sim = FarmSimulation()
        sim.x, sim.tick = 1, 7
        self.assertFalse(sim.harvest())
        self.assertEqual((sim.tick, sim.harvested), (9, 0))
        self.assertTrue(sim.harvest())

    def test_sensor_and_wait(self):
        sim = FarmSimulation()
        sim.x, sim.tick = 1, 7
        for _ in range(100):
            self.assertFalse(sim.can_harvest())
            self.assertEqual(sim.get_pos(), (1, 0))
        self.assertEqual(sim.tick, 7)
        self.assertIsNone(sim.wait())
        self.assertTrue(sim.can_harvest())

    def test_starter_baseline_and_repeatability(self):
        events = []
        result = run_source(STARTER, events.append)
        self.assertEqual(result["outcome"], "success")
        self.assertEqual(result["stats"], {"move": 35, "harvest": 36, "wait": 5})
        self.assertEqual(result["snapshot"], {"tick": 112, "x": 0, "y": 5, "harvested": 36, "cells": ["empty"] * 36})
        self.assertEqual(len(events), 76)
        self.assertEqual(result, run_source(STARTER))
        for i, event in enumerate(events):
            self.assertEqual(event["sequence"], i + 1)
            self.assertEqual(event["snapshot"]["tick"], event["after"])

    def test_success_at_200_precedes_budget(self):
        sim = FarmSimulation()
        sim.tick = 198
        sim.picked = [True] * 36
        sim.picked[0] = False
        sim.harvested = 35
        with self.assertRaises(RunFinished) as finished:
            sim.harvest()
        self.assertEqual(finished.exception.outcome, "success")
        self.assertEqual(sim.tick, 200)

    def test_non_success_at_200(self):
        sim = FarmSimulation()
        sim.tick = 199
        with self.assertRaises(RunFinished) as finished:
            sim.wait()
        self.assertEqual(finished.exception.outcome, "budget_exceeded")
        self.assertEqual(sim.tick, 200)

    def test_over_budget_does_not_apply(self):
        events = []
        sim = FarmSimulation(events.append)
        sim.tick = 199
        before = sim.snapshot()
        with self.assertRaises(RunFinished):
            sim.harvest()
        self.assertEqual(sim.snapshot(), before)
        self.assertEqual(events, [])

    def test_normal_end_incomplete(self):
        self.assertEqual(run_source('harvest()')["outcome"], "incomplete")
        self.assertEqual(run_source('')["snapshot"]["tick"], 0)

    def test_python_features_and_fresh_namespace(self):
        text = []
        result = run_source('def step():\n    return get_pos()\na = [step() for _ in range(3)]\nprint(a)', emit_output=text.append)
        self.assertEqual(result["outcome"], "incomplete")
        self.assertIn("(0, 0)", "".join(text))
        self.assertEqual(run_source('print(a)')["outcome"], "error")

    def test_syntax_and_runtime_locations(self):
        for source, line in [('if True\n    wait()', 1), ('wait()\n1/0', 2)]:
            result = run_source(source)
            self.assertEqual(result["outcome"], "error")
            self.assertEqual(result["error"]["line"], line)

    def test_stdout_is_bounded_and_safe_text(self):
        output = []
        result = run_source('for _ in range(10000):\n    print("<script>hello</script>")', emit_output=output.append)
        text = "".join(output)
        self.assertEqual(result["outcome"], "incomplete")
        self.assertLessEqual(len(text.encode()), 16384)
        self.assertLessEqual(text.count("\n"), 200)
        self.assertEqual(text.count("Output dipotong"), 1)
        self.assertEqual(text.count("<script>hello</script>"), 199)

    def test_unicode_byte_limit(self):
        chunks = []
        stream = LimitedOutput(chunks.append)
        stream.write("🌱" * 10000)
        stream.flush()
        self.assertGreater(stream.bytes, 16000)
        self.assertLessEqual(len("".join(chunks).encode()), 16384)

    def test_output_restored_and_source_limit(self):
        original = sys.stdout
        run_source('raise RuntimeError("error")')
        self.assertIs(sys.stdout, original)
        self.assertEqual(run_source("#" * 32769)["outcome"], "error")


if __name__ == "__main__":
    unittest.main()
