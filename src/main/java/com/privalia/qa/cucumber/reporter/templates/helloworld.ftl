<h1 id="sample-markdown">${featureDoc.featureName}</h1>
<p>This is some basic, sample markdown.</p>

<h2 id="contents">Contents</h2>
<ul>
    <#list featureDoc.ruleSet as ruleName>
        <li><a href="#${ruleName?replace(" ", "-")?lower_case}">${ruleName}</a></li>
    </#list>
</ul>

<#list featureDoc.featureRules as k, v>
    <h2 id="${k?replace(" ", "-")?lower_case}">${k}</h2>
    <#list v as k1, v1>
        <h3>${k1}</h3>
        <pre><code class="lang-gherkin"><span class="hljs-attribute">${v1}</span>
        </code></pre>
    </#list>
</#list>


