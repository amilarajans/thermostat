/*
 * Copyright 2012-2016 Red Hat, Inc.
 *
 * This file is part of Thermostat.
 *
 * Thermostat is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 2, or (at your
 * option) any later version.
 *
 * Thermostat is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Thermostat; see the file COPYING.  If not see
 * <http://www.gnu.org/licenses/>.
 *
 * Linking this code with other modules is making a combined work
 * based on this code.  Thus, the terms and conditions of the GNU
 * General Public License cover the whole combination.
 *
 * As a special exception, the copyright holders of this code give
 * you permission to link this code with independent modules to
 * produce an executable, regardless of the license terms of these
 * independent modules, and to copy and distribute the resulting
 * executable under terms of your choice, provided that you also
 * meet, for each linked independent module, the terms and conditions
 * of the license of that module.  An independent module is a module
 * which is not derived from or based on this code.  If you modify
 * this code, you may extend this exception to your version of the
 * library, but you are not obligated to do so.  If you do not wish
 * to do so, delete this exception statement from your version.
 */

package com.redhat.thermostat.agent.utils.windows;

import com.redhat.thermostat.common.utils.LoggingUtils;
import com.redhat.thermostat.shared.config.NativeLibraryResolver;
import com.redhat.thermostat.shared.config.OS;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Utility class to access Windows native code
 */
public class WindowsHelperImpl {

    private static final Logger logger = LoggingUtils.getLogger(WindowsHelperImpl.class);

    /*
     // from MemoryStatusEx (8 values)
        DWORD     dwMemoryLoad;
        DWORDLONG ullTotalPhys;
        DWORDLONG ullAvailPhys;
        DWORDLONG ullTotalPageFile;
        DWORDLONG ullAvailPageFile;
        DWORDLONG ullTotalVirtual;
        DWORDLONG ullAvailVirtual;
        DWORDLONG ullAvailExtendedVirtual;

     // from PERFORMANCE_INFORMATION (13 values)
        SIZE_T CommitTotal;
        SIZE_T CommitLimit;
        SIZE_T CommitPeak;
        SIZE_T PhysicalTotal;
        SIZE_T PhysicalAvailable;
        SIZE_T SystemCache;
        SIZE_T KernelTotal;
        SIZE_T KernelPaged;
        SIZE_T KernelNonpaged;
        SIZE_T PageSize;
        DWORD  HandleCount;
        DWORD  ProcessCount;
        DWORD  ThreadCount;
     */

    static {
        if (OS.IS_WINDOWS) {
            String lib = NativeLibraryResolver.getAbsoluteLibraryPath("WindowsHelperWrapper");
            try {
                System.load(lib);
                INSTANCE = new WindowsHelperImpl();
            } catch (UnsatisfiedLinkError e) {
                logger.severe("Could not load WindowsHelperImpl DLL:" + lib);
                INSTANCE = null;
                // do not throw here, because you'll get a NoClassDefFound thrown when running other tests that Mock this class
            }
        } else {
            INSTANCE = null;
        }
    }

    public static class MemoryStat {

        private final long timeStamp;
        private final long total;
        private final long free;
        private final long buffers;
        private final long cached;
        private final long swapTotal;
        private final long swapFree;
        private final long commitLimit;


        public MemoryStat() {
            final long[] info = INSTANCE.getMemoryInfo();
            this.timeStamp = System.currentTimeMillis();
            this.total = info[1];
            this.free = info[2];
            this.buffers = 0;
            this.cached = 0;
            this.swapTotal = 3;
            this.swapFree = 4;
            this.commitLimit = 0;
            // TODO - buffers, cached and commitLimit
        }

        public MemoryStat(long timeStamp, long total, long free, long buffers, long cached, long swapTotal, long swapFree, long commitLimit) {
            this.timeStamp = timeStamp;
            this.total = total;
            this.free = free;
            this.buffers = buffers;
            this.cached = cached;
            this.swapTotal = swapTotal;
            this.swapFree = swapFree;
            this.commitLimit = commitLimit;
        }

        public long getTimeStamp() {
            return timeStamp;
        }
        public long getTotal() {
            return total;
        }
        public long getFree() {
            return free;
        }
        public long getBuffers() {
            return buffers;
        }
        public long getCached() {
            return cached;
        }
        public long getSwapTotal() {
            return swapTotal;
        }
        public long getSwapFree() {
            return swapFree;
        }
        public long getCommitLimit() {
            return commitLimit;
        }
    }

    public static WindowsHelperImpl INSTANCE;

    private WindowsHelperImpl() {
    }
    // local host-wide information

    public String getHostName() {
        return getHostName0(true);
    }

    public String getOSName() {
        return System.getProperty("os.name");
    }

    public String getOSVersion() {
        final long info[] = new long[3];  // major, minor, build
        getOSVersion0(info);
        return "" + info[0] + "." + info[1] + " (Build " + info[2] + ")";
    }

    public String getCPUModel() {
        return getCPUString0();
    }

    public int getCPUCount() {
        return getCPUCount0();
    }

    public long getTotalMemory() {
        final long info[] = getMemoryInfo();
        return info[1]; // totalPhysical
    }

    // local process-specific information
    public String getUserName(int pid) {
        return getUserName0(pid,true);
    }

    public int getUid(int pid) {
        return getUid0(pid);
    }

    public Map<String, String> getEnvironment(int pid) {
        // the environment is returned as a 1D array of alternating env names and values
        final String[] envArray = getEnvironment0(pid);
        if (envArray == null || envArray.length == 0) {
            return Collections.emptyMap();
        }

        if (envArray.length % 2 != 0) {
            throw new AssertionError("environment array length not even");
        }

        final Map<String, String> env = new HashMap<>(envArray.length/2);
        for (int i = 0; i < envArray.length / 2; i++) {
            env.put(envArray[i * 2], envArray[i * 2 + 1]);
        }
        return env;
    }

    public long[] getProcessCPUInfo(int pid) {
        final long[] info = new long[4];
        getProcessInfo0(pid, info);
        return info;
    }

    public long[] getProcessMemInfo(int pid) {
        final long[] info = new long[4];
        getProcessInfo0(pid, info);
        return info;
    }


    private long[] getMemoryInfo() {
        final long[] mi = new long[8];
        getGlobalMemoryStatus0(mi);
        return mi;
    }

    private long[] getPerformanceInfo() {
        final long[] mi = new long[13];
        getPerformanceInfo0(mi);
        return mi;
    }

    public long getClockTicksPerSecond() {
        return getClockTicksPerSecond0();
    }

    private static native String getHostName0(boolean prependDomain);
    private static native void getOSVersion0(long[] versionAndBuild);
    private static native int getUid0(int pid);
    private static native String getUserName0(int pid, boolean prependDomain);
    private static native String[] getEnvironment0(int pid);
    private static native boolean getGlobalMemoryStatus0(long[] info);
    private static native boolean getPerformanceInfo0(long[] info);
    private static native boolean getProcessInfo0(int pid, long[] info);
    private static native String getCPUString0();
    private static native int getCPUCount0();
    private static native long getClockTicksPerSecond0();
}
