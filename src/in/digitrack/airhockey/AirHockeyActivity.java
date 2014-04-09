package in.digitrack.airhockey;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.widget.Toast;

import in.digitrack.airhockey.AirHockeyRenderer;

public class AirHockeyActivity extends Activity {

	private GLSurfaceView glSurfaceView;
	private boolean rendererSet = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		glSurfaceView = new GLSurfaceView(this);
		
		final ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		final ConfigurationInfo configInfo = activityManager.getDeviceConfigurationInfo();
		final boolean supportsEs2 = configInfo.reqGlEsVersion >= 0x20000;
		
		if(supportsEs2) {
			glSurfaceView.setEGLContextClientVersion(2);
			glSurfaceView.setRenderer(new AirHockeyRenderer(this));
			rendererSet = true;
		} else {
			Toast.makeText(this, "OpenglES 2 not supported", Toast.LENGTH_SHORT).show();
			return;
		}
		setContentView(glSurfaceView);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (rendererSet) {
			glSurfaceView.onResume();
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		if (rendererSet) {
			glSurfaceView.onPause();
		}
	}

}
