package com.campuscrib;

import com.campuscrib.config.AppConfig;
import com.campuscrib.db.Database;
import com.campuscrib.db.Migrator;
import com.campuscrib.seed.DemoData;
import com.campuscrib.service.Services;
import com.campuscrib.ui.App;
import com.campuscrib.ui.Theme;
import java.time.Clock;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 * Starts CampusCrib: connect, apply any new migrations, load demo data into an empty database,
 * then open the window. Pass --reset-demo to wipe and reload the embedded demo database.
 */
public final class Main {
    private Main() {}

    public static void main(String[] args) {
        AppConfig config = AppConfig.load();
        SwingUtilities.invokeLater(() -> {
            Theme.install();
            try {
                Database db = new Database(config.dbUrl(), config.dbUser(), config.dbPassword());
                if (config.demo() && java.util.Arrays.asList(args).contains("--reset-demo")) resetDemo(db);
                Migrator.migrate(db);
                Services services = new Services(db, Clock.systemDefaultZone());
                if (DemoData.isEmpty(services)) DemoData.load(db, services);
                new App(services, config).frame().setVisible(true);
            } catch (RuntimeException e) {
                JOptionPane.showMessageDialog(null,
                        "CampusCrib couldn't start with " + config.describe() + ".\n\n" + rootMessage(e)
                                + "\n\nCheck campuscrib.properties, or remove it to use the embedded demo database.",
                        "CampusCrib", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }

    private static void resetDemo(Database db) {
        db.withConnection(c -> {
            try (var s = c.createStatement()) {
                s.execute("DROP ALL OBJECTS");
            }
            return null;
        });
    }

    private static String rootMessage(Throwable e) {
        Throwable t = e;
        while (t.getCause() != null) t = t.getCause();
        return t.getMessage();
    }
}
