import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

// Main class that implements the tiling functionality
public class Test {
    public static final int TILE_SIZE = 4;
    public static final int GRID_SIZE = 20;
    
    // The pattern for the EL_SHAPE tile
    public static final int[][] EL_SHAPE_PATTERN = {
        {1, 0, 0, 0},
        {1, 0, 0, 0},
        {1, 0, 0, 0},
        {1, 1, 1, 1}
    };

    // Class to hold parsed input data
    static class ParsedData {
        int[][] landscape;
        Map<String, Integer> tiles;
        Map<Integer, Integer> targets;
        
        public ParsedData(int[][] landscape, Map<String, Integer> tiles, Map<Integer, Integer> targets) {
            this.landscape = landscape;
            this.tiles = tiles;
            this.targets = targets;
        }
    }
    
    // Class to hold tile placement data
    static class TilePlacement {
        int tileIndex;
        String tileType;
        
        public TilePlacement(int tileIndex, String tileType) {
            this.tileIndex = tileIndex;
            this.tileType = tileType;
        }
    }
    
    // Applies a tile to the grid given its type and top-left position.
    public static void applyTile(String[][] grid, String tileType, int row, int col) {
        if ("FULL_BLOCK".equals(tileType)) {
            for (int i = row; i < row + TILE_SIZE; i++) {
                for (int j = col; j < col + TILE_SIZE; j++) {
                    grid[i][j] = "X";
                }
            }
        } else if ("OUTER_BOUNDARY".equals(tileType)) {
            // left and right boundaries
            for (int i = row; i < row + TILE_SIZE; i++) {
                grid[i][col] = "X";
                grid[i][col + TILE_SIZE - 1] = "X";
            }
            // top and bottom boundaries
            for (int j = col; j < col + TILE_SIZE; j++) {
                grid[row][j] = "X";
                grid[row + TILE_SIZE - 1][j] = "X";
            }
        } else if ("EL_SHAPE".equals(tileType)) {
            for (int i = 0; i < TILE_SIZE; i++) {
                for (int j = 0; j < TILE_SIZE; j++) {
                    if (EL_SHAPE_PATTERN[i][j] == 1) {
                        grid[row + i][col + j] = "X";
                    }
                }
            }
        }
    }
    
    // Parses the input text from tilesproblem.txt into landscape, tiles, and targets.
    public static ParsedData parseInput(String inputText) {
        // Split into sections (expecting sections to start with "# ")
        String[] sections = inputText.split("# ");
        
        // Parse landscape section (skip the first line which is the header)
        String[] landscapeLines = sections[1].split("\n");
        int[][] landscape = new int[GRID_SIZE][GRID_SIZE];
        for (int j = 0; j < GRID_SIZE; j++) {
            String rowStr = landscapeLines[j + 1]; // skip header line
            for (int i = 0; i < GRID_SIZE; i++) {
                // In the input, each cell is separated by a space.
                char ch = rowStr.charAt(i * 2);
                if (ch == ' ') {
                    landscape[j][i] = 0;
                } else {
                    // Assuming the character represents a digit
                    landscape[j][i] = Character.getNumericValue(ch);
                }
            }
        }
        
        // Parse tiles section
        String[] tilesLines = sections[2].trim().split("\n");
        // The second line contains the tile info (remove leading/trailing whitespace and the curly braces)
        String tilesLine = tilesLines[1].trim();
        if (tilesLine.startsWith("{") && tilesLine.endsWith("}")) {
            tilesLine = tilesLine.substring(1, tilesLine.length() - 1);
        }
        Map<String, Integer> tilesDict = new HashMap<>();
        String[] tileParts = tilesLine.split(",");
        for (String part : tileParts) {
            String[] keyVal = part.split("=");
            if (keyVal.length == 2) {
                String key = keyVal[0].trim();
                int value = Integer.parseInt(keyVal[1].trim());
                tilesDict.put(key, value);
            }
        }
        
        // Parse targets section
        String[] targetLines = sections[3].trim().split("\n");
        Map<Integer, Integer> targets = new HashMap<>();
        // Skip the header line
        for (int k = 1; k < targetLines.length; k++) {
            String target = targetLines[k];
            String[] parts = target.split(":");
            if (parts.length == 2) {
                int color = Integer.parseInt(parts[0].trim());
                int count = Integer.parseInt(parts[1].trim());
                targets.put(color, count);
            }
        }
        
        return new ParsedData(landscape, tilesDict, targets);
    }
    
    // Reads the tilesproblem.txt file, applies the tile placements, and prints the resulting grid and counters.
    public static void visualizeTiling(List<TilePlacement> tilePlacements, int gridSize) throws IOException {
        String inputText = new String(Files.readAllBytes(Paths.get("tilesproblem.txt")), StandardCharsets.UTF_8);
        ParsedData pd = parseInput(inputText);
        int[][] landscape = pd.landscape;
        
        // Create grid filled with "." (as strings)
        String[][] grid = new String[gridSize][gridSize];
        for (int i = 0; i < gridSize; i++) {
            Arrays.fill(grid[i], ".");
        }
        
        int blocksPerRow = gridSize / TILE_SIZE;
        // Place each tile based on its index and type
        for (TilePlacement placement : tilePlacements) {
            int tileIndex = placement.tileIndex;
            String tileType = placement.tileType;
            int row = (tileIndex / blocksPerRow) * TILE_SIZE;
            int col = (tileIndex % blocksPerRow) * TILE_SIZE;
            applyTile(grid, tileType, row, col);
        }
        
        // Create counters for uncovered landscape cells and for tile types.
        // Counter for colors: indices 1 to 4 (index 0 is unused)
        int[] counter = new int[5];
        // Counter for tile types
        Map<String, Integer> counter2 = new HashMap<>();
        counter2.put("FULL_BLOCK", 0);
        counter2.put("OUTER_BOUNDARY", 0);
        counter2.put("EL_SHAPE", 0);
        
        // Print the grid and update counter for uncovered cells that have a positive value in the landscape.
        for (int i = 0; i < gridSize; i++) {
            System.out.println(String.join(" ", grid[i]));
            for (int j = 0; j < gridSize; j++) {
                if (grid[i][j].equals(".") && landscape[i][j] > 0) {
                    counter[landscape[i][j]]++;
                }
            }
        }
        
        // Count tile placements by type
        for (TilePlacement placement : tilePlacements) {
            counter2.put(placement.tileType, counter2.get(placement.tileType) + 1);
        }
        
        // Print the counters
        System.out.println("Landscape counter (colors 1-4): " + Arrays.toString(counter));
        System.out.println("Tile type counter: " + counter2);
    }
    
    // Main method
    public static void main(String[] args) {
        try {
            List<TilePlacement> tilePlacements = new ArrayList<>();
            List<String> lines = Files.readAllLines(Paths.get("output.txt"), StandardCharsets.UTF_8);
            // Each line should have a tile index and a tile type separated by a space.
            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split(" ");
                int num = Integer.parseInt(parts[0]);
                String name = parts[1];
                tilePlacements.add(new TilePlacement(num, name));
            }
            visualizeTiling(tilePlacements, GRID_SIZE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
