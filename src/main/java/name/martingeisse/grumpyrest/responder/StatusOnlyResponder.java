package name.martingeisse.grumpyrest.responder;

import name.martingeisse.grumpyrest.RequestCycle;

/**
 * Sends an empty response with a configurable HTTP status code.
 */
public final class StatusOnlyResponder implements Responder {

    private final int status;

    public StatusOnlyResponder(int status) {
        this.status = status;
    }

    @Override
    public void respond(RequestCycle requestCycle) {
        requestCycle.getResponse().setStatus(status);
    }

}
