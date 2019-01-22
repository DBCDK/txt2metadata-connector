/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GPLv3
 * See license text in LICENSE.txt or at https://opensource.dbc.dk/licenses/gpl-3.0/
 */

package dk.dbc.txt2metadata;

import dk.dbc.httpclient.FailSafeHttpClient;
import dk.dbc.httpclient.HttpGet;
import dk.dbc.httpclient.HttpPost;
import dk.dbc.httpclient.PathBuilder;
import dk.dbc.invariant.InvariantUtil;
import dk.dbc.util.Stopwatch;
import net.jodah.failsafe.RetryPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Txt2MetadataConnector {
    private static final Logger LOGGER = LoggerFactory.getLogger(Txt2MetadataConnector.class);

    public enum TimingLogLevel {
        TRACE, DEBUG, INFO, WARN, ERROR
    }

    private static final int DEFAULT_MATCHES = 10;
    private static final String PATH_VARIABLE_ARTICLE_ID = "articleId";
    private static final String PATH_SUGGESTIONS_FOR_ARTICLE = String.format("/api/documents/similar/{%s}",
            PATH_VARIABLE_ARTICLE_ID);
    private static final String PATH_SUGGESTIONS_FOR_ARTICLES = String.format("/api/documents/similar/articleids");
    private static final String PATH_SUGGESTIONS_ON_TEXT = String.format("/api/documents/similar");
    private static final RetryPolicy RETRY_POLICY = new RetryPolicy()
            .retryOn(Collections.singletonList(ProcessingException.class))
            .retryIf((Response response) -> response.getStatus() == 404 || response.getStatus() == 502)
            .withDelay(10, TimeUnit.SECONDS)
            .withMaxRetries(6);

    private final FailSafeHttpClient failSafeHttpClient;
    private final String baseUrl;
    private final LogLevelMethod logger;

    /**
     * Returns new instance with default retry policy
     *
     * @param httpClient web resources client
     * @param baseUrl    base URL for txt2metadata api endpoint
     */
    public Txt2MetadataConnector(Client httpClient, String baseUrl) {
        this(FailSafeHttpClient.create(httpClient, RETRY_POLICY), baseUrl, TimingLogLevel.INFO);
    }

    /**
     * Returns new instance with default retry policy
     *
     * @param httpClient web resources client
     * @param baseUrl    base URL for txt2metadata api endpoint
     * @param level      log level
     */
    public Txt2MetadataConnector(Client httpClient, String baseUrl, TimingLogLevel level) {
        this(FailSafeHttpClient.create(httpClient, RETRY_POLICY), baseUrl, level);
    }

    /**
     * Returns new instance with custom retry policy
     *
     * @param failSafeHttpClient web resources client with custom retry policy
     * @param baseUrl            base URL for txt2metadata api endpoint
     */
    public Txt2MetadataConnector(FailSafeHttpClient failSafeHttpClient, String baseUrl) {
        this(failSafeHttpClient, baseUrl, TimingLogLevel.INFO);
    }

    /**
     * Returns new instance with custom retry policy
     *
     * @param failSafeHttpClient web resources client with custom retry policy
     * @param baseUrl            base URL for txt2metadata api endpoint
     * @param level              log level
     */
    public Txt2MetadataConnector(FailSafeHttpClient failSafeHttpClient, String baseUrl, TimingLogLevel level) {
        this.failSafeHttpClient = InvariantUtil.checkNotNullOrThrow(failSafeHttpClient, "failSafeHttpClient");
        this.baseUrl = InvariantUtil.checkNotNullNotEmptyOrThrow(baseUrl, "baseUrl");
        switch (level) {
            case TRACE:
                logger = LOGGER::trace;
                break;
            case DEBUG:
                logger = LOGGER::debug;
                break;
            case INFO:
                logger = LOGGER::info;
                break;
            case WARN:
                logger = LOGGER::warn;
                break;
            case ERROR:
                logger = LOGGER::error;
                break;
            default:
                logger = LOGGER::info;
                break;
        }
    }

    public void close() {
        failSafeHttpClient.getClient().close();
    }

    public List<Txt2MetaData> getMetaDataForArticle(String articleId) throws Txt2MetaDataConnectorException {
        return getMetaDataForArticle(articleId, DEFAULT_MATCHES);
    }

    public List<Txt2MetaData> getMetaDataForArticle(String articleId, int matches) throws Txt2MetaDataConnectorException {
        return getRequest(PATH_SUGGESTIONS_FOR_ARTICLE, articleId, matches);
    }


    public List<Txt2MetaData> getMetaDataForArticles(List<String> articleIds) throws Txt2MetaDataConnectorException {
        return getMetaDataForArticles(articleIds, DEFAULT_MATCHES);
    }

    public List<Txt2MetaData> getMetaDataForArticles(List<String> articleIds, int matches) throws Txt2MetaDataConnectorException {
        final String body = "\"" + String.join("\",\"", articleIds) + "\"";

        return postRequest(PATH_SUGGESTIONS_FOR_ARTICLES, body, matches);
    }

    public List<Txt2MetaData> getMetaDataForText(String text) throws Txt2MetaDataConnectorException {
        return getMetaDataForText(text, DEFAULT_MATCHES);
    }

    public List<Txt2MetaData> getMetaDataForText(String text, int matches) throws Txt2MetaDataConnectorException {
        return postRequest(PATH_SUGGESTIONS_ON_TEXT, text, matches);
    }

    private List<Txt2MetaData> getRequest(String basePath, String articleId, int matches)
            throws Txt2MetaDataConnectorException {
        InvariantUtil.checkNotNullNotEmptyOrThrow(articleId, "articleId");
        final PathBuilder path = new PathBuilder(basePath)
                .bind(PATH_VARIABLE_ARTICLE_ID, articleId);
        LOGGER.info("GET " + baseUrl + String.join("/", path.build()));
        final Stopwatch stopwatch = new Stopwatch();

        try {
            final HttpGet httpGet = new HttpGet(failSafeHttpClient)
                    .withBaseUrl(baseUrl)
                    .withPathElements(path.build())
                    .withHeader("Accept", "application/json")
                    .withQueryParameter("matches", matches);
            final Response response = httpGet.execute();

            assertResponseStatus(response, Response.Status.OK);

            return readResponseEntity(response, new GenericType<List<Txt2MetaData>>() {
            });
        } finally {
            logger.log("GET {} took {} milliseconds", String.join("/", path.build()),
                    stopwatch.getElapsedTime(TimeUnit.MILLISECONDS));
        }
    }

    private List<Txt2MetaData> postRequest(String basePath, String data, int matches) throws Txt2MetaDataConnectorException {
        final Stopwatch stopwatch = new Stopwatch();

        final PathBuilder path = new PathBuilder(basePath);
        LOGGER.info("POST {} with data {}", String.join("/", path.build()), data);
        try {
            final HttpPost httpPost = new HttpPost(failSafeHttpClient)
                    .withBaseUrl(baseUrl)
                    .withPathElements(path.build())
                    .withData(data, "text/plain")
                    .withHeader("Accept", "application/json")
                    .withQueryParameter("matches", matches);
            final Response response = httpPost.execute();
            assertResponseStatus(response, Response.Status.OK);
            return readResponseEntity(response, new GenericType<List<Txt2MetaData>>() {
            });
        } finally {
            logger.log("POST {} took {} milliseconds", String.join("/", path.build()),
                    stopwatch.getElapsedTime(TimeUnit.MILLISECONDS));
        }
    }

    private <T> T readResponseEntity(Response response, GenericType<T> genericType) throws Txt2MetaDataConnectorException {
        response.bufferEntity();
        final T entity = response.readEntity(genericType);
        if (entity == null) {
            throw new Txt2MetaDataConnectorException(
                    String.format("job-store service returned with null-valued %s entity", genericType.getRawType().getName()));
        }
        return entity;
    }

    private void assertResponseStatus(Response response, Response.Status expectedStatus)
            throws Txt2MetaDataConnectorUnexpectedStatusCodeException {
        final Response.Status actualStatus =
                Response.Status.fromStatusCode(response.getStatus());
        LOGGER.info("Status code: {}", response.getStatus());
        if (actualStatus != expectedStatus) {
            throw new Txt2MetaDataConnectorUnexpectedStatusCodeException(
                    String.format("txt2metadata service returned with unexpected status code: %s",
                            actualStatus),
                    actualStatus.getStatusCode());
        }
    }

    @FunctionalInterface
    interface LogLevelMethod {
        void log(String format, Object... objs);
    }
}
