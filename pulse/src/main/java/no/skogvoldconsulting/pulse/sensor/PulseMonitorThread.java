package no.skogvoldconsulting.pulse.sensor;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.UUID;

public class PulseMonitorThread extends Thread {
    private static final String TAG = PulseMonitorThread.class.getName();

    private static final UUID UUID_SERIAL = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");


    private final LinkedList<Byte> bytes;
    private final BluetoothDevice polar;
    private final SensorListener listener;

    public PulseMonitorThread(BluetoothDevice polarDevice, SensorListener listener) {
        this.polar = polarDevice;
        this.listener = listener;
        bytes = new LinkedList<Byte>();
    }

    public void run() {
        byte[] byteBuffer = new byte[8];
        try {
            BluetoothSocket socket = polar
                    .createRfcommSocketToServiceRecord(UUID_SERIAL);
            InputStream inputStream = new BufferedInputStream(
                    socket.getInputStream(), 32);
            socket.connect();
            while (true) {
                int numBytes = inputStream.read(byteBuffer);
                for (int i = 0; i < numBytes; i++) {
                    bytes.add(byteBuffer[i]);
                }
                byte[] result = processQueue(bytes);
                if (result != null) {
                    listener.setPacket(result);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * @return Valid packet or null
     */
    private byte[] processQueue(LinkedList<Byte> bytes) {
        int PACKET_SIZE = 8;
        while (bytes.size() > 0 && (bytes.peek() & 0xFF) != 0xFE) {
            bytes.remove();
        }

        if (bytes.size() >= PACKET_SIZE) {
            byte[] packet = new byte[PACKET_SIZE];
            for (int i = 0; i < PACKET_SIZE; i++) {
                packet[i] = bytes.poll();
            }

            return processPacket(packet);

        }
        return null;
    }

    private byte[] processPacket(byte[] packet) {
        if ((packet[0] & 0xFF) != 0xFE) {
            Log.e(TAG, "Header invalid (not 0xFE)");
            return null;
        }

        int b1 = (packet[1] & 0xFF);
        int b2 = (packet[2] & 0xFF);
        if (b2 != 0xFF - b1) {
            Log.d(TAG, String.format("Checkbyte failed %d != 255 - %d", b2, b1));
            return null;
        }

        int b3 = packet[2] & 0xFF;
        if (b3 < 0x0F) {
            Log.d(TAG, String.format("Sequence failed. %02X < 0x0F", b3));
        }

        return packet;
    }
}