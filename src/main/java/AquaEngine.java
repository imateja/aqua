import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector3;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
public class AquaEngine extends ApplicationAdapter {

    private Texture texture;
    private ShaderProgram shader;
    private Mesh mesh;

    private static final boolean IS_MESH_STATIC=true;
    private static final int NUM_VERTICES=9, NUM_INDICES = 24;

    private OrthographicCamera camera;
    private static final int CAMWIDTH=2, CAMHEIGHT=2;
    private final int CENTER_VERTEX_INDEX = 20;

    private static final float c = com.badlogic.gdx.graphics.Color.WHITE.toFloatBits();
    private float[] vertices = new float[] { // X,Y,color,texU,texV
            -0.5f, -0.5f, c, 0.0f, 1.0f,
            0.0f, -0.5f, c, 0.5f, 1.0f,
            0.5f, -0.5f, c, 1.0f, 1.0f,

            -0.5f,  0.0f, c, 0.0f, 0.5f,
            0.0f,  0.0f, c, 0.5f, 0.5f,
            0.5f,  0.0f, c, 1.0f, 0.5f,

            -0.5f,  0.5f, c, 0.0f, 0.0f,
            0.0f,  0.5f, c, 0.5f, 0.0f,
            0.5f,  0.5f, c, 1.0f, 0.0f
    };

    private static final short[] indices = new short[] {
            0, 1, 4,  0, 4, 3, // Bottom-left quad
            1, 2, 5,  1, 5, 4, // Bottom-right quad
            3, 4, 7,  3, 7, 6, // Top-left quad
            4, 5, 8,  4, 8, 7  // Top-right quad
    };


    private static final int BUFF_SIZE=256;
    private static final int PORT=9000;
    private volatile float externalParam = 0.5f;
    private boolean isRunning = true;
    private Thread networkThread;

    private void disableThread() {
        isRunning = false;
        return;
    }

    @Override
    public void create() {
        texture = new Texture(Gdx.files.internal("star.png"));
        shader = SpriteBatch.createDefaultShader();
        mesh = new Mesh(IS_MESH_STATIC, NUM_VERTICES, NUM_INDICES,
                new VertexAttribute(VertexAttributes.Usage.Position, 2, "a_position"),
                new VertexAttribute(VertexAttributes.Usage.ColorPacked, 4, "a_color"),
                new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, "a_texCoord0"));

        mesh.setVertices(vertices);
        mesh.setIndices(indices);

        camera = new OrthographicCamera(CAMWIDTH, CAMHEIGHT);

        //there needs to be a network thread cuz i don't want my whole app to stop while it waits for params from camera
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
                        float newValue = Float.parseFloat(data);
                        // Clamp between 0.0 and 1.0 to prevent crazy mesh explosions
                        externalParam = Math.max(0.0f, Math.min(1.0f, newValue));
                    } catch (NumberFormatException e) {
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
        Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();

        float param = externalParam;

        // convert 0.0 -> 1.0 into a turn multiplier of -1.0 -> 1.0
        float turnMultiplier = (param - 0.5f) * 2.0f;

        int[] middleColumnXIndices = {5, 20, 35};
        //why these numbers? every vertex is X,Y,color,TexX,TexY.
        //i am warping the spine of my image(vertices 1,4 and 7) and i want X coords of those vertices.
        // since every vertex is 5 numbers, then X of vertex 1 would be the 5th number in 1D array(which is how RAM sees this)
        //X coord of vertex 4 would be the 20th number, and so on.

        float maxStretch = 0.35f;

        for (int index : middleColumnXIndices) {
            vertices[index] = turnMultiplier * maxStretch;
        }

        // Push the mutated array back to the GPU
        mesh.setVertices(vertices);

        texture.bind();
        shader.bind();
        shader.setUniformMatrix("u_projTrans", camera.combined);
        shader.setUniformi("u_texture", 0);

        mesh.render(shader, GL20.GL_TRIANGLES);
    }

    @Override
    public void dispose() {
        disableThread();
        texture.dispose();
        mesh.dispose();
        shader.dispose();
    }
}
