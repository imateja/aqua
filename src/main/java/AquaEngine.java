import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public class AquaEngine extends ApplicationAdapter {

    private Texture texture;
    private ShaderProgram shader;
    private Mesh mesh;

    private static final boolean isMeshStatic=true;
    private static final int numVertices=9, numIndices = 24;

    private static final float c = com.badlogic.gdx.graphics.Color.WHITE.toFloatBits();
    private static final float[] vertices = new float[] { // X,Y,color,texU,texV
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

    @Override
    public void create() {
        texture = new Texture(Gdx.files.internal("star.png"));
        shader = SpriteBatch.createDefaultShader();
        mesh = new Mesh(isMeshStatic, numVertices, numIndices,
                new VertexAttribute(VertexAttributes.Usage.Position, 2, "a_position"),
                new VertexAttribute(VertexAttributes.Usage.ColorPacked, 4, "a_color"),
                new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, "a_texCoord0"));

        mesh.setVertices(vertices);
        mesh.setIndices(indices);
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        texture.bind();
        shader.bind();

        shader.setUniformMatrix("u_projTrans", new com.badlogic.gdx.math.Matrix4().setToOrtho2D(-1, -1, 2, 2));
        //the pic that im rendering is known as 0 in gpu
        shader.setUniformi("u_texture", 0);

        mesh.render(shader, GL20.GL_TRIANGLES);
    }

    @Override
    public void dispose() {

        texture.dispose();
        mesh.dispose();
        shader.dispose();
    }
}
