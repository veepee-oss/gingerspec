<!DOCTYPE html>
<html>
<head>
    <title>GingerSpec Documentation</title>

    <style type="text/css">
        ${style}
    </style>
</head>
<body>

<h1>Feature List</h1>

<div data-toc>

    <pre><span>${gingerLogo}</span>
    </pre>

</div>

<hr>

<div data-content>


<ol>
<#list files as file>

    <#if file.name != "index.html">
        <li><a href="${file.name}">${file.name?replace("-", " ")?replace(".html", "")?capitalize}</a></li>
    </#if>

</#list>
</ol>

</div>

    <footer>
        <em>Automatically generated documentation for <a href="https://github.com/veepee-oss/gingerspec">GingerSpec library</a>.</em>
    </footer>

</body>
</html>