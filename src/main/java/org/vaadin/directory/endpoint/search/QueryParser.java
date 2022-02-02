package org.vaadin.directory.endpoint.search;

import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.vaadin.directory.entity.directory.Framework.*;

/**
 * Parser for search strings.
 *
 * Using Google Drive format as reference:
 * <code>https://drive.google.com/drive/search?q=type:pdf%20source:domain%20owner:sami%40vaadin.com</code>
 *
 */
public class QueryParser {

    public static final String AUTHOR_SELF_TOKEN = "me";

    private List<String> keywords = List.of();
    private List<String> tagGroups = List.of();
    private Framework framework = null;
    private String frameworkVersion = null;
    private String author = null;
    private boolean isAuthorMe = false;


    public boolean isAuthorMe() {
        return isAuthorMe;
    }

    public List<String> getTagGroups() { return tagGroups; }

    public String getAuthor() { return author; }

    public List<String> getKeywords() { return keywords; }

    public Framework getFramework() { return framework; }

    public String getFrameworkVersion() { return frameworkVersion; }

    // Vaadin 10+ needs special handling (from 10 to 24)
    private static final List<String> vaadin10plusVersions;
    static {
        vaadin10plusVersions =
                IntStream.range(10,24)
                        .mapToObj(i -> Integer.toString(i))
                        .collect(Collectors.toList());
    }
    /**
     * Parses the search string and updates the search field state.
     *
     * @param searchString full search string to be parsed
     */
    public void updateFromSearch(String searchString) {

        if (searchString == null || searchString.length() == 0) {
            return;
        }

        String[] words = searchString.split(" ");
        if (words == null || words.length == 0) {
            return;
        }

        // Collect all non-tokens
        List<String> wordList = Arrays.asList(words);
        List<String> keywordParams = wordList.stream()
                .filter(s -> !s.contains(":"))
                .collect(Collectors.toList());
        keywords = keywordParams != null && keywordParams.size() > 0 ? keywordParams : List.of();

        // Filter all token words
        Map<String, List<String>> searchTokens = wordList.stream()
                .filter(s -> s.contains(":"))
                .collect(Collectors.toMap(
                s -> s.split(":")[0],
                s -> Arrays.stream(getStringAfterColon(s).split(",")).collect(Collectors.toList()),
                (tagValues1,tagValues2) -> Stream.of(tagValues1, tagValues2)
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList())
                ));

        List<String> tagGroupParams = searchTokens.get(Token.TAG.getToken());
        tagGroups =
                tagGroupParams != null && tagGroupParams.size() > 0 ? tagGroupParams : List.of();

        List<String> authorParams = searchTokens.get(Token.OWNER.getToken());
        authorParams =
                authorParams != null ? authorParams : searchTokens.get(Token.USER.getToken());
        authorParams =
                authorParams != null ? authorParams : searchTokens.get(Token.AUTHOR.getToken());
        author = authorParams != null && authorParams.size() >= 1 ? authorParams.get(0) : null;
        author = author.replace('_',' '); // Use underscore as space
        isAuthorMe = AUTHOR_SELF_TOKEN.equalsIgnoreCase(author);

        List<String> frameworkParams =
          searchTokens.get(Token.FRAMEWORK.getToken());
        if (frameworkParams != null && frameworkParams.size() >= 1) {
            framework = new Framework(frameworkParams.get(0).replace('_', ' '), "none");
        }


        List<String> frameworkVersionParams =
                searchTokens.get(Token.FRAMEWORK_VERSION.getToken());
        this.frameworkVersion = frameworkVersionParams != null && frameworkVersionParams.size() >= 1 ?
                frameworkVersionParams.get(0) : null;

        if (this.frameworkVersion != null && framework == null) {
            // By default we use Vaadin as framework if omitted. V10+ needs special handling.
            framework = vaadin10plusVersions.stream().anyMatch(s -> this.frameworkVersion.startsWith(s)) ? new Framework(VAADIN_10) : null;
            framework = this.frameworkVersion.startsWith("8") ? new Framework(VAADIN_8) : framework;
            framework = this.frameworkVersion.startsWith("7") ? new Framework(VAADIN_7) : framework;
            framework = this.frameworkVersion.startsWith("6") ? new Framework(VAADIN_6) : framework;

            // If only major version was searched
            if (framework != null
                    && this.frameworkVersion != null
                    && !framework.equals(VAADIN_10)
                    && !this.frameworkVersion.contains(".")) {
                this.frameworkVersion = null;
            }
        };

    }

    private String getStringAfterColon(String s) {
        String[] sa = s.split(":");
        return sa.length > 1 ? sa[1] : "";
    }

    public static QueryParser parse(String searchString) {
        QueryParser qp = new QueryParser();
        qp.updateFromSearch(searchString);
        return qp;
    }

    /**
     * Enumeration of tokens supported by query parameters
     */
    public enum Token {
        FRAMEWORK("fw"), FRAMEWORK_VERSION("v"), AUTHOR("author"), USER(
                "user"), OWNER("owner"), TAG("tag");

        private String token;

        /**
         * Constructs the token enum object.
         *
         * @param token the token name.
         */
        Token(String token) {
            this.token = token;
        }

        /**
         * Retrieves the {@link Token} enumeration for the specified token string.
         *
         * @param token the token string.
         * @return the resolved {@link Token}, or {@link Optional#empty()} if not found.
         */
        public static Optional<Token> fromString(String token) {
            for (Token tokenEnum : Token.values()) {
                if (tokenEnum.token.equalsIgnoreCase(token)) {
                    return Optional.of(tokenEnum);
                }
            }
            return Optional.empty();
        }

        public String getToken() {
            return token;
        }
    }

    public static class Framework {

        private com.vaadin.directory.entity.directory.Framework framework = null;
        private String defaultName = null;

        private Framework(com.vaadin.directory.entity.directory.Framework framework) {
            this.framework = framework;
        }

        private Framework(String name, String defaultName) {
            Optional<com.vaadin.directory.entity.directory.Framework> fw = fromString(name);
            if (fw.isPresent()) this.framework = fw.get();
            else this.defaultName = defaultName;
        }

        public String getName() {
            return framework != null ? framework.getName() : this.defaultName;
        }
    }
}

