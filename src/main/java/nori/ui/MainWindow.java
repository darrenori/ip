package nori.ui;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import nori.Nori;

/**
 * Controls Nori's main JavaFX window.
 */
public final class MainWindow {
    private static final Duration EXIT_DELAY = Duration.millis(900);
    private static final String GREETING = "Hi! I'm Nori, your little task companion. "
            + "Type help to see everything we can do together.";

    @FXML
    private VBox dialogContainer;
    @FXML
    private StackPane logoSlot;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private Button sendButton;
    @FXML
    private Label statusLabel;
    @FXML
    private TextField userInput;

    /** Coordinates commands and persistent task state. */
    private Nori nori;
    /** Supplies the text response generated for each GUI command. */
    private GuiUi guiUi;

    /**
     * Initializes visual elements that are easier to express in Java.
     */
    @FXML
    public void initialize() {
        logoSlot.getChildren().add(new BotAvatar(50));
        dialogContainer.heightProperty()
                .addListener((observable, previousHeight, currentHeight) -> scrollPane.setVvalue(1.0));
        Platform.runLater(userInput::requestFocus);
    }

    /**
     * Injects Nori and displays the opening messages for this session.
     *
     * @param nori the application logic used to execute commands
     * @param guiUi the response adapter used by the graphical interface
     */
    public void setNori(Nori nori, GuiUi guiUi) {
        this.nori = nori;
        this.guiUi = guiUi;
        dialogContainer.getChildren().add(DialogBox.getNoriDialog(GREETING));

        String loadingMessage = nori.getLoadingMessage();
        if (loadingMessage != null) {
            dialogContainer.getChildren().add(DialogBox.getNoriDialog(loadingMessage));
        }
    }

    /** Adds the user's command and Nori's response to the conversation. */
    @FXML
    private void handleUserInput() {
        String input = userInput.getText().trim();
        if (input.isEmpty() || nori == null) {
            return;
        }

        userInput.clear();
        dialogContainer.getChildren().add(DialogBox.getUserDialog(input));
        boolean isExitRequested = nori.executeCommand(input);
        dialogContainer.getChildren().add(DialogBox.getNoriDialog(guiUi.consumeResponse()));

        if (isExitRequested) {
            endSession();
        } else {
            Platform.runLater(userInput::requestFocus);
        }
    }

    /** Disables further input and closes the application after the goodbye is visible. */
    private void endSession() {
        userInput.setDisable(true);
        sendButton.setDisable(true);
        statusLabel.setText("Session complete");

        PauseTransition exitPause = new PauseTransition(EXIT_DELAY);
        exitPause.setOnFinished(event -> Platform.exit());
        exitPause.play();
    }
}
