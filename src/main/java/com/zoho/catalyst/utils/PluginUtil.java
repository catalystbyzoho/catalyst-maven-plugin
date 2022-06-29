package com.zoho.catalyst.utils;

import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

public class PluginUtil {
    public static Xpp3Dom xmlToConfig(String xml) throws XmlPullParserException, IOException {
        if(xml == null || xml.isEmpty()) {
            return new Xpp3Dom("configuration");
        }
        try (Reader xmlReader = new StringReader(xml)) {
            Xpp3Dom dom = Xpp3DomBuilder.build(xmlReader);
            String rootName = dom.getName();
            if (!rootName.equalsIgnoreCase("configuration")) {
                return xmlToConfig("<configuration>" + xml + "</configuration>");
            }
            return dom;
        }
    }
}
