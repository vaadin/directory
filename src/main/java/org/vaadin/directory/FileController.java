package org.vaadin.directory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.vaadin.directory.backend.service.ComponentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.xml.bind.annotation.XmlTransient;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
public class FileController {

    private String baseUrl;

    private static DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private ComponentService service;
    private UrlSet cachedUrlSet;

    FileController(@Value("${app.url}") String appUrl,
                   @Autowired ComponentService service){
        this.service = service;
        this.baseUrl = appUrl;
    }

    @RequestMapping(path = "/robots.txt", method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> getRobotsFile() {
        return ResponseEntity.ok("User-agent: *\n" +
                "Disallow: /\n\n" + // TODO: Disable indexing for the time being
                "User-agent: Twitterbot\n" +
                "Disallow:\n\n" +
                "Sitemap: "+this.baseUrl+"sitemap.xml\n");
    }

    @RequestMapping(path = "/sitemap.xml", method = RequestMethod.GET, produces = MediaType.APPLICATION_XML_VALUE)
    public UrlSet getSitemap() {

        if (cachedUrlSet != null &&  LocalDateTime.now()
                .isAfter(cachedUrlSet.timestamp.plus(12,
                        ChronoUnit.HOURS))) {
            return cachedUrlSet;
        } else {
            UrlSet urlset = new UrlSet();
            urlset.timestamp = LocalDateTime.now();

            service.findAllPublishedComponents(Pageable.unpaged())
                    .forEach(c -> urlset.add(new Url(baseUrl +"component/"+c.getUrlIdentifier(), c.getLatestPublicationDate())));

            cachedUrlSet = urlset;
            return urlset;
        }
    }

    /** Root of sitemap.xml. */
    @JacksonXmlRootElement(localName = "urlset", namespace = "http://www.sitemaps.org/schemas/sitemap/0.9")
    public static class UrlSet {
        @JsonIgnore
        @XmlTransient
        private LocalDateTime timestamp;

        @JacksonXmlElementWrapper(useWrapping = false)
        @JacksonXmlProperty(localName = "url", namespace = "http://www.sitemaps.org/schemas/sitemap/0.9")
        private List<Url> urls = new ArrayList<>();

        public void add(Url url) {
            if (url != null) {
                urls.add(url);
            }
        }
    }

    /** Single URL in sitemap.xml. */
    public static class Url {

        @JacksonXmlProperty(localName = "loc", namespace = "http://www.sitemaps.org/schemas/sitemap/0.9")
        private String loc;

        @JacksonXmlProperty(localName = "lastmod", namespace = "http://www.sitemaps.org/schemas/sitemap/0.9")
        private String lastmod;

        public Url(String loc) {
            this.loc = loc;
            this.lastmod = null;
        }

        public Url(String loc, Date lastmod) {
            this.loc = loc;
            if (lastmod == null) {
                lastmod = new Date(0);
            }
            this.lastmod = dateFormat.format(LocalDate.ofInstant(lastmod.toInstant(), ZoneId.systemDefault()));
        }
    }
}
