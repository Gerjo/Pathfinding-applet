package Tile;


import java.awt.Color;

/**
 *
 * @author Gerjo Meier
 * This is actually an test effort to experiment with "settings" stored in
 * an enum, rather than the tile itself.
 * 
 */

public enum TileState {
    ERROR (Color.cyan,          '@'),
    TESTING (Color.pink,        'T'), // walkable
    WALKABLE (Color.magenta,    ' '), // walkable
    NONWALKABLE (Color.black,   '#'),
    START (Color.green,         'S'),
    END (Color.red,             'E'),
    PATH (Color.blue,           '+');

    private final Color backgroundColor;
    private final char asciiRepresentation;
    
    TileState(Color backgroundColor, char asciiRepresentation) {
        this.backgroundColor     = backgroundColor;
        this.asciiRepresentation = asciiRepresentation;
    }

    public final Color getBackgroundColor() {
        return backgroundColor;
    }
    
    public final char getAsciiRepresentation() {
        return asciiRepresentation;
    }

    public static TileState valueOf(char asciiRepresentation) {
        for(TileState test : values()) {
            if(test.getAsciiRepresentation() == asciiRepresentation) {
                return test;
            }
        }
        return ERROR;
    }

    public static TileState valueOf(Color backgroundColor) {
        for(TileState test : values()) {
            if(test.getBackgroundColor() == backgroundColor) {
                return test;
            }
        }
        return ERROR;
    }
}
