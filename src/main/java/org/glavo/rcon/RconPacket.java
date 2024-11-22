/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2024 Glavo
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.glavo.rcon;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

final class RconPacket {

    public static final int SERVERDATA_EXECCOMMAND = 2;
    public static final int SERVERDATA_AUTH = 3;

    private final int requestId;
    private final int type;
    private final byte[] payload;

    private RconPacket(int requestId, int type, byte[] payload) {
        this.requestId = requestId;
        this.type = type;
        this.payload = payload;
    }

    public int getRequestId() {
        return requestId;
    }

    public int getType() {
        return type;
    }

    public byte[] getPayload() {
        return payload;
    }

    /**
     * Send a Rcon packet and fetch the response
     *
     * @param rcon    Rcon instance
     * @param type    The packet type
     * @param payload The payload (password, command, etc.)
     * @return A RconPacket object containing the response
     */
    static RconPacket send(Rcon rcon, int type, byte[] payload) throws IOException {
        try {
            RconPacket.write(rcon.getSocket().getOutputStream(), rcon.getRequestId(), type, payload);
        } catch (SocketException se) {
            // Close the socket if something happens
            rcon.getSocket().close();

            // Rethrow the exception
            throw se;
        }

        return RconPacket.read(rcon.getSocket().getInputStream());
    }

    /**
     * Write a rcon packet on an outputstream
     *
     * @param out       The OutputStream to write on
     * @param requestId The request id
     * @param type      The packet type
     * @param payload   The payload
     */
    private static void write(OutputStream out, int requestId, int type, byte[] payload) throws IOException {
        int bodyLength = RconPacket.getBodyLength(payload.length);
        int packetLength = RconPacket.getPacketLength(bodyLength);

        ByteBuffer buffer = ByteBuffer.allocate(packetLength);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        buffer.putInt(bodyLength);
        buffer.putInt(requestId);
        buffer.putInt(type);
        buffer.put(payload);

        // Null bytes terminators
        buffer.put((byte) 0);
        buffer.put((byte) 0);

        // Woosh!
        out.write(buffer.array());
        out.flush();
    }

    /**
     * Read an incoming rcon packet
     *
     * @param in The InputStream to read on
     * @return The read RconPacket
     */
    private static RconPacket read(InputStream in) throws IOException {
        // Header is 3 4-bytes ints
        byte[] header = new byte[4 * 3];

        // Read the 3 ints
        //noinspection ResultOfMethodCallIgnored
        in.read(header);

        try {
            // Use a bytebuffer in little endian to read the first 3 ints
            ByteBuffer buffer = ByteBuffer.wrap(header);
            buffer.order(ByteOrder.LITTLE_ENDIAN);

            int length = buffer.getInt();
            int requestId = buffer.getInt();
            int type = buffer.getInt();

            // Payload size can be computed now that we have its length
            byte[] payload = new byte[length - 4 - 4 - 2];

            DataInputStream dis = new DataInputStream(in);

            // Read the full payload
            dis.readFully(payload);

            // Read the null bytes
            //noinspection ResultOfMethodCallIgnored
            dis.read(new byte[2]);

            return new RconPacket(requestId, type, payload);
        } catch (BufferUnderflowException | EOFException e) {
            throw new MalformedPacketException("Cannot read the whole packet");
        }
    }

    private static int getPacketLength(int bodyLength) {
        // 4 bytes for length + x bytes for body length
        return 4 + bodyLength;
    }

    private static int getBodyLength(int payloadLength) {
        // 4 bytes for requestId, 4 bytes for type, x bytes for payload, 2 bytes for two null bytes
        return 4 + 4 + payloadLength + 2;
    }

}
