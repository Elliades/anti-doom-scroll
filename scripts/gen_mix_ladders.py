"""One-off generator for ladder YAML blocks (run from repo root: python scripts/gen_mix_ladders.py)."""
from __future__ import annotations

import pathlib
import re

ROOT = pathlib.Path(__file__).resolve().parents[1]
APP_YML = ROOT / "src/main/resources/application.yml"


def word_ladder_yaml() -> str:
    diffs = (
        [["ULTRA_EASY"]] * 4
        + [["ULTRA_EASY", "EASY"]] * 2
        + [["EASY"]] * 5
        + [["EASY", "MEDIUM"]] * 2
        + [["MEDIUM"]] * 5
        + [["MEDIUM", "HARD"]] * 2
        + [["HARD"]] * 5
        + [["HARD", "VERY_HARD"]] * 2
        + [["VERY_HARD"]] * 3
    )
    assert len(diffs) == 30
    lines = [
        "      # Word ladder: Anagram + Wordle (WORD + WORDLE_FR + WORDLE_EN); same arc as anagram ladder",
        "      word:",
        '        name: "Word Ladder"',
        "        thresholds:",
        "          minScoreToStay: 0.40",
        "          minScoreToAdvance: 0.75",
        "          answersNeededToAdvance: 5",
        "        levels:",
    ]
    for i, d in enumerate(diffs):
        dstr = ", ".join(d)
        lines.append(f"          - levelIndex: {i}")
        lines.append(f"            allowedDifficulties: [{dstr}]")
        lines.append("            subjectCodes: [WORD, WORDLE_FR, WORDLE_EN]")
        lines.append("            allowedTypes: [ANAGRAM, WORDLE]")
    return "\n".join(lines)


def extract_level_blocks(section_body: str) -> list[str]:
    """Split YAML levels section into blocks starting with '          - levelIndex:'."""
    parts = section_body.split("          - levelIndex:")
    out = []
    for p in parts[1:]:
        out.append("          - levelIndex:" + p)
    return out


def main() -> None:
    text = APP_YML.read_text(encoding="utf-8")

    m_pair = re.search(
        r"(      pair:\n        name:.*?\n        levels:\n)(.*?)(\n      # N-Back ladder:)",
        text,
        re.DOTALL,
    )
    if not m_pair:
        raise SystemExit("pair ladder not found")
    pair_levels_body = m_pair.group(2)
    pair_blocks = extract_level_blocks(pair_levels_body)

    m_nb = re.search(
        r"(      nback:\n        name:.*?\n        levels:\n)(.*?)(\n      # Estimation ladder:)",
        text,
        re.DOTALL,
    )
    if not m_nb:
        raise SystemExit("nback ladder not found")
    nb_levels_body = m_nb.group(2)
    nb_blocks = extract_level_blocks(nb_levels_body)

    # Memory: pair 0-14 + nback 0-14 as levels 15-29
    mem_levels = pair_blocks[:15] + [
        re.sub(r"levelIndex: \d+", f"levelIndex: {15 + i}", b, count=1)
        for i, b in enumerate(nb_blocks[:15])
    ]
    memory_yaml = (
        "      # Memory ladder: card pairs then n-back (pairs + spatial/sequential memory)\n"
        "      memory:\n"
        '        name: "Memory Ladder"\n'
        "        thresholds:\n"
        "          minScoreToStay: 0.40\n"
        "          minScoreToAdvance: 0.75\n"
        "          answersNeededToAdvance: 5\n"
        "        levels:\n"
        + "\n".join(mem_levels)
    )

    # Working memory: pair 15-29 (sum pair) as 0-14, digit span 15-29
    wm_sum = [
        re.sub(r"levelIndex: \d+", f"levelIndex: {i}", b, count=1)
        for i, b in enumerate(pair_blocks[15:30])
    ]
    ds_diffs = (
        [["ULTRA_EASY"]] * 3
        + [["ULTRA_EASY", "EASY"]] * 2
        + [["EASY"]] * 3
        + [["EASY", "MEDIUM"]] * 2
        + [["MEDIUM"]] * 3
        + [["MEDIUM", "HARD"]] * 2
    )
    assert len(ds_diffs) == 15
    ds_lines: list[str] = []
    for i, d in enumerate(ds_diffs):
        li = 15 + i
        dstr = ", ".join(d)
        ds_lines.append(f"          - levelIndex: {li}")
        ds_lines.append(f"            allowedDifficulties: [{dstr}]")
        ds_lines.append("            subjectCodes: [DIGIT_SPAN]")
        ds_lines.append("            allowedTypes: [DIGIT_SPAN]")
    working_yaml = (
        "      # Working memory ladder: sum pairs then digit span\n"
        "      working_memory:\n"
        '        name: "Working Memory Ladder"\n'
        "        thresholds:\n"
        "          minScoreToStay: 0.40\n"
        "          minScoreToAdvance: 0.75\n"
        "          answersNeededToAdvance: 5\n"
        "        levels:\n"
        + "\n".join(wm_sum)
        + "\n"
        + "\n".join(ds_lines)
    )

    out = (
        word_ladder_yaml()
        + "\n"
        + memory_yaml
        + "\n"
        + working_yaml
        + "\n"
    )
    out_path = ROOT / "scripts/_generated_mix_ladders.yml"
    out_path.write_text(out, encoding="utf-8")
    print(f"Wrote {out_path} ({len(out)} chars)")


if __name__ == "__main__":
    main()
