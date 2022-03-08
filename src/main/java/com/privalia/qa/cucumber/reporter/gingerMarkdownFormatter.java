package com.privalia.qa.cucumber.reporter;


import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.TestSourceRead;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

public class gingerMarkdownFormatter implements ConcurrentEventListener {

    private final Writer writer;

    public gingerMarkdownFormatter(OutputStream out) {
        this.writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
    }

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(TestSourceRead.class, this::handleTestSourceRead);
    }

    private void handleTestSourceRead(TestSourceRead event) {
        generateMarkdown(event.getSource());
    }

    private void generateMarkdown(String source) {
        source = source.replace("Feature:", "# Feature").trim();
        source = source.replace("Rule:", "## Rule").trim();
        source = source.replace("Rule:", "## Rule").trim();
        source = source.replace("Scenario:", "### Scenario:").trim();
        source = source.replace("#log:", "'#'log:").trim();


        try {
            this.writer.write(source);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            this.writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
