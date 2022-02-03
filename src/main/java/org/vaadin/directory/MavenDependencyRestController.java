package org.vaadin.directory;

import com.vaadin.directory.backend.maven.PomXmlUtil;
import com.vaadin.directory.backend.service.ComponentService;
import com.vaadin.directory.entity.directory.Component;
import com.vaadin.directory.entity.directory.ComponentVersion;
import com.vaadin.directory.entity.directory.FileContent;
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
    public MavenDependency getMavenDependency(@PathVariable("addon") String urlIdentifier,
                                  @PathVariable("version") String version) {
        Optional<Component> maybeComponent = service.getComponentByUrl(urlIdentifier);
        if (maybeComponent.isPresent())  {
            final Component c = maybeComponent.get();
            Optional<ComponentVersion> maybeVersion = c.getVersions().stream()
                    .filter(v -> version.equalsIgnoreCase(v.getName()))
                    .findFirst();
            if (maybeVersion.isPresent()) {
                ComponentVersion componentVersion = maybeVersion.get();
                FileContent file = componentVersion.getContent();
                String groupId = componentVersion.getMavenGroupId();
                String artifactId = componentVersion.getMavenArtifactId();
                String versionId = file.getMavenVersionId();

                if (file.getMavenArtifactId() != null) {
                    artifactId = file.getMavenArtifactId();
                }
                return new MavenDependency(groupId,artifactId,versionId);
            }
        }
        return null;
    }


    /** Class for creating valid REST response.
     *
     */
    public static class MavenDependency {
        private String groupId, artifactId, version, scope;

        public MavenDependency() {
        }

        public MavenDependency(String groupId, String artifactId) {
            this(groupId, artifactId, null);
        }

        public MavenDependency(String groupId, String artifactId, String version) {
            this(groupId, artifactId, version, null);
        }

        public MavenDependency(String groupId, String artifactId, String version, String scope) {
            this.groupId = groupId;
            this.artifactId = artifactId;
            this.version = version;
            this.scope = scope;
        }

        public String asDependency() {
            StringBuilder s = new StringBuilder();
            s.append("        <dependency>\n");
            if (groupId != null)
                s.append("            <groupId>" + groupId + "</groupId>\n");
            if (artifactId != null)
                s.append("            <artifactId>" + artifactId + "</artifactId>\n");
            if (version != null)
                s.append("            <version>" + version + "</version>\n");

            s.append("        </dependency>\n");

            return s.toString();

        }

        public String getGroupId() {
            return groupId;
        }

        public void setGroupId(String groupId) {
            this.groupId = groupId;
        }

        public String getArtifactId() {
            return artifactId;
        }

        public void setArtifactId(String artifactId) {
            this.artifactId = artifactId;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getScope() {
            return scope;
        }

        public void setScope(String scope) {
            this.scope = scope;
        }

    }
}
