/*
 * This file is part of Bisq.
 *
 * Bisq is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * Bisq is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Bisq. If not, see <http://www.gnu.org/licenses/>.
 */

package bisq.desktop_app_launcher;

import bisq.common.logging.LogSetup;
import bisq.common.util.ExceptionUtil;
import bisq.common.util.FileUtils;
import bisq.common.util.OsUtils;
import bisq.desktop_app.DesktopApp;
import ch.qos.logback.classic.Level;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * We ship the binary with the current version of the DesktopApp and with the JRE.
 * If there is a jar file found for the given version at the data directory we use that jar file to start a new
 * java process with the new jar file. Otherwise, we use the provided DesktopApp.
 * The `java.home` system property is pointing to the provided JRE from the binary.
 */
@Slf4j
public class DesktopAppLauncher {
    private static final String DEFAULT_VERSION = "2.0.0";
    private static final String JAR_PATH = "/desktop.jar";

    public static void main(String[] args) {
        try {
            List<String> jvmArgs = getJvmArgs();
            String appName = getAppName(args, jvmArgs);
            String userDataDir = OsUtils.getUserDataDir().getAbsolutePath();
            String dataDir = userDataDir + File.separator + appName;

            LogSetup.setup(Paths.get(dataDir, "bisq").toString());
            LogSetup.setLevel(Level.INFO);

            String version = getVersion(args, userDataDir);
            String jarPath = dataDir + "/jar/" + version + JAR_PATH;
            if (new File(jarPath).exists()) {
                String javaHome = System.getProperty("java.home");
                log.info("javaHome {}", javaHome);
                log.info("Jar file found at {}. Start that application in a new process.", jarPath);
                String javaBinPath = javaHome + "/bin/java";
                List<String> command = new ArrayList<>();
                command.add(javaBinPath);
                command.addAll(jvmArgs);
                command.add("-jar");
                command.add(jarPath);
                command.addAll(Arrays.asList(args));
                ProcessBuilder processBuilder = new ProcessBuilder(command);
                processBuilder.environment().put("JAVA_HOME", javaHome);
                processBuilder.inheritIO();
                Process process = processBuilder.start();
                int exitCode = process.waitFor();
                log.info("Exited with code: {}", exitCode);
            } else {
                log.info("No jar file found at {}. Run default application.", jarPath);
                DesktopApp.main(args);
            }
        } catch (Exception e) {
            log.error("Error at launch: {}", ExceptionUtil.print(e));
        }
    }

    private static String getVersion(String[] args, String userDataDir) {
        Optional<String> versionFromArgs = getVersion(args);
        String versionFilePath = userDataDir + File.separator + "version.txt";
        return FileUtils.readFromFileIfPresent(new File(versionFilePath))
                .or(() -> versionFromArgs)
                .orElse(DEFAULT_VERSION);
    }

    private static Optional<String> getVersion(String[] args) {
        return Stream.of(args)
                .filter(e -> e.startsWith("--version="))
                .map(e -> e.replace("--version=", ""))
                .findAny();
    }

    private static String getAppName(String[] args, List<String> jvmArgs) {
        return jvmArgs.stream()
                .filter(e -> e.startsWith("-Dapplication.appName="))
                .map(e -> e.replace("-Dapplication.appName=", ""))
                .findAny()
                .or(() -> Arrays.stream(args)
                        .filter(e -> e.startsWith("--appName="))
                        .map(e -> e.replace("--appName=", ""))
                        .findAny())
                .orElse("Bisq2");
    }

    private static List<String> getJvmArgs() {
        return ManagementFactory.getRuntimeMXBean().getInputArguments().stream()
                .filter(e -> e.startsWith("-Dapplication"))
                .collect(Collectors.toList());
    }
}