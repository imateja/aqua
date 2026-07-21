import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

//if i want more meshes, i don't have to crowd my engine with new vertices and more code
//i can just say new_mesh = new ArtMesh() and be done with it and keep all the logic here

public class ArtMesh {
    private Texture texture;
    private Mesh mesh;

    private float[] baseVertices;
    private float[] currentVertices;
    private short[] indices;

    //why these numbers? every vertex is X,Y,color,TexX,TexY.
    //i am warping the spine of my image(vertices 1,4 and 7 - middle column) and i want X coords of those vertices.
    // since every vertex is 5 numbers, then X of vertex 1 would be the 5th number in 1D array
    //RAM sees these numbers as a 1D array - important!
    //X coord of vertex 4 would be the 20th number, and so on

    private final int[] middleColumnXIndices = {5, 20, 35};
    //similar for Y indices but it's the middle row now, not middle column
    private final int[] middleRowYIndices = {16, 21, 26};
    private final float maxStretch = 0.35f;

    // Constructor: When we type 'new ArtMesh()', it builds the grid automatically
    public ArtMesh(String imagePath) {
        texture = new Texture(Gdx.files.internal(imagePath));

        float c = Color.WHITE.toFloatBits();
        baseVertices = new float[] {
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

        // Create our working copy
        currentVertices = baseVertices.clone();

        indices = new short[] {
                0, 1, 4,  0, 4, 3,
                1, 2, 5,  1, 5, 4,
                3, 4, 7,  3, 7, 6,
                4, 5, 8,  4, 8, 7
        };

        mesh = new Mesh(true, 9, 24,
                new VertexAttribute(VertexAttributes.Usage.Position, 2, "a_position"),
                new VertexAttribute(VertexAttributes.Usage.ColorPacked, 4, "a_color"),
                new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, "a_texCoord0"));

        mesh.setIndices(indices);
    }

    // The engine just hands this object a multiplier, and it handles its own internal math
    public void update(float turnMultiplierX, float turnMultiplierY) {
        // Reset to baseline so math errors don't accumulate
        System.arraycopy(baseVertices, 0, currentVertices, 0, baseVertices.length);

        // Apply Horizontal Warp
        for (int index : middleColumnXIndices) {
            currentVertices[index] += turnMultiplierX * maxStretch;
        }

        // Apply Vertical Warp
        for (int index : middleRowYIndices) {
            currentVertices[index] += turnMultiplierY * maxStretch;
        }

        mesh.setVertices(currentVertices);
    }

    // The engine tells it when to draw itself
    public void render(ShaderProgram shader) {
        texture.bind();
        mesh.render(shader, GL20.GL_TRIANGLES);
    }

    // Clean up GPU memory
    public void dispose() {
        texture.dispose();
        mesh.dispose();
    }
}