package socktalk.client;

import java.awt.GraphicsEnvironment;
import java.awt.Window;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.SwingUtilities;
import socktalk.client.ui.LoginFrame;

/**
 * Dependency-free startup regression test: run main with JDK 21 and a graphical display.
 * Run in a dedicated JVM, since it opens and disposes the application's login window.
 */
public final class ClientMainTest {
    public static void main(String[] args) throws Exception {
        exposesJavaLauncherEntryPoint();
        opensLoginAsynchronouslyOnTheEventDispatchThread();
        System.out.println("ClientMainTest: 2 tests passed");
    }

    private static void exposesJavaLauncherEntryPoint() throws Exception {
        // Regression for the previous typo, mai(String[]), which the Java launcher cannot invoke.
        Method main = ClientMain.class.getDeclaredMethod("main", String[].class);
        check(Modifier.isPublic(main.getModifiers()), "Entry point must be public");
        check(Modifier.isStatic(main.getModifiers()), "Entry point must be static");
        check(main.getReturnType() == void.class, "Entry point must return void");
    }

    private static void opensLoginAsynchronouslyOnTheEventDispatchThread() throws Exception {
        check(!GraphicsEnvironment.isHeadless(), "Startup test requires a graphical display");
        List<Window> originalWindows = new ArrayList<>();
        try {
            SwingUtilities.invokeAndWait(() -> {
                originalWindows.addAll(Arrays.asList(Window.getWindows()));
                ClientMain.main(new String[0]);
                check(newLoginFrames(originalWindows).isEmpty(),
                        "main must queue window creation rather than create it synchronously");
            });
            // A second EDT task runs after the window creation queued by main.
            SwingUtilities.invokeAndWait(() -> {
                List<LoginFrame> frames = newLoginFrames(originalWindows);
                check(frames.size() == 1, "Startup must create exactly one login window");
                check(frames.get(0).isVisible(), "Startup must show the login window");
            });
        } finally {
            SwingUtilities.invokeAndWait(() -> {
                for (Window window : Window.getWindows()) {
                    if (!originalWindows.contains(window)) {
                        window.dispose();
                    }
                }
            });
        }
    }

    private static List<LoginFrame> newLoginFrames(List<Window> originalWindows) {
        List<LoginFrame> frames = new ArrayList<>();
        for (Window window : Window.getWindows()) {
            if (!originalWindows.contains(window) && window instanceof LoginFrame frame) {
                frames.add(frame);
            }
        }
        return frames;
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
