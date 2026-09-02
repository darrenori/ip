package nori.ui;

import java.net.URL;

import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;

/**
 * Displays Nori's photographic penguin avatar.
 */
public final class BotAvatar extends StackPane {
    private static final String IMAGE_PATH = "/images/pingu-background.png";
    private static final Rectangle2D FACE_VIEWPORT = new Rectangle2D(190, 285, 430, 430);
    private static final Image BOT_IMAGE = loadBotImage();

    /**
     * Creates a circular Nori avatar at the requested size.
     *
     * @param size the width and height of the avatar in pixels
     */
    public BotAvatar(double size) {
        ImageView portrait = new ImageView(BOT_IMAGE);
        portrait.setFitWidth(size);
        portrait.setFitHeight(size);
        portrait.setViewport(FACE_VIEWPORT);
        portrait.setClip(new Circle(size / 2, size / 2, size / 2));

        Circle border = new Circle(size / 2);
        border.getStyleClass().add("bot-avatar-border");

        getChildren().addAll(portrait, border);
        setMinSize(size, size);
        setPrefSize(size, size);
        setMaxSize(size, size);
        setAccessibleText("Nori penguin avatar");
        setMouseTransparent(true);
    }

    /** Loads the bundled portrait and reports a clear error if it is missing. */
    private static Image loadBotImage() {
        URL imageUrl = BotAvatar.class.getResource(IMAGE_PATH);
        if (imageUrl == null) {
            throw new IllegalStateException("Unable to find Nori's avatar image: " + IMAGE_PATH);
        }
        return new Image(imageUrl.toExternalForm());
    }
}
