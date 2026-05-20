import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPTestSender {
    public static void main(String[] args) {
        try {
            // Create a socket to send data out
            DatagramSocket socket = new DatagramSocket();
            InetAddress localAddress = InetAddress.getByName("localhost"); // Send to our own computer

            System.out.println("Starting fake face-tracker stream...");
            float time = 0;

            while (true) {
                // 1. Generate a fake parameter from 0.0 to 1.0
                float fakeSensorData = (float) ((Math.sin(time) + 1.0) / 2.0);

                // 2. Convert that number into a String, and then into raw bytes
                String message = String.valueOf(fakeSensorData);
                byte[] buffer = message.getBytes();

                // 3. Package it up and fire it at port 9000
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, localAddress, 9000);
                socket.send(packet);

                System.out.println("Sent tracking data: " + message);

                // 4. Move time forward and wait 50 milliseconds (simulating 20 frames per second)
                time += 0.1f;
                Thread.sleep(50);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}