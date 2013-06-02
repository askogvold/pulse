package no.skogvoldconsulting.pulse.audio;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

public class WaveThread extends PulseAudio {
    private static final String TAG = PulseAudio.class.getName();
	private AudioTrack track;
	private Context context;
	
	public WaveThread(Context context) {
		this.context = context;
	}
	
	@Override
	public void setBpm(int bpm) {
		super.setBpm(bpm);
		double initialBpm = 100.0;
		int initialSampleRate = 44000;
		track.setPlaybackRate((int) (initialSampleRate * (bpm/initialBpm)));
	}
	
	public void run() {
		try {
			InputStream wavInput = context.getAssets().open("Funk-100-1j.wav");
			
			byte[] data = IOUtils.toByteArray(wavInput);
			
			int sampleRate = 44000;
			int channelConfig = AudioFormat.CHANNEL_OUT_STEREO;
			int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
			int mode = AudioTrack.MODE_STATIC;
			int bufferSize = data.length;
			
			track = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, channelConfig, audioFormat, bufferSize, mode);
			
			track.write(data, 0, data.length);
			
			track.setLoopPoints(0, data.length/4, -1);
			track.play();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}		
	}
	
	@Override
	public void cancel() {
		super.cancel();
        try {
		track.stop();
		track.release();
        } catch (IllegalStateException e) {
            Log.d(TAG, "IllegalState on cancel", e);
        }
	}

}