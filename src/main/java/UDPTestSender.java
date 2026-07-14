import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPTestSender {
    public static void main(String[] args) {
        try {
            // Create a socket to send data out
            DatagramSocket socket = new DatagramSocket();
            InetAddress localAddress = InetAddress.getByName("localhost"); // Send to our own computer

            System.out.println("Starting fake 2D face-tracker stream...");
            float time = 0;

            while (true) {
                // 1. Generate a fake X parameter from 0.0 to 1.0 (Horizontal turn)
                float fakeX = (float) ((Math.sin(time) + 1.0) / 2.0);

                // 2. Generate a fake Y parameter from 0.0 to 1.0 (Vertical nod)
                // We use cosine here so it's slightly out of sync with X, creating a circular motion!
                float fakeY = (float) ((Math.cos(time * 0.8) + 1.0) / 2.0);

                // 3. Package both numbers separated by a comma (e.g. "0.75,0.42")
                String message = fakeX + "," + fakeY;
                byte[] buffer = message.getBytes();

                // 4. Package it up and fire it at port 9000
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, localAddress, 9000);
                socket.send(packet);

                System.out.println("Sent 2D tracking data: " + message);

                // 5. Move time forward and wait 50 milliseconds (simulating 20 frames per second)
                time += 0.1f;
                Thread.sleep(50);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}