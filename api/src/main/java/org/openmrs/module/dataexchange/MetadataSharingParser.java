package org.openmrs.module.dataexchange;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

@Component
public class MetadataSharingParser {

	public Set<Integer> parseConceptIds(InputStream inputStream) {
		Set<Integer> ids = new HashSet<>();
		try (ZipInputStream in = new ZipInputStream(inputStream)) {
			ZipEntry entry;
			while ((entry = in.getNextEntry()) != null) {
				if (entry.getName().equals("header.xml")) {
					ids.addAll(parseHeader(in));
					return ids;
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		return ids;
    }

	private Set<Integer> parseHeader(ZipInputStream in) {
		Set<Integer> ids = new HashSet<>();
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(in);
			XPathFactory xPathfactory = XPathFactory.newInstance();
			XPath xpath = xPathfactory.newXPath();
			for (String type: Arrays.asList("org.openmrs.Concept", "org.openmrs.ConceptNumeric", "org.openmrs.ConceptComplex")) {
				XPathExpression itemsExpr = xpath.compile("/package/items/org.openmrs.module.metadatasharing.Item[classname='" + type + "']/id");
				addIds(ids, doc, itemsExpr);

				XPathExpression relatedItemsExpr = xpath.compile("/package/relatedItems/org.openmrs.module.metadatasharing.Item[classname='" + type + "']/id");
				addIds(ids, doc, relatedItemsExpr);
			}
		} catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException e) {
			throw new RuntimeException(e);
		}
		return ids;
	}

	private void addIds(Set<Integer> ids, Document doc, XPathExpression itemsExpr) throws XPathExpressionException {
		NodeList nodes = (NodeList) itemsExpr.evaluate(doc, XPathConstants.NODESET);
		for (int i = 0; i < nodes.getLength(); i++)
			ids.add(Integer.valueOf(nodes.item(i).getFirstChild().getNodeValue()));
	}

}
