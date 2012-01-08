package org.onebusaway.king_county_metro.legacy_avl_to_siri;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

import org.apache.commons.codec.binary.Hex;
import org.onebusaway.king_county_metro.legacy_avl_to_siri.Packet.EStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PacketIO {

  private static final Logger _log = LoggerFactory.getLogger(PacketIO.class);

  public static void parsePacket(ByteBuffer buffer, int dataLength,
      List<Packet> packets) {

    buffer.order(ByteOrder.LITTLE_ENDIAN);

    short packetLength = (short) buffer.getShort();
    short packetSelector = (short) buffer.get();

    if (packetSelector != (short) -50)
      return;

    short packetVehicleCount = (short) buffer.get();

    if (_log.isDebugEnabled())
      _log.debug("packet: length=" + packetLength + "  selector="
          + packetSelector + " vehicles=" + packetVehicleCount);

    for (int packetVehicleIndex = 0; packetVehicleIndex < packetVehicleCount; ++packetVehicleIndex) {

      int remaining = dataLength - buffer.position();

      if (remaining < 60) {
        _log.warn("expected=60 actual=" + remaining);
        break;
      }

      Packet packet = new Packet();

      int startPosition = buffer.position();

      short vehicleId = buffer.getShort();
      packet.setVehicleId(vehicleId);

      int operatorId = buffer.getInt();
      packet.setOperatorId(operatorId);

      short route = buffer.getShort();
      packet.setRoute(route);

      short run = buffer.getShort();
      packet.setRun(run);

      short serviceRoute = buffer.getShort();
      packet.setServiceRoute(serviceRoute);

      byte pIconByte = buffer.get();

      String s = getByteAsBinaryString(pIconByte);
      packet.setUnknown(s);

      /**
       * TODO: Do these bit masks make sense? Shouldn't they be powers of two?
       */

      boolean express = (pIconByte & 0x01) == 1;
      packet.setExpress(express);

      EStatus status = EStatus.NORMAL;

      boolean noTrip = (pIconByte & (0x01 << 4)) != 0;
      packet.setNoTrip(noTrip);

      packet.setStatus(status);

      byte pState = buffer.get();
      String asString = getByteAsBinaryString(pState);
      packet.setUnknown(asString);
      int servicePattern = buffer.getInt();
      packet.setServicePattern(servicePattern);

      int servicePatternDistance = buffer.getInt();
      packet.setServicePatternDistance(servicePatternDistance);

      int tripDistance = buffer.getInt();
      packet.setTripDistance(tripDistance);

      short scheduleDeviation = buffer.getShort();
      packet.setScheduleDeviation(scheduleDeviation);

      // byte pcoordact = (byte) buffer.get();
      buffer.get();

      byte[] bytepattern = new byte[11];
      buffer.get(bytepattern, 0, 11);

      try {
        String pattern = new String(bytepattern, "utf-8");
        pattern.trim();

        String compressedString = "";
        int compressi = 0;
        while ((compressi < pattern.length())
            && (pattern.charAt(compressi) != 0)) {
          compressedString += pattern.charAt(compressi);
          ++compressi;
        }
        pattern = compressedString.trim();
        packet.setPattern(pattern);
      } catch (UnsupportedEncodingException e) {
        throw new IllegalStateException(e);
      }

      buffer.position(buffer.position() + 3); // Extra bytes

      int trip = buffer.getInt();
      packet.setTrip(trip);

      int time = buffer.getInt();
      packet.setTime(time);

      buffer.position(buffer.position() + 9);

      int endPosition = buffer.position();

      if (packet.getVehicleId() == 2668) {
        buffer.position(startPosition);
        byte[] copy = new byte[endPosition - startPosition];
        buffer.get(copy);
        System.out.println(Hex.encodeHex(copy));
      }

      packets.add(packet);
    }
  }

  private static String getByteAsBinaryString(byte b) {
    String s = Integer.toBinaryString(b);
    while (s.length() < 8)
      s = "0" + s;
    return s;
  }
}
