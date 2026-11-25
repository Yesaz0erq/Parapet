#!/usr/bin/env python3
"""Bake size-specific bathing, drinking and caged poses into editable bird projects via Blockbench MCP."""

import argparse
import json
from pathlib import Path
from urllib.request import Request, urlopen
import uuid


ROOT = Path(__file__).resolve().parents[1]
LIBRARIES = ROOT / "src/client/resources/assets/parapet/parrot_clips"
CLIP_NAMES = ("bathe", "drink", "cage_idle", "cage_feed", "stroke")


def compact(value):
    return json.dumps(value, separators=(",", ":"), ensure_ascii=False)


class Blockbench:
    def __init__(self, url):
        self.url = url
        self.session = None
        self.message_id = 0
        self.call("initialize", {"protocolVersion": "2025-03-26", "capabilities": {},
                                 "clientInfo": {"name": "ParapetBathAuthor", "version": "1"}})
        self.call("notifications/initialized", {})

    def call(self, method, params):
        message = {"jsonrpc": "2.0", "method": method, "params": params}
        if not method.startswith("notifications/"):
            self.message_id += 1
            message["id"] = self.message_id
        headers = {"Content-Type": "application/json", "Accept": "application/json, text/event-stream"}
        if self.session:
            headers["Mcp-Session-Id"] = self.session
        with urlopen(Request(self.url, data=compact(message).encode(), headers=headers), timeout=60) as response:
            self.session = response.headers.get("Mcp-Session-Id", self.session)
            body = response.read().decode()
        if not body:
            return None
        if body.lstrip().startswith(("data:", "event:")):
            body = next(line[5:] for line in body.splitlines() if line.startswith("data:"))
        result = json.loads(body)
        if "error" in result:
            raise RuntimeError(result["error"])
        return result.get("result")

    def evaluate(self, script):
        result = self.call("tools/call", {"name": "risky_eval", "arguments": {"code": script}})
        reply = result["content"][0]["text"]
        if result.get("isError"):
            raise RuntimeError(reply)
        return json.loads(json.loads(reply))


def model_clip(path, groups, clip):
    def uid(fragment):
        family = "bathing" if clip["name"] == "bathe" else "care/" + clip["name"]
        return str(uuid.uuid5(uuid.NAMESPACE_URL, "parapet:" + family + "/" + path.stem + "/" + fragment))

    animators = {}
    for bone, channels in clip["tracks"].items():
        keys = []
        for channel, rows in channels.items():
            for tick, x, y, z in rows:
                keys.append({"channel": channel, "data_points": [dict(zip("xyz", map(str, (x, y, z))))],
                             "uuid": uid(f"{bone}/{channel}/{tick}"), "time": tick,
                             "color": -1, "interpolation": "linear"})
        animators[groups[bone]] = {"name": bone, "type": "bone", "keyframes": keys}
    return {"uuid": uid("clip"), "name": "animation.parapet." + clip["name"], "loop": clip["loop"], "length": clip["length"],
            "snapping": 20, "selected": False, "animators": animators}


# A single synchronous call temporarily selects the target rig, then restores the original tab.
# For an already-open project, compare any existing bath edit with the disk copy before replacing it.
AUTHOR = """
const prior = Project;
const existing = ModelProject.all.find(p => p.save_path === data.path);
try {
    if (existing) existing.select();
    else Codecs.project.load(data.model, {name:data.filename, path:data.path});
    const groupIds = new Set(Group.all.map(g => g.uuid));
    if (Object.keys(data.clip.animators).some(id => !groupIds.has(id))) throw Error('Rig differs from disk');
    const signature = input => JSON.stringify(Object.values(input.animators).map(a => [
        a.name, a.keyframes.map(k => [k.channel, k.time, k.interpolation,
            ...['x','y','z'].map(axis => Number(k.data_points[0][axis]))]).sort()
    ]).sort().concat([input.length, input.loop]));
    const current = Animation.all.find(a => a.name === data.clip.name);
    if (current && signature(current.getUndoCopy(true)) === signature(data.clip)) {
        return current.getUndoCopy(true);
    }
    if (current && (!data.old || signature(current.getUndoCopy(true)) !== signature(data.old))) {
        throw Error('An open care clip has a separate unsaved edit: ' + data.filename);
    }
    const saved = Project.saved;
    const selected = current && Animation.selected === current;
    const time = Timeline.time;
    if (existing) Undo.initEdit({animations:current ? [current] : []});
    if (current) current.remove(false);
    const clip = new Animation(data.clip).add(false);
    if (existing) Undo.finishEdit('Author size-specific parrot care motion', {animations:[clip]});
    if (selected) {clip.select(); Timeline.setTime(time);}
    Project.saved = saved;
    return clip.getUndoCopy(true);
} finally {
    if (!existing && Project && Project !== prior) {Project.saved = true; Project.close(true);}
    if (prior && Project !== prior) prior.select();
}
"""


def signature(animation):
    return {a["name"]: sorted((k["channel"], k["time"], k["interpolation"],
                               *(float(k["data_points"][0][axis]) for axis in "xyz"))
                              for k in a["keyframes"]) for a in animation["animators"].values()}


def write_clip(path, raw, wire):
    # Splice just this entry; preserve unrelated animations, geometry and embedded PNGs exactly.
    start = raw.index("[", raw.index('"animations":')) + 1
    decoder = json.JSONDecoder()
    while True:
        while raw[start].isspace():
            start += 1
        if raw[start] == "]":
            raise ValueError("Expected existing bird animations")
        current, end = decoder.raw_decode(raw, start)
        if current["name"] == wire["name"]:
            updated = raw[:start] + compact(wire) + raw[end:]
            break
        start = end
        while raw[start].isspace():
            start += 1
        if raw[start] == "]":
            updated = raw[:end] + "," + compact(wire) + raw[end:]
            break
        start += 1
    if updated != raw:
        if path.read_text() != raw:
            raise RuntimeError(f"Project changed on disk during export: {path}")
        path.write_text(updated)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--server", default="http://localhost:3000/bb-mcp")
    parser.add_argument("--clip", choices=CLIP_NAMES, help="Sync a single animation")
    parser.add_argument("projects", nargs="*", type=Path)
    args = parser.parse_args()
    editor = Blockbench(args.server)
    # Match the rig's existing authored animation library. Some macaw rigs represent multiple species.
    by_idle = {}
    for name in ("pet_parrots", "medium_parrots", "large_parrots"):
        clips = {c["name"]: c for c in json.loads((LIBRARIES / (name + ".json")).read_text())["clips"]}
        by_idle[clips["idle"]["length"]] = [clips[args.clip]] if args.clip else [clips[name] for name in CLIP_NAMES]
    if len(by_idle) != 3:
        raise ValueError("Each bird library must have a distinct idle duration to identify its rig")
    paths = args.projects or sorted((ROOT / "art").rglob("*.bbmodel"))
    for path in paths:
        path = path.resolve()
        raw = path.read_text()
        model = json.loads(raw)
        animations = model.get("animations", [])
        idle = next((a for a in animations if a["name"] == "animation.parapet.idle"), None)
        if idle is None:
            continue
        groups = {g["name"]: g["uuid"] for g in model["groups"]}
        for clip in by_idle[idle["length"]]:
            preset = model_clip(path, groups, clip)
            old = next((a for a in animations if a["name"] == preset["name"]), None)
            data = compact({"path": str(path), "filename": path.name, "model": model, "clip": preset, "old": old})
            script = "JSON.stringify((data => {" + AUTHOR + "})(" + data.replace("/", r"\u002f") + "))"
            wire = editor.evaluate(script)
            if (wire["length"], wire["loop"], signature(wire)) != (preset["length"], preset["loop"], signature(preset)):
                raise ValueError(f"Blockbench keyframes differ from the game clip: {path}, {clip['name']}")
            if old is None or old["length"] != wire["length"] or old["loop"] != wire["loop"] or signature(old) != signature(wire):
                write_clip(path, raw, wire)
                raw = path.read_text()
                model = json.loads(raw)
                animations = model["animations"]
        print(path.name, flush=True)


if __name__ == "__main__":
    main()
