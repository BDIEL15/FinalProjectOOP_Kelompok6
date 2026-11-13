"""The same runner is used in CPython tests and Pyodide."""
import contextlib
import io
import traceback
from simulation import FarmSimulation, RunFinished


class LimitedOutput(io.TextIOBase):
    NOTICE = "[Output dipotong: batas 200 baris / 16 KiB.]\n"

    def __init__(self, emit):
        self.emit = emit
        self.bytes = self.lines = 0
        self.truncated = False
        self.pending = ""

    def flush(self):
        if self.pending:
            self.emit(self.pending)
            self.pending = ""

    def write(self, text):
        length = len(text)
        if self.truncated:
            return length
        byte_budget = 16384 - len(self.NOTICE.encode("utf-8"))
        remaining = byte_budget - self.bytes
        accepted = text.encode("utf-8")[:remaining].decode("utf-8", "ignore")
        pieces = accepted.splitlines(keepends=True)
        accepted = "".join(pieces[:max(0, 199 - self.lines)])
        self.lines += accepted.count("\n")
        self.bytes += len(accepted.encode("utf-8"))
        self.pending += accepted
        if len(self.pending) >= 1024:
            self.flush()
        if accepted != text or self.bytes >= byte_budget or self.lines >= 199:
            self.flush()
            self.emit(self.NOTICE)
            self.truncated = True
        return length


def run_source(source, emit_event=lambda event: None, emit_output=lambda text: None):
    sim = FarmSimulation(emit_event)
    output = LimitedOutput(emit_output)
    error = None
    outcome = "incomplete"
    namespace = {"__name__": "__main__"}
    for name in ("move", "can_harvest", "harvest", "get_pos", "wait"):
        namespace[name] = getattr(sim, name)
    try:
        if len(source.encode("utf-8")) > 32768:
            raise ValueError("Source melebihi batas 32 KiB")
        with contextlib.redirect_stdout(output), contextlib.redirect_stderr(output):
            exec(compile(source, "player.py", "exec"), namespace)
    except RunFinished as finished:
        outcome = finished.outcome
    except BaseException as exc:
        outcome = "error"
        line = exc.lineno if isinstance(exc, SyntaxError) else None
        for frame in traceback.extract_tb(exc.__traceback__):
            if frame.filename == "player.py":
                line = frame.lineno
        error = {"message": f"{type(exc).__name__}: {exc}"[:1000], "line": line}
    finally:
        output.flush()
    return {"outcome": outcome, "snapshot": sim.snapshot(),
            "stats": sim.stats.copy(), "error": error}
