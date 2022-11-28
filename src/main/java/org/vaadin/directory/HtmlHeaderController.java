package org.vaadin.directory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.vaadin.directory.endpoint.addon.Addon;
import org.vaadin.directory.endpoint.addon.AddonEndpoint;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/** Handle the static HTML requests by injecting metadata based on routes.
 *
 */
@Component
public class HtmlHeaderController implements Filter {

    public static final String ROUTE_COMPONENT = "/component/";

    private static final String URL = "https://vaadin.com/directory";
    public static final String TITLE = "Vaadin Add-on Directory";
    public static final String DESCRIPTION = "Find open-source widgets, add-ons, themes, and integrations for your Vaadin application\\.";
    public static final String IMAGE = "https://vaadin\\.com/images/trademark/PNG/VaadinLogomark_RGB_500x500\\.png";
    private final UrlConfig urlConfig;
    private final AddonEndpoint service;

    HtmlHeaderController(@Autowired UrlConfig urlConfig,
                         @Autowired AddonEndpoint service) {
        this.urlConfig = urlConfig;
        this.service = service;
    }

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain) throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        String uri = req.getRequestURI();
        if (uri.contains(ROUTE_COMPONENT)) {
            String urlIdentifier = uri.substring(uri.indexOf(ROUTE_COMPONENT)+ROUTE_COMPONENT.length());
            Addon oc = service.getAddon(urlIdentifier, "");
            if (oc != null) {
                CapturingResponseWrapper capturingResponseWrapper = new CapturingResponseWrapper((HttpServletResponse) response);
                chain.doFilter(request, capturingResponseWrapper);
                String content = capturingResponseWrapper.getCaptureAsString(); // This uses response character encoding.
                String replacedContent = content.replaceAll(TITLE, ""+oc.getName() + " - "+TITLE);
                replacedContent = replacedContent.replaceAll(URL, urlConfig.getComponentUrl()+urlIdentifier);
                replacedContent = replacedContent.replaceAll(DESCRIPTION, ""+oc.getSummary());
                replacedContent = replacedContent.replaceAll(IMAGE, urlConfig.getAppUrl()+"images/social/"+urlIdentifier);
                replacedContent = replacedContent.replaceAll("</head>", getJsonLd(oc.getName(), oc.getSummary(), oc.getIcon(), oc.getAuthor(),urlConfig.getComponentUrl()+urlIdentifier,oc.getLastUpdated(),null,oc.getRating(), oc.getRatingCount() )+"\n</head>");
                response.getOutputStream().write(replacedContent.getBytes(response.getCharacterEncoding()));
            } else {
                // Requested addon was not found
                ((HttpServletResponse)response).sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } else {
            chain.doFilter(request, response);
        }
    }

    static class CapturingResponseWrapper extends HttpServletResponseWrapper {

        private final ByteArrayOutputStream capture;
        private ServletOutputStream output;
        private PrintWriter writer;

        public CapturingResponseWrapper(HttpServletResponse response) {
            super(response);
            capture = new ByteArrayOutputStream(response.getBufferSize());
        }

        @Override
        public ServletOutputStream getOutputStream() {
            if (writer != null) {
                throw new IllegalStateException("getWriter() has already been called on this response.");
            }

            if (output == null) {
                output = new FilteredServletOutputStream(capture);
            }

            return output;
        }

        @Override
        public PrintWriter getWriter() throws IOException {
            if (output != null) {
                throw new IllegalStateException("getOutputStream() has already been called on this response.");
            }

            if (writer == null) {
                writer = new PrintWriter(new OutputStreamWriter(capture, getCharacterEncoding()));
            }

            return writer;
        }

        @Override
        public void flushBuffer() throws IOException {
            super.flushBuffer();

            if (writer != null) {
                writer.flush();
            } else if (output != null) {
                output.flush();
            }
        }

        public byte[] getCaptureAsBytes() throws IOException {
            if (writer != null) {
                writer.close();
            } else if (output != null) {
                output.close();
            }

            return capture.toByteArray();
        }

        public String getCaptureAsString() throws IOException {
            return new String(getCaptureAsBytes(), getCharacterEncoding());
        }
    }

    static class FilteredServletOutputStream extends ServletOutputStream {

        private final ByteArrayOutputStream output;
        private boolean ready;

        public FilteredServletOutputStream(ByteArrayOutputStream output) {
            this.output = output;
            this.ready = output != null;
        }

        @Override
        public boolean isReady() {
            return this.ready;
        }

        @Override
        public void setWriteListener(WriteListener writeListener) {
            // NA
        }

        @Override
        public void write(int b) throws IOException {
            output.write(b);
        }

        @Override
        public void flush() throws IOException {
            output.flush();
        }

        @Override
        public void close() throws IOException {
            ready = false;
            super.close();
        }
    }

    private static String getJsonLd(String name,
                                    String description,
                                    String iconUrl,
                                    String author,
                                    String url,
                                    LocalDate lastUpdated,
                                    String screenshotUrl,
                                    double rating,
                                    long ratingCount) {
        return "<script type=\"application/ld+json\">{\n" +
                "\"@context\": \"https://schema.org\",\n" +
                "\"@type\": \"SoftwareApplication\",\n" +
                "\"name\": \""+name+"\",\n" +
                "\"downloadUrl\": \""+url+"\",\n" +
                "\"image\": \""+iconUrl+"\",\n" +
                "\"author\": {\n" +
                "  \"@type\": \"Person\",\n" +
                "  \"name\": \""+author+"\"\n" +
                "  },\n" +
                "\"datePublished\": \""+ DateTimeFormatter.ISO_DATE.format(lastUpdated) +"\",\n" +
                "\"applicationCategory\": \"BrowserApplication\",\n" +
                (screenshotUrl != null ? "\"screenshot\": \""+screenshotUrl+"\",\n":"") +
                "\"aggregateRating\": {\n" +
                "  \"@type\": \"AggregateRating\",\n" +
                "  \"ratingValue\": \""+(rating > 0 ? rating : 0)+"\",\n" +
                "  \"ratingCount\": \""+ (ratingCount > 0 ? ratingCount:0)+"\"\n" +
                "  }      \n" +
                "}</script>";
    }
}