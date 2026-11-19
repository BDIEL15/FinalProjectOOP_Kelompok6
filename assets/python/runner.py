"""Thin trusted-learning adapter. Java owns all puzzle rules. This is not a sandbox."""
import builtins
import json
import sys
import traceback

transport_in = sys.stdin
transport_out = sys.stdout
packet = json.loads(transport_in.buffer.readline(128 * 1024 + 1))
identity = {key: packet[key] for key in ("protocolVersion", "generation", "runId", "scenarioId")}
out_sequence = 0
in_sequence = packet["sequence"]
request_id = 0


def send(kind, payload):
    global out_sequence
    out_sequence += 1
    frame = dict(identity, sequence=out_sequence, type=kind, payload=payload)
    transport_out.write(json.dumps(frame, ensure_ascii=False) + "\n")
    transport_out.flush()


class Output:
    encoding = "utf-8"

    def __init__(self):
        self.used = 0
        self.lines = 0
        self.truncated = False

    def write(self, text):
        # Bound the transport even when a program repeatedly prints after the cap.
        count = len(text)
        if self.truncated:
            return count
        accepted = []
        for char in text:
            size = len(char.encode("utf-8", errors="replace"))
            if self.used + size > 8192 or self.lines >= 64:
                if accepted:
                    send("output", {"text": "".join(accepted)})
                self.truncated = True
                send("output_truncated", {})
                return count
            accepted.append(char)
            self.used += size
            self.lines += char == "\n"
            if len(accepted) >= 512:
                send("output", {"text": "".join(accepted)})
                accepted.clear()
        if accepted:
            send("output", {"text": "".join(accepted)})
        return count

    def flush(self):
        pass


def call(name, args):
    global request_id, in_sequence
    request_id += 1
    send("request", {"requestId": request_id, "name": name, "args": args})
    raw = transport_in.buffer.readline(16 * 1024 + 1)
    if not raw or len(raw) > 16 * 1024:
        raise RuntimeError("Java response missing or too large")
    response = json.loads(raw)
    if (any(response.get(k) != v for k, v in identity.items())
            or response.get("sequence") != in_sequence + 1
            or response.get("type") != "response"
            or response["payload"].get("requestId") != request_id):
        raise RuntimeError("Invalid Java response identity/sequence")
    in_sequence += 1
    payload = response["payload"]
    if not payload.get("ok"):
        raise RuntimeError(payload.get("message", "Java rejected API call"))
    return payload["value"]


def move(direction):
    return call("move", [direction])


def harvest():
    return call("harvest", [])


def can_harvest():
    return call("can_harvest", [])


def water():
    return call("water", [])


def needs_water():
    return call("needs_water", [])


def get_pos():
    return tuple(call("get_pos", []))


sys.stdout = sys.stderr = Output()
send("ready", {})
try:
    namespace = dict(__builtins__=builtins, __name__="__main__", move=move, harvest=harvest,
                     can_harvest=can_harvest, get_pos=get_pos, water=water, needs_water=needs_water)
    exec(compile(packet["payload"]["source"], "solusi_p1.py", "exec"), namespace)
except BaseException as error:
    line = getattr(error, "lineno", None)
    if line is None:
        for frame in traceback.extract_tb(error.__traceback__):
            if frame.filename == "solusi_p1.py":
                line = frame.lineno
    send("error", {"message": (type(error).__name__ + ": " + str(error))[:1000], "line": line})
    sys.exit(1)
else:
    send("finished", {})
