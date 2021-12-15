package org.vaadin.directory.endpoint.search;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import com.vaadin.directory.entity.directory.ComponentFramework;
import com.vaadin.directory.entity.directory.ComponentFrameworkVersion;

/**
 * Parser for search strings.
 *
 * Using Google Drive format as reference:
 * <code>https://drive.google.com/drive/search?q=type:pdf%20source:domain%20owner:sami%40vaadin.com</code>
 *
 */
public class QueryParser {

    public final static String FRAMEWORK_FROM_VERSION_SEPARATOR = "_";
    public static final String AUTHOR_SELF_TOKEN = "me";

    private Optional<List<String>> tagGroups = Optional.empty();
    private Optional<String> author = Optional.empty();
    private Optional<List<String>> keywords = Optional.empty();
    private ComponentFramework framework;
    private Set<ComponentFrameworkVersion> frameworkVersions = Collections.emptySet();
    private boolean isAuthorMe = false;


    public boolean isAuthorMe() {
        return isAuthorMe;
    }

    public Optional<List<String>> getTagGroups() {
        return tagGroups;
    }

    public Optional<String> getAuthor() {
        return author;
    }

    public Optional<List<String>> getKeywords() {
        return keywords;
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
        List<String> keywordParams = Arrays.asList(words).stream().filter(s -> !s.contains(":"))
                .collect(Collectors.toList());
        keywords = keywordParams != null && keywordParams.size() > 0 ? Optional.of(keywordParams)
                : Optional.empty();

        // Filter all token words
        List<String> tokenWords = Arrays.asList(words).stream().filter(s -> s.contains(":"))
                .collect(Collectors.toList());
        Map<String, List<String>> searchTokens = tokenWords.stream().collect(Collectors.toMap(
                s -> s.split(":")[0],
                s -> Arrays.stream(s.split(":")[1].split(",")).collect(Collectors.toList())));

        List<String> tagGroupParams = searchTokens.get(Token.TAG.getToken());
        tagGroups =
                tagGroupParams != null && tagGroupParams.size() > 0 ? Optional.of(tagGroupParams)
                        : Optional.empty();

        List<String> authorParams = searchTokens.get(Token.OWNER.getToken());
        authorParams =
                authorParams != null ? authorParams : searchTokens.get(Token.USER.getToken());
        authorParams =
                authorParams != null ? authorParams : searchTokens.get(Token.AUTHOR.getToken());
        author = authorParams != null && authorParams.size() == 1 ? Optional.of(authorParams.get(0))
                : Optional.empty();
        author.ifPresent(authorName -> isAuthorMe = authorName.equalsIgnoreCase(AUTHOR_SELF_TOKEN));

        /**
         * TODO: Disabled framework parsing for now. Re-enable if needed by new search
         * functionality. List<String> keywordFramework =
         * searchTokens.get(Token.FRAMEWORK.getToken()); framework = keywordFramework != null &&
         * keywordFramework.size() == 1 ?
         * componentFrameworkRepository.findByName(keywordFramework.get(0)) : null;
         * 
         * frameworkVersions = new LinkedHashSet<>(); List<String> frameworkVersionNames =
         * searchTokens.get(Token.FRAMEWORK_VERSION.getToken());
         * 
         * if (frameworkVersionNames != null) { for (String frameworkVersion :
         * frameworkVersionNames) { String[] split =
         * frameworkVersion.split(FRAMEWORK_FROM_VERSION_SEPARATOR); if (split.length == 2) { String
         * frameworkName = split[0]; String versionName = split[1];
         * 
         * ComponentFramework framework = componentFrameworkRepository.findByName(frameworkName); if
         * (framework != null) { ComponentFrameworkVersion version =
         * componentFrameworkVersionRepository.findByFrameworkAndVersion(framework, versionName); if
         * (version != null) { frameworkVersions.add(version); } } } } }
         */
    }

    private static QueryParser parse(String searchString) {
        QueryParser qp = new QueryParser();
        qp.updateFromSearch(searchString);
        return qp;
    }

    /**
     * Enumeration of tokens supported by query parameters
     */
    public enum Token {
        FRAMEWORK("framework"), FRAMEWORK_VERSION("framework_version"), AUTHOR("author"), USER(
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
}

