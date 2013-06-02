package no.skogvoldconsulting.pulse.audio;

/**
 * Inspired by
 * http://stackoverflow.com/questions/2413426/playing-an-arbitrary-tone-with-android
 */
public class ToneGenerator {
    public static byte[] genTone(int sampleRate, int freqOfTone, double lengthInSeconds){
        double[] sample = new double[(int) (sampleRate*lengthInSeconds)];
        byte[] generatedSound = new byte[sample.length*2];
        // fill out the array
        for (int i = 0; i < sample.length; i++) {
            sample[i] = Math.sin(2 * Math.PI * i / (sampleRate/freqOfTone));
        }

        // convert to 16 bit pcm sound array
        // assumes the sample buffer is normalised.
        int idx = 0;
        for (final double dVal : sample) {
            // scale to maximum amplitude
            final short val = (short) ((dVal * Short.MAX_VALUE));
            // in 16 bit wav PCM, first byte is the low order byte
            generatedSound[idx++] = (byte) (val & 0x00ff);
            generatedSound[idx++] = (byte) ((val & 0xff00) >>> 8);
        }
        return generatedSound;
    }
}
