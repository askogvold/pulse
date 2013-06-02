package no.skogvoldconsulting.pulse.audio;

public abstract class PulseAudio extends Thread {
	protected int bpm = 60;
	protected boolean shouldCancel = false;

	public int getBpm() {
		return bpm;
	}

	public void setBpm(int bpm) {
		this.bpm = bpm;
	}

	public void cancel() {
		shouldCancel=true;
	}
	
}
