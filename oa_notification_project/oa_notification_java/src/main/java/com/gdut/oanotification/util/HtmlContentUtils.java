package com.gdut.oanotification.util;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class HtmlContentUtils {

    private static final Pattern IMG_SRC_PATTERN = Pattern.compile("<img[^>]+src=[\"'](?<src>[^\"']+)[\"']", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern CONTENT_DIV_PATTERN = Pattern.compile("<div[^>]+id=[\"']content[\"'][^>]*>(?<body>.*?)</div>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern SRC_ATTR_PATTERN = Pattern.compile("src=([\"'])(?<src>.*?)\\1", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern HREF_ATTR_PATTERN = Pattern.compile("href=([\"'])(?<href>.*?)\\1", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern IMG_TAG_PATTERN = Pattern.compile("<img(?<attrs>[^>]*)>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern LINK_TAG_PATTERN = Pattern.compile("<a\\b[^>]*href=([\"'])(?<href>.*?)\\1[^>]*>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private HtmlContentUtils() {
    }

    public static List<String> extractImageUrls(String contentHtml, String baseUrl) {
        if (contentHtml == null || contentHtml.isBlank()) {
            return List.of();
        }
        Matcher matcher = IMG_SRC_PATTERN.matcher(contentHtml);
        Set<String> urls = new LinkedHashSet<>();
        while (matcher.find()) {
            String src = toAbsoluteUrl(matcher.group("src"), baseUrl);
            if (!src.isBlank()) {
                urls.add(src);
            }
        }
        return new ArrayList<>(urls);
    }

    public static String buildMiniappContentHtml(String contentHtml, String baseUrl) {
        if (contentHtml == null || contentHtml.isBlank()) {
            return "";
        }
        String html = contentHtml;
        Matcher contentMatcher = CONTENT_DIV_PATTERN.matcher(html);
        if (contentMatcher.find()) {
            html = contentMatcher.group("body");
        }

        html = html.replaceAll("(?is)<script\\b[^>]*>.*?</script>", "");
        html = html.replaceAll("(?is)<style\\b[^>]*>.*?</style>", "");
        html = html.replaceAll("(?is)</?(table|tbody|tr|td|colgroup)\\b[^>]*>", "");
        html = html.replaceAll("(?is)<col\\b[^>]*>", "");
        html = html.replace("&nbsp;", " ");
        html = replaceAttributes(html, SRC_ATTR_PATTERN, "src", baseUrl);
        html = replaceAttributes(html, HREF_ATTR_PATTERN, "href", baseUrl);
        html = replaceImages(html, baseUrl);
        html = replaceLinkTags(html, baseUrl);
        html = html.replaceAll("(?is)<div\\b[^>]*>", "<p>");
        html = html.replaceAll("(?is)</div>", "</p>");
        html = html.replaceAll("(?is)<p\\b[^>]*>", "<p style=\"margin:0 0 14px;line-height:1.95;text-align:justify;text-indent:2em;font-size:16px;color:#3f3f46;\">");
        html = html.replaceAll("(?is)<br\\s*/?>", "<br/>");
        html = html.replaceAll("(?is)<(strong|b)\\b[^>]*>", "<strong>");
        html = html.replaceAll("(?is)</(strong|b)>", "</strong>");
        html = html.replaceAll("(?is)<(em|i)\\b[^>]*>", "<em>");
        html = html.replaceAll("(?is)</(em|i)>", "</em>");
        html = html.replaceAll("(?is)<p[^>]*>\\s*</p>", "");
        html = html.replaceAll("(?is)<(?!/?(p|br|img|strong|em|a)\\b)[^>]+>", "");
        html = html.replaceAll("\\s+", " ");
        html = html.replaceAll(">\\s+<", "><");
        return html;
    }

    private static String replaceAttributes(String html, Pattern pattern, String attrName, String baseUrl) {
        Matcher matcher = pattern.matcher(html);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String value = "src".equals(attrName) ? matcher.group("src") : matcher.group("href");
            String replacement = attrName + "=\"" + Matcher.quoteReplacement(toAbsoluteUrl(value, baseUrl)) + "\"";
            matcher.appendReplacement(buffer, replacement);
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    private static String replaceImages(String html, String baseUrl) {
        Matcher matcher = IMG_TAG_PATTERN.matcher(html);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            Matcher srcMatcher = SRC_ATTR_PATTERN.matcher(matcher.group("attrs"));
            if (!srcMatcher.find()) {
                matcher.appendReplacement(buffer, "");
                continue;
            }
            String src = toAbsoluteUrl(srcMatcher.group("src"), baseUrl);
            String replacement = "<img src=\"" + Matcher.quoteReplacement(src)
                + "\" mode=\"widthFix\" style=\"display:block;max-width:88%;height:auto;margin:20px auto;border-radius:10px;\">";
            matcher.appendReplacement(buffer, replacement);
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    private static String replaceLinkTags(String html, String baseUrl) {
        Matcher matcher = LINK_TAG_PATTERN.matcher(html);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String href = toAbsoluteUrl(matcher.group("href"), baseUrl);
            String replacement = "<a href=\"" + Matcher.quoteReplacement(href) + "\" style=\"color:#2563eb;\">";
            matcher.appendReplacement(buffer, replacement);
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    private static String toAbsoluteUrl(String path, String baseUrl) {
        if (path == null || path.isBlank()) {
            return "";
        }
        if (path.startsWith("http://") || path.startsWith("https://")) {
            return path;
        }
        if (baseUrl == null || baseUrl.isBlank()) {
            return path;
        }
        return baseUrl + path;
    }
}
