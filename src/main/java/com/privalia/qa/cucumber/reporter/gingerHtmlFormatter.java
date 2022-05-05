package com.privalia.qa.cucumber.reporter;


import com.privalia.qa.cucumber.reporter.templates.featureDoc;
import freemarker.cache.FileTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.Version;
import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.TestSourceRead;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class gingerHtmlFormatter implements ConcurrentEventListener {

    private final Writer writer;

    public gingerHtmlFormatter(OutputStream out) {
        this.writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
    }

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(TestSourceRead.class, this::handleTestSourceRead);
    }

    private void handleTestSourceRead(TestSourceRead event) {
        this.generateHtml(event);
    }

    private void generateHtml(TestSourceRead event) {

        featureDoc f = new featureDoc(event);

        if (!f.getFeatureRules().isEmpty()) {

            try {
                Configuration cfg = new Configuration();
                FileTemplateLoader templateLoader = new FileTemplateLoader(new File("src/main/java/com/privalia/qa/cucumber/reporter/templates"));
                cfg.setTemplateLoader(templateLoader);
                cfg.setIncompatibleImprovements(new Version(2, 3, 31));
                cfg.setDefaultEncoding("UTF-8");
                cfg.setLocale(Locale.US);
                Template template = cfg.getTemplate("helloworld.ftl");
                Writer fileWriter = new FileWriter(new File("output.html"));

                try {
                    Map<String, Object> root = new HashMap<>();
                    root.put("featureDoc", f);
                    template.process(root, fileWriter);
                } finally {
                    fileWriter.close();
                }

            } catch (IOException | TemplateException e) {
                e.printStackTrace();
            }
        }
    }
}
