"""Deterministic rules-v1. No browser or clock dependencies."""

RULES_VERSION = 1


class RunFinished(BaseException):
    def __init__(self, outcome):
        self.outcome = outcome


class FarmSimulation:
    def __init__(self, emit=lambda event: None):
        self.x = self.y = self.tick = self.harvested = self.sequence = 0
        self.ripe_at = [0 if (x + y) % 2 == 0 else 8
                        for y in range(6) for x in range(6)]
        self.picked = [False] * 36
        self.stats = {"move": 0, "harvest": 0, "wait": 0}
        self.outcome = None
        self.emit = emit

    def snapshot(self):
        return {"tick": self.tick, "x": self.x, "y": self.y,
                "harvested": self.harvested,
                "cells": ["empty" if self.picked[i] else
                          "ripe" if self.tick >= ripe else "young"
                          for i, ripe in enumerate(self.ripe_at)]}

    def _active(self):
        if self.outcome:
            raise RunFinished(self.outcome)

    def _finish(self, outcome):
        self.outcome = outcome
        raise RunFinished(outcome)

    def _action(self, name, cost, argument=None):
        self._active()
        if self.tick + cost > 200:
            self._finish("budget_exceeded")
        before = self.tick
        ok = True
        if name == "move":
            dx, dy = {"north": (0, 1), "east": (1, 0),
                      "south": (0, -1), "west": (-1, 0)}[argument]
            ok = 0 <= self.x + dx < 6 and 0 <= self.y + dy < 6
            if ok:
                self.x += dx
                self.y += dy
        elif name == "harvest":
            ok = self.can_harvest()
            if ok:
                self.picked[self.y * 6 + self.x] = True
                self.harvested += 1
        self.tick += cost
        self.stats[name] += 1
        self.sequence += 1
        self.emit({"sequence": self.sequence, "action": name,
                   "argument": argument, "ok": ok, "before": before,
                   "after": self.tick, "snapshot": self.snapshot()})
        if self.harvested == 36:
            self._finish("success")
        if self.tick == 200:
            self._finish("budget_exceeded")
        return ok

    def move(self, direction):
        self._active()
        if not isinstance(direction, str):
            raise TypeError("direction harus berupa string")
        if direction not in ("north", "east", "south", "west"):
            raise ValueError('direction harus "north", "east", "south", atau "west"')
        return self._action("move", 1, direction)

    def can_harvest(self):
        self._active()
        index = self.y * 6 + self.x
        return not self.picked[index] and self.tick >= self.ripe_at[index]

    def harvest(self):
        return self._action("harvest", 2)

    def get_pos(self):
        self._active()
        return self.x, self.y

    def wait(self):
        self._action("wait", 1)
