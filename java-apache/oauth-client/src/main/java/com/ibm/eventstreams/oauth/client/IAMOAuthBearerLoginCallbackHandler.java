// Copyright 2023 IBM
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.ibm.eventstreams.oauth.client;


import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.AppConfigurationEntry;

import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.security.auth.AuthenticateCallbackHandler;
import org.apache.kafka.common.security.oauthbearer.OAuthBearerToken;
import org.apache.kafka.common.security.oauthbearer.OAuthBearerTokenCallback;
import org.apache.kafka.common.security.oauthbearer.secured.AccessTokenRetriever;
import org.apache.kafka.common.security.oauthbearer.secured.AccessTokenValidator;
import org.apache.kafka.common.security.oauthbearer.secured.AccessTokenValidatorFactory;
import org.apache.kafka.common.security.oauthbearer.secured.JaasOptionsUtils;
import org.apache.kafka.common.security.oauthbearer.secured.ValidateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class IAMOAuthBearerLoginCallbackHandler implements AuthenticateCallbackHandler {

    private static final Logger logger = LoggerFactory.getLogger(IAMOAuthBearerLoginCallbackHandler.class);

    private Map<String, Object> moduleOptions;

    private AccessTokenRetriever accessTokenRetriever;

    private AccessTokenValidator accessTokenValidator;

    private boolean isInitialized = false;

    @Override
    public void configure(Map<String, ?> configs, String saslMechanism, List<AppConfigurationEntry> jaasConfigEntries) {
        moduleOptions = JaasOptionsUtils.getOptions(saslMechanism, jaasConfigEntries);
        AccessTokenRetriever accessTokenRetriever = IAMTokenRetrieverFactory.create(configs, saslMechanism, moduleOptions);
        AccessTokenValidator accessTokenValidator = AccessTokenValidatorFactory.create(configs, saslMechanism);
        init(accessTokenRetriever, accessTokenValidator);
    }

    void init(AccessTokenRetriever accessTokenRetriever, AccessTokenValidator accessTokenValidator) {
        this.accessTokenRetriever = accessTokenRetriever;
        this.accessTokenValidator = accessTokenValidator;

        try {
            this.accessTokenRetriever.init();
        } catch (IOException e) {
            throw new KafkaException("The OAuth login configuration encountered an error when initializing the AccessTokenRetriever", e);
        }

        isInitialized = true;
    }

    @Override
    public void close() {
        if (accessTokenRetriever != null) {
            try {
                this.accessTokenRetriever.close();
            } catch (IOException e) {
                logger.warn("The OAuth login configuration encountered an error when closing the AccessTokenRetriever", e);
            }
        }
    }

    @Override
    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        checkInitialized();

        for (Callback callback : callbacks) {
            if (callback instanceof OAuthBearerTokenCallback) {
                handleTokenCallback((OAuthBearerTokenCallback) callback);
            } else {
                throw new UnsupportedCallbackException(callback);
            }
        }
    }

    private void handleTokenCallback(OAuthBearerTokenCallback callback) throws IOException {
        checkInitialized();
        String accessToken = accessTokenRetriever.retrieve();

        try {
            OAuthBearerToken token = accessTokenValidator.validate(accessToken);
            callback.token(token);
        } catch (ValidateException e) {
            logger.warn(e.getMessage(), e);
            callback.error("invalid_token", e.getMessage(), null);
        }
    }


    private void checkInitialized() {
        if (!isInitialized)
            throw new IllegalStateException(String.format("To use %s, first call the configure or init method", getClass().getSimpleName()));
    }

}
