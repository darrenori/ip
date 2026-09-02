package nori;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import nori.ui.GuiUi;
import nori.ui.MainWindow;

/**
 * Displays Nori's JavaFX user interface.
 */
public class Main extends Application {
    private static final double WINDOW_MIN_HEIGHT = 600;
    private static final double WINDOW_MIN_WIDTH = 720;
    private static final double WINDOW_PREFERRED_HEIGHT = 720;
    private static final double WINDOW_PREFERRED_WIDTH = 920;

    /** Captures command responses for display in the JavaFX interface. */
    private final GuiUi guiUi = new GuiUi();
    /** Coordinates commands, tasks, and storage for the GUI session. */
    private final Nori nori = new Nori(guiUi);

    /** {@inheritDoc} */
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("/view/MainWindow.fxml"));
        Parent root = loader.load();
        MainWindow mainWindow = loader.getController();
        mainWindow.setNori(nori, guiUi);

        Scene scene = new Scene(root, WINDOW_PREFERRED_WIDTH, WINDOW_PREFERRED_HEIGHT);
        stage.setTitle("Nori — Task companion");
        stage.setMinHeight(WINDOW_MIN_HEIGHT);
        stage.setMinWidth(WINDOW_MIN_WIDTH);
        stage.setScene(scene);
        stage.show();
    }
}
