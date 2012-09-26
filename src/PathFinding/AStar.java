package PathFinding;

import Main.PathFindingApplet;
import Maps.MapTools;
import Tile.Tile;
import Tile.TileState;
import View.SettingsWindow;
import java.text.DecimalFormat;
import java.util.AbstractQueue;
import java.util.ArrayList;
import java.util.PriorityQueue;

/**
 *
 * @author Gerjo Meier
 */
public class AStar implements IPathFinding {
    private Tile[][] tiles;
    private PathFindingApplet owner;               // Not too gracefull.

    private long startTime;
    private Tile startTile;
    private Tile endTile;
    public boolean isSearching = false;

    public ArrayList<Tile> finalPath = new ArrayList<Tile>();


    public enum SearchModes { MANHATTAN, EULER, DIAGONAL, DIJKSTRA; }
    public SearchModes searchmode;

    public enum DataModes { HEAP, SORTEDSTACK; }
    public DataModes dataMode;

    public enum Directions { DIAGONAL, STRAIGHT; }


    // Some pathfinding magic:
    private AbstractQueue<Tile> openTiles;

    public AStar(PathFindingApplet owner) {
        this.owner      = owner;

        searchmode = SearchModes.MANHATTAN;
        dataMode   = DataModes.HEAP;
    }

    public void loadMap(Tile[][] tiles) {
        this.tiles      = tiles;

        for(int x = 0; x < tiles.length; ++x) {
            for(int y = 0; y < tiles[x].length; ++y) {
                switch(tiles[x][y].state) {
                    case START:
                        startTile = tiles[x][y];
                        break;
                    case END:
                        endTile = tiles[x][y];
                        break;
                }
            }
        }

        // TODO: Throw some exception here, so the owner can deal with the mess.
        if(startTile == null || endTile == null) {
            System.out.println("meh fail.");
        }

        SettingsWindow.INSTANCE.tilesInWorld.setText("" + (tiles.length * tiles[0].length));
    }

    public void solveMap() {
        dataMode = (SettingsWindow.INSTANCE.useHeap.isSelected())?DataModes.HEAP:DataModes.SORTEDSTACK;
        switch(dataMode) {
            case HEAP:
                openTiles = new PriorityQueue<Tile>(tiles.length * tiles[0].length);
                SettingsWindow.INSTANCE.openNodesStruct.setText("Using heap data structure.");
                break;
            case SORTEDSTACK:
                openTiles = new SortedStack<Tile>(tiles.length * tiles[0].length);
                SettingsWindow.INSTANCE.openNodesStruct.setText("Using stack + java merge sort.");
                break;
        }

        addToOpenList(startTile);
        isSearching = true;
        pathTo();

        startTime = System.nanoTime();
        new Thread(new Runnable() {
            public void run() {
                while(isSearching) {
                    pathTo();

                    if(SettingsWindow.INSTANCE.getSpeedDelay() < 2) continue;
                    try {
                        Thread.sleep(SettingsWindow.INSTANCE.getSpeedDelay()); // ZZZzzZzz
                    } catch (InterruptedException ex) {

                    }
                }
                owner.repaint();
                return;
            }
        }).start();
    }

    public synchronized void pathTo() {
        if(!isSearching) return;
        Tile current = openTiles.poll();
        if(current == null) {
            isSearching = false;
            SettingsWindow.INSTANCE.tilesInPath.setText("No path found!");
            SettingsWindow.INSTANCE.timetaken.setText("" + new DecimalFormat("###.############").format((System.nanoTime() - startTime) * 0.000000001));


            return;
        }
        
        // AdjectentTiles are unknown, so lets "find" them!
        if(current.adjecentTiles == null) {
            current.adjecentTiles = getTilesAdjacentTo(current);
        }

        if(current.state != TileState.START) current.state = TileState.TESTING;

        for(Tile adjacent : current.adjecentTiles) {

            //if(adjacent.state != TileState.START && adjacent.state != TileState.END) adjacent.state = TileState.TESTING;

            // Increment the steps taken:
            if(!adjacent.isInOpenList) {
            switch(searchmode) {
                case MANHATTAN:
                    adjacent.g = current.g + adjacent.c;//Double.valueOf(SettingsWindow.INSTANCE.manhattanDiagonalStepCost1.getText());
                    break;
                case EULER:
                    adjacent.g = current.g + adjacent.c;//Double.valueOf(SettingsWindow.INSTANCE.eulerDiagonalStepCost.getText());
                    break;
                case DIAGONAL:
                    adjacent.g = current.g + adjacent.c;//Double.valueOf(SettingsWindow.INSTANCE.diagonalDiagonalStepCost2.getText());
                    break;
                case DIJKSTRA:
                    adjacent.g = current.g + adjacent.c;//;
                    break;
            }}

            switch(adjacent.state) {
                case WALKABLE:
                case TESTING:
                    // Continue the linkedlist chain:
                    if(!adjacent.isInOpenList) adjacent.previous = current;
                    

                    // Mark this tile as "open".
                    addToOpenList(adjacent);

                    adjacent.state = TileState.TESTING;
                    break;

                case END: // TODO add final tile to linkedlist.
                    finalPath.clear();

                    Tile handle = current;
                    while(handle != null) {
                        finalPath.add(handle);
                        if(tiles[handle.raster.x][handle.raster.y].state != TileState.START) tiles[handle.raster.x][handle.raster.y].state = TileState.PATH;
                        handle = handle.previous;
                    }
                    SettingsWindow.INSTANCE.tilesInPath.setText("" + finalPath.size());

                    SettingsWindow.INSTANCE.timetaken.setText("" + new DecimalFormat("###.############").format((System.nanoTime() - startTime) * 0.000000001));

                    isSearching = false;
                    break;
            }
        }

        if(SettingsWindow.INSTANCE.getSpeedDelay() < 1) return;
        // Inform the "applet" that we're due a repaint.
        owner.repaint();
        //pathTo();
    }

    private void addToOpenList(Tile tile) {
        if(!tile.isInOpenList) {
            tile.isInOpenList = true;
            openTiles.offer(tile);

            SettingsWindow.INSTANCE.tilesScanned.setText("" + (Integer.valueOf(SettingsWindow.INSTANCE.tilesScanned.getText())+1));
        }
    }

    private double getStepCost(Directions direction) {

        switch(direction) {
            case DIAGONAL:
                switch(searchmode) {
                    case MANHATTAN: return Double.valueOf(SettingsWindow.INSTANCE.manhattanDiagonalStepCost1.getText());
                    case EULER:     return Double.valueOf(SettingsWindow.INSTANCE.eulerDiagonalStepCost.getText());
                    case DIAGONAL:  return Double.valueOf(SettingsWindow.INSTANCE.diagonalDiagonalStepCost2.getText());
                    case DIJKSTRA:  return Double.valueOf(SettingsWindow.INSTANCE.dijkstraDiagonalStepCost3.getText());
                    default: return 1;
                }
            case STRAIGHT:
                switch(searchmode) {
                    case MANHATTAN: return Double.valueOf(SettingsWindow.INSTANCE.manhattanStraightStepCost1.getText());
                    case EULER:     return Double.valueOf(SettingsWindow.INSTANCE.eulerStraightStepCost.getText());
                    case DIAGONAL:  return Double.valueOf(SettingsWindow.INSTANCE.diagonalStraightStepCost2.getText());
                    case DIJKSTRA:  return Double.valueOf(SettingsWindow.INSTANCE.dijkstraStraightStepCost3.getText());
                    default: return 1;
                }
            default: return 1;
        }
    }

    private boolean isTileAt(int x, int y) {
        return (x < tiles.length  && x >= 0 && y < tiles[0].length && y >= 0);
    }

    private Tile[] getTilesAdjacentTo(Tile someTile) {
        ArrayList<Tile> found = new ArrayList<Tile>(8);

        if(SettingsWindow.INSTANCE.allowStraightSteps.isSelected()) {
            // Straight movement:
            if(isTileAt(someTile.raster.x, someTile.raster.y + 1)) {
                found.add(setTileHeuristic(tiles[someTile.raster.x][someTile.raster.y + 1], Directions.STRAIGHT)); // "above"
            }
            if(isTileAt(someTile.raster.x, someTile.raster.y - 1)) {
                found.add(setTileHeuristic(tiles[someTile.raster.x][someTile.raster.y - 1], Directions.STRAIGHT)); // "underneath"
            }
            if(isTileAt(someTile.raster.x - 1, someTile.raster.y)) {
                found.add(setTileHeuristic(tiles[someTile.raster.x - 1][someTile.raster.y], Directions.STRAIGHT)); // "left"
            }
            if(isTileAt(someTile.raster.x + 1, someTile.raster.y)) {
                found.add(setTileHeuristic(tiles[someTile.raster.x + 1][someTile.raster.y], Directions.STRAIGHT)); // "right"
            }
        }
        if(SettingsWindow.INSTANCE.allowDiagonalSteps.isSelected()) {

            // Diagonal movement:
            if(isTileAt(someTile.raster.x + 1, someTile.raster.y + 1)) {
                found.add(setTileHeuristic(tiles[someTile.raster.x + 1][someTile.raster.y + 1], Directions.DIAGONAL)); // "above"
            }
            if(isTileAt(someTile.raster.x - 1, someTile.raster.y - 1)) {
                found.add(setTileHeuristic(tiles[someTile.raster.x - 1][someTile.raster.y - 1], Directions.DIAGONAL)); // "underneath"
            }
            if(isTileAt(someTile.raster.x - 1, someTile.raster.y + 1)) {
                found.add(setTileHeuristic(tiles[someTile.raster.x - 1][someTile.raster.y + 1], Directions.DIAGONAL)); // "left"
            }
            if(isTileAt(someTile.raster.x + 1, someTile.raster.y - 1)) {
                found.add(setTileHeuristic(tiles[someTile.raster.x + 1][someTile.raster.y - 1], Directions.DIAGONAL)); // "right"
            }
        }
       
        return found.toArray(new Tile[found.size()]);
    }

    public Tile setTileHeuristic(Tile someTile, Directions direction) {

        double weightManhattan = 0, weightEuler = 0, weightDiagonal = 0;


        int diffX = Math.abs(endTile.raster.x - someTile.raster.x);
        int diffY = Math.abs(endTile.raster.y - someTile.raster.y);

        someTile.c = getStepCost(direction);

        if(someTile.isInOpenList) return someTile;

        switch(searchmode) {
            case MANHATTAN:
                someTile.h = (diffX + diffY) * Double.valueOf(SettingsWindow.INSTANCE.manhattanHmultiplier1.getText());
                break;
            case EULER:
                someTile.h = Math.sqrt(diffX*diffX + diffY * diffY) * Double.valueOf(SettingsWindow.INSTANCE.eulerHmultiplier.getText());
                break;
            case DIAGONAL:
                someTile.h = Math.max(diffX, diffY) * Double.valueOf(SettingsWindow.INSTANCE.diagonalHmultiplier2.getText());
                break;
            case DIJKSTRA:
                someTile.h = 1;
                break;
        }

        return someTile;
    }
}
