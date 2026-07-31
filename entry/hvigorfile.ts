import { hapTasks } from '@ohos/hvigor-ohos-plugin';
import { HvigorPlugin, HvigorNode } from '@ohos/hvigor';
import * as fs from 'fs';
import * as path from 'path';

function copyHapPlugin(): HvigorPlugin {
    return {
        pluginId: 'copy-hap-plugin',
        apply(node: HvigorNode) {
            // 遍历该节点上的所有任务，精准匹配包含 PackageHap 或 assembleHap 的任务
            const targetNode = node as any;

            // 💡 使用 ExecTask 注册模式
            targetNode.regExecTask && targetNode.regExecTask({
                name: 'customCopyHapTask',
                run() {
                    // 自定义执行逻辑
                }
            });

            // 💡 精准挂载到打包任务（同时兼容大小写及组合任务）
            ['PackageHap', 'packageHap', 'assembleHap'].forEach(taskName => {
                targetNode.getTaskByName(taskName)?.afterRun(() => {
                    const appName = "Chis";
                    const version = "v1.0.0";

                    const date = new Date();
                    const formattedDate = date.getFullYear() +
                        String(date.getMonth() + 1).padStart(2, '0') +
                        String(date.getDate()).padStart(2, '0') + "_" +
                        String(date.getHours()).padStart(2, '0') +
                        String(date.getMinutes()).padStart(2, '0');

                    const outputDir = path.join(node.getNodePath(), 'build/default/outputs/default');
                    const oldHapPath = path.join(outputDir, 'entry-default-unsigned.hap');

                    const newHapName = `${appName}_${version}_release_${formattedDate}.hap`;
                    const newHapPath = path.join(outputDir, newHapName);

                    if (fs.existsSync(oldHapPath)) {
                        fs.copyFileSync(oldHapPath, newHapPath);
                        console.log(`\n==================================================`);
                        console.log(`🎉 [HAP 重命名成功] -> ${newHapName}`);
                        console.log(`==================================================\n`);
                    }
                });
            });
        }
    };
}

export default {
    system: hapTasks,
    plugins: [
        copyHapPlugin()
    ]
}