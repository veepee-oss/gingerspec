package com.privalia.qa.cucumber.reporter;

import freemarker.cache.FileTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.Version;
import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.TestSourceRead;
import org.apache.commons.io.FileUtils;
import org.apache.commons.text.WordUtils;
import scala.collection.immutable.Stream;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Simple formatter to create a html web page representation of the feature file with a
 * table of contents menu at the top for easy navigation
 *
 * @author Jose Fernandez
 */
public class gingerHtmlFormatter implements ConcurrentEventListener {

    private final String destinationFolder;

    public gingerHtmlFormatter(String destinationFolder) {
        this.destinationFolder = destinationFolder;
    }

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(TestSourceRead.class, this::handleTestSourceRead);
    }

    private void handleTestSourceRead(TestSourceRead event) {
        this.generateHtml(event);
    }

    private void generateHtml(TestSourceRead event) {

        featureDoc f = new featureDoc(event.getSource());

        if (!f.getFeatureRules().isEmpty()) {

            try {
                Configuration cfg = new Configuration();
                FileTemplateLoader templateLoader = new FileTemplateLoader(new File("src/main/java/com/privalia/qa/cucumber/reporter/resources"));
                cfg.setTemplateLoader(templateLoader);
                cfg.setIncompatibleImprovements(new Version(2, 3, 31));
                cfg.setDefaultEncoding("UTF-8");
                cfg.setLocale(Locale.US);
                Template featureTemplate = cfg.getTemplate("feature_doc_page.ftl");
                Template indexTemplate = cfg.getTemplate("index_template.ftl");
                new File(this.destinationFolder).mkdirs();
                Writer featureWriter = new FileWriter(this.destinationFolder + "/" + WordUtils.uncapitalize(f.getFeatureName()).replaceAll(" ", "-") + ".html");
                Writer indexWriter = new FileWriter(this.destinationFolder + "/index.html");

                try {
                    /*Build the feature html page*/
                    Map<String, Object> featurePageVariables = new HashMap<>();
                    featurePageVariables.put("featureDoc", f);
                    featurePageVariables.put("tableOfContents", this.getFileAsString("src/main/java/com/privalia/qa/cucumber/reporter/resources/table-of-contents.js"));
                    featurePageVariables.put("returnToTop", this.getFileAsString("src/main/java/com/privalia/qa/cucumber/reporter/resources/return-to-top.js"));
                    featurePageVariables.put("copyToClipboard", this.getFileAsString("src/main/java/com/privalia/qa/cucumber/reporter/resources/copy-to-clipboard.js"));
                    featurePageVariables.put("style", this.getFileAsString("src/main/java/com/privalia/qa/cucumber/reporter/resources/style.css"));
                    featureTemplate.process(featurePageVariables, featureWriter);

                    /*Build the index page with whatever *.html files it finds in the same folder*/
                    Map<String, Object> indexPageVariables = new HashMap<>();
                    File[] files = new File(this.destinationFolder).listFiles();
                    indexPageVariables.put("files", Arrays.asList(files));
                    indexPageVariables.put("style", this.getFileAsString("src/main/java/com/privalia/qa/cucumber/reporter/resources/style.css"));
                    indexPageVariables.put("gingerLogo", this.getFileAsString("src/test/resources/banner.txt"));
                    indexTemplate.process(indexPageVariables, indexWriter);

                } finally {
                    featureWriter.close();
                    indexWriter.close();
                }

            } catch (IOException | TemplateException e) {
                e.printStackTrace();
            }
        }
    }

    private String getFileAsString(String path) {

        File file = new File(path);
        try {
            return FileUtils.readFileToString(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
