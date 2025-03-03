# CSP Tile Placement

This repository contains a Java implementation of a Constraint Satisfaction Problem (CSP) for a tile placement puzzle.

## Overview

In this project, you are given a square landscape (e.g., 20x20, 100x100, 200x200, etc.) with bushes marked by colors (1, 2, 3, 4). The goal is to place a set of 4x4 tiles on the landscape so that after tile placement, a specific target number of visible bushes (by color) is achieved.

### Tile Types

- **Full Block**: Covers the entire 4x4 area, hiding all bushes in that patch.
- **Outer Boundary**: Covers the outer boundary of the 4x4 area, leaving the central bushes visible.
- **EL Shape**: Covers an "L" shape on the tile, hiding parts of the patch while allowing other bushes to remain visible.

### CSP Algorithm

The solution leverages several CSP techniques:
- **Backtracking Search:** Systematically explores possible tile placements.
- **Minimum Remaining Values (MRV):** Chooses the variable with the fewest remaining valid options first.
- **Least Constraining Value (LCV):** Orders the possible assignments by how little they restrict other variables.
- **Constraint Propagation (AC3):** Propagates constraints across the board to prune inconsistent assignments.

## Project Structure

```
.
├── Main.java             # Main CSP algorithm implementation.
├── Test.java             # Visualization and testing code for tile placements.
├── tilesproblem.txt      # Input file with landscape, tile counts, and target visible bushes.
├── output.txt  # Output file with tile placement results.
└── README.md             # This file.
```

## Input Files

- **tilesproblem.txt**:  
  Contains three sections:
  1. **Landscape**: A grid representing the bushes (colors 1-4) in the landscape (empty cells as spaces).
  2. **Tiles**: A dictionary with counts for each available tile type (e.g., `FULL_BLOCK`, `OUTER_BOUNDARY`, `EL_SHAPE`).
  3. **Targets**: The target counts for visible bushes of each color after tile placement.

- **tilesproblem_out.txt**:  
  Contains tile placement output with each line formatted as:
  ```
  tile_index tile_type
  ```
  Example:
  ```
  3 FULL_BLOCK
  ```

For more input files and a problem generator, please check:
- [Input Files Repository](https://github.com/amrinderarora/ai/tree/master/src/main/resources/csp/tileplacement)
- [Tile Placement Problem Generator](https://github.com/amrinderarora/ai/blob/master/src/main/java/edu/gwu/cs/ai/csp/tileplacement/TilePlacementProblemGenerator.java)

## How to Run

1. **Compile** the Java files using:
    ```bash
    javac Main.java Test.java
    ```
2. **Run** the main program:
    ```bash
    java Main
    ```
   The program will read `tilesproblem.txt`, solve the CSP, and print the tile placements. If no solution is found, it will output "No solution found".

## Testing & Visualization

The `Test.java` file includes code to visualize the tiling on the landscape. It prints the final grid along with counters for:
- Uncovered bushes per color.
- The count of each tile type used.

## License

This project is provided for educational purposes. Feel free to use, modify, and distribute the code.

## Acknowledgments

- **Assignment Details:** Project 2 – CSP – Tile Placement.
- **Input Files & Problem Generator:** Provided by [Amrinder Arora](https://github.com/amrinderarora/ai).
