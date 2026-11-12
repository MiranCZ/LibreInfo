package io.github.mirancz.update;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.api.model.Container;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;


public class Compose {

    private static final String PROJECT_LABEL = "com.docker.compose.project";
    private static final String SERVICE_LABEL = "com.docker.compose.service";

    private final DockerClient client;
    private final String project;

    public Compose(DockerClient client) {
        this.client = client;

        String hostname = hostname();

        Map<String, String> labels;
        try {
            // a container's default hostname is its (short) id, which inspect accepts
            labels = client.inspectContainerCmd(hostname).exec().getConfig().getLabels();
        } catch (NotFoundException e) {
            throw new IllegalStateException("cannot resolve our own container by hostname", e);
        }

        String project = labels == null ? null : labels.get(PROJECT_LABEL);
        if (project == null || project.isEmpty()) {
            throw new IllegalStateException("not running under Docker Compose: no project label");
        }

        this.project = project;
    }

    public Container get(String service) {
        List<Container> containers = client.listContainersCmd()
                .withShowAll(true)
                .withLabelFilter(Map.of(
                        PROJECT_LABEL, project,
                        SERVICE_LABEL, service
                ))
                .exec();

        if (containers.isEmpty()) {
            throw new IllegalStateException("no container for service '" + service + "' in project '" + project + "'");
        }
        return containers.getFirst();
    }

    public DockerClient client() {
        return client;
    }

    public String project() {
        return project;
    }

    private static String hostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            String env = System.getenv("HOSTNAME");
            if (env != null && !env.isEmpty()) {
                return env;
            }
            throw new IllegalStateException("cannot determine own hostname", e);
        }
    }

}
