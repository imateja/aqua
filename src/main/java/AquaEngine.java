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

    private ArtMesh mesh;

    private static final int BUFF_SIZE=256;
    private static final int PORT=9000;

    // network code
    private volatile float externalParamX = 0.5f;
    private volatile float externalParamY = 0.5f;

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
        mesh = new ArtMesh("star.png");

        //there needs to be a network thread cuz i don't want my whole app to stop while it waits for params from webcam/udp tester
        networkThread = new Thread(() -> {
            try {
                //udp is more suitable for my use-case, don't care about validity of every package
                DatagramSocket socket = new DatagramSocket(PORT);
                byte[] buffer = new byte[BUFF_SIZE];

                System.out.println("listening on 9000");

                while (isRunning) {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet); //blocking func call
                    String data = new String(packet.getData(), 0, packet.getLength()).trim();
                    try {
                        // Split the incoming message (expecting "paramX,paramY" format)
                        String[] parts = data.split(",");
                        if (parts.length == 2) {
                            float rawX = Float.parseFloat(parts[0]);
                            float rawY = Float.parseFloat(parts[1]);

                            // Clamp between 0.0 and 1.0 to prevent crazy mesh explosions
                            externalParamX = Math.max(0.0f, Math.min(1.0f, rawX));
                            externalParamY = Math.max(0.0f, Math.min(1.0f, rawY));
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

        networkThread.setDaemon(true); //daemon threads get killed when their parent program dies. i dont want more packets after i shut down the engine.
        networkThread.start();
    }

    @Override
    public void render() {
        // Clear the screen every frame so previous frames don't smear
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        float turnMultiplierX = (externalParamX - 0.5f) * 2.0f;
        float turnMultiplierY = (externalParamY - 0.5f) * 2.0f;

        mesh.update(turnMultiplierX, turnMultiplierY);

        shader.bind();
        shader.setUniformMatrix("u_projTrans", camera.combined);
        //the pic that im rendering is known as 0 in gpu
        shader.setUniformi("u_texture", 0);

        //draw it finally
        mesh.render(shader);
    }

    @Override
    public void dispose() {
        disableThread();
        shader.dispose();
        mesh.dispose();
    }
}