package org.vaadin.directory.discussion;

import com.github.javaparser.utils.SourceRoot;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.hilla.Endpoint;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.vaadin.directory.discussion.DiscourseClient.CategoryInfo;
import org.vaadin.directory.discussion.DiscourseClient.Topic;
import org.vaadin.directory.discussion.DiscourseClient.TopicPostsResponse;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Endpoint
@AnonymousAllowed
public class DiscussionEndpoint {

    private final DiscourseClient discourseClient;
    private final AddonInfoService addonInfoService;
    private final String discourseBaseUrl;

    private int mainCategoryId;

    private CacheManager cacheManager;

    public DiscussionEndpoint(@Autowired DiscourseClient discourseClient,
                              @Autowired AddonInfoService addonInfoService,
                              @Autowired CacheManager CacheManager,
                              @Value("${discourse.base-url}") String discourseBaseUrl,
                              @Value("${discourse.category}") int mainCategoryId) {
        this.discourseClient = discourseClient;
        this.discourseBaseUrl = discourseBaseUrl;
        this.mainCategoryId = mainCategoryId;
        this.addonInfoService = addonInfoService;
        this.cacheManager = CacheManager;
    }

    /** Check if discussions already exists.
     *
     * @param addonIdentifier The URL identifier of the addon
     * @return True if discussions already exists, false otherwise
     */
    @Cacheable(value = "cache15m", key = "'discussionExists' + #addonIdentifier")
    public boolean discussionExists(String addonIdentifier) {
        // Get all categories
        List<CategoryInfo> allSubcategories = discourseClient.listSubCategoriesInCategory(this.mainCategoryId); // Assuming 5 is the parent category ID

        // Find the subcategory for the addon by searching for the identifier in the description
        var addonCategory = allSubcategories.stream()
                .filter(category -> category.slug.equals(addonIdentifier))
                .findFirst()
                .orElse(null);

        return addonCategory != null;
    }

    /** Creates new discussion if needed.
     *
     * @param addonIdentifier The URL identifier of the addon
     * @return The URL of the created discussion
     */
    public String createDiscussionIfNeeded(String addonIdentifier) {
        // Get all categories
        List<CategoryInfo> allSubcategories = discourseClient.listSubCategoriesInCategory(this.mainCategoryId); // Assuming 5 is the parent category ID

        // Find the subcategory for the addon by searching for the identifier in the description
        var addonCategory = allSubcategories.stream()
                .filter(category -> category.slug.equals(addonIdentifier))
                .findFirst()
                .orElse(null);

        // If the subcategory does not exist, create it
        var categoryUrl = "%sc/%s/%d";
        if (addonCategory == null) {
            var addon = addonInfoService.getAddon(addonIdentifier);
            if (addon == null) {
                return null;
            }
            
            var description = "Discussion and feedback for '" + addon.getName()+"'";
            var bodyHtml = "<p>This is discussion and feedback for " + addon.getName()+"<p>" +
                    "<p>" + addon.getSummary()+"<p>" +
                    "<p><a href=\""+addonInfoService.getComponentUrl()+addonIdentifier+"\">"+addonInfoService.getComponentUrl()+addonIdentifier+"</a></p>";
            DiscourseClient.Category c = discourseClient.createSubcategoryWithDescription(
                    this.mainCategoryId,
                    addonIdentifier,
                    addon.getName(),
                    description,
                    description,
                    bodyHtml);
            categoryUrl = categoryUrl.formatted(discourseBaseUrl, c.slug, c.id);

            // Evict cache for key "'discussionExists' + #addonIdentifier"
            this.cacheManager.getCache("cache15m").evict("'discussionExists' + " + addonIdentifier);

        } else {
            categoryUrl = categoryUrl.formatted(discourseBaseUrl, addonCategory.slug, addonCategory.id);
        }

        // Evict cache for presenting the new discussions
        this.cacheManager.getCache("cache15m").evict("'alltopics' + #addon");
        this.cacheManager.getCache("cache15m").evict("'topics' + #addon");

        // Return the full URL of the category
        return categoryUrl;
    }


    /**
     * Lists messages for a specific addon identified by its URL identifier.
     *
     * @param addon The URL identifier of the addon
     * @return List of messages for the addon
     */
    @Cacheable(value = "cache15m", key = "'alltopics' + #addon")
    public List<Message> listMessages(String addon) {
        return listFirstMessages(addon, -1);
    }

    /**
     * Lists the first messages for a specific addon identified by its URL identifier.
     *
     * @param addon The URL identifier of the addon
     * @param maxTopics The maximum number of topics to retrieve, or -1 if all
     * @return List of messages for the addon
     */
    @Cacheable(value = "cache15m", key = "'topics' + #addon")
    public List<Message> listFirstMessages(String addon, int maxTopics) {
        // Get all categories
        List<CategoryInfo> allSubcategories = discourseClient.listSubCategoriesInCategory(this.mainCategoryId); // Assuming 5 is the parent category ID
        
        // Find the subcategory for the addon by searching for the identifier in the description
        Optional<CategoryInfo> addonCategory = allSubcategories.stream()
            .filter(category -> category.slug.equals(addon))
            .findFirst();
        
        if (addonCategory.isEmpty()) {
            return Collections.emptyList();
        }
        
        // Get topics for the category
        var cat = addonCategory.get();
        List<Topic> topics = discourseClient.listTopicsInCategory(cat.id,maxTopics);

        if (topics == null || topics.isEmpty()) {
            return Collections.emptyList();
        }

        // Sort topics by dateTime descending lastPostedAt
        topics.sort(Comparator.comparing(Topic::getLastPostedAt).reversed());

        // Convert topics to messages
        List<Message> messages = new ArrayList<>();
        for (Topic topic : topics) {
            // Get all posts for each topic
            List<TopicPostsResponse.Post> posts = discourseClient.listPostsInTopic(topic.id);
            
            // Convert posts to messages
            URI baseUrl = URI.create(discourseBaseUrl);
            List<Message> replies = new ArrayList<>();
            for (TopicPostsResponse.Post post : posts) {
                LocalDateTime dateTime = parseDateTime(post.createdAt);
                
                String formattedDate = dateTime.format(DateTimeFormatter.ISO_DATE);
                String formattedTime = dateTime.format(DateTimeFormatter.ISO_TIME);
                
                // Build avatar URL if provided
                String imageUrl = "";
                if (post.avatarTemplate != null && !post.avatarTemplate.isEmpty()) {
                    imageUrl = post.avatarTemplate.replace("{size}", "64");
                }
                
                var m = new Message(
                    topic.id,
                    dateTime,
                    post.postNumber > 1,
                    post.name,
                    post.cooked, // HTML content of the post
                    formattedDate,
                    formattedTime,
                    baseUrl.resolve(imageUrl).toString()
                );
                m.postCount = cat.postCount;
                replies.add(m);

            }
            // Sort replies by dateTime ascending
            replies.sort(Comparator.comparing(Message::getDateTime));
            messages.addAll(replies);
        }
        
        return messages;
    }
    
    /**
     * Parse ISO-8601 datetime string into LocalDateTime
     */
    private LocalDateTime parseDateTime(String dateTimeStr) {
        // Assuming format like: 2023-04-11T15:30:45.123Z
        return LocalDateTime.parse(dateTimeStr.substring(0, 19));
    }

    /** Message represents a single discussion message by an user.
     *
     */
    public static class Message {
        private final int topicId;
        private final boolean reply;
        private final LocalDateTime dateTime;
        private final String author;
        private final String text;
        private final String date;
        private final String time;
        private final String imageUrl;
        private int postCount;

        public Message(int topicId, LocalDateTime dateTime, boolean isReply, String author, String text, String date, String time, String imageUrl) {
            this.topicId = topicId;
            this.dateTime = dateTime;
            this.reply = isReply;
            this.author = author;
            this.text = text;
            this.date = date;
            this.time = time;
            this.imageUrl = imageUrl;
        }

        public int getPostCount() {
            return postCount;
        }

        public int getTopicId() {
            return topicId;
        }

        public boolean isReply() {
            return reply;
        }

        public String getAuthor() {
            return author;
        }

        public String getText() {
            return text;
        }

        public String getDate() {
            return date;
        }

        public String getTime() {
            return time;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public LocalDateTime getDateTime() {
            return dateTime;
        }
    }
}
