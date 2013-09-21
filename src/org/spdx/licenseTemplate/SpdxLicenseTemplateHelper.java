/**
 * Copyright (c) 2013 Source Auditor Inc.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
*/
package org.spdx.licenseTemplate;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


import org.apache.commons.lang3.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * Implements common conversion methods for processing SPDX license templates
 * @author Gary O'Neall
 *
 */
public class SpdxLicenseTemplateHelper {
	
	static final String START_RULE = "<<";
	static final String END_RULE = ">>";
	public static final Pattern RULE_PATTERN = Pattern.compile(START_RULE+"\\s*(.+?)\\s*"+END_RULE);
	private static final String FIVE_SPACES = "     ";

	/**
	 * Parses the license template calling the templateOutputHandler for any text and rules found
	 * @param licenseTemplate License template to be parsed
	 * @param templateOutputHandler Handles the text, optional text, and variable rules text found
	 */
	public static void parseTemplate(String licenseTemplate,
			ILicenseTemplateOutputHandler templateOutputHandler) throws LicenseTemplateRuleException {
		Matcher ruleMatcher = RULE_PATTERN.matcher(licenseTemplate);
		int end = 0;
		boolean inOptional = false;
		while (ruleMatcher.find()) {
			// copy everything up to the start of the find
			String upToTheFind = licenseTemplate.substring(end, ruleMatcher.start());
			if (!upToTheFind.isEmpty()) {
				if (inOptional) {
					templateOutputHandler.optionalText(upToTheFind);
				} else {
					templateOutputHandler.normalText(upToTheFind);
				}
			}
			end = ruleMatcher.end();
			String ruleString = ruleMatcher.group(1);
			LicenseTemplateRule rule = new LicenseTemplateRule(ruleString);
			if (rule.getType() == LicenseTemplateRule.RuleType.VARIABLE) {
				templateOutputHandler.variableRule(rule);
			} else if (rule.getType() == LicenseTemplateRule.RuleType.BEGIN_OPTIONAL) {
				if (inOptional) {
					throw(new LicenseTemplateRuleException("Invalid nested optional rule found"));
				} else {
					inOptional = true;
					templateOutputHandler.beginOptional(rule);
				}
			} else if (rule.getType() == LicenseTemplateRule.RuleType.END_OPTIONAL) {
				if (inOptional) {
					inOptional = false;
					templateOutputHandler.endOptional(rule);
				} else {
					throw(new LicenseTemplateRuleException("End optional rule found without a matching begin optional rule"));
				}
			} else {
				throw(new LicenseTemplateRuleException("Unrecognized rule: "+rule.getType().toString()));
			}
		}
		if (inOptional) {
			throw(new LicenseTemplateRuleException("Missing EndOptional rule"));
		}
		// copy the rest of the template to the end
		String restOfTemplate = licenseTemplate.substring(end);
		if (!restOfTemplate.isEmpty()) {
			templateOutputHandler.normalText(restOfTemplate);
		}
	}
	
	/**
	 * Converts a license template string to formatted HTML which highlights any 
	 * rules or tags
	 * @param licenseTemplate
	 * @return
	 * @throws LicenseTemplateRuleException 
	 */
	public static String templateTextToHtml(String licenseTemplate) throws LicenseTemplateRuleException {
		HtmlTemplateOutputHandler htmlOutput = new HtmlTemplateOutputHandler();
		parseTemplate(licenseTemplate, htmlOutput);
		return htmlOutput.getHtml();
	}

	/**
	 * Converts template text to standard default text using any default parameters in the rules
	 * @param template
	 * @return
	 * @throws LicenseTemplateRuleException 
	 */
	public static String templateToText(String template) throws LicenseTemplateRuleException {
		TextTemplateOutputHandler textOutput = new TextTemplateOutputHandler();
		parseTemplate(template, textOutput);
		return textOutput.getText();
	}
	
	public static String escapeHTML(String text) {
		String retval = StringEscapeUtils.escapeXml(text);
		return addHtmlFormatting(retval);
	}

	/**
	 * Adds HTML formatting <br> and <p>
	 * @param text
	 * @return
	 */
	public static String addHtmlFormatting(String text) {
		String[] lines = text.split("\n");
		StringBuilder result = new StringBuilder();
		result.append(lines[0]);
		boolean inParagraph = false;
		int i = 1;
		while (i < lines.length) {
			if (lines[i].trim().isEmpty()) {
				// paragraph boundary 
				if (inParagraph) {
					result.append("</p>");
				}
				result.append("\n");

				i++;
				if (i < lines.length) {
					if (lines[i].startsWith(FIVE_SPACES)) {
						result.append("<p style=\"margin-left: 20px;\">");
					} else {
						result.append("<p>");
					}
					result.append(lines[i++]);
				} else {
					result.append("<p>");
				}
				inParagraph = true;
			} else {
				// just a line break
				result.append("<br/>");
				result.append("\n");
				result.append(lines[i++]);
			}

		}
		if (inParagraph) {
			result.append("</p>");
		} else if (text.endsWith("\n")) {
			result.append("<br/>\n");
		}
		return result.toString();
	}

	/**
	 * Converts an HTML string to text preserving line breaks for <br/> tags
	 * @param html
	 * @return
	 */
	public static String HtmlToText(String html) {
		String newlineString = "NeWLineGoesHere";
		String replaceBrs = html.replaceAll("(?i)<br[^>]*>", newlineString);
		String replaceBrsAndPs = replaceBrs.replaceAll("(?i)<p[^>]*>", newlineString);
		Document doc = Jsoup.parse(replaceBrsAndPs);
		String retval  = doc.text();
		retval = retval.replace(newlineString, "\n");
		return retval;
	}

}