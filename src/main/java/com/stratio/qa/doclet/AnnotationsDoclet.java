/*
 * Copyright (C) 2014 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.stratio.qa.doclet;

import com.sun.javadoc.*;
import com.sun.tools.javadoc.MethodDocImpl;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class AnnotationsDoclet {

    public AnnotationsDoclet() throws IOException {
    }

    public static boolean start(RootDoc root) throws IOException {
        ClassDoc[] classes = root.classes();

        for (int i = 0; i < classes.length; ++i) {
            StringBuilder sb = new StringBuilder();
            ClassDoc cd = classes[i];
            printAnnotations(cd.constructors(), sb, classes[i].name());
            printAnnotations(cd.methods(), sb, classes[i].name());
        }

        return true;
    }

    static void printAnnotations(ExecutableMemberDoc[] mems, StringBuilder sb, String className) throws IOException {
        String annotation = "";
        for (int i = 0; i < mems.length; ++i) {
            AnnotationDesc[] annotations = mems[i].annotations();
            for (int j = 0; j < annotations.length; ++j) {
                annotation = annotations[j].annotationType().toString();
                if ((annotation.endsWith("Given")) || (annotation.endsWith("When")) || (annotation.endsWith("Then"))) {
                    FileWriter htmlAnnotationJavadoc = new FileWriter("com/stratio/qa/specs/" + className + "-annotations.html");
                    BufferedWriter out = new BufferedWriter(htmlAnnotationJavadoc);

                    sb.append("<html>");
                    sb.append("<head>");
                    sb.append("<title>" + className + " Annotations</title>");
                    sb.append("</head>");
                    sb.append("<body>");

                    sb.append("<dl>");
                    sb.append("<dt><b>Regex</b>: " + annotations[j].elementValues()[0].value().toString() + "</dt>");
                    sb.append("<dd><b>Step</b>: " + annotations[j].elementValues()[0].value().toString().replace("\\", "") + "</dd>");
                    sb.append("<dd><b>Method</b>: <a href=\"./" + className + ".html#" + ((MethodDocImpl) mems[i]).name() + "-" + ((MethodDocImpl) mems[i]).signature().replace("(","").replace(")","").replace(", ","-") + "-\">" + ((MethodDocImpl) mems[i]).qualifiedName() + ((MethodDocImpl) mems[i]).signature() + "</a></dd>");
                    sb.append("</dl>");

                    sb.append("</body>");
                    sb.append("</html>");
                    out.write(sb.toString());
                    out.close();
                }
            }
        }
    }
}
