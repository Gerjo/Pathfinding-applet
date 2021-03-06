package Main;



import Maps.MapTools;
import PathFinding.AStar;
import Tile.Tile;
import View.Container;
import View.SettingsWindow;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.JApplet;
import javax.swing.UIManager;

/**
 *
 * @author Gerjo Meier
 */
public class PathFindingApplet extends JApplet {
    public static PathFindingApplet INSTANCE;

    private Container mainInterface;
    public Tile[][] tiles;
    public AStar finder;

    @Override
    public void start() {
        try {
            //UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            System.out.println("fail :( " + e);
        }

        PathFindingApplet.INSTANCE = this;

        setSize(800, 600);

        try {
            //tiles = MapTools.getDefaultCanvas(this.getSize());
            tiles = MapTools.loadFromURL("http://gerardmeier.com/java/map.php");
        } catch (FileNotFoundException ex) {
            System.out.println(ex);
            tiles = MapTools.getDefaultCanvas();
        } catch (IOException ex) {
            System.out.println(ex);
            tiles = MapTools.getDefaultCanvas();
        }

        mainInterface = new Container();

        setContentPane(mainInterface);
        finder = new AStar(this);
    }

    public void startScript() {
        if(!finder.isSearching) {

            SettingsWindow.INSTANCE.type.setText(finder.searchmode.name());
            SettingsWindow.INSTANCE.tilesScanned.setText("0");
            SettingsWindow.INSTANCE.tilesInPath.setText("0");

            tiles = MapTools.resetMap(tiles);

            MapTools.storeToGerardMeier(MapTools.asString(tiles));

            mainInterface.repaint();

            finder.loadMap(tiles);
            finder.solveMap();



        }
    }

    public void repaintTiles() {
        // Propagate the event to the canvas which actually repaints the tiles.
        //mainInterface.getCanvas().repaint();
    }

    public Tile[][] getTiles() {
        return tiles;
    }
}
