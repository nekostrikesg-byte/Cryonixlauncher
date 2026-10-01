package net.kdt.pojavlaunch.multirt;

import static net.kdt.pojavlaunch.Tools.NATIVE_LIB_DIR;

import android.system.Os;
import android.util.Log;

import com.kdt.mcgui.ProgressLayout;

import com.cryonix.launcher.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.utils.FileUtils;
import net.kdt.pojavlaunch.utils.MathUtils;
import static net.kdt.pojavlaunch.utils.FileUtils.listFiles;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class MultiRTUtils {

    private static final HashMap<String,Runtime> sCache = new HashMap<>();

    private static final File RUNTIME_FOLDER = new File(Tools.MULTIRT_HOME);
    private static final String JAVA_VERSION_STR = "JAVA_VERSION=\"";
    private static final String OS_ARCH_STR = "OS_ARCH=\"";

    public static List<Runtime> getRuntimes() {
        if(!RUNTIME_FOLDER.exists() && !RUNTIME_FOLDER.mkdirs()) {
            throw new RuntimeException("Failed to create runtime directory");
        }

        ArrayList<Runtime> runtimes = new ArrayList<>();
        File[] files = RUNTIME_FOLDER.listFiles();
        if(files != null) for(File f : files) {
            runtimes.add(read(f.getName()));
        }
        else throw new RuntimeException("The runtime directory does not exist");

        return runtimes;
    }

    public static String getExactJreName(int majorVersion) {
        List<Runtime> runtimes = getRuntimes();
        for(Runtime r : runtimes)
            if(r.javaVersion == majorVersion)return r.name;

        return null;
    }

    public static String getNearestJreName(int majorVersion) {
        List<Runtime> runtimes = getRuntimes();
        MathUtils.RankedValue<Runtime> nearestRankedRuntime = MathUtils.findNearestPositive(majorVersion, runtimes, (runtime)->runtime.javaVersion);
        if(nearestRankedRuntime == null) return null;
        Runtime nearestRuntime = nearestRankedRuntime.value;
        if(nearestRuntime == null) return null;
        return nearestRuntime.name;
    }

    public static void installRuntimeNamed(String nativeLibDir, InputStream runtimeInputStream, String name) throws IOException {
        File dest = new File(RUNTIME_FOLDER,"/"+name);
        if(dest.exists()) FileUtils.deleteDirectory(dest);
        uncompressTarXZ(runtimeInputStream,dest);
        runtimeInputStream.close();
        unpack200(nativeLibDir,RUNTIME_FOLDER + "/" + name);
        ProgressLayout.clearProgress(ProgressLayout.UNPACK_RUNTIME);
        read(name);
    }

    public static void postPrepare(String name) throws IOException {
        File dest = new File(RUNTIME_FOLDER,"/" + name);
        if(!dest.exists()) return;
        Runtime runtime = read(name);
        String libFolder = "lib";
        if(new File(dest,libFolder + "/" + runtime.arch).exists()) libFolder = libFolder + "/" + runtime.arch;
        File ftIn = new File(dest, libFolder + "/libfreetype.so.6");
        File ftOut = new File(dest, libFolder + "/libfreetype.so");
        if (ftIn.exists() && (!ftOut.exists() || ftIn.length() != ftOut.length())) {
            if(!ftIn.renameTo(ftOut)) throw new IOException("Failed to rename freetype");
        }

        // Refresh libraries
        copyDummyNativeLib("libawt_xawt.so", dest, libFolder);
    }

    public static void installRuntimeNamedBinpack(InputStream universalFileInputStream, InputStream platformBinsInputStream, String name, String binpackVersion) throws IOException {
        File dest = new File(RUNTIME_FOLDER,"/"+name);
        if(dest.exists()) FileUtils.deleteDirectory(dest);
        installRuntimeNamedNoRemove(universalFileInputStream,dest);
        installRuntimeNamedNoRemove(platformBinsInputStream,dest);

        unpack200(NATIVE_LIB_DIR,RUNTIME_FOLDER + "/" + name);

        File binpack_verfile = new File(RUNTIME_FOLDER,"/"+name+"/pojav_version");
        FileOutputStream fos = new FileOutputStream(binpack_verfile);
        fos.write(binpackVersion.getBytes());
        fos.close();

        ProgressLayout.clearProgress(ProgressLayout.UNPACK_RUNTIME);

        forceReread(name);
    }


    public static String readInternalRuntimeVersion(String name) {
        File versionFile = new File(RUNTIME_FOLDER,"/" + name + "/pojav_version");
        try {
            if (versionFile.exists()) {
                return Tools.read(versionFile.getAbsolutePath());
            }else{
                return null;
            }
        }catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void removeRuntimeNamed(String name) throws IOException {
        File dest = new File(RUNTIME_FOLDER,"/"+name);
        if(dest.exists()) {
            FileUtils.deleteDirectory(dest);
            sCache.remove(name);
        }
    }

    public static File getRuntimeHome(String name) {
        File dest = new File(RUNTIME_FOLDER, name);
        Log.i("MiltiRTUitls", "Dest exists? "+dest.exists());
        if((!dest.exists()) || MultiRTUtils.forceReread(name).versionString == null) throw new RuntimeException("Selected runtime is broken!");
        return dest;
    }

    public static Runtime forceReread(String name) {
        sCache.remove(name);
        return read(name);
    }

    public static Runtime read(String name) {
        Runtime returnRuntime = sCache.get(name);
        if(returnRuntime != null) return returnRuntime;
        File release = new File(RUNTIME_FOLDER,name+"/release");
        if(!release.exists()) {
            return new Runtime(name);
        }
        try {
            String content = Tools.read(release.getAbsolutePath());
            String javaVersion = Tools.extractUntilCharacter(content, JAVA_VERSION_STR, '"');
            String osArch = Tools.extractUntilCharacter(content, OS_ARCH_STR, '"');
            if(javaVersion != null && osArch != null) {
                String[] javaVersionSplit = javaVersion.split("\\.");
                int javaVersionInt;
                if (javaVersionSplit[0].equals("1")) {
                    javaVersionInt = Integer.parseInt(javaVersionSplit[1]);
                } else {
                    javaVersionInt = Integer.parseInt(javaVersionSplit[0]);
                }
                returnRuntime = new Runtime(name, javaVersion, osArch, javaVersionInt);
            }else{
                returnRuntime =  new Runtime(name);
            }
        }catch(IOException e) {
            returnRuntime =  new Runtime(name);
        }
        sCache.put(name, returnRuntime);
        return returnRuntime;
    }

    /**
     * Unpacks all .pack files into .jar Serves only for java 8, as java 9 brought project jigsaw
     * @param nativeLibraryDir The native lib path, required to execute the unpack200 binary
     * @param runtimePath The path to the runtime to walk into
     */
    private static void unpack200(String nativeLibraryDir, String runtimePath) {

        File basePath = new File(runtimePath);
        Collection<File> files = listFiles(basePath, new String[]{"pack"}, true);

        File workdir = new File(nativeLibraryDir);

        ProcessBuilder processBuilder = new ProcessBuilder().directory(workdir);
        for(File jarFile : files){
            try{
                Process process = processBuilder.command("./libunpack200.so", "-r", jarFile.getAbsolutePath(), jarFile.getAbsolutePath().replace(".pack", "")).start();
                process.waitFor();
            }catch (InterruptedException | IOException e) {
                Log.e("MULTIRT", "Failed to unpack the runtime !");
            }
        }
    }

    @SuppressWarnings("SameParameterValue")
    private static void copyDummyNativeLib(String name, File dest, String libFolder) throws IOException {
        File fileLib = new File(dest, "/"+libFolder + "/" + name);
        FileInputStream is = new FileInputStream(new File(NATIVE_LIB_DIR, name));
        FileOutputStream os = new FileOutputStream(fileLib);
        FileUtils.copy(is, os);
        is.close();
        os.close();
    }

    private static void installRuntimeNamedNoRemove(InputStream runtimeInputStream, File dest) throws IOException {
        uncompressTarXZ(runtimeInputStream,dest);
        runtimeInputStream.close();
    }

    private static void uncompressTarXZ(final InputStream tarFileInputStream, final File dest) throws IOException {
        FileUtils.ensureDirectory(dest);
        File root = dest.getCanonicalFile();
        byte[] block = new byte[512];
        byte[] buffer = new byte[8192];
        String pendingName = null;
        String pendingLink = null;

        try (org.tukaani.xz.XZInputStream tarIn = new org.tukaani.xz.XZInputStream(tarFileInputStream)) {
            while (readTarBlock(tarIn, block)) {
                if (isZeroBlock(block)) break;

                String name = readTarString(block, 0, 100);
                String prefix = readTarString(block, 345, 155);
                if (!prefix.isEmpty()) name = prefix + "/" + name;
                String linkName = readTarString(block, 157, 100);
                long size = readTarNumber(block, 124, 12);
                int type = block[156] & 0xff;

                if (type == 'L' || type == 'K' || type == 'x' || type == 'g') {
                    byte[] metadata = readTarMetadata(tarIn, size);
                    skipTarPadding(tarIn, size);
                    if (type == 'L') pendingName = trimTarMetadata(metadata);
                    else if (type == 'K') pendingLink = trimTarMetadata(metadata);
                    else {
                        String[] pax = parsePaxMetadata(metadata);
                        if (pax[0] != null) pendingName = pax[0];
                        if (pax[1] != null) pendingLink = pax[1];
                    }
                    continue;
                }

                if (pendingName != null) name = pendingName;
                if (pendingLink != null) linkName = pendingLink;
                pendingName = null;
                pendingLink = null;

                File destPath = new File(root, name).getCanonicalFile();
                String rootPath = root.getPath();
                if (!destPath.getPath().equals(rootPath)
                        && !destPath.getPath().startsWith(rootPath + File.separator)) {
                    throw new IOException("Tar entry escapes destination: " + name);
                }
                ProgressLayout.setProgress(ProgressLayout.UNPACK_RUNTIME, 100, R.string.global_unpacking, name);
                FileUtils.ensureParentDirectory(destPath);

                if (type == '5' || name.endsWith("/")) {
                    FileUtils.ensureDirectory(destPath);
                    skipTarEntryData(tarIn, size, buffer);
                } else if (type == '2') {
                    skipTarEntryData(tarIn, size, buffer);
                    if (!destPath.exists()) {
                        try {
                            Os.symlink(linkName, destPath.getAbsolutePath());
                        } catch (Throwable e) {
                            Log.e("MultiRT", "Unable to create symlink " + destPath + " -> " + linkName, e);
                        }
                    }
                } else {
                    boolean shouldWrite = !destPath.exists() || destPath.length() != size;
                    if (shouldWrite) {
                        try (FileOutputStream output = new FileOutputStream(destPath)) {
                            copyTarBytes(tarIn, output, size, buffer);
                        }
                    } else {
                        skipTarBytes(tarIn, size, buffer);
                    }
                }
                skipTarPadding(tarIn, size);
            }
        }
    }

    private static boolean readTarBlock(InputStream input, byte[] block) throws IOException {
        int offset = 0;
        while (offset < block.length) {
            int count = input.read(block, offset, block.length - offset);
            if (count < 0) {
                if (offset == 0) return false;
                throw new IOException("Truncated TAR header");
            }
            if (count == 0) continue;
            offset += count;
        }
        return true;
    }

    private static boolean isZeroBlock(byte[] block) {
        for (byte value : block) if (value != 0) return false;
        return true;
    }

    private static String readTarString(byte[] block, int offset, int length) {
        int end = offset;
        while (end < offset + length && block[end] != 0) end++;
        return new String(block, offset, end - offset, java.nio.charset.StandardCharsets.UTF_8);
    }

    private static long readTarNumber(byte[] block, int offset, int length) throws IOException {
        if ((block[offset] & 0x80) != 0) {
            long value = block[offset] & 0x7f;
            for (int i = 1; i < length; i++) value = (value << 8) | (block[offset + i] & 0xff);
            return value;
        }
        int end = offset + length;
        int start = offset;
        while (start < end && (block[start] == 0 || block[start] == ' ')) start++;
        long value = 0;
        for (int i = start; i < end && block[i] >= '0' && block[i] <= '7'; i++) {
            value = (value << 3) + (block[i] - '0');
        }
        return value;
    }

    private static byte[] readTarMetadata(InputStream input, long size) throws IOException {
        if (size < 0 || size > 1024 * 1024) throw new IOException("Invalid TAR metadata size: " + size);
        byte[] data = new byte[(int) size];
        int offset = 0;
        while (offset < data.length) {
            int count = input.read(data, offset, data.length - offset);
            if (count < 0) throw new IOException("Truncated TAR metadata");
            if (count == 0) continue;
            offset += count;
        }
        return data;
    }

    private static String trimTarMetadata(byte[] data) {
        int end = data.length;
        while (end > 0 && (data[end - 1] == 0 || data[end - 1] == '\n' || data[end - 1] == '\r')) end--;
        return new String(data, 0, end, java.nio.charset.StandardCharsets.UTF_8);
    }

    private static String[] parsePaxMetadata(byte[] data) {
        String path = null;
        String linkPath = null;
        int offset = 0;
        while (offset < data.length) {
            int space = offset;
            while (space < data.length && data[space] != ' ') space++;
            if (space == data.length) break;
            int recordLength;
            try {
                recordLength = Integer.parseInt(new String(data, offset, space - offset,
                        java.nio.charset.StandardCharsets.US_ASCII));
            } catch (NumberFormatException e) {
                break;
            }
            int end = Math.min(data.length, offset + recordLength);
            int valueStart = space + 1;
            int equals = valueStart;
            while (equals < end && data[equals] != '=') equals++;
            if (equals < end) {
                String key = new String(data, valueStart, equals - valueStart,
                        java.nio.charset.StandardCharsets.UTF_8);
                int valueEnd = end;
                if (valueEnd > equals + 1 && data[valueEnd - 1] == '\n') valueEnd--;
                String value = new String(data, equals + 1, valueEnd - equals - 1,
                        java.nio.charset.StandardCharsets.UTF_8);
                if ("path".equals(key)) path = value;
                else if ("linkpath".equals(key)) linkPath = value;
            }
            if (recordLength <= 0) break;
            offset += recordLength;
        }
        return new String[]{path, linkPath};
    }

    private static void copyTarBytes(InputStream input, FileOutputStream output, long size, byte[] buffer) throws IOException {
        long remaining = size;
        while (remaining > 0) {
            int count = input.read(buffer, 0, (int) Math.min(buffer.length, remaining));
            if (count < 0) throw new IOException("Truncated TAR file entry");
            if (count == 0) continue;
            output.write(buffer, 0, count);
            remaining -= count;
        }
    }

    private static void skipTarBytes(InputStream input, long size, byte[] buffer) throws IOException {
        long remaining = size;
        while (remaining > 0) {
            int count = input.read(buffer, 0, (int) Math.min(buffer.length, remaining));
            if (count < 0) throw new IOException("Truncated TAR entry");
            if (count == 0) continue;
            remaining -= count;
        }
    }

    private static void skipTarEntryData(InputStream input, long size, byte[] buffer) throws IOException {
        skipTarBytes(input, size, buffer);
    }

    private static void skipTarPadding(InputStream input, long size) throws IOException {
        int padding = (int) ((512 - (size % 512)) % 512);
        while (padding > 0) {
            long skipped = input.skip(padding);
            if (skipped > 0) padding -= (int) skipped;
            else if (input.read() < 0) throw new IOException("Truncated TAR padding");
            else padding--;
        }
    }
}
