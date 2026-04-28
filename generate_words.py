#!/usr/bin/env python3
"""One-shot Gemini word-pair generator for Outlier/Sample Space.

Makes a SINGLE Gemini API call to produce ~510 tricky word pairs
(30 per category x 17 categories), then writes a new WordBank.kt.
"""

import json
import os
import sys
import urllib.request
import textwrap

API_KEY = os.environ.get("GEMINI_API_KEY", "")
if not API_KEY:
    sys.exit("ERROR: set GEMINI_API_KEY environment variable before running.")
MODEL = "gemini-3-flash-preview"
URL = f"https://generativelanguage.googleapis.com/v1beta/models/{MODEL}:generateContent?key={API_KEY}"

CATEGORIES = [
    "Food", "Beverages", "Animals", "Places", "Buildings",
    "Objects", "Sports", "Music", "Nature", "Jobs",
    "Home", "Travel", "School", "Weather", "Entertainment",
    "Science", "Technology",
]

PROMPT = textwrap.dedent("""\
You are generating word pairs for a social deduction party game called "Outlier" \
(similar to Undercover / Spyfall). In each round, most players get one word (the \
"civilian" word) and a few undercover players get the other word. Players take turns \
describing their word and try to figure out who has the different one.

CRITICAL QUALITY RULES for every pair:
1. DECEPTIVE SIMILARITY — both words should be describable in almost the same way \
in casual conversation, so undercovers can hide. Good: blizzard/storm, cat/rabbit, \
curse/wand, canoe/kayak, frost/ice, telescope/binoculars, puppet/doll, soup/broth.
2. NOT pure synonyms — there must be a real, subtle difference. "couch/sofa" is boring. \
"couch/beanbag" is better.
3. CROSS-CONCEPT pairs are great — related but from different angles: curse/wand, \
compass/map, astronaut/pilot.
4. Family-friendly only, no slang, no profanity.
5. Each word must be 1–2 words max, no numbers, no obscure jargon.
6. No duplicate pairs (even reversed). Each individual word can appear in at most 2 pairs.

Generate EXACTLY 30 pairs for EACH of these 17 categories (510 pairs total):
""" + ", ".join(CATEGORIES) + """

Respond with ONLY a JSON array. No markdown fences, no commentary. Each element:
{"category": "...", "word1": "...", "word2": "..."}
""")

OUT_PATH = "app/src/main/java/com/outlier/samplespace/game/WordBank.kt"


def call_gemini():
    body = json.dumps({
        "contents": [{"parts": [{"text": PROMPT}]}],
        "generationConfig": {
            "temperature": 1.0,
            "maxOutputTokens": 65536,
            "responseMimeType": "application/json",
        },
    }).encode()

    req = urllib.request.Request(
        URL,
        data=body,
        headers={"Content-Type": "application/json"},
        method="POST",
    )

    with urllib.request.urlopen(req, timeout=180) as resp:
        data = json.loads(resp.read())

    candidate = data["candidates"][0]
    finish = candidate.get("finishReason", "")
    text = candidate["content"]["parts"][0]["text"]

    if finish not in ("STOP", ""):
        print(f"WARNING: finishReason={finish}, response may be truncated", file=sys.stderr)

    pairs = json.loads(text)

    if not isinstance(pairs, list) or len(pairs) < 400:
        print(f"ERROR: expected 500+ pairs, got {len(pairs) if isinstance(pairs, list) else 'non-list'}", file=sys.stderr)
        sys.exit(1)

    return pairs


def validate(pairs):
    clean = []
    seen = set()
    for p in pairs:
        cat = p.get("category", "").strip()
        w1 = p.get("word1", "").strip()
        w2 = p.get("word2", "").strip()
        if not cat or not w1 or not w2:
            continue
        if w1.lower() == w2.lower():
            continue
        if any(c.isdigit() for c in w1 + w2):
            continue
        if len(w1.split()) > 2 or len(w2.split()) > 2:
            continue
        key = tuple(sorted([w1.lower(), w2.lower()]))
        if key in seen:
            continue
        seen.add(key)
        clean.append({"category": cat, "word1": w1, "word2": w2})
    return clean


def write_kotlin(pairs):
    lines = [
        'package com.outlier.samplespace.game',
        '',
        'object WordBank {',
        '    val allPairs: List<WordPair> = listOf(',
    ]

    for i, p in enumerate(pairs):
        cat = p["category"].replace('"', '\\"')
        w1 = p["word1"].replace('"', '\\"')
        w2 = p["word2"].replace('"', '\\"')
        comma = "," if i < len(pairs) - 1 else ""
        lines.append(f'        WordPair("{cat}", "{w1}", "{w2}"){comma}')

    lines.append('    )')
    lines.append('}')
    lines.append('')

    with open(OUT_PATH, "w") as f:
        f.write("\n".join(lines))

    print(f"Wrote {len(pairs)} pairs to {OUT_PATH}")


def main():
    print(f"Calling Gemini ({MODEL}) for word pairs...")
    raw = call_gemini()
    print(f"Got {len(raw)} raw pairs from Gemini")

    pairs = validate(raw)
    print(f"After validation: {len(pairs)} clean pairs across {len(set(p['category'] for p in pairs))} categories")

    write_kotlin(pairs)


if __name__ == "__main__":
    main()
