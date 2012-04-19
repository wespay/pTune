/**
 * 
 */
package com.ptune.audio;

import com.ptune.PTuneActivity;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder.AudioSource;
import android.os.AsyncTask;
import android.util.Log;

/**
 * @author weston
 *
 */
public class CaptureTask extends AsyncTask<Float, Float, Void> {
	
	private PTuneActivity mainActivity;
	
	public CaptureTask(PTuneActivity context) {
		this.mainActivity = context;
	}

	@Override
	protected Void doInBackground(Float... target) {
		int sRate = 44100;
		int bufferSize = 65536;
		//bufferSize = 32768;

		AudioRecord recorder = new AudioRecord(AudioSource.DEFAULT,
				sRate, AudioFormat.CHANNEL_IN_MONO,
				AudioFormat.ENCODING_PCM_16BIT, bufferSize);
		
		//AudioTrack track = new AudioTrack(AudioManager.STREAM_MUSIC, sRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize, AudioTrack.MODE_STREAM);

		// Create storage container for read data.
		byte buffer[] = new byte[bufferSize];
		
		boolean isRunning = true;
		
		recorder.startRecording();
		//track.play();

		while(isRunning) {

			// Read stream data into buffer container.
			//recorder.read(buffer, 0, buffer.length/16);
			// TODO: Put divide by 16 back in maybe
			int bytesRead = recorder.read(buffer, 0, buffer.length);
			//Log.d("CaptureTask", "Bytes read: " + Integer.toString(bytesRead));
			//track.write(buffer, 0, buffer.length);

			if (bytesRead > 0) {
				// Create frequency spectrum.
				Spectrum spectrum = new Spectrum(buffer, sRate);
				float frequency = spectrum.getFrequency(target[0]);

				publishProgress(frequency);
			}
			
			if (this.isCancelled()) {
				isRunning = false;
				//return null;
			}
		}
		
		recorder.stop();
		recorder.release();
		//track.stop();
		//track.release();
		
		return null;
	}

	@Override protected void onProgressUpdate(Float... freq) {
		mainActivity.updateDisplay(freq[0]);
	}
}
