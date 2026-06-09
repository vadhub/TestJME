import com.abg.testjem.MainGame;
import com.jme3.system.AppSettings;

public class DesktopLauncher {
    public static void main(String[] args) {
        MainGame app = new MainGame();
        AppSettings settings = new AppSettings(true);
        settings.setResolution(1280, 720);
        app.setSettings(settings);
        app.start();
    }
}