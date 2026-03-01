#!/usr/bin/env node
/**
 * Fetches French/English word lists and writes filtered JSON to src/main/resources/words/.
 * Run: node scripts/fetch-anagram-words.js
 * Sources: an-array-of-french-words, an-array-of-english-words (npm)
 */
const fs = require('fs');
const path = require('path');

const OUT_DIR = path.join(__dirname, '..', 'src', 'main', 'resources', 'words');

function filterWords(words, minLen = 2, maxLen = 10) {
  return words.filter(w => {
    const s = String(w).toLowerCase().trim();
    if (s.length < minLen || s.length > maxLen) return false;
    if (/[^a-zàâäéèêëïîôùûüçœæ]/.test(s)) return false; // letters only (incl. French accented)
    return true;
  });
}

async function main() {
  fs.mkdirSync(OUT_DIR, { recursive: true });

  try {
    const fr = require('an-array-of-french-words');
    const filtered = filterWords(fr);
    fs.writeFileSync(path.join(OUT_DIR, 'fr.json'), JSON.stringify(filtered), 'utf8');
    console.log(`French: ${filtered.length} words (2-10 letters)`);
  } catch (e) {
    console.warn('French: npm install an-array-of-french-words first. Using fallback.');
    const fallback = ['je','le','la','un','et','en','de','pas','est','que','une','bien','pour','tout','avec','mais','faire','autre','temps','art','rat','tar','âme','mare','rame','mer','rem','eau','mot','tom','trois','gare','rage','sage','âge','lit','til','fil','île','aile','lia'];
    fs.writeFileSync(path.join(OUT_DIR, 'fr.json'), JSON.stringify(fallback), 'utf8');
  }

  try {
    const en = require('an-array-of-english-words');
    const filtered = filterWords(en);
    fs.writeFileSync(path.join(OUT_DIR, 'en.json'), JSON.stringify(filtered), 'utf8');
    console.log(`English: ${filtered.length} words (2-10 letters)`);
  } catch (e) {
    console.warn('English: npm install an-array-of-english-words first. Using fallback.');
    const fallback = ['at','it','be','we','the','and','are','ear','era','eat','tea','ate','get','art','rat','tar','car','act','cat','dog','god','god','lot','let','elt','set','its','sit','die','tie','time','item','emit','mite'];
    fs.writeFileSync(path.join(OUT_DIR, 'en.json'), JSON.stringify(fallback), 'utf8');
  }
}

main().catch(console.error);
