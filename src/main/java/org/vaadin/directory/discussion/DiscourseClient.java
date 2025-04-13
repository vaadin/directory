package org.vaadin.directory.discussion;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Base64;

import org.springframework.stereotype.Service;

@Service
public class DiscourseClient {
    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String apiUsername;
    private final String apiKey;
    private final String basicAuthUsername;
    private final String basicAuthPassword;
    private final ObjectMapper objectMapper;
    private final boolean useBasicAuth;

    public DiscourseClient(
            RestTemplateBuilder restTemplateBuilder,
            @Value("${discourse.base-url}") String baseUrl,
            @Value("${discourse.api-username}") String apiUsername,
            @Value("${discourse.api-key}") String apiKey,
            @Value("${discourse.basic-auth.username:#{null}}") String basicAuthUsername,
            @Value("${discourse.basic-auth.password:#{null}}") String basicAuthPassword
    ) {
        this.baseUrl = baseUrl.replaceFirst("/$", "");
        this.apiUsername = apiUsername;
        this.apiKey = apiKey;
        this.basicAuthUsername = basicAuthUsername;
        this.basicAuthPassword = basicAuthPassword;
        this.useBasicAuth = basicAuthUsername != null && !basicAuthUsername.isEmpty() 
                          && basicAuthPassword != null && !basicAuthPassword.isEmpty();

        // Configure RestTemplate with headers and timeout
        this.restTemplate = restTemplateBuilder
                .interceptors((request, body, execution) -> {
                    // Add Discourse API headers
                    request.getHeaders().set("Api-Username", this.apiUsername);
                    request.getHeaders().set("Api-Key", this.apiKey);
                    
                    // Add HTTP Basic Auth if configured
                    if (useBasicAuth) {
                        String auth = this.basicAuthUsername + ":" + this.basicAuthPassword;
                        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
                        request.getHeaders().set(HttpHeaders.AUTHORIZATION, "Basic " + encodedAuth);
                    }
                    
                    return execution.execute(request, body);
                })
                .build();

        // Initialize Jackson ObjectMapper
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Fetch topics from a specific category with custom parameters
     *
     * @param categoryId ID of the category
     * @param maxTopics How many
     * @return List of topics in the category
     */
    public List<Topic> listTopicsInCategory(int categoryId, int maxTopics) {

        // Build URI with query parameters
        URI uri = UriComponentsBuilder
                .fromUriString(baseUrl + "/c/" + categoryId + ".json")
                .build()
                .toUri();

        try {
            // Fetch and parse response
            ResponseEntity<String> response = restTemplate.exchange(
                    uri,
                    HttpMethod.GET,
                    null,
                    String.class
            );

            // Parse JSON response
            JsonNode jsonResponse = objectMapper.readTree(response.getBody());
            JsonNode topicsNode = jsonResponse.path("topic_list").path("topics");

            // Convert to list of Topics
            List<Topic> list = objectMapper.readerForListOf(Topic.class)
                    .readValue(topicsNode);
            // Always skip the first one since it is category description
            if (!list.isEmpty()) {
                list.removeFirst();
            }

            // Sort by Topic.created_at latest first
            list.sort(Comparator.comparing(t -> ((Topic)t).createdAt).reversed());
            return maxTopics > 0 && list.size() > maxTopics ? list.subList(0,maxTopics-1) :list;

        } catch (Exception e) {
            throw new RuntimeException("Error fetching topics: " + e.getMessage(), e);
        }
    }

    /**
     * Fetch posts for a specific topic
     *
     * @param topicId ID of the topic
     * @return Topic details with posts
     */
    public List<TopicPostsResponse.Post> listPostsInTopic(String topicId) {
        URI uri = URI.create(baseUrl + "/t/" + topicId + ".json");

        try {
            // Fetch and parse response
            ResponseEntity<TopicPostsResponse> response = restTemplate.exchange(
                    uri,
                    HttpMethod.GET,
                    null,
                    TopicPostsResponse.class
            );

            // Parse JSON response
            return Objects.requireNonNull(response.getBody()).postStream.posts;


        } catch (Exception e) {
            throw new RuntimeException("Error fetching topic posts: " + e.getMessage(), e);
        }
    }

    /**
     * Creates a new subcategory under the specified parent category
     *
     * @param name The name of the new subcategory
     * @param description The description of the new subcategory
     * @param parentCategoryId The ID of the parent category
     * @return The newly created subcategory
     */
    public Category createSubcategory(String name, String description, int parentCategoryId) {
        URI uri = URI.create(baseUrl + "/categories.json");
        
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("name", name);
        formData.add("color", "0088CC"); // Default color
        formData.add("text_color", "FFFFFF"); // Default text color
        formData.add("description", description);
        formData.add("parent_category_id", String.valueOf(parentCategoryId));
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Api-Username", this.apiUsername);
        headers.set("Api-Key", this.apiKey);
        
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(formData, headers);
        
        try {
            ResponseEntity<CategoryCreateResponse> response = restTemplate.exchange(
                    uri,
                    HttpMethod.POST,
                    requestEntity,
                    CategoryCreateResponse.class
            );
            
            return Objects.requireNonNull(response.getBody()).category;
        } catch (Exception e) {
            throw new RuntimeException("Error creating subcategory: " + e.getMessage(), e);
        }
    }
    
    /**
     * Posts an initial message in a category to start a discussion
     *
     * @param categoryId The ID of the category to post in
     * @param title The title of the new topic
     * @param content The content of the first post
     * @return The created topic with its first post
     */
    public TopicPostsResponse createInitialPost(int categoryId, String title, String content) {
        URI uri = URI.create(baseUrl + "/posts.json");
        
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("title", title);
        formData.add("raw", content);
        formData.add("category", String.valueOf(categoryId));
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Api-Username", this.apiUsername);
        headers.set("Api-Key", this.apiKey);
        
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(formData, headers);
        
        try {
            // First create the topic with initial post
            ResponseEntity<PostCreateResponse> createResponse = restTemplate.exchange(
                    uri,
                    HttpMethod.POST,
                    requestEntity,
                    PostCreateResponse.class
            );

            PostCreateResponse body = createResponse.getBody();
            if (body == null || body.topicId <= 0) {
                throw new RuntimeException("Failed to create topic");
            }
            
            // Then fetch the created topic to return complete information
            String topicSlug = body.topicSlug + "/" + body.topicId;
            return fetchTopic(topicSlug);
        } catch (Exception e) {
            throw new RuntimeException("Error creating initial post: " + e.getMessage(), e);
        }
    }
    
    /**
     * A convenience method that creates a subcategory and posts an initial message
     *
     * @param subcategoryName The name of the new subcategory
     * @param subcategoryDescription The description of the new subcategory
     * @param parentCategoryId The ID of the parent category
     * @param topicTitle The title of the initial topic
     * @param topicContent The content of the initial post
     * @return The created topic with its first post
     */
    public TopicPostsResponse createSubcategoryWithInitialPost(
            String subcategoryName, 
            String subcategoryDescription, 
            int parentCategoryId, 
            String topicTitle, 
            String topicContent) {
        
        // First create the subcategory
        Category newCategory = createSubcategory(subcategoryName, subcategoryDescription, parentCategoryId);
        
        // Then create the initial post in that subcategory
        return createInitialPost(newCategory.id, topicTitle, topicContent);
    }
    
    /**
     * Fetches a complete topic by its slug/id
     *
     * @param topicId The topic slug/id (e.g., "topic-slug/123")
     * @return The complete topic with posts
     */
    public TopicPostsResponse fetchTopic(String topicId) {
        URI uri = URI.create(baseUrl + "/t/" + topicId + ".json");
        
        try {
            ResponseEntity<TopicPostsResponse> response = restTemplate.exchange(
                    uri,
                    HttpMethod.GET,
                    null,
                    TopicPostsResponse.class
            );
            
            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("Error fetching topic: " + e.getMessage(), e);
        }
    }
    
    /**
     * Fetches all categories from Discourse
     *
     * @return List of all categories
     */
    public List<Category> listCategories() {
        URI uri = URI.create(baseUrl + "/categories.json");

        try {
            // Fetch and parse response
            ResponseEntity<CategoryListResponse> response = restTemplate.exchange(
                    uri,
                    HttpMethod.GET,
                    null,
                    CategoryListResponse.class
            );

            // Return categories
            CategoryListResponse body = response.getBody();
            if (body != null && body.categoryList != null) {
                return body.categoryList.categories;
            }
            return Collections.emptyList();
        } catch (Exception e) {
            throw new RuntimeException("Error fetching categories: " + e.getMessage(), e);
        }
    }

    /**
     * Fetches subcategories for a specific category
     *
     * @param categoryId ID of the category
     * @return List of subcategories in the category
     */
    public List<CategoryInfo> listSubCategoriesInCategory(int categoryId) {
        Category c = listCategories().stream().filter(cat -> cat.id == categoryId).findFirst().orElse(null);
        if (c != null) {
            
            // Fetch details for each subcategory by ID
            List<CategoryInfo> subcategories = new ArrayList<>();
            for (Integer subcategoryId : c.subcategoryIds) {
                URI subcategoryUri = URI.create(baseUrl + "/c/" + subcategoryId + ".json");
                
                ResponseEntity<CategoryResponse> subcategoryResponse = restTemplate.exchange(
                        subcategoryUri,
                        HttpMethod.GET,
                        null,
                        CategoryResponse.class
                );
                
                // Parse subcategory details
                CategoryResponse subCategory = subcategoryResponse.getBody();                
                if (subCategory != null) {
                    subcategories.add(subCategory.getCategoryTopic(categoryId));
                }
            }
            
            return subcategories;
        }

        return Collections.emptyList();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CategoryListResponse {
        @JsonProperty("category_list")
        public CategoryList categoryList;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CategoryList {
        @JsonProperty("can_create_category")
        public boolean canCreateCategory;
        @JsonProperty("can_create_topic")
        public boolean canCreateTopic;
        public List<Category> categories;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CategoryResponse {
        public List<User> users;
        @JsonProperty("primary_groups")
        public List<Object> primaryGroups;
        @JsonProperty("flair_groups")
        public List<Object> flairGroups;
        @JsonProperty("topic_list")
        public TopicList topicList;

        CategoryInfo getCategoryTopic(int parentId) {
            int topicCount = topicList.topics.size();
            int postCount = topicList.topics.stream().map(t -> t.postsCount).reduce(0, Integer::sum);
            return toCategoryInfoTopic(topicList.topics.getFirst(), topicCount, postCount, parentId);
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class TopicList {
            @JsonProperty("can_create_topic")
            public boolean canCreateTopic;
            @JsonProperty("more_topics_url")
            public String moreTopicsUrl;
            @JsonProperty("per_page")
            public int perPage;
            @JsonProperty("top_tags")
            public List<String> topTags;
            public List<Topic> topics;
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class User {
            public int id;
            public String username;
            public String name;
            @JsonProperty("avatar_template")
            public String avatarTemplate;
            public boolean admin;
            public boolean moderator;
            @JsonProperty("trust_level")
            public int trustLevel;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CategoryCreateResponse {
        public Category category;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Category {
        public int id;
        public String name;
        public String color;
        @JsonProperty("text_color")
        public String textColor;
        public String slug;
        @JsonProperty("topic_count")
        public int topicCount;
        @JsonProperty("post_count")
        public int postCount;
        public String description;
        @JsonProperty("description_text")
        public String descriptionText;
        @JsonProperty("parent_category_id")
        public int parentCategoryId;
        @JsonProperty("has_children")
        public boolean hasChildren;
        @JsonProperty("subcategory_ids")
        public List<Integer> subcategoryIds;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Topic {
        public int id;
        public String title;
        @JsonProperty("fancy_title")
        public String fancyTitle;
        public String slug;
        @JsonProperty("posts_count")
        public int postsCount;
        @JsonProperty("reply_count")
        public int replyCount;
        @JsonProperty("highest_post_number")
        public int highestPostNumber;
        @JsonProperty("image_url")
        public String imageUrl;
        @JsonProperty("created_at")
        public String createdAt;
        @JsonProperty("last_posted_at")
        public String lastPostedAt;
        public boolean bumped;
        @JsonProperty("bumped_at")
        public String bumpedAt;
        public String archetype;
        public boolean unseen;
        public boolean pinned;
        public String unpinned;
        public String excerpt;
        public boolean visible;
        public boolean closed;
        public boolean archived;
        public String bookmarked;
        public String liked;
        public int views;
        @JsonProperty("like_count")
        public int likeCount;
        @JsonProperty("has_summary")
        public boolean hasSummary;
        @JsonProperty("last_poster_username")
        public String lastPosterUsername;
        @JsonProperty("category_id")
        public int categoryId;
        @JsonProperty("pinned_globally")
        public boolean pinnedGlobally;
        @JsonProperty("featured_link")
        public String featuredLink;
        public List<Poster> posters;
    }

    public static class CategoryInfo {
        public int id;
        public String name;
        public String description;
        public String slug;
        public int topicCount;
        public int postCount;
        public int parentCategoryId;
    }

    // Method for converting a Topic to CategoryInfoTopic
    public static CategoryInfo toCategoryInfoTopic(Topic topic, int topicCount, int postCount, int parentCategoryId) {
        CategoryInfo categoryInfoTopic = new CategoryInfo();
        categoryInfoTopic.id = topic.categoryId;
        categoryInfoTopic.name = topic.title;
        categoryInfoTopic.description = topic.excerpt;
        categoryInfoTopic.slug = topic.slug;
        categoryInfoTopic.topicCount = topicCount;
        categoryInfoTopic.postCount = postCount;
        categoryInfoTopic.parentCategoryId = parentCategoryId;

        return categoryInfoTopic;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Poster {
        public String extras;
        public String description;
        @JsonProperty("user_id")
        public int userId;
        @JsonProperty("primary_group_id")
        public int primaryGroupId;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TopicPostsResponse {
        @JsonProperty("post_stream")
        public PostStream postStream;

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class PostStream {
            public List<Post> posts;
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Post {
            public int id;
            public String name;
            public String username;
            @JsonProperty("avatar_template")
            public String avatarTemplate;
            @JsonProperty("created_at")
            public String createdAt;
            public String cooked;
            @JsonProperty("post_number")
            public int postNumber;
            @JsonProperty("post_type")
            public int postType;
            @JsonProperty("updated_at")
            public String updatedAt;
            @JsonProperty("reply_count")
            public int replyCount;
            @JsonProperty("reply_to_post_number")
            public String replyToPostNumber;
            @JsonProperty("quote_count")
            public int quoteCount;
            @JsonProperty("incoming_link_count")
            public int incomingLinkCount;
            public int reads;
            @JsonProperty("readers_count")
            public int readersCount;
            public double score;
            public boolean yours;
            @JsonProperty("topic_id")
            public int topicId;
            @JsonProperty("topic_slug")
            public String topicSlug;
            @JsonProperty("display_username")
            public String displayUsername;
            @JsonProperty("primary_group_name")
            public String primaryGroupName;
            @JsonProperty("flair_name")
            public String flairName;
            @JsonProperty("flair_url")
            public String flairUrl;
            @JsonProperty("flair_bg_color")
            public String flairBgColor;
            @JsonProperty("flair_color")
            public String flairColor;
            public int version;
            @JsonProperty("can_edit")
            public boolean canEdit;
            @JsonProperty("can_delete")
            public boolean canDelete;
            @JsonProperty("can_recover")
            public boolean canRecover;
            @JsonProperty("can_see_hidden_post")
            public boolean canSeeHiddenPost;
            @JsonProperty("can_wiki")
            public boolean canWiki;
            @JsonProperty("link_counts")
            public List<LinkCount> linkCounts;
            public boolean read;
            @JsonProperty("user_title")
            public String userTitle;
            public boolean bookmarked;
            @JsonProperty("actions_summary")
            public List<ActionSummary> actionsSummary;
            public boolean moderator;
            public boolean admin;
            public boolean staff;
            @JsonProperty("user_id")
            public int userId;
            public boolean hidden;
            @JsonProperty("trust_level")
            public int trustLevel;
            @JsonProperty("deleted_at")
            public String deletedAt;
            @JsonProperty("user_deleted")
            public boolean userDeleted;
            @JsonProperty("edit_reason")
            public String editReason;
            @JsonProperty("can_view_edit_history")
            public boolean canViewEditHistory;
            public boolean wiki;
            @JsonProperty("reviewable_id")
            public int reviewableId;
            @JsonProperty("reviewable_score_count")
            public int reviewableScoreCount;
            @JsonProperty("reviewable_score_pending_count")
            public int reviewableScorePendingCount;
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class LinkCount {
            public String url;
            public boolean internal;
            public boolean reflection;
            public String title;
            public int clicks;
        }


        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class ActionSummary {
            public int id;
            @JsonProperty("can_act")
            public boolean canAct;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PostCreateResponse {
        public int id;
        public String name;
        public String username;
        @JsonProperty("avatar_template")
        public String avatarTemplate;
        @JsonProperty("created_at")
        public String createdAt;
        public String cooked;
        @JsonProperty("post_number")
        public int postNumber;
        @JsonProperty("post_type")
        public int postType;
        @JsonProperty("updated_at")
        public String updatedAt;
        @JsonProperty("reply_count")
        public int replyCount;
        @JsonProperty("topic_id")
        public int topicId;
        @JsonProperty("topic_slug")
        public String topicSlug;
    }

}