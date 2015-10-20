package com.stratio.cucumber.aspects;

import com.stratio.exceptions.IncludeException;
import cucumber.runtime.CucumberException;
import cucumber.runtime.Runtime;
import cucumber.runtime.io.Resource;
import cucumber.runtime.model.CucumberScenario;
import gherkin.formatter.Formatter;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.Scenario;
import gherkin.formatter.model.Tag;
import gherkin.lexer.Encoding;
import gherkin.util.FixJava;
import org.apache.commons.collections.MapIterator;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Key;
import java.util.*;

@Aspect
public class IncludeTagAspect {

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getCanonicalName());


    @Pointcut("execution (private * cucumber.runtime.FeatureBuilder.read(..)) &&"  + "args (resource)")
    protected void addIncludeTagPointcutScenario(Resource resource) {
    }
/**
 * @param resource
 * @throws Throwable
 */
    @Around(value = "addIncludeTagPointcutScenario(resource)")
    public String aroundAddIncludeTagPointcutScenario(Resource resource) throws Throwable {
        logger.info("Executing pointcut CucumberScenario run method");

        String pth = resource.getPath();
        int endIndex = pth.lastIndexOf("/")+1;
        pth = pth.substring(0, endIndex);
        String featureName;
        String scenarioName;

        List<String> lines = Files.readAllLines(Paths.get(resource.getPath()), StandardCharsets.UTF_8);
        String nwsource="";
        boolean marked = false;

        for (int i=0; i<lines.size();i++){
            if(lines.get(i).contains("@include")){
                featureName = getFeatureName(lines.get(i));
                scenarioName= getScenName(lines.get(i));
                String[] params = getParams(lines.get(i));
                if (i==0){
                    for (int j=i; j<lines.size();j++){
                        if (lines.get(j).contains("Background:") && !marked) {
                            lines.add(j + 1, featureStepConverter(pth + featureName, scenarioName, params));
                            marked = true;
                        }
                    }
                    if (!marked){
                        lines.add (i+2,"Background:\n");
                        lines.add(i + 3, featureStepConverter(pth + featureName, scenarioName, params));
                    }
                } else {
                    lines.add(i + 2, featureStepConverter(pth + featureName, scenarioName, params));
                }
                lines.remove(i);
            }
            nwsource += lines.get(i) + "\n";
        }

        logger.info(nwsource);
        return nwsource;
    }

    private String getFeatureName(String s) {

        String feature = s.substring((s.lastIndexOf("feature:") + "feature:".length()));
        feature = feature.substring(0,feature.indexOf(","));

        return feature.trim();
    }

    private String getScenName(String s) {

        String scenName = s.substring((s.lastIndexOf("scenario:") + "scenario:".length()));
        if (s.contains("params")) {
            scenName = scenName.substring(0, scenName.indexOf(","));
        }else{
            scenName = scenName.substring(0, scenName.indexOf(")"));
        }

        return scenName.trim();
    }

    private String[] getParams(String s) {
        String[] vals = null;
        if (s.contains("params")) {
            String[] pairs = s.substring((s.lastIndexOf("[") + 1), (s.length()) - 2).split(",");
            vals = new String[(pairs.length)*2];
            int cont = 0;
            for (int m=0;m<pairs.length;m++){
                vals[cont]="<"+pairs[m].split(":")[0].trim()+">";  //key
                cont++;
                vals[cont]=pairs[m].split(":")[1].trim();           //value
                cont++;
            }
        }
        return vals;
    }

    private String doReplaceKeys(String parsedFeature, String[] params) {
        for (int i=0; i<params.length;i++) {
            parsedFeature = parsedFeature.replaceAll(params[i],params[i+1]);
            i++;
        }
        //return feature with no parses
        return parsedFeature;
    }

    private String featureStepConverter(String feature, String scenarioName, String[] params) throws IncludeException {
        boolean scenarioexists  = false;
        BufferedReader br = null;
        String parsedFeature = "";
        String sCurrentLine;

        try {

            br = new BufferedReader(new FileReader(feature));

            while ((sCurrentLine = br.readLine()) != null) {
                if (sCurrentLine.contains(scenarioName)){

                    while((sCurrentLine = br.readLine())!=null && !(sCurrentLine.contains("Scenario:") || sCurrentLine.contains("Examples:"))){
                        parsedFeature = parsedFeature + sCurrentLine + "\n";
                    }
                    scenarioexists= true;
                }
            }

            if (!scenarioexists){
                throw new IncludeException("-> Scenario not present at the given feature.");
            }
        } catch (FileNotFoundException e) {
            throw new IncludeException("-> Feature file were not found.");
        } catch (IOException e) {
            throw new IncludeException("-> An I/O error appeared.");
        } finally {
            try {
                if (br != null)br.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        if (params!=null) {
            parsedFeature = doReplaceKeys(parsedFeature, params);
        }

        return parsedFeature;

    }


}