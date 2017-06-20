package com.nilhcem.assistant.androidthings.ui.main.eyes;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.google.android.things.pio.PeripheralManagerService;
import com.google.android.things.pio.SpiDevice;

import java.io.IOException;

/**
 * See https://github.com/Nilhcem/ledcontrol-androidthings
 */
public class LedControl implements AutoCloseable {

    private static final byte OP_DIGIT0 = 1;
    private static final byte OP_DECODEMODE = 9;
    private static final byte OP_INTENSITY = 10;
    private static final byte OP_SCANLIMIT = 11;
    private static final byte OP_SHUTDOWN = 12;
    private static final byte OP_DISPLAYTEST = 15;

    private SpiDevice spiDevice;
    private byte[] spidata = new byte[16];
    private byte[] status = new byte[64];
    private int maxDevices;

    public LedControl(String spiGpio, int numDevices) throws IOException {
        PeripheralManagerService pioService = new PeripheralManagerService();
        spiDevice = pioService.openSpiDevice(spiGpio);
        spiDevice.setMode(SpiDevice.MODE0);
        spiDevice.setFrequency(1000000);
        spiDevice.setBitsPerWord(8);
        spiDevice.setBitJustification(false);

        maxDevices = numDevices;
        if (numDevices < 1 || numDevices > 8) {
            maxDevices = 8;
        }

        for (int i = 0; i < maxDevices; i++) {
            spiTransfer(i, OP_DISPLAYTEST, 0);
            setScanLimit(i, 7);
            spiTransfer(i, OP_DECODEMODE, 0);
            clearDisplay(i);
            shutdown(i, true);
        }
    }

    @Override
    public void close() throws IOException {
        try {
            spiDevice.close();
        } finally {
            spiDevice = null;
        }
    }

    public int getDeviceCount() {
        return maxDevices;
    }

    public void shutdown(int addr, boolean status) throws IOException {
        if (addr < 0 || addr >= maxDevices) {
            return;
        }

        spiTransfer(addr, OP_SHUTDOWN, status ? 0 : 1);
    }

    public void setScanLimit(int addr, int limit) throws IOException {
        if (addr < 0 || addr >= maxDevices) {
            return;
        }

        if (limit >= 0 || limit < 8) {
            spiTransfer(addr, OP_SCANLIMIT, limit);
        }
    }

    public void setIntensity(int addr, int intensity) throws IOException {
        if (addr < 0 || addr >= maxDevices) {
            return;
        }

        if (intensity >= 0 || intensity < 16) {
            spiTransfer(addr, OP_INTENSITY, intensity);
        }
    }

    public void clearDisplay(int addr) throws IOException {
        if (addr < 0 || addr >= maxDevices) {
            return;
        }

        int offset = addr * 8;
        for (int i = 0; i < 8; i++) {
            status[offset + i] = 0;
            spiTransfer(addr, (byte) (OP_DIGIT0 + i), status[offset + i]);
        }
    }

    public void setRow(int addr, int row, byte value) throws IOException {
        if (addr < 0 || addr >= maxDevices) {
            return;
        }
        if (row < 0 || row > 7) {
            return;
        }

        int offset = addr * 8;
        status[offset + row] = value;
        spiTransfer(addr, (byte) (OP_DIGIT0 + row), status[offset + row]);
    }

    public void draw(Bitmap bitmap) throws IOException {
        Bitmap scaled = Bitmap.createScaledBitmap(bitmap, 8 * maxDevices, 8, true);
        for (int row = 0; row < 8; row++) {
            for (int curDevice = 0; curDevice < maxDevices; curDevice++) {
                int value = 0;
                for (int col = 0; col < 8; col++) {
                    value |= scaled.getPixel((curDevice * 8) + col, row) == Color.WHITE ? (0x80 >> col) : 0;
                }
                setRow(maxDevices - curDevice - 1, row, (byte) value);
            }
        }
    }

    private void spiTransfer(int addr, byte opcode, int data) throws IOException {
        int offset = addr * 2;
        int maxbytes = maxDevices * 2;

        for (int i = 0; i < maxbytes; i++) {
            spidata[i] = (byte) 0;
        }
        spidata[maxbytes - offset - 2] = opcode;
        spidata[maxbytes - offset - 1] = (byte) data;
        spiDevice.write(spidata, maxbytes);
    }
}
