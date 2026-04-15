-- Remove IMAGE_PAIR exercises (discontinued / broken in app). MEMORY_CARD_PAIRS and other memory games stay.
DELETE FROM attempt WHERE exercise_id IN (SELECT id FROM exercise WHERE type = 'IMAGE_PAIR');
DELETE FROM exercise WHERE type = 'IMAGE_PAIR';
