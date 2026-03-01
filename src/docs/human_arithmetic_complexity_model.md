# Human Arithmetic Operation Complexity Model

## Purpose

Define a simple, computable scoring model to estimate the cognitive
complexity of basic arithmetic operations performed by humans.

Grounded in: - Working memory theory (Baddeley) - Cognitive Load Theory
(Sweller) - Numerical cognition research (Dehaene) - Procedural models
of arithmetic (Anderson)

------------------------------------------------------------------------

# 1. General Principle

Arithmetic difficulty increases with:

-   Working memory load
-   Number of intermediate operations
-   Carry / borrow operations
-   Iterative steps
-   Structural irregularities (zeros, remainders, decimals)

General form:

    Complexity = 
        α * digit_load
      + β * carry_borrow_load
      + γ * intermediate_steps
      + δ * structural_irregularities

All variables below are deterministic and easily computable.

------------------------------------------------------------------------

# 2. Addition

For A + B:

## Variables

-   D = max(number_of_digits(A), number_of_digits(B))
-   C = total number of carries
-   MC = maximum consecutive carries
-   SD = maximum column sum
-   ID = absolute difference in digit length

## Score

    Complexity_add =
        1.0 * D
      + 2.5 * C
      + 1.5 * MC
      + 0.2 * SD
      + 0.5 * ID

Key driver: carries.

------------------------------------------------------------------------

# 3. Subtraction

For A − B:

## Variables

-   D = number of columns
-   B = total borrows
-   MB = maximum consecutive borrows
-   Z = number of zeros involved in borrow chains

## Score

    Complexity_sub =
        1.0 * D
      + 3.0 * B
      + 2.0 * MB
      + 1.5 * Z

Borrows are cognitively more expensive than carries.

------------------------------------------------------------------------

# 4. Multiplication

## Case 1: Single-digit facts

Variables: - FT = fact memorized (1 = yes, 0 = no) - H = factor hardness
(1 if involves 7,8,9; else 0)

    Complexity_fact =
        2.0 * (1 - FT)
      + 1.0 * H

------------------------------------------------------------------------

## Case 2: Multi-digit multiplication

For A × B:

### Variables

-   D1 = digits of multiplicand
-   D2 = digits of multiplier
-   SP = D1 × D2 (number of subproducts)
-   C = total carries
-   Z = structural zeros

### Score

    Complexity_mult =
        1.2 * (D1 + D2)
      + 0.8 * SP
      + 2.0 * C
      + 0.5 * Z

------------------------------------------------------------------------

# 5. Division

For A ÷ B:

## Variables

-   D1 = digits of dividend
-   D2 = digits of divisor
-   S = number of subtraction steps
-   R = remainder_present (0 or 1)
-   DEC = decimal_result (0 or 1)

## Score

    Complexity_div =
        1.5 * D1
      + 2.0 * D2
      + 2.0 * S
      + 3.0 * R
      + 4.0 * DEC

Division is structurally iterative and has the highest working memory
cost.

------------------------------------------------------------------------

# 6. Recommended Use

1.  Generate candidate operations.
2.  Compute complexity score.
3.  Filter by target range.

Example difficulty bands (calibrate empirically):

-   0--5 : very easy
-   5--15 : elementary level
-   15--30 : intermediate
-   30+ : advanced

------------------------------------------------------------------------

# 7. Notes

-   Weights should be empirically calibrated.
-   Model assumes column-based written arithmetic.
-   Scores are ordinal (comparative), not absolute cognitive measures.
-   Adapt weights for specific age populations if needed.

End of specification.
