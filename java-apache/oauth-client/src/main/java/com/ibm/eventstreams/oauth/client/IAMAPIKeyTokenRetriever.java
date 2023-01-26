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
import java.util.HashMap;

import javax.net.ssl.SSLSocketFactory;

import org.apache.kafka.common.security.oauthbearer.secured.AccessTokenRetriever;

public class IAMAPIKeyTokenRetriever extends IAMTokenRetriever implements AccessTokenRetriever {

    public IAMAPIKeyTokenRetriever(String grantType, String apikey, String tokenEndpointUrl,
                                   SSLSocketFactory sslSocketFactory, long loginRetryBackoffMs, long loginRetryBackoffMaxMs,
                                   Integer loginConnectTimeoutMs, Integer loginReadTimeoutMs) {
        this.grantExtensions = new HashMap<String, String>();
        grantExtensions.put(IAMTokenRetrieverFactory.GRANT_TYPE_CONFIG, grantType);
        grantExtensions.put(IAMTokenRetrieverFactory.GRANT_EXT_APIKEY, apikey);

        this.tokenEndpointUrl = tokenEndpointUrl;
        this.sslSocketFactory = sslSocketFactory;
        this.loginRetryBackoffMs = loginRetryBackoffMs;
        this.loginRetryBackoffMaxMs = loginRetryBackoffMaxMs;
        this.loginConnectTimeoutMs = loginConnectTimeoutMs;
        this.loginReadTimeoutMs = loginReadTimeoutMs;
    }

    @Override
    public String retrieve() throws IOException {
        return super.retrieve();
    }
}
