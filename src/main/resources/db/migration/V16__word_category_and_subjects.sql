-- Word category: parent subject WORD with 30 sub-subjects.
-- Anagram exercises live on WORD and are accessible from parent + all 30 children.
-- GET /subjects/WORD/exercises and GET /subjects/WORD_01/../WORD_30/exercises return them.

-- Parent: Word (category)
INSERT INTO subject (id, code, name, description, parent_subject_id, scoring_config)
VALUES (
    'b0000000-0000-0000-0000-000000000010',
    'WORD',
    'Word',
    'Word games: anagrams and vocabulary.',
    NULL,
    '{"accuracyType":"BINARY","speedTargetMs":30000,"confidenceWeight":0.1,"streakBonusCap":0.1}'
) ON CONFLICT (id) DO NOTHING;

-- 30 sub-subjects under WORD
INSERT INTO subject (id, code, name, description, parent_subject_id, scoring_config)
VALUES
    ('b0000000-0000-0000-0000-000000000011', 'WORD_01', 'Word 01', 'Word games', 'b0000000-0000-0000-0000-000000000010', '{"accuracyType":"BINARY","speedTargetMs":30000,"confidenceWeight":0.1,"streakBonusCap":0.1}'),
    ('b0000000-0000-0000-0000-000000000012', 'WORD_02', 'Word 02', 'Word games', 'b0000000-0000-0000-0000-000000000010', '{"accuracyType":"BINARY","speedTargetMs":30000,"confidenceWeight":0.1,"streakBonusCap":0.1}'),
    ('b0000000-0000-0000-0000-000000000013', 'WORD_03', 'Word 03', 'Word games', 'b0000000-0000-0000-0000-000000000010', '{"accuracyType":"BINARY","speedTargetMs":30000,"confidenceWeight":0.1,"streakBonusCap":0.1}'),
    ('b0000000-0000-0000-0000-000000000014', 'WORD_04', 'Word 04', 'Word games', 'b0000000-0000-0000-0000-000000000010', '{"accuracyType":"BINARY","speedTargetMs":30000,"confidenceWeight":0.1,"streakBonusCap":0.1}'),
    ('b0000000-0000-0000-0000-000000000015', 'WORD_05', 'Word 05', 'Word games', 'b0000000-0000-0000-0000-000000000010', '{"accuracyType":"BINARY","speedTargetMs":30000,"confidenceWeight":0.1,"streakBonusCap":0.1}'),
    ('b0000000-0000-0000-0000-000000000016', 'WORD_06', 'Word 06', 'Word games', 'b0000000-0000-0000-0000-000000000010', '{"accuracyType":"BINARY","speedTargetMs":30000,"confidenceWeight":0.1,"streakBonusCap":0.1}'),
    ('b0000000-0000-0000-0000-000000000017', 'WORD_07', 'Word 07', 'Word games', 'b0000000-0000-0000-0000-000000000010', '{"accuracyType":"BINARY","speedTargetMs":30000,"confidenceWeight":0.1,"streakBonusCap":0.1}'),
    ('b0000000-0000-0000-0000-000000000018', 'WORD_08', 'Word 08', 'Word games', 'b0000000-0000-0000-0000-000000000010', '{"accuracyType":"BINARY","speedTargetMs":30000,"confidenceWeight":0.1,"streakBonusCap":0.1}'),
    ('b0000000-0000-0000-0000-000000000019', 'WORD_09', 'Word 09', 'Word games', 'b0000000-0000-0000-0000-000000000010', '{"accuracyType":"BINARY","speedTargetMs":30000,"confidenceWeight":0.1,"streakBonusCap":0.1}'),
    ('b0000000-0000-0000-0000-00000000001a', 'WORD_10', 'Word 10', 'Word games', 'b0000000-0000-0000-0000-000000000010', '{"accuracyType":"BINARY","speedTargetMs":30000,"confidenceWeight":0.1,"streakBonusCap":0.1}'),
    ('b0000000-0000-0000-0000-00000000001b', 'WORD_11', 'Word 11', 'Word games', 'b0000000-0000-0000-0000-000000000010', '{"accuracyType":"BINARY","speedTargetMs":30000,"confidenceWeight":0.1,"streakBonusCap":0.1}'),
    ('b0000000-0000-0000-0000-00000000001c', 'WORD_12', 'Word 12', 'Word games', 'b0000000-0000-0000-0000-000000000010', '{"accuracyType":"BINARY","speedTargetMs":30000,"confidenceWeight":0.1,"streakBonusCap":0.1}'),
    ('b0000000-0000-0000-0000-00000000001d', 'WORD_13', 'Word 13', 'Word games', 'b0000000-0000-0000-0000-000000000010', '{"accuracyType":"BINARY","speedTargetMs":30000,"confidenceWeight":0.1,"streakBonusCap":0.1}'),
    ('b0000000-0000-0000-0000-00000000001e', 'WORD_14', 'Word 14', 'Word games', 'b0000000-0000-0000-0000-000000000010', '{"accuracyType":"BINARY","speedTargetMs":30000,"confidenceWeight":0.1,"streakBonusCap":0.1}'),
    ('b0000000-0000-0000-0000-00000000001f', 'WORD_15', 'Word 15', 'Word games', 'b0000000-0000-0000-0000-000000000010', '{"accuracyType":"BINARY","speedTargetMs":30000,"confidenceWeight":0.1,"streakBonusCap":0.1}'),
    ('b0000000-0000-0000-0000-000000000020', 'WORD_16', 'Word 16', 'Word games', 'b0000000-0000-0000-0000-000000000010', '{"accuracyType":"BINARY","speedTargetMs":30000,"confidenceWeight":0.1,"streakBonusCap":0.1}'),
    ('b0000000-0000-0000-0000-000000000021', 'WORD_17', 'Word 17', 'Word games', 'b0000000-0000-0000-0000-000000000010', '{"accuracyType":"BINARY","speedTargetMs":30000,"confidenceWeight":0.1,"streakBonusCap":0.1}'),
    ('b0000000-0000-0000-0000-000000000022', 'WORD_18', 'Word 18', 'Word games', 'b0000000-0000-0000-0000-000000000010', '{"accuracyType":"BINARY","speedTargetMs":30000,"confidenceWeight":0.1,"streakBonusCap":0.1}'),
    ('b0000000-0000-0000-0000-000000000023', 'WORD_19', 'Word 19', 'Word games', 'b0000000-0000-0000-0000-000000000010', '{"accuracyType":"BINARY","speedTargetMs":30000,"confidenceWeight":0.1,"streakBonusCap":0.1}'),
    ('b0000000-0000-0000-0000-000000000024', 'WORD_20', 'Word 20', 'Word games', 'b0000000-0000-0000-0000-000000000010', '{"accuracyType":"BINARY","speedTargetMs":30000,"confidenceWeight":0.1,"streakBonusCap":0.1}'),
    ('b0000000-0000-0000-0000-000000000025', 'WORD_21', 'Word 21', 'Word games', 'b0000000-0000-0000-0000-000000000010', '{"accuracyType":"BINARY","speedTargetMs":30000,"confidenceWeight":0.1,"streakBonusCap":0.1}'),
    ('b0000000-0000-0000-0000-000000000026', 'WORD_22', 'Word 22', 'Word games', 'b0000000-0000-0000-0000-000000000010', '{"accuracyType":"BINARY","speedTargetMs":30000,"confidenceWeight":0.1,"streakBonusCap":0.1}'),
    ('b0000000-0000-0000-0000-000000000027', 'WORD_23', 'Word 23', 'Word games', 'b0000000-0000-0000-0000-000000000010', '{"accuracyType":"BINARY","speedTargetMs":30000,"confidenceWeight":0.1,"streakBonusCap":0.1}'),
    ('b0000000-0000-0000-0000-000000000028', 'WORD_24', 'Word 24', 'Word games', 'b0000000-0000-0000-0000-000000000010', '{"accuracyType":"BINARY","speedTargetMs":30000,"confidenceWeight":0.1,"streakBonusCap":0.1}'),
    ('b0000000-0000-0000-0000-000000000029', 'WORD_25', 'Word 25', 'Word games', 'b0000000-0000-0000-0000-000000000010', '{"accuracyType":"BINARY","speedTargetMs":30000,"confidenceWeight":0.1,"streakBonusCap":0.1}'),
    ('b0000000-0000-0000-0000-00000000002a', 'WORD_26', 'Word 26', 'Word games', 'b0000000-0000-0000-0000-000000000010', '{"accuracyType":"BINARY","speedTargetMs":30000,"confidenceWeight":0.1,"streakBonusCap":0.1}'),
    ('b0000000-0000-0000-0000-00000000002b', 'WORD_27', 'Word 27', 'Word games', 'b0000000-0000-0000-0000-000000000010', '{"accuracyType":"BINARY","speedTargetMs":30000,"confidenceWeight":0.1,"streakBonusCap":0.1}'),
    ('b0000000-0000-0000-0000-00000000002c', 'WORD_28', 'Word 28', 'Word games', 'b0000000-0000-0000-0000-000000000010', '{"accuracyType":"BINARY","speedTargetMs":30000,"confidenceWeight":0.1,"streakBonusCap":0.1}'),
    ('b0000000-0000-0000-0000-00000000002d', 'WORD_29', 'Word 29', 'Word games', 'b0000000-0000-0000-0000-000000000010', '{"accuracyType":"BINARY","speedTargetMs":30000,"confidenceWeight":0.1,"streakBonusCap":0.1}'),
    ('b0000000-0000-0000-0000-00000000002e', 'WORD_30', 'Word 30', 'Word games', 'b0000000-0000-0000-0000-000000000010', '{"accuracyType":"BINARY","speedTargetMs":30000,"confidenceWeight":0.1,"streakBonusCap":0.1}')
ON CONFLICT (id) DO NOTHING;

-- Move Anagram exercises from ANAGRAM_FR to WORD (parent)
UPDATE exercise
SET subject_id = 'b0000000-0000-0000-0000-000000000010'
WHERE id IN (
    'a0000000-0000-0000-0000-000000000100',
    'a0000000-0000-0000-0000-000000000101',
    'a0000000-0000-0000-0000-000000000102',
    'a0000000-0000-0000-0000-000000000103'
);

-- Remove legacy ANAGRAM_FR subject (exercises moved to WORD)
DELETE FROM subject WHERE code = 'ANAGRAM_FR';
