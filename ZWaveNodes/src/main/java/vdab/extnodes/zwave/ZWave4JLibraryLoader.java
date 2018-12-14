package vdab.extnodes.zwave;

import java.io.*;

import com.lcrc.af.AnalysisObject;

/**
 * @author zagumennikov
 */

public class ZWave4JLibraryLoader {

    public static final String NATIVE_LIBS_DIRECTORY_NAME = "native_libs";
    public static final String WINDOWS_DIRECTORY_NAME = "windows";
    public static final String LINUX_DIRECTORY_NAME = "linux";
    public static final String SOLARIS_DIRECTORY_NAME = "solaris";
    public static final String OSX_DIRECTORY_NAME = "osx";
    public static final String X86_DIRECTORY_NAME = "x86";
    public static final String AMD64_DIRECTORY_NAME = "amd64";
    public static final String ARM_DIRECTORY_NAME = "arm-v7";

    private static final String TEMP_FILE_PREFIX = "native-lib-";

    public static void loadLibrary(AnalysisObject ao, String libraryName, Class clazz) {
         String libraryPath = getLibraryPath(libraryName);
        ao.logInfo("NativeLibraryLoader: Attempting to load the native library PATH="+libraryPath);

        File tempLibraryFile;
        try (InputStream libraryStream = clazz != null ? clazz.getResourceAsStream(libraryPath) : ClassLoader.getSystemResourceAsStream(libraryPath)) {
            if (libraryStream == null) {
                throw new RuntimeException(String.format("Library not found %s", libraryPath));
            }

            tempLibraryFile = File.createTempFile(TEMP_FILE_PREFIX, null);
            tempLibraryFile.deleteOnExit();

            try (OutputStream tempLibraryStream = new FileOutputStream(tempLibraryFile)) {
                int len;
                byte[] buffer = new byte[8192];
                while ((len = libraryStream.read(buffer)) > -1) {
                    tempLibraryStream.write(buffer, 0, len);
                }
            }
        } catch (Throwable e) {
            ao.setError("NativeLibraryLoader: Unable to load native library PATH="+libraryPath);
            throw new RuntimeException(e);
        }

        System.load(tempLibraryFile.getAbsolutePath());
        ao.logInfo("NativeLibraryLoader: Load completed PATH="+libraryPath);
        tempLibraryFile.delete();
    }

    private static String getLibraryPath(String libraryName) {
        StringBuilder libraryPathBuilder = new StringBuilder("/" + NATIVE_LIBS_DIRECTORY_NAME + "/");

        String osName = System.getProperty("os.name");
        if (isLinux(osName)) {
            libraryPathBuilder.append(LINUX_DIRECTORY_NAME);
        } else if (isWindows(osName)) {
            libraryPathBuilder.append(WINDOWS_DIRECTORY_NAME);
        } else if (isSunOS(osName)) {
            libraryPathBuilder.append(SOLARIS_DIRECTORY_NAME);
        } else if (isOSX(osName)) {
            libraryPathBuilder.append(OSX_DIRECTORY_NAME);
        }

        libraryPathBuilder.append('/');

        String architecture = System.getProperty("os.arch");
        if (isX86(architecture)) {
            libraryPathBuilder.append(X86_DIRECTORY_NAME);
        } else if (isAmd64(architecture)) {
            libraryPathBuilder.append(AMD64_DIRECTORY_NAME);
        } else if (isArm(architecture)) {
            libraryPathBuilder.append(ARM_DIRECTORY_NAME);
        }

        libraryPathBuilder.append('/').append(System.mapLibraryName(libraryName));
        return libraryPathBuilder.toString();
    }

    private static boolean isLinux(String osName) {
        return osName.equals("Linux");
    }

    private static boolean isWindows(String osName) {
        return osName.startsWith("Windows");
    }

    private static boolean isSunOS(String osName) {
        return osName.equals("SunOS");
    }

    private static boolean isOSX(String osName) {
        return osName.endsWith("OS X");
    }

    private static boolean isX86(String architecture) {
        return architecture.endsWith("86");
    }

    private static boolean isArm(String architecture) {
        return architecture.equals("arm");
    }

    private static boolean isAmd64(String architecture) {
        return architecture.equals("amd64") || architecture.equals("x86_64");
    }
}
