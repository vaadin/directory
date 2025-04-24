package org.vaadin.directory.discussion;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@SpringBootTest(classes = DiscourseClientTest.Config.class)
@TestPropertySource(locations = { "classpath:application.properties", "file:config/local/application.properties" })
public class DiscourseClientTest {

    @Configuration
    @EnableAutoConfiguration(exclude = {
            org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class,
            org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration.class
    })
    @ComponentScan(basePackages = "org.vaadin.directory.discussion")
    public static class Config {}

    @Value("${discourse.category}")
    private int disCourseCategory;

    @Autowired
    private DiscourseClient discourse;

    @Test
    void testGreet() {
        List<DiscourseClient.Topic> topics = discourse.listTopicsInCategory(this.disCourseCategory, -1);

        // Check if the topics list is not empty
        assertFalse(topics.isEmpty(), "Topics list should not be empty");

        // Iterate the topics and print
        for (DiscourseClient.Topic topic : topics) {
            System.out.println("Topic ID: " + topic.id);
            System.out.println("Title: " + topic.title);
            System.out.println("Created At: " + topic.createdAt);
            System.out.println("Last Post Date: " + topic.lastPostedAt);
            System.out.println("Views: " + topic.views);
            System.out.println("Likes: " + topic.likeCount);
            System.out.println("------");
        }

        // List all posts in a topic
        DiscourseClient.Topic firstTopic = topics.get(0);
        String topicId = firstTopic.slug+"/"+firstTopic.id;
        List<DiscourseClient.TopicPostsResponse.Post> posts = discourse.listPostsInTopic(topicId);
        // Check if the posts list is not empty
        assertFalse(posts.isEmpty(), "Posts list should not be empty");
        // Iterate the posts and print
        for (DiscourseClient.TopicPostsResponse.Post post : posts) {
            System.out.println("Post ID: " + post.id);
            System.out.println("User ID: " + post.userId);
            System.out.println("Username: " + post.username);
            System.out.println("Created At: " + post.createdAt);
            System.out.println("------");
        }
    }
    
    @Test
    void testListAllPostsInCategoryAndSubcategories() {

        // Create a list to hold all posts
        List<DiscourseClient.TopicPostsResponse.Post> allPosts = new ArrayList<>();
        
        // First, get topics from main category
        List<DiscourseClient.Topic> mainCategoryTopics = discourse.listTopicsInCategory(this.disCourseCategory, -1);
        assertFalse(mainCategoryTopics.isEmpty(), "Main category topics should not be empty");
        
        System.out.println("=== POSTS IN MAIN CATEGORY ===");
        processTopics(mainCategoryTopics, allPosts);

        // Summary
        System.out.println("\n=== SUMMARY ===");
        System.out.println("Total posts collected: " + allPosts.size());
        System.out.println("Posts by user (username count):");
        
        // Count posts by username
        Map<String, Integer> postsByUser = new HashMap<>();
        for (DiscourseClient.TopicPostsResponse.Post post : allPosts) {
            postsByUser.put(post.username, postsByUser.getOrDefault(post.username, 0) + 1);
        }
        
        // Print post statistics by user
        postsByUser.forEach((username, count) -> 
            System.out.println("- " + username + ": " + count + " posts"));
    }
    
    /**
     * Helper method to process topics and collect posts
     */
    private void processTopics(List<DiscourseClient.Topic> topics, List<DiscourseClient.TopicPostsResponse.Post> allPosts) {
        // Process only the first 3 topics to avoid excessive API calls in tests
        int topicCount = Math.min(topics.size(), 3);
        
        for (int i = 0; i < topicCount; i++) {
            DiscourseClient.Topic topic = topics.get(i);
            System.out.println("\nTopic: " + topic.title + " (ID: " + topic.id + ")");
            
            // Get all posts for this topic
            String topicId = topic.slug + "/" + topic.id;
            List<DiscourseClient.TopicPostsResponse.Post> posts = discourse.listPostsInTopic(topicId);
            assertFalse(posts.isEmpty(), "Posts list should not be empty for topic: " + topic.title);
            
            // Add all posts to our collection
            allPosts.addAll(posts);
            
            // Print summarized information about each post
            for (DiscourseClient.TopicPostsResponse.Post post : posts) {
                System.out.println("- Post #" + post.postNumber + " by " + post.username + 
                                  " on " + post.createdAt + " (ID: " + post.id + ")");
            }
        }
    }
    
    @Test
    void testListSubCategoriesInCategory() {
        // Test listing subcategories directly from the /c/<id>.json endpoint
        List<DiscourseClient.CategoryInfo> subCategories = discourse.listSubCategoriesInCategory(this.disCourseCategory);
        
        // Check if the subcategories list is not null
        assertNotNull(subCategories, "Subcategories list should not be null");
        
        // Print information about all subcategories
        System.out.println("\n=== SUBCATEGORIES IN CATEGORY " + this.disCourseCategory + " ===");
        System.out.println("Total subcategories found: " + subCategories.size());
        
        // If we have subcategories, print details and validate
        if (!subCategories.isEmpty()) {
            for (DiscourseClient.CategoryInfo subCategory : subCategories) {
                System.out.println("\nSubcategory: " + subCategory.name + " (ID: " + subCategory.id + ")");
                System.out.println("- Slug: " + subCategory.slug);
                System.out.println("- Description: " + (subCategory.description != null ? subCategory.description : "N/A"));
                System.out.println("- Topic count: " + subCategory.topicCount);
                System.out.println("- Post count: " + subCategory.postCount);
                System.out.println("- Parent category ID: " + subCategory.parentCategoryId);
                
            }
            
            // Verify at least the first subcategory has the expected properties
            DiscourseClient.CategoryInfo firstSubCategory = subCategories.get(0);
            assertNotNull(firstSubCategory.name, "Subcategory name should not be null");
            assertTrue(firstSubCategory.id > 0, "Subcategory ID should be greater than 0");
            assertEquals(this.disCourseCategory, firstSubCategory.parentCategoryId, 
                    "Parent category ID should match the requested category ID");
        } else {
            System.out.println("No subcategories found in category (ID: " + this.disCourseCategory + ")");
        }
        
        // Test getting main categories separately for comparison
        System.out.println("\n=== MAIN CATEGORIES ===");
        List<DiscourseClient.Category> allCategories = discourse.listCategories();
        assertNotNull(allCategories, "Categories list should not be null");
        assertFalse(allCategories.isEmpty(), "Categories list should not be empty");
        
        System.out.println("Total main categories found: " + allCategories.size());
        
        // Find the current category in the main categories list
        Optional<DiscourseClient.Category> currentCategory = allCategories.stream()
                .filter(category -> category.id == this.disCourseCategory)
                .findFirst();

        if (currentCategory.isPresent()) {
            DiscourseClient.Category category = currentCategory.get();
            System.out.println("\nFound target category in main list: " + category.name + " (ID: " + category.id + ")");
            System.out.println("- Has children: " + category.hasChildren);
        }
    }


    void testCreateSubcategoryWithInitialPost() {
        // Test data
        int directoryParentCategoryId = 1234; // The "Directory" category ID
        String subcategoryName = "Test Subcategory " + System.currentTimeMillis(); // Adding timestamp for uniqueness
        String subcategoryDescription = "This is a test subcategory created by automated tests";
        String topicTitle = "Welcome to the test subcategory";
        String topicContent = "This is the first post in this subcategory. It serves as a welcome message for users.";
        
        // Create the subcategory and initial post
        DiscourseClient.TopicPostsResponse response = discourse.createSubcategoryWithInitialPost(
            subcategoryName, 
            subcategoryDescription, 
            directoryParentCategoryId, 
            topicTitle, 
            topicContent
        );
        
        // Verify the response
        assertNotNull(response, "Response should not be null");
        assertNotNull(response.postStream, "Post stream should not be null");
        assertNotNull(response.postStream.posts, "Posts list should not be null");
        assertFalse(response.postStream.posts.isEmpty(), "Posts list should not be empty");
        
        // Find the first post
        DiscourseClient.TopicPostsResponse.Post firstPost = response.postStream.posts.stream()
            .filter(post -> post.postNumber == 1)
            .findFirst()
            .orElse(null);
        
        // Verify the first post
        assertNotNull(firstPost, "First post should exist");
        assertTrue(firstPost.cooked.contains("welcome"), "Post content should contain welcome message");
        
        System.out.println("Created subcategory: " + subcategoryName);
        System.out.println("Created initial topic: " + topicTitle);
        System.out.println("First post by: " + firstPost.username);
        System.out.println("Post content (excerpt): " + firstPost.cooked.substring(0, Math.min(firstPost.cooked.length(), 100)) + "...");
    }

    void testCreateSubcategoryWithInitialTopic() {
        // Get current datetime for the topic title
        String dateTime = java.time.LocalDateTime.now().toString();
        
        // Test data
        String subcategoryName = "Test Subcategory " + System.currentTimeMillis(); // Adding timestamp for uniqueness
        String subcategoryDescription = "This is a test subcategory created via API";
        String topicTitle = "Click Test " + dateTime;
        String topicContent = "Discussion starter";
        
        // Create the subcategory and initial post
        DiscourseClient.TopicPostsResponse response = discourse.createSubcategoryWithInitialPost(
            subcategoryName, 
            subcategoryDescription, 
            this.disCourseCategory, 
            topicTitle, 
            topicContent
        );
        
        // Verify the response
        assertNotNull(response, "Response should not be null");
        assertNotNull(response.postStream, "Post stream should not be null");
        assertNotNull(response.postStream.posts, "Posts list should not be null");
        assertFalse(response.postStream.posts.isEmpty(), "Posts list should not be empty");
        
        // Find the first post
        DiscourseClient.TopicPostsResponse.Post firstPost = response.postStream.posts.stream()
            .filter(post -> post.postNumber == 1)
            .findFirst()
            .orElse(null);
        
        // Verify the first post
        assertNotNull(firstPost, "First post should exist");
        assertEquals(topicContent, firstPost.cooked, "Post content should match what we sent");
        
        // Print information about the created topic
        System.out.println("Created subcategory: " + subcategoryName);
        System.out.println("Created initial topic: " + topicTitle);
        System.out.println("First post by: " + firstPost.username);
        System.out.println("Post content: " + firstPost.cooked);
        
        // Print the public link to the topic
        String topicLink = "/t/" + firstPost.topicSlug + "/" + firstPost.topicId;
        System.out.println("Public link to topic: " + topicLink);
    }
}