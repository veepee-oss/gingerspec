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
			<pre><code class="language-gherkin">${v1}
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