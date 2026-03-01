-- Remove the 30 Word sub-subjects (WORD_01 through WORD_30).
-- Keep the parent subject WORD with its Anagram exercises.

DELETE FROM subject WHERE code LIKE 'WORD_%' AND code != 'WORD';
