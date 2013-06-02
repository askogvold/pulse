package no.skogvoldconsulting.pulse.audio;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

public class BeepThread extends PulseAudio {
	@Override
	public void run() {
		int sampleRate = 44000;
		double lengthInSeconds = 0.1;
        int frequency = 1600;
        byte[] noise = ToneGenerator.genTone(sampleRate, frequency, lengthInSeconds);

		final AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
				sampleRate, AudioFormat.CHANNEL_OUT_MONO,
				AudioFormat.ENCODING_PCM_16BIT, noise.length,
				AudioTrack.MODE_STATIC);
		audioTrack.write(noise, 0, noise.length);

		while(!shouldCancel) {
			try {
				sleep((long) Math.min((1000 * 60.0/bpm), 2000));
				
				audioTrack.stop();
				if(bpm==0) {
					audioTrack.setLoopPoints(0, noise.length/2, -1);
				} else {
					audioTrack.setLoopPoints(0, 0, 0);
				}
				audioTrack.reloadStaticData();
				audioTrack.setPlaybackHeadPosition(0);
				audioTrack.play();
			} catch (InterruptedException e) {
			}
		}
		audioTrack.stop();
		audioTrack.release();
	}
}