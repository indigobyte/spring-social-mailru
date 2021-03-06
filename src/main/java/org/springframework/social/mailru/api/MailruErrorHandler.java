/*
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.social.mailru.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.social.*;
import org.springframework.social.mailru.Const;
import org.springframework.web.client.DefaultResponseErrorHandler;

import java.io.IOException;

/**
 * Subclass of {@link DefaultResponseErrorHandler} that handles errors from Mailru's
 * API, interpreting them into appropriate exceptions.
 *
 * @author Cackle
 */
public class MailruErrorHandler extends DefaultResponseErrorHandler {

    @Override
    public void handleError(ClientHttpResponse response) throws IOException {
        HttpStatus statusCode = response.getStatusCode();
        if (statusCode.series() == HttpStatus.Series.SERVER_ERROR) {
            handleServerErrors(statusCode);
        } else if (statusCode.series() == HttpStatus.Series.CLIENT_ERROR) {
            handleClientErrors(response);
        }

        // if not otherwise handled, do default handling and wrap with UncategorizedApiException
        try {
            super.handleError(response);
        } catch (Exception e) {
            throw new UncategorizedApiException(Const.providerId, "Error consuming Mailru REST API", e);
        }
    }

    private void handleClientErrors(ClientHttpResponse response) throws IOException {
        HttpStatus statusCode = response.getStatusCode();

        if (statusCode == HttpStatus.UNAUTHORIZED) {
            throw new NotAuthorizedException(Const.providerId, "User was not authorised.");
        } else if (statusCode == HttpStatus.FORBIDDEN) {
            throw new OperationNotPermittedException(Const.providerId, "User is forbidden to access this resource.");
        } else if (statusCode == HttpStatus.NOT_FOUND) {
            throw new ResourceNotFoundException(Const.providerId, "Resource was not found.");
        }
    }

    private void handleServerErrors(HttpStatus statusCode) throws IOException {
        if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR) {
            throw new InternalServerErrorException(Const.providerId, "Something is broken at Mailru.");
        } else if (statusCode == HttpStatus.BAD_GATEWAY) {
            throw new ServerDownException(Const.providerId, "Mailru is down or is being upgraded.");
        } else if (statusCode == HttpStatus.SERVICE_UNAVAILABLE) {
            throw new ServerOverloadedException(Const.providerId, "Mailru is overloaded with requests. Try again later.");
        }
    }
}
