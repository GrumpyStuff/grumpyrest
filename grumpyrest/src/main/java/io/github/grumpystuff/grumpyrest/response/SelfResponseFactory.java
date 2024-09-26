package io.github.grumpystuff.grumpyrest.response;

import java.io.IOException;

/**
 * Implementations of this interface can be returned as a response without using a specialized factory, but instead
 * of implementing the {@link Response} interface manually, they just produce another Response instance. This
 * simplifies re-using a standard response implementation for specific cases without registering a custom factory.
 */
public interface SelfResponseFactory extends Response {

    /**
     * Creates the response.
     *
     * @return the response
     */
    Response createResponse();

    @Override
    default void transmit(ResponseTransmitter responseTransmitter) throws IOException {
        createResponse().transmit(responseTransmitter);
    }

}
