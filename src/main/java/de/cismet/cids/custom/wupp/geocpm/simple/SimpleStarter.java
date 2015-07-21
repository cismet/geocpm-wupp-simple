/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.custom.wupp.geocpm.simple;

import java.io.File;

import java.util.Collection;
import java.util.Properties;

import de.cismet.cids.custom.wupp.geocpm.OAB_FolderGeoCPMImportTransformer;
import de.cismet.cids.custom.wupp.geocpm.OAB_ZipGeoCPMImportTransformer;
import de.cismet.cids.custom.wupp.geocpm.OAB_ZustandMassnahme_PostgisSQLTransformer;

import de.cismet.geocpm.api.GeoCPMProject;
import de.cismet.geocpm.api.transform.GeoCPMEinPointToMemoryTransformer;
import de.cismet.geocpm.api.transform.GeoCPMEinTriangleToMemoryTransformer;
import de.cismet.geocpm.api.transform.GeoCPMImportTransformer;
import de.cismet.geocpm.api.transform.GeoCPMMaxToMemoryTransformer;
import de.cismet.geocpm.api.transform.GeoCPMResultElementsToMemoryTransformer;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  1.0
 */
public class SimpleStarter {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  args  DOCUMENT ME!
     */
    public static void main(final String[] args) {
        if ((args == null) || (args.length != 1)) {
            System.err.println("Benutzung: java -jar meine" + File.separator + "eingabe" + File.separator + "datei"
                        + File.separator + "oder" + File.separator + "ordner");
            System.exit(1);
        }

        final File arg = new File(args[0]);

        if (!arg.exists() || !arg.canRead()) {
            System.err.println("Kann die Eingabe nicht lesen: " + arg.getAbsolutePath());
            System.exit(2);
        }

        final Properties props = new Properties();
        props.put("log4j.appender.Console", "org.apache.log4j.ConsoleAppender");                     // NOI18N
        props.put("log4j.appender.Console.layout", "org.apache.log4j.PatternLayout");                // NOI18N
        props.put("log4j.appender.Console.layout.ConversionPattern", "%-4r [%t] %-5p %c %x - %m%n"); // NOI18N
        props.put("log4j.rootLogger", "WARN,Console");                                               // NOI18N
        org.apache.log4j.PropertyConfigurator.configure(props);

        final GeoCPMImportTransformer it;
        if (arg.getName().toLowerCase().endsWith(".zip")) {
            it = new OAB_ZipGeoCPMImportTransformer();
        } else {
            it = new OAB_FolderGeoCPMImportTransformer();
        }

        if (it.accept(arg)) {
            System.out.println("Transformiere Eingabe zu GeoCPM Projekten");
            final Collection<GeoCPMProject> l = it.transform(arg);

            final GeoCPMEinPointToMemoryTransformer t1 = new GeoCPMEinPointToMemoryTransformer();
            final GeoCPMEinTriangleToMemoryTransformer t2 = new GeoCPMEinTriangleToMemoryTransformer();
            final GeoCPMMaxToMemoryTransformer t3 = new GeoCPMMaxToMemoryTransformer();
            final GeoCPMResultElementsToMemoryTransformer t4 = new GeoCPMResultElementsToMemoryTransformer();
            final OAB_ZustandMassnahme_PostgisSQLTransformer t5 = new OAB_ZustandMassnahme_PostgisSQLTransformer();

            System.out.println("Starte Transformation von " + l.size() + " GeoCPM Projekten");
            for (final GeoCPMProject p : l) {
                System.out.println("Transformiere Punkte: " + p.getName());
                GeoCPMProject result = t1.transform(p);
                System.out.println("Transformiere Dreiecke: " + p.getName());
                result = t2.transform(result);
                System.out.println("Transformiere Max-Wasser: " + p.getName());
                result = t3.transform(result);
                System.out.println("Transformiere Wasser-Zeit: " + p.getName());
                result = t4.transform(result);
                System.out.println("Schreibe SQL: " + p.getName());
                result = t5.transform(result);
                result.setTriangles(null);
                result.setResults(null);
                result.setPoints(null);
                System.out.println("GeoCPM Projekt '" + p.getName() + "' abgeschlossen");
                System.out.println();
            }

            System.out.println("FERTIG");
            System.out.println();
        } else {
            System.err.println("Kein Transformierer für Eingabe-Datei gefunden: " + arg);
            System.exit(3);
        }
    }
}
