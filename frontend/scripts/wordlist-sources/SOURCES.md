# Word list sources (Wordle build)

These files are **downloaded** by `scripts/build-wordle-wordlists.mjs` on first run and cached here (see `.gitignore`). Regenerate outputs with:

`npm run build:wordle-words`

Outputs:

- **Backend:** flat `wordle_en.json` / `wordle_fr.json` under `src/main/resources/words/` (used by `WordleGenerator`).
- **Frontend:** chunked `src/data/wordle_words/{en,fr}/{3..7}.json` (one dynamic import per exercise length; avoids a ~900KB single bundle).

| File | URL | License |
|------|-----|---------|
| `words_alpha.txt` | [dwyl/english-words](https://github.com/dwyl/english-words) `words_alpha.txt` | [Unlicense](https://unlicense.org/) |
| `fr_50k.txt` | [hermitdave/FrequencyWords](https://github.com/hermitdave/FrequencyWords) `content/2018/fr/fr_50k.txt` | [CC BY-SA 4.0](https://creativecommons.org/licenses/by-sa/4.0/) (word frequencies derived from OpenSubtitles; we only use the word column) |
