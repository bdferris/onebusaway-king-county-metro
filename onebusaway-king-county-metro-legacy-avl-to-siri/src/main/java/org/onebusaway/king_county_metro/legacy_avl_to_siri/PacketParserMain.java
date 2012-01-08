package org.onebusaway.king_county_metro.legacy_avl_to_siri;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class PacketParserMain {

  public static void main(String[] args) throws IOException {
    List<File> files = new ArrayList<File>();
    for (String arg : args) {
      listFiles(new File(arg), files);
    }

    byte[] rawBuffer = new byte[2048];
    ByteBuffer buffer = ByteBuffer.wrap(rawBuffer);

    System.out.println("===");

    for (File file : files) {
      BufferedInputStream in = new BufferedInputStream(
          new FileInputStream(file));
      int offset = 0;
      while (true) {
        int rc = in.read(rawBuffer, offset, rawBuffer.length - offset);
        if (rc == -1)
          break;
        offset += rc;
      }

      buffer.rewind();
      List<Packet> packets = new ArrayList<Packet>();
      PacketIO.parsePacket(buffer, 1024, packets);
      if (packets.isEmpty()) {
        continue;
      }

      for (Packet packet : packets) {
        if (packet.getVehicleId() == 2668) {
          System.out.println(file.getName() + " " + packet.getVehicleId() + " "
              + packet.getTrip() + " " + packet.getTime() + " "
              + packet.getScheduleDeviation());
        }
      }
    }
  }

  private static void listFiles(File file, List<File> files) {
    if (file.isFile()) {
      files.add(file);
    } else if (file.isDirectory()) {
      for (File child : file.listFiles()) {
        listFiles(child, files);
      }
    }
  }
}
