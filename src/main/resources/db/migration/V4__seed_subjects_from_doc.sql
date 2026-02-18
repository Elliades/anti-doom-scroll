-- Seed subjects from exercises.md (Chapitres A–E) for easy scaling.
-- Exercises can be added later per subject.

INSERT INTO subject (id, code, name, description, scoring_config)
VALUES
    ('b0000000-0000-0000-0000-000000000002', 'A1', 'Flashcards (Q/R, Cloze, QCM)', 'Mémoire long terme: flashcards avec espacement SM-2', '{"accuracyType":"BINARY","speedTargetMs":10000,"confidenceWeight":0.1,"streakBonusCap":0.1}'),
    ('b0000000-0000-0000-0000-000000000003', 'A2', 'Blurting guidé (mots-clés)', 'Sujet → mots-clés attendus, matching pondéré', '{"accuracyType":"WEIGHTED","speedTargetMs":60000,"confidenceWeight":0.2,"streakBonusCap":0.1}'),
    ('b0000000-0000-0000-0000-000000000004', 'B1', 'N-back (lettres/positions)', 'Mémoire de travail: n-back', '{"accuracyType":"BINARY","speedTargetMs":60000,"confidenceWeight":0,"streakBonusCap":0.1}'),
    ('b0000000-0000-0000-0000-000000000005', 'B2', 'Mise à jour numérique', '2–4 valeurs, opérations successives', '{"accuracyType":"PARTIAL","partialMatchThreshold":0.5,"speedTargetMs":45000,"streakBonusCap":0.1}'),
    ('b0000000-0000-0000-0000-000000000006', 'C1', 'Logique / trucs rapides', 'Proportionnalité, syllogismes, mini-problèmes', '{"accuracyType":"BINARY","speedTargetMs":60000,"confidenceWeight":0.3,"streakBonusCap":0.1}'),
    ('b0000000-0000-0000-0000-000000000007', 'E1', 'Ordre de grandeur (QCM)', 'Estimation grosse maille: decades', '{"accuracyType":"PARTIAL","partialMatchThreshold":0.5,"speedTargetMs":15000,"confidenceWeight":0.25,"streakBonusCap":0.1}')
ON CONFLICT (id) DO NOTHING;
