package nori.ui;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

/**
 * Displays one chat message together with an avatar and speaker label.
 */
public final class DialogBox extends HBox {
    private static final double AVATAR_SIZE = 46;
    /** Style class marking a reply that reports something Nori could not do. */
    private static final String ERROR_STYLE_CLASS = "bubble-error";

    @FXML
    private StackPane avatarSlot;
    @FXML
    private Label dialog;
    @FXML
    private VBox messageColumn;
    @FXML
    private Label speakerLabel;

    /** Creates a styled dialog box for one speaker. */
    private DialogBox(String text, String speaker, Node avatar, String bubbleStyle) {
        FXMLLoader loader = new FXMLLoader(DialogBox.class.getResource("/view/DialogBox.fxml"));
        loader.setController(this);
        loader.setRoot(this);
        try {
            loader.load();
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load the dialog box layout.", exception);
        }

        dialog.setText(text);
        dialog.setStyle(ConversationFont.getFamilyStyle());
        dialog.getStyleClass().add(bubbleStyle);
        speakerLabel.setText(speaker);
        avatarSlot.getChildren().setAll(avatar);
    }

    /**
     * Creates a dialog aligned to the right for user input.
     *
     * @param text the command entered by the user.
     * @return the user dialog box.
     */
    public static DialogBox getUserDialog(String text) {
        DialogBox dialogBox = new DialogBox(text, "YOU", createUserAvatar(), "user-bubble");
        dialogBox.alignToRight();
        return dialogBox;
    }

    /**
     * Creates a dialog aligned to the left for Nori's response.
     *
     * @param text Nori's response text.
     * @return Nori's dialog box.
     */
    public static DialogBox getNoriDialog(String text) {
        return getNoriDialog(text, false);
    }

    /**
     * Creates a dialog aligned to the left for Nori's response, marking an error as one.
     *
     * A reply saying a command could not be carried out is the reply a user
     * most needs to notice, and it arrives among replies that all look alike.
     * Marking it out means a user scanning the conversation can see that
     * something did not happen without reading every bubble to find out.
     *
     * @param text Nori's response text.
     * @param isError whether the response reports something Nori could not do.
     * @return Nori's dialog box.
     */
    public static DialogBox getNoriDialog(String text, boolean isError) {
        DialogBox dialogBox = new DialogBox(text, isError ? "NORI · PROBLEM" : "NORI",
                new BotAvatar(AVATAR_SIZE), "nori-bubble");
        if (isError) {
            dialogBox.dialog.getStyleClass().add(ERROR_STYLE_CLASS);
            dialogBox.speakerLabel.getStyleClass().add(ERROR_STYLE_CLASS);
        }
        return dialogBox;
    }

    /** Creates the user's simple, original text avatar. */
    private static StackPane createUserAvatar() {
        Circle background = new Circle(AVATAR_SIZE / 2);
        background.getStyleClass().add("user-avatar-background");

        Label initials = new Label("YOU");
        initials.getStyleClass().add("user-avatar-text");

        StackPane avatar = new StackPane(background, initials);
        avatar.setMinSize(AVATAR_SIZE, AVATAR_SIZE);
        avatar.setPrefSize(AVATAR_SIZE, AVATAR_SIZE);
        avatar.setMaxSize(AVATAR_SIZE, AVATAR_SIZE);
        avatar.setAccessibleText("User avatar");
        return avatar;
    }

    /** Places the avatar after the message and right-aligns the speaker details. */
    private void alignToRight() {
        getChildren().setAll(messageColumn, avatarSlot);
        setAlignment(Pos.TOP_RIGHT);
        messageColumn.setAlignment(Pos.TOP_RIGHT);
        speakerLabel.setAlignment(Pos.CENTER_RIGHT);
        dialog.setAlignment(Pos.CENTER_RIGHT);
    }
}
