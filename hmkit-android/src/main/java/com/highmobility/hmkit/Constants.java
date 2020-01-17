/*
 * The MIT License
 *
 * Copyright (c) 2014- High-Mobility GmbH (https://high-mobility.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.highmobility.hmkit;

import java.util.UUID;

public class Constants {
    static final UUID SERVICE_UUID = UUID.fromString("713D0100-503E-4C75-BA94-3148F18D941E");
    static final UUID READ_CHAR_UUID = UUID.fromString("713D0102-503E-4C75-BA94-3148F18D941E");
    static final UUID WRITE_CHAR_UUID = UUID.fromString("713D0103-503E-4C75-BA94-3148F18D941E");
    static final UUID ALIVE_CHAR_UUID = UUID.fromString("713D0104-503E-4C75-BA94-3148F18D941E");
    static final UUID INFO_CHAR_UUID = UUID.fromString("713D0105-503E-4C75-BA94-3148F18D941E");
    static final UUID SENSING_READ_CHAR_UUID = UUID.fromString("713D0106-503E-4C75-BA94-3148F18D941E");
    static final UUID SENSING_WRITE_CHAR_UUID = UUID.fromString("713D0107-503E-4C75-BA94-3148F18D941E");
    static final int MAX_COMMAND_LENGTH = 253; // this is without commandId, requiresHMAC and size

    static final UUID NOTIFY_DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805F9B34FB");

    public static final float registerTimeout = 10.0f;
    public static final int certificateStorageCount = 30;

    public interface ResponseCallback {
        void response(int errorCode);
    }
}