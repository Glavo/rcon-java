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

import java.io.Closeable;
import java.io.Console;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

public final class Rcon implements Closeable {

    public static final int DEFAULT_PORT = 25575;

    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    private final ReentrantLock lock = new ReentrantLock();
    private final Random rand = new Random();

    private int requestId;
    private Socket socket;

    private Charset charset = DEFAULT_CHARSET;
    private int timeout = 0;

    public Rcon() {
    }

    public Rcon(String host, String password) throws IOException, AuthenticationException {
        this(host, DEFAULT_PORT, password.getBytes(Rcon.DEFAULT_CHARSET));
    }

    public Rcon(String host, byte[] password) throws IOException, AuthenticationException {
        this(host, DEFAULT_PORT, password);
    }

    public Rcon(String host, int port, String password) throws IOException, AuthenticationException {
        this(host, port, password.getBytes(Rcon.DEFAULT_CHARSET));
    }

    /**
     * Create, connect and authenticate a new Rcon object
     *
     * @param host     Rcon server address
     * @param port     Rcon server port
     * @param password Rcon server password
     */
    public Rcon(String host, int port, byte[] password) throws IOException, AuthenticationException {
        // Connect to host
        this.connect(host, port, password);
    }

    /**
     * Connect to a rcon server
     *
     * @param address  Rcon server address
     * @param password Rcon server password
     * @since 3.0
     */
    public void connect(SocketAddress address, byte[] password) throws IOException, AuthenticationException {
        // Connect to the rcon server
        lock.lock();
        try {
            // New random request id
            this.requestId = rand.nextInt();

            // We can't reuse a socket, so we need a new one
            this.socket = new Socket();
            this.socket.connect(address, this.timeout);
            if (this.timeout > 0) {
                this.socket.setSoTimeout(this.timeout);
            }
        } finally {
            lock.unlock();
        }

        // Send the auth packet
        RconPacket res = this.send(RconPacket.SERVERDATA_AUTH, password);

        // Auth failed
        if (res.getRequestId() == -1) {
            throw new AuthenticationException("Password rejected by server");
        }
    }

    /**
     * Connect to a rcon server
     *
     * @param host     Rcon server address
     * @param port     Rcon server port
     * @param password Rcon server password
     */
    public void connect(String host, int port, byte[] password) throws IOException, AuthenticationException {
        connect(new InetSocketAddress(host, port), password);
    }

    /**
     * Disconnect from the current server
     */
    public void disconnect() throws IOException {
        lock.lock();
        try {
            this.socket.close();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Disconnect from the current server
     *
     * @see #disconnect()
     */
    @Override
    public void close() throws IOException {
        disconnect();
    }

    /**
     * Send a command to the server
     *
     * @param payload The command to send
     * @return The payload of the response
     */
    public String command(String payload) throws IOException {
        if (payload == null || payload.trim().isEmpty()) {
            throw new IllegalArgumentException("Payload can't be null or empty");
        }

        byte[] bytes = payload.getBytes(charset);
        if (bytes.length > 1446) {
            throw new IllegalArgumentException("Payload too long");
        }

        RconPacket response = this.send(RconPacket.SERVERDATA_EXECCOMMAND, bytes);

        return new String(response.getPayload(), this.charset);
    }

    private RconPacket send(int type, byte[] payload) throws IOException {
        lock.lock();
        try {
            return RconPacket.send(this, type, payload);
        } finally {
            lock.unlock();
        }
    }

    public int getRequestId() {
        return requestId;
    }

    public Socket getSocket() {
        return socket;
    }

    public Charset getCharset() {
        return charset;
    }

    public void setCharset(Charset charset) {
        this.charset = charset == null ? Rcon.DEFAULT_CHARSET : charset;
    }

    /**
     * @since 3.0
     */
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public static void main(String[] args) {
        final Console console = System.console();
        if (console == null) {
            System.err.println("Unable to obtain the Console instance, please run it in the terminal!");
            System.exit(-1);
        }
        String ans = console.readLine("The server IP or domain name (default 127.0.0.1): ");
        String ip = "127.0.0.1";

        if (ans == null) {
            return;
        } else if (!ans.isEmpty()) {
            try {
                ip = InetAddress.getByName(ans.trim()).getHostAddress();
            } catch (UnknownHostException e) {
                System.err.println("Name or service not known: " + ans);
                System.exit(-1);
            }
        }

        String ps = console.readLine("The RCON network port (defaule 25575): ");
        int port = 25575;
        if (ps == null) {
            return;
        } else if (!ps.isEmpty()) {
            try {
                port = Integer.parseInt(ps.trim());
                if (port < 1 || port > 65535) {
                    System.err.println("Port " + port + " is out of range");
                    System.exit(-1);
                }
            } catch (NumberFormatException e) {
                System.err.println("Wrong port: " + ps);
                System.exit(-1);
            }
        }


        char[] pws = console.readPassword("The password for RCON: ");
        if (pws == null) {
            return;
        }
        ByteBuffer pwsa = DEFAULT_CHARSET.encode(CharBuffer.wrap(pws));
        byte[] password = new byte[pwsa.remaining()];
        System.arraycopy(pwsa.array(), pwsa.position(), password, 0, password.length);

        try (Rcon rcon = new Rcon(ip, port, password)) {
            System.out.println();

            while (true) {
                String c = console.readLine("RCON> ");
                if (c == null) {
                    break;
                }
                if (!c.isEmpty()) {
                    if (c.trim().equals("exit")) {
                        break;
                    }
                    try {
                        String a = rcon.command(c);

                        System.out.println(a);
                        if (!a.isEmpty()) {
                            System.out.println();
                        }
                    } catch (Throwable e) {
                        System.err.println(e.getMessage());
                        System.err.println();
                    }
                }
            }
        } catch (Throwable e) {
            System.err.println(e.getMessage());
            System.exit(-1);
        }

        System.out.println("Bye bye!");
    }

}
