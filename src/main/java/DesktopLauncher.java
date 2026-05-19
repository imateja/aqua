import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

public class DesktopLauncher {

    private static final int WIDTH=800, HEIGHT=600;
    private static final boolean VSYNC_TOGGLE = true;

    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config =  new Lwjgl3ApplicationConfiguration();
        config.setTitle("triangle-mesh");
        config.setWindowedMode(WIDTH,HEIGHT);
        config.useVsync(VSYNC_TOGGLE);

        new Lwjgl3Application(new AquaEngine(), config);
    }
}
