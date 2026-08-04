import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector3;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class AquaEngine extends ApplicationAdapter {

    private ShaderProgram shader;
    private OrthographicCamera camera;
    private static final int CAMWIDTH=2, CAMHEIGHT=2;

    private ArtMesh headMesh;
    private ArtMesh noseMesh;
    private ArtMesh eyesMesh;
    private ArtMesh eyebrowsMesh;
    private ArtMesh mouthMesh;

    private static final int BUFF_SIZE=256;
    private static final int PORT=9000;

    // network params
    private volatile float externalParamX = 0.5f;
    private volatile float externalParamY = 0.5f;
    private volatile float externalBlink = 1.0f; // 1.0 = open, 0.0 = closed

    private boolean isRunning = true;
    private Thread networkThread;

    private void disableThread() {
        isRunning = false;
        return;
    }

    @Override
    public void create() {
        shader = SpriteBatch.createDefaultShader();
        camera = new OrthographicCamera(CAMWIDTH, CAMHEIGHT);

        // example = new ArtMesh("asset.png", offsetX, offsetY, scale)
        headMesh     = new ArtMesh("head.png",     0.0f,  0.0f, 1.0f);
        noseMesh     = new ArtMesh("nose.png",     0.0f, 0.0f, 1.0f);
        eyesMesh     = new ArtMesh("eyes.png",     0.0f,  -0.02f, 1.0f);
        eyebrowsMesh = new ArtMesh("eyebrows.png", 0.0f,  0.0f,  1.0f);
        mouthMesh    = new ArtMesh("mouth.png",    0.0f, -0.006f,  1.0f);

        // Background network thread to listen for UDP face tracking data from Python
        networkThread = new Thread(() -> {
            try {
                // UDP is more suitable for my use-case, don't care about validity of every package
                DatagramSocket socket = new DatagramSocket(PORT);
                byte[] buffer = new byte[BUFF_SIZE];

                System.out.println("listening on 9000");

                while (isRunning) {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet); // blocking func
                    String data = new String(packet.getData(), 0, packet.getLength()).trim();
                    try {
                        String[] parts = data.split(",");
                        if (parts.length >= 2) {
                            float rawX = Float.parseFloat(parts[0]);
                            float rawY = Float.parseFloat(parts[1]);

                            // Clamp between 0.0 and 1.0 to prevent mesh explosions
                            externalParamX = Math.max(0.0f, Math.min(1.0f, rawX));
                            externalParamY = Math.max(0.0f, Math.min(1.0f, rawY));
                        }
                        if (parts.length >= 3) {
                            float rawBlink = Float.parseFloat(parts[2]);
                            externalBlink = Math.max(0.0f, Math.min(1.0f, rawBlink));
                        }
                    } catch (Exception e) {
                        System.out.println("Received junk data: " + data);
                    }
                }
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // daemon threads get killed when their parent program dies. i don't want more packets after I shut down the engine.
        networkThread.setDaemon(true);
        networkThread.start();
    }

    @Override
    public void render() {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        float turnX = (externalParamX - 0.5f) * 2.0f;
        float turnY = (externalParamY - 0.5f) * 2.0f;

        // multipliers affect how much will a component of a characters face move as i move my head on camera
        headMesh.update(turnX * 0.8f, turnY * 0.8f);
        noseMesh.update(turnX * 0.85f, turnY * 0.85f);
        eyesMesh.update(turnX * 1.0f, turnY * 1.0f, externalBlink);
        eyebrowsMesh.update(turnX * 1.1f, turnY * 1.1f);
        mouthMesh.update(turnX * 0.9f, turnY * 0.9f);

        shader.bind();
        shader.setUniformMatrix("u_projTrans", camera.combined);
        shader.setUniformi("u_texture", 0);

        headMesh.render(shader);
        noseMesh.render(shader);
        eyesMesh.render(shader);
        eyebrowsMesh.render(shader);
        mouthMesh.render(shader);
    }

    @Override
    public void dispose() {
        disableThread();
        shader.dispose();
        headMesh.dispose();
        noseMesh.dispose();
        eyesMesh.dispose();
        eyebrowsMesh.dispose();
        mouthMesh.dispose();
    }
}