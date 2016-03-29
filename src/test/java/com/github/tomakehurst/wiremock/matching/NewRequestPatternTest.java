package com.github.tomakehurst.wiremock.matching;

import com.github.tomakehurst.wiremock.common.Json;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import static com.github.tomakehurst.wiremock.http.RequestMethod.GET;
import static com.github.tomakehurst.wiremock.http.RequestMethod.PUT;
import static com.github.tomakehurst.wiremock.matching.MockRequest.mockRequest;
import static com.github.tomakehurst.wiremock.matching.StringValuePattern.equalTo;
import static com.github.tomakehurst.wiremock.matching.StringValuePattern.matches;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class NewRequestPatternTest {

    @Test
    public void matchesExactlyWith0DistanceWhenUrlAndMethodAreExactMatch() {
        NewRequestPattern requestPattern = NewRequestPatternBuilder
            .newRequestPattern(PUT, UrlPathPattern.equalTo("/my/url"))
            .build();

        MatchResult matchResult = requestPattern.match(mockRequest().method(PUT).url("/my/url"));
        assertThat(matchResult.getDistance(), is(0.0));
        assertTrue(matchResult.isExactMatch());
    }

    @Test
    public void returnsNon0DistanceWhenUrlDoesNotMatch() {
        NewRequestPattern requestPattern = NewRequestPatternBuilder
            .newRequestPattern(PUT, UrlPathPattern.equalTo("/my/url"))
            .withUrl("/my/url")
            .build();

        MatchResult matchResult = requestPattern.match(mockRequest().url("/totally/other/url"));
        assertThat(matchResult.getDistance(), greaterThan(0.0));
        assertFalse(matchResult.isExactMatch());
    }

    @Test
    public void matchesExactlyWith0DistanceWhenAllRequiredHeadersMatch() {
        NewRequestPattern requestPattern = NewRequestPatternBuilder
            .newRequestPattern(PUT, UrlPathPattern.equalTo("/my/url"))
            .withHeader("My-Header", MultiValuePattern.of(equalTo("my-expected-header-val")))
            .build();

        MatchResult matchResult = requestPattern.match(mockRequest()
            .method(PUT)
            .header("My-Header", "my-expected-header-val")
            .url("/my/url"));
        assertThat(matchResult.getDistance(), is(0.0));
        assertTrue(matchResult.isExactMatch());
    }

    @Test
    public void doesNotMatchWhenHeaderDoesNotMatch() {
        NewRequestPattern requestPattern = NewRequestPatternBuilder
            .newRequestPattern(GET, UrlPathPattern.equalTo("/my/url"))
            .withHeader("My-Header", MultiValuePattern.of(equalTo("my-expected-header-val")))
            .withHeader("My-Other-Header", MultiValuePattern.of(equalTo("my-other-expected-header-val")))
            .build();

        MatchResult matchResult = requestPattern.match(mockRequest()
            .method(GET)
            .header("My-Header", "my-expected-header-val")
            .header("My-Other-Header", "wrong")
            .url("/my/url"));

        assertFalse(matchResult.isExactMatch());
    }

    @Test
    public void bindsToJsonCompatibleWithOriginalRequestPatternForUrlPathAndHeaders() throws Exception {
        NewRequestPattern requestPattern = NewRequestPatternBuilder
            .newRequestPattern(GET, UrlPathPattern.equalTo("/my/url"))
            .withHeader("Accept", MultiValuePattern.of(matches("(.*)xml(.*)")))
            .withHeader("If-None-Match", MultiValuePattern.of(matches("([a-z0-9]*)")))
            .build();

        String actualJson = Json.write(requestPattern);

        JSONAssert.assertEquals(
            URL_PATH_EXAMPLE,
            actualJson,
            true);
    }

    @Test
    public void bindsToJsonCompatibleWithOriginalRequestPatternForUrl() throws Exception {
        NewRequestPattern requestPattern = NewRequestPatternBuilder
            .newRequestPattern(GET, UrlPattern.equalTo("/my/url"))
            .build();

        String actualJson = Json.write(requestPattern);

        JSONAssert.assertEquals(
            "{									                \n" +
                "		\"method\": \"GET\",						\n" +
                "		\"url\": \"/my/url\"                		\n" +
                "}												    ",
            actualJson,
            true);
    }

    @Test
    public void bindsToJsonCompatibleWithOriginalRequestPatternForUrlPattern() throws Exception {
        NewRequestPattern requestPattern = NewRequestPatternBuilder
            .newRequestPattern(GET, UrlPattern.matching("/my/url"))
            .build();

        String actualJson = Json.write(requestPattern);

        JSONAssert.assertEquals(
            "{									                \n" +
            "		\"method\": \"GET\",						\n" +
            "		\"urlPattern\": \"/my/url\"           		\n" +
            "}												    ",
            actualJson,
            true);
    }

    @Test
    public void bindsToJsonCompatibleWithOriginalRequestPatternForUrlPathPattern() throws Exception {
        NewRequestPattern requestPattern = NewRequestPatternBuilder
            .newRequestPattern(GET, UrlPathPattern.matching("/my/url"))
            .build();

        String actualJson = Json.write(requestPattern);

        JSONAssert.assertEquals(
            "{									                \n" +
            "		\"method\": \"GET\",						\n" +
            "		\"urlPathPattern\": \"/my/url\"             \n" +
            "}												    ",
            actualJson,
            true);
    }

    static final String URL_PATH_EXAMPLE =
        "{									                \n" +
            "		\"method\": \"GET\",						\n" +
            "		\"urlPath\": \"/my/url\",             		\n" +
            "		\"headers\": {								\n" +
            "			\"Accept\": {							\n" +
            "				\"matches\": \"(.*)xml(.*)\"		\n" +
            "			},										\n" +
            "			\"If-None-Match\": {					\n" +
            "				\"matches\": \"([a-z0-9]*)\"		\n" +
            "			}										\n" +
            "		}											\n" +
            "}												    ";
}