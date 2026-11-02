package com.campuscrib.ui;

import com.campuscrib.TestDb;
import com.campuscrib.config.AppConfig;
import com.campuscrib.db.Database;
import com.campuscrib.domain.User;
import com.campuscrib.seed.DemoData;
import com.campuscrib.service.Services;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.Clock;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * Renders the README screenshots from the real UI on demo data, without showing windows:
 * ./mvnw -q test-compile exec:java -Dexec.classpathScope=test -Dexec.mainClass=com.campuscrib.ui.ScreenshotTour -Dexec.args=docs/images
 */
public final class ScreenshotTour {
    private static final double SCALE = 2.0;

    private ScreenshotTour() {}

    public static void main(String[] args) throws Exception {
        File out = new File(args.length > 0 ? args[0] : "docs/images");
        out.mkdirs();
        Database db = TestDb.fresh();
        Services services = new Services(db, Clock.systemDefaultZone());
        DemoData.load(db, services);
        AppConfig demo = new AppConfig(AppConfig.DEMO_URL, "sa", "", true);

        SwingUtilities.invokeAndWait(() -> {
            Theme.install();
            App app = new App(services, demo);
            JFrame frame = app.frame();
            frame.setSize(1320, 840);
            frame.addNotify();
            try {
                save(frame, new File(out, "login.png"));
                shot(app, services, "ananya", "Find a place", new File(out, "student-search.png"));
                Shell student = app.showShell(login(services, "ananya"));
                Window detail = ((SearchScreen) student.screens().get(0)).openFirst();
                detail.addNotify();
                save(detail, new File(out, "student-apartment.png"));
                detail.dispose();
                shot(app, services, "ananya", "My bookings", new File(out, "student-bookings.png"));
                shot(app, services, "sofia", "Booking requests", new File(out, "agent-inbox.png"));
                shot(app, services, "sofia", "My listings", new File(out, "agent-listings.png"));
                shot(app, services, "jordan", "My requests", new File(out, "resident-requests.png"));
                Shell resident = app.showShell(login(services, "jordan"));
                Window form = new NewRequestDialog(frame, services, login(services, "jordan"));
                form.addNotify();
                save(form, new File(out, "resident-new-request.png"));
                form.dispose();
                resident.show(0);
                shot(app, services, "ravi", "Work queue", new File(out, "crew-queue.png"));
                shot(app, services, "helen", "All requests", new File(out, "manager-requests.png"));
                shot(app, services, "helen", "Reports", new File(out, "manager-reports.png"));
                shot(app, services, "admin", "People", new File(out, "admin-people.png"));
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
            frame.dispose();
        });
        System.out.println("Screenshots written to " + out.getAbsolutePath());
        System.exit(0);
    }

    private static User login(Services s, String username) {
        return s.auth.login(username, DemoData.PASSWORD);
    }

    private static void shot(App app, Services services, String username, String screen, File file) throws Exception {
        Shell shell = app.showShell(login(services, username));
        shell.show(screen);
        save(app.frame(), file);
    }

    private static void save(Window window, File file) throws Exception {
        Container content = window instanceof JFrame f ? f.getContentPane() : ((javax.swing.RootPaneContainer) window).getContentPane();
        window.validate();
        layoutAll(content);
        int w = content.getWidth();
        int h = content.getHeight();
        BufferedImage img = new BufferedImage((int) (w * SCALE), (int) (h * SCALE), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.scale(SCALE, SCALE);
        content.printAll(g);
        g.dispose();
        ImageIO.write(img, "png", file);
    }

    private static void layoutAll(Container c) {
        c.doLayout();
        for (var child : c.getComponents()) {
            if (child instanceof Container cc) layoutAll(cc);
        }
    }
}
