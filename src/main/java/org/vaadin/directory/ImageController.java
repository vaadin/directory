package org.vaadin.directory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.vaadin.directory.endpoint.addon.Addon;
import org.vaadin.directory.endpoint.addon.AddonEndpoint;
import org.vaadin.directory.store.AddonRatingInfo;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.WeakHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
public class ImageController {


    private static final Color VAADIN_LIGHT_GREY = new Color(228,232,232);

    private static final Color VAADIN_BLUE = new Color(0,180,240);
    private static final Color VAADIN_DARK_GREY = new Color(45,48,51);
    private BufferedImage logo;
    private final String logoUrl;
    private String baseUrl;

    private static DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private WeakHashMap<String, ByteArrayOutputStream> imageCache = new WeakHashMap<>();
    private AddonEndpoint service;

    ImageController(@Autowired UrlConfig urlConfig,
                    @Autowired AddonEndpoint service){
        this.service = service;
        this.baseUrl = urlConfig.getAppUrl();
        this.logoUrl = urlConfig.getAppIconUrl();
    }

    @RequestMapping(
            value = "/embed/{addon}",
            method = RequestMethod.GET,
            produces = MediaType.TEXT_PLAIN_VALUE
    )
    public String getEmbedScript(@PathVariable("addon") String urlIdentifier) {
        return "(() => {\n" +
                "  var frame = document.createElement('iframe');\n" +
                "  frame.src='"+this.baseUrl+"addon-card/"+urlIdentifier+"';\n" +
                "  frame.width='405';\n" +
                "  frame.height='250';\n" +
                "  frame.style.border='none';\n" +
                "  document.querySelector('#addon-card').appendChild(frame);\n" +
                "})();";
    }

    @RequestMapping(
            value = "/images/social/{addon}",
            method = RequestMethod.GET,
            produces = MediaType.IMAGE_PNG_VALUE
    )
    public @ResponseBody byte[] getSocialMediaImage(@PathVariable("addon") String urlIdentifier)
            throws IOException {

        synchronized (imageCache) {
            if (imageCache.containsKey(urlIdentifier)) {
                return imageCache.get(urlIdentifier).toByteArray();
            }
        }

        Addon addon = service.getAddon(urlIdentifier, "");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        if (addon != null) {
            String name = addon.getName();
            String desc = addon.getSummary();
            String author = addon.getAuthor();
            String iconUrl = addon.getIcon();
            String rating = addon.getRating() > 0 ? Stream.of(1,2,3,4,5).map(v -> v < addon.getRating()?"★":"☆").collect(Collectors.joining()) : "☆☆☆☆☆" ;
            String updated = addon.getLastUpdated().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM));

            // Reset
            BufferedImage image = new BufferedImage(800, 418, BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = image.createGraphics();
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            graphics.setBackground(new Color(0, true));

            graphics.clearRect(0, 0, 800, 418);

            // White background
            graphics.setPaint(Color.WHITE);
            //RoundRectangle2D bg = new RoundRectangle2D.Float(1, 1, 800-2, 418-2, 32, 32);
            //graphics.draw(bg);
            //graphics.fill(bg);
            graphics.fillRect(0,0,800,418);

            // Name
            graphics.setColor(VAADIN_DARK_GREY);
            Font font = new Font("Poppins", Font.BOLD, 48);
            graphics.setFont(font);
            graphics.drawString(name, 32, 80);

            // Description
            font = new Font("Poppins", Font.PLAIN, 24);
            graphics.setFont(font);
            int yCursor = 100;
            List<String> descLines = wrap(desc, graphics.getFontMetrics(), 500);
            for (int i = 0; i < Math.min(descLines.size(),5); i++) {
                yCursor += 30;
                graphics.drawString(descLines.get(i), 32, yCursor);
            }
            yCursor += 50;

            // Stars
            font = new Font("Poppins", Font.PLAIN, 36);
            graphics.setColor(VAADIN_DARK_GREY);
            graphics.setFont(font);
            graphics.drawString(rating,  32, yCursor);

            // Icon with rounded border
            RoundRectangle2D border = new RoundRectangle2D.Float(800-232-2, 105-2, 200+4, 200+4, 32, 32);
            graphics.setPaint(VAADIN_LIGHT_GREY);
            graphics.fill(border);

            BufferedImage iconImg = resize(ImageIO.read(new URL(iconUrl)), 200, 200);
            graphics.setClip(border);
            graphics.drawImage(iconImg,800-232, 105, null);
            graphics.setClip(null);

            // Screenshot
            //BufferedImage screenshot = resize(ImageIO.read(new URL(iconUrl)),200,200);

            // Vaadin logo loaded only once
            if (logo == null) {
                logo = resize(ImageIO.read(new URL(logoUrl)),22,22);
            }

            // Footer
            graphics.setColor(VAADIN_LIGHT_GREY);
            graphics.fillRect(0, 418-40, 800, 40);
            graphics.drawImage(logo,800-32-22, 418-8-22, null);

            // Author
            font = new Font("Poppins", Font.PLAIN, 18);
            graphics.setColor(VAADIN_DARK_GREY);
            graphics.setFont(font);
            graphics.drawString(author + " published on " + updated, 32, 418-14);


            ImageIO.write(image,"PNG", baos);
            graphics.dispose();

            synchronized (imageCache) {
                imageCache.put(addon.getUrlIdentifier(), baos);
            }

        }
        return baos.toByteArray();
    }

    private static BufferedImage resize(BufferedImage img, int height, int width) {
        Image tmp = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = resized.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();
        return resized;
    }
    private static List<String> wrap(String txt, FontMetrics fm, int maxWidth){
        StringTokenizer st =  new  StringTokenizer(txt)  ;

        List<String> list = new ArrayList<String>();
        String line = "";
        String lineBeforeAppend = "";
        while (st.hasMoreTokens()){
            String seg = st.nextToken();
            lineBeforeAppend = line;
            line += seg + " ";
            int width = fm.stringWidth(line);
            if(width  < maxWidth){
                continue;
            }else { //new Line.
                list.add(lineBeforeAppend);
                line = seg + " ";
            }
        }
        //the remaining part.
        if(line.length() > 0){
            list.add(line);
        }
        return list;
    }
}
