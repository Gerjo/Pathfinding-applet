package Maps;


import Tile.TileState;
import Tile.Tile;
import java.awt.Dimension;
import java.beans.Encoder;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Random;



/**
 *
 * @author Gerjo Meier
 */
public final class MapTools {

    // TODO: this is where we eventually add a XML parser.
    // TODO: the hardcoded size, should be loaded via XML.
    // TODO: add some sort of data validation.
    public static Tile[][] loadFromURL(String filename) throws FileNotFoundException, IOException {
        // ArrayList is used because we don't know yet how many rows the map has:
        ArrayList<Tile[]> tiles = new ArrayList<Tile[]>();

        URL webFile = new URL(filename);

        // Stream to read the file: (TODO: use InputStreamReader via a URL):
        BufferedReader reader = new BufferedReader(new InputStreamReader(webFile.openStream()));

        String line;

        // TIP: we can rework this by creating a large array, then trim to size upon return
        
        // Iterate over every single line in the file:
        for(int y = 0; (line = reader.readLine()) != null; ++y) {
            // Add a new row to the ArrayList:
            tiles.add(y, new Tile[line.length()]);

            // Iterate over every character, adding a new column enrty to the map.
            for(int x = 0; x < line.length(); ++x) {
                
                // Notice how y and x are stored the other way around.
                tiles.get(y)[x] = new Tile(x, y, 20, 20, TileState.valueOf(line.charAt(x)));
            }
        }



        if(tiles.isEmpty()) {
            Tile tmp[][] = getDefaultCanvas();
            storeToGerardMeier(asString(tmp));  
            
            return tmp;
        }

        // Next we store the items in the correct location, dus flip X with Y.
        Tile[][] toggle = new Tile[tiles.get(0).length][tiles.size()];

        for(int i = 0; i < toggle.length; ++i) {
            for(int j = 0; j < toggle[0].length; ++j) {
                toggle[i][j] = tiles.get(j)[i];
            }
        }
        tiles = null; // Help the GC
        return toggle;
        //return tiles.toArray(new Tile[tiles.size()][largestX]);
    }

    public static Tile[][] getDefaultCanvas() {



        Tile[][] tiles  = new Tile[400][400];

        for(int x = 0; x < tiles.length ; ++x) {
            for(int y = 0; y < tiles[0].length; ++y) {
                tiles[x][y] = new Tile(x, y, 3, 3, TileState.WALKABLE);
            }
        }

        tiles[0][0].state = TileState.START;
        tiles[tiles.length-1][tiles[0].length-1].state = TileState.END;

        return tiles;
    }

    public static Tile[][] resetMap(Tile[][] tiles) {
        for(int x = 0; x < tiles.length ; ++x) {
            for(int y = 0; y < tiles[0].length; ++y) {
                // Reset the state:
                if(tiles[x][y].state == TileState.PATH || tiles[x][y].state == TileState.TESTING) {
                    tiles[x][y].state = TileState.WALKABLE;
                }
                
                // Reset other data:
                tiles[x][y].adjecentTiles   = null;
                tiles[x][y].previous        = null;
                tiles[x][y].g = tiles[x][y].h = 0;
                tiles[x][y].isInOpenList    = false;
            }
        }
        return tiles;
    }

    public static String asString(Tile[][] tiles) {
        tiles = resetMap(tiles);

        Tile[][] flip = new Tile[tiles[0].length][tiles.length];
        for(int x = 0; x < tiles.length ; ++x) {
            for(int y = 0; y < tiles[0].length; ++y) {
                flip[y][x] = tiles[x][y];
            }
        }

        String out = "";
        for(int x = 0; x < flip.length ; ++x) {
            for(int y = 0; y < flip[0].length; ++y) {
                out += flip[x][y].state.getAsciiRepresentation();
            }
            out += "\n";
        }

        return out;//.trim();
    }

    public static void storeToGerardMeier(String data) {
        try {
            new URL("http://gerardmeier.com/java/map.php?d=" + URLEncoder.encode(data)).openStream();
        } catch (Exception ex) {
            System.out.println(ex);
        }
    }

    public static void randomMap(Tile[][] tiles) {

        int density = 1 + new Random().nextInt(2);

        flushMap(tiles);
        for(int x = 0; x < tiles.length ; ++x) {
            for(int y = 0; y < tiles[0].length; ++y) {
                if(tiles[x][y].state != TileState.END && tiles[x][y].state != TileState.START) {
                    tiles[x][y].state = TileState.WALKABLE;

                    if(new Random().nextInt(5) < density) {
                        tiles[x][y].state = TileState.NONWALKABLE;
                    }
                }
            }
        }
    }

    public static void flushMap(Tile[][] tiles) {
        tiles = resetMap(tiles);
        for(int x = 0; x < tiles.length ; ++x) {
            for(int y = 0; y < tiles[0].length; ++y) {
                if(tiles[x][y].state != TileState.END && tiles[x][y].state != TileState.START) {
                    tiles[x][y].state = TileState.WALKABLE;
                }
            }
        }
    }
}
