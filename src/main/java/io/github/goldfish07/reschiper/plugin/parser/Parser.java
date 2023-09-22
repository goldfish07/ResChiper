
package io.github.goldfish07.reschiper.plugin.parser;

import io.github.goldfish07.reschiper.plugin.parser.xml.FileFilterConfig;
import io.github.goldfish07.reschiper.plugin.parser.xml.ResChiperConfig;
import io.github.goldfish07.reschiper.plugin.parser.xml.StringFilterConfig;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.nio.file.Path;
import java.util.Iterator;

import static com.android.tools.build.bundletool.model.utils.files.FilePreconditions.checkFileExistsAndReadable;

/**
 * The Parser class provides methods for parsing configuration files and extracting specific settings.
 */
public class Parser {

    /**
     * The XML class provides methods to parse XML configuration files and extract specific settings.
     */
    public static class XML {

        private final Path configPath;

        /**
         * Constructs an XML parser with the specified configuration file path.
         *
         * @param configPath The path to the XML configuration files to be parsed.
         */
        public XML(Path configPath) {
            checkFileExistsAndReadable(configPath);
            this.configPath = configPath;
        }

        /**
         * Parses a file filter configuration from the XML document.
         *
         * @return A FileFilterConfig object containing file filter settings.
         * @throws DocumentException If there is an issue parsing the XML document.
         */
        public FileFilterConfig fileFilterParse() throws DocumentException {
            FileFilterConfig fileFilter = new FileFilterConfig();

            SAXReader reader = new SAXReader();
            Document doc = reader.read(configPath.toFile());
            Element root = doc.getRootElement();
            for (Iterator<Element> i = root.elementIterator("filter"); i.hasNext(); ) {
                Element element = i.next();
                String isActiveValue = element.attributeValue("isactive");
                if (isActiveValue != null && isActiveValue.equals("true")) {
                    fileFilter.setActive(true);
                }
                for (Iterator<Element> rules = element.elementIterator("rule"); rules.hasNext(); ) {
                    Element ruleElement = rules.next();
                    String rule = ruleElement.attributeValue("value");
                    if (rule != null) {
                        fileFilter.addRule(rule);
                    }
                }
            }
            return fileFilter;
        }

        /**
         * Parses a ResChiper configuration from the XML document.
         *
         * @return A ResChiperConfig object containing ResChiper settings.
         * @throws DocumentException If there is an issue parsing the XML document.
         */
        public ResChiperConfig resChiperParse() throws DocumentException {
            ResChiperConfig resChiperConfig = new ResChiperConfig();
            SAXReader reader = new SAXReader();
            Document doc = reader.read(configPath.toFile());
            Element root = doc.getRootElement();
            for (Iterator<Element> i = root.elementIterator("issue"); i.hasNext(); ) {
                Element element = i.next();
                String id = element.attributeValue("id");
                if (id == null || !id.equals("whitelist")) {
                    continue;
                }
                String isActive = element.attributeValue("isactive");
                if (isActive != null && isActive.equals("true")) {
                    resChiperConfig.setUseWhiteList(true);
                }
                for (Iterator<Element> rules = element.elementIterator("path"); rules.hasNext(); ) {
                    Element ruleElement = rules.next();
                    String rule = ruleElement.attributeValue("value");
                    if (rule != null) {
                        resChiperConfig.addWhiteList(rule);
                    }
                }
            }
            // File filter
            resChiperConfig.setFileFilter(fileFilterParse());
            // String filter
            resChiperConfig.setStringFilterConfig(stringFilterParse());
            return resChiperConfig;
        }

        /**
         * Parses a StringFilterConfig from the XML document.
         *
         * @return A StringFilterConfig object containing string filter settings.
         * @throws DocumentException If there is an issue parsing the XML document.
         */
        public StringFilterConfig stringFilterParse() throws DocumentException {
            StringFilterConfig config = new StringFilterConfig();
            SAXReader reader = new SAXReader();
            Document doc = reader.read(configPath.toFile());
            Element root = doc.getRootElement();

            for (Iterator<Element> i = root.elementIterator("filter-str"); i.hasNext(); ) {
                Element element = i.next();
                String isActive = element.attributeValue("isactive");
                if (isActive != null && isActive.equalsIgnoreCase("true")) {
                    config.setActive(true);
                }
                for (Iterator<Element> rules = element.elementIterator("path"); rules.hasNext(); ) {
                    Element ruleElement = rules.next();
                    String path = ruleElement.attributeValue("value");
                    config.setPath(path);
                }
                for (Iterator<Element> rules = element.elementIterator("language"); rules.hasNext(); ) {
                    Element ruleElement = rules.next();
                    String path = ruleElement.attributeValue("value");
                    config.getLanguageWhiteList().add(path);
                }
            }
            return config;
        }
    }
}