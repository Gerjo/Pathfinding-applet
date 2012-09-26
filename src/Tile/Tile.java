package Tile;


import Main.PathFindingApplet;
import PathFinding.AStar;
import View.SettingsWindow;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.util.Comparator;

/**
 *
 * @author Gerjo Meier
 */
public final class Tile implements Comparable<Tile> {
    public  final Point position;   // Position based on the screen.
    public  final Point raster;     // Position based on raster.
    public  final Dimension size;   // height and width of this tile.
    public  TileState state;        // Also holds the colors and settings.
    public  boolean isInOpenList     = false;

    // LinkedList:
    public  Tile previous = null;

    // The adjacent tiles are only loaded when required.
    public  Tile[] adjecentTiles;

    // The required A* variables for this tile:
    public  double g = 0; // steps taken
    public  double h = 0; // Heuristic
    public  double c = 0; // Potential cost to move to this tile. This value changes frequently.
    public  AStar.Directions tempDirection;
    //private int f = 0; // g+h (score)

    public Tile() {
        this(0, 0, 0, 0, TileState.ERROR);
    }

    public Tile(int x, int y, int sizeX, int sizeY, TileState state) {
        this.size   = new Dimension(sizeX, sizeY);
        this.state  = state;

        position    = new Point(x * size.width, y * size.height);
        raster      = new Point(x, y);
    }

    public final void drawObject(Graphics graphics) {
        // Background color of this tile:
        graphics.setColor(state.getBackgroundColor());
        graphics.fillRect(position.x, position.y, size.width, size.height);

        // A nice border around this tile:
        graphics.setColor(Color.black);
        graphics.drawRect(position.x, position.y, size.width, size.height);

        // Show F, G and H values.
        //graphics.setColor(Color.black);
        //graphics.drawString("" + (int)getF(), position.x + 0, position.y + 15);
        //graphics.drawString("g:" + g + " " + "h:" + h, position.x + 2, position.y + 30);
        
        if(previous != null) {
            //graphics.drawString(previous.raster.x + "." + previous.raster.y, position.x + 2, position.y + 45);
        }
       // graphics.drawString(this.raster.x + "." + this.raster.y, position.x + 2, position.y + 60);
    }

    public final double getF() {
        boolean useTieBreaker = false;
        switch(PathFindingApplet.INSTANCE.finder.searchmode) {
            case MANHATTAN:
                if(SettingsWindow.INSTANCE.manhattanTiebreak.isSelected()) useTieBreaker = true;
                break;
            case EULER:
                if(SettingsWindow.INSTANCE.eulerTiebreak.isSelected()) useTieBreaker = true;
                break;
            case DIAGONAL:
                if(SettingsWindow.INSTANCE.manhattanTiebreak.isSelected()) useTieBreaker = true;
                break;
        }
        if(useTieBreaker) {
            return g + h + (h*0.0001);
        } else {
            return g + h;
        }
    }

    public final Dimension getSize() {
        return size;
    }

    @Override
    public final String toString() {
        return "[Tile Class (x:" + raster.x + ", y:" + raster.y + ", f:" + getF() + ", g:" + g + ", h:" + h + ")]";
    }

    // Note: this class has a natural ordering that is inconsistent with equals.
    public int compareTo(Tile o) {
        if(o.getF() == getF()) return 0;
        if(o.getF() > getF())  return -1;
        return 1;
    }
}
