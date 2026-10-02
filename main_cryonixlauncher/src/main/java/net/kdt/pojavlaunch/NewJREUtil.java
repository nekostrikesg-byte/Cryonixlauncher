package net.kdt.pojavlaunch;

import com.cryonix.launcher.R;
import com.kdt.mcgui.ProgressLayout;

import static net.kdt.pojavlaunch.Architecture.archAsString;

import android.app.Activity;
import android.content.res.AssetManager;
import android.util.Log;

import net.kdt.pojavlaunch.multirt.MultiRTUtils;
import net.kdt.pojavlaunch.multirt.Runtime;
import net.kdt.pojavlaunch.utils.MathUtils;
import net.kdt.pojavlaunch.value.launcherprofiles.LauncherProfiles;
import net.kdt.pojavlaunch.value.launcherprofiles.MinecraftProfile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import net.kdt.pojavlaunch.utils.DownloadUtils;
import net.kdt.pojavlaunch.utils.FileUtils;

public class NewJREUtil {
    private static boolean checkInternalRuntime(AssetManager assetManager, InternalRuntime internalRuntime) {
        String installedRuntimeVersion = MultiRTUtils.readInternalRuntimeVersion(internalRuntime.name);
        boolean installedRuntimeValid = installedRuntimeVersion != null
                && MultiRTUtils.forceReread(internalRuntime.name).javaVersion >= internalRuntime.majorVersion;
        String bundledRuntimeVersion;
        try {
            bundledRuntimeVersion = Tools.read(assetManager.open(internalRuntime.path + "/version"));
        } catch (IOException missingBundledRuntime) {
            // Runtime archives are intentionally excluded from the APK to keep it small. An
            // existing runtime is still valid; otherwise fetch the matching runtime on demand.
            if (installedRuntimeValid) return true;
            return downloadAndInstallRuntime(internalRuntime);
        }

        if (bundledRuntimeVersion.equals(installedRuntimeVersion) && installedRuntimeValid) return true;
        if (unpackInternalRuntime(assetManager, internalRuntime, bundledRuntimeVersion)) return true;

        // A partial/mismatched bundled runtime should not make supported game versions
        // unlaunchable when the official rolling runtime archive is available.
        return downloadAndInstallRuntime(internalRuntime);
    }

    private static boolean downloadAndInstallRuntime(InternalRuntime internalRuntime) {
        String archiveName = "jre" + internalRuntime.majorVersion + "-pojav.zip";
        String archiveUrl = "https://github.com/MojoLauncher/android-openjdk-build-17-25"
                + "/releases/download/rolling/" + archiveName;
        File archive = new File(Tools.DIR_CACHE, "runtimes/" + archiveName);
        try {
            File parent = archive.getParentFile();
            if (parent == null) throw new IOException("Unable to create JRE download directory");
            FileUtils.ensureDirectory(parent);

            ProgressLayout.setProgress(ProgressLayout.DOWNLOAD_MINECRAFT, 0,
                    R.string.newdl_downloading_metadata, archiveName);
            DownloadUtils.downloadFile(archiveUrl, archive);

            try (ZipFile runtimeZip = new ZipFile(archive)) {
                ZipEntry universal = runtimeZip.getEntry("universal.tar.xz");
                ZipEntry platform = runtimeZip.getEntry("bin-" + archAsString(Tools.DEVICE_ARCHITECTURE) + ".tar.xz");
                if (universal == null || platform == null) {
                    throw new IOException("The Java " + internalRuntime.majorVersion
                            + " archive does not contain the files for " + archAsString(Tools.DEVICE_ARCHITECTURE));
                }

                try (InputStream universalStream = runtimeZip.getInputStream(universal);
                     InputStream platformStream = runtimeZip.getInputStream(platform)) {
                    MultiRTUtils.installRuntimeNamedBinpack(universalStream, platformStream,
                            internalRuntime.name, "mojo-openjdk-" + internalRuntime.majorVersion + "-rolling");
                }
            }

            MultiRTUtils.postPrepare(internalRuntime.name);
            Runtime installedRuntime = MultiRTUtils.forceReread(internalRuntime.name);
            if (installedRuntime.javaVersion < internalRuntime.majorVersion) {
                MultiRTUtils.removeRuntimeNamed(internalRuntime.name);
                throw new IOException("Downloaded runtime does not provide Java " + internalRuntime.majorVersion);
            }
            Log.i("NewJREAuto", "Installed Java " + internalRuntime.majorVersion + " from " + archiveUrl);
            return true;
        } catch (IOException | RuntimeException e) {
            Log.e("NewJREAuto", "Unable to download or install Java " + internalRuntime.majorVersion, e);
            try {
                MultiRTUtils.removeRuntimeNamed(internalRuntime.name);
            } catch (IOException cleanupError) {
                Log.w("NewJREAuto", "Unable to remove incomplete runtime", cleanupError);
            }
            return false;
        } finally {
            if (archive.exists() && !archive.delete()) {
                Log.w("NewJREAuto", "Unable to remove downloaded runtime archive " + archive);
            }
        }
    }

    private static boolean unpackInternalRuntime(AssetManager assetManager, InternalRuntime internalRuntime, String version) {
        try {
            MultiRTUtils.installRuntimeNamedBinpack(
                    assetManager.open(internalRuntime.path+"/universal.tar.xz"),
                    assetManager.open(internalRuntime.path+"/bin-" + archAsString(Tools.DEVICE_ARCHITECTURE) + ".tar.xz"),
                    internalRuntime.name, version);
            MultiRTUtils.postPrepare(internalRuntime.name);
            return true;
        }catch (IOException e) {
            Log.e("NewJREAuto", "Internal JRE unpack failed", e);
            return false;
        }
    }

    private static InternalRuntime getInternalRuntime(Runtime runtime) {
        for(InternalRuntime internalRuntime : InternalRuntime.values()) {
            if(internalRuntime.name.equals(runtime.name)) return internalRuntime;
        }
        return null;
    }

    private static MathUtils.RankedValue<Runtime> getNearestInstalledRuntime(int targetVersion) {
        List<Runtime> runtimes = MultiRTUtils.getRuntimes();
        return MathUtils.findNearestPositive(targetVersion, runtimes, (runtime)->runtime.javaVersion);
    }

    private static MathUtils.RankedValue<InternalRuntime> getNearestInternalRuntime(int targetVersion) {
        List<InternalRuntime> runtimeList = Arrays.asList(InternalRuntime.values());
        return MathUtils.findNearestPositive(targetVersion, runtimeList, (runtime)->runtime.majorVersion);
    }


    /** @return true if everything is good, false otherwise.  */
    public static boolean installNewJreIfNeeded(Activity activity, JMinecraftVersionList.Version versionInfo) {
        //Now we have the reliable information to check if our runtime settings are good enough
        if (versionInfo.javaVersion == null || versionInfo.javaVersion.component.equalsIgnoreCase("jre-legacy"))
            return true;

        int gameRequiredVersion = versionInfo.javaVersion.majorVersion;

        LauncherProfiles.load();
        AssetManager assetManager = activity.getAssets();
        MinecraftProfile minecraftProfile = LauncherProfiles.getCurrentProfile();
        String profileRuntime = Tools.getSelectedRuntime(minecraftProfile);
        Runtime runtime = MultiRTUtils.read(profileRuntime);
        // Partly trust the user with his own selection, if the game can even try to run in this case
        if (runtime.javaVersion >= gameRequiredVersion) {
            // Check whether the selection is an internal runtime
            InternalRuntime internalRuntime = getInternalRuntime(runtime);
            // If it is, check if updates are available from the APK file
            if(internalRuntime != null) {
                // Not calling showRuntimeFail on failure here because we did, technically, find the compatible runtime
                return checkInternalRuntime(assetManager, internalRuntime);
            }
            return true;
        }

        // If the runtime version selected by the user is not appropriate for this version (which means the game won't run at all)
        // automatically pick from either an already installed runtime, or a runtime packed with the launcher
        MathUtils.RankedValue<?> nearestInstalledRuntime = getNearestInstalledRuntime(gameRequiredVersion);
        MathUtils.RankedValue<?> nearestInternalRuntime = getNearestInternalRuntime(gameRequiredVersion);

        MathUtils.RankedValue<?> selectedRankedRuntime = MathUtils.objectMin(
                nearestInternalRuntime, nearestInstalledRuntime, (value)->value.rank
        );

        // No possible selections
        if(selectedRankedRuntime == null) {
            showRuntimeFail(activity, versionInfo);
            return false;
        }

        Object selected = selectedRankedRuntime.value;
        String appropriateRuntime;
        InternalRuntime internalRuntime;

        // Perform checks on the picked runtime
        if(selected instanceof Runtime) {
            // If it's an already installed runtime, save its name and check if
            // it's actually an internal one (just in case)
            Runtime selectedRuntime = (Runtime) selected;
            appropriateRuntime = selectedRuntime.name;
            internalRuntime = getInternalRuntime(selectedRuntime);
        } else if (selected instanceof InternalRuntime) {
            // If it's an internal runtime, set it's name as the appropriate one.
            internalRuntime = (InternalRuntime) selected;
            appropriateRuntime = internalRuntime.name;
        } else {
            throw new RuntimeException("Unexpected type of selected: "+selected.getClass().getName());
        }

        // If it turns out the selected runtime is actually an internal one, attempt automatic installation or update
        if(internalRuntime != null && !checkInternalRuntime(assetManager, internalRuntime)) {
            // Not calling showRuntimeFail here because we did, technically, find the compatible runtime
            return false;
        }

        minecraftProfile.javaDir = Tools.LAUNCHERPROFILES_RTPREFIX + appropriateRuntime;
        LauncherProfiles.write();
        return true;
    }

    private static void showRuntimeFail(Activity activity, JMinecraftVersionList.Version verInfo) {
        Tools.dialogOnUiThread(activity, activity.getString(R.string.global_error),
                activity.getString(R.string.multirt_nocompatiblert, verInfo.javaVersion.majorVersion));
    }

    private enum InternalRuntime {
        JRE_17(17, "Internal-17", "components/jre-new"),
        JRE_21(21, "Internal-21", "components/jre-21");
        public final int majorVersion;
        public final String name;
        public final String path;
        InternalRuntime(int majorVersion, String name, String path) {
            this.majorVersion = majorVersion;
            this.name = name;
            this.path = path;
        }
    }

}