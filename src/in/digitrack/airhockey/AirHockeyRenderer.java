package in.digitrack.airhockey;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_LINES;
import static android.opengl.GLES20.GL_POINTS;
import static android.opengl.GLES20.GL_TRIANGLE_FAN;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;
import static android.opengl.GLES20.*;
import in.digitrack.airhockey.util.LoggerConfig;
import in.digitrack.airhockey.util.ShaderHelper;
import in.digitrack.airhockey.util.TextResourceReader;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.Matrix;

public class AirHockeyRenderer implements Renderer {

	private static final int POSITION_COMPONENT_COUNT = 2;
	private static final int COLOR_COMPONENT_COUNT = 3;
	private static final int BYTES_PER_FLOAT = 4;
	private final FloatBuffer vertexData;
	private final Context context;
	private int program;
	
	private static final String U_MATRIX = "u_Matrix";
	private final float[] projectionMatrix = new float[16];
	private int uMatrixLocation;
	private static final String A_COLOR = "a_Color";
	private int aColorLocation;
	private final String A_POSITION = "a_Position";
	private int aPositionLocation;
	private static final int STRIDE =
			(POSITION_COMPONENT_COUNT + COLOR_COMPONENT_COUNT) * BYTES_PER_FLOAT;
	
	public AirHockeyRenderer(Context context) {
		float[] tableVerticesWithTriangles = {
				// Order of coordinates: X, Y, R, G, B
				// Triangle Fan
				0f, 0f, 1f, 1f, 1f,
				-0.5f, -0.8f, 0.7f, 0.7f, 0.7f,
				 0.5f, -0.8f, 0.7f, 0.7f, 0.7f,
				 0.5f,  0.8f, 0.7f, 0.7f, 0.7f,
				-0.5f,  0.8f, 0.7f, 0.7f, 0.7f,
				-0.5f, -0.8f, 0.7f, 0.7f, 0.7f,
				// Line 1
				-0.5f, 0f, 1f, 0f, 0f,
				0.5f, 0f, 1f, 0f, 0f,
				// Mallets
				0f, -0.4f, 0f, 0f, 1f,
				0f,  0.4f, 1f, 0f, 0f
		};
		
		vertexData = ByteBuffer.allocateDirect(BYTES_PER_FLOAT * tableVerticesWithTriangles.length)
							   .order(ByteOrder.nativeOrder())
							   .asFloatBuffer();
		vertexData.put(tableVerticesWithTriangles);
		
		this.context = context;
	}
	
	@Override
	public void onDrawFrame(GL10 arg0) {
		glClear(GL_COLOR_BUFFER_BIT);
		glUniformMatrix4fv(uMatrixLocation, 1, false, projectionMatrix, 0);
		
		glDrawArrays(GL_TRIANGLE_FAN, 0, 6);
		glDrawArrays(GL_LINES, 6, 2);
		// Draw the first mallet.
		glDrawArrays(GL_POINTS, 8, 1);
		// Draw the second mallet.
		glDrawArrays(GL_POINTS, 9, 1);
	}

	@Override
	public void onSurfaceChanged(GL10 arg0, int width, int height) {
		glViewport(0, 0, width, height);
		final float aspectRatio = width > height ?
			(float) width / (float) height :
			(float) height / (float) width;
			if (width > height) {
				// Landscape
				Matrix.orthoM(projectionMatrix, 0, -aspectRatio, aspectRatio, -1f, 1f, -1f, 1f);
			} else {
				// Portrait or square
				Matrix.orthoM(projectionMatrix, 0, -1f, 1f, -aspectRatio, aspectRatio, -1f, 1f);
			}
	}

	@Override
	public void onSurfaceCreated(GL10 arg0, EGLConfig arg1) {
		glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		String vertexShaderSource = TextResourceReader.readTextFileFromResource(context, R.raw.simple_vertex_shader);
		String fragmentShaderSource = TextResourceReader.readTextFileFromResource(context, R.raw.simple_fragment_shader);
		int vertexShader = ShaderHelper.compileVertexShader(vertexShaderSource);
		int fragmentShader = ShaderHelper.compileFragmentShader(fragmentShaderSource);
		program = ShaderHelper.linkProgram(vertexShader, fragmentShader);
		
		if(LoggerConfig.ON) {
			ShaderHelper.validateProgram(program);
		}
		glUseProgram(program);
		
		uMatrixLocation = glGetUniformLocation(program, U_MATRIX);
		aColorLocation = glGetAttribLocation(program, A_COLOR);
		aPositionLocation = glGetAttribLocation(program, A_POSITION);
		
		vertexData.position(0);
		glVertexAttribPointer(aPositionLocation, POSITION_COMPONENT_COUNT, GL_FLOAT, false, STRIDE, vertexData);
		glEnableVertexAttribArray(aPositionLocation);
		
		vertexData.position(POSITION_COMPONENT_COUNT);
		glVertexAttribPointer(aColorLocation, COLOR_COMPONENT_COUNT, GL_FLOAT, false, STRIDE, vertexData);
		glEnableVertexAttribArray(aColorLocation);
	}

}
