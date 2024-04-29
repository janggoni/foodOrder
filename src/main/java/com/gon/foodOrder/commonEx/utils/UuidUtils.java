package com.sharp.common.utils;


import java.nio.ByteBuffer;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.fasterxml.uuid.Generators;

@Component
public class UuidUtils {

    private final static int LENGTH_20_LONG_RADIX = 9;
    private final static int LENGTH_10_INT_RADIX = 9;

    // UUID 생성
    public static String makeOriginUUID() {
        return UUID.randomUUID().toString();
    }

    // TIMESTAMP base UUID 생성
    public static String makeTimestampUUID() {
    	return Generators.timeBasedGenerator().generate().toString();
    }

    // 10자리의 UUID 생성
    public static String makeShortUUID() {
        UUID uuid = UUID.randomUUID();
        return parseToShortUUID(uuid.toString());
    }

    public static String parseToIntRadixUUID(String uuid, int radix) {
        int l = ByteBuffer.wrap(uuid.getBytes()).getInt();
        return Integer.toString(l, radix);
    }

    public static String parseToLongRadixUUID(String uuid, int radix) {
        long l = ByteBuffer.wrap(uuid.getBytes()).getLong();
        return Long.toString(l, radix);
    }

    // 파라미터로 받은 값을 10자리의 UUID로 변환
    public static String parseToShortUUID(String uuid) {
        int l = ByteBuffer.wrap(uuid.getBytes()).getInt();
        return Integer.toString(l, LENGTH_10_INT_RADIX);
    }

    // 파라미터로 받은 값을 20자리의 UUID로 변환
    public static String parseToLongUUID(String uuid) {
        long l = ByteBuffer.wrap(uuid.getBytes()).getLong();
        return Long.toString(l, LENGTH_20_LONG_RADIX);
    }

    public static String toUnsignedString(long i, int shift) {
        char[] buf = new char[64];
        int charPos = 64;
        int radix = 1 << shift;
        long mask = radix - 1;
        long number = i;

        do {
            buf[--charPos] = digits[(int) (number & mask)];
            number >>>= shift;
        } while (number != 0);

        return new String(buf, charPos, (64 - charPos));
    }

    final static char[] digits = {
            '0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', 'a', 'b', 'c', 'd', 'e', 'f',
            'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
            'o', 'p', 'q', 'r', 's', 't', 'u', 'v',
            'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D',
            'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L',
            'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
            'U', 'V', 'W', 'X', 'Y', 'Z', '_', '=' // '.', '-'
    };

}