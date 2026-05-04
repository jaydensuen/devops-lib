def call(Map config = [:]) {
    // 1. 参数定义与规范化
    def appName = config.appName ?: "unnamed-app"
    def manifestRepo = "github.com/jaydensuen/gitops-manifests.git"

    pipeline {
        agent { label 'base' } // KubeSphere 环境下的通用构建节点

        stages {
            stage('1. 环境初始化') {
                steps {
                    echo "正在启动流水线: ${appName}"
                  //  checkout scm
                }
            }

            stage('2. 模拟构建与扫描') {
                steps {
                    echo "正在执行标准化代码构建..."
                    sh "echo 'Maven/NPM Build Success'"
                }
            }

            stage('3. 模拟镜像打包') {
                steps {
                    echo "正在构建镜像并推送 (模拟模式)..."
                    sh "echo 'Docker Push Success'"
                }
            }

            stage('4. GitOps 自动回写 (核心核心核心)') {
                steps {
//                    container('base') {
                        // 使用你刚才在 Jenkins 创建的凭据 ID: github-token
                        withCredentials([string(credentialsId: 'github-token', variable: 'GITHUB_TOKEN')]) {
                            script {
                                def imageTag = "${env.BUILD_NUMBER}"

                                echo ">>> 正在更新 GitOps 配置库: ${manifestRepo}"

                                sh """
                                    # 配置 Git
                                    git config --global user.email "911634993@qq.com"
                                    git config --global user.name "jaydensuen"

                                    # 清理旧目录并克隆
                                    rm -rf tmp_manifest
                                    mkdir -p tmp_manifest && cd tmp_manifest

                                    # 使用 Token 克隆
                                    git clone https://${GITHUB_TOKEN}@${manifestRepo} .

                                    # 自动修改 deployment.yaml 中的镜像版本
                                    # 对应你之前在 gitops-manifests 里创建的文件内容
                                    sed -i "s|image: nginx:.*|image: nginx:${imageTag}|g" deployment.yaml

                                    # 提交并推送
                                    git add .
                                    git commit -m "chore: 自动化上线 - 版本更新为 ${imageTag} [skip ci]"
                                    git push origin master
                                """
                            }
                        }
                   // }
                }
            }
        }
    }
}

