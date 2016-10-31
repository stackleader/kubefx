package com.stackleader.kubefx.ui.auth;

import com.stackleader.kubefx.kubernetes.api.model.BasicAuthCredential;
import aQute.bnd.annotation.component.Component;
import java.time.Duration;
import java.util.Arrays;
import javax.net.ssl.SSLContext;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dcnorris
 */
@Component(immediate = true, provide = BasicAuthCredentialValidator.class)
public class BasicAuthCredentialValidator {

    private static final Logger LOG = LoggerFactory.getLogger(BasicAuthCredentialValidator.class);
    private RequestConfig defaultConfig;

    public BasicAuthCredentialValidator() {
        init();
    }

    private void init() {
        try {
            SSLContext sslcontext = SSLContexts.custom().loadTrustMaterial(null,
                    new TrustSelfSignedStrategy()).build();
            sslsf = new SSLConnectionSocketFactory(sslcontext,
                    new String[]{"TLSv1", "TLSv1.2"}, null, new NoopHostnameVerifier());

            defaultConfig = RequestConfig.custom()
                    .setCookieSpec(CookieSpecs.IGNORE_COOKIES)
                    .setTargetPreferredAuthSchemes(Arrays.asList(AuthSchemes.BASIC))
                    .build();
        } catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
        }
    }
    private SSLConnectionSocketFactory sslsf;

    public boolean credentialsValid(BasicAuthCredential authCredential) {
        final String username = authCredential.getUsername();
        final String password = authCredential.getPassword();
        final String masterUrl = authCredential.getMasterUrl() + "/api/v1";
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password);
        credentialsProvider.setCredentials(AuthScope.ANY, credentials);
        CloseableHttpClient client = HttpClients.custom()
                .setSSLSocketFactory(sslsf)
                .setDefaultCredentialsProvider(credentialsProvider)
                .build();
        RequestConfig requestConfig = RequestConfig.copy(defaultConfig)
                .setSocketTimeout((int) Duration.ofSeconds(4).toMillis())
                .setConnectTimeout((int) Duration.ofSeconds(3).toMillis())
                .setConnectionRequestTimeout((int) Duration.ofSeconds(3).toMillis())
                .setAuthenticationEnabled(true)
                .build();
        HttpGet httpGet = new HttpGet(masterUrl);
        httpGet.setConfig(requestConfig);
        try {
            CloseableHttpResponse response = client.execute(httpGet);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                return true;
            } else if (statusCode == 401) {
                return false;
            }
        } catch (Throwable t) {
        }
        return false;
    }

}
