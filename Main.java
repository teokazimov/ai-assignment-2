import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Main {

    static final int TILE_SIZE = 4;

    // Container for the parsed input data.
    static class InputData {
        int[][] landscape;
        Map<String, Integer> tilesDict;
        Map<Integer, Integer> targets;

        InputData(int[][] landscape, Map<String, Integer> tilesDict, Map<Integer, Integer> targets) {
            this.landscape = landscape;
            this.tilesDict = tilesDict;
            this.targets = targets;
        }
    }

    static InputData parseInput(String inputText) {
    // Split sections by "# "
    String[] sections = inputText.split("# ");
    // sections[1] is the landscape section
    String[] landscapeLines = sections[1].split("\n");
    int size = landscapeLines.length - 2; // first line is header
    System.out.println(size + "x" + size);

    int[][] landscape = new int[size][size];
    int expectedLength = size * 2; // expected row length based on fixed-position indexing

    for (int j = 0; j < size; j++) {
        String row = landscapeLines[j + 1];
        // Pad the row with spaces if it's shorter than expected.
        if (row.length() < expectedLength) {
            row = String.format("%-" + expectedLength + "s", row);
        }
        for (int i = 0; i < size; i++) {
            char ch = row.charAt(i * 2);
            int value = (ch == ' ') ? 0 : Character.getNumericValue(ch);
            landscape[j][i] = value;
        }
    }
    
    for (int i = 0; i < landscape.length; i++) {
        for (int j = 0; j < landscape[i].length; j++) {
            System.out.print(landscape[i][j] + " ");
        }
        System.out.println(); // New line after each row
    }

    // Parse the tiles dictionary from section[2]
    String tilesSection = sections[2].trim();
    String[] tilesLines = tilesSection.split("\n");
    // Assume the second line contains the dictionary data.
    String tilesLine = tilesLines[1].trim();
    // Remove surrounding braces "{}"
    tilesLine = tilesLine.replace("{", "").replace("}", "");
    Map<String, Integer> tilesDict = new HashMap<>();
    for (String part : tilesLine.split(",")) {
        String[] keyVal = part.split("=");
        if (keyVal.length < 2) continue;
        String key = keyVal[0].trim();
        int val = Integer.parseInt(keyVal[1].trim());
        tilesDict.put(key, val);
    }

    // Parse targets from section[3]
    String targetsSection = sections[3].trim();
    String[] targetsLines = targetsSection.split("\n");
    Map<Integer, Integer> targets = new HashMap<>();
    // Skip the header (first line)
    for (int i = 1; i < targetsLines.length; i++) {
        String targetLine = targetsLines[i].trim();
        if (targetLine.isEmpty()) continue;
        String[] parts = targetLine.split(":");
        if (parts.length < 2) continue;
        int color = Integer.parseInt(parts[0].trim());
        int count = Integer.parseInt(parts[1].trim());
        targets.put(color, count);
    }
    return new InputData(landscape, tilesDict, targets);
}

    // Returns a patch (submatrix) of the landscape for the given tile position.
    static int[][] getPatch(int[][] landscape, int tileRow, int tileCol) {
        int startRow = tileRow * TILE_SIZE;
        int startCol = tileCol * TILE_SIZE;
        int[][] patch = new int[TILE_SIZE][TILE_SIZE];
        for (int i = 0; i < TILE_SIZE; i++) {
            for (int j = 0; j < TILE_SIZE; j++) {
                patch[i][j] = landscape[startRow + i][startCol + j];
            }
        }
        return patch;
    }

    // Computes the contribution for a given patch and tile type.
    static Map<Integer, Integer> computeContribution(int[][] patch, String tileType) {
        Map<Integer, Integer> contrib = new HashMap<>();
        if (tileType.equals("FULL_BLOCK")) {
            return contrib; // no contribution
        } else if (tileType.equals("OUTER_BOUNDARY")) {
            for (int i : new int[]{1, 2}) {
                for (int j : new int[]{1, 2}) {
                    int c = patch[i][j];
                    if (c != 0) {
                        contrib.put(c, contrib.getOrDefault(c, 0) + 1);
                    }
                }
            }
            return contrib;
        } else if (tileType.equals("EL_SHAPE")) {
            int[][] pattern = {
                {1, 0, 0, 0},
                {1, 0, 0, 0},
                {1, 0, 0, 0},
                {1, 1, 1, 1}
            };
            for (int i = 0; i < TILE_SIZE; i++) {
                for (int j = 0; j < TILE_SIZE; j++) {
                    if (pattern[i][j] == 0) {
                        int c = patch[i][j];
                        if (c != 0) {
                            contrib.put(c, contrib.getOrDefault(c, 0) + 1);
                        }
                    }
                }
            }
            return contrib;
        }
        return contrib;
    }

    // AC3-like consistency check.
    static boolean ac3(Map<Integer, List<String>> domains,
                       Map<Integer, String> assignment,
                       Map<Integer, Map<String, Map<Integer, Integer>>> tileContrib,
                       Map<Integer, Integer> targets,
                       int numTiles) {
        List<Integer> unassigned = new ArrayList<>();
        for (int v = 0; v < numTiles; v++) {
            if (!assignment.containsKey(v)) {
                unassigned.add(v);
            }
        }

        // current contributions for colors 1,2,3,4
        Map<Integer, Integer> current = new HashMap<>();
        for (int c = 1; c <= 4; c++) {
            current.put(c, 0);
        }
        for (Map.Entry<Integer, String> entry : assignment.entrySet()) {
            int v = entry.getKey();
            String ttype = entry.getValue();
            Map<Integer, Integer> contribMap = tileContrib.get(v).get(ttype);
            for (Map.Entry<Integer, Integer> ce : contribMap.entrySet()) {
                int color = ce.getKey();
                if (color == 0) continue;
                current.put(color, current.get(color) + ce.getValue());
            }
        }

        // For each unassigned tile, compute min and max possible contributions.
        Map<Integer, Integer> minAdd = new HashMap<>();
        Map<Integer, Integer> maxAdd = new HashMap<>();
        for (int c = 1; c <= 4; c++) {
            minAdd.put(c, 0);
            maxAdd.put(c, 0);
        }
        for (int v : unassigned) {
            Map<Integer, Integer> mins = new HashMap<>();
            Map<Integer, Integer> maxs = new HashMap<>();
            for (int c = 1; c <= 4; c++) {
                mins.put(c, Integer.MAX_VALUE);
                maxs.put(c, Integer.MIN_VALUE);
            }
            for (String val : domains.get(v)) {
                Map<Integer, Integer> contrib = tileContrib.get(v).get(val);
                for (int c = 1; c <= 4; c++) {
                    int cnt = contrib.getOrDefault(c, 0);
                    mins.put(c, Math.min(mins.get(c), cnt));
                    maxs.put(c, Math.max(maxs.get(c), cnt));
                }
            }
            for (int c = 1; c <= 4; c++) {
                int currentMin = minAdd.get(c);
                if (mins.get(c) != Integer.MAX_VALUE) {
                    currentMin += mins.get(c);
                }
                minAdd.put(c, currentMin);

                int currentMax = maxAdd.get(c);
                if (maxs.get(c) != Integer.MIN_VALUE) {
                    currentMax += maxs.get(c);
                }
                maxAdd.put(c, currentMax);
            }
        }
        for (int c = 1; c <= 4; c++) {
            int totalMin = current.get(c) + minAdd.get(c);
            int totalMax = current.get(c) + maxAdd.get(c);
            int target = targets.get(c);
            if (target < totalMin || target > totalMax) {
                return false;
            }
        }
        return true;
    }

    // Selects an unassigned variable using MRV (minimum remaining values).
    static int selectVariable(Map<Integer, String> assignment, Map<Integer, List<String>> domains) {
        int selected = -1;
        int minSize = Integer.MAX_VALUE;
        for (Integer v : domains.keySet()) {
            if (!assignment.containsKey(v)) {
                int size = domains.get(v).size();
                if (size < minSize) {
                    minSize = size;
                    selected = v;
                }
            }
        }
        return selected;
    }

    // Orders the domain values for a variable by the sum of contributions (LCV heuristic).
    static List<String> orderValues(int var, Map<Integer, List<String>> domains,
                                    Map<Integer, Map<String, Map<Integer, Integer>>> tileContrib) {
        List<String> values = new ArrayList<>(domains.get(var));
        values.sort(Comparator.comparingInt(val -> {
            int sum = 0;
            for (int cnt : tileContrib.get(var).get(val).values()) {
                sum += cnt;
            }
            return sum;
        }));
        return values;
    }

    // Checks if the global assignment meets the targets.
    static boolean globalTargetOk(Map<Integer, String> assignment,
                                  Map<Integer, Map<String, Map<Integer, Integer>>> tileContrib,
                                  Map<Integer, Integer> targets) {
        Map<Integer, Integer> total = new HashMap<>();
        for (int c = 1; c <= 4; c++) {
            total.put(c, 0);
        }
        for (Map.Entry<Integer, String> entry : assignment.entrySet()) {
            int v = entry.getKey();
            String ttype = entry.getValue();
            Map<Integer, Integer> contrib = tileContrib.get(v).get(ttype);
            for (Map.Entry<Integer, Integer> ce : contrib.entrySet()) {
                total.put(ce.getKey(), total.get(ce.getKey()) + ce.getValue());
            }
        }
        for (int c = 1; c <= 4; c++) {
            if (!total.get(c).equals(targets.get(c))) {
                return false;
            }
        }
        return true;
    }

    // Backtracking search with MRV and LCV heuristics.
    static Map<Integer, String> backtrack(Map<Integer, String> assignment,
                                          Map<Integer, List<String>> domains,
                                          Map<Integer, Map<String, Map<Integer, Integer>>> tileContrib,
                                          Map<String, Integer> remainingTiles,
                                          Map<Integer, Integer> targets,
                                          int numTiles) {
        if (assignment.size() == numTiles) {
            return globalTargetOk(assignment, tileContrib, targets) ? assignment : null;
        }

        int var = selectVariable(assignment, domains);
        for (String val : orderValues(var, domains, tileContrib)) {
            if (remainingTiles.getOrDefault(val, 0) <= 0) continue;
            Map<Integer, String> newAssignment = new HashMap<>(assignment);
            newAssignment.put(var, val);

            Map<String, Integer> newRemaining = new HashMap<>(remainingTiles);
            newRemaining.put(val, newRemaining.get(val) - 1);

            // Deep copy domains.
            Map<Integer, List<String>> newDomains = new HashMap<>();
            for (Map.Entry<Integer, List<String>> entry : domains.entrySet()) {
                newDomains.put(entry.getKey(), new ArrayList<>(entry.getValue()));
            }
            newDomains.put(var, new ArrayList<>(Collections.singletonList(val)));

            if (ac3(newDomains, newAssignment, tileContrib, targets, numTiles)) {
                Map<Integer, String> result = backtrack(newAssignment, newDomains, tileContrib, newRemaining, targets, numTiles);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    // Solves the constraint satisfaction problem.
    static Map<Integer, String> solveCSP(int[][] landscape, Map<String, Integer> tilesDict, Map<Integer, Integer> targets) {
        int n = landscape.length;
        int tileRows = n / TILE_SIZE;
        int tileCols = n / TILE_SIZE;
        int numTiles = tileRows * tileCols;

        // Build the tile contribution dictionary.
        Map<Integer, Map<String, Map<Integer, Integer>>> tileContrib = new HashMap<>();
        for (int idx = 0; idx < numTiles; idx++) {
            int r = idx / tileCols;
            int c = idx % tileCols;
            int[][] patch = getPatch(landscape, r, c);
            Map<String, Map<Integer, Integer>> contribMap = new HashMap<>();
            contribMap.put("FULL_BLOCK", computeContribution(patch, "FULL_BLOCK"));
            contribMap.put("OUTER_BOUNDARY", computeContribution(patch, "OUTER_BOUNDARY"));
            contribMap.put("EL_SHAPE", computeContribution(patch, "EL_SHAPE"));
            tileContrib.put(idx, contribMap);
        }

        // Initialize domains.
        Map<Integer, List<String>> domains = new HashMap<>();
        for (int v = 0; v < numTiles; v++) {
            List<String> domainList = new ArrayList<>();
            domainList.add("FULL_BLOCK");
            domainList.add("OUTER_BOUNDARY");
            domainList.add("EL_SHAPE");
            domains.put(v, domainList);
        }

        // Copy remaining tiles from the input dictionary.
        Map<String, Integer> remainingTiles = new HashMap<>(tilesDict);

        return backtrack(new HashMap<>(), domains, tileContrib, remainingTiles, targets, numTiles);
    }

    public static void main(String[] args) {
        try {
            String inputText = new String(Files.readAllBytes(Paths.get("tilesproblem.txt")), StandardCharsets.UTF_8);
            InputData inputData = parseInput(inputText);
            Map<Integer, String> solution = solveCSP(inputData.landscape, inputData.tilesDict, inputData.targets);
            if (solution == null) {
                System.out.println("No solution found");
            } else {
                List<Integer> keys = new ArrayList<>(solution.keySet());
                Collections.sort(keys);
                for (int key : keys) {
                    System.out.println(key + " " + solution.get(key));
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading the file: " + e.getMessage());
        }
    }
}
