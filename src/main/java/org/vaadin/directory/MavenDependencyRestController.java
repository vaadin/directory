package org.vaadin.directory;

import com.vaadin.directory.backend.maven.PomXmlUtil;
import com.vaadin.directory.backend.service.ComponentService;
import com.vaadin.directory.entity.directory.Component;
import com.vaadin.directory.entity.directory.ComponentVersion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController("")
public class MavenDependencyRestController {

    private final ComponentService service;

    public MavenDependencyRestController(@Autowired ComponentService service) {
        this.service = service;
    }


    @GetMapping("/maven/{addon}/{version}")
    @Transactional(readOnly = true)
    public String getMavenSnippet(@PathVariable("addon") String urlIdentifier,
                                  @PathVariable("version") String version) {
        Optional<Component> maybeComponent = service.getComponentByUrl(urlIdentifier);
        if (maybeComponent.isPresent())  {
            final Component c = maybeComponent.get();
            Optional<ComponentVersion> maybeVersion = c.getVersions().stream()
                    .filter(v -> version.equalsIgnoreCase(v.getName()))
                    .findFirst();
            if (maybeVersion.isPresent()) {
                return PomXmlUtil.getDependencyPomSnippet(maybeVersion.get());
            }
        }
        return null;
    }
}
