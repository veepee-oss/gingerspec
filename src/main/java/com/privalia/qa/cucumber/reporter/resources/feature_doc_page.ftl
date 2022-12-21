<!DOCTYPE html>
<html>
<head>

	<header>
		<em>Back to <a href="index.html">Index</a>.</em>
	</header>

	<title>${featureDoc.featureName}</title>

	<style type="text/css">
		${style}
	</style>
</head>
<body>

	<h1>${featureDoc.featureName}</h1>

	<p>${featureDoc.featureDescription}</p>

	<div data-toc></div>

	<hr>

	<div data-content>

		<button onclick="topFunction()" id="myBtn" title="Go to top">Back to top</button>

		<ol>
		<#list featureDoc.featureRules as k, v>
			<li><h2>${k}</h2></li>
			<#list v as k1, v1>
			<ul>
			<li><h3>${k1}</h3></li>
			<div class="highlight">
			<pre><code class="language-gherkin">
			${v1
			?replace("Scenario: ", "<span class=\"gherkin-keyword\">Scenario: </span>")
			?replace("Scenario Outline: ", "<span class=\"gherkin-keyword\">Scenario Outline: </span>")
			?replace("Given ", "<span class=\"gherkin-keyword\">Given </span>")
			?replace("When ", "<span class=\"gherkin-keyword\">When </span>")
			?replace("Then ", "<span class=\"gherkin-keyword\">Then </span>")
			?replace("And ", "<span class=\"gherkin-keyword\">And </span>")
			?replace("But ", "<span class=\"gherkin-keyword\">But </span>")
			?replace("Examples:", "<span class=\"gherkin-keyword\">Examples:</span>")
			?replace("|", "<span class=\"gherkin-keyword\">|</span>")
			}
			</code></pre>
			</div>
			</ul>
			</#list>

			<hr>

		</#list>
		</ol>
	</div>

	<footer>
		<em>Automatically generated documentation for <a href="https://github.com/veepee-oss/gingerspec">GingerSpec library</a>.</em>
	</footer>

	<script>
		${tableOfContents}
	</script>
	<script>
		${returnToTop}
	</script>
	<script>
		${copyToClipboard}
	</script>
	<script>
		tableOfContents('[data-content]', '[data-toc]', {
			heading: 'On this page...', // Change the headings
			listType: 'ol', // Change the list type
			// levels: 'h2, h3' // Change the levels used
		});
	</script>
</body>
</html>