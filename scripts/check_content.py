#!/usr/bin/env python3
"""Validate the packaged museum exhibits and their stable IDs."""
import argparse
import json
import math
from pathlib import Path

root = Path(__file__).resolve().parents[1] / "app/src/main/assets/content"
wing_ids = {"scale", "mechanics", "fields", "light", "astro", "telecom", "matter"}
topic_ids, legacy_indices, old_stations = set(), set(), set()
labs = 0
history = []
manifest = []
assert {file.stem for file in root.glob("*.json")} == wing_ids

for path in sorted(root.glob("*.json")):
    wing = json.loads(path.read_text(encoding="utf-8"))
    assert wing["id"] == path.stem
    assert wing["topics"], path
    for topic in wing["topics"]:
        key = topic["id"]
        assert key.startswith(wing["id"] + ".") and key not in topic_ids, key
        topic_ids.add(key)
        assert all(topic[name].strip() for name in ("titleAr", "titleEn", "summary", "keyIdea")), key
        quiz = topic["quiz"]
        options = quiz["options"]
        assert len(options) == len(set(options)) == 4 and all(options), key
        assert 0 <= quiz["correctIndex"] < len(options), key
        assert quiz["question"].strip() and quiz["explanation"].strip(), key
        if "scaleMeters" in topic:
            assert wing["id"] == "scale" and math.isfinite(topic["scaleMeters"]) and topic["scaleMeters"] > 0, key
        if "legacyStation" in topic:
            station = topic["legacyStation"]
            pair = (station["axis"], station["index"])
            assert pair not in old_stations and pair[0] in (0, 1) and 0 <= pair[1] < 35, key
            old_stations.add(pair)
        if "timeline" in topic:
            beat = topic["timeline"]
            assert wing["id"] == "matter" and topic["section"] == "history" and not topic.get("controls"), key
            assert isinstance(beat["yearsAgo"], int) and beat["yearsAgo"] >= 0, key
            assert all(beat[field].strip() for field in ("date", "evidence", "credit", "related")), key
            assert beat["source"].startswith("https://"), key
            assert (root.parent / "exhibits" / (beat["art"] + ".webp")).is_file(), key
            history.append(beat)
        controls = topic.get("controls", [])
        if controls:
            labs += 1
            assert 0 <= topic["axis"] < len(controls) and topic["symbol"].strip(), key
            assert isinstance(topic["unit"], str), key
            fields = []
            for control in controls:
                assert control["symbol"].strip() and isinstance(control["unit"], str), key
                discrete = control.get("discrete", False)
                if discrete:
                    count = (control["max"] - control["min"]) / control["step"]
                    assert 1 <= count <= 300 and math.isclose(count, round(count)), key
                    position = (control["init"] - control["min"]) / control["step"]
                    assert math.isclose(position, round(position)), key
                fields.append(",".join(str(control[k]) for k in ("min", "max", "init")) +
                              "," + str(control["step"] if discrete else 0))
            manifest.append(f"{key}\t{topic.get('legacyIndex', -1)}\t{';'.join(fields)}")
            assert all(topic.get(name, "").strip() for name in ("metric", "assumptions", "challenge", "application")), key
        if "legacyIndex" in topic:
            index = topic["legacyIndex"]
            assert index not in legacy_indices and len(controls) == (3 if index == 2 else 2), key
            legacy_indices.add(index)
        for control in controls:
            lo, default, hi, step = (control[field] for field in ("min", "init", "max", "step"))
            assert all(map(math.isfinite, (lo, default, hi, step))), key
            # Legacy defaults may intentionally start at a range endpoint.
            assert (lo <= default <= hi if "legacyIndex" in topic else lo < default < hi), key
            assert 0 < step <= hi - lo and control["label"].strip(), key

assert legacy_indices == set(range(47)), sorted(legacy_indices)
assert labs == 118, labs
assert sum(line.startswith("telecom.") for line in manifest) == 44
assert sum(line.startswith("matter.") for line in manifest) == 20
assert len(history) == 17 and all(a["yearsAgo"] > b["yearsAgo"] for a, b in zip(history, history[1:]))
assert history[0]["yearsAgo"] == 4200000000 and history[-1]["yearsAgo"] == 0
lab_ids = {line.split("\t", 1)[0] for line in manifest}
assert all(beat["related"] in lab_ids for beat in history)
for name in ("ran", "transport"):
    assert (root.parent / "exhibits" / (name + ".webp")).is_file()
parser = argparse.ArgumentParser()
parser.add_argument("--graph-manifest", type=Path)
args = parser.parse_args()
if args.graph_manifest:
    args.graph_manifest.write_text("\n".join(manifest) + "\n", encoding="utf-8")
print(f"Validated {len(wing_ids)} wings, {len(topic_ids)} exhibits and {labs} labs.")
