package org.vaadin.directory;

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

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
public class FileController {


    private String baseUrl;

    private static DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private ComponentService service;

    FileController(@Value("${app.url}") String appUrl,
                   @Autowired ComponentService service){
        this.service = service;
        this.baseUrl = appUrl;
    }

    @RequestMapping(path = "/robots.txt", method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> getRobotsFile() {
        return ResponseEntity.ok("User-agent: *\n" +
                "Allow: /\n" +
                "Sitemap: "+this.baseUrl+"sitemap.xml\n");
    }

    @RequestMapping(path = "/sitemap.xml", method = RequestMethod.GET, produces = MediaType.APPLICATION_XML_VALUE)
    public UrlSet getSitemap() {
        UrlSet urlset = new UrlSet();

        //TODO: We limit this at this point to test it out
        service.findAllPublishedComponents(Pageable.ofSize(10))
                .forEach(c -> urlset.add(new Url(baseUrl +"addon/"+c.getUrlIdentifier(), c.getLatestPublicationDate())));

        return urlset;
    }

    /** Root of sitemap.xml. */
    @JacksonXmlRootElement(localName = "urlset", namespace = "http://www.sitemaps.org/schemas/sitemap/0.9")
    public static class UrlSet {
        @JacksonXmlElementWrapper(useWrapping = false)
        @JacksonXmlProperty(localName = "url", namespace = "http://www.sitemaps.org/schemas/sitemap/0.9")
        private List<Url> urls = new ArrayList<>();

        public void add(Url url) {
            urls.add(url);
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
            this.lastmod = dateFormat.format(LocalDate.ofInstant(lastmod.toInstant(), ZoneId.systemDefault()));
        }
    }
}
