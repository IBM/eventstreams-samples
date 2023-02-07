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

import org.apache.kafka.common.security.oauthbearer.secured.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLSocketFactory;
import java.net.URL;
import java.util.Map;

import static org.apache.kafka.common.config.SaslConfigs.*;

public class IAMTokenRetrieverFactory {

    private static final Logger logger = LoggerFactory.getLogger(IAMTokenRetrieverFactory.class);

    public static final String IAM_TOKEN_ENDPOINT = "https://iam.cloud.ibm.com/identity/token";
    public static final String GRANT_TYPE_CONFIG = "grant_type";
    public static final String GRANT_TYPE_APIKEY = "urn:ibm:params:oauth:grant-type:apikey";
    public static final String GRANT_EXT_APIKEY = "apikey";
    public static final String GRANT_EXT_TOKEN = "access_token";
    public static final String SASL_MECHANISM_OAUTH = "OAUTHBEARER";

    public static AccessTokenRetriever create(
            Map<String, ?> configs,
            String saslMechanism,
            Map<String, Object> jaasConfig) {
        if (!SASL_MECHANISM_OAUTH.equals(saslMechanism)) {
            throw new IllegalArgumentException(String.format("unsupported SASL_MECHANISM:'%s'", saslMechanism));
        }
        ConfigurationUtils cu = new ConfigurationUtils(configs, saslMechanism);
        URL tokenEndpointUrl = null ;
        try {
            tokenEndpointUrl = cu.validateUrl(SASL_OAUTHBEARER_TOKEN_ENDPOINT_URL);
        }catch (Exception e) {
            logger.warn("invalid "+SASL_OAUTHBEARER_TOKEN_ENDPOINT_URL +" fall back to :"+ IAM_TOKEN_ENDPOINT);
            try {
                tokenEndpointUrl = new URL(IAM_TOKEN_ENDPOINT);
            }catch(Exception ee) {
                // this should not happen
            }
        }

        JaasOptionsUtils jou = new JaasOptionsUtils(jaasConfig);
        String grantType = jou.validateString(GRANT_TYPE_CONFIG, true);
        String apikey = jou.validateString(GRANT_EXT_APIKEY, true);

        SSLSocketFactory sslSocketFactory = null;

        if (jou.shouldCreateSSLSocketFactory(tokenEndpointUrl))
            sslSocketFactory = jou.createSSLSocketFactory();

        if (grantType.equals(GRANT_TYPE_APIKEY)) {
            return new IAMAPIKeyTokenRetriever(grantType, apikey, tokenEndpointUrl.toString(), sslSocketFactory,
                    cu.validateLong(SASL_LOGIN_RETRY_BACKOFF_MS), cu.validateLong(SASL_LOGIN_RETRY_BACKOFF_MAX_MS),
                    cu.validateInteger(SASL_LOGIN_CONNECT_TIMEOUT_MS, false),
                    cu.validateInteger(SASL_LOGIN_READ_TIMEOUT_MS, false));
        } else {
            throw new IllegalArgumentException(String.format("unsupported grant_type:'%s'", grantType));
        }
    }
}
