package org.mycore.ubo.export;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.common.MCRException;
import org.mycore.common.content.MCRContent;
import org.mycore.common.content.transformer.MCRContentTransformer;
import org.mycore.common.content.transformer.MCRContentTransformerFactory;
import org.mycore.frontend.basket.MCRBasket;
import org.mycore.frontend.basket.MCRBasketEntry;
import org.mycore.frontend.basket.MCRBasketManager;
import org.mycore.frontend.cli.MCRObjectCommands;
import org.mycore.frontend.cli.annotation.MCRCommand;
import org.mycore.frontend.cli.annotation.MCRCommandGroup;
import org.mycore.frontend.export.MCRExportCollection;

/**
 * @author Frank L\u00FCtzenkirchen
 */
@MCRCommandGroup(name = "UBO export commands")
public class MCRExportCommands {

    private static final Logger LOGGER = LogManager.getLogger();

    @MCRCommand(
        syntax = "ubo export selected using transformer {0} to file {1}",
        help = "Exports previously selected objects using the transformer with the ID {0} and writes the output to file {1}",
        order = 1)
    public static void export(String transformerID, String pathOfOutputFile) throws IOException {
        List<String> objectIDs = MCRObjectCommands.getSelectedObjectIDs();
        LOGGER.info("Exporting {} objects using transformer {}...", objectIDs.size(), transformerID);

        MCRContent source = buildExportCollection(objectIDs);
        MCRContentTransformer transformer = MCRContentTransformerFactory.getTransformer(transformerID);
        MCRContent result = transformer.transform(source);

        LOGGER.info("Writing transformed output to file {}", pathOfOutputFile);
        result.sendTo(new File(pathOfOutputFile));
    }

    private static MCRContent buildExportCollection(List<String> objectIDs) {
        MCRBasket basket = MCRBasketManager.getOrCreateBasketInSession("objects");
        objectIDs.forEach(objectID -> {
            basket.add(new MCRBasketEntry(objectID, "mcrobject:" + objectID));
        });

        MCRExportCollection collection = new MCRExportCollection();
        try {
            collection.add(basket);
        } catch (Exception ex) {
            throw new MCRException(ex);
        }

        basket.clear();
        return collection.getContent();
    }
}
