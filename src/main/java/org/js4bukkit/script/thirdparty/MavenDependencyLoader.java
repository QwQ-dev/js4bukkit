package org.js4bukkit.script.thirdparty;

import de.leonhard.storage.Yaml;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.js4bukkit.io.config.ConfigManager;
import org.js4bukkit.script.thirdparty.objects.MavenDependency;
import org.js4bukkit.thread.Scheduler;
import org.js4bukkit.thread.enums.SchedulerExecutionMode;
import org.js4bukkit.thread.enums.SchedulerTypeEnum;
import org.js4bukkit.utils.common.text.QuickUtils;
import org.js4bukkit.utils.common.text.enums.ConsoleMessageTypeEnum;

import java.io.File;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

/**
 * Maven 依赖下载类。
 *
 * @author NaerQAQ
 * @version 1.0
 * @since 2023/12/18
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MavenDependencyLoader {
    /**
     * 下载所有 Yaml 内的依赖。
     *
     * <p>
     * 注意: 该方法使用阻塞式多线程下载，确保等待所有任务完成后再继续执行。
     * </p>
     */
    @SneakyThrows
    public static void download() {
        Yaml mavenDependencies = ConfigManager.getMavenDependencies();

        Set<String> singleLayerKeySet = mavenDependencies.singleLayerKeySet();

        int totalTasks = singleLayerKeySet.size();
        CountDownLatch latch = new CountDownLatch(totalTasks);

        // 多线程下载
        for (String key : singleLayerKeySet) {
            Runnable downloadRunnable = () -> {
                try {
                    MavenDependency mavenDependency =
                            new MavenDependency().init(mavenDependencies, key);

                    if (mavenDependency != null) {
                        download(mavenDependency);
                        MavenDependency.MAVEN_DEPENDENCIES.add(mavenDependency);
                    }
                } catch (Throwable throwable) {
                    QuickUtils.sendMessageByKey(
                            ConsoleMessageTypeEnum.ERROR,
                            "maven-dependency-download-error",
                            "<message>", throwable.getMessage()
                    );
                } finally {
                    latch.countDown();
                }
            };

            new Scheduler()
                    .setSchedulerTypeEnum(SchedulerTypeEnum.NEW_THREAD)
                    .setSchedulerExecutionMode(SchedulerExecutionMode.ASYNC)
                    .setRunnable(downloadRunnable)
                    .run();
        }

        latch.await();
    }

    /**
     * 下载指定依赖。
     *
     * @param mavenDependency 指定的 Maven 依赖对象
     */
    @SneakyThrows
    public static void download(MavenDependency mavenDependency) {
        String version = mavenDependency.getVersion();
        String groupId = mavenDependency.getGroupId();
        String artifactId = mavenDependency.getArtifactId();
        String repository = mavenDependency.getRepository();

        // 拼接
        String artifactPath =
                groupId.replace('.', '/') + "/" + artifactId + "/" +
                        version + "/" + artifactId + "-" + version + ".jar";

        List<String> strings = Arrays.asList(
                "<version>", version,
                "<groupId>", groupId,
                "<artifactId>", artifactId,
                "<repository>", repository
        );

        // 下载链接
        URL downloadUrl = new URL(
                repository + artifactPath
        );

        // sha512 下载以校验
        URL sha512Url = new URL(
                downloadUrl + ".sha512"
        );

        // 构建目标文件
        String targetFilePath =
                ThirdPartyJarLoader.THIRD_PARTY_JARS_FOLDER + "/" +
                        groupId + "/" + artifactId + "/" + version + "/" +
                        artifactId + "-" + version + ".jar";

        File targetFile = new File(targetFilePath);

        // 如果已经存在
        if (targetFile.exists()) {
            mavenDependency.setFile(targetFile);

            QuickUtils.sendMessageByKey(
                    ConsoleMessageTypeEnum.NORMAL,
                    "maven-dependency-exists",
                    strings.toArray(new String[0])
            );

            return;
        }

        QuickUtils.sendMessageByKey(
                ConsoleMessageTypeEnum.NORMAL,
                "maven-dependency-start-download",
                strings.toArray(new String[0])
        );

        // 下载
        FileUtils.copyURLToFile(
                downloadUrl, targetFile
        );

        // sha512 比较
        String expectedSha512 = IOUtils.toString(
                sha512Url, StandardCharsets.UTF_8
        );

        String actualSha512 = DigestUtils.sha512Hex(
                FileUtils.readFileToByteArray(targetFile)
        );

        boolean sha512Equals = expectedSha512.equals(actualSha512);

        if (!sha512Equals) {
            File newFile = new File(targetFile.getParent(), targetFile.getName() + "_" + actualSha512);
            Files.move(targetFile.toPath(), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            strings.add("<err_sha512>");
            strings.add(actualSha512);

            strings.add("<new_file_name>");
            strings.add(newFile.getName());
        } else {
            mavenDependency.setFile(targetFile);
        }

        QuickUtils.sendMessageByKey(
                sha512Equals ? ConsoleMessageTypeEnum.NORMAL : ConsoleMessageTypeEnum.ERROR,
                sha512Equals ? "libs-download-sha512-done" : "libs-download-sha512-error",
                strings.toArray(new String[0])
        );
    }
}
