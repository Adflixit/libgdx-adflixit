package adf.gdx.app;

import adf.gdx.BaseGame;
import adf.gdx.XApi;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;

public class TestApp extends BaseGame {
  public TestApp() {
    super();
  }

  public TestApp(XApi xApi) {
    super(xApi);
  }

  @Override public void create() {
    super.create();
    loadSkin(internalFile("tests/data/uiskin.json"), internalFile("tests/data/textures.atlas"));
    loadProps("tests/data/cfg.properties");
    loadPrefs("test");
    Gdx.input.setCatchBackKey(true);
  }

  public static void launch(TestApp app) {
    new LwjglApplication(app, "Test app", 360, 640);
  }
}
