package nori.ui;

/**
 * Captures Nori responses so that a graphical interface can display them.
 */
public final class GuiUi extends Ui {
    /** The most recent command response that has not been displayed. */
    private String pendingResponse = "";

    /** {@inheritDoc} */
    @Override
    public void showResponse(String... lines) {
        pendingResponse = String.join("\n", lines);
    }

    /**
     * Returns and clears the pending response.
     *
     * @return the response produced by the latest command
     */
    public String consumeResponse() {
        String response = pendingResponse;
        pendingResponse = "";
        return response;
    }
}
