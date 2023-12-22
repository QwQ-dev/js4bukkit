package me.naerqaq.script.objects.objects;

import de.leonhard.storage.Yaml;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import me.naerqaq.basic.interfaces.ObjectAutoInit;
import me.naerqaq.io.file.utils.IOUtils;
import me.naerqaq.script.ScriptHandler;

import java.io.File;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

/**
 * 脚本插件对象。
 *
 * @author NaerQAQ
 * @version 1.0
 * @since 2023/12/17
 */
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class ScriptPlugin implements ObjectAutoInit {
    /**
     * 文件夹。
     */
    private String folder;

    /**
     * 名称。
     */
    private String name;

    /**
     * 作者。
     */
    private String author;

    /**
     * 版本。
     */
    private String version;

    /**
     * 描述。
     */
    private String description;

    /**
     * 所有的脚本插件对象实例。
     */
    private Queue<File> scriptFiles =
            new ConcurrentLinkedQueue<>();

    /**
     * 通过指定 Yaml 文件读取指定键值属性设置对象属性。
     *
     * @param yaml    指定 Yaml 文件
     * @param yamlKey 键值
     * @return 设置完成后的对象
     */
    @Override
    public ScriptPlugin init(Yaml yaml, String yamlKey) {
        setFolder(yamlKey);

        ObjectAutoInit.super.init(yaml, yamlKey);

        // 处理本文件后返回
        return setScriptFiles(
                IOUtils.getFiles(ScriptHandler.SCRIPT_PATH + folder).stream()
                        .filter(file -> file.getName().endsWith(".js"))
                        .collect(Collectors.toCollection(ConcurrentLinkedQueue::new))
        );
    }
}
