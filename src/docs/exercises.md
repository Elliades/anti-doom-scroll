Chapitre A. Mémoire long terme (Flashcards + Espacement)
A1) Flashcards (Q/R, Cloze, QCM)

Ultra-facile

QCM à 2 choix sur concepts déjà vus (succès garanti)

Temps cible: 6–10 s

Facile

QCM 4 choix

Cloze 1 trou

Moyen

Réponse courte (1–3 mots)

Cloze 2 trous

Difficile (optionnel)

Réponse libre 2–3 phrases (auto-évaluation + correction proposée)

Scoring

Acc: 1 correct, 0 sinon (ou partiel pour cloze multi-trous)

Speed: min(1, Ttarget / Tactual) (capé à 1)

Calib: optionnel si vous demandez confiance après réponse

Espacement

SM-2 simplifié (facile à coder) :

qualité q ∈ {0,1,2,3} (boutons: “dur”, “moyen”, “facile”, “parfait”)

intervalle I évolue (1j, 3j, 7j, 14j, 30j…)

facteur EF borné (ex: [1.3..2.7])

A2) “Blurting guidé” (mots-clés)

Implémentation sans IA lourde

Sujet → champ texte 60s → tokenizer simple → matching sur liste attendue

Ultra-facile

Liste à cocher: “cochez les notions que vous pourriez expliquer”
(aucune saisie)

Facile

10 mots-clés attendus, l’utilisateur en saisit 3–5

Moyen

20 mots-clés + 2 équations (si applicable)

Difficile

30 mots-clés + relations (mini-carte conceptuelle)

Scoring

Acc: matched / expected (avec pondération: essentiels valent 2)

Speed: basé sur temps restant

Calib: confiance sur “maîtrise du sujet” avant correction

Chapitre B. Mémoire de travail (MdT)
B1) N-back (lettres / positions)

Ultra-facile

1-back, 30 secondes, vitesse lente

Facile

1-back, 60 secondes

Moyen

2-back, 60 secondes

Difficile

3-back, 60 secondes (optionnel)

Scoring

Acc: F1-score simple ou (hits - falseAlarmsNormalized)

MVP: Acc = correctResponses / totalPrompts

Speed: RT moyen vs cible (réaction time)

Calib: optional

B2) Mise à jour numérique (Update)

On montre 2–4 valeurs, puis des opérations successives.

Ultra-facile

2 valeurs, 2 opérations (+1, -1)

Facile

3 valeurs, 3 opérations (+/-)

Moyen

3 valeurs, 4–5 opérations (+/-, ×2)

Difficile

4 valeurs, 6 opérations (+/-, ×2, ÷2 exact)

Scoring

Acc: proportion de valeurs finales correctes

Speed: temps total vs cible

Bonus: “zéro erreur” +5 (capé)

B3) Séquence et rappel

Ultra-facile

retenir 4 items, rappeler 1 position

Facile

6 items

Moyen

7–8 items + rappel inverse

Difficile

alternance chiffre/lettre + contraintes

Scoring

Acc: exact match (ou partiel par position)

Speed: temps de réponse

Chapitre C. Raisonnement (mini-problèmes)
C1) Logique “trucs rapides” (satisfaction)

Ultra-facile

“5 machines font 5 objets en 5 min…” (1 étape)

“Population double…” (insight)

Facile

proportionnalité simple

syllogismes (2 prémisses)

Moyen

problèmes 2 étapes (probabilité simple, combinatoire légère)

Difficile

problèmes type “Monty Hall”, base-rate (optionnel)

Scoring

Acc: exact (ou QCM)

Speed: bonus si < 45–75s

Calib: fortement utile ici (évite surconfiance)

C2) “Erreur volontaire”

On affiche une solution fausse, l’utilisateur doit détecter l’erreur.

Ultra-facile

erreurs arithmétiques évidentes (addition dénominateurs)

Facile

algebra simplification incorrecte

Moyen

Bayes incomplet (manque P(A))

confusion corrélation/causalité

Difficile

raisonnement fallacieux plus subtil (biais de sélection)

Scoring

60%: identifier la ligne fautive

40%: expliquer en 1 phrase (choix multiple des raisons au MVP)

Speed: temps

C3) “Feynman light”

Sans validation stricte.

Ultra-facile

choisir 3 points clés parmi 8 (checklist)

Facile

60s audio/texte, puis l’app montre 3 points attendus

Moyen

l’utilisateur doit réorganiser 6 cartes “idée” dans le bon ordre

Difficile

expliquer + donner un exemple concret + contre-exemple

Scoring (sans IA)

checklist: points sélectionnés / points essentiels

ordre: score de permutation simple (distance)

IA optionnelle: lisibilité + manques (plus tard)

Chapitre D. Vitesse de traitement / Attention
D1) Recherche visuelle (scan)

Grille de symboles/chiffres, trouver cible.

Ultra-facile

1 cible très distincte, 15s

Facile

1 cible parmi distracteurs proches, 30s

Moyen

2 cibles, 45s

Difficile

cibles changeantes, 60s

Scoring

Acc: cibles trouvées / total

Speed: temps

Pénalité légère pour faux positifs (max -10)

D2) Go / No-Go (décision simple)

Appuyer si voyelle, ne rien faire sinon.

Ultra-facile

vitesse lente, 30s

Facile

60s

Moyen

vitesse plus rapide + règle change au milieu (voyelles puis consonnes)

Difficile

2 règles alternées (couleur + lettre)

Scoring

Acc: (hits + correctRejections) / total

Impulsivité: faux positifs pèsent double (contrôle inhibiteur)

Speed: RT moyen correct uniquement

Chapitre E. Estimation “grosse maille” (sens du nombre)
E1) Ordre de grandeur (QCM)

Ultra-facile

10 / 100 / 1000 / 10000

Facile

mêmes échelles mais plus piégeux (masse avion, distance Paris-Lyon…)

Moyen

logs: “entre 10^x et 10^(x+1)”

Difficile

estimation en 2 étapes (surface, volume, densité)

Scoring

Acc: 1 si bonne decade, 0.5 si adjacent, 0 sinon

Speed: rapide = bonus

Calib: très pertinent

E2) Fermi simplifié

Ultra-facile

l’utilisateur choisit une chaîne d’estimations pré-remplies (sliders)
ex: “population”, “conso/jour”, “taux d’équipement”

Facile

2 sliders + 1 multiplication

Moyen

3 facteurs

Difficile

incertitude: donner une fourchette

Scoring

distance log entre estimation et valeur cible (si vous avez une base)

MVP sans base: scoring sur cohérence des étapes (checklist)

E3) Bayes QCM

Ultra-facile

scénarios à chiffres ronds (prévalence 1%, faux positifs 10%)

Facile

calcul approximatif demandé (ordre de grandeur)

Moyen

chiffres réalistes

Difficile

double test / séquentiel

Scoring

Acc: QCM

Bonus: si l’utilisateur explique “base-rate” (optionn